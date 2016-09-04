/* 
 * File:   main.cpp
 * Author: Sergio
 *
 * Created on 5 de enero de 2013, 23:14
 */
#include "config.h"
#include "log.h"
#include "rtp.h"
#include "rtpsession.h"
#include <string.h>


class Sender : public RTPSession::Listener
{
public:
	Sender()
	{
		//Set default values
		ip = NULL;
		port = 0;
		localPort = 0;
		rate = 512000;
		rateMin = 0;
		rateLimit = 0;
		fps = 30;
		gop = 300;
		maxRTPSize = 1300;
		scaleIP = 1;
		sending = false;
		limitCount = 0;
	}

	virtual void onFPURequested(RTPSession *session)
	{
		Log("-onFPURequested\n");
	}
	
	virtual void onReceiverEstimatedMaxBitrate(RTPSession *session,DWORD bitrate)
	{
		Log("-onReceiverEstimatedMaxBitrate [bitrate:%d]\n",bitrate);
		setRateLimit(bitrate);
	}

	virtual void onTempMaxMediaStreamBitrateRequest(RTPSession *session,DWORD bitrate,DWORD overhead)
	{
		Log("-onTempMaxMediaStreamBitrateRequest [bitrate:%d,overhead:%d]\n",bitrate,overhead);
		setRateLimit(bitrate+overhead);
		limitCount = fps;
		session->SendTempMaxMediaStreamBitrateNotification(bitrate,overhead);
	}

	bool Start()
	{
		//Check params
		if (!ip)
			return Error("Destination IP not set\n");
		if (!port)
			return Error("Destination port not set\n");

		//Check if no min rate has been set
		if (!rateMin)
			//Set to max, so no variability
			rateMin = rate;

		//Ensure rate constrinst
		if (rateMin>rate)
			return Error("Rate [%d] has to be bigger than the min rate [%d]\n",rate,rateMin);

		//We are sending
		sending = 1;
		
		//Start thread
		return pthread_create(&thread,NULL,run,this)==0;
	}

	bool Stop()
	{
		//Stop
		sending = 0;
		//Wait for sending thread
		pthread_join(thread,NULL);
		//Ok
		return true;
	}

	void setSending(bool sending)		{ this->sending = sending;		}
        void setScaleIP(DWORD scaleIP)		{ this->scaleIP = scaleIP;		}
        void setMaxRTPSize(DWORD maxRTPSize)	{ this->maxRTPSize = maxRTPSize;        }
        void setGop(DWORD gop)			{ this->gop = gop;			}
        void setFps(DWORD fps)			{ this->fps = fps;			}
        void setRate(DWORD rate)		{ this->rate = rate;			}
	void setRateMin(DWORD rate)		{ this->rateMin = rate;			}
	void setRateLimit(DWORD rate)		{ this->rateLimit = rate;		}
        void setPort(int port)			{ this->port = port;			}
	void setLocalPort(int port)		{ this->localPort = port;		}
        void setIp(char* ip)			{ this->ip = ip;			}
	void IncreasRate()			{ ChangeRate(rate*1.10);		}
	void DecreaseRate()			{ ChangeRate(rate*0.90);		}
	void ChangeRate(DWORD bitrate)
	{
		//Calculate new min based on the new rate
		rateMin = ((QWORD)bitrate)*rateMin/rate;
		//Set rate
		rate = bitrate;
		//Log
		Log("-New bitrate [rate:%d,min:%d]\n",rate,rateMin);
	}
private:
	static void * run(void *par)
	{
		Sender *sender = (Sender *)par;
		sender->Run();
		pthread_exit(0);
	}
	
protected:
	void Run()
	{
		timeval ini;

		//RTP objects
		RTPSession sess(MediaFrame::Video,this);
		RTPPacket rtp(MediaFrame::Video,96);
		RTPMap map;
		RTPSession::Properties prop;
		Acumulator fpsAcu(1000);
		Acumulator rateAcu(1000);

		//Set local port
		sess.SetLocalPort(localPort);
		
		//Init session
		sess.Init();

		//Enable rtcp-mux and nack
		prop["rtcp-mux"] = "1";
		prop["useNACK"] = "1";
		
		//Add properties
		sess.SetProperties(prop);

		//Set rtp map
		map[96] = 96;
		//Set map
		sess.SetSendingRTPMap(map);

		//Set codec
		sess.SetSendingCodec(96);

		//Set receiver ip and port
		sess.SetRemotePort(ip,port);

		//Set packet clock rate
		rtp.SetClockRate(90000);

		//Initialize rtp values
		DWORD num = 0;
		QWORD ts = 0;
		DWORD seq = 0;

		//Get start time
		getUpdDifTime(&ini);

		//Set initial bitrate
		int current = rate/2;

		//Until ctrl-c is pressed
		while(sending)
		{
			//Calculate target bitrate
			int target = current;

			//Check temporal limits
			if (rateAcu.IsInWindow())
			{
				//Get real sent bitrate during last second and convert to kbits (*1000/1000)
				DWORD instant = rateAcu.GetInstantAvg();
				//Check if are not in quarentine period or sending below limits
				if (!limitCount || instant<rateLimit )
					//Increase a 8% each second
					target += fmax(target*0.08,10000)/fps+1;
				else
					//Calculate decrease rate and apply it
					target = rateLimit;
			}

			//Check limits counter
			if (limitCount>0)
				//One frame less of limit
				limitCount--;

			//check max bitrate
			if (target>rate)
				//Set limit to max bitrate
				target = rate;

			//Calculate min rate
			int targetMin = ((QWORD)target)*rateMin/rate;

			//Calculate frame
			DWORD frameSize = (gop+scaleIP-1)*target/(8*fps*gop);
			//Calculate min frame size
			DWORD frameSizeMin = (gop+scaleIP-1)*targetMin/(8*fps*gop);
			//Set P frame size
			DWORD size = frameSizeMin + ((QWORD)(frameSize-frameSizeMin))*rand()/RAND_MAX;

			Log("-%d sent frames [instant:%llf,target:%d,targetMin:%d,current:%d,rate:%d,rateMin:%d,rateLimit:%d]\n",num,rateAcu.GetInstantAvg(),target,targetMin,current,rate,rateMin,rateLimit);

			//Upate current
			current = target;
			
			//Check if its the firs frame of gop
			if (!(num % gop))
			{
				//Set I frame size
				size = frameSize*scaleIP;
				//If not first
				if (num)
					//Log it
					Log("-%d sent frames [rate max:%llf,rate min:%llf,fps max:%lld,fps min:%lld]\n",num,rateAcu.GetMaxAvg(),rateAcu.GetMinAvg(),fpsAcu.GetMax(),fpsAcu.GetMin());
				//Reset stats
				rateAcu.ResetMinMax();
				fpsAcu.ResetMinMax();
			}

			//New frame
			fpsAcu.Update(getTime()/1000,1);
			
			//Set timestamp
			rtp.SetTimestamp(ts);
			//ReSet mark
			rtp.SetMark(false);
			//Packetize
			while(size)
			{
				//Get size
				DWORD len = size;
				//Check if too much
				if (len>maxRTPSize)
					//Limit it
					len = maxRTPSize;
				//Set packet length, content is random
				rtp.SetMediaLength(len);
				//Increase seq number
				rtp.SetSeqNum(seq++);
				//Decrease size to send
				size -= len;
				//If it is last
				if (!size)
					//Set mark
					rtp.SetMark(true);
				//Send it
				sess.SendPacket(rtp);
				//Acumulate
				rateAcu.Update(getTime()/1000,rtp.GetSize()*8);
			}

			//Increase num of frames
			num++;
			//Increase timestamp for next one
			ts+=90000/fps;

			//Calculate sleep time until next frame
			QWORD diff = ts*1000/90-getDifTime(&ini);
			
			//And sleep
			msleep(diff);
		}

		//End it
		sess.End();

		//Exit
		pthread_exit(0);
	}
private:
	pthread_t thread;
	char* ip;
	int   port;
	int   localPort;
	DWORD rate;
	DWORD rateMin;
	DWORD rateLimit;
	DWORD fps;
	DWORD gop;
	DWORD maxRTPSize;
	DWORD scaleIP;
	int   limitCount;
	bool  sending;
};

int main(int argc, char** argv)
{
	//Create sender object
	Sender sender;

	//Get all parameters
	for(int i=1;i<argc;i++)
	{
		//Check options
		if (strcmp(argv[i],"-h")==0 || strcmp(argv[i],"--help")==0)
		{
			//Show usage
			printf("Usage: sender [-h] [--smooth] [--ip ip] [--port port] [--local-port port [--rate bps] [--rate-min bps] [--gop-size frames] [--fps fps] [--rtp-max-size size] [--IP-scale scale]\r\n\r\n"
				"Options:\r\n"
				" -h,--help        Print help\r\n"
				" --ip             Destination IP\r\n"
				" --port           Destination port\r\n"
				" --local-port     Local port\r\n"
				" --rate           Initial bitrate\r\n"
				" --rate-min       Minimum bitrate, target bitrate will then randomly in [min-rate,rate]\r\n"
				" --gop-size       GOP size in number of frames\r\n"
				" --fps            Frames per second\r\n"
				" --rtp-max-size   Max RTP packet size\r\n"
				" --IP-scale       I frame size vs P frame size\r\n"
				" --smooth         Smooth sending of rtp packets of a frame (Traffic shaping)\r\n");
			//Exit
			return 0;
		} else if (strcmp(argv[i],"--smooth")==0) {
			//Nothing yet
		} else if (strcmp(argv[i],"--ip")==0 && (i+1<argc)) {
			//Get ip
			sender.setIp(argv[++i]);
		} else if (strcmp(argv[i],"--port")==0 && (i+1<argc)) {
			//Get port
			sender.setPort(atoi(argv[++i]));
		} else if (strcmp(argv[i],"--local-port")==0 && (i+1<argc)) {
			//Get port
			sender.setLocalPort(atoi(argv[++i]));
		} else if (strcmp(argv[i],"--rate")==0 && (i+1<argc)) {
			//Get rate
			sender.setRate(atoi(argv[++i]));
		} else if (strcmp(argv[i],"--rate-min")==0 && (i+1<argc)) {
			//Get rate
			sender.setRateMin(atoi(argv[++i]));
		} else if (strcmp(argv[i],"--gop-size")==0 && (i+1<argc)) {
			//Get gop size
			sender.setGop(atoi(argv[++i]));
		} else if (strcmp(argv[i],"--fps")==0 && (i+1<argc)) {
			//Get fps
			sender.setFps(atoi(argv[++i]));
		} else if (strcmp(argv[i],"--rtp-max-size")==0 && (i+1<argc)) {
			//Get rtmp port
			sender.setMaxRTPSize(atoi(argv[++i]));
		} else if (strcmp(argv[i],"--IP-scale")==0 && (i+1<=argc)) {
			//Get rtmp port
			sender.setScaleIP(atoi(argv[++i]));
		} else {
			Error("Unknown parameter [%s]\n",argv[i]);
		}
	}

	//Start
	if (!sender.Start())
		//Error
		return -1;
	
	//We are sending
	printf("Sending... press [q] to stop\n");
	//Get char
	char c=getchar();
	//Read until q is pressed
	while (c!='q')
	{
		switch(c)
		{
			case 'i':
				//Increase sending rate
				sender.IncreasRate();
				break;
			case 'd':
				//Decrease sending rate
				sender.DecreaseRate();
				break;
		}
		//Get char
		c = getchar();
	}

	//Stop
	sender.Stop();

	//OK
	return 0;
}


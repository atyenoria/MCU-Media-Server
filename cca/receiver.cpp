/* 
 * File:   receiver.cpp
 * Author: Sergio
 *
 * Created on 16 de enero de 2013, 10:41
 */
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


class Receiver
{
public:
	Receiver() : sess(MediaFrame::Video,NULL)
	{
		//Set default values
		port = 0;
		receiving = false;
	}

	bool Start()
	{
		RTPMap map;
		RTPSession::Properties prop;

		//Check params
		if (!port)
			return Error("Local port not set\n");

		//Set local port range
		sess.SetLocalPort(port);
		
		//Init
		if (!sess.Init())
			return Error("Error initing rtp session\n");

		//Enable rtcp-mux and nack
		prop["rtcp-mux"] = "1";
		prop["useNACK"] = "1";
		
		//Add properties
		sess.SetProperties(prop);
		
		//Set rtp map
		map[96] = 96;
		//Set map
		sess.SetReceivingRTPMap(map);

		//Set receiver ip and port to NAT
		sess.SetRemotePort((char*)"0.0.0.0",0);
		
		//We are receiving
		receiving = 1;

		//Start thread
		return pthread_create(&thread,NULL,run,this)==0;
	}

	bool Stop()
	{

		//Stop
		receiving = 0;
		//Cancel any pending grab
		sess.CancelGetPacket();
		//Wait for sending thread
		pthread_join(thread,NULL);
		//End it
		sess.End();

		//Ok
		return true;
	}

	void setPort(int port)			{ this->port = port;			}
        
private:
	static void * run(void *par)
	{
		Receiver *recv = (Receiver *)par;
		recv->Run();
		pthread_exit(0);
	}

protected:
	void Run()
	{
		DWORD lastSeq = 0;

		//Until ctrl-c is pressed
		while(receiving)
		{
			//Get next RTP packet
			RTPPacket* rtp = sess.GetPacket();

			//Get seq num
			DWORD seq = rtp->GetExtSeqNum();

			//Calculate lost count
			if (lastSeq!=0 && seq!=lastSeq+1)
				//Print count
				Log("-%d lost packets\n",lastSeq-seq-1);
			//Update
			lastSeq = seq;

			//free it
			delete(rtp);
		}


		//Exit
		pthread_exit(0);
	}
private:
	pthread_t thread;
	RTPSession sess;
	int   port;
	bool  receiving;
};

int main(int argc, char** argv)
{
	char c;
	//Create sender object
	Receiver recv;

	//Get all parameters
	for(int i=1;i<argc;i++)
	{
		//Check options
		if (strcmp(argv[i],"-h")==0 || strcmp(argv[i],"--help")==0)
		{
			//Show usage
			printf("Usage: receiver [-h] [--port port] ]\r\n\r\n"
				"Options:\r\n"
				" -h,--help        Print help\r\n"
				" --local-port     Local port\r\n"
				);
			//Exit
			return 0;
		} else if (strcmp(argv[i],"--local-port")==0 && (i+1<argc)) {
			//Get port
			recv.setPort(atoi(argv[++i]));
		} else {
			Error("Unknown parameter [%s]\n",argv[i]);
		}
	}

	//Start
	if (!recv.Start())
		//Error
		return -1;

	//We are sending
	printf("Receiving... press [q] to stop\n");
	
	//Read until q is pressed
	while ((c=getchar())!='q');

	//Stop
	recv.Stop();

	//OK
	return 0;
}


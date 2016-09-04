package org.murillo.mscontrol;

import java.util.HashMap;
import java.util.Map;

import javax.media.mscontrol.Parameter;
import javax.media.mscontrol.Parameters;

/**
 * 
 * @author amit bhayani
 * 
 */
public class ParametersImpl extends HashMap<Parameter, Object> implements Parameters {

    public static final ParametersImpl sanetize(Parameters params){
	    if (params==null)
		    return new ParametersImpl();
	    else if (params==Parameters.NO_PARAMETER)
		     return new ParametersImpl();
	    else
		    return new ParametersImpl(params);
    }
	
    public boolean hasParameter(Parameter key) {
        return containsKey(key);
    }
    
    public Object getParameter(Parameter key) {
        return get(key);
    }
    
    public ParametersImpl() {
	    
    }
    
    public ParametersImpl(Parameters params) {
	    if (params!=null && params!=Parameters.NO_PARAMETER)
		    for (Map.Entry<Parameter,Object> entry : params.entrySet())
			    put(entry.getKey(), entry.getValue());
    }
    
    public ParametersImpl clone() {
	    return new ParametersImpl(this);
    }

    public Integer getIntParameter(Parameter key,Integer defaultValue) {
	Integer value;
	//Try to conver ti
	try { value = (Integer)getParameter(key); } catch (Exception e) { value = defaultValue; }
	//Check if found
	if (value==null) return defaultValue;
	//return converted or default
        return value;
    }
    
    public Boolean getBooleanParameter(Parameter key,Boolean defaultValue) {
	Boolean value;
	//Try to conver it
	try { value = (Boolean)getParameter(key); } catch (Exception e) {  value = defaultValue; }
	//Check if found
	if (value==null) return defaultValue;
	//return converted or default
        return value;
    }
    
    
    public String getStringParameter(Parameter key,String defaultValue) {
	String value;
	//Try to conver it
	try { value = (String)getParameter(key); } catch (Exception e) {  value = defaultValue; }
	//Check if found
	if (value==null) return defaultValue;
	//return converted or default
        return value;
    }

}

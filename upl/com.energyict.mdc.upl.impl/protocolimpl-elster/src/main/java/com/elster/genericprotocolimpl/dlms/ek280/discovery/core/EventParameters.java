package com.elster.genericprotocolimpl.dlms.ek280.discovery.core;

public class EventParameters extends Parameters {

    private String event = null;

    /**
     * Creates a new instance of RequestParameter
     */
    public EventParameters(String request) {
        super(request, EVENT_PARAMS);
        parse();
    }

    protected String doToString() {
    	return " event="+getEvent();
    }
    
    protected void doParse() {
        event = getStringAttribute("event");
    }

    public String toString() {
        return "dbaseId=" + getDbaseId() + ", ip=" + getIp() + ", serialId=" + getSerialId() + ", ipAddress=" + getIpAddress() + ", type=" + getType() + ", comPort=" + getComPort() + ", meterType=" + getMeterType() + ", event=" + getEvent();
    }

    public static void main(String[] strs) {

    	EventParameters rp = new EventParameters("<EVENT>meterType=AS230,serialId=12345,dbaseId=5,ipAddress=192.168.0.1,event=10000101100110</EVENT>");
        System.out.println(rp);
    }

	public String getEvent() {
		return event;
	}

} // public class DeployParameters extends Parameters

package com.elster.genericprotocolimpl.dlms.ek280.discovery.core;

public class EventPOParameters extends Parameters {

    private String event = null;

    /**
     * Creates a new instance of RequestParameter
     */
    public EventPOParameters(String request) {
        super(request, EVENTPO_PARAMS);
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

    	EventPOParameters rp = new EventPOParameters("<EVENTPO>meterType=AS230,serialId=12345,dbaseId=5,ipAddress=192.168.0.1,event=10000101100110</EVENTPO>");
        System.out.println(rp);
    }

	public String getEvent() {
		return event;
	}

} // public class EventPOParameters extends Parameters

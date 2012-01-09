/*
 * Parameters.java
 *
 * Created on 24 februari 2004, 10:38
 */

package com.elster.genericprotocolimpl.dlms.ek280.discovery.core;

import java.util.*;


/**
 * @author Koen
 */
abstract public class Parameters {

    String str;
    int type;
    static public final int RESPONSE_PARAMS = 0;
    static public final int ERROR_PARAMS = 1;
    static public final int REQUEST_PARAMS = 2;
    static public final int DEPLOY_PARAMS = 3;
    static public final int EVENT_PARAMS = 4;
    static public final int EVENTPO_PARAMS = 5;

    abstract protected void doParse();
    abstract protected String doToString();

    int dbaseId = -1;
    String ip = null;
    String serialId = null;
    private String comPort = null;
    private String ipAddress = null;
    private String meterType = null;
    private int ipPort = 80;
    
    List<Attribute> unknownAttributes = new ArrayList();
    
    
    /**
     * Creates a new instance of Parameters
     */
    public Parameters(String str, int type) {
        this.str = str;
        this.type = type;
    }

    public String toString() {
        return "dbaseId=" + getDbaseId() + ", ip=" + getIp() + ", serialId=" + getSerialId() + ", ipAddress=" + getIpAddress()+ ", ipPort=" + getIpPort() + ", type=" + getType() + ", comPort=" + getComPort() + ", meterType=" + getMeterType()+doToString();
    }
    

    private void buildAttributeList() {
    	String temp="";
    	int beginIndex = str.indexOf('>');
    	int endIndex = str.lastIndexOf('<');
    	if (endIndex > beginIndex)
    		temp = str.substring(beginIndex+1, endIndex);
    	StringTokenizer strTok = new StringTokenizer(temp,",");
    	while(strTok.hasMoreTokens())
    		unknownAttributes.add(new Attribute(strTok.nextToken()));
    }
    private Attribute findAndRemoveAttr(String attr) {
    	Iterator<Attribute> it=unknownAttributes.iterator();
    	while(it.hasNext()) {
    		Attribute attribute = it.next();
    		String key = attribute.getKey();
    		if (key.compareTo(attr)==0) {
    			it.remove();
    			return attribute;
    		}
    	}
    	return null;
    	 
    }
    
    protected void parse() {
    	
    	buildAttributeList();
    	
        // <...tag...>data</...tag...>
        // data: dbaseId=345,ip=192.168.0.9:23
        // dbaseId = primary key id in RTU table
        // ip = ip & port of the socketlistener
    	
        dbaseId = getIntAttribute("dbaseId");
        ip = getStringAttribute("ip");
        serialId = getStringAttribute("serialId");
        comPort = getStringAttribute("comPort");
        ipAddress = getStringAttribute("ipAddress");
        ipPort = getIntAttribute("ipPort",80);
        meterType = getStringAttribute("meterType");
    	
    	doParse();
    }
    
    
    private int getStartIndex(String attr) {
    	Attribute attribute = findAndRemoveAttr(attr);
    	if (attribute == null) return -1;
    	
        if (str.indexOf(attr + "=") == -1) return -1;
        return str.indexOf(attr + "=") + (attr.length() + 1);
    }

    private int getEndIndex(int indexstart) {
        int pos;
        pos = str.indexOf(",", indexstart);
        if (pos == -1) pos = str.indexOf("<", indexstart);
        return pos;
    }

    protected char getCharAttribute(String attr) {
        int indexstart = getStartIndex(attr);
        if (indexstart == -1) return (char) 0;
        int indexend = getEndIndex(indexstart);
        return (str.substring(indexstart, indexend).charAt(0));
    }

    protected int getIntAttribute(String attr) {
    	return getIntAttribute(attr,-1);
    }
    protected int getIntAttribute(String attr, int defaultVal) {
        int indexstart = getStartIndex(attr);
        if (indexstart == -1) return defaultVal;
        int indexend = getEndIndex(indexstart);
        return (Integer.parseInt(str.substring(indexstart, indexend)));
    }

    protected String getStringAttribute(String attr) {
        int indexstart = getStartIndex(attr);
        if (indexstart == -1) return null;
        int indexend = getEndIndex(indexstart);
        return (str.substring(indexstart, indexend));

    }
    protected boolean attributeExist(String attr) {
    	
    	Attribute attribute = findAndRemoveAttr(attr);
    	if (attribute == null) return false;
    	
        return str.indexOf(attr) >= 0;
    }

    /**
     * Getter for property type.
     *
     * @return Value of property type.
     */
    public int getType() {
        return type;
    }

    public String getCallHomeId() {
    	return (getSerialId()==null?"":getSerialId())+(getMeterType()==null?"":getMeterType());
    }
    /**
     * Setter for property type.
     *
     * @param type New value of property type.
     */
    public void setType(int type) {
        this.type = type;
    }

    public boolean isResponse() {
        return getType() == RESPONSE_PARAMS;
    }

    public boolean isRequest() {
        return getType() == REQUEST_PARAMS;
    }

    public boolean isError() {
        return getType() == ERROR_PARAMS;
    }
    
    public boolean isDeploy() {
        return getType() == DEPLOY_PARAMS;
    }

    public boolean isEvent() {
        return getType() == EVENT_PARAMS;
    }

    public boolean isEventPO() {
        return getType() == EVENTPO_PARAMS;
    }
    
    public int getDbaseId() {
        return dbaseId;
    }

    public java.lang.String getIp() {
        return ip;
    }

    public java.lang.String getSerialId() {
        return serialId;
    }

    public String getComPort() {
        return comPort;
    }
	public String getIpAddress() {
		return ipAddress;
	}

	public String getMeterType() {
		return meterType;
	}
	public int getIpPort() {
		return ipPort;
	}
	public List<Attribute> getUnknownAttributes() {
		return unknownAttributes;
	}
    
}

/*
 * RequestParameter.java
 *
 * Created on 24 februari 2004, 9:53
 */

package com.elster.genericprotocolimpl.dlms.ek280.discovery.core;

/**
 * @author Koen
 *
 * 15/11/2011 modified for EK280 gh
 */
public class RequestParameters extends Parameters {


    String rtuType = "";

    /**
     * Creates a new instance of RequestParameter
     */
    public RequestParameters(String request) {
        super(request, REQUEST_PARAMS);
        parse();
    }

    protected String doToString() {
    	return "";
    }
    
    protected void doParse() {
        rtuType = getStringAttribute("type");
    }

    public String getRtuType() {
        return rtuType;
    }

    public static void main(String[] strs) {

        RequestParameters rp = new RequestParameters("<REQUEST>meterType=AS230,serialId=12345,dbaseId=5,ipAddress=192.168.0.1,type=EK280_C</REQUEST>");
        System.out.println(rp);
        System.out.println("type=" + rp.getRtuType());
    }
}

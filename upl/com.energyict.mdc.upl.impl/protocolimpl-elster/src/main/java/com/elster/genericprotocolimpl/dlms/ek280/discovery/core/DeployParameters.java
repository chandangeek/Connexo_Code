package com.elster.genericprotocolimpl.dlms.ek280.discovery.core;

import java.util.Iterator;

public class DeployParameters extends Parameters {

	boolean validate = false;
	
    /**
     * Creates a new instance of RequestParameter
     */
    public DeployParameters(String request) {
        super(request, DEPLOY_PARAMS);
        parse();
    }

    protected String doToString() {
    	return ", validate="+isValidate();
    }
    
    protected void doParse() {
    	validate = attributeExist("validate");
    }

    public static void main(String[] strs) {

//    	DeployParameters rp = new DeployParameters("<DEPLOY>ipPort=5678, meterType=AS230,serialId=12345,dbaseId=5,ipAddress=192.168.0.1</DEPLOY>");
//    	DeployParameters rp = new DeployParameters("<DEPLOY/>");
    	DeployParameters rp = new DeployParameters("<DEPLOY></DEPLOY>");
        System.out.println(rp);
        Iterator<Attribute> it = rp.getUnknownAttributes().iterator();
        while(it.hasNext()) {
        	System.out.println(it.next());
        }
    }

	public boolean isValidate() {
		return validate;
	}

} // public class DeployParameters extends Parameters

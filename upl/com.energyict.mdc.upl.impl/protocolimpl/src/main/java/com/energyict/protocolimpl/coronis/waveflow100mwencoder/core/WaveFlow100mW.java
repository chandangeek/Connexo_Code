package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.*;
import java.util.*;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;

public class WaveFlow100mW extends AbstractProtocol {

	private WaveFlowConnect waveFlowConnect;
	
	private ParameterFactory parameterFactory;
	
	final public WaveFlowConnect getWaveFlowConnect() {
		return waveFlowConnect;
	}

	@Override
	protected void doConnect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doDisConnect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected List doGetOptionalKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ProtocolConnection doInit(InputStream inputStream,
			OutputStream outputStream, int timeoutProperty,
			int protocolRetriesProperty, int forcedDelay, int echoCancelling,
			int protocolCompatible, Encryptor encryptor,
			HalfDuplexController halfDuplexController) throws IOException {
		
		parameterFactory = new ParameterFactory(this);
		waveFlowConnect = new WaveFlowConnect(inputStream,outputStream,timeoutProperty,getLogger(),forcedDelay);
		return waveFlowConnect;
	}

	@Override
	protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout","40000").trim()));
	}

	@Override
	public String getFirmwareVersion() throws IOException, UnsupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProtocolVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getTime() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTime() throws IOException {
		// TODO Auto-generated method stub
		
	}

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        //System.out.println(ProtocolUtils.outputHexString(waveFlowConnect.sendData(new byte[]{0x0b})));
        //System.out.println(new String(waveFlowConnect.sendData(new byte[]{0x0b})));
    	//System.out.println(ProtocolUtils.outputHexString(waveFlowConnect.sendData(new byte[]{0x07,0x01,0x00,0x01,0x00,0x00})));
        //System.out.println(new String(waveFlowConnect.sendData(new byte[]{0x07,0x01,0x00,0x01,0x00,0x00})));
    	
//    	System.out.println("write application status with 0x55");
//    	parameterFactory.writeApplicationStatus(0x55);
//    	System.out.println("write operating mode with 0x0001");
//    	parameterFactory.writeOperatingMode(0x1);
//    	
//    	System.out.println("read applicationstatus : "+Utils.toHexString(parameterFactory.readApplicationStatus()));
//    	System.out.println("read operating mode : "+Utils.toHexString(parameterFactory.readOperatingMode()));
//    	
//    	System.out.println("write application status with 0x0");
//    	parameterFactory.writeApplicationStatus(0x0);
//    	System.out.println("write operating mode with 0x0005");
//    	parameterFactory.writeOperatingMode(0x5);
//    	
//    	System.out.println("read applicationstatus : "+Utils.toHexString(parameterFactory.readApplicationStatus()));
//    	System.out.println("read operating mode : "+Utils.toHexString(parameterFactory.readOperatingMode()));
    	
//    	System.out.println("timedate: "+parameterFactory.readTimeDateRTC());
//    	Calendar calendar = Calendar.getInstance(getTimeZone());
//    	calendar.add(Calendar.HOUR,1);
//    	parameterFactory.writeTimeDateRTC(calendar.getTime());
//    	
//    	System.out.println("timedate: "+parameterFactory.readTimeDateRTC());
//    	parameterFactory.writeTimeDateRTC(new Date());
//    	System.out.println("timedate: "+parameterFactory.readTimeDateRTC());

    	
//    	System.out.println("read sampling period: "+parameterFactory.readSamplingPeriod()+" seconds");
//    	System.out.println("read measurementPeriod: "+parameterFactory.readMeasurementPeriod());
//    	System.out.println("profileinterval = "+parameterFactory.getProfileIntervalInSeconds());
    	
    	System.out.println("read nr of logged records: "+parameterFactory.readNrOfLoggedRecords());
    	
    	return null;
    }	 


}

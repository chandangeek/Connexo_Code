package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.*;
import java.util.*;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;


public class WaveFlow100mW extends AbstractProtocol {

	private WaveFlowConnect waveFlowConnect;
	
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
    	System.out.println(ProtocolUtils.outputHexString(waveFlowConnect.sendData(new byte[]{0x07,0x01,0x00,0x01,0x00,0x00})));
        //System.out.println(new String(waveFlowConnect.sendData(new byte[]{0x07,0x01,0x00,0x01,0x00,0x00})));
    	return null;
    }	 


}

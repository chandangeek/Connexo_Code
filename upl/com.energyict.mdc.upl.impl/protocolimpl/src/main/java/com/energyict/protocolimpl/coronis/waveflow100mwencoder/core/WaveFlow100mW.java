package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.*;
import java.util.*;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderUnitInfo.EncoderUnitType;


public class WaveFlow100mW extends AbstractProtocol {

	private WaveFlowConnect waveFlowConnect;
	
	private ParameterFactory parameterFactory;
	private RadioCommandFactory radioCommandFactory;
	
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
		radioCommandFactory = new RadioCommandFactory(this);
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

	void restartDataLogging() throws IOException {
		int om = parameterFactory.readOperatingMode();
		parameterFactory.writeOperatingMode(om & 0xFFF3,0x000C);
		parameterFactory.writeSamplingActivationNextHour();
		parameterFactory.writeOperatingMode(om);
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
    	
    	//System.out.println("read nr of logged records: "+parameterFactory.readNrOfLoggedRecords());
    	
//    	System.out.println("A encodermodel: "+parameterFactory.readEncoderModel(0).getEncoderModelInfo().getEncoderModelType()+", "+parameterFactory.readEncoderModel(0).getEncoderModelInfo().getManufacturerId());
//    	System.out.println("B encodermodel: "+parameterFactory.readEncoderModel(1).getEncoderModelInfo().getEncoderModelType()+", "+parameterFactory.readEncoderModel(1).getEncoderModelInfo().getManufacturerId());
//    	
//    	System.out.println("A encoderunit: "+parameterFactory.readEncoderUnit(0).getEncoderUnitInfo().getEncoderUnitType()+", "+parameterFactory.readEncoderUnit(0).getEncoderUnitInfo().getNrOfDigitsBeforeDecimalPoint());
//    	System.out.println("B encoderunit: "+parameterFactory.readEncoderUnit(1).getEncoderUnitInfo().getEncoderUnitType()+", "+parameterFactory.readEncoderUnit(1).getEncoderUnitInfo().getNrOfDigitsBeforeDecimalPoint());
//    	
//    	
//    	System.out.println("set unit port A");
//    	parameterFactory.writeEncoderUnit(0, EncoderUnitType.CubicMeters,4);
//    	System.out.println("set unit port B");
//    	parameterFactory.writeEncoderUnit(1, EncoderUnitType.Unknown,0);
//    	
//    	System.out.println("A encoderunit: "+parameterFactory.readEncoderUnit(0).getEncoderUnitInfo().getEncoderUnitType()+", "+parameterFactory.readEncoderUnit(0).getEncoderUnitInfo().getNrOfDigitsBeforeDecimalPoint());
//    	System.out.println("B encoderunit: "+parameterFactory.readEncoderUnit(1).getEncoderUnitInfo().getEncoderUnitType()+", "+parameterFactory.readEncoderUnit(1).getEncoderUnitInfo().getNrOfDigitsBeforeDecimalPoint());
//    	
//    	
//    	System.out.println("Remaining battery life: "+parameterFactory.readBatteryLifeDurationCounter().remainingBatteryLife());
//    	System.out.println("Battery life end time: "+parameterFactory.readBatteryLifeDateEnd());
    	
    	
//    	System.out.println("encoder current reading: "+radioCommandFactory.readEncoderCurrentReading());
//    	System.out.println();
//    	System.out.println("encoder datalogging table readings: "+radioCommandFactory.readEncoderDataloggingTable(true,true,30,0));
    	
    	//restartDataLogging();
    	
    	System.out.println("encoder datalogging table readings: "+radioCommandFactory.readEncoderDataloggingTable(false,true,60,0));
    	
    	System.out.println("firmware version = "+Utils.toHexString(radioCommandFactory.readFirmwareVersion().getFirmwareVersion()));
    	
    	return null;
    }	 


}

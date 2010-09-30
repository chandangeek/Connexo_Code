package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.*;
import java.util.*;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderUnitInfo.EncoderUnitType;


public class WaveFlow100mW extends AbstractProtocol implements MessageProtocol {

	/**
	 * reference to the lower connect latyers of the wavenis stack
	 */
	private WaveFlowConnect waveFlowConnect;
	
	/**
	 * reference to the parameter factory
	 */
	private ParameterFactory parameterFactory;

	/**
	 * reference to the radio commands factory
	 */
	private RadioCommandFactory radioCommandFactory;

	/**
	 * read and build the profiledata
	 */
	private ProfileDataReader profileDataReader;
	
	/**
	 * reference to the obiscode mapper.
	 */
	private ObisCodeMapper obisCodeMapper;
	
	/**
	 * reference to the message protocol parser
	 */
	private WaveFlow100mWMessages waveFlow100mWMessages = new WaveFlow100mWMessages(this);
	
	/**
	 * the correcttime property. this property is set from the protocolreader in order to allow to sync the time...
	 */
	private int correctTime;
	
	/**
	 * cached encoder generic header...
	 */
	private EncoderGenericHeader cachedEncoderGenericHeader=null;
	
	final EncoderGenericHeader getCachedEncoderGenericHeader() {
		return cachedEncoderGenericHeader;
	}

	final void setCachedEncoderGenericHeader(EncoderGenericHeader cachedEncoderGenericHeader) {
		this.cachedEncoderGenericHeader = cachedEncoderGenericHeader;
	}

	/**
	 * The obiscode for the load profile. Since the Waveflow100mw can connect 2 watermeters, there are 2 independent load profiles.
	 */
	ObisCode loadProfileObisCode;
	
	final ParameterFactory getParameterFactory() {
		return parameterFactory;
	}

	final RadioCommandFactory getRadioCommandFactory() {
		return radioCommandFactory;
	}
	
	final public WaveFlowConnect getWaveFlowConnect() {
		return waveFlowConnect;
	}

	@Override
	protected void doConnect() throws IOException {
		if (getExtendedLogging() >= 1) {
			obisCodeMapper.getRegisterExtendedLogging();
		}
	}

	@Override
	protected void doDisConnect() throws IOException {
		// TODO Auto-generated method stub
		
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
		obisCodeMapper = new ObisCodeMapper(this);
		profileDataReader = new ProfileDataReader(this);
		
		return waveFlowConnect;
	}

	@Override
	protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout","40000").trim()));
		setLoadProfileObisCode(ObisCode.fromString(properties.getProperty("LoadProfileObisCode", "0.0.99.1.0.255")));
		correctTime = Integer.parseInt(properties.getProperty(MeterProtocol.CORRECTTIME,"0"));
	}

	@Override
	public String getFirmwareVersion() throws IOException, UnsupportedException {
		try {
			return "V"+getRadioCommandFactory().readFirmwareVersion().getFirmwareVersion()+", Mode of transmission "+getRadioCommandFactory().readFirmwareVersion().getModeOfTransmission();
		} catch (IOException e) {
			return "Error requesting firmware version";
		}
	}

	@Override
	public String getProtocolVersion() {
		String rev = "$Revision: 43219 $" + " - " + "$Date: 2010-09-21 10:31:34 +0100 (tu, 21 sep 2010) $";
		String manipulated = "Revision " + rev.substring(rev.indexOf("$Revision: ") + "$Revision: ".length(), rev.indexOf("$ -")) + "at "
				+ rev.substring(rev.indexOf("$Date: ") + "$Date: ".length(), rev.indexOf("$Date: ") + "$Date: ".length() + 19);
    	return manipulated;
	}

	@Override
	public Date getTime() throws IOException {
		// If we need to sync the time, then we need to request the RTC in the waveflow device in order to determine the shift.
		// However, if no timesync needs to be done, we're ok with the current RTC from the cached generic header.
		// we do this because we want to limit the roudtrips to eiserver
		if ((correctTime==0) &&  (cachedEncoderGenericHeader != null)) {
			return cachedEncoderGenericHeader.getCurrentRTC();
		}
		else {
			return parameterFactory.readTimeDateRTC();
		}
		
	}

	@Override
	public void setTime() throws IOException {
		if (correctTime>0) {
			parameterFactory.writeTimeDateRTC(new Date());
		}
	}

	final void restartDataLogging() throws IOException {
		int om = parameterFactory.readOperatingMode();
		parameterFactory.writeOperatingMode(om & 0xFFF3,0x000C);
		parameterFactory.writeSamplingActivationNextHour();
		parameterFactory.writeOperatingMode(om|0x0004);
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
    	//System.out.println("Remaining battery life: "+parameterFactory.readBatteryLifeDurationCounter().remainingBatteryLife());
//    	System.out.println("Battery life end time: "+parameterFactory.readBatteryLifeDateEnd());
    	
    	
//    	System.out.println("encoder current reading: "+radioCommandFactory.readEncoderCurrentReading());
//    	System.out.println();
    	//System.out.println("encoder datalogging table readings: "+radioCommandFactory.readEncoderDataloggingTable(true,false,10,0));
    	
//    	System.out.println("encoder internal data: "+radioCommandFactory.readEncoderInternalData());
    	
    	//restartDataLogging();
//    	System.out.println("read operating mode : "+Utils.toHexString(parameterFactory.readOperatingMode()));
//    	System.out.println("encoder datalogging table readings: "+radioCommandFactory.readEncoderDataloggingTable(true,false,60,0));
    	
//    	System.out.println("firmware version = "+Utils.toHexString(radioCommandFactory.readFirmwareVersion().getFirmwareVersion()));
  /*
    	Calendar calendar = Calendar.getInstance(getTimeZone());
    	calendar.add(Calendar.DATE,-1);
    	System.out.println(profileDataReader.getProfileData(calendar.getTime(), 0));
    */	
    	return obisCodeMapper.getRegisterValue(obisCode);
    }	 

    /**
     * Override this method to provide meter specific info for an obiscode mapped register. This method is called outside the communication session. So the info provided is static info in the protocol.
     * @param obisCode obiscode of the register to lookup
     * @throws java.io.IOException thrown when somethiong goes wrong
     * @return RegisterInfo object
     */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }
    
    /**
     * Override this method to requesting the load profile integration time
     * @throws com.energyict.protocol.UnsupportedException thrown when not supported
     * @throws java.io.IOException Thrown when something goes wrong
     * @return integration time in seconds
     */
    public int getProfileInterval() throws UnsupportedException, IOException {
        return getParameterFactory().getProfileIntervalInSeconds();
    }

    /**
     * Override this method to request the load profile from the meter starting at lastreading until now.
     * @param lastReading request from
     * @param includeEvents enable or disable tht reading of meterevents
     * @throws java.io.IOException When something goes wrong
     * @return All load profile data in the meter from lastReading
     */
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
    	
    	int portId;
    	if (getLoadProfileObisCode().getD() == 1) {
    		portId=0; // port A
    	}
    	else if (getLoadProfileObisCode().getD() == 2) {
    		portId=1; // port B
    	}
    	else {
    		portId=2; // port A & B
    	}
    	try {
    		return profileDataReader.getProfileData(lastReading,portId,includeEvents);
    	}
    	catch(WaveFlow100mwEncoderException e) {
    		getLogger().warning("No profile data available. Probably datalogging restarted...");
    		return null;
    	}
    }    
   
	public void applyMessages(List messageEntries) throws IOException {
		waveFlow100mWMessages.applyMessages(messageEntries);
	}

	public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		return waveFlow100mWMessages.queryMessage(messageEntry);
	}

	public List getMessageCategories() {
		return waveFlow100mWMessages.getMessageCategories();
	}

	public String writeMessage(Message msg) {
		return waveFlow100mWMessages.writeMessage(msg);
	}

	public String writeTag(MessageTag tag) {
		return waveFlow100mWMessages.writeTag(tag);
	}

	public String writeValue(MessageValue value) {
		return waveFlow100mWMessages.writeValue(value);
	}
    
	public ObisCode getLoadProfileObisCode() {
		return loadProfileObisCode;
	}

	public void setLoadProfileObisCode(ObisCode loadProfileObisCode) {
		this.loadProfileObisCode = loadProfileObisCode;
	}

	@Override
    protected List doGetOptionalKeys() {
        List result = new ArrayList();
        return result;
    }
	
    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
    	// absorb
    }
}

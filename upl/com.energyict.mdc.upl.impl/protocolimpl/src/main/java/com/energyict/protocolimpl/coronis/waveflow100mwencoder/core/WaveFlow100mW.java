package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.*;
import java.util.*;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.coronis.core.*;


abstract public class WaveFlow100mW extends AbstractProtocol implements MessageProtocol,ProtocolLink,EventMapper {

	private static final int WAVEFLOW_NR_OF_CHANNELS = 2;

	abstract protected void doTheConnect() throws IOException;
    abstract protected void doTheInit() throws IOException;
    abstract protected void doTheDisConnect() throws IOException;
    abstract protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException;
	abstract protected ProfileData getTheProfileData(Date lastReading, int portId, boolean includeEvents) throws UnsupportedException, IOException;
    abstract protected MeterProtocolType getMeterProtocolType();
	
    public enum MeterProtocolType { 
    	SM150E(0),
    	ECHODIS(1);
    	
    	int type;
    	
    	MeterProtocolType(int type) {
    		this.type=type;
    	}
    }
    
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
	 * Reference to the escape command factory. this factory allows calling
	 * wavenis protocolstack specific commands if implemented...  
	 */
	private EscapeCommandFactory escapeCommandFactory;
	
	final public EscapeCommandFactory getEscapeCommandFactory() {
		return escapeCommandFactory;
	}

	/**
	 * reference to the obiscode mapper.
	 */
	private CommonObisCodeMapper commonObisCodeMapper;
	
	final public CommonObisCodeMapper getCommonObisCodeMapper() {
		return commonObisCodeMapper;
	}

	/**
	 * reference to the message protocol parser
	 */
	private WaveFlow100mWMessages waveFlow100mWMessages = new WaveFlow100mWMessages(this);
	
	/**
	 * the correcttime property. this property is set from the protocolreader in order to allow to sync the time...
	 */
	private int correctTime;
	
	/**
	 * cached generic header...
	 */
	private GenericHeader cachedGenericHeader=null;
	
	final public GenericHeader getCachedGenericHeader() throws IOException {
		if (cachedGenericHeader == null) {
			radioCommandFactory.readInternalData();
		}
		return cachedGenericHeader;
	}

	final void setCachedGenericHeader(GenericHeader cachedGenericHeader) {
		this.cachedGenericHeader = cachedGenericHeader;
	}

	/**
	 * The obiscode for the load profile. Since the Waveflow100mw can connect 2 watermeters, there are 2 independent load profiles.
	 */
	ObisCode loadProfileObisCode;
	
	final public ParameterFactory getParameterFactory() {
		return parameterFactory;
	}

	final public RadioCommandFactory getRadioCommandFactory() {
		return radioCommandFactory;
	}


	@Override
	protected void doConnect() throws IOException {
		if (getExtendedLogging() >= 1) {
			commonObisCodeMapper.getRegisterExtendedLogging();
		}
		doTheConnect();
	}
	
	
	@Override
	protected void doDisConnect() throws IOException {
		doTheDisConnect();
	}

	@Override
	protected ProtocolConnection doInit(InputStream inputStream,
			OutputStream outputStream, int timeoutProperty,
			int protocolRetriesProperty, int forcedDelay, int echoCancelling,
			int protocolCompatible, Encryptor encryptor,
			HalfDuplexController halfDuplexController) throws IOException {
		
		parameterFactory = new ParameterFactory(this);
		radioCommandFactory = new RadioCommandFactory(this);
		waveFlowConnect = new WaveFlowConnect(inputStream,outputStream,timeoutProperty,getLogger(),forcedDelay,getInfoTypeProtocolRetriesProperty());
		commonObisCodeMapper = new CommonObisCodeMapper(this);
		escapeCommandFactory = new EscapeCommandFactory(this);
		
		
		
		doTheInit();
		
		return waveFlowConnect;
		
	}

	@Override
	protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout","40000").trim()));
		setLoadProfileObisCode(ObisCode.fromString(properties.getProperty("LoadProfileObisCode", "0.0.99.1.0.255")));
		correctTime = Integer.parseInt(properties.getProperty(MeterProtocol.CORRECTTIME,"0"));
		
		doTheValidateProperties(properties);
	}

	@Override
	public String getFirmwareVersion() throws IOException, UnsupportedException {
		try {
			return "V"+WaveflowProtocolUtils.toHexString(getRadioCommandFactory().readFirmwareVersion().getFirmwareVersion())+", Mode of transmission "+getRadioCommandFactory().readFirmwareVersion().getModeOfTransmission();
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
		// we do this because we want to limit the roudtrips to the RF device
		if ((correctTime==0) &&  (cachedGenericHeader != null)) {
			return cachedGenericHeader.getCurrentRTC();
		}
		else {
			return parameterFactory.readTimeDateRTC();
		}
		
	}

	final void forceSetTime() throws IOException {
		parameterFactory.writeTimeDateRTC(new Date());
	}
	
	
	
	final void setWaveFlowTime() throws IOException {
		parameterFactory.writeTimeDateRTC(new Date());
	}
	
	@Override
	public void setTime() throws IOException {
		if (correctTime>0) {
			parameterFactory.writeTimeDateRTC(new Date());
			getLogger().warning("Restart the datalogging after a timeset!");
			restartDataLogging();
		}
	}

	final public void restartDataLogging() throws IOException {
/*		int om = parameterFactory.readOperatingMode();
		parameterFactory.writeOperatingMode(om & 0xFFF3,0x000C);
		parameterFactory.writeSamplingActivationNextHour();
		parameterFactory.writeOperatingMode(om|0x0004);*/
		//int om = parameterFactory.readOperatingMode();
		parameterFactory.writeOperatingMode(0,0x000C);
		parameterFactory.writeSamplingActivationNextHour();
		parameterFactory.writeOperatingMode(0x0004,0x000C);
	}
	
	final public void writeSamplingRate() throws IOException {
		parameterFactory.writeSamplingPeriod(getProfileInterval());
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
    		return getTheProfileData(lastReading,portId,includeEvents);
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
    
    public InternalData[] readInternalDatas() throws IOException {
      return getRadioCommandFactory().readInternalData().getInternalDatas();	
    }

    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
    	return WAVEFLOW_NR_OF_CHANNELS;
    }

    public WaveFlowConnect getWaveFlowConnect() {
    	return waveFlowConnect;
    }
    
	public List map2MeterEvent(String event) throws IOException {
		AlarmFrameParser alarmFrame = new AlarmFrameParser(event.getBytes(), this);
		return alarmFrame.getMeterEvents();	
	}
    
}

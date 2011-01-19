package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

import org.omg.Dynamic.Parameter;

import com.energyict.cbo.*;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.util.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.coronis.core.*;

abstract public class AbstractDLMS extends AbstractProtocol implements ProtocolLink,MessageProtocol,EventMapper  {
	
	
	
	static Map<ObisCode,ObjectEntry> objectEntries = new HashMap();
	
	static final ObisCode CLOCK_OBIS_CODE=ObisCode.fromString("0.0.1.0.0.255");
	
	
	static {
		objectEntries.put(CLOCK_OBIS_CODE,new ObjectEntry("Clock",8));
	}
	
	public static Map<ObisCode,ObjectEntry> getObjectEntries() {
		return objectEntries;
	}
	
	public static ObjectEntry findObjectByObiscode(final ObisCode obisCode) throws NoSuchRegisterException {
		ObjectEntry o = objectEntries.get(obisCode);
		if (o==null) {
			throw new NoSuchRegisterException("Register with obiscode ["+obisCode+"] not found.");
		}
		else {
			return o;
		}
	}	

	private WaveFlowDLMSWMessages waveFlowDLMSWMessages = new WaveFlowDLMSWMessages(this);

	private TransparantObjectAccessFactory transparantObjectAccessFactory;
	
	private ObisCodeMapper obisCodeMapper;

	/**
	 * the correcttime property. this property is set from the protocolreader in order to allow to sync the time...
	 */
	private int correctTime;
	
	/**
	 * List of obiscodes to retrieve at the beginning of the session and cache for later use.
	 */
	private List<ObjectInfo> objectInfos;	
	
	final List<ObjectInfo> getObjectInfos() {
		return objectInfos;
	}


	/**
	 * the load profile obis code custom property
	 */
	private ObisCode loadProfileObisCode;
	
	/**
	 * reference to the radio commands factory
	 */
	private RadioCommandFactory radioCommandFactory;	
	
	/**
	 * Reference to the parameter factory
	 */
	private ParameterFactory parameterFactory;
	
	public final ParameterFactory getParameterFactory() {
		return parameterFactory;
	}

	public final RadioCommandFactory getRadioCommandFactory() {
		return radioCommandFactory;
	}

	public final TransparantObjectAccessFactory getTransparantObjectAccessFactory() {
		return transparantObjectAccessFactory;
	}

	/**
	 * Allow auto pairing when reading an e-meter fails. Default 1 (enabled)
	 */
	private int autoPairingRetry;
	
	
    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
    	// absorb
    }
	
	/**
	 * Reference to the escape command factory. this factory allows calling
	 * wavenis protocolstack specific commands if implemented...  
	 */
	private EscapeCommandFactory escapeCommandFactory;
	
	final EscapeCommandFactory getEscapeCommandFactory() {
		return escapeCommandFactory;
	}

	/**
	 * reference to the lower connect latyers of the wavenis stack
	 */
	private WaveFlowConnect waveFlowConnect;	
	
	
	private byte[] buildPairingFrame() throws IOException {
		
		// fill in the default password of "22222222" and device address  0x1057
		byte[] pairingFrame = new byte[]{(byte)0x30,(byte)0x02,(byte)0x02,(byte)0x11,
				                         (byte)0x02,(byte)0x10,(byte)0x57,(byte)0x00,(byte)0x00,
				                         (byte)0x08,(byte)0x32,(byte)0x32,(byte)0x32,(byte)0x32,(byte)0x32,(byte)0x32,(byte)0x32,(byte)0x32,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
				                         (byte)0x00,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x60,(byte)0x01,(byte)0x00,(byte)0xFF,(byte)0x02};
		
		int OFFSET_PASSWORD_LENGTH=9;
		int OFFSET_PASSWORD=10;
		int OFFSET_ADDRESS_LENGTH=4;
		int OFFSET_ADDRESS=5;
		
		String address = getInfoTypeDeviceID();
		if ((address != null) && (address.compareTo("") != 0)) {
			getLogger().info("Build pairingrequest frame with device address ["+address+"]");
			long addr = Long.parseLong(address);
			
			if (addr>=0x100000000L) {
				throw new IOException("Address > maxint!!");
			}
			else if (addr>=0x1000000L) {
				pairingFrame[OFFSET_ADDRESS_LENGTH] = 4;
				pairingFrame[OFFSET_ADDRESS] = (byte)(addr>>24);
				pairingFrame[OFFSET_ADDRESS+1] = (byte)(addr>>16);
				pairingFrame[OFFSET_ADDRESS+2] = (byte)(addr>>8);
				pairingFrame[OFFSET_ADDRESS+3] = (byte)(addr);
			}
			else if (addr>=0x10000L) {
				pairingFrame[OFFSET_ADDRESS_LENGTH] = 3;
				pairingFrame[OFFSET_ADDRESS] = (byte)(addr>>16);
				pairingFrame[OFFSET_ADDRESS+1] = (byte)(addr>>8);
				pairingFrame[OFFSET_ADDRESS+2] = (byte)(addr);
			}
			else {
				pairingFrame[OFFSET_ADDRESS_LENGTH] = 2;
				pairingFrame[OFFSET_ADDRESS] = (byte)(addr>>8);
				pairingFrame[OFFSET_ADDRESS+1] = (byte)(addr);
			}
			
			pairingFrame[OFFSET_ADDRESS] = (byte)(addr>>8);
			pairingFrame[OFFSET_ADDRESS+1] = (byte)(addr);
		}
		else return null;
		
		String password = getInfoTypePassword();
		if ((password != null) && (password.compareTo("") != 0)) {
			getLogger().info("Build pairingrequest frame with password ["+password+"]");
			if (password.length()==40) {
				pairingFrame[OFFSET_PASSWORD_LENGTH]=20;
				//convert to byte values
				for (int i=0;i<40;i+=2) {
					int val = Integer.parseInt(password.substring(i, i+2));
					pairingFrame[OFFSET_PASSWORD+(i/2)]=(byte)val;
				}
			}
			else if (password.length()>20) {
				throw new IOException("Password length > 20 characters!");
			}
			else {
				byte[] pw = password.getBytes();
				pairingFrame[OFFSET_PASSWORD_LENGTH]=(byte)pw.length;
				for (int i=0;i<pw.length;i++) {
					pairingFrame[OFFSET_PASSWORD+i]=pw[i];
				}
			}
			
		}
		else return null;
		
		return pairingFrame;
	}
	
	@Override
	protected void doConnect() throws IOException {
//		System.out.println("Tune the Wavecard for Waveflow AC");
		escapeCommandFactory.setAndVerifyWavecardAwakeningPeriod(1);
		escapeCommandFactory.setAndVerifyWavecardRadiotimeout(20);
		escapeCommandFactory.setAndVerifyWavecardWakeupLength(110);    	
	}

	

	
	@Override
	protected void doDisConnect() throws IOException {
		System.out.println("Restore Wavecard settings...");
		escapeCommandFactory.setAndVerifyWavecardRadiotimeout(2);
		escapeCommandFactory.setAndVerifyWavecardWakeupLength(1100);
		escapeCommandFactory.setAndVerifyWavecardAwakeningPeriod(10);
		
		
		
	}

	@Override
	protected List doGetOptionalKeys() {
		List list = new ArrayList();
		list.add("AutoPairingRetry");
		return null;
	}

	@Override
	protected ProtocolConnection doInit(InputStream inputStream,
			OutputStream outputStream, int timeoutProperty,
			int protocolRetriesProperty, int forcedDelay, int echoCancelling,
			int protocolCompatible, Encryptor encryptor,
			HalfDuplexController halfDuplexController) throws IOException {
		
	    escapeCommandFactory = new EscapeCommandFactory(this);
	    waveFlowConnect = new WaveFlowConnect(inputStream,outputStream,timeoutProperty,getLogger(),forcedDelay,getInfoTypeProtocolRetriesProperty());
	    radioCommandFactory = new RadioCommandFactory(this);
	    parameterFactory = new ParameterFactory(this);
	    transparantObjectAccessFactory = new TransparantObjectAccessFactory(this);
		obisCodeMapper = new ObisCodeMapper(this);
		
		return waveFlowConnect;
	}

	@Override
	protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout","40000").trim()));
		autoPairingRetry = Integer.parseInt(properties.getProperty("AutoPairingRetry","1").trim());
		setLoadProfileObisCode(ObisCode.fromString(properties.getProperty("LoadProfileObisCode", "0.0.99.1.0.255")));
		correctTime = Integer.parseInt(properties.getProperty(MeterProtocol.CORRECTTIME,"0"));
		objectInfos = buildObisInfos(properties.getProperty("ObisCodeList", ""));  
		
		
		//doTheValidateProperties(properties);
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
		AbstractDataType o = transparantObjectAccessFactory.readObjectAttribute(CLOCK_OBIS_CODE, 2);
		DateTime dateTime = new DateTime(o.getOctetString(), getTimeZone());
		return dateTime.getValue().getTime();
	}

	final void forceSetTime() throws IOException {
		DateTime dateTime = new DateTime(getTimeZone());
		transparantObjectAccessFactory.writeObjectAttribute(CLOCK_OBIS_CODE, 2, dateTime);
	}
	
	@Override
	public void setTime() throws IOException {
		if (correctTime>0) {
			DateTime dateTime = new DateTime(getTimeZone());
			transparantObjectAccessFactory.writeObjectAttribute(CLOCK_OBIS_CODE, 2, dateTime);
		}
	}	

/*	
	@Override
	public Date getTime() throws IOException {
		// If we need to sync the time, then we need to request the RTC in the waveflow device in order to determine the shift.
		// However, if no timesync needs to be done, we're ok with the current RTC from the cached generic header.
		// we do this because we want to limit the roudtrips to the RF device
		if ((correctTime==0) &&  (transparantObjectAccessFactory.getGenericHeader() != null)) {
			return transparantObjectAccessFactory.getGenericHeader().getCurrentDateTime();
		}
		else {
			return parameterFactory.readTimeDateRTC();
		}
		
	}

	final void forceSetTime() throws IOException {
		parameterFactory.writeTimeDateRTC(new Date());
	}
*/	
	public void setWaveFlowTime() throws IOException {
		parameterFactory.writeTimeDateRTC(new Date());
	}	
	
	
    /**
     * Override this method to provide meter specific info for an obiscode mapped register. This method is called outside the communication session. So the info provided is static info in the protocol.
     * @param obisCode obiscode of the register to lookup
     * @throws java.io.IOException thrown when somethiong goes wrong
     * @return RegisterInfo object
     */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }
    
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
    	return obisCodeMapper.getRegisterValue(obisCode);
    }
    
    public boolean pairWithEMeter() throws IOException {
    	try {
    		getEscapeCommandFactory().setAndVerifyWavecardAwakeningPeriod(1);
    		getEscapeCommandFactory().setAndVerifyWavecardRadiotimeout(20);
    		getEscapeCommandFactory().setAndVerifyWavecardWakeupLength(110);
			return doPairWithEMeter();
    	}
		finally {
			getEscapeCommandFactory().setAndVerifyWavecardRadiotimeout(2);
			getEscapeCommandFactory().setAndVerifyWavecardWakeupLength(1100);
			getEscapeCommandFactory().setAndVerifyWavecardAwakeningPeriod(10);
		}    	
    }
    
    private boolean doPairWithEMeter() throws IOException {
    	
			byte[] pairingframe = buildPairingFrame();
			if (pairingframe == null) {
				getLogger().warning("Cannot pair with the meter again because password and/or meteraddress is not known... Wait 15 minutes for the waveflow to pair with the mater again...");
				return false;
			}
			else {
				int retry=1;
				getLogger().warning("Try to pair with the meter, try ["+retry+"]...");
				
				while(true) {
					
	    			if (retry++ >= 5) {
	    				getLogger().severe("Unable to pair with the meter after 5 retries, give up...");
	    				return false;
	    			}
	
	    			byte[] pairingResponse = waveFlowConnect.sendData(pairingframe);
	    			// 30046E4AC000ABB002
	    			int PAIRING_RESULT_OFFFSET=1;
	    			int PAIRING_RESULT_DATA_LENGTH=2;
	    			int PAIRING_RESULT_DATA_OFFSET=3;
	    			if (pairingResponse.length<2) {
	    				getLogger().warning("Pairing result length is anvalid. Expected [9], received ["+pairingResponse.length+"], try ["+retry+"]...");
	    			}
	    			else {
	    				if ((pairingResponse[PAIRING_RESULT_OFFFSET] > 0) && (WaveflowProtocolUtils.toInt(pairingResponse[PAIRING_RESULT_OFFFSET]) < 0xFD)) {
	    					int length = pairingResponse[PAIRING_RESULT_DATA_LENGTH];
	    					byte[] data = ProtocolUtils.getSubArray(pairingResponse, PAIRING_RESULT_DATA_OFFSET);
		    				getLogger().warning("Pairing with the meter was successfull, returned data is ["+ProtocolUtils.outputHexString(data)+"]");
		    				return true;
	    				}
	    				else if (WaveflowProtocolUtils.toInt(pairingResponse[PAIRING_RESULT_OFFFSET]) == 0) {
	    					getLogger().warning("Pairing failed, no answer to GET Meter Serial Number, result code [0], leave loop!");
	    					return true;
	    				}
	    				else if (WaveflowProtocolUtils.toInt(pairingResponse[PAIRING_RESULT_OFFFSET]) == 0xFD) {
	    					getLogger().warning("Pairing with the meter was already done, result code [0xFD], leave loop!");
	    					return true;
	    				}
	    				else if (WaveflowProtocolUtils.toInt(pairingResponse[PAIRING_RESULT_OFFFSET]) == 0xFE) {
	    					getLogger().warning("Pairing failed, no meter connected or connection rejected, result code [0xFE], leave loop!");
	    					return true;
	    				}
	    				else if (WaveflowProtocolUtils.toInt(pairingResponse[PAIRING_RESULT_OFFFSET]) == 0xFF) {
	    					getLogger().warning("Bad request format, result code [0xFF], leave loop!");
	    					return true;
	    				}
	    			}
				
	    			try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	    			
				} // while(true)
				
			} // else
    }
    
	public WaveFlowConnect getWaveFlowConnect() {
		return waveFlowConnect;
	}

	public void applyMessages(List messageEntries) throws IOException {
		waveFlowDLMSWMessages.applyMessages(messageEntries);
	}

	public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		return waveFlowDLMSWMessages.queryMessage(messageEntry);
	}

	public List getMessageCategories() {
		return waveFlowDLMSWMessages.getMessageCategories();
	}

	public String writeMessage(Message msg) {
		return waveFlowDLMSWMessages.writeMessage(msg);
	}

	public String writeTag(MessageTag tag) {
		return waveFlowDLMSWMessages.writeTag(tag);
	}

	public String writeValue(MessageValue value) {
		return waveFlowDLMSWMessages.writeValue(value);
	}	

	public ObisCode getLoadProfileObisCode() {
		return loadProfileObisCode;
	}

	public void setLoadProfileObisCode(ObisCode loadProfileObisCode) {
		this.loadProfileObisCode = loadProfileObisCode;
	}
	
	public List map2MeterEvent(String event) throws IOException {
		
		//FIXME: we should implement a new interface in the protocols to be used for the alarm ack return data...
		
		AlarmFrameParser alarmFrame = new AlarmFrameParser(event.getBytes(), this);

		// this is tricky. We need to return the "alarmstatus" bytes to acknowledge the alarm. 
		// so to avoid changing the signature of interfaceEventMapper in Ethernet, we add the return "alarmstatus" as first element in the list.
		byte[] alarmDataByteArray = event.getBytes();
		List statusAndEvents = new ArrayList();
		statusAndEvents.add(alarmFrame.getResponse());
		statusAndEvents.add(alarmFrame.getMeterEvents());
		
		return statusAndEvents;
	}
	
	private List<ObjectInfo> buildObisInfos(String obisCodeList) throws InvalidPropertyException {
		
		List<ObjectInfo> objectInfos = new ArrayList<ObjectInfo>();
		
		if (obisCodeList.compareTo("") != 0) {
		
			// format class_obiscode_attribute,class.obiscode.attribut,... e.g. 3_1.1.1.8.0.255_2 class 3 obiscode 1.1.1.8.0.255 attribute 2
			
			String[] infos = obisCodeList.split(",");
			
			for (String info : infos) {
				
				String[] fields = info.split("_");
				if (fields.length == 3) {
					int classId = Integer.parseInt(fields[0]);
					ObisCode obisCode = ObisCode.fromString(fields[1]);
					int attribute = Integer.parseInt(fields[2]);
					objectInfos.add(new ObjectInfo(attribute, classId, obisCode));
					
				}
				else if (fields.length == 1) {
					ObisCode obisCode = ObisCode.fromString(fields[0]);
					objectInfos.add(new ObjectInfo(2, 1, obisCode));
				}
				else {
					throw new InvalidPropertyException("Error in obisCodeList property ["+obisCodeList+"]");
				}
			}
		}
		return objectInfos;
		
	}	
}

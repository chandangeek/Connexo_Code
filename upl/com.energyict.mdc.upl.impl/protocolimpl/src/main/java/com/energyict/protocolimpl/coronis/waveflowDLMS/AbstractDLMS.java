package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

import org.omg.Dynamic.Parameter;

import com.energyict.cbo.*;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dlms.axrdencoding.util.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.coronis.core.*;

abstract public class AbstractDLMS extends AbstractProtocol implements ProtocolLink,MessageProtocol  {
	
	abstract byte[] getRequest();
	
	static Map<ObisCode,ObjectEntry> objectEntries = new HashMap();
	
	
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

	WaveFlowDLMSWMessages waveFlowDLMSWMessages = new WaveFlowDLMSWMessages(this);

	TransparantObjectAccessFactory transparantObjectAccessFactory;
	
	ObisCodeMapper obisCodeMapper;

	/**
	 * the correcttime property. this property is set from the protocolreader in order to allow to sync the time...
	 */
	private int correctTime;
	
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
	
	final RadioCommandFactory getRadioCommandFactory() {
		return radioCommandFactory;
	}

	final TransparantObjectAccessFactory getTransparantObjectAccessFactory() {
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
			byte[] pw = password.getBytes();
			pairingFrame[OFFSET_PASSWORD_LENGTH]=(byte)pw.length;
			if (pw.length>20) {
				throw new IOException("Password length > 20 characters!");
			}
			for (int i=0;i<pw.length;i++) {
				pairingFrame[OFFSET_PASSWORD+i]=pw[i];
			}
			
		}
		else return null;
		
		return pairingFrame;
	}
	
	@Override
	protected void doConnect() throws IOException {
		System.out.println("Tune the Wavecard for Waveflow AC");
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
		TransparentGet o = new TransparentGet(this,new ObjectInfo(2,8,ObisCode.fromString("0.0.1.0.0.255")));
		o.invoke();
		DateTime dateTime = new DateTime(o.getDataType().getOctetString(), getTimeZone());
		return dateTime.getValue().getTime();
	}

	final void forceSetTime() throws IOException {
		DateTime dateTime = new DateTime(getTimeZone());
		transparantObjectAccessFactory.writeObjectAttribute(ObisCode.fromString("0.0.1.0.0.255"), 2, dateTime);
	}
	
	@Override
	public void setTime() throws IOException {
		if (correctTime>0) {
			DateTime dateTime = new DateTime(getTimeZone());
			transparantObjectAccessFactory.writeObjectAttribute(ObisCode.fromString("0.0.1.0.0.255"), 2, dateTime);
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
	
	@Override
	public void setTime() throws IOException {
		if (correctTime>0) {
			parameterFactory.writeTimeDateRTC(new Date());
		}
	}	
*/	
	
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
    
    /**
     * Override this method when requesting an obiscode mapped register from the meter.
     * @param obisCode obiscode rmapped register to request from the meter
     * @throws java.io.IOException thrown when somethiong goes wrong
     * @return RegisterValue object
     */
//    public RegisterValue readRegister2(ObisCode obisCode) throws IOException {
//
//    	if (simpleDataParser == null) {
//    		try {
//    			
//    			escapeCommandFactory.setAndVerifyWavecardAwakeningPeriod(1);
//    			escapeCommandFactory.setAndVerifyWavecardRadiotimeout(20);
//    			escapeCommandFactory.setAndVerifyWavecardWakeupLength(110);
//	    		byte[] response = waveFlowConnect.sendData(getRequest());
//	    		simpleDataParser = new SimpleDataParser(getLogger());
//	    		
//	    		try {
//	    			simpleDataParser.parse(getRequest(), response);
//	    		}
//	    		catch(WaveflowDLMSStatusError e) {
//	    			getLogger().warning(e.getMessage());
//	    			if (autoPairingRetry == 1) {
//		    			if (doPairWithEMeter()) {
//			    			try {
//			    				Thread.sleep(2000);
//			    			} catch (InterruptedException e1) {
//			    				// TODO Auto-generated catch block
//			    				e1.printStackTrace();
//			    			}
//			    			
//			    			// retry to read the meter...
//			    			response = waveFlowConnect.sendData(getRequest());
//			    			simpleDataParser.parse(getRequest(), response);
//		    			}
//	    			}
//	    			else {
//	    				throw new IOException(e.getMessage()+"[Auto pairing DISABLED]");
//	    			}
//	    			
//	    		} // catch(WaveflowDLMSStatusError e)
//	    		
//    		}
//    		finally {
//    			escapeCommandFactory.setAndVerifyWavecardRadiotimeout(2);
//    			escapeCommandFactory.setAndVerifyWavecardWakeupLength(1100);
//    			escapeCommandFactory.setAndVerifyWavecardAwakeningPeriod(10);
//    		}
//    	}
//
//    	
//    	if (obisCode.equals(ObisCode.fromString("0.0.96.6.15.255"))) {
//    		return new RegisterValue(obisCode,new Quantity(new BigDecimal(simpleDataParser.getQos()), Unit.get("")),new Date());
//    	}
//    	else {
//	        RegisterValue registerValue =  simpleDataParser.getRegisterValues().get(obisCode);
//	        if (registerValue == null) {
//	        	throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] does not exist!");
//	        }
//	        else {
//	        	return registerValue;
//	        }
//    	}
//    }

    
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
	
}

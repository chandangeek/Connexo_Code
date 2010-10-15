package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.*;
import java.util.*;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.*;

abstract public class AbstractDLMS extends AbstractProtocol implements ProtocolLink,MessageProtocol  {
	
	abstract byte[] getRequest();
	
	SimpleDataParser simpleDataParser=null;

	WaveFlowDLMSWMessages waveFlowDLMSWMessages = new WaveFlowDLMSWMessages(this);
	
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
				                         (byte)0x08,(byte)0x32,(byte)0x32,(byte)0x32,(byte)0x32,(byte)0x32,(byte)0x32,(byte)0x32,(byte)0x32,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doDisConnect() throws IOException {
		// TODO Auto-generated method stub
		
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
		
		return waveFlowConnect;
	}

	@Override
	protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout","40000").trim()));
		autoPairingRetry = Integer.parseInt(properties.getProperty("AutoPairingRetry","1").trim());
	}

	@Override
	public String getFirmwareVersion() throws IOException, UnsupportedException {
		return "V1.0";
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
		return new Date();
	}

	@Override
	public void setTime() throws IOException {
		
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
    /**
     * Override this method when requesting an obiscode mapped register from the meter.
     * @param obisCode obiscode rmapped register to request from the meter
     * @throws java.io.IOException thrown when somethiong goes wrong
     * @return RegisterValue object
     */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {

    	if (simpleDataParser == null) {
    		try {
    			
    			escapeCommandFactory.setAndVerifyWavecardAwakeningPeriod(1);
    			escapeCommandFactory.setAndVerifyWavecardRadiotimeout(20);
    			escapeCommandFactory.setAndVerifyWavecardWakeupLength(110);
	    		byte[] response = waveFlowConnect.sendData(getRequest());
	    		simpleDataParser = new SimpleDataParser(getLogger());
	    		
	    		try {
	    			simpleDataParser.parse(getRequest(), response);
	    		}
	    		catch(WaveflowDLMSStatusError e) {
	    			getLogger().warning(e.getMessage());
	    			if (autoPairingRetry == 1) {
		    			if (pairWithEMeter()) {
			    			try {
			    				Thread.sleep(2000);
			    			} catch (InterruptedException e1) {
			    				// TODO Auto-generated catch block
			    				e1.printStackTrace();
			    			}
			    			
			    			// retry to read the meter...
			    			response = waveFlowConnect.sendData(getRequest());
			    			simpleDataParser.parse(getRequest(), response);
		    			}
	    			}
	    			else {
	    				throw new IOException(e.getMessage()+"[Auto pairing DISABLED]");
	    			}
	    			
	    		} // catch(WaveflowDLMSStatusError e)
	    		
    		}
    		finally {
    			escapeCommandFactory.setAndVerifyWavecardRadiotimeout(2);
    			escapeCommandFactory.setAndVerifyWavecardWakeupLength(1100);
    			escapeCommandFactory.setAndVerifyWavecardAwakeningPeriod(10);
    		}
    	}
    	
        RegisterValue registerValue =  simpleDataParser.getRegisterValues().get(obisCode);
        if (registerValue == null) {
        	throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] does not exist!");
        }
        else {
        	return registerValue;
        }
    }

    public boolean pairWithEMeter() throws IOException {
    	
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
	    			if (pairingResponse.length<2) {
	    				getLogger().warning("Pairing result length is anvalid. Expected [9], received ["+pairingResponse.length+"], try ["+retry+"]...");
	    			}
	    			else {
	    				if (pairingResponse[PAIRING_RESULT_OFFFSET] == 0) {
		    				getLogger().warning("Pairing with the meter was successfull!");
		    				return true;
	    				}
	    				else if (pairingResponse[PAIRING_RESULT_OFFFSET] == 2) {
	    					getLogger().warning("Pairing with the meter was already done, result code [2], leave loop!");
	    					return true;
	    				}
	    				else {
		    				getLogger().warning("Pairing with the meter resulted in an result code different from [0], received ["+pairingResponse[PAIRING_RESULT_OFFFSET]+"], try ["+retry+"]...");
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
	
}

package com.energyict.genericprotocolimpl.webrtuz3.messagehandling;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.genericprotocolimpl.common.messages.MessageHandler;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.genericprotocolimpl.webrtuz3.MbusDevice;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.sql.SQLException;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author gna
 * Changes:
 * GNA |03062009| Added correction switch to switch between corrected and uncorrected Gas loadprofile
 */
public class MbusMessageExecutor extends GenericMessageExecutor{
	
    public static final ObisCode MBUS_CLIENT_OBIS = ObisCode.fromString("0.0.24.1.0.255");
    public static final ObisCode MBUS_DISCONNECT_CONTROL_OBIS = ObisCode.fromString("0.0.24.4.0.255");
    public static final ObisCode MBUS_DISCONNECT_CONTROL_SCHEDULE_OBIS = ObisCode.fromString("0.0.24.6.0.255");
    public static final ObisCode MBUS_DISCONNECT_SCRIPT_TABLE_OBIS = ObisCode.fromString("0.0.24.7.0.255");


    private MbusDevice mbusDevice;

    public MbusMessageExecutor(MbusDevice mbusDevice){
		this.mbusDevice = mbusDevice;
	}

	public void doMessage(RtuMessage rtuMessage) throws BusinessException,SQLException {
		boolean success = false;
		String content = rtuMessage.getContents();
		MessageHandler messageHandler = new MessageHandler();
		try {
			
			importMessage(content, messageHandler);
			
			boolean connect			= messageHandler.getType().equals(RtuMessageConstant.CONNECT_LOAD);
			boolean disconnect		= messageHandler.getType().equals(RtuMessageConstant.DISCONNECT_LOAD);
			boolean connectMode		= messageHandler.getType().equals(RtuMessageConstant.CONNECT_CONTROL_MODE);
			boolean decommission 	= messageHandler.getType().equals(RtuMessageConstant.MBUS_DECOMMISSION);
			boolean mbusEncryption 	= messageHandler.getType().equals(RtuMessageConstant.MBUS_ENCRYPTION_KEYS);
			boolean mbusCorrected 	= messageHandler.getType().equals(RtuMessageConstant.MBUS_CORRECTED_VALUES);
			boolean mbusUnCorrected = messageHandler.getType().equals(RtuMessageConstant.MBUS_UNCORRECTED_VALUES);
			
			if(connect){
				
				getLogger().log(Level.INFO, "Handling MbusMessage " + rtuMessage.displayString() + ": Connect");

				if(!messageHandler.getConnectDate().equals("") && !messageHandler.getConnectDate().equals("0")){	// use the disconnectControlScheduler
					
					Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getConnectDate());
					SingleActionSchedule sasConnect = getCosemObjectFactory().getSingleActionSchedule(getCorrectedObisCode(MBUS_DISCONNECT_CONTROL_SCHEDULE_OBIS));
					
					Structure scriptStruct = new Structure();
                    scriptStruct.addDataType(new OctetString(getCorrectedObisCode(MBUS_DISCONNECT_SCRIPT_TABLE_OBIS).getLN()));
					scriptStruct.addDataType(new Unsigned16(2)); 	// method '2' is the 'remote_connect' method
					
					sasConnect.writeExecutedScript(scriptStruct);
					sasConnect.writeExecutionTime(executionTimeArray);
					
				} else { // immediate connect
					Disconnector connector = getCosemObjectFactory().getDisconnector(getCorrectedObisCode(MBUS_DISCONNECT_CONTROL_OBIS));
					connector.remoteReconnect();
				}
				
				success = true;
				
			} else if(disconnect){
				
				getLogger().log(Level.INFO, "Handling MbusMessage " + rtuMessage.displayString() + ": Disconnect");
				
				if(!messageHandler.getDisconnectDate().equals("") && !messageHandler.getDisconnectDate().equals("0")){	// use the disconnectControlScheduler
					
					Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getDisconnectDate());
					SingleActionSchedule sasDisconnect = getCosemObjectFactory().getSingleActionSchedule(getCorrectedObisCode(MBUS_DISCONNECT_CONTROL_SCHEDULE_OBIS));
					
					Structure scriptStruct = new Structure();
					scriptStruct.addDataType(new OctetString(getCorrectedObisCode(MBUS_DISCONNECT_SCRIPT_TABLE_OBIS).getLN()));
					scriptStruct.addDataType(new Unsigned16(1));	// method '1' is the 'remote_disconnect' method
					
					sasDisconnect.writeExecutedScript(scriptStruct);
					sasDisconnect.writeExecutionTime(executionTimeArray);
					
				} else { // immediate disconnect
					Disconnector connector = getCosemObjectFactory().getDisconnector(getCorrectedObisCode(MBUS_DISCONNECT_CONTROL_OBIS));
					connector.remoteDisconnect();
				}
				
				success = true;
			} else if(connectMode){
				
				getLogger().log(Level.INFO, "Handling MbusMessage " + rtuMessage.displayString() + ": ConnectControl mode");
				String mode = messageHandler.getConnectControlMode();
				
				if(mode != null){
					try {
						int modeInt = Integer.parseInt(mode);
						
						if((modeInt >=0) && (modeInt <=6)){
							Disconnector connectorMode = getCosemObjectFactory().getDisconnector(getCorrectedObisCode(MBUS_DISCONNECT_CONTROL_OBIS));
							connectorMode.writeControlMode(new TypeEnum(modeInt));
							
						} else {
							throw new IOException("Mode is not a valid entry for message " + rtuMessage.displayString() + ", value must be between 0 and 6");
						}
						
					} catch (NumberFormatException e) {
						e.printStackTrace();
						throw new IOException("Mode is not a valid entry for message " + rtuMessage.displayString());
					}
				} else {
					// should never get to the else, can't leave message empty 
					throw new IOException("Message " + rtuMessage.displayString() + " can not be empty");
				}
				
				success = true;
			} else if(decommission){

				getLogger().log(Level.INFO, "Handling MbusMessage " + rtuMessage.displayString() + ": Decommission MBus device");

				MBusClient mbusClient = getCosemObjectFactory().getMbusClient(getCorrectedObisCode(MBUS_CLIENT_OBIS));
				mbusClient.deinstallSlave();

				success = true;
			} else if(mbusEncryption){
				
				getLogger().log(Level.INFO, "Handling MbusMessage " + rtuMessage.displayString() + ": Set encryption keys");
				
				String openKey = messageHandler.getOpenKey();
				String transferKey = messageHandler.getTransferKey();
				
				MBusClient mbusClient = getCosemObjectFactory().getMbusClient(getCorrectedObisCode(MBUS_CLIENT_OBIS));
				
				if(openKey == null){
					mbusClient.setEncryptionKey("");
				} else if(transferKey != null){
					mbusClient.setEncryptionKey(convertStringToByte(openKey));
					mbusClient.setTransportKey(convertStringToByte(transferKey));
				} else {
					throw new IOException("Transfer key may not be empty when setting the encryption keys.");
				}
				
				success = true;
			} else if(mbusCorrected){
				
				// Old implementation
//				getLogger().log(Level.INFO, "Handling MbusMessage " + rtuMessage.displayString() + ": Set loadprofile correction switch");
//				String corrSwitchOc =  "0."+getPhysicalAddress()+".24.8.0.255";
//				Data corrSwitch = getCosemObjectFactory().getData(ObisCode.fromString(corrSwitchOc));
//				BooleanObject bo = new BooleanObject(messageHandler.useCorrected());
//				corrSwitch.setValueAttr(bo);
				
				getLogger().log(Level.INFO, "Handling MbusMessage " + rtuMessage.displayString() + ": Set loadprofile to corrected values");
				MBusClient mc = getCosemObjectFactory().getMbusClient(getCorrectedObisCode(MBUS_CLIENT_OBIS));
				Array capDef = new Array();
				Structure struct = new Structure();
				OctetString dib = new OctetString(new byte[]{0x0C});
				struct.addDataType(dib);
				OctetString vib = new OctetString(new byte[]{0x13});
				struct.addDataType(vib);
				capDef.addDataType(struct);
				mc.writeCaptureDefinition(capDef);
				
				success = true;
				
			} else if(mbusUnCorrected){
				
				getLogger().log(Level.INFO, "Handling MbusMessage " + rtuMessage.displayString() + ": Set loadprofile to unCorrected values");
				MBusClient mc = getCosemObjectFactory().getMbusClient(getCorrectedObisCode(MBUS_CLIENT_OBIS));
				Array capDef = new Array();
				Structure struct = new Structure();
				OctetString dib = new OctetString(new byte[]{(byte)0x0C});
				struct.addDataType(dib);
				OctetString vib = new OctetString(new byte[]{(byte)0x93, (byte)0x3A});
				struct.addDataType(vib);
				capDef.addDataType(struct);
				mc.writeCaptureDefinition(capDef);
				
				success = true;
			}
			else {	// unknown message
				success = false;
				throw new IOException("Unknown message");
			}
			
		} catch (BusinessException e) {
			e.printStackTrace();
			getLogger().log(Level.INFO, "Message " + rtuMessage.displayString() + " has failed. " + e.getMessage());
		} catch (ConnectionException e){
			e.printStackTrace();
			getLogger().log(Level.INFO, "Message " + rtuMessage.displayString() + " has failed. " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			getLogger().log(Level.INFO, "Message " + rtuMessage.displayString() + " has failed. " + e.getMessage());
		} finally {
			if(success){
				rtuMessage.confirm();
				getLogger().log(Level.INFO, "Message " + rtuMessage.displayString() + " has finished.");
			} else {
				rtuMessage.setFailed();
			}
		}
	}
	
	private byte[] convertStringToByte(String string) throws IOException {
		try {
			byte[] b = new byte[string.length()/2];
			int offset = 0;
			for(int i = 0; i < b.length; i++){
				b[i] = (byte) Integer.parseInt(string.substring(offset, offset+=2),16);
			}
			return b;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new IOException("String " + string + " can not be formatted to byteArray");
		}
	}
	
	private int getPhysicalAddress(){
		return getMbusDevice().getPhysicalAddress();
	}
	
	private CosemObjectFactory getCosemObjectFactory() {
		return getMbusDevice().getWebRTU().getCosemObjectFactory();
	}
	
	private DLMSMeterConfig getMeterConfig() {
		return getMbusDevice().getWebRTU().getMeterConfig();
	}

    /**
     * Getter for the TimeZone
     * @return
     */
    protected TimeZone getTimeZone() {
		return getMbusDevice().getWebRTU().getTimeZone();
	}

    /**
     * Getter for the MBusDevice
     * @return
     */
    private MbusDevice getMbusDevice(){
		return this.mbusDevice;
	}

    /**
     * Getter for the logger
     * @return
     */
    private Logger getLogger(){
		return getMbusDevice().getLogger();
	}

    /**
     * Get the correct obisCode, with the B-field set to the physicalAddress 
     * @param obisCode
     * @return
     */
    private ObisCode getCorrectedObisCode(ObisCode obisCode) {
        return ProtocolTools.setObisCodeField(obisCode, 1, (byte) getPhysicalAddress());
    }

}

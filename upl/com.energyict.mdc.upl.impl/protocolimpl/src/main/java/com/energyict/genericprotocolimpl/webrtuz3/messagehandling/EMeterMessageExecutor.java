package com.energyict.genericprotocolimpl.webrtuz3.messagehandling;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.Limiter.ValueDefinitionType;
import com.energyict.dlms.cosem.PPPSetup.PPPAuthenticationType;
import com.energyict.genericprotocolimpl.common.*;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.common.messages.*;
import com.energyict.genericprotocolimpl.webrtu.common.csvhandling.CSVParser;
import com.energyict.genericprotocolimpl.webrtu.common.csvhandling.TestObject;
import com.energyict.genericprotocolimpl.webrtuz3.EMeter;
import com.energyict.genericprotocolimpl.webrtuz3.WebRTUZ3;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.RtuMessageShadow;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gna
 * @beginChanges
 * GNA |15042009| The dataField in the TestMessage has a '0x' prefix for better visualization.
 * GNA |04062009| Activate now is not a message value of the FirmwareUpgrade anymore, functionality is changes as it works with the disconnector
 * 					if the activation date isn't filled in, then the activation takes place immediately
 * @endChanges
 */
public class EMeterMessageExecutor extends GenericMessageExecutor{

    public static final ObisCode DISCONNECTOR_OBIS = ObisCode.fromString("0.0.96.3.10.255");

	private EMeter webRtu;
	private boolean DEBUG = false;

	private static final byte[] defaultMonitoredAttribute = new byte[]{1,0,90,7,0,(byte)255};	// Total current, instantaneous value

	public EMeterMessageExecutor(EMeter webRTU) {
		this.webRtu = webRTU;
	}

	private EMeter getWebRtu(){
		return this.webRtu;
	}

	public void doMessage(RtuMessage rtuMessage) throws BusinessException, SQLException {
		byte theMonitoredAttributeType = -1;
		boolean success = false;
		String content = rtuMessage.getContents();
		MessageHandler messageHandler = new MessageHandler();
		try {
			importMessage(content, messageHandler);

			boolean p1Text 			= messageHandler.getType().equals(RtuMessageConstant.P1TEXTMESSAGE);
			boolean p1Code 			= messageHandler.getType().equals(RtuMessageConstant.P1CODEMESSAGE);
			boolean connect			= messageHandler.getType().equals(RtuMessageConstant.CONNECT_LOAD);
			boolean disconnect		= messageHandler.getType().equals(RtuMessageConstant.DISCONNECT_LOAD);
			boolean connectMode		= messageHandler.getType().equals(RtuMessageConstant.CONNECT_CONTROL_MODE);
			boolean llConfig		= messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_CONFIGURE);
			boolean llClear			= messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_DISABLE);
			boolean llSetGrId		= messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_EMERGENCY_PROFILE_GROUP_ID_LIST);
			boolean globalReset		= messageHandler.getType().equals(RtuMessageConstant.GLOBAL_METER_RESET);

            if(p1Code){

				log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Consumer message Code");

				Data dataCode = getCosemObjectFactory().getData(getMeterConfig().getConsumerMessageCode().getObisCode());
				dataCode.setValueAttr(OctetString.fromString(messageHandler.getP1Code()));

				success = true;


			} else if(p1Text){

				log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Consumer message Text");

				Data dataCode = getCosemObjectFactory().getData(getMeterConfig().getConsumerMessageText().getObisCode());
				dataCode.setValueAttr(OctetString.fromString(messageHandler.getP1Text()));

				success = true;

			} else if(connect){

				log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Connect");

				if(!messageHandler.getConnectDate().equals("")){	// use the disconnectControlScheduler

					Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getConnectDate());
					SingleActionSchedule sasConnect = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getDisconnectControlSchedule().getObisCode());

					ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getMeterConfig().getDisconnectorScriptTable().getObisCode());
					byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn();
					Structure scriptStruct = new Structure();
					scriptStruct.addDataType(new OctetString(scriptLogicalName));
					scriptStruct.addDataType(new Unsigned16(2)); 	// method '2' is the 'remote_connect' method

					sasConnect.writeExecutedScript(scriptStruct);
					sasConnect.writeExecutionTime(executionTimeArray);

				} else {	// immediate connect
					Disconnector connector = getCosemObjectFactory().getDisconnector();
					connector.remoteReconnect();
				}

				success = true;
			} else if(disconnect){

				log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Disconnect");

				if(!messageHandler.getDisconnectDate().equals("")){ // use the disconnectControlScheduler

					Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getDisconnectDate());
					SingleActionSchedule sasDisconnect = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getDisconnectControlSchedule().getObisCode());

					ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getMeterConfig().getDisconnectorScriptTable().getObisCode());
					byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn();
					Structure scriptStruct = new Structure();
					scriptStruct.addDataType(new OctetString(scriptLogicalName));
					scriptStruct.addDataType(new Unsigned16(1));	// method '1' is the 'remote_disconnect' method

					sasDisconnect.writeExecutedScript(scriptStruct);
					sasDisconnect.writeExecutionTime(executionTimeArray);

				} else { 	// immediate disconnect
					Disconnector disconnector = getCosemObjectFactory().getDisconnector();
					disconnector.remoteDisconnect();
				}

				success = true;
			} else if(connectMode){

				log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": ConnectControl mode");
				String mode = messageHandler.getConnectControlMode();

				if(mode != null){
					try {
						int modeInt = Integer.parseInt(mode);

						if((modeInt >=0) && (modeInt <=6)){
							Disconnector connectorMode = getCosemObjectFactory().getDisconnector();
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
			} else if (llClear){

				log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Clear LoadLimit configuration");

				Limiter clearLLimiter = getCosemObjectFactory().getLimiter();

				// first do it the Iskra way, if it fails do it oure way

				Structure emptyStruct = new Structure();
				emptyStruct.addDataType(new Unsigned16(0));
				emptyStruct.addDataType(new OctetString(new byte[14]));
				emptyStruct.addDataType(new Unsigned32(0));
				try {
					clearLLimiter.writeEmergencyProfile(clearLLimiter.new EmergencyProfile(emptyStruct.getBEREncodedByteArray(), 0, 0));
				} catch (IOException e) {
					e.printStackTrace();
					if(e.getMessage().indexOf("Could not write the emergencyProfile structure.Cosem Data-Access-Result exception Type unmatched") != -1){ // do it oure way
						emptyStruct = new Structure();
						emptyStruct.addDataType(new NullData());
						emptyStruct.addDataType(new NullData());
						emptyStruct.addDataType(new NullData());
						clearLLimiter.writeEmergencyProfile(clearLLimiter.new EmergencyProfile(emptyStruct.getBEREncodedByteArray(), 0, 0));
					} else {
						throw e;
					}
				}

				success = true;
			} else if (llConfig){

				log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Set LoadLimit configuration");

				Limiter loadLimiter = getCosemObjectFactory().getLimiter();

				if(theMonitoredAttributeType == -1){	// check for the type of the monitored value
					ValueDefinitionType valueDefinitionType = loadLimiter.getMonitoredValue();
					if(valueDefinitionType.getClassId().getValue() == 0){
						setMonitoredValue(loadLimiter);
						valueDefinitionType = loadLimiter.readMonitoredValue();
					}
					theMonitoredAttributeType = getMonitoredAttributeType(valueDefinitionType);
				}

				// Write the normalThreshold
				if(messageHandler.getNormalThreshold() != null){
					try {
						loadLimiter.writeThresholdNormal(convertToMonitoredType(theMonitoredAttributeType, messageHandler.getNormalThreshold()));
					} catch (NumberFormatException e) {
						e.printStackTrace();
						log(Level.INFO, "Could not pars the normalThreshold value to an integer.");
						throw new IOException("Could not pars the normalThreshold value to an integer." + e.getMessage());
					}
				}

				// Write the emergencyThreshold
				if(messageHandler.getEmergencyThreshold() != null){
					try{
						loadLimiter.writeThresholdEmergency(convertToMonitoredType(theMonitoredAttributeType, messageHandler.getEmergencyThreshold()));
					} catch (NumberFormatException e) {
						e.printStackTrace();
						log(Level.INFO, "Could not pars the emergencyThreshold value to an integer.");
						throw new IOException("Could not pars the emergencyThreshold value to an integer." + e.getMessage());
					}
				}

				// Write the minimumOverThresholdDuration
				if(messageHandler.getOverThresholdDurtion() != null){
					try{
						loadLimiter.writeMinOverThresholdDuration(new Unsigned32(Integer.parseInt(messageHandler.getOverThresholdDurtion())));
					} catch (NumberFormatException e) {
						e.printStackTrace();
						log(Level.INFO, "Could not pars the minimum over threshold duration value to an integer.");
						throw new IOException("Could not pars the minimum over threshold duration value to an integer." + e.getMessage());
					}
				}

				// Construct the emergencyProfile
				Structure emergencyProfile = new Structure();
				if(messageHandler.getEpProfileId() != null){	// The EmergencyProfileID
					try {
						emergencyProfile.addDataType(new Unsigned16(Integer.parseInt(messageHandler.getEpProfileId())));
					} catch (NumberFormatException e) {
						e.printStackTrace();
						log(Level.INFO, "Could not pars the emergency profile id value to an integer.");
						throw new IOException("Could not pars the emergency profile id value to an integer." + e.getMessage());
					}
				}
				if(messageHandler.getEpActivationTime() != null){	// The EmergencyProfileActivationTime
					try{
//						emergencyProfile.addDataType(new OctetString(convertStringToDateTimeOctetString(messageHandler.getEpActivationTime()).getBEREncodedByteArray(), 0, true));
						emergencyProfile.addDataType(new OctetString(convertUnixToGMTDateTime(messageHandler.getEpActivationTime()).getBEREncodedByteArray(), 0, true));
					} catch (NumberFormatException e) {
						e.printStackTrace();
						log(Level.INFO, "Could not pars the emergency profile activationTime value to a valid date.");
						throw new IOException("Could not pars the emergency profile activationTime value to a valid date." + e.getMessage());
					}
				}
				if(messageHandler.getEpDuration() != null){		// The EmergencyProfileDuration
					try{
						emergencyProfile.addDataType(new Unsigned32(Integer.parseInt(messageHandler.getEpDuration())));
					} catch (NumberFormatException e) {
						e.printStackTrace();
						log(Level.INFO, "Could not pars the emergency profile duration value to an integer.");
						throw new IOException("Could not pars the emergency profile duration value to an integer." + e.getMessage());
					}
				}
				if((emergencyProfile.nrOfDataTypes() > 0) && (emergencyProfile.nrOfDataTypes() != 3)){	// If all three elements are correct, then send it, otherwise throw error
					throw new IOException("The complete emergecy profile must be filled in before sending it to the meter.");
				} else {
					if(emergencyProfile.nrOfDataTypes() > 0){
						loadLimiter.writeEmergencyProfile(emergencyProfile.getBEREncodedByteArray());
					}
				}

				success = true;
			} else if (llSetGrId){

				log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Set LoadLimit EmergencyProfile group ID's");

				Limiter epdiLimiter = getCosemObjectFactory().getLimiter();
				try {
					Lookup lut = mw().getLookupFactory().find(Integer.parseInt(messageHandler.getEpGroupIdListLookupTableId()));
					if(lut == null){
						throw new IOException("No lookuptable defined with id '" + messageHandler.getEpGroupIdListLookupTableId() + "'");
					} else {
						Iterator entriesIt = lut.getEntries().iterator();
						Array idArray = new Array();
						while(entriesIt.hasNext()){
							LookupEntry lue = (LookupEntry)entriesIt.next();
							idArray.addDataType(new Unsigned16(lue.getKey()));
						}
						epdiLimiter.writeEmergencyProfileGroupIdList(idArray);
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
					throw new IOException("The given lookupTable id is not a valid entry.");
				}

				success = true;

			} else if(globalReset){

				log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Global Meter Reset.");
				ScriptTable globalResetST = getCosemObjectFactory().getGlobalMeterResetScriptTable();
				globalResetST.invoke(1);	// execute script one

				success = true;
			} else {
				success = false;
			}

		} catch (BusinessException e) {
			e.printStackTrace();
			log(Level.INFO, "Message " + rtuMessage.displayString() + " has failed. " + e.getMessage());
		} catch (ConnectionException e){
			e.printStackTrace();
			log(Level.INFO, "Message " + rtuMessage.displayString() + " has failed. " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			log(Level.INFO, "Message " + rtuMessage.displayString() + " has failed. " + e.getMessage());
        } finally {
			if(success){
				rtuMessage.confirm();
				log(Level.INFO, "Message " + rtuMessage.displayString() + " has finished.");
			} else {
				rtuMessage.setFailed();
			}
		}
	}

	private void setMonitoredValue(Limiter loadLimiter) throws IOException {
		ValueDefinitionType vdt = loadLimiter.new ValueDefinitionType();
		vdt.addDataType(new Unsigned16(3));
		OctetString os = new OctetString(defaultMonitoredAttribute);
		vdt.addDataType(os);
		vdt.addDataType(new Integer8(2));
		loadLimiter.writeMonitoredValue(vdt);
	}

	/**
	 * Get the monitoredAttributeType
	 * @param vdt
	 * @return the abstractDataType of the monitored attribute
	 * @throws java.io.IOException
	 */
	private byte getMonitoredAttributeType(ValueDefinitionType vdt) throws IOException{

      if (getMeterConfig().getClassId(vdt.getObisCode()) == Register.CLASSID){
    	  return getCosemObjectFactory().getRegister(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
      } else if (getMeterConfig().getClassId(vdt.getObisCode()) == ExtendedRegister.CLASSID){
    	  return getCosemObjectFactory().getExtendedRegister(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
      }else if (getMeterConfig().getClassId(vdt.getObisCode()) == DLMSClassId.DEMAND_REGISTER.getClassId()){
    	  return getCosemObjectFactory().getDemandRegister(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
      }else if (getMeterConfig().getClassId(vdt.getObisCode()) == Data.CLASSID){
    	  return getCosemObjectFactory().getData(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
      } else{
    	  throw new IOException("WebRtuKP, getMonitoredAttributeType, invalid classID " + getMeterConfig().getClassId(vdt.getObisCode())+" for obisCode "+vdt.getObisCode().toString()) ;
      }
	}

	/**
	 * Convert the value to write to the Limiter object to the correct monitored value type ...
	 * @param theMonitoredAttributeType
	 * @param value
	 * @return
	 * @throws java.io.IOException
	 */
	private AbstractDataType convertToMonitoredType(byte theMonitoredAttributeType, String value) throws IOException {
		try {
			switch(theMonitoredAttributeType){
			case DLMSCOSEMGlobals.TYPEDESC_NULL:{return new NullData();}
			case DLMSCOSEMGlobals.TYPEDESC_BOOLEAN:{return new BooleanObject(value.equalsIgnoreCase("1"));}
			case DLMSCOSEMGlobals.TYPEDESC_BITSTRING:{return new BitString(Integer.parseInt(value));}
			case DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG:{return new Integer32(Integer.parseInt(value));}
			case DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG_UNSIGNED:{return new Unsigned32(Integer.parseInt(value));}
			case DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING:{return OctetString.fromString(value);}
			case DLMSCOSEMGlobals.TYPEDESC_VISIBLE_STRING:{return new VisibleString(value);}
			case DLMSCOSEMGlobals.TYPEDESC_INTEGER:{return new Integer8(Integer.parseInt(value));}
			case DLMSCOSEMGlobals.TYPEDESC_LONG:{return new Integer16(Integer.parseInt(value));}
			case DLMSCOSEMGlobals.TYPEDESC_UNSIGNED:{return new Unsigned8(Integer.parseInt(value));}
			case DLMSCOSEMGlobals.TYPEDESC_LONG_UNSIGNED:{return new Unsigned16(Integer.parseInt(value));}
			case DLMSCOSEMGlobals.TYPEDESC_LONG64:{return new Integer64(Integer.parseInt(value));}
			case DLMSCOSEMGlobals.TYPEDESC_ENUM:{return new TypeEnum(Integer.parseInt(value));}
			default:
			    throw new IOException("convertToMonitoredtype error, unknown type.");
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new NumberFormatException();
		}
	}

	private void log(Level level, String msg){
		getLogger().log(level, msg);
	}

	private Logger getLogger() {
		return getWebRtu().getLogger();
	}

	private CosemObjectFactory getCosemObjectFactory() {
		return getWebRtu().getCosemObjectFactory();
	}

	private DLMSMeterConfig getMeterConfig() {
		return getWebRtu().getMeterConfig();
	}

	/**
	 * @return the current MeteringWarehouse
	 */
	private MeteringWarehouse mw(){
		return CommonUtils.mw();
	}

	protected TimeZone getTimeZone() {
		return getWebRtu().getTimeZone();
	}


}
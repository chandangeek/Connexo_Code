package com.energyict.genericprotocolimpl.nta.messagehandling;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Integer16;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.Integer64;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.VisibleString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.AssociationSN;
import com.energyict.dlms.cosem.AutoConnect;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.GenericInvoke;
import com.energyict.dlms.cosem.GenericRead;
import com.energyict.dlms.cosem.GenericWrite;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.Limiter;
import com.energyict.dlms.cosem.Limiter.ValueDefinitionType;
import com.energyict.dlms.cosem.PPPSetup;
import com.energyict.dlms.cosem.PPPSetup.PPPAuthenticationType;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.dlms.cosem.SecuritySetup;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.SpecialDaysTable;
import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.common.messages.ActivityCalendarMessage;
import com.energyict.genericprotocolimpl.common.messages.MessageHandler;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractNTAProtocol;
import com.energyict.genericprotocolimpl.webrtu.common.csvhandling.CSVParser;
import com.energyict.genericprotocolimpl.webrtu.common.csvhandling.TestObject;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.CodeCalendar;
import com.energyict.mdw.core.OldDeviceMessage;
import com.energyict.mdw.core.Lookup;
import com.energyict.mdw.core.LookupEntry;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.shadow.OldDeviceMessageShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
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
public class MessageExecutor extends GenericMessageExecutor{
	
	private AbstractNTAProtocol webRtu;
	private boolean DEBUG = false;
	
//	private String[] weekNames = {"a", "b", "c", "d"};

	private static final byte[] defaultMonitoredAttribute = new byte[]{1,0,90,7,0,(byte)255};	// Total current, instantaneous value
	
	public MessageExecutor(AbstractNTAProtocol webRTUKP) {
		this.webRtu = webRTUKP;
	}
	
	private AbstractNTAProtocol getWebRtu(){
		return this.webRtu;
	}

	public void doMessage(OldDeviceMessage rtuMessage) throws BusinessException, SQLException {
		byte theMonitoredAttributeType = -1;
		boolean success = false;
		String content = rtuMessage.getContents();
		MessageHandler messageHandler = new NTAMessageHandler();
		try {
			importMessage(content, messageHandler);
			
			boolean xmlConfig		= messageHandler.getType().equals(RtuMessageConstant.XMLCONFIG);
			boolean firmware		= messageHandler.getType().equals(RtuMessageConstant.FIRMWARE_UPGRADE);
			boolean p1Text 			= messageHandler.getType().equals(RtuMessageConstant.P1TEXTMESSAGE);
			boolean p1Code 			= messageHandler.getType().equals(RtuMessageConstant.P1CODEMESSAGE);
			boolean connect			= messageHandler.getType().equals(RtuMessageConstant.CONNECT_LOAD);
			boolean disconnect		= messageHandler.getType().equals(RtuMessageConstant.DISCONNECT_LOAD);
			boolean connectMode		= messageHandler.getType().equals(RtuMessageConstant.CONNECT_CONTROL_MODE);
			boolean llConfig		= messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_CONFIGURE);
			boolean llClear			= messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_DISABLE);
			boolean llSetGrId		= messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_EMERGENCY_PROFILE_GROUP_ID_LIST);
			boolean touCalendar		= messageHandler.getType().equals(RtuMessageConstant.TOU_ACTIVITY_CAL);
			boolean touSpecialDays 	= messageHandler.getType().equals(RtuMessageConstant.TOU_SPECIAL_DAYS);
			boolean specialDelEntry	= messageHandler.getType().equals(RtuMessageConstant.TOU_SPECIAL_DAYS_DELETE);
			boolean setTime			= messageHandler.getType().equals(RtuMessageConstant.SET_TIME);
			boolean fillUpDB		= messageHandler.getType().equals(RtuMessageConstant.ME_MAKING_ENTRIES);
			boolean gprsParameters 	= messageHandler.getType().equals(RtuMessageConstant.GPRS_MODEM_SETUP);
            boolean gprsCredentials = messageHandler.getType().equals(RtuMessageConstant.GPRS_MODEM_CREDENTIALS);
			boolean testMessage 	= messageHandler.getType().equals(RtuMessageConstant.TEST_MESSAGE);
			boolean globalReset		= messageHandler.getType().equals(RtuMessageConstant.GLOBAL_METER_RESET);
			boolean wakeUpWhiteList = messageHandler.getType().equals(RtuMessageConstant.WAKEUP_ADD_WHITELIST);
			boolean changeHLSSecret = messageHandler.getType().equals(RtuMessageConstant.AEE_CHANGE_HLS_SECRET);
			boolean changeLLSSecret = messageHandler.getType().equals(RtuMessageConstant.AEE_CHANGE_LLS_SECRET);
			boolean changeGlobalkey = messageHandler.getType().equals(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_ENCRYPTION_KEY);
			boolean changeAuthkey 	= messageHandler.getType().equals(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_AUTHENTICATION_KEY);
			boolean activateSMS		= messageHandler.getType().equals(RtuMessageConstant.WAKEUP_ACTIVATE);
			boolean deActivateSMS 	= messageHandler.getType().equals(RtuMessageConstant.WAKEUP_DEACTIVATE);
			boolean actSecuritLevel = messageHandler.getType().equals(RtuMessageConstant.AEE_ACTIVATE_SECURITY);
            boolean changeAuthLevel = messageHandler.getType().equals(RtuMessageConstant.AEE_CHANGE_AUTHENTICATION_LEVEL);
            boolean resetAlarmRegisterRequest = messageHandler.getType().equals(RtuMessageConstant.RESET_ALARM_REGISTER);
                boolean isChangeDefaultResetWindow = messageHandler.getType().equals(RtuMessageConstant.CHANGE_DEFAULT_RESET_WINDOW);
			
			if(xmlConfig){
				
				log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": XmlConfig");
				
				String xmlConfigStr = getMessageValue(content, RtuMessageConstant.XMLCONFIG);
				
				getCosemObjectFactory().getData(getMeterConfig().getXMLConfig().getObisCode()).setValueAttr(OctetString.fromString(xmlConfigStr));
				
				success = true;
				
			} else if(firmware){
				
				log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Firmware upgrade");
				
				String userFileID = messageHandler.getUserFileId();
				if(DEBUG) {
					System.out.println("UserFileID: " + userFileID);
				}
				
				if(!ParseUtils.isInteger(userFileID)){
					String str = "Not a valid entry for the current meter message (" + content + ").";
            		throw new IOException(str);
				} 
				UserFile uf = mw().getUserFileFactory().find(Integer.parseInt(userFileID));
				if(!(uf instanceof UserFile )){
					String str = "Not a valid entry for the userfileID " + userFileID;
					throw new IOException(str);
				}
				
				byte[] imageData = uf.loadFileInByteArray();
				ImageTransfer it = getCosemObjectFactory().getImageTransfer();
				it.upgrade(imageData);
				if(DEBUG) {
					System.out.println("UserFile is send to the device.");
				}
				if(messageHandler.getActivationDate().equalsIgnoreCase("")){ // Do an execute now
					if(DEBUG) {
						System.out.println("Start the activateNow.");
					}
					it.imageActivation();
					
					//Below is a solution for not immediately activating the image so the current connection isn't lost
//					Calendar cal = Calendar.getInstance();
//					cal.add(Calendar.MINUTE, 2);
//					SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
//					String strDate = Long.toString(cal.getTimeInMillis()/1000);
//					Array dateArray = convertUnixToDateTimeArray(strDate);
//					
//					sas.writeExecutionTime(dateArray);
					
					if(DEBUG) {
						System.out.println("ActivateNow complete.");
					}
				} else if(!messageHandler.getActivationDate().equalsIgnoreCase("")){
					SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
					String strDate = messageHandler.getActivationDate();
					Array dateArray = convertUnixToDateTimeArray(strDate);
					if(DEBUG) {
						System.out.println("Write the executionTime");
					}
					sas.writeExecutionTime(dateArray);
					if(DEBUG) {
						System.out.println("ExecutionTime sent...");
					}
				}
				
				success = true;
				
			} else if(p1Code){
				
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
					scriptStruct.addDataType(OctetString.fromByteArray(scriptLogicalName));
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
					scriptStruct.addDataType(OctetString.fromByteArray(scriptLogicalName));
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
				emptyStruct.addDataType(OctetString.fromByteArray(new byte[14]));
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
				
			} else if(touCalendar){
				
				log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Set Activity calendar");
				
				String name = messageHandler.getTOUCalendarName();
				String activateDate = messageHandler.getTOUActivationDate();
				String codeTable = messageHandler.getTOUCodeTable();
				String userFile = messageHandler.getTOUUserFile();
				
				if((codeTable == null) &&(userFile == null)){
					throw new IOException("CodeTable-ID AND UserFile-ID can not be both empty.");
				} else if((codeTable != null) &&(userFile != null)){
					throw new IOException("CodeTable-ID AND UserFile-ID can not be both filled in.");
				}
				
				if(codeTable != null){
					
					Code ct = mw().getCodeFactory().find(Integer.parseInt(codeTable));
					if(ct == null){
						throw new IOException("No CodeTable defined with id '" + codeTable + "'");
					} else {
						
						ActivityCalendarMessage acm = new ActivityCalendarMessage(ct, getMeterConfig());
						acm.parse();
						
						ActivityCalendar ac = getCosemObjectFactory().getActivityCalendar(getMeterConfig().getActivityCalendar().getObisCode());
						ac.writeSeasonProfilePassive(acm.getSeasonProfile());
						ac.writeWeekProfileTablePassive(acm.getWeekProfile());
						ac.writeDayProfileTablePassive(acm.getDayProfile());
						
						if(name != null){
							if(name.length() > 8){
								name = name.substring(0, 8);
							}
							ac.writeCalendarNamePassive(OctetString.fromString(name));
						} 
						if(activateDate != null){
//							ac.writeActivatePassiveCalendarTime(new OctetString(convertStringToDateTimeOctetString(activateDate).getBEREncodedByteArray(), 0, true));
							ac.writeActivatePassiveCalendarTime(new OctetString(convertUnixToGMTDateTime(activateDate).getBEREncodedByteArray(), 0));
						}
						
					}
					
				} else if(userFile != null){
					throw new IOException("ActivityCalendar by userfile is not supported yet.");
				} else {
					// should never get here 
					throw new IOException("CodeTable-ID AND UserFile-ID can not be both empty.");
				}
				
				success = true;
				
			} else if(touSpecialDays){
				
				log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Set Special Days table");
				
				String codeTable = messageHandler.getSpecialDaysCodeTable();
				
				if(codeTable == null){
					throw new IOException("CodeTable-ID can not be empty.");
				} else {
					
					Code ct = mw().getCodeFactory().find(Integer.parseInt(codeTable));
					if(ct == null){
						throw new IOException("No CodeTable defined with id '" + codeTable + "'");
					} else {

						List calendars = ct.getCalendars();
						Array sdArray = new Array();

						SpecialDaysTable sdt = getCosemObjectFactory().getSpecialDaysTable(getMeterConfig().getSpecialDaysTable().getObisCode());
						
						for(int i = 0; i < calendars.size(); i++){
							CodeCalendar cc = (CodeCalendar)calendars.get(i);
							if(cc.getSeason() == 0){
								OctetString os = OctetString.fromByteArray(new byte[]{(byte) ((cc.getYear()==-1)?0xff:((cc.getYear()>>8)&0xFF)), (byte) ((cc.getYear()==-1)?0xff:(cc.getYear())&0xFF),
										(byte) ((cc.getMonth()==-1)?0xFF:cc.getMonth()), (byte) ((cc.getDay()==-1)?0xFF:cc.getDay()),
										(byte) ((cc.getDayOfWeek()==-1)?0xFF:cc.getDayOfWeek())});
								Unsigned8 dayType = new Unsigned8(cc.getDayType().getId());
								Structure struct = new Structure();
								AXDRDateTime dt = new AXDRDateTime(new byte[]{(byte)0x09,(byte)0x0C, (byte) ((cc.getYear()==-1)?0x07:((cc.getYear()>>8)&0xFF)), (byte) ((cc.getYear()==-1)?0xB2:(cc.getYear())&0xFF), 
										(byte) ((cc.getMonth()==-1)?0xFF:cc.getMonth()), (byte) ((cc.getDay()==-1)?0xFF:cc.getDay()),
										(byte) ((cc.getDayOfWeek()==-1)?0xFF:cc.getDayOfWeek()), 0, 0, 0, 0, 0, 0, 0});
								long days = dt.getValue().getTimeInMillis()/1000/60/60/24;
								struct.addDataType(new Unsigned16((int)days));
								struct.addDataType(os);
								struct.addDataType(dayType);
//								sdt.insert(struct);
								sdArray.addDataType(struct);
							}
						}
						
						if(sdArray.nrOfDataTypes() != 0){
							sdt.writeSpecialDays(sdArray);
						}
						
						success = true;
					}
				}
			} else if(specialDelEntry){
				try {
					SpecialDaysTable sdt = getCosemObjectFactory().getSpecialDaysTable(getMeterConfig().getSpecialDaysTable().getObisCode());
					sdt.delete(Integer.parseInt(messageHandler.getSpecialDayDeleteEntry()));
				} catch (NumberFormatException e) {
					e.printStackTrace();
					throw new IOException("Delete index is not a valid entry");
				}
				
				success = true;
			} else if(setTime){
				
				String epochTime = messageHandler.getEpochTime();
				log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Setting the device time to: " + convertUnixToGMTDateTime(epochTime).getValue().getTime());
				getWebRtu().forceClock(convertUnixToGMTDateTime(epochTime).getValue().getTime());
				success = true;
				
			} else if(fillUpDB){
				
				log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Making database entries.");
				log(Level.INFO, "(This can take several minutes/houres, depending on the number of entries you want to simulate)");
				
				if(messageHandler.getMEEntries() > 0){
					// Start the entry making ...
					
					int entries = messageHandler.getMEEntries();
					String type = messageHandler.getMEInterval();
					Long millis = Long.parseLong(messageHandler.getMEStartDate())*1000;
					Date startTime = new Date(Long.parseLong(messageHandler.getMEStartDate())*1000);
					startTime = getFirstDate(startTime, type);
					while(entries > 0){
						log(Level.INFO, "Setting meterTime to: " + startTime );
						getWebRtu().setClock(startTime);
						waitForCrossingBoundry();
						startTime = setBeforeNextInterval(startTime, type);
						entries--;
					}
				}
				
				if(messageHandler.getMESyncAtEnd()){
	        		Date currentTime = Calendar.getInstance(getTimeZone()).getTime();
	        		getLogger().log(Level.INFO, "Synced clock to: " + currentTime);
	        		getWebRtu().forceClock(currentTime);
				}
				
				success = true;
				
			} else if(gprsParameters){
				
				log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Changing gprs modem parameters");
				
				PPPAuthenticationType pppat = getCosemObjectFactory().getPPPSetup().new PPPAuthenticationType();
				pppat.setAuthenticationType(PPPSetup.LCPOptionsType.AUTH_PAP);
				if(messageHandler.getGprsUsername() != null){
					pppat.setUserName(messageHandler.getGprsUsername());
				}
				if(messageHandler.getGprsPassword() != null){
					pppat.setPassWord(messageHandler.getGprsPassword());
				}
				if((messageHandler.getGprsUsername() != null) || (messageHandler.getGprsPassword() != null)){
					getCosemObjectFactory().getPPPSetup().writePPPAuthenticationType(pppat);
				}
				
				if(messageHandler.getGprsApn() != null){
					getCosemObjectFactory().getGPRSModemSetup().writeAPN(messageHandler.getGprsApn());
				}

                success = true;
            } else if (gprsCredentials) {
                log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Changing gprs modem credentials");

                PPPAuthenticationType pppat = getCosemObjectFactory().getPPPSetup().new PPPAuthenticationType();
                pppat.setAuthenticationType(PPPSetup.LCPOptionsType.AUTH_PAP);
                if (messageHandler.getGprsUsername() != null) {
                    pppat.setUserName(messageHandler.getGprsUsername());
                }
                if (messageHandler.getGprsPassword() != null) {
                    pppat.setPassWord(messageHandler.getGprsPassword());
                }
                if ((messageHandler.getGprsUsername() != null) || (messageHandler.getGprsPassword() != null)) {
                    getCosemObjectFactory().getPPPSetup().writePPPAuthenticationType(pppat);
                }

                success = true;
            } else if(testMessage){
				
				log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": TestMessage");
				int failures = 0;
				String userFileId = messageHandler.getTestUserFileId();
				Date currentTime;
				if(!userFileId.equalsIgnoreCase("")){
					if(ParseUtils.isInteger(userFileId)){
						UserFile uf = mw().getUserFileFactory().find(Integer.parseInt(userFileId));
						if(uf != null){
							byte[] data = uf.loadFileInByteArray();
							CSVParser csvParser = new CSVParser();
							csvParser.parse(data);
							boolean hasWritten;
							TestObject to = new TestObject("");
							for(int i = 0; i < csvParser.size(); i++){
								to = csvParser.getTestObject(i);
								if(csvParser.isValidLine(to)){
									currentTime = new Date(System.currentTimeMillis());
									hasWritten = false;
									try {
										switch(to.getType()){
										case 0 :{ // GET
											GenericRead gr = getCosemObjectFactory().getGenericRead(to.getObisCode(), DLMSUtils.attrLN2SN(to.getAttribute()), to.getClassId());
											to.setResult("0x"+ParseUtils.decimalByteToString(gr.getResponseData()));
											hasWritten = true;
										}break;
										case 1 :{ // SET
											GenericWrite gw = getCosemObjectFactory().getGenericWrite(to.getObisCode(), to.getAttribute(), to.getClassId());
											gw.write(ParseUtils.hexStringToByteArray(to.getData()));
											to.setResult("OK");
											hasWritten = true;
										}break;
										case 2 :{ // ACTION
											GenericInvoke gi = getCosemObjectFactory().getGenericInvoke(to.getObisCode(), to.getClassId(), to.getMethod());
											if(to.getData().equalsIgnoreCase("")){
												gi.invoke();
											} else {
												gi.invoke(ParseUtils.hexStringToByteArray(to.getData()));
											}
											to.setResult("OK");
											hasWritten = true;
										}break;
										case 3 :{ // MESSAGE
											OldDeviceMessageShadow rms = new OldDeviceMessageShadow();
											rms.setContents(csvParser.getTestObject(i).getData());
											rms.setRtuId(getWebRtu().getFullShadow().getRtuShadow().getRtuId());
											OldDeviceMessage rm = mw().getRtuMessageFactory().create(rms);
											doMessage(rm);
											if(rm.getState().getId() == rm.getState().CONFIRMED.getId()){
												to.setResult("OK");
											} else {
												to.setResult("MESSAGE failed, current state " + rm.getState().getId());
											}
											hasWritten = true;
										}break;
										case 4:{ // WAIT
											waitCyclus(Integer.parseInt(to.getData()));
											to.setResult("OK");
											hasWritten = true;
										}break; 
										case 5:{
											// do nothing, it's no valid line
										}break;
										default:{
											throw new ApplicationException("Row " + i + " of the CSV file does not contain a valid type.");
										}
										}
										to.setTime(currentTime.getTime());
										
										// Check if the expected value is the same as the result
										if((to.getExpected() == null) ||(!to.getExpected().equalsIgnoreCase(to.getResult()))){
											to.setResult("Failed - " + to.getResult());
											failures++;
											getLogger().log(Level.INFO, "Test " + i + " has successfully finished, but the result didn't match the expected value.");
										} else {
											getLogger().log(Level.INFO, "Test " + i + " has successfully finished.");
										}
										
									} catch (Exception e) {
										e.printStackTrace();
										if(!hasWritten){
											if((to.getExpected() != null) && (e.getMessage().indexOf(to.getExpected()) != -1)){
												to.setResult(e.getMessage());
												getLogger().log(Level.INFO, "Test " + i + " has successfully finished.");
												hasWritten = true;
											} else {
												getLogger().log(Level.INFO, "Test " + i + " has failed.");
												String eMessage;
												if(e.getMessage().indexOf("\r\n") != -1){
													eMessage = e.getMessage().substring(0, e.getMessage().indexOf("\r\n")) + "...";
												} else {
													eMessage = e.getMessage();
												}
												to.setResult("Failed. " + eMessage);
												hasWritten = true;
												failures++;
											}
											to.setTime(currentTime.getTime());
										}
									} finally {
										if(!hasWritten){
											to.setResult("Failed - Unknow exception ...");
											failures++;
											to.setTime(currentTime.getTime());
										}
									}
								}
							}
							if(failures == 0){
								csvParser.addLine("All the tests are successfully finished.");
							} else {
								csvParser.addLine("" + failures + " of the " + csvParser.getValidSize() + " tests " + ((failures==1)?"has":"have") +" failed.");
							}
							mw().getUserFileFactory().create(csvParser.convertResultToUserFile(uf, getWebRtu().getFullShadow().getRtuShadow().getFolderId()));
						} else {
							throw new ApplicationException("Userfile with ID " + userFileId + " does not exist.");
						}
					} else {
						throw new IOException("UserFileId is not a valid number");
					}
				} else {
					throw new IOException("No userfile id is given.");
				}
				
				success = true;
			} else if(globalReset){
				
				log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Global Meter Reset.");
				ScriptTable globalResetST = getCosemObjectFactory().getGlobalMeterResetScriptTable();
				globalResetST.invoke(1);	// execute script one 
				
				success = true;
			} else if(wakeUpWhiteList){
				
				log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Setting whitelist.");
				AutoConnect autoConnect = getCosemObjectFactory().getAutoConnect();
				
				Array list = new Array();
				list.addDataType(OctetString.fromString(messageHandler.getNr1()));
				list.addDataType(OctetString.fromString(messageHandler.getNr2()));
				list.addDataType(OctetString.fromString(messageHandler.getNr3()));
				list.addDataType(OctetString.fromString(messageHandler.getNr4()));
				list.addDataType(OctetString.fromString(messageHandler.getNr5()));

				autoConnect.writeDestinationList(list);
				
				success = true;
			} else if (changeGlobalkey){
				
				Array globalKeyArray = new Array();
				Structure keyData = new Structure();
				keyData.addDataType(new TypeEnum(0));	// 0 means keyType: global unicast encryption key
				keyData.addDataType(OctetString.fromByteArray(getWebRtu().getSecurityProvider().getNEWGlobalKey()));
				globalKeyArray.addDataType(keyData);
				
				SecuritySetup ss = getCosemObjectFactory().getSecuritySetup();
				ss.transferGlobalKey(globalKeyArray);
				
				success = true;
			} else if (changeAuthkey){
				
				Array globalKeyArray = new Array();
				Structure keyData = new Structure();
				keyData.addDataType(new TypeEnum(2));	// 2 means keyType: authenticationKey
				keyData.addDataType(OctetString.fromByteArray(getWebRtu().getSecurityProvider().getNEWAuthenticationKey()));
				globalKeyArray.addDataType(keyData);
				
				SecuritySetup ss = getCosemObjectFactory().getSecuritySetup();
				ss.transferGlobalKey(globalKeyArray);
				
				success = true;
			} else if(changeHLSSecret){
				
				if(getWebRtu().getReference() == ProtocolLink.LN_REFERENCE){
					AssociationLN aln = getCosemObjectFactory().getAssociationLN();
					
					// We just return the byteArray because it is possible that the berEncoded octetString contains
					// extra check bits ...
					aln.changeHLSSecret(getWebRtu().getSecurityProvider().getNEWHLSSecret());
				} else if(getWebRtu().getReference() == ProtocolLink.SN_REFERENCE){
					AssociationSN asn = getCosemObjectFactory().getAssociationSN();
					
					// We just return the byteArray because it is possible that the berEncoded octetString contains
					// extra check bits ...
					//TODO low lever security should set the value directly to the secret attribute of the SNAssociation
					asn.changeSecret(getWebRtu().getSecurityProvider().getNEWHLSSecret());
				}
				success = true;
			} else if(changeLLSSecret){
				
				// changing the LLS secret in LN_referencing is a set of an attribute
				if(getWebRtu().getReference() == ProtocolLink.LN_REFERENCE){
					AssociationLN aln = getCosemObjectFactory().getAssociationLN();
					aln.writeSecret(OctetString.fromByteArray(getWebRtu().getSecurityProvider().getNEWLLSSecret()));
					
				// changing the LLS secret in SN_referencing is the same action as for the HLS secret
				} else if(getWebRtu().getReference() == ProtocolLink.SN_REFERENCE){
					AssociationSN asn = getCosemObjectFactory().getAssociationSN();
					
					// We just return the byteArray because it is possible that the berEncoded octetString contains
					// extra check bits ...
					//TODO low lever security should set the value directly to the secret attribute of the SNAssociation
					asn.changeSecret(getWebRtu().getSecurityProvider().getNEWHLSSecret());
				}
				
				success = true;
			} else if(activateSMS){
				getCosemObjectFactory().getAutoConnect().writeMode(4);
				success = true;
			} else if(deActivateSMS){
				getCosemObjectFactory().getAutoConnect().writeMode(1);
				success = true;
			} else if(actSecuritLevel){
				getCosemObjectFactory().getSecuritySetup().activateSecurity(new TypeEnum(messageHandler.getSecurityLevel()));
				success = true;
            } else if(changeAuthLevel){
                int newAuthLevel = messageHandler.getAuthenticationLevel();
                if (newAuthLevel != -1) {
                    if (getWebRtu().getReference() == ProtocolLink.LN_REFERENCE) {
                        AssociationLN aln = getCosemObjectFactory().getAssociationLN();
                        AbstractDataType adt = aln.readAuthenticationMechanismName();
                        if (adt.isOctetString()) {
                            byte[] octets = ((OctetString) adt).getOctetStr();
                            if (octets[octets.length - 1] != newAuthLevel) {
                                octets[octets.length - 1] = (byte) newAuthLevel;
                                aln.writeAuthenticationMechanismName(new OctetString(octets, 0));
                                success = true;
                            } else {
                                log(Level.INFO, "New authenticationLevel is the same as the one that is already configured in the device, " +
                                        "new level will not be written.");
                                success = true;
                            }
                        }
                    } else {
                        // TODO how to do this for with ShortName referencing???
                        log(Level.WARNING, "Changing authenticationLevel using ShortName referencing is not supported.");
                        success = false;
                    }

                } else {
                    log(Level.WARNING, "Message " + rtuMessage.displayString() + " contained an invalid authenticationLevel.");
                    success = false;
                }
            } else if (resetAlarmRegisterRequest) {
                log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Reset Alarm register.");
                getCosemObjectFactory().getData(ObisCode.fromString("0.0.97.98.0.255")).setValueAttr(new Unsigned32(0));
                success = true;
            } else if (isChangeDefaultResetWindow) {
                log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Change default reset window");
                getCosemObjectFactory().getRegister(ObisCode.fromString("0.0.96.50.5.255")).setValueAttr(new Unsigned32(messageHandler.getDefaultResetWindow()));
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
		} catch (InterruptedException e) {
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
		OctetString os = OctetString.fromByteArray(defaultMonitoredAttribute);
		vdt.addDataType(os);
		vdt.addDataType(new Integer8(2));
		loadLimiter.writeMonitoredValue(vdt);
	}
	
	/**
	 * Get the monitoredAttributeType
	 * @param vdt
	 * @return the abstractDataType of the monitored attribute
	 * @throws IOException
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
	 * @throws IOException
	 */
	private AbstractDataType convertToMonitoredType(byte theMonitoredAttributeType, String value) throws IOException {
		try {
            final AxdrType axdrType = AxdrType.fromTag(theMonitoredAttributeType);
            switch(axdrType){
			case NULL:{return new NullData();}
			case BOOLEAN:{return new BooleanObject(value.equalsIgnoreCase("1"));}
			case BIT_STRING:{return new BitString(Integer.parseInt(value));}
			case DOUBLE_LONG:{return new Integer32(Integer.parseInt(value));}
			case DOUBLE_LONG_UNSIGNED:{return new Unsigned32(Integer.parseInt(value));}
			case OCTET_STRING:{return OctetString.fromString(value);}
			case VISIBLE_STRING:{return new VisibleString(value);}
			case INTEGER:{return new Integer8(Integer.parseInt(value));}
			case LONG:{return new Integer16(Integer.parseInt(value));}
			case UNSIGNED:{return new Unsigned8(Integer.parseInt(value));}
			case LONG_UNSIGNED:{return new Unsigned16(Integer.parseInt(value));}
			case LONG64:{return new Integer64(Integer.parseInt(value));}
			case ENUM:{return new TypeEnum(Integer.parseInt(value));}
			default:    
			    throw new IOException("convertToMonitoredtype error, unknown type.");
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new NumberFormatException();
		}
	}
	
	
	// You are doing one step to many
	private ArrayList getSortedList(HashMap seasonsProfile) throws IOException {
		LinkedList list = new LinkedList();
		Structure struct;
		Iterator it = seasonsProfile.entrySet().iterator();
		boolean check;
		while(it.hasNext()){
			Map.Entry entry = (Map.Entry)it.next();
			AXDRDateTime dt = new AXDRDateTime((OctetString)entry.getKey());
			check = false;
//			for(int i = 0; i < list.size(); i++){
//				if(dt.getValue().getTime().before((new AXDRDateTime((OctetString)((Structure)list.get(i)).getDataType(0))).getValue().getTime())){
//					struct = new Structure();
//					struct.addDataType((OctetString)entry.getKey());
//					struct.addDataType(new Unsigned8((Integer)entry.getValue()));
//					list.add(i, struct);
//					check = true;
//					break;
//				}
//			}
			if(!check){
				struct = new Structure();
				struct.addDataType((OctetString)entry.getKey());
				struct.addDataType(new Unsigned8((Integer)entry.getValue()));
				list.add(struct);
			}
		}
		
		return new ArrayList(list);
	}

	private Date getFirstDate(Date startTime, String type) throws IOException{
		return getFirstDate(startTime, type, getWebRtu().getTimeZone());
		}
	
	private Date getFirstDate(Date startTime, String type, TimeZone timeZone) throws IOException{
		Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal1.setTime(startTime);
		cal1.getTime();
		if(type.equalsIgnoreCase("15")){
			if(cal1.get(Calendar.MINUTE) < 15){
				cal1.set(Calendar.MINUTE, 14);
				cal1.set(Calendar.SECOND, 40);
			} else if(cal1.get(Calendar.MINUTE) < 30){
				cal1.set(Calendar.MINUTE, 29);
				cal1.set(Calendar.SECOND, 40);
			} else if(cal1.get(Calendar.MINUTE) < 45){
				cal1.set(Calendar.MINUTE, 44);
				cal1.set(Calendar.SECOND, 40);
			} else {
				cal1.set(Calendar.MINUTE, 59);
				cal1.set(Calendar.SECOND, 40);
			}
			return cal1.getTime();
		} else if(type.equalsIgnoreCase("day")){
			cal1.set(Calendar.HOUR_OF_DAY, (23 - (timeZone.getOffset(startTime.getTime())/3600000)));
			cal1.set(Calendar.MINUTE, 59);
			cal1.set(Calendar.SECOND, 40);
			return cal1.getTime();
		} else if(type.equalsIgnoreCase("month")){
			cal1.set(Calendar.DATE, cal1.getActualMaximum(Calendar.DAY_OF_MONTH));
			cal1.set(Calendar.HOUR_OF_DAY, (23 - (timeZone.getOffset(startTime.getTime())/3600000)));
			cal1.set(Calendar.MINUTE, 59);
			cal1.set(Calendar.SECOND, 40);
			return cal1.getTime();
		}
		
		throw new IOException("Invalid intervaltype.");
	}

	private void waitForCrossingBoundry() throws IOException{
		try {
			for(int i = 0; i < 3; i++){
				ProtocolTools.delay(15000);
				log(Level.INFO, "Keeping connection alive");
				getWebRtu().getTime();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not keep connection alive." + e.getMessage());
		}
	}

	private Date setBeforeNextInterval(Date startTime, String type) throws IOException {
		return setBeforeNextInterval(startTime, type, getWebRtu().getTimeZone());
	}
	
	private Date setBeforeNextInterval(Date startTime, String type, TimeZone timeZone) throws IOException{
		Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal1.setTime(startTime);
		int zoneOffset = 0;
		if(type.equalsIgnoreCase("15")){
			cal1.add(Calendar.MINUTE, 15);
			return cal1.getTime();
		} else if(type.equalsIgnoreCase("day")){
			zoneOffset = timeZone.getOffset(cal1.getTimeInMillis()) / 3600000;
			cal1.add(Calendar.DAY_OF_MONTH, 1);
			zoneOffset = zoneOffset - (timeZone.getOffset(cal1.getTimeInMillis()) / 3600000);
			cal1.add(Calendar.HOUR_OF_DAY, zoneOffset);
			return cal1.getTime();
		} else if(type.equalsIgnoreCase("month")){
			zoneOffset = timeZone.getOffset(cal1.getTimeInMillis()) / 3600000;
			cal1.add(Calendar.MONTH, 1);
			cal1.set(Calendar.DATE, cal1.getActualMaximum(Calendar.DAY_OF_MONTH));
			zoneOffset = zoneOffset - (timeZone.getOffset(cal1.getTimeInMillis()) / 3600000);
			cal1.add(Calendar.HOUR_OF_DAY, zoneOffset);
			return cal1.getTime();
		}
		
		throw new IOException("Invalid intervaltype.");
	}
	
	private void waitCyclus(int delay) throws IOException{
		try {
			int nrOfPolls = (delay/((getConnectionMode()==0)?10:20)) + (delay%((getConnectionMode()==0)?10:20)==0?0:1);
			for(int i = 0; i < nrOfPolls; i++){
				if(i < nrOfPolls-1){
                    ProtocolTools.delay((getConnectionMode()==0)?10000:20000);
				} else {
                    ProtocolTools.delay((delay-(i*((getConnectionMode()==0)?10:20)))*1000);
				}
				log(Level.INFO, "Keeping connection alive");
				getWebRtu().getTime();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not keep connection alive." + e.getMessage());
		}
	}
	
	private int getConnectionMode(){
		return getWebRtu().getConnectionMode();
	}
	
	protected void log(Level level, String msg){
		getLogger().log(level, msg);
	}
	
	private Logger getLogger() {
		return getWebRtu().getLogger();
	}
	
	protected CosemObjectFactory getCosemObjectFactory() {
		return getWebRtu().getCosemObjectFactory();
	}
	
	protected DLMSMeterConfig getMeterConfig() {
		return getWebRtu().getMeterConfig();
	}
	
	private MeteringWarehouse mw(){
		return getWebRtu().mw();
	}

	protected TimeZone getTimeZone() {
		return getWebRtu().getTimeZone();
	}
	

}

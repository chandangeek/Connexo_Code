package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.cpo.ObjectMapperFactory;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.ClientTypeManager;
import com.energyict.dlms.cosem.ConcentratorSetup;
import com.energyict.dlms.cosem.DeviceTypeManager;
import com.energyict.dlms.cosem.ScheduleManager;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NotInObjectListException;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.Beacon3100Messaging;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects.MasterDataAnalyser.SyncAction;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100Properties;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/08/2015 - 17:13
 */
public class MasterDataSync {

    private final Beacon3100Messaging beacon3100Messaging;

    protected StringBuilder info = new StringBuilder();

    protected DeviceMessageStatus syncStatus = null;

    public MasterDataSync(Beacon3100Messaging beacon3100Messaging) {
        this.beacon3100Messaging = beacon3100Messaging;
    }

    /**
     * Sync all master data of the device types (tasks, schedules, security levels, master data obiscodes, etc)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public CollectedMessage syncMasterData(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
    	this.info.setLength(0);
    	
        AllMasterData allMasterData;
        try {
            final String serializedMasterData = pendingMessage.getPreparedContext();    //This context field contains the serialized version of the master data.
            if(serializedMasterData.contains("DeviceConfigurationException")) {
                return generateFailedMessage(pendingMessage, collectedMessage, serializedMasterData);
            }
            final JSONObject jsonObject = new JSONObject(serializedMasterData);
            allMasterData = ObjectMapperFactory.getObjectMapper().readValue(new StringReader(jsonObject.toString()), AllMasterData.class);
        } catch (JSONException | IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
            collectedMessage.setFailureInformation(ResultType.InCompatible, beacon3100Messaging.createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }

        final ScheduleManager scheduleManager = this.getScheduleManager();
        final DeviceTypeManager deviceTypeManager = this.getDeviceTypeManager();
        final ClientTypeManager clientTypeManager = this.getClientTypeManager();
        final ConcentratorSetup concentratorSetup = this.getConcentratorSetup();
        
        final MasterDataAnalyser analyzer = new MasterDataAnalyser(allMasterData, scheduleManager, deviceTypeManager, clientTypeManager, concentratorSetup, this.getIsFirmwareVersion140OrAbove(), !this.readOldObisCodes());
        final List<SyncAction<?>> actions = analyzer.analyze();

        for (final SyncAction<?> action : actions) {
            try {
                this.info.append(action.toString());
                action.execute();
                this.info.append(" - OK");
            } catch (Exception ex){
                info.append("  > " + ex.getMessage());
                syncStatus = DeviceMessageStatus.FAILED;
            }
            info.append("\n");
        }
        
        this.info.append("\nPlan executed, sync complete.");

        collectedMessage.setDeviceProtocolInformation(getInfoMessage());
        if (syncStatus!=null){
            collectedMessage.setNewDeviceMessageStatus(syncStatus);
        }

        //Now see if there were any warning while parsing the EIServer model, and add them as proper issues.
		List<Issue> issues = new ArrayList<>();
        for (int index = 0; index < allMasterData.getWarningKeys().size(); index++) {
            String warningKey = allMasterData.getWarningKeys().get(index);
            String warningArgument = allMasterData.getWarningArguments().get(index);
            issues.add(MdcManager.getIssueFactory().createWarning(pendingMessage, warningKey, warningArgument));
        }
        if (!issues.isEmpty()) {
            collectedMessage.setFailureInformation(ResultType.ConfigurationMisMatch, issues);
        }

        return collectedMessage;
    }
    
    /**
     * Creates a string representation of the synchronization plan.
     * 
     * @param 		actions		The sync plan.
     * 
     * @return		The string representation.
     */
    private static final String syncPlanToString(final List<SyncAction<?>> actions) {
    	final StringBuilder builder = new StringBuilder("About to execute synchronization plan : \n\n");
    	
    	for (final SyncAction<?> action : actions) {
    		builder.append(action.toString()).append("\n");
    	}
    	
    	return builder.toString();
    }
    
    /**
     * Returns a reference to the {@link ConcentratorSetup}.
     * 
     * @return	A reference to the {@link ConcentratorSetup}.
     * 
     * @throws 	NotInObjectListException		If the {@link ConcentratorSetup} was not in the object-list.
     */
    private final ConcentratorSetup getConcentratorSetup() throws NotInObjectListException {
    	if (this.readOldObisCodes()) {
    		return this.getProtocol().getDlmsSession().getCosemObjectFactory().getConcentratorSetup();
    	} else {
    		return this.getProtocol().getDlmsSession().getCosemObjectFactory().getConcentratorSetup(Beacon3100Messaging.CONCENTRATOR_SETUP_NEW_LOGICAL_NAME);
    	}
    }

    private DeviceTypeManager getDeviceTypeManager() throws NotInObjectListException {
        if(readOldObisCodes()) {
            return getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager();
        }else{
            return getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager(DeviceTypeManager.NEW_FW_OBISCODE);
        }
    }

    private ClientTypeManager getClientTypeManager() throws NotInObjectListException {
        if(readOldObisCodes()) {
            return getProtocol().getDlmsSession().getCosemObjectFactory().getClientTypeManager();
        }else{
            return getProtocol().getDlmsSession().getCosemObjectFactory().getClientTypeManager(Beacon3100Messaging.CLIENT_MANAGER_NEW_OBISCODE);
        }
    }

    private ScheduleManager getScheduleManager() throws NotInObjectListException {
        if(readOldObisCodes()) {
            return getProtocol().getDlmsSession().getCosemObjectFactory().getScheduleManager();
        }else{
            return getProtocol().getDlmsSession().getCosemObjectFactory().getScheduleManager(Beacon3100Messaging.SCHEDULE_MANAGER_NEW_OBISCODE);
        }

    }


    // truncate to max 4000 - as supported by varchar field in oracle
    protected String getInfoMessage() {
        String message = info.toString();
        return message.substring(0, Math.min(4000, message.length()));
    }

    /**
     * Sync the meter details. This assumes that the relevant device types are already synced!
     */
    public CollectedMessage syncDeviceData(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        Beacon3100MeterDetails[] meterDetails;
        try {
            final String serializedMasterData = pendingMessage.getPreparedContext();    //This context field contains the serialized version of the master data.
            if(serializedMasterData.contains("DeviceConfigurationException")) {
                return generateFailedMessage(pendingMessage, collectedMessage, serializedMasterData);
            }
            final JSONArray jsonObject = new JSONArray(serializedMasterData);
            meterDetails = ObjectMapperFactory.getObjectMapper().readValue(new StringReader(jsonObject.toString()), Beacon3100MeterDetails[].class);
        } catch (JSONException | IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
            collectedMessage.setFailureInformation(ResultType.InCompatible, beacon3100Messaging.createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }

        syncDevices(meterDetails);

        boolean cleanupUnusedMasterData = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.cleanUpUnusedDeviceTypesAttributeName).getDeviceMessageAttributeValue());
        if (cleanupUnusedMasterData) {
            // We'll just trim the fat from the Beacon here.
        	final AllMasterData masterData = new AllMasterData();
        	
        	final MasterDataAnalyser analyzer = new MasterDataAnalyser(masterData, this.getScheduleManager(), this.getDeviceTypeManager(), this.getClientTypeManager(), this.getConcentratorSetup(), this.getIsFirmwareVersion140OrAbove(), !this.readOldObisCodes());
        	final List<SyncAction<?>> plan = analyzer.analyze();
        	
        	for (final SyncAction<?> action : plan) {
        		action.execute();
        	}
        }

        return collectedMessage;
    }

    private CollectedMessage generateFailedMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage, String serializedMasterData) {
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
        collectedMessage.setDeviceProtocolInformation(serializedMasterData);
        collectedMessage.setFailureInformation(ResultType.InCompatible, beacon3100Messaging.createMessageFailedIssue(pendingMessage, new Exception()));
        return collectedMessage;
    }

    public AbstractDlmsProtocol getProtocol() {
        return beacon3100Messaging.getProtocol();
    }

    private boolean getIsFirmwareVersion140OrAbove() throws IOException {
        String firmwareVersion = getProtocol().getDlmsSession().getCosemObjectFactory().getData(ObisCode.fromString("0.0.0.2.1.255")).getString();
        StringTokenizer tokenizer = new StringTokenizer(firmwareVersion, ".");
        String token = tokenizer.nextToken();
        int firstNr = Integer.parseInt(token);
        if(firstNr < 1){
            return false;
        }
        token = tokenizer.nextToken();
        int secondNr = Integer.parseInt(token);
        if(secondNr < 4){
            return false;
        }
        return true;
    }

    private void syncDevices(Beacon3100MeterDetails[] allMeterDetails) throws IOException {
        boolean isFirmwareVersion140OrAbove = getIsFirmwareVersion140OrAbove();
        for (Beacon3100MeterDetails beacon3100MeterDetails : allMeterDetails) {
            if(readOldObisCodes()) {
                syncOneDevice(beacon3100MeterDetails.toStructure(isFirmwareVersion140OrAbove));
            }else{
                syncOneDevice(beacon3100MeterDetails.toStructureFWVersion10AndAbove(beacon3100MeterDetails));
            }
        }
    }

    public CollectedMessage syncAllDeviceData(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        syncAllDevices(new MasterDataSerializer(getBeacon3100Properties()).getMeterDetails(pendingMessage.getDeviceId()));

        return collectedMessage;
    }

    private Beacon3100Properties getBeacon3100Properties() {
        return (Beacon3100Properties) getProtocol().getDlmsSessionProperties();
    }


    private void syncAllDevices(Beacon3100MeterDetails[] allMeterDetails) throws IOException {
        for (Beacon3100MeterDetails beacon3100MeterDetails : allMeterDetails) {
            syncOneDevice(beacon3100MeterDetails.toStructureFWVersion10AndAbove(beacon3100MeterDetails));
        }
    }

    private void syncOneDevice(Structure meterDetails) throws IOException {
        getDeviceTypeManager().assignDeviceType(meterDetails);
    }

    public CollectedMessage syncOneDeviceData(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        int deviceId = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.deviceId).getDeviceMessageAttributeValue());
        Beacon3100MeterDetails meterDetails = new MasterDataSerializer(getBeacon3100Properties()).getMeterDetails(deviceId, pendingMessage.getDeviceId());

        if(meterDetails != null) {
            syncOneDevice(meterDetails.toStructureFWVersion10AndAbove(meterDetails));
        }else{
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.InCompatible, beacon3100Messaging.createMessageFailedIssue(pendingMessage, new ProtocolException("Device id not found on the master device.")));
        }

        return collectedMessage;
    }

    public CollectedMessage syncOneDeviceWithDCAdvanced(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        long configurationId = Long.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.configurationId).getDeviceMessageAttributeValue());
        String startTime = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.startDate).getDeviceMessageAttributeValue();
        String endTime = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.endDate).getDeviceMessageAttributeValue();
        int deviceId = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.deviceId).getDeviceMessageAttributeValue());
        Beacon3100MeterDetails meterDetails = new MasterDataSerializer(getBeacon3100Properties()).getMeterDetails(deviceId, pendingMessage.getDeviceId());

        if(meterDetails!= null) {
            try {
                syncOneDevice(meterDetails, configurationId, startTime, endTime);
            } catch (ParseException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setDeviceProtocolInformation(e.getMessage());
                collectedMessage.setFailureInformation(ResultType.InCompatible, beacon3100Messaging.createMessageFailedIssue(pendingMessage, e));
                return collectedMessage;
            }
        }

        return collectedMessage;
    }

    public void updateDeviceTypeForNewFW(Beacon3100DeviceType beacon3100DeviceType) throws IOException {
        DeviceTypeManager deviceTypeManager = getDeviceTypeManager();

        info.append("*** UPDATING DeviceType " + beacon3100DeviceType.getId() + " ***\n");

        try {
            deviceTypeManager.updateDeviceType(beacon3100DeviceType.toStructure(false));
            info.append("- DeviceType UPDATED: [").append(beacon3100DeviceType.getId()).append("]: ").append(beacon3100DeviceType.getName()).append("\n");
        } catch (IOException ex) {
            info.append("- Could not update DeviceType [" + beacon3100DeviceType.getId() + "]: " + ex.getMessage() + "\n");
        }
    }

    private void syncOneDevice(Beacon3100MeterDetails beacon3100MeterDetails, long configurationId, String startTime, String endTime) throws ParseException, IOException {
        List<DeviceTypeAssignment> deviceTypeAssignements = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

        deviceTypeAssignements.add(new DeviceTypeAssignment(configurationId, dateFormat.parse(startTime), dateFormat.parse(endTime)));
        beacon3100MeterDetails.setDeviceTypeAssignments(deviceTypeAssignements);

        syncOneDevice(beacon3100MeterDetails.toStructureFWVersion10AndAbove(beacon3100MeterDetails));
    }

    public CollectedMessage setBufferForSpecificRegister(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        String obisCode = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.obisCode).getDeviceMessageAttributeValue();
        int bufferSize = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.bufferSize).getDeviceMessageAttributeValue());

        AllMasterData allMasterData = new AllMasterData();
        List<Beacon3100DeviceType> deviceTypes = allMasterData.getDeviceTypes();

        for(Beacon3100DeviceType beacon3100DeviceType : deviceTypes){
            if(beacon3100DeviceType.updateBufferSizeForRegister(ObisCode.fromString(obisCode), new Unsigned16(bufferSize))) {
                collectedMessage.setDeviceProtocolInformation("Setting buffer size for obis code : " + ObisCode.fromString(obisCode));
                updateDeviceTypeForNewFW(beacon3100DeviceType);
                break;
            }
        }

        return collectedMessage;
    }

    public CollectedMessage setBufferForAllRegisters(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        int bufferSize = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.bufferSize).getDeviceMessageAttributeValue());

        AllMasterData allMasterData = new AllMasterData();
        List<Beacon3100DeviceType> deviceTypes = allMasterData.getDeviceTypes();

        for(Beacon3100DeviceType beacon3100DeviceType : deviceTypes){
            if(beacon3100DeviceType.updateBufferSizeForAllRegisters(new Unsigned16(bufferSize))) {
                collectedMessage.setDeviceProtocolInformation("Setting buffer size for all registers from device with id: " + pendingMessage.getDeviceId());
                updateDeviceTypeForNewFW(beacon3100DeviceType);
                break;
            }
        }

        return collectedMessage;
    }

    public CollectedMessage setBufferForSpecificEventLog(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        String obisCode = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.obisCode).getDeviceMessageAttributeValue();
        long bufferSize = Long.parseLong(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.bufferSize).getDeviceMessageAttributeValue());

        AllMasterData allMasterData = new AllMasterData();
        List<Beacon3100DeviceType> deviceTypes = allMasterData.getDeviceTypes();

        for(Beacon3100DeviceType beacon3100DeviceType : deviceTypes){
            if(beacon3100DeviceType.updateBufferSizeForEventLogs(ObisCode.fromString(obisCode), new Unsigned32(bufferSize))) {
                collectedMessage.setDeviceProtocolInformation("Setting buffer size for obis code : " + ObisCode.fromString(obisCode));
                updateDeviceTypeForNewFW(beacon3100DeviceType);
                break;
            }
        }

        return collectedMessage;
    }

    public CollectedMessage setBufferForAllEventLogs(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        long bufferSize = Long.parseLong(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.bufferSize).getDeviceMessageAttributeValue());

        List<Beacon3100DeviceType> deviceTypes =  new MasterDataSerializer(getBeacon3100Properties()).getDeviceTypes(pendingMessage.getDeviceId(), readOldObisCodes());

        for(Beacon3100DeviceType beacon3100DeviceType : deviceTypes){
            if(beacon3100DeviceType.updateBufferSizeForAllEventLogs(new Unsigned32(bufferSize))) {
                collectedMessage.setDeviceProtocolInformation("Setting buffer size for all event logs for device with id : " + pendingMessage.getDeviceId());
                updateDeviceTypeForNewFW(beacon3100DeviceType);
                break;
            }
        }

        return collectedMessage;
    }

    public CollectedMessage setBufferForSpecificLoadProfile(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        String obisCode = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.obisCode).getDeviceMessageAttributeValue();
        long bufferSize = Long.parseLong(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.bufferSize).getDeviceMessageAttributeValue());

        AllMasterData allMasterData = new AllMasterData();
        List<Beacon3100DeviceType> deviceTypes = allMasterData.getDeviceTypes();

        for(Beacon3100DeviceType beacon3100DeviceType : deviceTypes){
            if(beacon3100DeviceType.updateBufferSizeForLoadProfiles(ObisCode.fromString(obisCode), new Unsigned32(bufferSize))) {
                collectedMessage.setDeviceProtocolInformation("Setting buffer size for obis code : " + ObisCode.fromString(obisCode));
                updateDeviceTypeForNewFW(beacon3100DeviceType);
                break;
            }

        }

        return collectedMessage;
    }

    public CollectedMessage setBufferForAllLoadProfiles(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        long bufferSize = Long.parseLong(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.bufferSize).getDeviceMessageAttributeValue());

        List<Beacon3100DeviceType> beacon3100DeviceTypes =  new MasterDataSerializer(getBeacon3100Properties()).getDeviceTypes(pendingMessage.getDeviceId(), readOldObisCodes());

        for(Beacon3100DeviceType beacon3100DeviceType : beacon3100DeviceTypes){
            if(beacon3100DeviceType.updateBufferSizeForAllLoadProfiles(new Unsigned32(bufferSize))) {
                collectedMessage.setDeviceProtocolInformation("Setting buffer size for all load profiles from device : " + pendingMessage.getDeviceId());
                updateDeviceTypeForNewFW(beacon3100DeviceType);
                break;
            }
        }

        return collectedMessage;
    }

    private boolean readOldObisCodes(){
        return ((Beacon3100Properties)getProtocol().getDlmsSessionProperties()).getReadOldObisCodes();
    }
}
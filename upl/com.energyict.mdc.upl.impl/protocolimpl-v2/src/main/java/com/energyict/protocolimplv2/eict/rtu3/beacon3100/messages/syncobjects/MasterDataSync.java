package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.cpo.ObjectMapperFactory;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.ClientTypeManager;
import com.energyict.dlms.cosem.DeviceTypeManager;
import com.energyict.dlms.cosem.ScheduleManager;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NotInObjectListException;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.Beacon3100Messaging;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/08/2015 - 17:13
 */
public class MasterDataSync {

    private final Beacon3100Messaging beacon3100Messaging;

    protected StringBuilder info = new StringBuilder();

    public MasterDataSync(Beacon3100Messaging beacon3100Messaging) {
        this.beacon3100Messaging = beacon3100Messaging;
    }

    /**
     * Sync all master data of the device types (tasks, schedules, security levels, master data obiscodes, etc)
     */
    public CollectedMessage syncMasterData(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage, boolean readOldObisCodes) throws IOException {
        AllMasterData allMasterData;
        try {
            final String serializedMasterData = pendingMessage.getPreparedContext();    //This context field contains the serialized version of the master data.
            final JSONObject jsonObject = new JSONObject(serializedMasterData);
            allMasterData = ObjectMapperFactory.getObjectMapper().readValue(new StringReader(jsonObject.toString()), AllMasterData.class);
        } catch (JSONException | IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
            collectedMessage.setFailureInformation(ResultType.InCompatible, beacon3100Messaging.createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }

        MasterDataAnalyser masterDataAnalyser = analyseWhatToSync(allMasterData);

        info.append(masterDataAnalyser.getInfo());

        /**
         * Do the synchronization sequence
         * order of those operation is very important!
         * see https://jira.eict.vpdc/browse/COMMUNICATION-1613
         */

        createSchedules(masterDataAnalyser.getSchedulesToAdd());
        createClientTypes(masterDataAnalyser.getClientTypesToAdd());
        createDeviceTypes(masterDataAnalyser.getDeviceTypesToAdd(), readOldObisCodes);

        updateDeviceTypes(getDeviceTypesToUpdate(masterDataAnalyser), readOldObisCodes);
        updateClientTypes(masterDataAnalyser.getClientTypesToUpdate());
        updateSchedules(masterDataAnalyser.getSchedulesToUpdate());

        deleteDeviceTypes(masterDataAnalyser.getDeviceTypesToDelete());
        deleteClientTypes(masterDataAnalyser.getClientTypesToDelete());
        deleteSchedules(masterDataAnalyser.getSchedulesToDelete());
        /**
         * synchronization sequence ended
         */


        collectedMessage.setDeviceProtocolInformation(getInfoMessage());

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

    private MasterDataAnalyser analyseWhatToSync(AllMasterData allMasterData) throws IOException {
        MasterDataAnalyser masterDataAnalyser = new MasterDataAnalyser();

        masterDataAnalyser.analyseClientTypes( getClientTypeManager().readClients(),
                allMasterData.getClientTypes(),
                getIsFirmwareVersion140OrAbove());

        masterDataAnalyser.analyseDeviceTypes(getDeviceTypeManager().readDeviceTypes(),
                allMasterData.getDeviceTypes());

        masterDataAnalyser.analyseSchedules(getScheduleManager().readSchedules(),
                allMasterData.getSchedules());

        return masterDataAnalyser;
    }

    private DeviceTypeManager getDeviceTypeManager() throws NotInObjectListException {
        if(beacon3100Messaging.readOldObisCodes()) {
            return getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager();
        }else{
            return getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager(DeviceTypeManager.NEW_FW_OBISCODE);
        }
    }

    private ClientTypeManager getClientTypeManager() throws NotInObjectListException {
        if(beacon3100Messaging.readOldObisCodes()) {
            return getProtocol().getDlmsSession().getCosemObjectFactory().getClientTypeManager();
        }else{
            return getProtocol().getDlmsSession().getCosemObjectFactory().getClientTypeManager(Beacon3100Messaging.CLIENT_MANAGER_NEW_OBISCODE);
        }
    }

    private ScheduleManager getScheduleManager() throws NotInObjectListException {
        if(beacon3100Messaging.readOldObisCodes()) {
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
    public CollectedMessage syncDeviceData(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage,  boolean firmwareVersionLowerThan10) throws IOException {
        Beacon3100MeterDetails[] meterDetails;
        try {
            final String serializedMasterData = pendingMessage.getPreparedContext();    //This context field contains the serialized version of the master data.
            final JSONArray jsonObject = new JSONArray(serializedMasterData);
            meterDetails = ObjectMapperFactory.getObjectMapper().readValue(new StringReader(jsonObject.toString()), Beacon3100MeterDetails[].class);
        } catch (JSONException | IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
            collectedMessage.setFailureInformation(ResultType.InCompatible, beacon3100Messaging.createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }

        syncDevices(meterDetails, firmwareVersionLowerThan10);

        boolean cleanupUnusedMasterData = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.cleanUpUnusedDeviceTypesAttributeName).getDeviceMessageAttributeValue());
        if (cleanupUnusedMasterData) {
            //Remove the device types in the Beacon3100 data that are no longer defined in EIServer

            final List<Long> beacon3100DeviceTypeIds = readDeviceTypesIDs();

            final List<Long> eiServerDeviceTypeIds = new ArrayList<>();
            for (Beacon3100MeterDetails meterDetail : meterDetails) {
                if (!eiServerDeviceTypeIds.contains(meterDetail.getDeviceTypeId())) {
                    eiServerDeviceTypeIds.add(meterDetail.getDeviceTypeId());
                }
            }

            for (Long beacon3100DeviceTypeId : beacon3100DeviceTypeIds) {
                if (shouldBeRemoved(eiServerDeviceTypeIds, beacon3100DeviceTypeId)) {
                    getDeviceTypeManager().removeDeviceType(beacon3100DeviceTypeId);
                }
            }
        }

        return collectedMessage;
    }

    private boolean shouldBeRemoved(List<Long> eiServerDeviceTypeIds, Long beacon3100DeviceTypeId) {
        return !eiServerDeviceTypeIds.contains(beacon3100DeviceTypeId);
    }

    /**
     * Return a cached (read out only once) list of the IDs of all device types in the Beacon3100 data.
     */
    private List<Long> readDeviceTypesIDs() throws IOException {
        List<Long> deviceTypesIDs = new ArrayList<>();
        for (AbstractDataType deviceType : getDeviceTypeManager().readDeviceTypes()) {
            if (deviceType.isStructure() && deviceType.getStructure().nrOfDataTypes() > 0) {
                final long deviceTypeId = deviceType.getStructure().getDataType(0).longValue();     //First element of the structure is the deviceType ID
                deviceTypesIDs.add(deviceTypeId);
            }
        }
        return deviceTypesIDs;
    }

    /**
     * Return a cached (read out only once) list of the IDs of all device types in the Beacon3100 data.
     */
    private Array readDeviceTypes() throws IOException {

        return getDeviceTypeManager().readDeviceTypes();
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

    private void syncDevices(Beacon3100MeterDetails[] allMeterDetails, boolean firmwareVersionLowerThan10) throws IOException {
        boolean isFirmwareVersion140OrAbove = getIsFirmwareVersion140OrAbove();
        for (Beacon3100MeterDetails beacon3100MeterDetails : allMeterDetails) {
            if(firmwareVersionLowerThan10) {
                syncOneDevice(beacon3100MeterDetails.toStructure(isFirmwareVersion140OrAbove));
            }else{
                syncOneDevice(beacon3100MeterDetails.toStructureFWVersion10AndAbove());
            }
        }
    }


    private void createSchedules(List<Beacon3100Schedule> schedulesToAdd) throws NotInObjectListException {
        ScheduleManager scheduleManager = getScheduleManager();

        info.append("*** CREATING Schedules ***\n");
        for (Beacon3100Schedule beacon3100Schedule : schedulesToAdd){
            try {
                scheduleManager.addSchedule(beacon3100Schedule.toStructure());
                info.append("- Schedule ADDED: [").append(beacon3100Schedule.getId()).append("] ").append(beacon3100Schedule.getName()).append("\n");
            } catch (IOException ex) {
                info.append("- Could not add schedule [" + beacon3100Schedule.getId() + "]: " + ex.getMessage() + "\n");

            }
        }
    }

    private void updateSchedules(List<Beacon3100Schedule> schedulesToUpdate) throws IOException {
        ScheduleManager scheduleManager = getScheduleManager();

        info.append("*** UPDATING Schedules ***\n");
        for (Beacon3100Schedule beacon3100Schedule : schedulesToUpdate){
            try {
                scheduleManager.updateSchedule(beacon3100Schedule.toStructure());
                info.append("- Schedule UPDATED: [").append(beacon3100Schedule.getId()).append("] ").append(beacon3100Schedule.getName()).append("\n");
            } catch (IOException ex) {
                info.append("- Could not update schedule [" + beacon3100Schedule.getId() + "]: " + ex.getMessage() + "\n");
            }
        }
    }

    private void deleteSchedules(List<Long> schedulesToDelete) throws IOException {
        ScheduleManager scheduleManager = getScheduleManager();

        info.append("*** DELETING Schedules ***\n");
        for (Long beacon3100ScheduleId : schedulesToDelete){
            try {
                scheduleManager.removeSchedule(beacon3100ScheduleId);
                info.append("- Schedule DELETED: [").append(beacon3100ScheduleId).append("]\n");
            } catch (IOException ex) {
                info.append("- Could not delete schedule [" + beacon3100ScheduleId + "]: " + ex.getMessage() + "\n");
            }
        }
    }


    private void createClientTypes(List<Beacon3100ClientType> clientTypesToAdd) throws NotInObjectListException {
        ClientTypeManager clientTypeManager = getClientTypeManager();

        info.append("*** CREATING ClientTypes ***\n");
        for (Beacon3100ClientType beacon3100ClientType : clientTypesToAdd){
            try {
                clientTypeManager.addClientType(beacon3100ClientType.toStructure());
                info.append("- ClientType ADDED: [").append(beacon3100ClientType.getId()).append("] ClientMacAddress:").append(beacon3100ClientType.getClientMacAddress()).append("\n");
            } catch (IOException ex) {
                info.append("- Could not add ClientType [" + beacon3100ClientType.getId() + "]: " + ex.getMessage() + "\n");
            }
        }
    }


    private void updateClientTypes(List<Beacon3100ClientType> clientTypesToUpdate) throws NotInObjectListException {
        ClientTypeManager clientTypeManager = getClientTypeManager();

        info.append("*** UPDATING ClientTypes ***\n");
        for (Beacon3100ClientType beacon3100ClientType : clientTypesToUpdate){
            try {
                clientTypeManager.updateClientType(beacon3100ClientType.toStructure());
                info.append("- ClientType UPDATED: [").append(beacon3100ClientType.getId()).append("] ClientMacAddress:").append(beacon3100ClientType.getClientMacAddress()).append("\n");
            } catch (IOException ex) {
                info.append("- Could not update ClientType [" + beacon3100ClientType.getId() + "]: " + ex.getMessage() + "\n");
            }
        }
    }


    private void deleteClientTypes(List<Long> clientTypesToDelete) throws NotInObjectListException {
        ClientTypeManager clientTypeManager = getClientTypeManager();

        info.append("*** DELETING ClientTypes ***\n");
        for (Long beacon3100ClientTypeId : clientTypesToDelete){
            try {
                clientTypeManager.removeClientType(beacon3100ClientTypeId);
                info.append("- ClientType DELETED: [").append(beacon3100ClientTypeId).append("]\n");
            } catch (IOException ex) {
                info.append("- Could not delete client type [" + beacon3100ClientTypeId + "]: " + ex.getMessage() + "\n");
            }
        }
    }

    private void createDeviceTypes(List<Beacon3100DeviceType> devicesTypesToAdd, boolean readOldObisCodes) throws NotInObjectListException {
        DeviceTypeManager deviceTypeManager = getDeviceTypeManager();

        info.append("*** CREATING DeviceTypes ***\n");
        for (Beacon3100DeviceType beacon3100DeviceType : devicesTypesToAdd){
            try {
                deviceTypeManager.addDeviceType(beacon3100DeviceType.toStructure(readOldObisCodes));
                info.append("- DeviceType ADDED: [").append(beacon3100DeviceType.getId()).append("]: ").append(beacon3100DeviceType.getName()).append("\n");
            } catch (IOException ex) {
                info.append("- Could not add DeviceType [" + beacon3100DeviceType.getId() + "]: " + ex.getMessage() + "\n");
            }
        }
    }

    private void updateDeviceTypes(List<Beacon3100DeviceType> devicesTypesToUpdate, boolean readOldObisCodes) throws NotInObjectListException {
        DeviceTypeManager deviceTypeManager = getDeviceTypeManager();

        info.append("*** UPDATING DeviceTypes ***\n");
        for (Beacon3100DeviceType beacon3100DeviceType : devicesTypesToUpdate){
            try {
                deviceTypeManager.updateDeviceType(beacon3100DeviceType.toStructure(readOldObisCodes));
                info.append("- DeviceType UPDATED: [").append(beacon3100DeviceType.getId()).append("]: ").append(beacon3100DeviceType.getName()).append("\n");
            } catch (IOException ex) {
                info.append("- Could not update DeviceType [" + beacon3100DeviceType.getId() + "]: " + ex.getMessage() + "\n");
            }
        }
    }


    private void deleteDeviceTypes(List<Long> devicesTypesToDelete) throws NotInObjectListException {
        DeviceTypeManager deviceTypeManager = getDeviceTypeManager();

        info.append("*** DELETING DeviceTypes ***\n");
        for (Long beacon3100DeviceTypeId : devicesTypesToDelete){
            try {
                deviceTypeManager.removeDeviceType(beacon3100DeviceTypeId);
                info.append("- DeviceType DELETED: [").append(beacon3100DeviceTypeId).append("]\n");
            } catch (IOException ex) {
                info.append("- Could not delete DeviceType [" + beacon3100DeviceTypeId + "]: " + ex.getMessage() + "\n");
            }
        }
    }

    public CollectedMessage syncAllDeviceData(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        syncAllDevices(new MasterDataSerializer().getMeterDetails(pendingMessage.getDeviceId()));

        return collectedMessage;
    }



    private void syncAllDevices(Beacon3100MeterDetails[] allMeterDetails) throws IOException {
        for (Beacon3100MeterDetails beacon3100MeterDetails : allMeterDetails) {
            syncOneDevice(beacon3100MeterDetails.toStructureFWVersion10AndAbove());
        }
    }

    private void syncOneDevice(Structure meterDetails) throws IOException {
        getDeviceTypeManager().assignDeviceType(meterDetails);
    }

    public CollectedMessage syncOneDeviceData(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        int deviceId = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.deviceId).getDeviceMessageAttributeValue());
        Beacon3100MeterDetails[] meterDetails = new MasterDataSerializer().getMeterDetails(deviceId);

        if(meterDetails!= null && meterDetails[0] != null) {
            syncOneDevice(meterDetails[0].toStructureFWVersion10AndAbove());
        }

        return collectedMessage;
    }

    public CollectedMessage syncOneDeviceWithDCAdvanced(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        long configurationId = Long.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.configurationId).getDeviceMessageAttributeValue());
        String startTime = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.startDate).getDeviceMessageAttributeValue();
        String endTime = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.endDate).getDeviceMessageAttributeValue();
        int deviceId = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.deviceId).getDeviceMessageAttributeValue());
        Beacon3100MeterDetails[] meterDetails = new MasterDataSerializer().getMeterDetails(deviceId);

        if(meterDetails!= null && meterDetails[0] != null) {
            try {
                syncOneDevice(meterDetails[0], configurationId, startTime, endTime);
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
            deviceTypeManager.updateDeviceType(beacon3100DeviceType.toStructure(true));
            info.append("- DeviceType UPDATED: [").append(beacon3100DeviceType.getId()).append("]: ").append(beacon3100DeviceType.getName()).append("\n");
        } catch (IOException ex) {
            info.append("- Could not update DeviceType [" + beacon3100DeviceType.getId() + "]: " + ex.getMessage() + "\n");
        }
    }

        private void syncOneDevice(Beacon3100MeterDetails beacon3100MeterDetails, long configurationId, String startTime, String endTime) throws ParseException, IOException {
        ArrayList deviceTypeAssignements = new ArrayList();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        deviceTypeAssignements.add(new DeviceTypeAssignment(configurationId, dateFormat.parse(startTime), dateFormat.parse(endTime)));
        beacon3100MeterDetails.setDeviceTypeAssignments(deviceTypeAssignements);

        syncOneDevice(beacon3100MeterDetails.toStructureFWVersion10AndAbove());
    }

    public CollectedMessage setBufferForSpecificRegister(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        String obisCode = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.obisCode).getDeviceMessageAttributeValue();
        int bufferSize = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.bufferSize).getDeviceMessageAttributeValue());
        MasterDataAnalyser masterDataAnalyser = analyseWhatToSync(new AllMasterData());
        List<Beacon3100DeviceType> deviceTypes = getDeviceTypesToUpdate(masterDataAnalyser);
        for(Beacon3100DeviceType beacon3100DeviceType : deviceTypes){
            if(beacon3100DeviceType.getId() == pendingMessage.getDeviceId()){
                if(beacon3100DeviceType.updateBufferSizeForRegister(ObisCode.fromString(obisCode), new Unsigned16(bufferSize))) {
                    collectedMessage.setDeviceProtocolInformation("Setting buffer size for obis code : " + ObisCode.fromString(obisCode));
                    updateDeviceTypeForNewFW(beacon3100DeviceType);
                    break;
                }
            }
        }

        deviceTypes = masterDataAnalyser.getDeviceTypesToAdd();
        for(Beacon3100DeviceType beacon3100DeviceType : deviceTypes){
            if(beacon3100DeviceType.getId() == pendingMessage.getDeviceId()){
                if(beacon3100DeviceType.updateBufferSizeForRegister(ObisCode.fromString(obisCode), new Unsigned16(bufferSize))) {
                    collectedMessage.setDeviceProtocolInformation("Setting buffer size for obis code : " + ObisCode.fromString(obisCode));
                    updateDeviceTypeForNewFW(beacon3100DeviceType);
                    break;
                }
            }
        }

        return collectedMessage;
    }

    public CollectedMessage setBufferForAllRegisters(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        int bufferSize = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.bufferSize).getDeviceMessageAttributeValue());

        MasterDataAnalyser masterDataAnalyser = analyseWhatToSync(new AllMasterData());
        List<Beacon3100DeviceType> deviceTypes = getDeviceTypesToUpdate(masterDataAnalyser);
        for(Beacon3100DeviceType beacon3100DeviceType : deviceTypes){
            if(beacon3100DeviceType.getId() == pendingMessage.getDeviceId()){
                if(beacon3100DeviceType.updateBufferSizeForAllRegisters(new Unsigned16(bufferSize))) {
                    collectedMessage.setDeviceProtocolInformation("Setting buffer size for device : " + pendingMessage.getDeviceId());
                    updateDeviceTypeForNewFW(beacon3100DeviceType);
                    break;
                }
            }
        }

        deviceTypes = masterDataAnalyser.getDeviceTypesToAdd();
        for(Beacon3100DeviceType beacon3100DeviceType : deviceTypes){
            if(beacon3100DeviceType.getId() == pendingMessage.getDeviceId()){
                if(beacon3100DeviceType.updateBufferSizeForAllRegisters(new Unsigned16(bufferSize))) {
                    collectedMessage.setDeviceProtocolInformation("Setting buffer size for device : " + pendingMessage.getDeviceId());
                    updateDeviceTypeForNewFW(beacon3100DeviceType);
                    break;
                }
            }
        }

        return collectedMessage;
    }

    public CollectedMessage setBufferForSpecificEventLog(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        String obisCode = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.obisCode).getDeviceMessageAttributeValue();
        long bufferSize = Long.parseLong(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.bufferSize).getDeviceMessageAttributeValue());

        MasterDataAnalyser masterDataAnalyser = analyseWhatToSync(new AllMasterData());
        List<Beacon3100DeviceType> deviceTypes = getDeviceTypesToUpdate(masterDataAnalyser);
        for(Beacon3100DeviceType beacon3100DeviceType : deviceTypes){
            if(beacon3100DeviceType.getId() == pendingMessage.getDeviceId()){
                if(beacon3100DeviceType.updateBufferSizeForEventLogs(ObisCode.fromString(obisCode), new Unsigned32(bufferSize))) {
                    collectedMessage.setDeviceProtocolInformation("Setting buffer size for obis code : " + ObisCode.fromString(obisCode));
                    updateDeviceTypeForNewFW(beacon3100DeviceType);
                    break;
                }
            }
        }

        deviceTypes = masterDataAnalyser.getDeviceTypesToAdd();
        for(Beacon3100DeviceType beacon3100DeviceType : deviceTypes){
            if(beacon3100DeviceType.getId() == pendingMessage.getDeviceId()){
                if(beacon3100DeviceType.updateBufferSizeForEventLogs(ObisCode.fromString(obisCode), new Unsigned32(bufferSize))) {
                    collectedMessage.setDeviceProtocolInformation("Setting buffer size for obis code : " + ObisCode.fromString(obisCode));
                    updateDeviceTypeForNewFW(beacon3100DeviceType);
                    break;
                }
            }
        }

        return collectedMessage;
    }

    public CollectedMessage setBufferForAllEventLogs(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        long bufferSize = Long.parseLong(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.bufferSize).getDeviceMessageAttributeValue());

        MasterDataAnalyser masterDataAnalyser = analyseWhatToSync(new AllMasterData());
        List<Beacon3100DeviceType> deviceTypes = getDeviceTypesToUpdate(masterDataAnalyser);
        for(Beacon3100DeviceType beacon3100DeviceType : deviceTypes){
            if(beacon3100DeviceType.getId() == pendingMessage.getDeviceId()){
                if(beacon3100DeviceType.updateBufferSizeForAllEventLogs(new Unsigned32(bufferSize))) {
                    collectedMessage.setDeviceProtocolInformation("Setting buffer size for device : " + pendingMessage.getDeviceId());
                    updateDeviceTypeForNewFW(beacon3100DeviceType);
                    break;
                }
            }
        }

        deviceTypes = masterDataAnalyser.getDeviceTypesToAdd();
        for(Beacon3100DeviceType beacon3100DeviceType : deviceTypes){
            if(beacon3100DeviceType.getId() == pendingMessage.getDeviceId()){
                if(beacon3100DeviceType.updateBufferSizeForAllEventLogs(new Unsigned32(bufferSize))) {
                    collectedMessage.setDeviceProtocolInformation("Setting buffer size for device : " + pendingMessage.getDeviceId());
                    updateDeviceTypeForNewFW(beacon3100DeviceType);
                    break;
                }
            }
        }

        return collectedMessage;
    }

    public CollectedMessage setBufferForSpecificLoadProfile(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        String obisCode = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.obisCode).getDeviceMessageAttributeValue();
        long bufferSize = Long.parseLong(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.bufferSize).getDeviceMessageAttributeValue());

        MasterDataAnalyser masterDataAnalyser = analyseWhatToSync(new AllMasterData());
        List<Beacon3100DeviceType> deviceTypes = getDeviceTypesToUpdate(masterDataAnalyser);
        for(Beacon3100DeviceType beacon3100DeviceType : deviceTypes){
            if(beacon3100DeviceType.getId() == pendingMessage.getDeviceId()){
                if(beacon3100DeviceType.updateBufferSizeForLoadProfiles(ObisCode.fromString(obisCode), new Unsigned32(bufferSize))) {
                    collectedMessage.setDeviceProtocolInformation("Setting buffer size for obis code : " + ObisCode.fromString(obisCode));
                    updateDeviceTypeForNewFW(beacon3100DeviceType);
                    break;
                }
            }
        }

        deviceTypes = masterDataAnalyser.getDeviceTypesToAdd();
        for(Beacon3100DeviceType beacon3100DeviceType : deviceTypes){
            if(beacon3100DeviceType.getId() == pendingMessage.getDeviceId()){
                if(beacon3100DeviceType.updateBufferSizeForLoadProfiles(ObisCode.fromString(obisCode), new Unsigned32(bufferSize))) {
                    collectedMessage.setDeviceProtocolInformation("Setting buffer size for obis code : " + ObisCode.fromString(obisCode));
                    updateDeviceTypeForNewFW(beacon3100DeviceType);
                    break;
                }
            }
        }

        return collectedMessage;
    }

    public CollectedMessage setBufferForAllLoadProfiles(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        long bufferSize = Long.parseLong(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.bufferSize).getDeviceMessageAttributeValue());
        MasterDataAnalyser masterDataAnalyser = analyseWhatToSync(new AllMasterData());
        List<Beacon3100DeviceType> deviceTypes = masterDataAnalyser.getDeviceTypesToUpdate();
        for(Beacon3100DeviceType beacon3100DeviceType : deviceTypes){
            if(beacon3100DeviceType.getId() == pendingMessage.getDeviceId()){
                if(beacon3100DeviceType.updateBufferSizeForAllLoadProfiles(new Unsigned32(bufferSize))) {
                    collectedMessage.setDeviceProtocolInformation("Setting buffer size for device : " + pendingMessage.getDeviceId());
                    updateDeviceTypeForNewFW(beacon3100DeviceType);
                    break;
                }
            }
        }

        deviceTypes = masterDataAnalyser.getDeviceTypesToAdd();
        for(Beacon3100DeviceType beacon3100DeviceType : deviceTypes){
            if(beacon3100DeviceType.getId() == pendingMessage.getDeviceId()){
                if(beacon3100DeviceType.updateBufferSizeForAllLoadProfiles(new Unsigned32(bufferSize))) {
                    collectedMessage.setDeviceProtocolInformation("Setting buffer size for device : " + pendingMessage.getDeviceId());
                    updateDeviceTypeForNewFW(beacon3100DeviceType);
                    break;
                }
            }
        }

        return collectedMessage;
    }

    private List<Beacon3100DeviceType> getDeviceTypesToUpdate(MasterDataAnalyser masterDataAnalyser) {
        return masterDataAnalyser.getDeviceTypesToUpdate();
    }
}
package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.tasks.Issue;

import com.energyict.cpo.ObjectMapperFactory;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.ClientTypeManager;
import com.energyict.dlms.cosem.DeviceTypeManager;
import com.energyict.dlms.cosem.ScheduleManager;
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

    public MasterDataSync(Beacon3100Messaging beacon3100Messaging) {
        this.beacon3100Messaging = beacon3100Messaging;
    }

    /**
     * Sync all master data of the device types (tasks, schedules, security levels, master data obiscodes, etc)
     */
    public CollectedMessage syncMasterData(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
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
        createDeviceTypes(masterDataAnalyser.getDeviceTypesToAdd());

        updateDeviceTypes(masterDataAnalyser.getDeviceTypesToUpdate());
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

        masterDataAnalyser.analyseClientTypes( getProtocol().getDlmsSession().getCosemObjectFactory().getClientTypeManager().readClients(),
                allMasterData.getClientTypes(),
                getIsFirmwareVersion140OrAbove());

        masterDataAnalyser.analyseDeviceTypes(getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager().readDeviceTypes(),
                allMasterData.getDeviceTypes());

        masterDataAnalyser.analyseSchedules(getProtocol().getDlmsSession().getCosemObjectFactory().getScheduleManager().readSchedules(),
                allMasterData.getSchedules());

        return masterDataAnalyser;
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
            final JSONArray jsonObject = new JSONArray(serializedMasterData);
            meterDetails = ObjectMapperFactory.getObjectMapper().readValue(new StringReader(jsonObject.toString()), Beacon3100MeterDetails[].class);
        } catch (JSONException | IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
            collectedMessage.setFailureInformation(ResultType.InCompatible, beacon3100Messaging.createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }

        syncDevices(meterDetails);

        boolean cleanupUnusedMasterData = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.cleanUpUnusedDeviceTypesAttributeName).getValue());
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
                    getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager().removeDeviceType(beacon3100DeviceTypeId);
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
        for (AbstractDataType deviceType : getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager().readDeviceTypes()) {
            if (deviceType.isStructure() && deviceType.getStructure().nrOfDataTypes() > 0) {
                final long deviceTypeId = deviceType.getStructure().getDataType(0).longValue();     //First element of the structure is the deviceType ID
                deviceTypesIDs.add(deviceTypeId);
            }
        }
        return deviceTypesIDs;
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
            getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager().assignDeviceType(beacon3100MeterDetails.toStructure(isFirmwareVersion140OrAbove));
        }
    }


    private void createSchedules(List<Beacon3100Schedule> schedulesToAdd) throws NotInObjectListException {
        ScheduleManager scheduleManager = getProtocol().getDlmsSession().getCosemObjectFactory().getScheduleManager();

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
        ScheduleManager scheduleManager = getProtocol().getDlmsSession().getCosemObjectFactory().getScheduleManager();

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
        ScheduleManager scheduleManager = getProtocol().getDlmsSession().getCosemObjectFactory().getScheduleManager();

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
        ClientTypeManager clientTypeManager = getProtocol().getDlmsSession().getCosemObjectFactory().getClientTypeManager();

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
        ClientTypeManager clientTypeManager = getProtocol().getDlmsSession().getCosemObjectFactory().getClientTypeManager();

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
        ClientTypeManager clientTypeManager = getProtocol().getDlmsSession().getCosemObjectFactory().getClientTypeManager();

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

    private void createDeviceTypes(List<Beacon3100DeviceType> devicesTypesToAdd) throws NotInObjectListException {
        DeviceTypeManager deviceTypeManager = getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager();

        info.append("*** CREATING DeviceTypes ***\n");
        for (Beacon3100DeviceType beacon3100DeviceType : devicesTypesToAdd){
            try {
                deviceTypeManager.addDeviceType(beacon3100DeviceType.toStructure());
                info.append("- DeviceType ADDED: [").append(beacon3100DeviceType.getId()).append("]: ").append(beacon3100DeviceType.getName()).append("\n");
            } catch (IOException ex) {
                info.append("- Could not add DeviceType [" + beacon3100DeviceType.getId() + "]: " + ex.getMessage() + "\n");
            }
        }
    }

    private void updateDeviceTypes(List<Beacon3100DeviceType> devicesTypesToUpdate) throws NotInObjectListException {
        DeviceTypeManager deviceTypeManager = getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager();

        info.append("*** UPDATING DeviceTypes ***\n");
        for (Beacon3100DeviceType beacon3100DeviceType : devicesTypesToUpdate){
            try {
                deviceTypeManager.updateDeviceType(beacon3100DeviceType.toStructure());
                info.append("- DeviceType UPDATED: [").append(beacon3100DeviceType.getId()).append("]: ").append(beacon3100DeviceType.getName()).append("\n");
            } catch (IOException ex) {
                info.append("- Could not update DeviceType [" + beacon3100DeviceType.getId() + "]: " + ex.getMessage() + "\n");
            }
        }
    }


    private void deleteDeviceTypes(List<Long> devicesTypesToDelete) throws NotInObjectListException {
        DeviceTypeManager deviceTypeManager = getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager();

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
}
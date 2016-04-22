package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.cpo.ObjectMapperFactory;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.cosem.ClientTypeManager;
import com.energyict.dlms.cosem.DeviceTypeManager;
import com.energyict.dlms.cosem.ScheduleManager;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/08/2015 - 17:13
 */
public class MasterDataSync {

    private final Beacon3100Messaging beacon3100Messaging;

    private StringBuilder info = new StringBuilder();

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

        syncSchedules(allMasterData);
        syncClientTypes(allMasterData);
        syncDeviceTypes(allMasterData);

        collectedMessage.setDeviceProtocolInformation(info.toString());

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

    private void syncDevices(Beacon3100MeterDetails[] allMeterDetails) throws IOException {
        for (Beacon3100MeterDetails beacon3100MeterDetails : allMeterDetails) {
            getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager().assignDeviceType(beacon3100MeterDetails.toStructure());
        }
    }

    private void syncDeviceTypes(AllMasterData allMasterData) throws IOException {
        final DeviceTypeManager deviceTypeManager = getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager();
        final Array existingBeaconDeviceTypesArray = deviceTypeManager.readDeviceTypes();

        Map<Long, AbstractDataType> existingBeaconDeviceTypes = new HashMap<>();
        Map<Long, Boolean> active = new HashMap<>();

        for (AbstractDataType existingDeviceType : existingBeaconDeviceTypesArray) {
            if (existingDeviceType.isStructure() && existingDeviceType.getStructure().nrOfDataTypes() > 0) {
                final long existingDeviceTypeId = existingDeviceType.getStructure().getDataType(0).longValue(); //First element of the structure is the deviceType ID
                existingBeaconDeviceTypes.put(existingDeviceTypeId, existingDeviceType);
                active.put(existingDeviceTypeId, false); // for start consider that beacon items are obsolete
            }
        }

        info.append("DeviceType Sync:\n");

        for (Beacon3100DeviceType deviceType : allMasterData.getDeviceTypes()) {
            active.put(deviceType.getId(), true); // types found in masterdata are still active

            if (existingBeaconDeviceTypes.containsKey(deviceType.getId())) {
                if (deviceType.equals( existingBeaconDeviceTypes.get(deviceType.getId()))){
                    // do nothing, the same
                    info.append("-DeviceType SKIPPED: [" + deviceType.getId() + "] " + deviceType.getName() + "\n");
                } else {
                    info.append("-DeviceType UPDATED: [" + deviceType.getId() + "] " + deviceType.getName() + "\n");
                    deviceTypeManager.updateDeviceType(deviceType.toStructure());
                }
            } else {
                info.append("-Device type ADDED: [" + deviceType.getId() + "] " + deviceType.getName() + "\n");
                deviceTypeManager.addDeviceType(deviceType.toStructure());
            }
        }

        // delete the remaining inactive items
        for (Long beaconDeviceTypeId : active.keySet()){
            if (active.get(beaconDeviceTypeId).equals(Boolean.FALSE)){
                try {
                    deviceTypeManager.removeDeviceType(beaconDeviceTypeId);
                    info.append("-DeviceType DELETED: [" + beaconDeviceTypeId + "]\n");
                } catch (Exception ex){
                    info.append("-Could not delete DeviceType [" + beaconDeviceTypeId + "] - " + ex.getMessage() + "\n");
                }
            }
        }
    }

    private void syncClientTypes(AllMasterData allMasterData) throws IOException {
        final ClientTypeManager clientTypeManager = getProtocol().getDlmsSession().getCosemObjectFactory().getClientTypeManager();
        final Array clientTypesArray = clientTypeManager.readClients();

        Map<Long, AbstractDataType> existingClientTypes = new HashMap<>();
        Map<Long, Boolean> active = new HashMap<>();

        for (AbstractDataType clientType : clientTypesArray) {
            if (clientType.isStructure() && clientType.getStructure().nrOfDataTypes() > 0) {
                final long clientTypeId = clientType.getStructure().getDataType(0).longValue(); //First element of the structure is the clientType ID
                existingClientTypes.put(clientTypeId, clientType);
                active.put(clientTypeId, false); // for start consider that beacon items are obsolete
            }
        }

        info.append("ClientType Sync:\n");

        for (Beacon3100ClientType beacon3100ClientType : allMasterData.getClientTypes()) {
            active.put(beacon3100ClientType.getId(), true); // types found in masterdata are still active
            if (existingClientTypes.containsKey(beacon3100ClientType.getId())) {
                if (beacon3100ClientType.equals( existingClientTypes.get(beacon3100ClientType.getId()))){
                    // do nothing, the same
                    info.append("-ClientType SKIPPED: [" + beacon3100ClientType.getId() + "] ClientMacAddress:"+beacon3100ClientType.getClientMacAddress()+"\n");
                } else {
                    info.append("-ClientType UPDATED: [" + beacon3100ClientType.getId() + "] ClientMacAddress:"+beacon3100ClientType.getClientMacAddress()+"\n");
                    clientTypeManager.updateClientType(beacon3100ClientType.toStructure());
                }
            } else {
                info.append("-ClientType ADDED: [" + beacon3100ClientType.getId() + "]\n");
                clientTypeManager.addClientType(beacon3100ClientType.toStructure());
            }
        }

        // delete the remaining inactive items
        for (Long clientTypeId : active.keySet()){
            if (active.get(clientTypeId).equals(Boolean.FALSE)){
                try {
                    clientTypeManager.removeClientType(clientTypeId);
                    info.append("-ClientType DELETED: [" + clientTypeId + "]\n");
                } catch (Exception ex){
                    info.append("-Could not delete client type [" + clientTypeId + "] - "+ex.getMessage() +"\n");
                }
            }
        }
    }

    private void syncSchedules(AllMasterData allMasterData) throws IOException {
        final ScheduleManager scheduleManager = getProtocol().getDlmsSession().getCosemObjectFactory().getScheduleManager();
        final Array schedulesArray = scheduleManager.readSchedules();

        HashMap<Long, AbstractDataType> existingSchedules = new HashMap<>();
        Map<Long, Boolean> active = new HashMap<>();
        for (AbstractDataType schedule : schedulesArray) {
            if (schedule.isStructure() && schedule.getStructure().nrOfDataTypes() > 0) {
                final long scheduleId = schedule.getStructure().getDataType(0).longValue(); //First element of the structure is the schedule ID
                existingSchedules.put(scheduleId, schedule);
                active.put(scheduleId, false);
            }
        }

        info.append("Schedules Sync:\n");

        for (Beacon3100Schedule beacon3100Schedule : allMasterData.getSchedules()) {
            active.put(beacon3100Schedule.getId(), true);
            if (existingSchedules.containsKey(beacon3100Schedule.getId())) {
                if (beacon3100Schedule.equals( existingSchedules.get(beacon3100Schedule.getId()))){
                    // do nothing, the same
                    info.append("-Schedule SKIPPED: [" + beacon3100Schedule.getId() + "] " + beacon3100Schedule.getName() + "\n");
                } else {
                    info.append("-Schedule UPDATED: [" + beacon3100Schedule.getId() + "] "+beacon3100Schedule.getName()+"\n");
                    scheduleManager.updateSchedule(beacon3100Schedule.toStructure());
                }
            } else {
                info.append("-Schedule ADDED: [" + beacon3100Schedule.getId() + "] "+beacon3100Schedule.getName()+"\n");
                scheduleManager.addSchedule(beacon3100Schedule.toStructure());
            }
        }

        // delete the remaining inactive items
        for (Long scheduleId : active.keySet()){
            if (active.get(scheduleId).equals(Boolean.FALSE)){
                try{
                    scheduleManager.removeSchedule(scheduleId);
                    info.append("-Schedule DELETED: [" + scheduleId + "]\n");
                } catch (Exception ex){
                    info.append("-Could not remove schedule [" + scheduleId + "] from beacon- " + ex.getMessage() + "\n");
                }
            }
        }
    }
}
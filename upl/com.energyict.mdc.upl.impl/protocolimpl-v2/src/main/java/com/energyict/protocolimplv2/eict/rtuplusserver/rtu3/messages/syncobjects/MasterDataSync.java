package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.syncobjects;

import com.energyict.cpo.ObjectMapperFactory;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.cosem.ClientTypeManager;
import com.energyict.dlms.cosem.DeviceTypeManager;
import com.energyict.dlms.cosem.ScheduleManager;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.RTU3Messaging;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/08/2015 - 17:13
 */
public class MasterDataSync {

    private final RTU3Messaging rtu3Messaging;

    public MasterDataSync(RTU3Messaging rtu3Messaging) {
        this.rtu3Messaging = rtu3Messaging;
    }

    /**
     * Sync all master data of the device types (tasks, schedules, security levels, master data obiscodes, etc)
     */
    public CollectedMessage syncMasterData(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage, boolean all) throws IOException {
        AllMasterData allMasterData;
        try {
            final String serializedMasterData = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
            final JSONObject jsonObject = new JSONObject(serializedMasterData);
            allMasterData = ObjectMapperFactory.getObjectMapper().readValue(new StringReader(jsonObject.toString()), AllMasterData.class);
        } catch (JSONException | IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
            collectedMessage.setFailureInformation(ResultType.InCompatible, rtu3Messaging.createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }

        syncSchedules(allMasterData, all);
        syncClientTypes(allMasterData, all);
        syncDeviceTypes(allMasterData, all);

        return collectedMessage;
    }

    /**
     * Sync the meter details. This assumes that the relevant device types are already synced!
     */
    public CollectedMessage syncDeviceData(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        RTU3MeterDetails[] meterDetails;
        try {
            final String serializedMasterData = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
            final JSONArray jsonObject = new JSONArray(serializedMasterData);
            meterDetails = ObjectMapperFactory.getObjectMapper().readValue(new StringReader(jsonObject.toString()), RTU3MeterDetails[].class);
        } catch (JSONException | IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
            collectedMessage.setFailureInformation(ResultType.InCompatible, rtu3Messaging.createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }

        syncDevices(meterDetails);
        return collectedMessage;
    }

    public AbstractDlmsProtocol getProtocol() {
        return rtu3Messaging.getProtocol();
    }

    private void syncDevices(RTU3MeterDetails[] allMeterDetails) throws IOException {
        for (RTU3MeterDetails rtu3MeterDetails : allMeterDetails) {
            getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager().assignDeviceType(rtu3MeterDetails.toStructure());
        }
    }

    private void syncDeviceTypes(AllMasterData allMasterData, boolean all) throws IOException {
        final DeviceTypeManager deviceTypeManager = getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager();
        final Array deviceTypesArray = deviceTypeManager.readDeviceTypes();

        List<Long> deviceTypeIds = new ArrayList<>();
        for (AbstractDataType deviceType : deviceTypesArray) {
            if (deviceType.isStructure() && deviceType.getStructure().nrOfDataTypes() > 0) {
                final long deviceTypeId = deviceType.getStructure().getDataType(0).longValue(); //First element of the structure is the deviceType ID
                deviceTypeIds.add(deviceTypeId);
            }
        }

        List<Long> syncedDeviceTypes = new ArrayList<>();
        for (RTU3DeviceType deviceType : allMasterData.getDeviceTypes()) {
            if (deviceTypeIds.contains(deviceType.getId())) {
                deviceTypeManager.updateDeviceType(deviceType.toStructure());   //TODO optimize: only update if something's different
                syncedDeviceTypes.add(deviceType.getId());
            } else {
                deviceTypeManager.addDeviceType(deviceType.toStructure());
                syncedDeviceTypes.add(deviceType.getId());
            }
        }

        //Only remove 'ghost' data in the beacon if we just sync'ed ALL master data
        if (all) {
            //Remove device type information from the Beacon that is no longer in EIServer
            for (Long deviceTypeId : deviceTypeIds) {
                if (!syncedDeviceTypes.contains(deviceTypeId)) {
                    deviceTypeManager.removeDeviceType(deviceTypeId);
                }
            }
        }
    }

    private void syncClientTypes(AllMasterData allMasterData, boolean all) throws IOException {
        final ClientTypeManager clientTypeManager = getProtocol().getDlmsSession().getCosemObjectFactory().getClientTypeManager();
        final Array clientTypesArray = clientTypeManager.readClients();

        List<Long> clientTypeIds = new ArrayList<>();
        for (AbstractDataType clientType : clientTypesArray) {
            if (clientType.isStructure() && clientType.getStructure().nrOfDataTypes() > 0) {
                final long clientTypeId = clientType.getStructure().getDataType(0).longValue(); //First element of the structure is the clientType ID
                clientTypeIds.add(clientTypeId);
            }
        }

        List<Long> syncedClientTypes = new ArrayList<>();
        for (RTU3ClientType rtu3ClientType : allMasterData.getClientTypes()) {
            if (clientTypeIds.contains(rtu3ClientType.getId())) {
                clientTypeManager.updateClientType(rtu3ClientType.toStructure());     //TODO optimize: only update if something's different
                syncedClientTypes.add(rtu3ClientType.getId());
            } else {
                clientTypeManager.addClientType(rtu3ClientType.toStructure());
                syncedClientTypes.add(rtu3ClientType.getId());
            }
        }

        //Only remove 'ghost' data in the beacon if we just sync'ed ALL master data
        if (all) {
            //Remove client information from the Beacon that is no longer in EIServer
            for (Long clientTypeId : clientTypeIds) {
                if (!syncedClientTypes.contains(clientTypeId)) {
                    clientTypeManager.removeClientType(clientTypeId);
                }
            }
        }
    }

    private void syncSchedules(AllMasterData allMasterData, boolean all) throws IOException {
        final ScheduleManager scheduleManager = getProtocol().getDlmsSession().getCosemObjectFactory().getScheduleManager();
        final Array schedulesArray = scheduleManager.readSchedules();

        List<Long> scheduleIds = new ArrayList<>();
        for (AbstractDataType schedule : schedulesArray) {
            if (schedule.isStructure() && schedule.getStructure().nrOfDataTypes() > 0) {
                final long scheduleId = schedule.getStructure().getDataType(0).longValue(); //First element of the structure is the schedule ID
                scheduleIds.add(scheduleId);
            }
        }

        List<Long> syncedSchedules = new ArrayList<>();
        for (RTU3Schedule rtu3Schedule : allMasterData.getSchedules()) {
            if (scheduleIds.contains(rtu3Schedule.getId())) {
                scheduleManager.updateSchedule(rtu3Schedule.toStructure());   //TODO optimize: only update if something's different
                syncedSchedules.add(rtu3Schedule.getId());
            } else {
                scheduleManager.addSchedule(rtu3Schedule.toStructure());
                syncedSchedules.add(rtu3Schedule.getId());
            }
        }

        //Only remove 'ghost' data in the beacon if we just sync'ed ALL master data
        if (all) {
            //Remove scheduling information from the Beacon that is no longer in EIServer
            for (Long scheduleId : scheduleIds) {
                if (!syncedSchedules.contains(scheduleId)) {
                    scheduleManager.removeSchedule(scheduleId);
                }
            }
        }
    }
}
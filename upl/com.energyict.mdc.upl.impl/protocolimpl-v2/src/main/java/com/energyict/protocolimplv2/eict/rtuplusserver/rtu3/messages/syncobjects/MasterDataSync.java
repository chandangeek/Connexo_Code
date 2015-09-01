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
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
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
    public CollectedMessage syncMasterData(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
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

        syncSchedules(allMasterData);
        syncClientTypes(allMasterData);
        syncDeviceTypes(allMasterData);

        return collectedMessage;
    }

    /**
     * Sync the meter details. This assumes that the relevant device types are already synced!
     */
    public CollectedMessage syncDeviceData(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        RTU3MeterDetails[] meterDetails;
        try {
            final String serializedMasterData = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.dcDeviceID2AttributeName).getDeviceMessageAttributeValue();
            final JSONArray jsonObject = new JSONArray(serializedMasterData);
            meterDetails = ObjectMapperFactory.getObjectMapper().readValue(new StringReader(jsonObject.toString()), RTU3MeterDetails[].class);
        } catch (JSONException | IOException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setDeviceProtocolInformation(e.getMessage());
            collectedMessage.setFailureInformation(ResultType.InCompatible, rtu3Messaging.createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }

        syncDevices(meterDetails);

        boolean cleanupUnusedMasterData = Boolean.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.cleanUpUnusedDeviceTypesAttributeName).getDeviceMessageAttributeValue());
        if (cleanupUnusedMasterData) {
            //Remove the device types in the RTU3 data that are no longer defined in EIServer

            final List<Long> rtu3DeviceTypeIds = readDeviceTypesIDs();

            final List<Long> eiServerDeviceTypeIds = new ArrayList<>();
            for (RTU3MeterDetails meterDetail : meterDetails) {
                if (!eiServerDeviceTypeIds.contains(meterDetail.getDeviceTypeId())) {
                    eiServerDeviceTypeIds.add(meterDetail.getDeviceTypeId());
                }
            }

            for (Long rtu3DeviceTypeId : rtu3DeviceTypeIds) {
                if (shouldBeRemoved(eiServerDeviceTypeIds, rtu3DeviceTypeId)) {
                    getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager().removeDeviceType(rtu3DeviceTypeId);
                }
            }
        }

        return collectedMessage;
    }

    private boolean shouldBeRemoved(List<Long> eiServerDeviceTypeIds, Long rtu3DeviceTypeId) {
        return !eiServerDeviceTypeIds.contains(rtu3DeviceTypeId);
    }

    /**
     * Return a cached (read out only once) list of the IDs of all device types in the RTU3 data.
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
        return rtu3Messaging.getProtocol();
    }

    private void syncDevices(RTU3MeterDetails[] allMeterDetails) throws IOException {
        for (RTU3MeterDetails rtu3MeterDetails : allMeterDetails) {
            getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager().assignDeviceType(rtu3MeterDetails.toStructure());
        }
    }

    private void syncDeviceTypes(AllMasterData allMasterData) throws IOException {
        final DeviceTypeManager deviceTypeManager = getProtocol().getDlmsSession().getCosemObjectFactory().getDeviceTypeManager();
        final Array deviceTypesArray = deviceTypeManager.readDeviceTypes();

        List<Long> deviceTypeIds = new ArrayList<>();
        for (AbstractDataType deviceType : deviceTypesArray) {
            if (deviceType.isStructure() && deviceType.getStructure().nrOfDataTypes() > 0) {
                final long deviceTypeId = deviceType.getStructure().getDataType(0).longValue(); //First element of the structure is the deviceType ID
                deviceTypeIds.add(deviceTypeId);
            }
        }

        for (RTU3DeviceType deviceType : allMasterData.getDeviceTypes()) {
            if (deviceTypeIds.contains(deviceType.getId())) {
                deviceTypeManager.updateDeviceType(deviceType.toStructure());   //TODO optimize: only update if something's different
            } else {
                deviceTypeManager.addDeviceType(deviceType.toStructure());
            }
        }
    }

    private void syncClientTypes(AllMasterData allMasterData) throws IOException {
        final ClientTypeManager clientTypeManager = getProtocol().getDlmsSession().getCosemObjectFactory().getClientTypeManager();
        final Array clientTypesArray = clientTypeManager.readClients();

        List<Long> clientTypeIds = new ArrayList<>();
        for (AbstractDataType clientType : clientTypesArray) {
            if (clientType.isStructure() && clientType.getStructure().nrOfDataTypes() > 0) {
                final long clientTypeId = clientType.getStructure().getDataType(0).longValue(); //First element of the structure is the clientType ID
                clientTypeIds.add(clientTypeId);
            }
        }

        for (RTU3ClientType rtu3ClientType : allMasterData.getClientTypes()) {
            if (clientTypeIds.contains(rtu3ClientType.getId())) {
                clientTypeManager.updateClientType(rtu3ClientType.toStructure());     //TODO optimize: only update if something's different
            } else {
                clientTypeManager.addClientType(rtu3ClientType.toStructure());
            }
        }
    }

    private void syncSchedules(AllMasterData allMasterData) throws IOException {
        final ScheduleManager scheduleManager = getProtocol().getDlmsSession().getCosemObjectFactory().getScheduleManager();
        final Array schedulesArray = scheduleManager.readSchedules();

        List<Long> scheduleIds = new ArrayList<>();
        for (AbstractDataType schedule : schedulesArray) {
            if (schedule.isStructure() && schedule.getStructure().nrOfDataTypes() > 0) {
                final long scheduleId = schedule.getStructure().getDataType(0).longValue(); //First element of the structure is the schedule ID
                scheduleIds.add(scheduleId);
            }
        }

        for (RTU3Schedule rtu3Schedule : allMasterData.getSchedules()) {
            if (scheduleIds.contains(rtu3Schedule.getId())) {
                scheduleManager.updateSchedule(rtu3Schedule.toStructure());   //TODO optimize: only update if something's different
            } else {
                scheduleManager.addSchedule(rtu3Schedule.toStructure());
            }
        }
    }
}
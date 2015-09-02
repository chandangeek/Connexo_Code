package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

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

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/08/2015 - 17:13
 */
public class MasterDataSync {

    private final Beacon3100Messaging beacon3100Messaging;

    public MasterDataSync(Beacon3100Messaging beacon3100Messaging) {
        this.beacon3100Messaging = beacon3100Messaging;
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
            collectedMessage.setFailureInformation(ResultType.InCompatible, beacon3100Messaging.createMessageFailedIssue(pendingMessage, e));
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
        Beacon3100MeterDetails[] meterDetails;
        try {
            final String serializedMasterData = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.dcDeviceID2AttributeName).getDeviceMessageAttributeValue();
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
        final Array deviceTypesArray = deviceTypeManager.readDeviceTypes();

        List<Long> deviceTypeIds = new ArrayList<>();
        for (AbstractDataType deviceType : deviceTypesArray) {
            if (deviceType.isStructure() && deviceType.getStructure().nrOfDataTypes() > 0) {
                final long deviceTypeId = deviceType.getStructure().getDataType(0).longValue(); //First element of the structure is the deviceType ID
                deviceTypeIds.add(deviceTypeId);
            }
        }

        for (Beacon3100DeviceType deviceType : allMasterData.getDeviceTypes()) {
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

        for (Beacon3100ClientType beacon3100ClientType : allMasterData.getClientTypes()) {
            if (clientTypeIds.contains(beacon3100ClientType.getId())) {
                clientTypeManager.updateClientType(beacon3100ClientType.toStructure());     //TODO optimize: only update if something's different
            } else {
                clientTypeManager.addClientType(beacon3100ClientType.toStructure());
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

        for (Beacon3100Schedule beacon3100Schedule : allMasterData.getSchedules()) {
            if (scheduleIds.contains(beacon3100Schedule.getId())) {
                scheduleManager.updateSchedule(beacon3100Schedule.toStructure());   //TODO optimize: only update if something's different
            } else {
                scheduleManager.addSchedule(beacon3100Schedule.toStructure());
            }
        }
    }
}
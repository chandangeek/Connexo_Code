package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;


import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.ClientTypeManager;
import com.energyict.dlms.cosem.DeviceTypeManager;
import com.energyict.dlms.cosem.ScheduleManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by iulian on 5/27/2016.
 */
public class MasterDataAnalyser {

    private List<Beacon3100Schedule>      schedulesToUpdate;
    private List<Beacon3100Schedule>      schedulesToAdd;
    private List<Long>           schedulesToDelete;

    private List<Beacon3100ClientType>      clientTypesToUpdate;
    private List<Beacon3100ClientType>      clientTypesToAdd;
    private List<Long>                   clientTypesToDelete;

    private List<Beacon3100DeviceType>      deviceTypesToUpdate;
    private List<Beacon3100DeviceType>      deviceTypesToAdd;
    private List<Long>                   deviceTypesToDelete;

    private StringBuilder           info;

    public MasterDataAnalyser() {
        schedulesToAdd = new ArrayList<>();
        schedulesToDelete = new ArrayList<>();
        schedulesToUpdate = new ArrayList<>();
        clientTypesToDelete = new ArrayList<>();
        clientTypesToAdd = new ArrayList<>();
        clientTypesToUpdate = new ArrayList<>();
        deviceTypesToDelete = new ArrayList<>();
        deviceTypesToAdd = new ArrayList<>();
        deviceTypesToUpdate = new ArrayList<>();
        info = new StringBuilder();
    }

    public List<Beacon3100Schedule> getSchedulesToUpdate() {
        return schedulesToUpdate;
    }

    public List<Beacon3100Schedule> getSchedulesToAdd() {
        return schedulesToAdd;
    }

    public List<Long> getSchedulesToDelete() {
        return schedulesToDelete;
    }

    public List<Beacon3100ClientType> getClientTypesToUpdate() {
        return clientTypesToUpdate;
    }

    public List<Beacon3100ClientType> getClientTypesToAdd() {
        return clientTypesToAdd;
    }

    public List<Long> getClientTypesToDelete() {
        return clientTypesToDelete;
    }

    public List<Beacon3100DeviceType> getDeviceTypesToUpdate() {
        return deviceTypesToUpdate;
    }

    public List<Beacon3100DeviceType> getDeviceTypesToAdd() {
        return deviceTypesToAdd;
    }

    public List<Long> getDeviceTypesToDelete() {
        return deviceTypesToDelete;
    }

    public void analyseSchedules(Array beaconSchedulesArray, List<Beacon3100Schedule> masterDataSchedules) throws IOException {
        HashMap<Long, AbstractDataType> existingSchedules = new HashMap<>();
        Map<Long, Boolean> active = new HashMap<>();
        for (AbstractDataType schedule : beaconSchedulesArray) {
            if (schedule.isStructure() && schedule.getStructure().nrOfDataTypes() > 0) {
                final long scheduleId = schedule.getStructure().getDataType(0).longValue(); //First element of the structure is the schedule ID
                existingSchedules.put(scheduleId, schedule);
                active.put(scheduleId, false);
            }
        }

        for (Beacon3100Schedule beacon3100Schedule : masterDataSchedules) {
            active.put(beacon3100Schedule.getId(), true);
            if (existingSchedules.containsKey(beacon3100Schedule.getId())) {
                if (beacon3100Schedule.equals( existingSchedules.get(beacon3100Schedule.getId()))){
                    // do nothing, the same, SKIP
                } else {
                    // UPDATE this schedule
                    schedulesToUpdate.add(beacon3100Schedule);
                }
            } else {
                // we have to add this schedule, since it's new
                schedulesToAdd.add(beacon3100Schedule);
            }
        }

        // delete the remaining inactive items
        for (Long scheduleId : active.keySet()){
            if (!active.get(scheduleId)){
                // we have to delete this schedule
                schedulesToDelete.add(scheduleId);
            }
        }
    }


    public void analyseClientTypes(Array clientTypesArray, List<Beacon3100ClientType> masterDataClientTypes,  boolean isFirmwareVersion140OrAbove) throws IOException {

        Map<Long, AbstractDataType> existingClientTypes = new HashMap<>();
        Map<Long, Boolean> active = new HashMap<>();

        for (AbstractDataType clientType : clientTypesArray) {
            if (clientType.isStructure() && clientType.getStructure().nrOfDataTypes() > 0) {
                final long clientTypeId = clientType.getStructure().getDataType(0).longValue(); //First element of the structure is the clientType ID
                existingClientTypes.put(clientTypeId, clientType);
                active.put(clientTypeId, false); // for start consider that beacon items are obsolete
            }
        }

        for (Beacon3100ClientType beacon3100ClientType : masterDataClientTypes) {
            beacon3100ClientType.setIsFirmware140orAbove(isFirmwareVersion140OrAbove);
            active.put(beacon3100ClientType.getId(), true); // types found in masterdata are still active
            if (existingClientTypes.containsKey(beacon3100ClientType.getId())) {
                if (beacon3100ClientType.equals( existingClientTypes.get(beacon3100ClientType.getId()))){
                    // SKIP this
                } else {
                    // we'll have to update this
                    clientTypesToUpdate.add(beacon3100ClientType);
                }
            } else {
                // we'll have to add this
                clientTypesToAdd.add(beacon3100ClientType);
            }
        }

        // delete the remaining inactive items
        for (Long clientTypeId : active.keySet()){
            if (!active.get(clientTypeId)){
                // we'll have to delete this
                clientTypesToDelete.add(clientTypeId);
            }
        }
    }


    public void analyseDeviceTypes(Array existingBeaconDeviceTypesArray ,  List<Beacon3100DeviceType> masterDataDeviceTypes) throws IOException {
        Map<Long, AbstractDataType> existingBeaconDeviceTypes = new HashMap<>();
        Map<Long, Boolean> active = new HashMap<>();

        log("Analysing DeviceType existing in Beacon:");
        for (AbstractDataType existingDeviceType : existingBeaconDeviceTypesArray) {
            if (existingDeviceType.isStructure() && existingDeviceType.getStructure().nrOfDataTypes() > 0) {
                final long existingDeviceTypeId = existingDeviceType.getStructure().getDataType(0).longValue(); //First element of the structure is the deviceType ID
                existingBeaconDeviceTypes.put(existingDeviceTypeId, existingDeviceType);
                active.put(existingDeviceTypeId, false); // for start consider that beacon items are obsolete
                log("- deviceType exists in Beacon: "+existingDeviceTypeId);
            }
        }

        log("Checking DeviceType defined in EIServer:");
        for (Beacon3100DeviceType deviceType : masterDataDeviceTypes) {
            active.put(deviceType.getId(), true); // types found in masterdata are still active

            if (existingBeaconDeviceTypes.containsKey(deviceType.getId())) {
                if (deviceType.equals( existingBeaconDeviceTypes.get(deviceType.getId()))){
                    // do nothing, the same - SKIP
                    log("- deviceType is the same as in Beacon, will not be updated: "+deviceType.getId());
                } else {
                    // we'll have to update this one
                    deviceTypesToUpdate.add(deviceType);
                    log("- deviceType is different as the one in the Beacon, will be updated: "+deviceType.getId());
                }
            } else {
                // we'll have to add this one
                deviceTypesToAdd.add(deviceType);
                log("- deviceType is new (does not exist in the Beacon), will be added: "+deviceType.getId());
            }
        }

        log("Checking DeviceType which are not in use in EIServer anymore, but exists in the Beacon:");
        // delete the remaining inactive items
        for (Long beaconDeviceTypeId : active.keySet()){
            if (!active.get(beaconDeviceTypeId)){
                // we'll have to delete this one
                deviceTypesToDelete.add(beaconDeviceTypeId);
                log("- deviceType is not used anymore, will be deleted: "+beaconDeviceTypeId);
            }
        }
        log("/finished analysing DeviceTypes");
    }

    private void log(String text) {
        info.append(text + "\n");
    }

    public String getInfo() {
        return info.toString();
    }
}

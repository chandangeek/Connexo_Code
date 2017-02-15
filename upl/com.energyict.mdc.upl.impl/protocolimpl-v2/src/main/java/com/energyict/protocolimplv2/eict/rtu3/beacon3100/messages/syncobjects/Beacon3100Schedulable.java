package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.Register;
import com.energyict.mdc.tasks.ComTaskEnablement;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2015 - 14:29
 */
@XmlRootElement
public class Beacon3100Schedulable {

    private long scheduleId;
    private int logicalDeviceId;
    private int clientTypeId;
    private List<Object> profiles;
    private List<Object> registers;
    private List<Object> eventLogs;
    boolean readOldObisCodes;


    /**
     * A reference to the original comTaskEnablement that defined this masterdata
     */
    private ComTaskEnablement comTaskEnablement;

    public Beacon3100Schedulable(ComTaskEnablement comTaskEnablement, long scheduleId, int logicalDeviceId, int clientTypeId, List<Object> profiles, List<Object> registers, List<Object> eventLogs) {
        this.comTaskEnablement = comTaskEnablement;
        this.scheduleId = scheduleId;
        this.logicalDeviceId = logicalDeviceId;
        this.clientTypeId = clientTypeId;
        this.profiles = profiles;
        this.registers = registers;
        this.eventLogs = eventLogs;
        readOldObisCodes = true;
    }

    public Beacon3100Schedulable(ComTaskEnablement comTaskEnablement, long scheduleId, int logicalDeviceId, int clientTypeId, List<Object> profiles, List<Object> registers, List<Object> eventLogs, boolean readOldObisCodes) {
        this.comTaskEnablement = comTaskEnablement;
        this.scheduleId = scheduleId;
        this.logicalDeviceId = logicalDeviceId;
        this.clientTypeId = clientTypeId;
        this.profiles = profiles;
        this.registers = registers;
        this.eventLogs = eventLogs;
        this.readOldObisCodes = readOldObisCodes;
    }

    //JSon constructor
    private Beacon3100Schedulable() {
    }

    public Structure toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(new Unsigned32(getScheduleId()));
        structure.addDataType(new Unsigned16(getLogicalDeviceId()));
        structure.addDataType(new Unsigned32(getClientTypeId()));

        final Array profileArray = new Array();
        for (Object obisCode : getProfiles()) {
            profileArray.addDataType(OctetString.fromObisCode((ObisCode) obisCode));
        }
        structure.addDataType(profileArray);

        final Array registerArray = new Array();
        for (Object obisCode : getRegisters()) {
            registerArray.addDataType(OctetString.fromObisCode((ObisCode)obisCode));
        }
        structure.addDataType(registerArray);

        final Array eventLogArray = new Array();
        for (Object obisCode : getEventLogs()) {
            eventLogArray.addDataType(OctetString.fromObisCode((ObisCode)obisCode));
        }
        structure.addDataType(eventLogArray);

        return structure;
    }

    public Structure toStructureForNewFirmware() {
        final Structure structure = new Structure();
        structure.addDataType(new Unsigned32(getScheduleId()));
        structure.addDataType(new Unsigned16(getLogicalDeviceId()));
        structure.addDataType(new Unsigned32(getClientTypeId()));

        final Array profileArray = new Array();
        for (Object profileItem : getProfiles()) {
            profileArray.addDataType(((RegisterItem)profileItem).toStructure());
        }
        structure.addDataType(profileArray);

        final Array registerArray = new Array();
        for (Object registerItem : getRegisters()) {
            registerArray.addDataType(((RegisterItem)registerItem).toStructure());
        }
        structure.addDataType(registerArray);

        final Array eventLogArray = new Array();
        for (Object eventLogItem : getEventLogs()) {
            eventLogArray.addDataType(((RegisterItem)eventLogItem).toStructure());
        }
        structure.addDataType(eventLogArray);

        return structure;
    }

    public ComTaskEnablement getComTaskEnablement() {
        return comTaskEnablement;
    }

    @XmlAttribute
    public long getScheduleId() {
        return scheduleId;
    }

    @XmlAttribute
    public int getLogicalDeviceId() {
        return logicalDeviceId;
    }

    @XmlAttribute
    public int getClientTypeId() {
        return clientTypeId;
    }

    @XmlAttribute
    public List<Object> getProfiles() {
        return profiles;
    }

    @XmlAttribute
    public List<Object> getRegisters() {
        return registers;
    }

    @XmlAttribute
    public List<Object> getEventLogs() {
        return eventLogs;
    }

    public boolean updateBufferSizeForRegister(ObisCode obisCode, int bufferSize) {
        RegisterItem registerItem = findObisCode(obisCode, registers);
        if(registerItem != null){
            registerItem.setBufferSize(bufferSize);
            return true;
        }
        return false;
    }

    public void updateBufferSizeForAllRegisters(int bufferSize) {
        for(Object registerItem : registers){
            ((RegisterItem)registerItem).setBufferSize(bufferSize);
        }
    }

    public boolean updateBufferSizeForLoadProfile(ObisCode obisCode, int bufferSize) {
        RegisterItem registerItem = findObisCode(obisCode, profiles);
        if(registerItem != null){
            registerItem.setBufferSize(bufferSize);
            return true;
        }
        return false;
    }

    public void updateBufferSizeForAllLoadProfiles(int bufferSize) {
        for(Object registerItem : profiles){
            ((RegisterItem)registerItem).setBufferSize(bufferSize);
        }
    }

    public boolean updateBufferSizeForEventLogs(ObisCode obisCode, int bufferSize) {
        RegisterItem registerItem = findObisCode(obisCode, eventLogs);
        if(registerItem != null){
            registerItem.setBufferSize(bufferSize);
            return true;
        }
        return false;
    }

    public void updateBufferSizeForAllEventLogs(int bufferSize) {
        for(Object registerItem : eventLogs){
            ((RegisterItem)registerItem).setBufferSize(bufferSize);
        }
    }

    public RegisterItem findObisCode(ObisCode obisCode, List<Object> list){
        for(Object registerItem : list){
            if(((RegisterItem)registerItem).getObisCode().equals(obisCode)){
                return (RegisterItem)registerItem;
            }
        }
        return null;
    }
}
package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.Register;
import com.energyict.mdc.tasks.ComTaskEnablement;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.abnt.common.structure.field.EventField;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashMap;
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
        for (Object item : getProfiles()) {
            LoadProfileItem loadProfileItem = new LoadProfileItem(getObisCodeFromLinkedHashMap((LinkedHashMap) item), new Unsigned32(0));
            profileArray.addDataType((loadProfileItem).toStructure());
        }
        structure.addDataType(profileArray);

        final Array registerArray = new Array();
        for (Object item : getRegisters()) {
            RegisterItem registerItem = new RegisterItem(getObisCodeFromLinkedHashMap((LinkedHashMap)item), new Unsigned16(0));
            registerArray.addDataType(registerItem.toStructure());
        }
        structure.addDataType(registerArray);

        final Array eventLogArray = new Array();
        for (Object item : getEventLogs()) {
            EventLogItem eventLogItem = new EventLogItem(getObisCodeFromLinkedHashMap((LinkedHashMap)item), new Unsigned32(0));
            eventLogArray.addDataType(eventLogItem.toStructure());
        }
        structure.addDataType(eventLogArray);

        return structure;
    }

    private ObisCode getObisCodeFromLinkedHashMap(LinkedHashMap item) {
        int a = ((Integer) (item).get("a")).intValue();
        int b = ((Integer) (item).get("b")).intValue();
        int c = ((Integer) (item).get("c")).intValue();
        int d = ((Integer) (item).get("d")).intValue();
        int e = ((Integer) (item).get("e")).intValue();
        int f = ((Integer) (item).get("f")).intValue();
        return new ObisCode(a,b,c,d,e,f);
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

    public boolean updateBufferSizeForRegister(ObisCode obisCode, Unsigned16 bufferSize) {
        RegisterItem registerItem = (RegisterItem) Item.findObisCode(obisCode, registers);
        if(registerItem != null){
            registerItem.setBufferSize(bufferSize);
            return true;
        }
        return false;
    }

    public void updateBufferSizeForAllRegisters(Unsigned16 bufferSize) {
        for(Object registerItem : registers){
            ((RegisterItem)registerItem).setBufferSize(bufferSize);
        }
    }

    public boolean updateBufferSizeForLoadProfile(ObisCode obisCode, Unsigned32 bufferSize) {
        LoadProfileItem loadProfileItem = (LoadProfileItem) Item.findObisCode(obisCode, profiles);
        if(loadProfileItem != null){
            loadProfileItem.setBufferSize(bufferSize);
            return true;
        }
        return false;
    }

    public void updateBufferSizeForAllLoadProfiles(Unsigned32 bufferSize) {
        for(Object loadProfileItem : profiles){
            ((LoadProfileItem)loadProfileItem).setBufferSize(bufferSize);
        }
    }

    public boolean updateBufferSizeForEventLogs(ObisCode obisCode, Unsigned32 bufferSize) {
        EventLogItem registerItem = (EventLogItem) Item.findObisCode(obisCode, eventLogs);
        if(registerItem != null){
            registerItem.setBufferSize(bufferSize);
            return true;
        }
        return false;
    }

    public void updateBufferSizeForAllEventLogs(Unsigned32 bufferSize) {
        for(Object eventLogItem : eventLogs){
            ((EventLogItem)eventLogItem).setBufferSize(bufferSize);
        }
    }

}
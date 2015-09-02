package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.*;
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
    private List<ObisCode> profiles;
    private List<ObisCode> registers;
    private List<ObisCode> eventLogs;

    /**
     * A reference to the original comTaskEnablement that defined this masterdata
     */
    private ComTaskEnablement comTaskEnablement;

    public Beacon3100Schedulable(ComTaskEnablement comTaskEnablement, long scheduleId, int logicalDeviceId, int clientTypeId, List<ObisCode> profiles, List<ObisCode> registers, List<ObisCode> eventLogs) {
        this.comTaskEnablement = comTaskEnablement;
        this.scheduleId = scheduleId;
        this.logicalDeviceId = logicalDeviceId;
        this.clientTypeId = clientTypeId;
        this.profiles = profiles;
        this.registers = registers;
        this.eventLogs = eventLogs;
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
        for (ObisCode obisCode : getProfiles()) {
            profileArray.addDataType(OctetString.fromObisCode(obisCode));
        }
        structure.addDataType(profileArray);

        final Array registerArray = new Array();
        for (ObisCode obisCode : getRegisters()) {
            registerArray.addDataType(OctetString.fromObisCode(obisCode));
        }
        structure.addDataType(registerArray);

        final Array eventLogArray = new Array();
        for (ObisCode obisCode : getEventLogs()) {
            eventLogArray.addDataType(OctetString.fromObisCode(obisCode));
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
    public List<ObisCode> getProfiles() {
        return profiles;
    }

    @XmlAttribute
    public List<ObisCode> getRegisters() {
        return registers;
    }

    @XmlAttribute
    public List<ObisCode> getEventLogs() {
        return eventLogs;
    }
}
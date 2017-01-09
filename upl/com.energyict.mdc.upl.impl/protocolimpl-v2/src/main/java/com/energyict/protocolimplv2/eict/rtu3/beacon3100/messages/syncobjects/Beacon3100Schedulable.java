package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
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

    private long originalId;
    private long scheduleId;
    private int logicalDeviceId;
    private long clientTypeId;
    private List<ObisCode> profiles;
    private List<ObisCode> registers;
    private List<ObisCode> eventLogs;

    public Beacon3100Schedulable(long originalId, long scheduleId, int logicalDeviceId, long clientTypeId, List<ObisCode> profiles, List<ObisCode> registers, List<ObisCode> eventLogs) {
        this.originalId = originalId;
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

    @XmlAttribute
    public long getOriginalId() {
        return originalId;
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
    public long getClientTypeId() {
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
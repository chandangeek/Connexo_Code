package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.syncobjects;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned32;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2015 - 14:13
 */
@XmlRootElement
public class RTU3DeviceType {

    private long id;
    private String name;
    private RTU3ProtocolConfiguration protocolConfiguration;
    private List<RTU3Schedulable> schedulables;
    private RTU3ClockSyncConfiguration clockSyncConfiguration;
    private RTU3MeterSerialConfiguration meterSerialConfiguration;

    /**
     * Note that the ID is actually the one of the device type configuration, since every new config is considered as a unique new device type in the Beacon model.
     */
    public RTU3DeviceType(long id, String name, RTU3MeterSerialConfiguration meterSerialConfiguration, RTU3ProtocolConfiguration protocolConfiguration, List<RTU3Schedulable> schedulables, RTU3ClockSyncConfiguration clockSyncConfiguration) {
        this.id = id;
        this.name = name;
        this.meterSerialConfiguration = meterSerialConfiguration;
        this.protocolConfiguration = protocolConfiguration;
        this.schedulables = schedulables;
        this.clockSyncConfiguration = clockSyncConfiguration;
    }

    //JSon constructor
    private RTU3DeviceType() {
    }

    public Structure toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(new Unsigned32(getId()));
        structure.addDataType(OctetString.fromString(getName()));
        structure.addDataType(getMeterSerialConfiguration().toStructure());
        structure.addDataType(getProtocolConfiguration().toStructure());

        final Array schedulableArray = new Array();
        for (RTU3Schedulable rtu3Schedulable : getSchedulables()) {
            schedulableArray.addDataType(rtu3Schedulable.toStructure());
        }
        structure.addDataType(schedulableArray);

        structure.addDataType(getClockSyncConfiguration().toStructure());

        return structure;
    }

    @XmlAttribute
    public long getId() {
        return id;
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    @XmlAttribute
    public RTU3MeterSerialConfiguration getMeterSerialConfiguration() {
        return meterSerialConfiguration;
    }

    @XmlAttribute
    public RTU3ProtocolConfiguration getProtocolConfiguration() {
        return protocolConfiguration;
    }

    @XmlAttribute
    public List<RTU3Schedulable> getSchedulables() {
        return schedulables;
    }

    @XmlAttribute
    public RTU3ClockSyncConfiguration getClockSyncConfiguration() {
        return clockSyncConfiguration;
    }
}
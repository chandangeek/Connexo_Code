package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2015 - 14:13
 */
@XmlRootElement
public class Beacon3100DeviceType {

    /**
     * Parses the given {@link Structure} received from the device into a {@link Beacon3100DeviceType} object.
     *
     * @param structure THe {@link Structure} received from the device.
     * @return The parsed {@link Beacon3100DeviceType}.
     * @throws IOException        If an error occurs parsing the data.
     */
    public static final Beacon3100DeviceType fromStructure(final Structure structure) throws IOException {
        final long id = structure.getDataType(0, Unsigned32.class).longValue();
        final String name = structure.getDataType(1, OctetString.class).stringValue();
        final Beacon3100MeterSerialConfiguration serialConfiguration = Beacon3100MeterSerialConfiguration.fromStructure(structure.getDataType(2, Structure.class));
        final Beacon3100ProtocolConfiguration protocolConfiguration = Beacon3100ProtocolConfiguration.fromStructure(structure.getDataType(3, Structure.class));

        final List<Beacon3100Schedulable> schedulables = new ArrayList<>();
        final Array schedulableArray = structure.getDataType(4, Array.class);

        for (final AbstractDataType schedulable : schedulableArray) {
            schedulables.add(Beacon3100Schedulable.fromStructure(schedulable.getStructure()));
        }

        final Beacon3100ClockSyncConfiguration clockSyncConfiguration = Beacon3100ClockSyncConfiguration.fromStructure(structure.getDataType(5, Structure.class));

        return new Beacon3100DeviceType(id, name, serialConfiguration, protocolConfiguration, schedulables, clockSyncConfiguration, false);
    }

    private long id;
    private String name;
    private Beacon3100ProtocolConfiguration protocolConfiguration;
    private List<Beacon3100Schedulable> schedulables;
    private Beacon3100ClockSyncConfiguration clockSyncConfiguration;
    private Beacon3100MeterSerialConfiguration meterSerialConfiguration;
    boolean readOldObisCodes;

    /**
     * Note that the ID is actually the one of the device type configuration, since every new config is considered as a unique new device type in the Beacon model.
     */
    public Beacon3100DeviceType(long id, String name, Beacon3100MeterSerialConfiguration meterSerialConfiguration, Beacon3100ProtocolConfiguration protocolConfiguration, List<Beacon3100Schedulable> schedulables, Beacon3100ClockSyncConfiguration clockSyncConfiguration, boolean readOldObisCodes) {
        this.id = id;
        this.name = name;
        this.meterSerialConfiguration = meterSerialConfiguration;
        this.protocolConfiguration = protocolConfiguration;
        this.schedulables = schedulables;
        this.clockSyncConfiguration = clockSyncConfiguration;
        this.readOldObisCodes = readOldObisCodes;
    }

    public boolean equals(AbstractDataType anotherClientTypeStructure) {

        try {
            byte[] otherByteArray = anotherClientTypeStructure.getBEREncodedByteArray();
            byte[] thisByteArray = toStructure(readOldObisCodes).getBEREncodedByteArray();

            return Arrays.equals(thisByteArray, otherByteArray);

        } catch (Exception ex) {
            return false;
        }
    }

    public boolean equals(Beacon3100ClientType anotherClientType) {
        return this.equals(toStructure(readOldObisCodes));
    }

    //JSon constructor
    private Beacon3100DeviceType() {
    }

    public Structure toStructure() {
        return toStructure(true);
    }

    public Structure toStructure(boolean oldFirmware) {
        final Structure structure = new Structure();
        structure.addDataType(new Unsigned32(getId()));
        structure.addDataType(OctetString.fromString(getName()));
        structure.addDataType(getMeterSerialConfiguration().toStructure());
        structure.addDataType(getProtocolConfiguration().toStructure());

        final Array schedulableArray = new Array();
        for (Beacon3100Schedulable beacon3100Schedulable : getSchedulables()) {
            schedulableArray.addDataType(toStructure(beacon3100Schedulable, oldFirmware));
        }
        structure.addDataType(schedulableArray);

        structure.addDataType(getClockSyncConfiguration().toStructure());

        return structure;
    }

    private Structure toStructure(Beacon3100Schedulable beacon3100Schedulable, boolean oldFirmware) {
        if (oldFirmware) {
            return beacon3100Schedulable.toStructure();
        } else {
            return beacon3100Schedulable.toStructureForNewFirmware();
        }
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
    public Beacon3100MeterSerialConfiguration getMeterSerialConfiguration() {
        return meterSerialConfiguration;
    }

    @XmlAttribute
    public Beacon3100ProtocolConfiguration getProtocolConfiguration() {
        return protocolConfiguration;
    }

    @XmlAttribute
    public List<Beacon3100Schedulable> getSchedulables() {
        return schedulables;
    }

    @XmlAttribute
    public Beacon3100ClockSyncConfiguration getClockSyncConfiguration() {
        return clockSyncConfiguration;
    }

    public boolean updateBufferSizeForRegister(ObisCode obisCode, Unsigned16 bufferSize) {
        for (Beacon3100Schedulable schedulable : schedulables) {
            if (schedulable.updateBufferSizeForRegister(obisCode, bufferSize))
                return true;
        }
        return false;
    }

    public boolean updateBufferSizeForAllRegisters(Unsigned16 bufferSize) {
        boolean updated = false;
        for (Beacon3100Schedulable schedulable : schedulables) {
            schedulable.updateBufferSizeForAllRegisters(bufferSize);
            updated = updated || schedulable.getRegisters().size() != 0;
        }
        return updated;
    }

    public boolean updateBufferSizeForLoadProfiles(ObisCode obisCode, Unsigned32 bufferSize) {
        for (Beacon3100Schedulable schedulable : schedulables) {
            if (schedulable.updateBufferSizeForLoadProfile(obisCode, bufferSize))
                return true;
        }
        return false;
    }

    public boolean updateBufferSizeForAllLoadProfiles(Unsigned32 bufferSize) {
        boolean updated = false;
        for (Beacon3100Schedulable schedulable : schedulables) {
            schedulable.updateBufferSizeForAllLoadProfiles(bufferSize);
            updated = updated || schedulable.getProfiles().size() != 0;
        }
        return updated;
    }

    public boolean updateBufferSizeForEventLogs(ObisCode obisCode, Unsigned32 bufferSize) {
        for (Beacon3100Schedulable schedulable : schedulables) {
            if (schedulable.updateBufferSizeForEventLogs(obisCode, bufferSize))
                return true;
        }
        return false;
    }

    public boolean updateBufferSizeForAllEventLogs(Unsigned32 bufferSize) {
        boolean updated = false;
        for (Beacon3100Schedulable schedulable : schedulables) {
            schedulable.updateBufferSizeForAllEventLogs(bufferSize);
            updated = updated || schedulable.getEventLogs().size() != 0;
        }
        return updated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Beacon3100DeviceType other = (Beacon3100DeviceType) obj;
        if (id != other.id)
            return false;
        return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {
		return new StringBuilder("ID [").append(this.id).append("], name [").append(this.name).append("]").toString();
    }
}
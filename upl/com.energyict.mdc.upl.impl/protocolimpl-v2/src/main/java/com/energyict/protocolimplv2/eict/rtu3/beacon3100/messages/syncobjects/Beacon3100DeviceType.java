package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
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

    public boolean equals(AbstractDataType anotherClientTypeStructure){

        try {
            byte[] otherByteArray = anotherClientTypeStructure.getBEREncodedByteArray();
            byte[] thisByteArray = toStructure(readOldObisCodes).getBEREncodedByteArray();

            return Arrays.equals(thisByteArray, otherByteArray);

        }catch (Exception ex){
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
        if(oldFirmware) {
            return beacon3100Schedulable.toStructure();
        }else{
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

    public boolean updateBufferSizeForRegister(ObisCode obisCode, int bufferSize) {
        for(Beacon3100Schedulable schedulable: schedulables){
            if(schedulable.updateBufferSizeForRegister(obisCode, bufferSize))
                return true;
        }
        return false;
    }

    public boolean updateBufferSizeForAllRegisters(int bufferSize) {
        for(Beacon3100Schedulable schedulable: schedulables){
           schedulable.updateBufferSizeForAllRegisters(bufferSize);
        }
        return false;
    }

    public boolean updateBufferSizeForLoadProfiles(ObisCode obisCode, int bufferSize) {
        for(Beacon3100Schedulable schedulable: schedulables){
            if(schedulable.updateBufferSizeForLoadProfile(obisCode, bufferSize))
                return true;
        }
        return false;
    }

    public boolean updateBufferSizeForAllLoadProfiles(int bufferSize) {
        for(Beacon3100Schedulable schedulable: schedulables){
            schedulable.updateBufferSizeForAllLoadProfiles(bufferSize);
        }
        return false;
    }

    public boolean updateBufferSizeForEventLogs(ObisCode obisCode, int bufferSize) {
        for(Beacon3100Schedulable schedulable: schedulables){
            if(schedulable.updateBufferSizeForEventLogs(obisCode, bufferSize))
                return true;
        }
        return false;
    }

    public boolean updateBufferSizeForAllEventLogs(int bufferSize) {
        for(Beacon3100Schedulable schedulable: schedulables){
            schedulable.updateBufferSizeForAllEventLogs(bufferSize);
        }
        return false;
    }
}
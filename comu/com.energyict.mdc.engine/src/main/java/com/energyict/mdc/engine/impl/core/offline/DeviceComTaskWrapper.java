package com.energyict.mdc.engine.impl.core.offline;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 15/07/2014 - 16:59
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@XmlRootElement
public class DeviceComTaskWrapper {

    private final long comTaskId;
    private final DeviceIdentifier deviceIdentifier;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private DeviceComTaskWrapper() {
        this.comTaskId = 0;
        this.deviceIdentifier = null;
    }

    public DeviceComTaskWrapper(DeviceIdentifier deviceIdentifier, long comTaskId) {
        this.deviceIdentifier = deviceIdentifier;
        this.comTaskId = comTaskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceComTaskWrapper that = (DeviceComTaskWrapper) o;

        return
                (this.comTaskId == that.comTaskId)
                        && (DeviceIdentifier.is(this.getDeviceIdentifier()).equalTo(that.getDeviceIdentifier()));
    }

    /**
     * 2 objects containing the same comtaskId and identifier will have the same hash code.
     * This is used when getting values from a hashmap!
     */
    @Override
    public int hashCode() {
        int result = Long.hashCode(comTaskId);
        result = 31 * result + deviceIdentifier.hashCode();
        return result;
    }

    @XmlAttribute
    public long getComTaskId() {
        return comTaskId;
    }

    @XmlAttribute
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}

package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.identifiers.DeviceIdentifierForAlreadyKnownDevice;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wraps a {@link DeviceProtocolCache} for the purpose of
 * using it as a top level xml document
 * in methods that are part of the remote query api.
 */
@XmlRootElement
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public class DeviceProtocolCacheXmlWrapper {

    private DeviceIdentifier deviceIdentifier;
    private DeviceProtocolCache deviceProtocolCache;

    public DeviceProtocolCacheXmlWrapper() {
        super();
    }

    public DeviceProtocolCacheXmlWrapper(DeviceIdentifier deviceIdentifier, DeviceProtocolCache deviceProtocolCache) {
        this();
        this.deviceIdentifier = deviceIdentifier;
        this.deviceProtocolCache = deviceProtocolCache;
    }

    @XmlElements(
            {@XmlElement(type = DeviceIdentifierById.class), @XmlElement(type = DeviceIdentifierBySerialNumber.class), @XmlElement(type = DeviceIdentifierForAlreadyKnownDevice.class)}
    )
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @XmlElement
    public DeviceProtocolCache getDeviceProtocolCache() {
        return deviceProtocolCache;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}
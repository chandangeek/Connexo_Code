package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.syncobjects;

import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2015 - 14:38
 */
@XmlRootElement
public class RTU3ProtocolConfiguration {

    private String className;

    /**
     * The protocol general properties and the dialect properties of the master device.
     * The security and connection properties are not included, they are stored in other sync objects.
     */
    private TypedProperties properties;

    public RTU3ProtocolConfiguration(String className, TypedProperties properties) {
        this.className = className;
        this.properties = properties;
    }

    //JSon constructor
    private RTU3ProtocolConfiguration() {
    }

    public Structure toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(OctetString.fromString(getClassName()));

        final Array protocolTypedProperties = new Array();
        for (String name : getProperties().propertyNames()) {
            final Object value = getProperties().getProperty(name);

            //Only add if the type of the value is supported
            if (value != null && RTU3ProtocolTypedProperty.isSupportedType(value)) {
                protocolTypedProperties.addDataType(new RTU3ProtocolTypedProperty(name, value).toStructure());
            }
        }
        structure.addDataType(protocolTypedProperties);

        return structure;
    }

    @XmlAttribute
    public String getClassName() {
        return className;
    }

    @XmlAttribute
    public TypedProperties getProperties() {
        return properties;
    }
}
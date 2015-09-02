package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

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
public class Beacon3100ProtocolConfiguration {

    private String className;

    /**
     * The protocol general properties and the dialect properties of the master device.
     * The security and connection properties are not included, they are stored in other sync objects.
     */
    private TypedProperties properties;

    public Beacon3100ProtocolConfiguration(String className, TypedProperties properties) {
        this.className = className;
        this.properties = properties;
    }

    //JSon constructor
    private Beacon3100ProtocolConfiguration() {
    }

    public Structure toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(OctetString.fromString(getClassName()));

        final Array protocolTypedProperties = new Array();
        for (String name : getProperties().propertyNames()) {
            final Object value = getProperties().getProperty(name);

            //Only add if the type of the value is supported
            if (value != null && Beacon3100ProtocolTypedProperty.isSupportedType(value)) {
                protocolTypedProperties.addDataType(new Beacon3100ProtocolTypedProperty(name, value).toStructure());
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
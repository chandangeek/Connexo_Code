package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.mdc.upl.properties.HexString;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.TemporalAmount;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 25/08/2015 - 14:18
 */
@XmlRootElement
public class Beacon3100ProtocolTypedProperty {
	
	/**
	 * Parses the given protocol property from the given {@link Structure}.
	 * 
	 * @param 		structure	The {@link Structure} received from the device.
	 * 
	 * @return		The {@link Structure} parsed into a {@link Beacon3100ProtocolTypedProperty}.
	 * 
	 * @throws 		IOException		If an IO error occurs.
	 */
	public static final Beacon3100ProtocolTypedProperty fromStructure(final Structure structure) throws IOException {
		final String name = structure.getDataType(0, OctetString.class).stringValue();
		final String value = structure.getDataType(1, OctetString.class).stringValue();
		final PropertyType type = PropertyType.forType(structure.getDataType(2, TypeEnum.class).getValue());
		
		return new Beacon3100ProtocolTypedProperty(name, value, type);
	}

    private String name;
    private String value;
    private PropertyType type;

    //JSon constructor
    private Beacon3100ProtocolTypedProperty() {

    }

    /**
     * Create a new instance.
     * 
     * @param 	name		The name of the property.	
     * @param 	value		The value of the property.
     * @param 	type		The type of the property.
     */
    public Beacon3100ProtocolTypedProperty(final String name, final String value, final PropertyType type) {
    	this.name = name;
    	this.value = value;
    	this.type = type;
    }
    
    public Beacon3100ProtocolTypedProperty(String name, Object value) {
        this.name = name;

        if (value instanceof BigDecimal) {
            type = PropertyType.BigDecimal;
            this.value = value.toString();
        } else if (value instanceof TemporalAmount) {
            type = PropertyType.TimeDuration;
            this.value = String.valueOf(((Duration) value).toMillis());
        } else if (value instanceof String) {
            type = PropertyType.String;
            this.value = (String) value;
        } else if (value instanceof HexString) {
            type = PropertyType.String;
            this.value = ((HexString) value).getContent();
        } else if (value instanceof Boolean) {
            type = PropertyType.Boolean;
            this.value = value.toString();
        } else {
            type = PropertyType.Unknown;
            this.value = value.toString();
        }
    }

    public static boolean isSupportedType(Object value) {
        return value instanceof HexString || value instanceof BigDecimal || value instanceof TemporalAmount || value instanceof String || value instanceof Boolean;
    }

    public Structure toStructure() {
        final Structure result = new Structure();
        result.addDataType(OctetString.fromString(getName()));
        result.addDataType(OctetString.fromString(getValue()));
        result.addDataType(new TypeEnum(getType().getType()));
        return result;
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    @XmlAttribute
    public String getValue() {
        return value;
    }

    @XmlAttribute
    public PropertyType getType() {
        return type;
    }

    public enum PropertyType {
        Unknown(-1),
        String(0),
        BigDecimal(1),
        TimeDuration(2),
        Boolean(3);

    	/**
    	 * Returns the {@link PropertyType} that matches the given type ID.
    	 * 
    	 * @param 		type		The ID of the type.
    	 * 
    	 * @return		The matching {@link PropertyType}.
    	 */
    	public static final PropertyType forType(final int type) {
    		for (final PropertyType propertyType : PropertyType.values()) {
    			if (propertyType.type == type) {
    				return propertyType;
    			}
    		}
    		
    		return null;
    	}
    	
        private final int type;

        PropertyType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }
}

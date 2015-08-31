package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.syncobjects;

import com.energyict.cbo.HexString;
import com.energyict.cbo.TimeDuration;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 25/08/2015 - 14:18
 */
@XmlRootElement
public class RTU3ProtocolTypedProperty {

    private final String name;
    private final String value;
    private final PropertyType type;

    public RTU3ProtocolTypedProperty(String name, Object value) {
        this.name = name;

        if (value instanceof BigDecimal) {
            type = PropertyType.BigDecimal;
            this.value = ((BigDecimal) value).toString();
        } else if (value instanceof TimeDuration) {
            type = PropertyType.TimeDuration;
            this.value = String.valueOf(((TimeDuration) value).getMilliSeconds());
        } else if (value instanceof String) {
            type = PropertyType.String;
            this.value = (String) value;
        } else if (value instanceof HexString) {
            type = PropertyType.String;
            this.value = ((HexString) value).getContent();
        } else if (value instanceof Boolean) {
            type = PropertyType.Boolean;
            this.value = ((Boolean) value).toString();
        } else {
            type = PropertyType.Unknown;
            this.value = value.toString();
        }
    }

    public static boolean isSupportedType(Object value) {
        return value instanceof HexString || value instanceof BigDecimal || value instanceof TimeDuration || value instanceof String || value instanceof Boolean;
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

    private enum PropertyType {
        Unknown(-1),
        String(0),
        BigDecimal(1),
        TimeDuration(2),
        Boolean(3);

        private final int type;

        private PropertyType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }
}

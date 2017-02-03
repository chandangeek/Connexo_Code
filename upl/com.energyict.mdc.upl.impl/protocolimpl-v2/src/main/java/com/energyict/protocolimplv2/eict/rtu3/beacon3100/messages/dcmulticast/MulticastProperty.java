package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.mdc.upl.properties.HexString;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 25/08/2015 - 14:18
 */
@XmlRootElement
public class MulticastProperty {

    /* Property name */
    private String name;
    /* Set to zero (utf-8 string) */
    private String value;
    /* Property value */
    private PropertyType type;

    //JSon constructor
    private MulticastProperty() {
    }

    public MulticastProperty(String name, Object value) {
        this.name = name;

        if (value instanceof BigDecimal) {
            type = PropertyType.BigDecimal;
            this.value = value.toString();
        } else if (value instanceof TemporalAmount) {
            type = PropertyType.TimeDuration;
            this.value = String.valueOf(((TemporalAmount) value).get(ChronoUnit.MILLIS));
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
        result.addDataType(new TypeEnum(getType().getType()));
        result.addDataType(OctetString.fromString(getValue()));
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

        PropertyType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }
}
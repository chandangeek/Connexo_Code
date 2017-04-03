package com.energyict.mdc.protocol.pluggable.adapters.upl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.pluggable.UPLEndDeviceGroupHolder;

import java.util.Properties;

/**
 * Adapts the CXO values of the given TypedProperties to UPL values, so that the 9.1 protocols can use them.
 *
 * @author khe
 * @since 17/02/2017 - 13:49
 */
public class TypedPropertiesValueAdapter {

    public static com.energyict.mdc.upl.properties.TypedProperties adaptToUPLValues(com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        TypedProperties result = TypedProperties.empty();
        for (String name : typedProperties.propertyNames()) {
            Object typedProperty = typedProperties.getTypedProperty(name);
            result.setProperty(name, adaptToUPLValue(typedProperty));
        }
        return result;
    }

    public static com.energyict.mdc.upl.properties.TypedProperties adaptToUPLValues(Properties properties) {
        TypedProperties result = TypedProperties.empty();
        for (String name : properties.stringPropertyNames()) {
            Object typedProperty = properties.get(name);
            result.setProperty(name, adaptToUPLValue(typedProperty));
        }
        return result;
    }

    public static Object adaptToUPLValue(Object value) {
        if (value instanceof TimeDuration) {
            value = ((TimeDuration) value).asTemporalAmount();
        } else if (value instanceof Calendar) {
            value = new TariffCalendarAdapter(((Calendar) value));
        } else if (value instanceof EndDeviceGroup) {
            return UPLEndDeviceGroupHolder.from((EndDeviceGroup) value);
        }
        //TODO complete
        return value;
    }

    // Hide utility class constructor
    private TypedPropertiesValueAdapter() {}
}
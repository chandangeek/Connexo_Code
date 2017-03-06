package com.energyict.mdc.protocol.pluggable.adapters.upl;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.TypedProperties;

/**
 * Copyrights EnergyICT
 * <p>
 * Adapts the CXO values of the given TypedProperties to UPL values, so that the 9.1 protocols can use them.
 *
 * @author khe
 * @since 17/02/2017 - 13:49
 */
public class TypedPropertiesValueAdapter {

    private TypedPropertiesValueAdapter() {
    }

    public static com.energyict.mdc.upl.properties.TypedProperties adaptToUPLValues(com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        com.energyict.mdc.upl.properties.TypedProperties result = TypedProperties.empty();

        for (String name : typedProperties.propertyNames()) {
            Object typedProperty = typedProperties.getTypedProperty(name);
            result.setProperty(name, adaptToUPLValue(typedProperty));
        }
        return result;
    }

    public static Object adaptToUPLValue(Object value) {
        if (value instanceof TimeDuration) {
            value = ((TimeDuration) value).asTemporalAmount();
        }
        //TODO complete
        return value;
    }
}
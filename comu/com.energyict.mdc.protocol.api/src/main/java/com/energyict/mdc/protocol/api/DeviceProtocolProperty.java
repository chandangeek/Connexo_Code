package com.energyict.mdc.protocol.api;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;

/**
 * Provides the names of commonly used properties
 * of {@link DeviceProtocol}s.
 * Note that those properties could be general,
 * related to a protocol dialect or related to
 * the connection that is established
 * to talk to the actual Device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-01-15 (16:23)
 */
public enum DeviceProtocolProperty {

    CALL_HOME_ID("callHomeId", "CALLHOMEID"),
    DEVICE_TIME_ZONE("deviceTimeZone", "DEVICETIMEZONE"),
    PHONE_NUMBER("phoneNumber", "PHONENUMBER");

    DeviceProtocolProperty(String javaName, String databaseName) {
        this.javaName = javaName;
        this.databaseName = databaseName;
    }

    private final String javaName;
    private final String databaseName;

    public String javaFieldName() {
        return javaName;
    }

    public String databaseColumnName() {
        return databaseName;
    }

    public PropertySpec propertySpec(PropertySpecService propertySpecService, boolean required) {
        return propertySpecService.basicPropertySpec(this.javaFieldName(), required, new StringFactory());
    }

}
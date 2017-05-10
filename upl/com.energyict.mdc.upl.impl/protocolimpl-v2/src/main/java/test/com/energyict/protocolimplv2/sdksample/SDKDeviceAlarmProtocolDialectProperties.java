/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Arrays;
import java.util.List;

public class SDKDeviceAlarmProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    public static final String RAISE_ON_EVENT_PROPERTY_NAME = "raiseOnEventPropertyName";
    public static final String CLEARING_EVENT_PROPERTY_NAME = "clearingEventPropertyName";

    public static final String RAISE_ON_EVENT_MASK = "*.12.*.257";
    public static final String CLEARING_EVENT_MASK = "*.12.*.219";

    public SDKDeviceAlarmProtocolDialectProperties(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.SDK_DEVICE_ALARM_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "SDK dialect for device alarm testing";
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.getAlarmRaiseOnEventPropertyNamePropertySpec(),
                this.getAlarmClearEventPropertyNamePropertySpec()
        );
    }


    private PropertySpec getAlarmRaiseOnEventPropertyNamePropertySpec() {
        return this.alarmEventsPropertySpec(RAISE_ON_EVENT_PROPERTY_NAME, RAISE_ON_EVENT_MASK);
    }

    private PropertySpec getAlarmClearEventPropertyNamePropertySpec() {
        return this.alarmEventsPropertySpec(CLEARING_EVENT_PROPERTY_NAME, CLEARING_EVENT_MASK);
    }

    private PropertySpec alarmEventsPropertySpec(String propName, String propValue) {
        return propertySpecService
                .stringSpec()
                .named(propName, propName)
                .describedAs("Description for " + propName)
                .addValues(propValue)
                .finish();
    }
}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.properties.nls.PropertyTranslationKeys;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Collections;
import java.util.List;

public class SDKDeviceAlarmProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    static final String DEVICE_ALARM_EVENT_TYPE_PROPERTY_NAME = "deviceAlarmEventType";

    static final String DEFAULT_EVENT_TYPE_VALUE = EndDeviceEventTypeMapping.OTHER.getEventType().getCode();

    public SDKDeviceAlarmProtocolDialectProperties(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.SDK_SAMPLE_DEVICE_ALARM_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "SDK dialect for device alarm testing";
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Collections.singletonList(
                UPLPropertySpecFactory.specBuilder(DEVICE_ALARM_EVENT_TYPE_PROPERTY_NAME, false, PropertyTranslationKeys.SDKSAMPLE_DEVICE_ALARM_EVENT_TYPE, propertySpecService::stringSpec).finish());
    }
}

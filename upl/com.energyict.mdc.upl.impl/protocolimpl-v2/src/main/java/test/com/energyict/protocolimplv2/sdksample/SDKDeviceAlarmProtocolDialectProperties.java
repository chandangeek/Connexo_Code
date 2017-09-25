/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.properties.nls.PropertyTranslationKeys;
import com.energyict.protocolimplv2.DeviceProtocolDialectTranslationKeys;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Collections;
import java.util.List;

public class SDKDeviceAlarmProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    static final String DEVICE_ALARM_EVENT_TYPE_PROPERTY_NAME = "deviceAlarmEventType";

    static final String DEFAULT_EVENT_TYPE_VALUE = EndDeviceEventTypeMapping.OTHER.getEventType().getCode();

    public SDKDeviceAlarmProtocolDialectProperties(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    public SDKDeviceAlarmProtocolDialectProperties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectTranslationKeys.SDK_SAMPLE_DEVICE_ALARM_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return getThesaurus().getFormat(DeviceProtocolDialectTranslationKeys.SDK_SAMPLE_DEVICE_ALARM_DIALECT_NAME).format();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Collections.singletonList(
                UPLPropertySpecFactory.specBuilder(DEVICE_ALARM_EVENT_TYPE_PROPERTY_NAME, false, PropertyTranslationKeys.SDKSAMPLE_DEVICE_ALARM_EVENT_TYPE, getPropertySpecService()::stringSpec).finish());
    }
}

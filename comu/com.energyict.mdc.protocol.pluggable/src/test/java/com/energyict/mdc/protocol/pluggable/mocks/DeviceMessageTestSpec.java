/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.mocks;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DeviceMessageTestSpec implements DeviceMessageSpec {

    public static final String ACTIVATIONDATE_PROPERTY_SPEC_NAME = "testMessageSpec.activationdate";
    public static final String SIMPLE_STRING_PROPERTY_SPEC_NAME = "testMessageSpec.simpleString";
    public static final String SIMPLE_BIGDECIMAL_PROPERTY_SPEC_NAME = "testMessageSpec.simpleBigDecimal";

    private static final DeviceMessageCategory CATEGORY = DeviceMessageTestCategories.FIRST_TEST_CATEGORY;

    private List<PropertySpec> deviceMessagePropertySpecs;
    private String name;

    public static List<DeviceMessageSpec> allTestSpecs (PropertySpecService propertySpecService) {
        List<DeviceMessageSpec> allTestSpecs = new ArrayList<>(3);
        allTestSpecs.add(allSimpleSpecs());
        allTestSpecs.add(extendedSpecs(propertySpecService));
        allTestSpecs.add(noSpecs());
        return allTestSpecs;
    }

    public static DeviceMessageTestSpec allSimpleSpecs() {
        PropertySpecService propertySpecService = new PropertySpecServiceImpl();
        return new DeviceMessageTestSpec(
                        "TEST_SPEC_WITH_SIMPLE_SPECS",
                        propertySpecService
                                .bigDecimalSpec()
                                .named(SIMPLE_BIGDECIMAL_PROPERTY_SPEC_NAME, SIMPLE_BIGDECIMAL_PROPERTY_SPEC_NAME)
                                .describedAs(null)
                                .markRequired()
                                .finish(),
                        propertySpecService
                                .stringSpec()
                                .named(SIMPLE_STRING_PROPERTY_SPEC_NAME, SIMPLE_STRING_PROPERTY_SPEC_NAME)
                                .describedAs(null)
                                .markRequired()
                                .finish());
    };

    public static DeviceMessageTestSpec extendedSpecs(PropertySpecService propertySpecService) {
        return new DeviceMessageTestSpec(
                "TEST_SPEC_WITH_EXTENDED_SPECS",
                propertySpecService
                        .specForValuesOf(new DateAndTimeFactory())
                        .named(ACTIVATIONDATE_PROPERTY_SPEC_NAME, ACTIVATIONDATE_PROPERTY_SPEC_NAME)
                        .describedAs(null)
                        .markRequired()
                        .finish());
    }
    public static DeviceMessageTestSpec noSpecs() {
        return new DeviceMessageTestSpec("TEST_SPEC_WITHOUT_SPECS");
    }

    private DeviceMessageTestSpec(String name, PropertySpec... deviceMessagePropertySpecs) {
        super();
        this.name = name;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public DeviceMessageId getId() {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.deviceMessagePropertySpecs;
    }

    @Override
    public String toString() {
        return "DeviceMessageTestSpec." + this.getName();
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeviceMessageTestSpec) {
            DeviceMessageTestSpec other = (DeviceMessageTestSpec) obj;
            return this.getName().equals(other.getName());
        }
        else {
            return false;
        }
    }

}
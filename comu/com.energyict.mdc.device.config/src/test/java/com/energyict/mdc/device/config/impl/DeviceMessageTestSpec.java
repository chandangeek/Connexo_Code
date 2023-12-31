package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.TemporalAmountValueFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test enum implementing DeviceMessageSpec
 * <p>
 *
 * Date: 8/02/13
 * Time: 15:16
 */
public enum DeviceMessageTestSpec implements DeviceMessageSpec {

    TEST_SPEC_WITH_SIMPLE_SPECS(
            mockPropertySpec("testMessageSpec.simpleBigDecimal", new BigDecimalFactory()),
            mockPropertySpec("testMessageSpec.simpleString", new StringFactory())),
    TEST_SPEC_WITH_EXTENDED_SPECS(
            mockPropertySpec("testMessageSpec.activationdate", new TemporalAmountValueFactory())),
    TEST_SPEC_WITHOUT_SPECS;

    private static final DeviceMessageCategory activityCalendarCategory = DeviceMessageTestCategories.FIRST_TEST_CATEGORY;

    private List<PropertySpec> deviceMessagePropertySpecs;

    DeviceMessageTestSpec(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    private static PropertySpec mockPropertySpec(String name, ValueFactory valueFactory) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getDescription()).thenReturn(name);
        when(propertySpec.getName()).thenReturn(name);
        when(propertySpec.getDisplayName()).thenReturn(name);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        when(propertySpec.isRequired()).thenReturn(true);
        return propertySpec;
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return activityCalendarCategory;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public DeviceMessageId getId() {
        return DeviceMessageId.from(this.ordinal() + 1);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return deviceMessagePropertySpecs;
    }

    @Override
    public Optional<PropertySpec> getPropertySpec(String name) {
        for (PropertySpec securityProperty : getPropertySpecs()) {
            if (securityProperty.getName().equals(name)) {
                return Optional.of(securityProperty);
            }
        }
        return Optional.empty();
    }
}
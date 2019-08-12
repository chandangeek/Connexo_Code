package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.TemporalAmountValueFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.dynamic.DateAndTimeFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Test enum implementing DeviceMessageSpec
 * <p>
 *
 * Date: 8/02/13
 * Time: 15:16
 */
public enum DeviceMessageTestSpec implements DeviceMessageSpec {

    CONTACTOR_OPEN_WITH_OUTPUT(
            mockPropertySpec("ContactorDeviceMessage.digitalOutput", new BigDecimalFactory())),
    SET_DISPLAY_MESSAGE_WITH_OPTIONS(
            mockPropertySpec("DisplayDeviceMessage.displaymessage", new StringFactory()),
            mockPropertySpec("DisplayMessage.timeduration", new BigDecimalFactory()),
            mockPropertySpec("DisplayMessage.activationdate", new DateAndTimeFactory())
    ),
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
        BasicPropertySpec propertySpec = spy(new BasicPropertySpec(valueFactory));
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
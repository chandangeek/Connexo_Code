package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.dynamic.TemporalAmountValueFactory;
import com.energyict.mdc.dynamic.impl.PasswordFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test enum implementing DeviceMessageSpec
 * <p>
 * Copyrights EnergyICT
 * Date: 8/02/13
 * Time: 15:16
 */
public enum DeviceMessageTestSpec implements DeviceMessageSpec {

    TEST_SPEC_WITH_SIMPLE_SPECS(
            1,
            mockPropertySpec("testMessageSpec.simpleBigDecimal", new BigDecimalFactory()),
            mockPropertySpec("testMessageSpec.simpleString", new StringFactory())),
    TEST_SPEC_WITH_EXTENDED_SPECS(
            2,
            mockPropertySpec("testMessageSpec.codetable", new PasswordFactory(mock(DataVaultService.class))),
            mockPropertySpec("testMessageSpec.activationdate", new TemporalAmountValueFactory())),
    TEST_SPEC_WITHOUT_SPECS(3),
    CONTACTOR_OPEN(DeviceMessageId.CONTACTOR_OPEN.dbValue()),
    CONTACTOR_CLOSE(DeviceMessageId.CONTACTOR_CLOSE.dbValue()),
    CONTACTOR_OPEN_WITH_OUTPUT(DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT.dbValue()),
    CONTACTOR_CLOSE_WITH_OUTPUT(
            DeviceMessageId.CONTACTOR_CLOSE_WITH_OUTPUT.dbValue(),
            mockPropertySpec("ContactorDeviceMessage.digitalOutput", new BigDecimalFactory())
    );

    private static final DeviceMessageCategory activityCalendarCategory = DeviceMessageTestCategories.FIRST_TEST_CATEGORY;

    private List<PropertySpec> deviceMessagePropertySpecs;
    private long id;

    DeviceMessageTestSpec(long id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
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
        return DeviceMessageId.havingId(id);
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
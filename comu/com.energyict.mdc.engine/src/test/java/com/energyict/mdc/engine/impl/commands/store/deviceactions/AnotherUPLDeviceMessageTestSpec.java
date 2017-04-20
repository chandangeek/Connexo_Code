/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.TemporalAmountValueFactory;
import com.energyict.mdc.dynamic.impl.PasswordFactory;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLValueFactoryAdapter;
import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.ValueFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public enum AnotherUPLDeviceMessageTestSpec implements DeviceMessageSpec {

    TEST_SPEC_WITH_SIMPLE_SPECS(
            mockPropertySpec("testMessageSpec.simpleBigDecimal", ConnexoToUPLValueFactoryAdapter.adapt(new BigDecimalFactory())),
            mockPropertySpec("testMessageSpec.simpleString", ConnexoToUPLValueFactoryAdapter.adapt(new StringFactory()))),
    TEST_SPEC_WITH_EXTENDED_SPECS(
            mockPropertySpec("testMessageSpec.codetable", ConnexoToUPLValueFactoryAdapter.adapt(new PasswordFactory(mock(DataVaultService.class)))),
            mockPropertySpec("testMessageSpec.activationdate", ConnexoToUPLValueFactoryAdapter.adapt(new TemporalAmountValueFactory()))),
    TEST_SPEC_WITHOUT_SPECS;

    private static final DeviceMessageCategory activityCalendarCategory = UPLDeviceMessageTestCategories.SECOND_TEST_CATEGORY;

    private List<PropertySpec> deviceMessagePropertySpecs;

    AnotherUPLDeviceMessageTestSpec(PropertySpec... deviceMessagePropertySpecs) {
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
    public TranslationKey getNameTranslationKey() {
        TranslationKey translationKey = mock(TranslationKey.class);
        when(translationKey.getKey()).thenReturn(name());
        when(translationKey.getDefaultFormat()).thenReturn(name());
        return translationKey;
    }

    @Override
    public long getId() {
        return this.ordinal() + 1;
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
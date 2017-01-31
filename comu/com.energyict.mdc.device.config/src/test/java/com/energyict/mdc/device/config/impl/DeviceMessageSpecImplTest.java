/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;
import com.energyict.mdc.device.config.DeviceMessageFile;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceMessageSpecImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-13 (11:18)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceMessageSpecImplTest {

    private static final String EXPECTED_SPEC_NAME = "DeviceMessageSpecImplTest";
    private static final DeviceMessageId EXPECTED_DEVICE_MESSAGE_ID = DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE;

    @Mock
    private TimeService timeService;
    @Mock
    private OrmService ormService;
    @Mock
    private BeanService beanService;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceMessageSpec deviceMessageSpec;
    @Mock
    private DeviceMessageCategory category;

    private PropertySpecService propertySpecService;

    @Before
    public void initializeMocks() {
        this.propertySpecService = new PropertySpecServiceImpl(this.timeService, this.ormService, this.beanService);
        when(this.deviceMessageSpec.getId()).thenReturn(EXPECTED_DEVICE_MESSAGE_ID);
        when(this.deviceMessageSpec.getCategory()).thenReturn(this.category);
        when(this.deviceMessageSpec.getName()).thenReturn(EXPECTED_SPEC_NAME);
    }

    @Test
    public void noSpecsAtAll() {
        when(this.deviceMessageSpec.getPropertySpecs()).thenReturn(Collections.emptyList());

        // Business method
        DeviceMessageSpecImpl deviceMessageSpec = this.getTestInstance();

        // Asserts
        assertThat(deviceMessageSpec.getId()).isEqualTo(EXPECTED_DEVICE_MESSAGE_ID);
        assertThat(deviceMessageSpec.getCategory()).isEqualTo(this.category);
        assertThat(deviceMessageSpec.getName()).isEqualTo(EXPECTED_SPEC_NAME);
        assertThat(deviceMessageSpec.getPropertySpecs()).isEmpty();
    }

    @Test
    public void onlyStringSpecs() {
        PropertySpec string1 = this.propertySpecService
                .stringSpec()
                .named("string1", "One")
                .describedAs("Description for string1")
                .finish();
        PropertySpec string2 = this.propertySpecService
                .stringSpec()
                .named("string2", "Two")
                .describedAs("Description for string2")
                .finish();
        when(this.deviceMessageSpec.getPropertySpecs()).thenReturn(Arrays.asList(string1, string2));

        // Business method
        DeviceMessageSpecImpl deviceMessageSpec = this.getTestInstance();

        // Asserts
        assertThat(deviceMessageSpec.getId()).isEqualTo(EXPECTED_DEVICE_MESSAGE_ID);
        assertThat(deviceMessageSpec.getCategory()).isEqualTo(this.category);
        assertThat(deviceMessageSpec.getName()).isEqualTo(EXPECTED_SPEC_NAME);
        List<PropertySpec> propertySpecs = deviceMessageSpec.getPropertySpecs();
        assertThat(propertySpecs).hasSize(2);
        assertThat(propertySpecs.get(0)).isSameAs(string1);
        assertThat(propertySpecs.get(1)).isSameAs(string2);
    }

    @Test
    public void oneDeviceMessageFileSpecWithPossibleValues() {
        DeviceType otherDeviceType = mock(DeviceType.class);
        DeviceMessageFile file1 = mock(DeviceMessageFile.class);
        when(file1.getName()).thenReturn("file1");
        DeviceMessageFile file2 = mock(DeviceMessageFile.class);
        when(file2.getName()).thenReturn("file2");
        DeviceMessageFile file3 = mock(DeviceMessageFile.class);
        when(file3.getName()).thenReturn("file3");
        when(otherDeviceType.getDeviceMessageFiles()).thenReturn(Collections.singletonList(file1));
        when(this.deviceType.getDeviceMessageFiles()).thenReturn(Arrays.asList(file2, file3));
        PropertySpec propertySpec = this.propertySpecService
                .referenceSpec(DeviceMessageFile.class)
                .named("propertySpec", "PropertySpec")
                .describedAs("Description for propertySpec")
                .addValues(file1, file2, file3)
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .markRequired()
                .markEditable()
                .finish();
        when(this.deviceMessageSpec.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));

        // Business method
        DeviceMessageSpecImpl deviceMessageSpec = this.getTestInstance();

        // Asserts
        assertThat(deviceMessageSpec.getId()).isEqualTo(EXPECTED_DEVICE_MESSAGE_ID);
        assertThat(deviceMessageSpec.getCategory()).isEqualTo(this.category);
        assertThat(deviceMessageSpec.getName()).isEqualTo(EXPECTED_SPEC_NAME);
        List<PropertySpec> propertySpecs = deviceMessageSpec.getPropertySpecs();
        assertThat(propertySpecs).hasSize(1);
        PropertySpec copyOfPropertySpec = propertySpecs.get(0);
        assertThat(copyOfPropertySpec).isNotSameAs(propertySpec);
        assertThat(copyOfPropertySpec.getName()).isEqualTo("propertySpec");
        assertThat(copyOfPropertySpec.getDisplayName()).isEqualTo("PropertySpec");
        assertThat(copyOfPropertySpec.getDescription()).isEqualTo("Description for propertySpec");
        assertThat(copyOfPropertySpec.isRequired()).isTrue();
        assertThat(copyOfPropertySpec.supportsMultiValues()).isFalse();
        PropertySpecPossibleValues possibleValues = copyOfPropertySpec.getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.getSelectionMode()).isEqualTo(PropertySelectionMode.COMBOBOX);
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.isEditable()).isTrue();
        assertThat(possibleValues.getAllValues()).containsOnly(file2, file3);
        assertThat(possibleValues.getDefault()).isNull();
    }

    private DeviceMessageSpecImpl getTestInstance() {
        return new DeviceMessageSpecImpl(
                this.deviceType,
                this.deviceMessageSpec,
                this.propertySpecService);
    }

}
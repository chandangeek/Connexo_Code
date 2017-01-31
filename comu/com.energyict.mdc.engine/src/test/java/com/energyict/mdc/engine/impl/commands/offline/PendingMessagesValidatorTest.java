/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageAttributes;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link PendingMessagesValidator} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class PendingMessagesValidatorTest {

    private static final String CALENDAR_NAME = "Whatever";

    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private Device device;
    @Mock
    private DeviceMessage<Device> deviceMessage;
    @Mock
    private Calendar calendar;

    @Before
    public void initializeMocks() {
        AllowedCalendar allowedCalendar = mock(AllowedCalendar.class);
        when(allowedCalendar.getCalendar()).thenReturn(Optional.of(this.calendar));
        when(allowedCalendar.getName()).thenReturn(CALENDAR_NAME);
        when(this.calendar.getName()).thenReturn(CALENDAR_NAME);
        when(this.deviceType.getAllowedCalendars()).thenReturn(Collections.singletonList(allowedCalendar));
        when(this.deviceConfiguration.getDeviceType()).thenReturn(this.deviceType);
        when(this.device.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.device.getDeviceType()).thenReturn(this.deviceType);
    }

    @Test
    public void messageWithoutCalendarAttributes() {
        when(this.deviceMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN);

        // Business method
        boolean stillValid = this.getTestInstance().isStillValid(this.deviceMessage);

        // Asserts
        assertThat(stillValid).isTrue();
    }

    @Test
    public void messageWithAllowedCalendarAttributeValue() {
        when(this.deviceMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.ACTIVITY_CALENDER_SEND);
        DeviceMessageAttribute calendarNameAttribute = mock(DeviceMessageAttribute.class);
        when(calendarNameAttribute.getDeviceMessage()).thenReturn(this.deviceMessage);
        when(calendarNameAttribute.getName()).thenReturn(DeviceMessageAttributes.activityCalendarNameAttributeName.getDefaultFormat());
        when(calendarNameAttribute.getValue()).thenReturn("messageWithAllowedCalendarAttributeValue");
        PropertySpec calendarNamePropertySpec = this.stringPropertySpec(DeviceMessageAttributes.activityCalendarNameAttributeName.getDefaultFormat());
        when(calendarNameAttribute.getSpecification()).thenReturn(calendarNamePropertySpec);
        DeviceMessageAttribute calendarAttribute = mock(DeviceMessageAttribute.class);
        when(calendarAttribute.getDeviceMessage()).thenReturn(this.deviceMessage);
        when(calendarAttribute.getName()).thenReturn(DeviceMessageAttributes.activityCalendarAttributeName.getDefaultFormat());
        when(calendarAttribute.getValue()).thenReturn(this.calendar);
        PropertySpec calendarPropertySpec = this.calendarPropertySpec(DeviceMessageAttributes.activityCalendarAttributeName.getDefaultFormat());
        when(calendarAttribute.getSpecification()).thenReturn(calendarPropertySpec);
        when(this.deviceMessage.getAttributes()).thenReturn(Arrays.asList(calendarNameAttribute, calendarAttribute));
        DeviceMessageSpec messageSpec = mock(DeviceMessageSpec.class);
        when(messageSpec.getId()).thenReturn(DeviceMessageId.ACTIVITY_CALENDER_SEND);
        when(messageSpec.getPropertySpecs()).thenReturn(Arrays.asList(calendarNamePropertySpec, calendarPropertySpec));
        when(this.deviceMessage.getSpecification()).thenReturn(messageSpec);

        // Business method
        boolean stillValid = this.getTestInstance().isStillValid(this.deviceMessage);

        // Asserts
        assertThat(stillValid).isTrue();
    }

    @Test
    public void messageWithoutAllowedCalendarAttributeValue() {
        when(this.deviceMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.ACTIVITY_CALENDER_SEND);
        DeviceMessageAttribute calendarNameAttribute = mock(DeviceMessageAttribute.class);
        when(calendarNameAttribute.getDeviceMessage()).thenReturn(this.deviceMessage);
        when(calendarNameAttribute.getName()).thenReturn(DeviceMessageAttributes.activityCalendarNameAttributeName.getDefaultFormat());
        when(calendarNameAttribute.getValue()).thenReturn("messageWithAllowedCalendarAttributeValue");
        PropertySpec calendarNamePropertySpec = this.stringPropertySpec(DeviceMessageAttributes.activityCalendarNameAttributeName.getDefaultFormat());
        when(calendarNameAttribute.getSpecification()).thenReturn(calendarNamePropertySpec);
        DeviceMessageAttribute calendarAttribute = mock(DeviceMessageAttribute.class);
        when(calendarAttribute.getDeviceMessage()).thenReturn(this.deviceMessage);
        when(calendarAttribute.getName()).thenReturn(DeviceMessageAttributes.activityCalendarAttributeName.getDefaultFormat());
        when(calendarAttribute.getValue()).thenReturn(this.calendar);
        PropertySpec calendarPropertySpec = this.calendarPropertySpec(DeviceMessageAttributes.activityCalendarAttributeName.getDefaultFormat());
        when(calendarAttribute.getSpecification()).thenReturn(calendarPropertySpec);
        when(this.deviceMessage.getAttributes()).thenReturn(Arrays.asList(calendarNameAttribute, calendarAttribute));
        DeviceMessageSpec messageSpec = mock(DeviceMessageSpec.class);
        when(messageSpec.getId()).thenReturn(DeviceMessageId.ACTIVITY_CALENDER_SEND);
        when(messageSpec.getPropertySpecs()).thenReturn(Arrays.asList(calendarNamePropertySpec, calendarPropertySpec));
        when(this.deviceMessage.getSpecification()).thenReturn(messageSpec);
        when(this.deviceType.getAllowedCalendars()).thenReturn(Collections.emptyList());

        // Business method
        boolean stillValid = this.getTestInstance().isStillValid(this.deviceMessage);

        // Asserts
        assertThat(stillValid).isFalse();
    }

    @Test
    public void failingCalendarNames() {
        when(this.deviceMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.ACTIVITY_CALENDER_SEND);
        DeviceMessageAttribute calendarNameAttribute = mock(DeviceMessageAttribute.class);
        when(calendarNameAttribute.getDeviceMessage()).thenReturn(this.deviceMessage);
        when(calendarNameAttribute.getName()).thenReturn(DeviceMessageAttributes.activityCalendarNameAttributeName.getDefaultFormat());
        when(calendarNameAttribute.getValue()).thenReturn("messageWithAllowedCalendarAttributeValue");
        PropertySpec calendarNamePropertySpec = this.stringPropertySpec(DeviceMessageAttributes.activityCalendarNameAttributeName.getDefaultFormat());
        when(calendarNameAttribute.getSpecification()).thenReturn(calendarNamePropertySpec);
        DeviceMessageAttribute calendarAttribute = mock(DeviceMessageAttribute.class);
        when(calendarAttribute.getDeviceMessage()).thenReturn(this.deviceMessage);
        when(calendarAttribute.getName()).thenReturn(DeviceMessageAttributes.activityCalendarAttributeName.getDefaultFormat());
        when(calendarAttribute.getValue()).thenReturn(this.calendar);
        PropertySpec calendarPropertySpec = this.calendarPropertySpec(DeviceMessageAttributes.activityCalendarAttributeName.getDefaultFormat());
        when(calendarAttribute.getSpecification()).thenReturn(calendarPropertySpec);
        when(this.deviceMessage.getAttributes()).thenReturn(Arrays.asList(calendarNameAttribute, calendarAttribute));
        DeviceMessageSpec messageSpec = mock(DeviceMessageSpec.class);
        when(messageSpec.getId()).thenReturn(DeviceMessageId.ACTIVITY_CALENDER_SEND);
        when(messageSpec.getPropertySpecs()).thenReturn(Arrays.asList(calendarNamePropertySpec, calendarPropertySpec));
        when(this.deviceMessage.getSpecification()).thenReturn(messageSpec);
        when(this.deviceType.getAllowedCalendars()).thenReturn(Collections.emptyList());

        // Business method
        String failingCalendarNames = this.getTestInstance().failingCalendarNames(this.deviceMessage);

        // Asserts
        assertThat(failingCalendarNames).isEqualTo(CALENDAR_NAME);
    }

    private PropertySpec stringPropertySpec(String name) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        PropertySpecPossibleValues noPossibleValues = this.noPossibleValues();
        when(propertySpec.getPossibleValues()).thenReturn(noPossibleValues);
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        when(propertySpec.isReference()).thenReturn(false);
        when(propertySpec.isRequired()).thenReturn(true);
        return propertySpec;
    }

    private PropertySpec calendarPropertySpec(String name) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        PropertySpecPossibleValues possibleValues = toPossibleValue(this.calendar);
        when(propertySpec.getPossibleValues()).thenReturn(possibleValues);
        ValueFactory<Calendar> valueFactory = this.calendarValueFactory();
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        when(propertySpec.isReference()).thenReturn(true);
        when(propertySpec.isRequired()).thenReturn(true);
        return propertySpec;
    }

    private PropertySpecPossibleValues noPossibleValues() {
        PropertySpecPossibleValues none = mock(PropertySpecPossibleValues.class);
        when(none.getAllValues()).thenReturn(Collections.emptyList());
        when(none.getDefault()).thenReturn(null);
        when(none.getSelectionMode()).thenReturn(PropertySelectionMode.COMBOBOX);
        when(none.isEditable()).thenReturn(true);
        when(none.isExhaustive()).thenReturn(false);
        return none;
    }

    private PropertySpecPossibleValues toPossibleValue(Calendar calendar) {
        PropertySpecPossibleValues singleCalendar = mock(PropertySpecPossibleValues.class);
        when(singleCalendar.getAllValues()).thenReturn(Collections.singletonList(calendar));
        when(singleCalendar.getDefault()).thenReturn(calendar);
        when(singleCalendar.getSelectionMode()).thenReturn(PropertySelectionMode.COMBOBOX);
        when(singleCalendar.isEditable()).thenReturn(true);
        when(singleCalendar.isExhaustive()).thenReturn(true);
        return singleCalendar;
    }

    @SuppressWarnings("unchecked")
    private ValueFactory<Calendar> calendarValueFactory() {
        ValueFactory<Calendar> valueFactory = mock(ValueFactory.class);
        when(valueFactory.isReference()).thenReturn(true);
        when(valueFactory.getValueType()).thenReturn(Calendar.class);
        return valueFactory;
    }

    private PendingMessagesValidator getTestInstance() {
        return new PendingMessagesValidator(this.device);
    }

}
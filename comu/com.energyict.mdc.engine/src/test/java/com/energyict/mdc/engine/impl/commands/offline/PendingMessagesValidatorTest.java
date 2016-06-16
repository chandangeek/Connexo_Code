package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
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
        when(calendarAttribute.getValue()).thenReturn("messageWithAllowedCalendarAttributeValue");
        PropertySpec calendarPropertySpec = this.calendarPropertySpec(DeviceMessageAttributes.activityCalendarAttributeName.getDefaultFormat());
        when(calendarAttribute.getSpecification()).thenReturn(calendarPropertySpec);
        when(this.deviceMessage.getAttributes()).thenReturn(Arrays.asList(calendarNameAttribute, calendarAttribute));
        DeviceMessageSpec messageSpec = mock(DeviceMessageSpec.class);
        when(messageSpec.getId()).thenReturn(DeviceMessageId.ACTIVITY_CALENDER_SEND);
        when(messageSpec.getPropertySpecs()).thenReturn(Arrays.asList(calendarNamePropertySpec, calendarPropertySpec));
        when(this.deviceMessage.getSpecification()).thenReturn(messageSpec);
        when(device.getDeviceType())

        // Business method
        boolean stillValid = this.getTestInstance().isStillValid(this.deviceMessage);

        // Asserts
        assertThat(stillValid).isTrue();
    }

    private PropertySpec stringPropertySpec(String name) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        when(propertySpec.getPossibleValues()).thenReturn(noPossibleValues());
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        when(propertySpec.isReference()).thenReturn(false);
        when(propertySpec.isRequired()).thenReturn(true);
        return propertySpec;
    }

    private PropertySpec calendarPropertySpec(String name) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        when(propertySpec.getPossibleValues()).thenReturn(toPossibleValue(this.calendar));
        when(propertySpec.getValueFactory()).thenReturn(this.calendarValueFactory());
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
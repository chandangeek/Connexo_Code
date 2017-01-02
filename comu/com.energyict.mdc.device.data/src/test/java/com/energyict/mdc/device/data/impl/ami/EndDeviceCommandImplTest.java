package com.energyict.mdc.device.data.impl.ami;

import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.rest.FieldValidationException;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.ami.commands.ArmRemoteSwitchCommand;
import com.energyict.mdc.device.data.impl.ami.commands.OpenRemoteSwitchCommand;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.TrackingCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Date;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EndDeviceCommandImplTest {

    private static final TranslationKey CONTACTOR_ACTIVATION_DATE_ATTREIBUTE_TRANSLATION_KEY = new TranslationKey() {
        @Override
        public String getKey() {
            return DeviceMessageConstants.contactorActivationDateAttributeName;
        }

        @Override
        public String getDefaultFormat() {
            return "Activation date";
        }
    };
    private static final String END_DEVICE_MRID = "endDeviceMRID";
    private static final long SERVICE_CALL_ID = 1;
    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();
    @Mock
    private EndDevice endDevice;
    @Mock
    private Device device;
    @Mock
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    @Mock
    private TimeService timeService;
    @Mock
    private OrmService ormService;
    @Mock
    private BeanService beanService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private EndDeviceControlType endDeviceControlType;
    @Mock
    private Thesaurus thesaurus;

    private List<DeviceMessageId> deviceMessageIds = Collections.singletonList(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
    private EndDeviceCommandImpl endDeviceCommand;
    private List<PropertySpec> propertySpecs;
    private PropertySpecService propertySpecService;

    @Before
    public void setup() {
        when(thesaurus.getFormat(any(TranslationKey.class))).thenAnswer(invocationOnMock -> {
            TranslationKey translationKey = (TranslationKey) invocationOnMock.getArguments()[0];
            return new NlsMessageFormat() {
                @Override
                public String format(Object... args) {
                    return MessageFormat.format(translationKey.getDefaultFormat(), args);
                }

                @Override
                public String format(Locale locale, Object... args) {
                    return MessageFormat.format(translationKey.getDefaultFormat(), args);
                }
            };
        });
        when(thesaurus.getFormat(any(MessageSeed.class))).thenAnswer(invocationOnMock -> {
            MessageSeed messageSeed = (MessageSeed) invocationOnMock.getArguments()[0];
            return new NlsMessageFormat() {
                @Override
                public String format(Object... args) {
                    return MessageFormat.format(messageSeed.getDefaultFormat(), args);
                }

                @Override
                public String format(Locale locale, Object... args) {
                    return MessageFormat.format(messageSeed.getDefaultFormat(), args);
                }
            };
        });
        when(endDevice.getMRID()).thenReturn(END_DEVICE_MRID);
        when(deviceService.findByUniqueMrid(END_DEVICE_MRID)).thenReturn(Optional.of(device));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);

        List<com.energyict.mdc.upl.messages.DeviceMessageSpec> deviceMessageIds = new ArrayList<>();
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec1 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec1.getId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.dbValue());
        deviceMessageIds.add(deviceMessageSpec1);

        when(deviceProtocol.getSupportedMessages()).thenReturn(deviceMessageIds);
        DeviceProtocolPluggableClass protocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(protocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(device.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(protocolPluggableClass));

        this.propertySpecService = new PropertySpecServiceImpl(this.timeService, this.ormService, this.beanService);
        DeviceMessageSpec message = mock(DeviceMessageSpec.class);
        when(deviceMessageSpecificationService.findMessageSpecById(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.dbValue())).thenReturn(Optional.of(message));
        PropertySpec string1 = this.propertySpecService
                .stringSpec()
                .named("string1", "One")
                .describedAs("Description for string1")
                .setDefaultValue("Value1")
                .finish();
        PropertySpec string2 = this.propertySpecService
                .stringSpec()
                .named("string2", "Two")
                .describedAs("Description for string2")
                .setDefaultValue("Value2")
                .finish();
        when(message.getPropertySpecs()).thenReturn(propertySpecs = Arrays.asList(string1, string2));
        endDeviceCommand = new OpenRemoteSwitchCommand(endDevice, endDeviceControlType, deviceMessageIds, deviceService, deviceMessageSpecificationService, thesaurus);
    }

    @Test
    public void getCommandArgumentSpecs() {
        // Business method
        List<PropertySpec> commandArgsSpecs = endDeviceCommand.getCommandArgumentSpecs();

        // Asserts
        assertEquals(2, commandArgsSpecs.size());
        assertCommandArgsSpecs(commandArgsSpecs, propertySpecs);
    }

    @Test
    public void getCommandArgumentSpecsOnlyContainsUniqueSpecs() {
        DeviceMessageSpec otherMessage = mock(DeviceMessageSpec.class);
        when(deviceMessageSpecificationService.findMessageSpecById(DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE.dbValue())).thenReturn(Optional.of(otherMessage));
        PropertySpec string1 = this.propertySpecService
                .stringSpec()
                .named("string1", "One")
                .describedAs("Description for string1")
                .setDefaultValue("Value1")
                .finish();
        PropertySpec string3 = this.propertySpecService
                .stringSpec()
                .named("string3", "Three")
                .describedAs("Description for string3")
                .setDefaultValue("Value3")
                .finish();
        when(otherMessage.getPropertySpecs()).thenReturn(propertySpecs = Arrays.asList(string1, string3));

        // Business method
        DeviceMessageId[] allDeviceMessageIds = new DeviceMessageId[]{DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE, DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE};
        endDeviceCommand = new ArmRemoteSwitchCommand(endDevice, endDeviceControlType, Arrays.asList(allDeviceMessageIds), deviceService, deviceMessageSpecificationService, thesaurus);
        List<PropertySpec> commandArgsSpecs = endDeviceCommand.getCommandArgumentSpecs();

        // Asserts
        assertEquals(3, commandArgsSpecs.size());
        List<PropertySpec> desiredPropertySpecs = Stream.concat(propertySpecs.stream(), Collections.singletonList(string3).stream()).collect(Collectors.toList());
        assertCommandArgsSpecs(commandArgsSpecs, desiredPropertySpecs);

    }

    private void assertCommandArgsSpecs(List<PropertySpec> commandArgsSpecs, List<PropertySpec> expectedPropertySpecs) {
        expectedPropertySpecs.stream().forEach(spec -> {
            PropertySpec argumentSpec = commandArgsSpecs.stream().filter(argSpec -> argSpec.getName().equals(spec.getName())).findFirst().get();
            assertEquals(spec.getName(), argumentSpec.getName());
            assertEquals(spec.getDisplayName(), argumentSpec.getDisplayName());
            assertEquals(spec.getDescription(), argumentSpec.getDescription());
        });
    }

    @Test
    @Expected(value = FieldValidationException.class, message = "Property spec not found")
    public void setPropertyValueForUnknownPropertySpec() {
        PropertySpec unknownSpec = this.propertySpecService
                .stringSpec()
                .named("unknown", "One")
                .describedAs("Description for unknown")
                .setDefaultValue("unknown")
                .finish();

        //Business method
        endDeviceCommand.setPropertyValue(unknownSpec, "testString");
    }

    @Test
    @Expected(value = FieldValidationException.class, message = "Incorrect type")
    public void setPropertyValueWithInvalidValue() {
        //Business method
        endDeviceCommand.setPropertyValue(propertySpecs.get(0), true);
    }

    @Test
    public void setPropertyValue() {
        //Business method
        PropertySpec propertySpec = propertySpecs.get(0);
        String updatedValue = "updatedValue";
        endDeviceCommand.setPropertyValue(propertySpec, updatedValue);


        // Asserts
        Map<PropertySpec, Object> propertyValueMap = endDeviceCommand.getPropertyValueMap();
        assertEquals(1, propertyValueMap.size());
        assertTrue(propertyValueMap.containsKey(propertySpec));
        assertEquals(updatedValue, propertyValueMap.get(propertySpec));
    }

    @Test
    public void testCreateCorrespondingMultiSenseDeviceMessageHavingActivationDateSpec() throws Exception {
        DeviceMessageSpec messageSpec = mock(DeviceMessageSpec.class);
        when(deviceMessageSpecificationService.findMessageSpecById(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.dbValue())).thenReturn(Optional.of(messageSpec));
        PropertySpec dateTimeSpec = propertySpecService
                .specForValuesOf(new DateAndTimeFactory())
                .named(CONTACTOR_ACTIVATION_DATE_ATTREIBUTE_TRANSLATION_KEY)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        when(messageSpec.getPropertySpecs()).thenReturn(Collections.singletonList(dateTimeSpec));
        when(endDeviceControlType.getMRID()).thenReturn(EndDeviceControlTypeMapping.OPEN_REMOTE_SWITCH.getEndDeviceControlTypeMRID());
        endDeviceCommand = new OpenRemoteSwitchCommand(endDevice, endDeviceControlType, deviceMessageIds, deviceService, deviceMessageSpecificationService, thesaurus);

        ServiceCall serviceCall = mock(ServiceCall.class);
        when(serviceCall.getId()).thenReturn(SERVICE_CALL_ID);
        Instant releaseDate = Instant.ofEpochSecond(1465941600);    // 15/06/20165 00:00:00
        endDeviceCommand.setPropertyValue(dateTimeSpec, Date.from(releaseDate));

        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        Device.DeviceMessageBuilder deviceMessageBuilder = mock(Device.DeviceMessageBuilder.class);
        when(deviceMessageBuilder.setTrackingId(anyString())).thenReturn(deviceMessageBuilder);
        when(deviceMessageBuilder.setReleaseDate(anyObject())).thenReturn(deviceMessageBuilder);
        when(deviceMessageBuilder.addProperty(anyString(), anyObject())).thenReturn(deviceMessageBuilder);
        when(deviceMessageBuilder.add()).thenReturn(deviceMessage);
        when(device.newDeviceMessage(any(DeviceMessageId.class), any(TrackingCategory.class))).thenReturn(deviceMessageBuilder);

        // Business method
        List<DeviceMessage> deviceMessages = endDeviceCommand.createCorrespondingMultiSenseDeviceMessages(serviceCall, releaseDate);

        // Asserts
        assertEquals(1, deviceMessages.size());
        ArgumentCaptor<String> trackingIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Instant> releaseDateArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<String> propertyArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Date> valueArgumentCaptor = ArgumentCaptor.forClass(Date.class);
        verify(deviceMessageBuilder).setTrackingId(trackingIdArgumentCaptor.capture());
        verify(deviceMessageBuilder).setReleaseDate(releaseDateArgumentCaptor.capture());
        verify(deviceMessageBuilder).addProperty(propertyArgumentCaptor.capture(), valueArgumentCaptor.capture());

        assertEquals(Long.toString(SERVICE_CALL_ID), trackingIdArgumentCaptor.getValue());
        assertTrue(releaseDateArgumentCaptor.getValue().isAfter(releaseDate));
        assertEquals(dateTimeSpec.getName(), propertyArgumentCaptor.getValue());
        assertEquals(Date.from(releaseDate), valueArgumentCaptor.getValue());
    }

    @Test
    public void testCreateMultipleCorrespondingMultiSenseDeviceMessagesHavingActivationDateSpec() throws Exception {
        DeviceMessageSpec messageSpec = mock(DeviceMessageSpec.class);
        when(deviceMessageSpecificationService.findMessageSpecById(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.dbValue())).thenReturn(Optional.of(messageSpec));
        when(deviceMessageSpecificationService.findMessageSpecById(DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE.dbValue())).thenReturn(Optional.of(messageSpec));
        PropertySpec dateTimeSpec = propertySpecService
                .specForValuesOf(new DateAndTimeFactory())
                .named(CONTACTOR_ACTIVATION_DATE_ATTREIBUTE_TRANSLATION_KEY)
                .fromThesaurus(thesaurus)
                .markRequired()
                .finish();
        when(messageSpec.getPropertySpecs()).thenReturn(Collections.singletonList(dateTimeSpec));
        when(endDeviceControlType.getMRID()).thenReturn(EndDeviceControlTypeMapping.ARM_REMOTE_SWITCH_FOR_OPEN.getEndDeviceControlTypeMRID());
        endDeviceCommand = new ArmRemoteSwitchCommand(endDevice, endDeviceControlType, deviceMessageIds, deviceService, deviceMessageSpecificationService, thesaurus);

        ServiceCall serviceCall = mock(ServiceCall.class);
        when(serviceCall.getId()).thenReturn(SERVICE_CALL_ID);
        Instant releaseDate = Instant.ofEpochSecond(1465941600);    // 15/06/20165 00:00:00
        endDeviceCommand.setPropertyValue(dateTimeSpec, Date.from(releaseDate));

        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        Device.DeviceMessageBuilder deviceMessageBuilder = mock(Device.DeviceMessageBuilder.class);
        when(deviceMessageBuilder.setTrackingId(anyString())).thenReturn(deviceMessageBuilder);
        when(deviceMessageBuilder.setReleaseDate(anyObject())).thenReturn(deviceMessageBuilder);
        when(deviceMessageBuilder.addProperty(anyString(), anyObject())).thenReturn(deviceMessageBuilder);
        when(deviceMessageBuilder.add()).thenReturn(deviceMessage);
        when(device.newDeviceMessage(any(DeviceMessageId.class), any(TrackingCategory.class))).thenReturn(deviceMessageBuilder);

        // Business method
        List<DeviceMessage> deviceMessages = endDeviceCommand.createCorrespondingMultiSenseDeviceMessages(serviceCall, releaseDate);

        // Asserts
        assertEquals(2, deviceMessages.size());
        ArgumentCaptor<String> trackingIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Instant> releaseDateArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<String> propertyArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Date> valueArgumentCaptor = ArgumentCaptor.forClass(Date.class);
        verify(deviceMessageBuilder, times(2)).setTrackingId(trackingIdArgumentCaptor.capture());
        verify(deviceMessageBuilder, times(2)).setReleaseDate(releaseDateArgumentCaptor.capture());
        verify(deviceMessageBuilder, times(2)).addProperty(propertyArgumentCaptor.capture(), valueArgumentCaptor.capture());

        argumentCaptureValuesAreEqual(trackingIdArgumentCaptor);
        argumentCaptureValuesAreEqual(propertyArgumentCaptor);
        argumentCaptureValuesAreEqual(valueArgumentCaptor);
        assertEquals(Long.toString(SERVICE_CALL_ID), trackingIdArgumentCaptor.getValue());
        assertTrue(releaseDateArgumentCaptor.getAllValues().get(0).isAfter(releaseDate));
        assertTrue(releaseDateArgumentCaptor.getAllValues().get(1).isAfter(releaseDate));
        assertEquals(dateTimeSpec.getName(), propertyArgumentCaptor.getValue());
        assertEquals(Date.from(releaseDate), valueArgumentCaptor.getValue());
    }

    @Test
    public void testCreateCorrespondingMultiSenseDeviceMessageWithoutActivationDateSpec() throws Exception {
        DeviceMessageSpec messageSpec = mock(DeviceMessageSpec.class);
        when(deviceMessageSpecificationService.findMessageSpecById(DeviceMessageId.CONTACTOR_OPEN.dbValue())).thenReturn(Optional.of(messageSpec));
        when(messageSpec.getPropertySpecs()).thenReturn(Collections.emptyList());
        when(endDeviceControlType.getMRID()).thenReturn(EndDeviceControlTypeMapping.OPEN_REMOTE_SWITCH.getEndDeviceControlTypeMRID());
        endDeviceCommand = new OpenRemoteSwitchCommand(endDevice, endDeviceControlType, deviceMessageIds, deviceService, deviceMessageSpecificationService, thesaurus);

        ServiceCall serviceCall = mock(ServiceCall.class);
        when(serviceCall.getId()).thenReturn(SERVICE_CALL_ID);
        Instant releaseDate = Instant.now();

        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        Device.DeviceMessageBuilder deviceMessageBuilder = mock(Device.DeviceMessageBuilder.class);
        when(deviceMessageBuilder.setTrackingId(anyString())).thenReturn(deviceMessageBuilder);
        when(deviceMessageBuilder.setReleaseDate(anyObject())).thenReturn(deviceMessageBuilder);
        when(deviceMessageBuilder.addProperty(anyString(), anyObject())).thenReturn(deviceMessageBuilder);
        when(deviceMessageBuilder.add()).thenReturn(deviceMessage);
        when(device.newDeviceMessage(any(DeviceMessageId.class), any(TrackingCategory.class))).thenReturn(deviceMessageBuilder);

        // Business method
        List<DeviceMessage> deviceMessages = endDeviceCommand.createCorrespondingMultiSenseDeviceMessages(serviceCall, releaseDate);

        // Asserts
        assertEquals(1, deviceMessages.size());
        ArgumentCaptor<String> trackingIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Instant> releaseDateArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(deviceMessageBuilder).setTrackingId(trackingIdArgumentCaptor.capture());
        verify(deviceMessageBuilder).setReleaseDate(releaseDateArgumentCaptor.capture());
        verify(deviceMessageBuilder, never()).addProperty(anyString(), anyObject());    // Should never be called, as the 'Contactor open' message has no property specs

        assertEquals(Long.toString(SERVICE_CALL_ID), trackingIdArgumentCaptor.getValue());
        assertEquals(releaseDate, releaseDateArgumentCaptor.getValue());
    }

    @Test
    public void testCreateMultipleCorrespondingMultiSenseDeviceMessagesWithoutActivationDateSpec() throws Exception {
        DeviceMessageSpec messageSpec = mock(DeviceMessageSpec.class);
        when(deviceMessageSpecificationService.findMessageSpecById(DeviceMessageId.CONTACTOR_OPEN.dbValue())).thenReturn(Optional.of(messageSpec));
        when(deviceMessageSpecificationService.findMessageSpecById(DeviceMessageId.CONTACTOR_ARM.dbValue())).thenReturn(Optional.of(messageSpec));
        when(messageSpec.getPropertySpecs()).thenReturn(Collections.emptyList());
        when(endDeviceControlType.getMRID()).thenReturn(EndDeviceControlTypeMapping.ARM_REMOTE_SWITCH_FOR_OPEN.getEndDeviceControlTypeMRID());
        endDeviceCommand = new ArmRemoteSwitchCommand(endDevice, endDeviceControlType, deviceMessageIds, deviceService, deviceMessageSpecificationService, thesaurus);

        ServiceCall serviceCall = mock(ServiceCall.class);
        when(serviceCall.getId()).thenReturn(SERVICE_CALL_ID);
        Instant releaseDate = Instant.now();

        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        Device.DeviceMessageBuilder deviceMessageBuilder = mock(Device.DeviceMessageBuilder.class);
        when(deviceMessageBuilder.setTrackingId(anyString())).thenReturn(deviceMessageBuilder);
        when(deviceMessageBuilder.setReleaseDate(anyObject())).thenReturn(deviceMessageBuilder);
        when(deviceMessageBuilder.addProperty(anyString(), anyObject())).thenReturn(deviceMessageBuilder);
        when(deviceMessageBuilder.add()).thenReturn(deviceMessage);
        when(device.newDeviceMessage(any(DeviceMessageId.class), any(TrackingCategory.class))).thenReturn(deviceMessageBuilder);

        // Business method
        List<DeviceMessage> deviceMessages = endDeviceCommand.createCorrespondingMultiSenseDeviceMessages(serviceCall, releaseDate);

        // Asserts
        assertEquals(2, deviceMessages.size());
        ArgumentCaptor<String> trackingIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Instant> releaseDateArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(deviceMessageBuilder, times(2)).setTrackingId(trackingIdArgumentCaptor.capture());
        verify(deviceMessageBuilder, times(2)).setReleaseDate(releaseDateArgumentCaptor.capture());
        verify(deviceMessageBuilder, never()).addProperty(anyString(), anyObject());    // Should never be called, as the 'Contactor open' message has no property specs

        argumentCaptureValuesAreEqual(trackingIdArgumentCaptor);
        assertEquals(Long.toString(SERVICE_CALL_ID), trackingIdArgumentCaptor.getValue());
        assertEquals(releaseDate, releaseDateArgumentCaptor.getAllValues().get(0));
        assertEquals("Release date of 2th message should be 1 milliseconds shifted", releaseDate.plusMillis(1), releaseDateArgumentCaptor.getAllValues().get(1));
    }

    private void argumentCaptureValuesAreEqual(ArgumentCaptor argumentCaptor) {
        List values = argumentCaptor.getAllValues();
        assertEquals(values.size(), Collections.frequency(values, values.get(0)));
    }
}
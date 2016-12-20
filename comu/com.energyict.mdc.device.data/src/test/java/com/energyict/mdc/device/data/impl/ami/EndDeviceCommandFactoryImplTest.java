package com.energyict.mdc.device.data.impl.ami;

import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ami.CommandFactory;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.metering.ami.UnsupportedCommandException;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EndDeviceCommandFactoryImplTest {

    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();

    @Mock
    private MeteringService meteringService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    @Mock
    private DataModel dataModel;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private HeadEndInterface headEndInterface;
    @Mock
    private EndDeviceControlType endDeviceControlType;
    @Mock
    private EndDevice endDevice;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private TimeService timeService;
    @Mock
    private OrmService ormService;
    @Mock
    private BeanService beanService;
    @Mock
    private PropertySpecService propertySpecService;

    private CommandFactory commandFactory;

    private Device device = mock(Device.class, Mockito.RETURNS_DEEP_STUBS);

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

        when(meteringService.getEndDeviceControlType(anyString())).thenReturn(Optional.of(endDeviceControlType));
        commandFactory = new EndDeviceCommandFactoryImpl(meteringService, deviceService, deviceMessageSpecificationService);
        propertySpecService = new PropertySpecServiceImpl(this.timeService, this.ormService, this.beanService);
        when(deviceService.findByUniqueMrid(anyString())).thenReturn(Optional.of(device));
        DeviceProtocolPluggableClass protocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(protocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(device.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(protocolPluggableClass));

        when(device.getDeviceConfiguration().getDeviceType().getId()).thenReturn(6L);
        when(deviceMessageSpecificationService.findMessageSpecById(anyLong())).thenReturn(Optional.empty());
    }

    @Test
    public void createCommand() {
        DeviceMessageSpec messageSpec = mock(DeviceMessageSpec.class);
        PropertySpec spec = this.propertySpecService
                .stringSpec()
                .named("string1", "One")
                .describedAs("Description for string1")
                .setDefaultValue("Value1")
                .finish();
        when(messageSpec.getPropertySpecs()).thenReturn(Collections.singletonList(spec));
        when(deviceMessageSpecificationService.findMessageSpecById(anyLong())).thenReturn(Optional.of(messageSpec));
        when(endDeviceControlType.getMRID()).thenReturn(EndDeviceControlTypeMapping.CLOSE_REMOTE_SWITCH.getEndDeviceControlTypeMRID());

        List<com.energyict.mdc.upl.messages.DeviceMessageSpec> deviceMessageIds = new ArrayList<>();
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec1 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec1.getMessageId()).thenReturn(DeviceMessageId.CONTACTOR_CLOSE.dbValue());
        deviceMessageIds.add(deviceMessageSpec1);

        when(deviceProtocol.getSupportedMessages()).thenReturn(deviceMessageIds);

        // Business method
        EndDeviceCommand command = commandFactory.createConnectCommand(endDevice, null);

        // asserts
        assertEquals(endDevice, command.getEndDevice());
        assertEquals(endDeviceControlType, command.getEndDeviceControlType());
        assertEquals(1, command.getCommandArgumentSpecs().size());
        assertEquals(spec, command.getCommandArgumentSpecs().get(0));
    }

    @Test
    @Expected(value = UnsupportedCommandException.class)
    public void createCommandDeviceProtocolDoesNotSupport() {
        DeviceMessageSpec messageSpec = mock(DeviceMessageSpec.class);
        PropertySpec spec = this.propertySpecService
                .stringSpec()
                .named("string1", "One")
                .describedAs("Description for string1")
                .setDefaultValue("Value1")
                .finish();
        when(messageSpec.getPropertySpecs()).thenReturn(Collections.singletonList(spec));
        when(deviceMessageSpecificationService.findMessageSpecById(anyLong())).thenReturn(Optional.of(messageSpec));
        when(endDeviceControlType.getMRID()).thenReturn(EndDeviceControlTypeMapping.CLOSE_REMOTE_SWITCH.getEndDeviceControlTypeMRID());

        List<com.energyict.mdc.upl.messages.DeviceMessageSpec> deviceMessageIds = new ArrayList<>();
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec1 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec1.getMessageId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN.dbValue());
        deviceMessageIds.add(deviceMessageSpec1);

        when(deviceProtocol.getSupportedMessages()).thenReturn(deviceMessageIds);

        // Business method
        commandFactory.createConnectCommand(endDevice, Instant.now());
    }
}
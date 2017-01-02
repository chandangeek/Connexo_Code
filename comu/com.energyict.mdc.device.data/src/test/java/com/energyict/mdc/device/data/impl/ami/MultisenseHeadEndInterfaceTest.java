package com.energyict.mdc.device.data.impl.ami;


import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ami.EndDeviceCapabilities;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ami.EndDeviceCommandFactory;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.ServiceCallCommands;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.OnDemandReadServiceCallHandler;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.RegistersTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MultisenseHeadEndInterfaceTest {

    private static final String DEVICE_MRID = "deviceMRID";
    private static final long COMTASK_ID = 1;

    private final String url = "https://demo.eict.local:8080/apps/multisense/index.html#";
    @Mock
    User user;
    @Mock
    ServiceCallCommands serviceCallCommands;
    @Mock
    ServiceCall serviceCall;
    @Mock
    ComTaskExecutionImpl comTaskExecution;
    @Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    Device device;
    @Mock
    BundleContext context;
    @Mock
    private EndDevice endDevice;
    @Mock
    private volatile Clock clock;
    @Mock
    private volatile DeviceService deviceService;
    @Mock
    private volatile MeteringService meteringService;
    @Mock
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    @Mock
    private volatile DeviceConfigurationService deviceConfigurationService;
    @Mock
    private volatile MessageService messageService;
    @Mock
    private volatile NlsService nlsService;
    @Mock
    private volatile Thesaurus thesaurus;
    @Mock
    private volatile ServiceCallService serviceCallService;
    @Mock
    private volatile CustomPropertySetService customPropertySetService;
    @Mock
    private volatile PropertySpecService propertySpecService;
    @Mock
    private volatile ThreadPrincipalService threadPrincipalService;
    @Mock
    private EndDeviceCommandFactory endDeviceCommandFactory;
    @Mock
    private ReadingType readingType;
    @Mock
    private EndDeviceControlType contactorOpenEndDeviceControlType;
    @Mock
    private EndDeviceControlType contactoCloseEndDeviceControlType;
    private MultiSenseHeadEndInterfaceImpl headEndInterface;

    @Before
    public void setup() {
        when(clock.instant()).thenReturn(Instant.EPOCH);
        when(threadPrincipalService.getPrincipal()).thenReturn(user);
        when(user.hasPrivilege(KnownAmrSystem.MDC.getName(), Privileges.Constants.VIEW_DEVICE)).thenReturn(true);
        when(context.getProperty(MultiSenseHeadEndInterfaceImpl.MDC_URL)).thenReturn(url);
        headEndInterface = Mockito.spy(new MultiSenseHeadEndInterfaceImpl(deviceService, deviceConfigurationService, meteringService, thesaurus, serviceCallService, customPropertySetService, endDeviceCommandFactory, threadPrincipalService, clock));
        when(headEndInterface.getServiceCallCommands()).thenReturn(serviceCallCommands);    // Use mocked variant of ServiceCallCommands, as for this test we are not interested in what happens with ServiceCalls
        headEndInterface.activate(context);
        when(serviceCallCommands.createOperationServiceCall(any(), any(), any(), any())).thenReturn(serviceCall);
        when(serviceCall.getExtensionFor(any(CommandCustomPropertySet.class))).thenReturn(Optional.of(new CommandServiceCallDomainExtension()));
        endDevice.setMRID(DEVICE_MRID);
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(endDevice.getAmrSystem()).thenReturn(amrSystem);
        when(amrSystem.getId()).thenReturn(1);
        when(amrSystem.is(KnownAmrSystem.MDC)).thenReturn(true);
        when(deviceService.findByUniqueMrid(anyString())).thenReturn(Optional.of(device));
        when(device.getmRID()).thenReturn(DEVICE_MRID);

        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);

        List<com.energyict.mdc.upl.messages.DeviceMessageSpec> deviceMessageIds = new ArrayList<>();
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec1 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec1.getId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN.dbValue());
        deviceMessageIds.add(deviceMessageSpec1);
        com.energyict.mdc.upl.messages.DeviceMessageSpec deviceMessageSpec2 = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
        when(deviceMessageSpec2.getId()).thenReturn(DeviceMessageId.CONTACTOR_CLOSE.dbValue());
        deviceMessageIds.add(deviceMessageSpec2);

        when(deviceProtocol.getSupportedMessages()).thenReturn(deviceMessageIds);
        DeviceProtocolPluggableClass protocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(protocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(device.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(protocolPluggableClass));

        when(deviceConfigurationService.getReadingTypesRelatedToConfiguration(any(DeviceConfiguration.class))).thenReturn(Collections.singletonList(readingType));
        when(device.getDeviceConfiguration().getDeviceType().getId()).thenReturn(3L);
        when(device.getDeviceConfiguration().getComTaskEnablements()).thenReturn(Collections.emptyList());
        when(meteringService.getEndDeviceControlType(EndDeviceControlTypeMapping.OPEN_REMOTE_SWITCH.getEndDeviceControlTypeMRID())).thenReturn(Optional.of(contactorOpenEndDeviceControlType));
        when(meteringService.getEndDeviceControlType(EndDeviceControlTypeMapping.CLOSE_REMOTE_SWITCH.getEndDeviceControlTypeMRID())).thenReturn(Optional.of(contactoCloseEndDeviceControlType));
    }

    @Test
    public void getURLForEndDevice() {
        Optional<URL> url = headEndInterface.getURLForEndDevice(endDevice);
        if (url.isPresent()) {
            assertTrue(url.get().toString().equals(this.url + "/devices/" + DEVICE_MRID));
        } else {
            throw new AssertionError("URL not found");
        }
    }

    @Test
    public void getDeviceCapabilities() {
        // Business method
        EndDeviceCapabilities endDeviceCapabilities = headEndInterface.getCapabilities(endDevice);

        // Asserts
        assertEquals(1, endDeviceCapabilities.getConfiguredReadingTypes().size());
        assertEquals(readingType, endDeviceCapabilities.getConfiguredReadingTypes().get(0));
        assertEquals(2, endDeviceCapabilities.getSupportedControlTypes().size());
        assertArrayEquals(Arrays.asList(contactoCloseEndDeviceControlType, contactorOpenEndDeviceControlType).toArray(), endDeviceCapabilities.getSupportedControlTypes().toArray());
    }

    @Test
    public void sendCommandTest() throws Exception {
        MessagesTask messagesTask = mock(MessagesTask.class);
        DeviceMessageCategory deviceMessageCategory = mock(DeviceMessageCategory.class);
        DeviceMessageSpec deviceMessageSpec = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec.getId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN);
        doReturn(Collections.singletonList(deviceMessageSpec)).when(deviceMessageCategory).getMessageSpecifications();
        when(messagesTask.getDeviceMessageCategories()).thenReturn(Collections.singletonList(deviceMessageCategory));

        ComTask comTask = mock(ComTask.class);
        when(comTask.getId()).thenReturn(COMTASK_ID);
        when(comTask.getProtocolTasks()).thenReturn(Collections.singletonList(messagesTask));
        when(comTaskExecution.getComTasks()).thenReturn(Collections.singletonList(comTask));
        when(device.getComTaskExecutions()).thenReturn(Collections.singletonList(comTaskExecution));

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class, Mockito.RETURNS_DEEP_STUBS);
        when(device.getDeviceConfiguration().getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement));
        when(comTaskEnablement.getComTask()).thenReturn(comTask);

        Instant messageReleaseDate = Instant.now();
        EndDeviceCommandImpl endDeviceCommand = mock(EndDeviceCommandImpl.class);
        when(endDeviceCommand.getEndDevice()).thenReturn(endDevice);
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getReleaseDate()).thenReturn(messageReleaseDate);
        when(deviceMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN);
        when(endDeviceCommand.createCorrespondingMultiSenseDeviceMessages(any(ServiceCall.class), any(Instant.class))).thenReturn(Collections.singletonList(deviceMessage));

        // Business method
        headEndInterface.sendCommand(endDeviceCommand, messageReleaseDate);

        // Asserts
        verify(comTaskExecution).addNewComTaskExecutionTrigger(messageReleaseDate);
        verify(comTaskExecution).updateNextExecutionTimestamp();
    }

    @Test
    public void readMeterTest() throws Exception {

        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        RegistersTask registersTask = mock(RegistersTask.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getProtocolTasks()).thenReturn(Collections.singletonList(registersTask));

        Meter meter = mock(Meter.class);
        when(deviceService.findByUniqueMrid(any())).thenReturn(Optional.of(device));

        RegisteredCustomPropertySet registredReadSet = mock(RegisteredCustomPropertySet.class);
        OnDemandReadServiceCallCustomPropertySet readSet = mock(OnDemandReadServiceCallCustomPropertySet.class);
        when(registredReadSet.getCustomPropertySet()).thenReturn(readSet);
        when(readSet.getId()).thenReturn(OnDemandReadServiceCallDomainExtension.class.getName());
        when(readSet.getName()).thenReturn(OnDemandReadServiceCallCustomPropertySet.class.getSimpleName());
        RegisteredCustomPropertySet registredCompletionSet = mock(RegisteredCustomPropertySet.class);
        CompletionOptionsCustomPropertySet completionSet = mock(CompletionOptionsCustomPropertySet.class);
        when(registredCompletionSet.getCustomPropertySet()).thenReturn(completionSet);
        when(completionSet.getId()).thenReturn(CompletionOptionsServiceCallDomainExtension.class.getName());
        when(completionSet.getName()).thenReturn(CompletionOptionsCustomPropertySet.class.getSimpleName());
        when(customPropertySetService.findActiveCustomPropertySets(any())).thenReturn(Arrays.asList(registredReadSet, registredCompletionSet));

        ServiceCallType serviceCallType = mock(ServiceCallType.class);
        when(serviceCallService.findServiceCallType(OnDemandReadServiceCallHandler.SERVICE_CALL_HANDLER_NAME, OnDemandReadServiceCallHandler.VERSION))
                .thenReturn(Optional.of(serviceCallType));
        ServiceCallBuilder builder = mock(ServiceCallBuilder.class);
        when(builder.origin(anyString())).thenReturn(builder);
        when(builder.extendedWith(any())).thenReturn(builder);
        when(builder.targetObject(any(Device.class))).thenReturn(builder);
        when(builder.create()).thenReturn(serviceCall);
        when(serviceCallType.newServiceCall()).thenReturn(builder);


        List<ReadingType> readingTypes = Collections.singletonList(readingType);

        // Business method
        headEndInterface.readMeter(meter, readingTypes);

        // Asserts
        verify(serviceCall, times(1)).requestTransition(DefaultState.PENDING);
    }
}

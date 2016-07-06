package com.energyict.mdc.device.data.impl.ami;


import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ami.EndDeviceCapabilities;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ami.EndDeviceCommandFactory;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.ServiceCallCommands;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;

import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MultisenseHeadEndInterfaceTest {

    private static final String DEVICE_MRID = "deviceMRID";
    private static final long COMTASK_ID = 1;

    private final String url = "https://demo.eict.local:8080/apps/multisense/index.html#";

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

    private MultiSenseHeadEndInterfaceImpl headEndInterface;

    @Before
    public void setup() {
        when(threadPrincipalService.getPrincipal()).thenReturn(user);
        when(user.hasPrivilege(KnownAmrSystem.MDC.getName(), Privileges.Constants.VIEW_DEVICE)).thenReturn(true);
        Map<KnownAmrSystem, String> knownAmrSystemStringMap = new HashMap<>();
        knownAmrSystemStringMap.put(KnownAmrSystem.MDC, url);
        when(meteringService.getSupportedApplicationsUrls()).thenReturn(knownAmrSystemStringMap);
        headEndInterface = Mockito.spy(new MultiSenseHeadEndInterfaceImpl(deviceService, deviceConfigurationService, meteringService, thesaurus, serviceCallService, customPropertySetService, endDeviceCommandFactory, threadPrincipalService));
        when(headEndInterface.getServiceCallCommands()).thenReturn(serviceCallCommands);    // Use mocked variant of ServiceCallCommands, as for this test we are not interested in what happens with ServiceCalls
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
        Set<DeviceMessageId> deviceMessageIds = new HashSet<>();
        deviceMessageIds.add(DeviceMessageId.CONTACTOR_OPEN);
        deviceMessageIds.add(DeviceMessageId.CONTACTOR_CLOSE);
        when(deviceProtocol.getSupportedMessages()).thenReturn(deviceMessageIds);
        DeviceProtocolPluggableClass protocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(protocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(device.getDeviceProtocolPluggableClass()).thenReturn(protocolPluggableClass);

        when(deviceConfigurationService.getReadingTypesRelatedToConfiguration(any(DeviceConfiguration.class))).thenReturn(Collections.singletonList(readingType));
        when(device.getDeviceConfiguration().getDeviceType().getId()).thenReturn(3L);
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
        when(deviceMessageCategory.getMessageSpecifications()).thenReturn(Collections.singletonList(deviceMessageSpec));
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
        DeviceMessage<Device> deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getReleaseDate()).thenReturn(messageReleaseDate);
        when(deviceMessage.getDeviceMessageId()).thenReturn(DeviceMessageId.CONTACTOR_OPEN);
        when(endDeviceCommand.createCorrespondingMultiSenseDeviceMessages(any(ServiceCall.class), any(Instant.class))).thenReturn(Collections.singletonList(deviceMessage));

        // Business method
        headEndInterface.sendCommand(endDeviceCommand, messageReleaseDate);

        // Asserts
        verify(comTaskExecution).addNewComTaskExecutionTrigger(messageReleaseDate);
        verify(comTaskExecution).updateNextExecutionTimestamp();
    }
}

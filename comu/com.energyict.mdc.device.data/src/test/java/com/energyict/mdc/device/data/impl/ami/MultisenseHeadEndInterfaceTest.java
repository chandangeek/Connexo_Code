package com.energyict.mdc.device.data.impl.ami;


import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.EndDeviceControlTypeMapping;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ami.EndDeviceCapabilities;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MultisenseHeadEndInterfaceTest {

    @Mock
    private EndDevice endDevice;

    @Mock
    private volatile Clock clock;

    @Mock
    private volatile DeviceService deviceService;

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
    private ReadingType readingType;

    @Mock
    private ThreadPrincipalService threadPrincipalService;

    @Mock
    User user;

    private HeadEndInterface headEndInterface;
    private final String mRid = "mRID0123456789";
    private final String uri = "https://demo.eict.local:8080/apps/multisense/index.html#";


    @Before
    public void setup() throws MalformedURLException {
        MeteringService meteringService = mock(MeteringService.class, Mockito.RETURNS_DEEP_STUBS);
        headEndInterface = new MultiSenseHeadEndInterface(deviceService, meteringService, deviceMessageSpecificationService, deviceConfigurationService, nlsService, thesaurus, propertySpecService, threadPrincipalService);
        endDevice.setMRID(mRid);
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(endDevice.getAmrSystem()).thenReturn(amrSystem);
        when(amrSystem.getId()).thenReturn(1);
        Device device = mock(Device.class, Mockito.RETURNS_DEEP_STUBS);
        when(amrSystem.is(KnownAmrSystem.MDC)).thenReturn(true);
        when(deviceService.findByUniqueMrid(anyString())).thenReturn(Optional.of(device));
        when(device.getmRID()).thenReturn(mRid);
        when(deviceConfigurationService.getReadingTypesRelatedToConfiguration(any(DeviceConfiguration.class))).thenReturn(Collections.singletonList(readingType));
        when(device.getDeviceConfiguration().getDeviceType().getId()).thenReturn(3L);
        EndDeviceControlType endDeviceControlType = mock(EndDeviceControlType.class);
        when(meteringService.getEndDeviceControlType(anyString())).thenReturn(Optional.of(endDeviceControlType));
        when(threadPrincipalService.getPrincipal()).thenReturn(user);
        when(user.hasPrivilege(KnownAmrSystem.MDC.getName(), Privileges.Constants.VIEW_DEVICE)).thenReturn(true);
        when(meteringService.getSupportedApplicationsUrls().get(KnownAmrSystem.MDC)).thenReturn(uri);
    }

    @Test
    public void getURLForEndDevice() {
        Optional<URL> url = headEndInterface.getURLForEndDevice(endDevice);
        if (url.isPresent()) {
            assertTrue(url.get().toString().equals(uri + "/devices/" + mRid));
        } else {
            throw new AssertionError("URL not found");
        }
    }

    @Test
    public void getDeviceCapabilities() {
        EndDeviceCapabilities endDeviceCapabilities = headEndInterface.getCapabilities(endDevice);
        AtomicInteger count = new AtomicInteger(0);
        Stream.of(EndDeviceControlTypeMapping.values()).forEach(type -> {
            if (type.getEndDeviceControlTypeMRID().startsWith(3 + ".")
                    || type.getEndDeviceControlTypeMRID().startsWith("0.")
                    || type.getEndDeviceControlTypeMRID().startsWith("*.")) {
                count.incrementAndGet();
            }
        });

        assertThat(endDeviceCapabilities.getSupportedControlTypes()).isEmpty();
        assertThat(endDeviceCapabilities.getConfiguredReadingTypes()).hasSize(1);
        assertTrue(endDeviceCapabilities.getConfiguredReadingTypes().get(0).equals(readingType));
    }
}

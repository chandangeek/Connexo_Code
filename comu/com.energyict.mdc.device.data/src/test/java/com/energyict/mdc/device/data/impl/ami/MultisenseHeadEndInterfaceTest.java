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
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import java.net.URI;
import java.net.URL;
import java.time.Clock;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

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
    private ReadingType readingType;

    private HeadEndInterface headEndInterface;
    private final String mRid = "mRID";

    @Before
    public void setup() {
        headEndInterface = new MultiSenseHeadEndInterface(clock, deviceService, meteringService, deviceMessageSpecificationService, deviceConfigurationService, messageService, nlsService, thesaurus, serviceCallService, customPropertySetService, propertySpecService);
        endDevice.setMRID(mRid);
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(endDevice.getAmrSystem()).thenReturn(amrSystem);
        when(amrSystem.getId()).thenReturn(1);
        Device device = mock(Device.class, Mockito.RETURNS_DEEP_STUBS);
        when(amrSystem.is(KnownAmrSystem.MDC)).thenReturn(true);
        when(deviceService.findByUniqueMrid(anyString())).thenReturn(Optional.of(device));
        when(device.getmRID()).thenReturn(mRid);
        when(deviceConfigurationService.getReadingTypesRelatedToConfiguration(any(DeviceConfiguration.class))).thenReturn(Arrays.asList(readingType));
        when(device.getDeviceConfiguration().getDeviceType().getId()).thenReturn(3L);
        EndDeviceControlType endDeviceControlType = mock(EndDeviceControlType.class);
        when(meteringService.getEndDeviceControlType(anyString())).thenReturn(Optional.of(endDeviceControlType));
    }

    @Test
    public void getURLForEndDevice(){
        Optional<URI> uri = headEndInterface.getURIForEndDevice(endDevice);
        if(uri.isPresent()){
            assertTrue(uri.get().toString().equals("/devices/"+mRid));
        }else{
            throw new AssertionError("URL not found");
        }
    }

    @Test
    public void getDeviceCapabilities(){
        EndDeviceCapabilities endDeviceCapabilities = headEndInterface.getCapabilities(endDevice);
        AtomicInteger count = new AtomicInteger(0);
        Stream.of(EndDeviceControlTypeMapping.values()).forEach(type -> {
                    if (type.getEndDeviceControlTypeMRID().startsWith(3 + ".")
                            || type.getEndDeviceControlTypeMRID().startsWith("0.")
                            || type.getEndDeviceControlTypeMRID().startsWith("*.")) {
                        count.incrementAndGet();
                    }
                });

        assertTrue(endDeviceCapabilities.getSupportedControlTypes().size() == count.get());
        assertTrue(endDeviceCapabilities.getConfiguredReadingTypes().size()==1);
        assertTrue(endDeviceCapabilities.getConfiguredReadingTypes().get(0).equals(readingType));
    }
}

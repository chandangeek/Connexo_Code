package com.energyict.mdc.device.data.impl.ami;


import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.exceptions.DataEncryptionException;

import java.net.URI;
import java.net.URL;
import java.time.Clock;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
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

    private HeadEndInterface headEndInterface;
    private final String mRid = "mRID";

    @Before
    public void setup() {
        headEndInterface = new MultiSenseHeadEndInterface(clock, deviceService, meteringService, deviceMessageSpecificationService, deviceConfigurationService, messageService, nlsService, thesaurus, serviceCallService, customPropertySetService, propertySpecService);
        endDevice.setMRID(mRid);
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(endDevice.getAmrSystem()).thenReturn(amrSystem);
        when(amrSystem.getId()).thenReturn(1);
        Device device = mock(Device.class);
        when(amrSystem.is(KnownAmrSystem.MDC)).thenReturn(true);
        when(deviceService.findByUniqueMrid(anyString())).thenReturn(Optional.of(device));
        when(device.getmRID()).thenReturn(mRid);
    }

    @Test
    public void getURLForEndDevice(){
        Optional<URI> uri = headEndInterface.getURIForEndDevice(endDevice);
        uri.ifPresent(found -> assertTrue(found.toString().equals("/devices/"+mRid)));
    }

    @Test
    public void getDeviceCapabilities(){

    }
}

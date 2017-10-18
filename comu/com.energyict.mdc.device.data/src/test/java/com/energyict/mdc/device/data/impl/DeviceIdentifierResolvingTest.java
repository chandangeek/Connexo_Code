/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierByConnectionTypeAndProperty;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierById;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierByMRID;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierByPropertyValue;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierForAlreadyKnownDevice;
import com.energyict.mdc.device.data.impl.tasks.InboundIpConnectionTypeImpl;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.Introspector;

import com.energyict.protocolimplv2.identifiers.CallHomeIdPlaceHolder;
import org.reflections.Reflections;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests the resolving of all different {@link DeviceIdentifier}s to actual devices.
 * This should ensure the resolving of the different {@link Introspector}s (based on textual type name)
 * matches the definition of the {@link Introspector} given in the different {@link DeviceIdentifier}s. *
 *
 * @author Stijn Vanhoorelbeke
 * @since 15.09.17 - 17:22
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceIdentifierResolvingTest extends PersistenceIntegrationTest {

    private static DeviceServiceImpl deviceService;

    @BeforeClass
    public static void setUp() throws Exception {
        deviceService = new DeviceServiceImpl(
                inMemoryPersistence.getDeviceDataModelService(),
                mock(MeteringService.class),
                mock(QueryService.class),
                mock(Thesaurus.class),
                inMemoryPersistence.getClock()
        );
    }

    @Test
    @Transactional
    public void testMatchingIntrospectorResolvingForAllKnownDeviceIdentifierImplementations() throws Exception {
        Reflections reflections = new Reflections("com.energyict");
        Set<Class<? extends DeviceIdentifier>> classes = reflections.getSubTypesOf(DeviceIdentifier.class);
        List<Class<? extends DeviceIdentifier>> deviceIdentifierClasses = classes.stream().filter(identifierClass ->
                !identifierClass.getSimpleName().equals("NullDeviceIdentifier") && !identifierClass.getName().equals("com.energyict.mdc.upl.meterdata.identifiers.FindMultipleDevices"))
                .collect(Collectors.toList());
        for (Class<? extends DeviceIdentifier> aClass : deviceIdentifierClasses) {
            Introspector introspector = aClass.newInstance().forIntrospection();
            Optional<DeviceServiceImpl.IntrospectorTypes> matchingIntrospectorType = DeviceServiceImpl.IntrospectorTypes.forName(introspector.getTypeName());
            assertThat(matchingIntrospectorType)
                    .as("No matching introspector found in DeviceServiceImpl.IntrospectorTypes for type " + introspector.getTypeName() +
                            ". Please add corresponding entry and make sure it is used inside DeviceServiceImpl#find.")
                    .isPresent();
            assertThat(introspector.getRoles()).containsOnlyElementsOf(matchingIntrospectorType.get().getRoles()); // If not present, than above assert should already have failed
        }
    }

    @Test
    @Transactional
    public void testDeviceDataDeviceIdentifierById() throws Exception {
        DeviceService spiedService = spy(deviceService);
        spiedService.findAllDevicesByIdentifier(new DeviceIdentifierById(1));
        verify(spiedService).findDeviceById(1);
    }

    @Test
    @Transactional
    public void testDeviceDataDeviceIdentifierByMRID() throws Exception {
        DeviceService spiedService = spy(deviceService);
        spiedService.findAllDevicesByIdentifier(new DeviceIdentifierByMRID("mrid"));
        verify(spiedService).findDeviceByMrid("mrid");
    }

    @Test
    @Transactional
    public void testDeviceDataDeviceIdentifierBySerialNumberMRID() throws Exception {
        DeviceService spiedService = spy(deviceService);
        spiedService.findAllDevicesByIdentifier(new DeviceIdentifierBySerialNumber("serialNumber"));
        verify(spiedService).findDevicesBySerialNumber("serialNumber");
    }

    @Test
    @Transactional
    public void testDeviceDataDeviceIdentifierForAlreadyKnownDevice() throws Exception {
        Device myDevice = mock(Device.class);
        DeviceService spiedService = spy(deviceService);
        List<com.energyict.mdc.device.data.Device> devices = spiedService.findAllDevicesByIdentifier(new DeviceIdentifierForAlreadyKnownDevice(myDevice));
        assertThat(devices).containsOnly(myDevice);
    }

    @Test
    @Transactional
    public void testDeviceDataDeviceIdentifierByPropertyValue() throws Exception {
        DeviceService spiedService = spy(deviceService);
        spiedService.findAllDevicesByIdentifier(new DeviceIdentifierByPropertyValue("myProperty", "myValue"));
        verify(spiedService).findDevicesByPropertySpecValue("myProperty", "myValue");
    }

    @Test
    @Transactional
    public void testDeviceDataDeviceIdentifierByConnectionTypeAndProperty() throws Exception {
        DeviceService spiedService = spy(deviceService);
        spiedService.findAllDevicesByIdentifier(new DeviceIdentifierByConnectionTypeAndProperty(InboundIpConnectionTypeImpl.class, "myProperty", "myValue"));
        verify(spiedService).findDevicesByConnectionTypeAndProperty(InboundIpConnectionTypeImpl.class, "myProperty", "myValue");
    }

    @Test
    @Transactional
    public void tesProtocolDeviceIdentifierById() throws Exception {
        DeviceService spiedService = spy(deviceService);
        spiedService.findAllDevicesByIdentifier(new com.energyict.protocolimplv2.identifiers.DeviceIdentifierById(1));
        verify(spiedService).findDeviceById(1);
    }

    @Test
    @Transactional
    public void tesProtocolDeviceIdentifierBySerialNumber() throws Exception {
        DeviceService spiedService = spy(deviceService);
        spiedService.findAllDevicesByIdentifier(new com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber("mySerial"));
        verify(spiedService).findDevicesBySerialNumber("mySerial");
    }

    @Test
    @Transactional
    public void tesProtocolDeviceIdentifierLikeSerialNumber() throws Exception {
        DeviceServiceImpl spiedService = spy(deviceService);
        spiedService.findAllDevicesByIdentifier(new com.energyict.protocolimplv2.identifiers.DeviceIdentifierLikeSerialNumber("mySerial"));
        verify(spiedService).findDevicesBySerialNumberPattern("mySerial");
    }

    @Test
    @Transactional
    public void tesProtocolDeviceIdentifierBySystemTitle() throws Exception {
        DeviceServiceImpl spiedService = spy(deviceService);
        spiedService.findAllDevicesByIdentifier(new com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySystemTitle("systemTitle"));
        verify(spiedService).findDevicesByPropertySpecValue("DeviceSystemTitle", "systemTitle");
    }

    @Test
    @Transactional
    public void tesProtocolDeviceIdentifierByDialHomeId() throws Exception {
        DeviceServiceImpl spiedService = spy(deviceService);
        spiedService.findAllDevicesByIdentifier(new com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier("callHomeId"));
        verify(spiedService).findDevicesByPropertySpecValue(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, "callHomeId");
    }

    @Test
    @Transactional
    public void tesProtocolDeviceIdentifierByDialHomeIdPlaceHolder() throws Exception {
        DeviceServiceImpl spiedService = spy(deviceService);
        CallHomeIdPlaceHolder callHomeIdPlaceHolder = new CallHomeIdPlaceHolder();
        callHomeIdPlaceHolder.setSerialNumber("systemTitle");
        spiedService.findAllDevicesByIdentifier(new com.energyict.protocolimplv2.identifiers.DialHomeIdPlaceHolderDeviceIdentifier(callHomeIdPlaceHolder));
        verify(spiedService).findDevicesByPropertySpecValue(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, "systemTitle");
    }

    @Test
    @Transactional
    public void testProtocolDeviceIdentifierByConnectionTypeAndProperty() throws Exception {
        DeviceService spiedService = spy(deviceService);
        spiedService.findAllDevicesByIdentifier(new com.energyict.protocolimplv2.identifiers.DeviceIdentifierByConnectionTypeAndProperty(InboundIpConnectionTypeImpl.class, "myProperty", "myValue"));
        verify(spiedService).findDevicesByConnectionTypeAndProperty(InboundIpConnectionTypeImpl.class, "myProperty", "myValue");
    }
}
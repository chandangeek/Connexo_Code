/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.RegisterService;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierForAlreadyKnownDevice;
import com.energyict.mdc.device.data.impl.identifiers.RegisterIdentifierByAlreadyKnownRegister;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.Introspector;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import com.energyict.obis.ObisCode;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the resolving of all different {@link RegisterIdentifier}s to actual registers.
 * This should ensure the resolving of the different {@link Introspector}s (based on textual type name)
 * matches the definition of the {@link Introspector} given in the different {@link RegisterIdentifier}s.
 *
 * @author Stijn Vanhoorelbeke
 * @since 15.09.17 - 17:22
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterIdentifierResolvingTest extends PersistenceIntegrationTest {

    private static final ObisCode REGISTER_OBIS_CODE = ObisCode.fromString("0.0.96.1.0.255");
    private static RegisterServiceImpl registerService;

    @Mock
    Device device;

    DeviceIdentifier deviceIdentifier;

    @BeforeClass
    public static void setUpClass() throws Exception {
        registerService = new RegisterServiceImpl(inMemoryPersistence.getDeviceDataModelService());
    }

    @Before
    public void setUp() throws Exception {
        deviceIdentifier = new DeviceIdentifierForAlreadyKnownDevice(device);
        when(device.getRegisterWithDeviceObisCode(REGISTER_OBIS_CODE)).thenReturn(Optional.empty());
    }


    @Test
    @Transactional
    public void testMatchingIntrospectorResolvingForAllKnownRegisterIdentifierImplementations() throws Exception {
        Reflections reflections = new Reflections("com.energyict");
        List<Class<? extends RegisterIdentifier>> registerIdentifierClasses = new ArrayList<>(reflections.getSubTypesOf(RegisterIdentifier.class));
        for (Class<? extends RegisterIdentifier> aClass : registerIdentifierClasses) {
            Introspector introspector = aClass.newInstance().forIntrospection();
            Optional<RegisterServiceImpl.IntrospectorTypes> matchingIntrospectorType = RegisterServiceImpl.IntrospectorTypes.forName(introspector.getTypeName());
            assertThat(matchingIntrospectorType)
                    .as("No matching introspector found in RegisterServiceImpl.IntrospectorTypes for type " + introspector.getTypeName() +
                            ". Please add corresponding entry and make sure it is used inside RegisterServiceImpl#find.")
                    .isPresent();
            assertThat(introspector.getRoles()).containsOnlyElementsOf(matchingIntrospectorType.get().getRoles()); // If not present, than above assert should already have failed
        }
    }

    @Test
    @Transactional
    public void testDeviceDataRegisterIdentifierByAlreadyKnownRegister() throws Exception {
        RegisterService spiedService = spy(registerService);
        Register myRegister = mock(Register.class);
        when(myRegister.getDevice()).thenReturn(device);
        Optional<Register> foundRegister = spiedService.findByIdentifier(new RegisterIdentifierByAlreadyKnownRegister(myRegister));
        assertThat(foundRegister).isPresent();
        assertThat(foundRegister.get()).isEqualTo(myRegister);
    }

    @Test
    @Transactional
    public void tesProtocolRegisterIdentifierById() throws Exception {
        RegisterServiceImpl spiedService = spy(registerService);

        spiedService.findByIdentifier(new com.energyict.protocolimplv2.identifiers.RegisterIdentifierById(1, REGISTER_OBIS_CODE, deviceIdentifier));
        verify(spiedService).find(device, 1L);
    }

    @Test
    @Transactional
    public void tesProtocolPrimeRegisterForChannelIdentifier() throws Exception {
        RegisterServiceImpl spiedService = spy(registerService);

        spiedService.findByIdentifier(new com.energyict.protocolimplv2.identifiers.PrimeRegisterForChannelIdentifier(deviceIdentifier, 1, REGISTER_OBIS_CODE));
        verify(spiedService).findByDeviceAndChannelIndex(device, 1);
    }

    @Test
    @Transactional
    public void tesProtocolRegisterDataIdentifierByObisCodeAndDevice() throws Exception {
        RegisterServiceImpl spiedService = spy(registerService);

        spiedService.findByIdentifier(new com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice(REGISTER_OBIS_CODE, deviceIdentifier));
        verify(spiedService).findByDeviceAndObisCode(device, REGISTER_OBIS_CODE);
    }
}
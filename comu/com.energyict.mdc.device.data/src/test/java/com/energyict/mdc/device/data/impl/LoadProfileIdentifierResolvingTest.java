/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierForAlreadyKnownDevice;
import com.energyict.mdc.device.data.impl.identifiers.LoadProfileIdentifierById;
import com.energyict.mdc.device.data.impl.identifiers.LoadProfileIdentifierByObisCodeAndDevice;
import com.energyict.mdc.device.data.impl.identifiers.LoadProfileIdentifierFirstOnDevice;
import com.energyict.mdc.device.data.impl.identifiers.LoadProfileIdentifierForAlreadyKnownLoadProfile;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.Introspector;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.eict.eiweb.FirstLoadProfileOnDevice;
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
 * Tests the resolving of all different {@link LoadProfileIdentifier}s to actual load profiles.
 * This should ensure the resolving of the different {@link Introspector}s (based on textual type name)
 * matches the definition of the {@link Introspector} given in the different {@link LoadProfileIdentifier}s.
 *
 * @author Stijn Vanhoorelbeke
 * @since 15.09.17 - 17:22
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadProfileIdentifierResolvingTest extends PersistenceIntegrationTest {

    private static final ObisCode LOADPROFILE_OBIS_CODE = ObisCode.fromString("0.0.96.1.0.255");
    private static LoadProfileServiceImpl loadProfileService;

    @Mock
    Device device;

    DeviceIdentifier deviceIdentifier;

    @BeforeClass
    public static void setUpClass() throws Exception {
        loadProfileService = new LoadProfileServiceImpl(inMemoryPersistence.getDeviceDataModelService());
    }

    @Before
    public void setUp() throws Exception {
        deviceIdentifier = new DeviceIdentifierForAlreadyKnownDevice(device);
    }

    @Test
    @Transactional
    public void testMatchingIntrospectorResolvingForAllKnownLoadProfileIdentifierImplementations() throws Exception {
        Reflections reflections = new Reflections("com.energyict");
        List<Class<? extends LoadProfileIdentifier>> identifierClasses = new ArrayList<>(reflections.getSubTypesOf(LoadProfileIdentifier.class));
        for (Class<? extends LoadProfileIdentifier> aClass : identifierClasses) {
            Introspector introspector = aClass.newInstance().forIntrospection();
            Optional<LoadProfileServiceImpl.IntrospectorTypes> matchingIntrospectorType = LoadProfileServiceImpl.IntrospectorTypes.forName(introspector.getTypeName());
            assertThat(matchingIntrospectorType)
                    .as("No matching introspector found in LoadProfileServiceImpl.IntrospectorTypes for type " + introspector.getTypeName() +
                            ". Please add corresponding entry and make sure it is used inside LoadProfileServiceImpl#doFind.")
                    .isPresent();
            assertThat(introspector.getRoles()).containsOnlyElementsOf(matchingIntrospectorType.get().getRoles()); // If not present, than above assert should already have failed
        }
    }

    @Test
    @Transactional
    public void testDeviceDataLoadProfileIdentifierByAlreadyKnownLoadProfile() throws Exception {
        LoadProfileService spiedService = spy(loadProfileService);
        LoadProfile myLoadProfile = mock(LoadProfile.class);
        when(myLoadProfile.getDevice()).thenReturn(device);
        Optional<LoadProfile> foundLoadProfile = spiedService.findByIdentifier(new LoadProfileIdentifierForAlreadyKnownLoadProfile(myLoadProfile, LOADPROFILE_OBIS_CODE));
        assertThat(foundLoadProfile).isPresent();
        assertThat(foundLoadProfile.get()).isEqualTo(myLoadProfile);
    }

    @Test
    @Transactional
    public void testDeviceDataLoadProfileIdentifierById() throws Exception {
        LoadProfileService spiedService = spy(loadProfileService);

        spiedService.findByIdentifier(new LoadProfileIdentifierById(1L, LOADPROFILE_OBIS_CODE, deviceIdentifier));
        verify(spiedService).findById(1L);
    }

    @Test
    @Transactional
    public void testDeviceDataLoadProfileIdentifierByObisCodeAndDevice() throws Exception {
        LoadProfileServiceImpl spiedService = spy(loadProfileService);

        spiedService.findByIdentifier(new LoadProfileIdentifierByObisCodeAndDevice(LOADPROFILE_OBIS_CODE, deviceIdentifier));
        verify(spiedService).findByDeviceAndObisCode(device, LOADPROFILE_OBIS_CODE);
    }

    @Test
    @Transactional
    public void testDeviceDataLoadProfileIdentifierFirstOnDevice() throws Exception {
        LoadProfileServiceImpl spiedService = spy(loadProfileService);

        spiedService.findByIdentifier(new LoadProfileIdentifierFirstOnDevice(deviceIdentifier, LOADPROFILE_OBIS_CODE));
        verify(spiedService).findFirstOnDevice(device);
    }

    @Test
    @Transactional
    public void testProtocolLoadProfileIdentifierById() throws Exception {
        LoadProfileService spiedService = spy(loadProfileService);

        spiedService.findByIdentifier(new com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierById(1L, LOADPROFILE_OBIS_CODE, deviceIdentifier));
        verify(spiedService).findById(1L);
    }

    @Test
    @Transactional
    public void testProtocolLoadProfileIdentifierByObisCodeAndDevice() throws Exception {
        LoadProfileServiceImpl spiedService = spy(loadProfileService);

        spiedService.findByIdentifier(new com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierByObisCodeAndDevice(LOADPROFILE_OBIS_CODE, deviceIdentifier));
        verify(spiedService).findByDeviceAndObisCode(device, LOADPROFILE_OBIS_CODE);
    }

    @Test
    @Transactional
    public void testProtocolLoadProfileIdentifierFirstOnDevice() throws Exception {
        LoadProfileServiceImpl spiedService = spy(loadProfileService);

        spiedService.findByIdentifier(new FirstLoadProfileOnDevice(deviceIdentifier, LOADPROFILE_OBIS_CODE));
        verify(spiedService).findFirstOnDevice(device);
    }
}
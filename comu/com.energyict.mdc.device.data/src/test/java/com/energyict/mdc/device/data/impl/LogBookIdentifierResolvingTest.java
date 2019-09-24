/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LogBook;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.identifiers.DeviceIdentifierForAlreadyKnownDevice;
import com.energyict.mdc.identifiers.LogBookIdentifierByDeviceAndObisCode;
import com.energyict.mdc.identifiers.LogBookIdentifierById;
import com.energyict.mdc.identifiers.LogBookIdentifierForAlreadyKnowLogBook;
import com.energyict.mdc.identifiers.LogBookIdentifierByObisCodeAndDevice;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.Introspector;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;

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
 * Tests the resolving of all different {@link LogBookIdentifier}s to actual load profiles.
 * This should ensure the resolving of the different {@link Introspector}s (based on textual type name)
 * matches the definition of the {@link Introspector} given in the different {@link LogBookIdentifier}s.
 *
 * @author Stijn Vanhoorelbeke
 * @since 15.09.17 - 17:22
 */
@RunWith(MockitoJUnitRunner.class)
public class LogBookIdentifierResolvingTest extends PersistenceIntegrationTest {

    private static final ObisCode LOGBOOK_OBIS_CODE = ObisCode.fromString("0.0.96.1.0.255");

    private static LogBookServiceImpl logBookService;

    Device device;

    DeviceIdentifier deviceIdentifier;

    @BeforeClass
    public static void setUpClass() throws Exception {
        logBookService = new LogBookServiceImpl(inMemoryPersistence.getDeviceDataModelService());
    }

    @Before
    public void setUp() throws Exception {
        device = createDevice();
        deviceIdentifier = new DeviceIdentifierForAlreadyKnownDevice(device.getId(), device.getmRID());
    }

    private Device createDevice() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "test", "testMRID", inMemoryPersistence.getClock().instant());
        device.save();
        return device;
    }

    @Test
    @Transactional
    public void testMatchingIntrospectorResolvingForAllKnownLogBookIdentifierImplementations() throws Exception {
        Reflections reflections = new Reflections("com.energyict");
        List<Class<? extends LogBookIdentifier>> identifierClasses = new ArrayList<>(reflections.getSubTypesOf(LogBookIdentifier.class));
        for (Class<? extends LogBookIdentifier> aClass : identifierClasses) {
            Introspector introspector = aClass.newInstance().forIntrospection();
            Optional<LogBookServiceImpl.IntrospectorTypes> matchingIntrospectorType = LogBookServiceImpl.IntrospectorTypes.forName(introspector.getTypeName());
            assertThat(matchingIntrospectorType)
                    .as("No matching introspector found in LogBookServiceImpl.IntrospectorTypes for type " + introspector.getTypeName() +
                            ". Please add corresponding entry and make sure it is used inside LogBookServiceImpl#doFind.")
                    .isPresent();
            assertThat(introspector.getRoles()).containsOnlyElementsOf(matchingIntrospectorType.get().getRoles()); // If not present, than above assert should already have failed
        }
    }

    @Test
    @Transactional
    public void testDeviceDataLogBookIdentifierById() throws Exception {
        LogBookService spiedService = spy(logBookService);
        spiedService.findByIdentifier(new LogBookIdentifierById(1L, LOGBOOK_OBIS_CODE, deviceIdentifier));
        verify(spiedService).findById(1L);
    }

    @Test
    @Transactional
    public void testDeviceDataLogBookIdentifierByDeviceAndObisCode() throws Exception {
        LogBookServiceImpl spiedService = spy(logBookService);
        spiedService.findByIdentifier(new LogBookIdentifierByDeviceAndObisCode(deviceIdentifier, LOGBOOK_OBIS_CODE));
        verify(spiedService).findByDeviceAndObisCode(device, LOGBOOK_OBIS_CODE);
    }

    @Test
    @Transactional
    public void testProtocolLogBookIdentifierByObisCodeAndDevice() throws Exception {
        LogBookServiceImpl spiedService = spy(logBookService);

        spiedService.findByIdentifier(new LogBookIdentifierByObisCodeAndDevice(deviceIdentifier, LOGBOOK_OBIS_CODE));
        verify(spiedService).findByDeviceAndObisCode(device, LOGBOOK_OBIS_CODE);
    }
}
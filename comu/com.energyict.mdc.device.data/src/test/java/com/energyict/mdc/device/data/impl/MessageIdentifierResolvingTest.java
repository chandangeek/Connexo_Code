/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.identifiers.DeviceIdentifierForAlreadyKnownDevice;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.Introspector;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import com.energyict.mdc.identifiers.DeviceMessageIdentifierByDeviceAndProtocolInfoParts;
import com.energyict.mdc.identifiers.DeviceMessageIdentifierById;
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
 * Tests the resolving of all different {@link MessageIdentifier}s to actual load profiles.
 * This should ensure the resolving of the different {@link Introspector}s (based on textual type name)
 * matches the definition of the {@link Introspector} given in the different {@link MessageIdentifier}s.
 *
 * @author Stijn Vanhoorelbeke
 * @since 15.09.17 - 17:22
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageIdentifierResolvingTest extends PersistenceIntegrationTest {

    private static DeviceMessageServiceImpl deviceMessageService;

    Device device;

    DeviceIdentifier deviceIdentifier;

    @BeforeClass
    public static void setUpClass() throws Exception {
        deviceMessageService = new DeviceMessageServiceImpl(
                inMemoryPersistence.getDeviceDataModelService(),
                inMemoryPersistence.getThreadPrincipalService(),
                inMemoryPersistence.getMeteringGroupsService(),
                inMemoryPersistence.getClock());
    }

    @Before
    public void setUp() throws Exception {
        device = spy(createDevice());
        deviceIdentifier = new DeviceIdentifierForAlreadyKnownDevice(device.getId(), device.getmRID());
    }

    private Device createDevice() {
        Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "test", "testMRID", inMemoryPersistence.getClock().instant());
        device.save();
        return device;
    }

    @Test
    @Transactional
    public void testMatchingIntrospectorResolvingForAllKnownDeviceMessageIdentifierImplementations() throws Exception {
        Reflections reflections = new Reflections("com.energyict");
        List<Class<? extends MessageIdentifier>> identifierClasses = new ArrayList<>(reflections.getSubTypesOf(MessageIdentifier.class));
        for (Class<? extends MessageIdentifier> aClass : identifierClasses) {
            Introspector introspector = aClass.newInstance().forIntrospection();
            Optional<DeviceMessageServiceImpl.IntrospectorTypes> matchingIntrospectorType = DeviceMessageServiceImpl.IntrospectorTypes.forName(introspector.getTypeName());
            assertThat(matchingIntrospectorType)
                    .as("No matching introspector found in DeviceMessageServiceImpl.IntrospectorTypes for type " + introspector.getTypeName() +
                            ". Please add corresponding entry and make sure it is used inside DeviceMessageServiceImpl#find.")
                    .isPresent();
            assertThat(introspector.getRoles()).containsOnlyElementsOf(matchingIntrospectorType.get().getRoles()); // If not present, than above assert should already have failed
        }
    }

    @Test
    @Transactional
    public void testProtocolDeviceMessageIdentifierById() throws Exception {
        DeviceMessageService spiedService = spy(deviceMessageService);

        spiedService.findDeviceMessageByIdentifier(new DeviceMessageIdentifierById(1, deviceIdentifier));
        verify(spiedService).findDeviceMessageById(1L);
    }

    @Test
    @Transactional
    public void testProtocolDeviceMessageIdentifierByDeviceAndProtocolInfoParts() throws Exception {
        DeviceMessageServiceImpl spiedService = spy(deviceMessageService);
        spiedService.findDeviceMessageByIdentifier(new DeviceMessageIdentifierByDeviceAndProtocolInfoParts(deviceIdentifier, "part_A", "part_B"));
        verify(spiedService).findByDeviceAndProtocolInfoParts(device, "part_A", "part_B");
    }
}
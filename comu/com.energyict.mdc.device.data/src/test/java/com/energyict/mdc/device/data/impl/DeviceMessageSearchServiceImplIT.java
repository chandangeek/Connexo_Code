/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageQueryFilter;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class DeviceMessageSearchServiceImplIT extends PersistenceIntegrationTest {

    private static final String DEVICE_NAME = "MyUniqueName";
    private static final TimeZone testDefaultTimeZone = TimeZone.getTimeZone("Canada/East-Saskatchewan");
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @BeforeClass
    public static void setup() {
        try (TransactionContext context = getTransactionService().getContext()) {
            deviceProtocolPluggableClass = inMemoryPersistence.getProtocolPluggableService().newDeviceProtocolPluggableClass("MyTestProtocol", TestProtocol.class.getName());
            deviceProtocolPluggableClass.save();
            context.commit();
        }
    }

    @After
    // MultiplierType is a cached object - make sure the cache is cleared after each test
    public void clearCache() {
        inMemoryPersistence.getDataModel().getInstance(OrmService.class).invalidateCache("MTR", "MTR_MULTIPLIERTYPE");
    }

    private Device createSimpleDeviceWithName(String name) {
        return createSimpleDeviceWithName(name, inMemoryPersistence.getClock().instant());
    }

    private Device createSimpleDeviceWithName(String name, Instant start) {
        return inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, name, start);
    }

    private EnumeratedEndDeviceGroup createDeviceGroup(Device device, AmrSystem amrSystem, String name) {
        return inMemoryPersistence
                .getMeteringGroupsService()
                .createEnumeratedEndDeviceGroup(amrSystem.findMeter("" + device.getId()).get())
                .at(inMemoryPersistence.getClock().instant())
                .setName(name)
                .create();
    }


    @Test
    @Transactional
    public void successfulCreateTest() {
        Device device = createSimpleDeviceWithName(DEVICE_NAME);

        assertThat(device).isNotNull();
        assertThat(device.getId()).isGreaterThan(0L);
        assertThat(device.getName()).isEqualTo(DEVICE_NAME);
        assertThat(device.getSerialNumber()).isNullOrEmpty();
    }


    @Test
    @Transactional
    public void selectDeviceMessagesByDeviceGroups() throws Exception {
        Device device1 = createSimpleDeviceWithName("dev1");
        Device device2 = createSimpleDeviceWithName("dev2");
        Device device3 = createSimpleDeviceWithName("dev3");
        Device device4 = createSimpleDeviceWithName("dev4");
        AmrSystem amrSystem = inMemoryPersistence.getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId()).orElseThrow(() -> new IllegalStateException("ARM not found"));

        EnumeratedEndDeviceGroup deviceGroup1 = createDeviceGroup(device1, amrSystem, "group1");
        EnumeratedEndDeviceGroup deviceGroup2 = createDeviceGroup(device2, amrSystem, "group2");
        EnumeratedEndDeviceGroup deviceGroup3 = createDeviceGroup(device3, amrSystem, "group3");
        EnumeratedEndDeviceGroup deviceGroup4 = createDeviceGroup(device4, amrSystem, "group4");

        DeviceMessageId deviceMessageId = DeviceMessageId.CONTACTOR_CLOSE;
        DeviceMessage deviceMessage1 = device1.newDeviceMessage(deviceMessageId).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage2 = device2.newDeviceMessage(deviceMessageId).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage3 = device3.newDeviceMessage(deviceMessageId).setReleaseDate(inMemoryPersistence.getClock().instant()).add();
        DeviceMessage deviceMessage4 = device4.newDeviceMessage(deviceMessageId).setReleaseDate(inMemoryPersistence.getClock().instant()).add();

        DeviceMessageQueryFilter deviceMessageQueryFilter = new DeviceMessageQueryFilter() {

            @Override
            public Collection<EndDeviceGroup> getDeviceGroups() {
                return Arrays.asList(deviceGroup1, deviceGroup4);
            }

        };

        List<DeviceMessage> deviceMessages = inMemoryPersistence.getDeviceMessageService()
                .findDeviceMessagesByFilter(deviceMessageQueryFilter)
                .find();
        assertThat(deviceMessages).hasSize(2);
        assertThat(deviceMessages.stream().map(DeviceMessage::getDevice).map(Device.class::cast).collect(Collectors.toList())).contains(device1, device4);
        assertThat(deviceMessages.stream().map(DeviceMessage::getDevice).map(Device.class::cast).collect(Collectors.toList())).doesNotContain(device2, device3);
    }

}

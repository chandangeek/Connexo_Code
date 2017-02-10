/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Subquery;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnumeratedEndDeviceGroupImplIT {

    private static final String ED_NAME = " ( ";
    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(UserService.class).toInstance(mock(UserService.class));
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @BeforeClass
    public static void setUp() {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new IdsModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new MeteringModule(),
                new BasicPropertiesModule(),
                new TimeModule(),
                new MeteringGroupsModule(),
                new SearchModule(),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new NlsModule(),
                new FiniteStateMachineModule(),
                new CalendarModule(),
                new DataVaultModule(),
                new CustomPropertySetsModule()
        );
        injector.getInstance(TransactionService.class).execute(() -> {
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringGroupsService.class);
            return null;
        });
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Rule
    public TransactionalRule transactional = new TransactionalRule(injector.getInstance(TransactionService.class));
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Test
    @Transactional
    public void testPersistence() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        EndDevice endDevice = meteringService.findAmrSystem(1).get().newMeter("1", ED_NAME).create();

        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        meteringGroupsService.createEnumeratedEndDeviceGroup(endDevice)
                .at(Instant.EPOCH)
                .setName("Mine")
                .setMRID("mine")
                .create();

        Optional<EndDeviceGroup> found = meteringGroupsService.findEndDeviceGroup("mine");
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get()).isInstanceOf(EnumeratedEndDeviceGroup.class);
        EnumeratedEndDeviceGroup group = (EnumeratedEndDeviceGroup) found.get();
        List<EndDevice> members = group.getMembers(ZonedDateTime.of(2014, 1, 23, 14, 54, 0, 0, ZoneId.systemDefault()).toInstant());
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getId()).isEqualTo(endDevice.getId());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.DUPLICATE_NAME + "}")
    public void testPersistenceDuplicateName() {
        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        meteringGroupsService.createEnumeratedEndDeviceGroup().setName("Mine").setMRID("mine").create();
        meteringGroupsService.createEnumeratedEndDeviceGroup().setName("Mine").setMRID("mine").create();
    }

    @Test
    @Transactional
    public void findEndDeviceContainmentWithoutGroups() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        EndDevice endDevice = meteringService.findAmrSystem(1).get().newMeter("1", ED_NAME).create();

        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);

        // Business method
        List<EnumeratedEndDeviceGroup> deviceGroups = meteringGroupsService.findEnumeratedEndDeviceGroupsContaining(endDevice);

        // Asserts
        assertThat(deviceGroups).isEmpty();
    }

    @Test
    @Transactional
    public void findEndDeviceContainmentWithMultipleGroupsThatDoNotContainTheDevice() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        EndDevice endDevice = meteringService.findAmrSystem(1).get().newMeter("1", ED_NAME).create();
        EndDevice otherDevice = meteringService.findAmrSystem(1).get().newMeter("2", "OTHER").create();

        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        meteringGroupsService.createEnumeratedEndDeviceGroup(otherDevice)
                .setName("First")
                .at(Instant.EPOCH)
                .setMRID("first")
                .create();
        meteringGroupsService.createEnumeratedEndDeviceGroup(otherDevice)
                .setName("Second")
                .at(Instant.EPOCH)
                .setMRID("second")
                .create();

        // Business method
        List<EnumeratedEndDeviceGroup> deviceGroups = meteringGroupsService.findEnumeratedEndDeviceGroupsContaining(endDevice);

        // Asserts
        assertThat(deviceGroups).isEmpty();
    }

    @Test
    @Transactional
    public void findEndDeviceContainmentWithGroupContainingTheDeviceInThePast() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        EndDevice endDevice = meteringService.findAmrSystem(1).get().newMeter("1", ED_NAME).create();

        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup(endDevice)
                .setName("Mine")
                .setMRID("mine")
                .at(Instant.EPOCH)
                .create();
        enumeratedEndDeviceGroup.endMembership(endDevice, Instant.ofEpochMilli(86400L));
        enumeratedEndDeviceGroup.update();

        // Business method
        List<EnumeratedEndDeviceGroup> deviceGroups = meteringGroupsService.findEnumeratedEndDeviceGroupsContaining(endDevice);

        // Asserts
        assertThat(deviceGroups).isEmpty();
    }

    @Test
    @Transactional
    public void findEndDeviceContainment() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        EndDevice endDevice = meteringService.findAmrSystem(1).get().newMeter("1", ED_NAME).create();
        EndDevice otherDevice = meteringService.findAmrSystem(1).get().newMeter("2", "OTHER").create();

        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        EnumeratedEndDeviceGroup expectedGroup = meteringGroupsService.createEnumeratedEndDeviceGroup(endDevice)
                .setName("First")
                .setMRID("first")
                .at(Instant.EPOCH)
                .create();
        meteringGroupsService.createEnumeratedEndDeviceGroup(otherDevice)
                .setName("Second")
                .setMRID("second")
                .at(Instant.EPOCH)
                .create();

        // Business method
        List<EnumeratedEndDeviceGroup> deviceGroups = meteringGroupsService.findEnumeratedEndDeviceGroupsContaining(endDevice);

        // Asserts
        assertThat(deviceGroups).hasSize(1);
        assertThat(deviceGroups.get(0).getId()).isEqualTo(expectedGroup.getId());
    }

    @Test
    @Transactional
    public void testAmrIdQuery() {
        int NUMBER_OF_DEVICES_IN_GROUP = 24;
        List<EndDevice> endDevices = new ArrayList<>();
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        AmrSystem MDC = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).orElseThrow(IllegalStateException::new);
        AmrSystem EAMS = meteringService.findAmrSystem(KnownAmrSystem.ENERGY_AXIS.getId()).orElseThrow(IllegalStateException::new);
        for (int i = 0; i < 2 * NUMBER_OF_DEVICES_IN_GROUP; i++) {
            EndDevice endDevice = (i % 2 == 0 ? MDC : EAMS).newMeter("" + i, "Name" + i).create();
            endDevices.add(endDevice);
        }

        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        EndDevice[] list = new EndDevice[NUMBER_OF_DEVICES_IN_GROUP];
        for (int i = 0; i < NUMBER_OF_DEVICES_IN_GROUP; i++) {
            EndDevice endDevice = endDevices.get(i);
            list[i] = endDevice;
        }

        meteringGroupsService.createEnumeratedEndDeviceGroup(list)
                .at(Instant.EPOCH)
                .setName("Mine")
                .setMRID("mine")
                .create();

        Optional<EndDeviceGroup> found = meteringGroupsService.findEndDeviceGroup("mine");
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get()).isInstanceOf(EnumeratedEndDeviceGroup.class);
        EnumeratedEndDeviceGroup group = (EnumeratedEndDeviceGroup) found.get();

        // get all
        Subquery subQuery = group.getAmrIdSubQuery();
        List<EndDevice> deviceList = meteringService.getEndDeviceQuery().select(ListOperator.IN.contains(subQuery, "amrId"));
        assertThat(deviceList).hasSize(NUMBER_OF_DEVICES_IN_GROUP);

        subQuery = group.getAmrIdSubQuery(MDC);
        deviceList = meteringService.getEndDeviceQuery().select(ListOperator.IN.contains(subQuery, "amrId"));
        assertThat(deviceList).hasSize(NUMBER_OF_DEVICES_IN_GROUP / 2);
    }

    @Test
    @Transactional
    public void testGetDevicesPagination() {
        int NUMBER_OF_DEVICES_IN_GROUP = 24;
        List<EndDevice> endDevices = new ArrayList<>();
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        AmrSystem MDC = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).orElseThrow(IllegalStateException::new);
        for (int i = NUMBER_OF_DEVICES_IN_GROUP; i > 0; i--) {
            EndDevice endDevice = MDC.newMeter("" + i, "Name" + i).create();
            endDevices.add(endDevice);
        }

        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        EndDevice[] list = new EndDevice[NUMBER_OF_DEVICES_IN_GROUP];
        for (int i = 0; i < NUMBER_OF_DEVICES_IN_GROUP; i++) {
            EndDevice endDevice = endDevices.get(i);
            list[i] = endDevice;
        }

        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup(list)
                .setName("Mine")
                .setMRID("STATIC DEVICE GROUP")
                .at(Instant.now())
                .create();
        enumeratedEndDeviceGroup.endMembership(endDevices.get(23), Instant.now());
        enumeratedEndDeviceGroup.update();

        Optional<EndDeviceGroup> optDeviceGroup = meteringGroupsService.findEndDeviceGroup("STATIC DEVICE GROUP");
        assertThat(optDeviceGroup.isPresent()).isTrue();

        EndDeviceGroup group = optDeviceGroup.get();
        assertThat(group.isMember(endDevices.get(1), Instant.now())).isTrue();
        assertThat(group.isMember(endDevices.get(23), Instant.now())).isFalse();
        //page 1
        List<EndDevice> devicesPage_1 = group.getMembers(Instant.now(), 0, 10);
        assertThat(devicesPage_1).hasSize(11);
        assertThat(devicesPage_1).isSortedAccordingTo(Comparator.comparing(EndDevice::getName, String.CASE_INSENSITIVE_ORDER));
        assertThat(devicesPage_1.get(0).getName()).isEqualTo("Name10");
        //page 2
        List<EndDevice> devicesPage_2 = group.getMembers(Instant.now(), 10, 10);
        assertThat(devicesPage_2).hasSize(11);
        assertThat(devicesPage_2).isSortedAccordingTo(Comparator.comparing(EndDevice::getName, String.CASE_INSENSITIVE_ORDER));
        assertThat(devicesPage_2.get(0).getName()).isEqualTo("Name2");
        //page 3
        List<EndDevice> devicesPage_3 = group.getMembers(Instant.now(), 20, 100);
        assertThat(devicesPage_3).hasSize(3);
    }

    @Test
    @Transactional
    public void testEndDeviceDeletion() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        MeteringGroupsService meteringGroupsService = injector.getInstance(MeteringGroupsService.class);
        AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).orElseThrow(IllegalStateException::new);
        EndDevice endDevice = amrSystem.createEndDevice("amrId", "name");
        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup(endDevice).setName("test").create();
        Instant activeMemberTime = Instant.now();
        assertThat(enumeratedEndDeviceGroup.getMemberCount(activeMemberTime)).isEqualTo(1);
        endDevice.makeObsolete();

        EndDeviceDeletionEventHandler endDeviceDeletionEventHandler = injector.getInstance(EndDeviceDeletionEventHandler.class);
        LocalEvent endDeviceDeletionEvent = mock(LocalEvent.class);
        when(endDeviceDeletionEvent.getSource()).thenReturn(endDevice);

        // Business method
        endDeviceDeletionEventHandler.handle(endDeviceDeletionEvent);

        // Assert
        enumeratedEndDeviceGroup = meteringGroupsService.findEnumeratedEndDeviceGroup(enumeratedEndDeviceGroup.getId()).get();
        assertThat(enumeratedEndDeviceGroup.getMemberCount(Instant.now())).isEqualTo(0);
        assertThat(enumeratedEndDeviceGroup.getMembers(Instant.now())).isEmpty();
        assertThat(enumeratedEndDeviceGroup.getMemberCount(activeMemberTime)).isEqualTo(1);
        assertThat(enumeratedEndDeviceGroup.getMembers(activeMemberTime)).contains(endDevice);
    }
}

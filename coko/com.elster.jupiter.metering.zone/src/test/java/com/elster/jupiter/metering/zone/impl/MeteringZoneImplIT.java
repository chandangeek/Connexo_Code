/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.Zone;
import com.elster.jupiter.metering.zone.ZoneType;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;

import java.util.Dictionary;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeteringZoneImplIT extends BaseZoneIT {
    private static final String ZONE_NAME = "ZoneName";
    private static final String APPLICATION = "APPNAME";
    private static final String ZONE_TYPE_NAME = "ZoneTypeName";
    private ZoneType zoneType;

    @Before
    public void init() {
        super.init();
        zoneType = createZoneType(meteringZoneService);
    }

    @Test
    @Transactional
    public void testSave() {
        meteringZoneService.newZoneBuilder()
                .withName(ZONE_NAME)
                .withZoneType(zoneType)
                .create();

        Finder<Zone> finder = meteringZoneService.getZones(APPLICATION, meteringZoneService.newZoneFilter());

        assertThat(finder.stream().map(Zone::getName).findFirst().get()).isEqualTo(ZONE_NAME);
        assertThat(finder.stream().map(Zone::getApplication).findFirst().get()).isEqualTo(APPLICATION);
        assertThat(finder.stream().map(zone -> zone.getZoneType().getName()).findFirst().get()).isEqualTo(ZONE_TYPE_NAME);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.ZONE_NAME_NOT_UNIQUE + "}")
    public void testSaveWithDuplicateName() {
        meteringZoneService.newZoneBuilder()
                .withName(ZONE_NAME)
                .withZoneType(zoneType)
                .create();
        meteringZoneService.newZoneBuilder()
                .withName(ZONE_NAME)
                .withZoneType(zoneType)
                .create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    public void testSaveWithEmptyName() {
        meteringZoneService.newZoneBuilder()
                .withName("")
                .withZoneType(zoneType)
                .create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    public void testSaveWithLargeName() {
        meteringZoneService.newZoneBuilder()
                .withName("123456789012345678901234567890123456789012345678901234567890123456789012345678901")
                .withZoneType(zoneType)
                .create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{" + MessageSeeds.Constants.ZONE_NAME_REQUIRED + "}")
    public void testSaveWithNULLName() {
        meteringZoneService.newZoneBuilder()
                .withName(null)
                .withZoneType(zoneType)
                .create();
    }

    private ZoneType createZoneType(MeteringZoneService meteringZoneService) {
        return meteringZoneService
                .newZoneTypeBuilder()
                .withName(ZONE_TYPE_NAME)
                .withApplication(APPLICATION)
                .create();
    }
}

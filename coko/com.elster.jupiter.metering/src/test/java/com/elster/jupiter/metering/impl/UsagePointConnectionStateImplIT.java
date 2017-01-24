package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;

import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointConnectionStateImplIT {

    private static final Instant JANUARY_2014 = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant FEBRUARY_2014 = ZonedDateTime.of(2014, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule();

    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void testConnectionState() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        DataModel dataModel = meteringService.getDataModel();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("name", Instant.EPOCH).create();
        assertThat(dataModel.mapper(UsagePoint.class).find()).hasSize(1);

        //get connection state
        assertThat(usagePoint.getCurrentConnectionState().isPresent()).isFalse();

        //add connection state valid from 1 january 2014
        usagePoint.setConnectionState(ConnectionState.CONNECTED, JANUARY_2014);

        //get connection state
        assertThat(usagePoint.getCurrentConnectionState()).contains(ConnectionState.CONNECTED);

        //add connection state valid from 1 february 2014 (this closes the previous detail on this date)
        usagePoint.setConnectionState(ConnectionState.LOGICALLY_DISCONNECTED, FEBRUARY_2014);


        //get connection state
        assertThat(usagePoint.getCurrentConnectionState()).contains(ConnectionState.LOGICALLY_DISCONNECTED);
    }
}


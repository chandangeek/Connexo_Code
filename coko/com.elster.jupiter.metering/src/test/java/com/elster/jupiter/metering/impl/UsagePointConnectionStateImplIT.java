/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConnectionState;
import com.elster.jupiter.users.UserService;

import com.google.common.collect.Range;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointConnectionStateImplIT {

    private static final Instant JANUARY_2014 = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant FEBRUARY_2014 = ZonedDateTime.of(2014, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();

    private static final String UP_NAME = "UP000001";

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
    @Rule
    public ExpectedException exception = ExpectedException.none();

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
    public void noConnectionStateRightAfterUsagePointCreation() {
        UsagePoint usagePoint = createUsagePoint(UP_NAME, JANUARY_2014);

        // Business method
        Optional<UsagePointConnectionState> currentConnectionState = usagePoint.getCurrentConnectionState();

        // Asserts
        assertThat(currentConnectionState).isEmpty();
    }

    @Test
    @Transactional
    public void setConnectionStateOnUsagePointInstallationTime() {
        UsagePoint usagePoint = createUsagePoint(UP_NAME, JANUARY_2014);

        // Business method
        usagePoint.setConnectionState(ConnectionState.CONNECTED, JANUARY_2014);

        // Asserts
        usagePoint = getMeteringService().findUsagePointByName(UP_NAME).get();
        assertThat(usagePoint.getCurrentConnectionState()).isPresent();
        UsagePointConnectionState currentState = usagePoint.getCurrentConnectionState().get();
        assertThat(currentState.getConnectionState()).isEqualTo(ConnectionState.CONNECTED);
        assertThat(currentState.getRange()).isEqualTo(Range.atLeast(usagePoint.getInstallationTime()));
        assertThat(currentState.getUsagePoint()).isEqualTo(usagePoint);
    }

    @Test
    @Transactional
    public void setConnectionStateBeforeInstallationTime() {
        UsagePoint usagePoint = createUsagePoint(UP_NAME, JANUARY_2014);

        exception.expect(ConnectionStateChangeException.class);
        exception.expectMessage(MessageSeeds.CONNECTION_STATE_CHANGE_BEFORE_INSTALLATION_TIME.getDefaultFormat());

        // Business method
        usagePoint.setConnectionState(ConnectionState.CONNECTED, JANUARY_2014.minusMillis(1));
    }

    @Test
    @Transactional
    public void setConnectionStateOnTheDateOfLatestStateChange() {
        UsagePoint usagePoint = createUsagePoint(UP_NAME, JANUARY_2014);
        usagePoint.setConnectionState(ConnectionState.CONNECTED, JANUARY_2014);

        exception.expect(ConnectionStateChangeException.class);
        exception.expectMessage(MessageSeeds.CONNECTION_STATE_CHANGE_BEFORE_LATEST_CHANGE.getDefaultFormat());

        // Business method
        usagePoint.setConnectionState(ConnectionState.LOGICALLY_DISCONNECTED, JANUARY_2014);
    }

    @Test
    @Transactional
    public void setTheSameConnectionStateAsAlreadySet() {
        UsagePoint usagePoint = createUsagePoint(UP_NAME, JANUARY_2014);

        // Business method
        usagePoint.setConnectionState(ConnectionState.CONNECTED, JANUARY_2014);
        usagePoint.setConnectionState(ConnectionState.CONNECTED, JANUARY_2014);

        // Asserts
        usagePoint = getMeteringService().findUsagePointByName(UP_NAME).get();
        assertThat(usagePoint.getCurrentConnectionState()).isPresent();
        UsagePointConnectionState currentState = usagePoint.getCurrentConnectionState().get();
        assertThat(currentState.getConnectionState()).isEqualTo(ConnectionState.CONNECTED);
        assertThat(currentState.getRange()).isEqualTo(Range.atLeast(usagePoint.getInstallationTime()));
        assertThat(currentState.getUsagePoint()).isEqualTo(usagePoint);
    }

    @Test
    @Transactional
    public void setTheSameConnectionStateButWithDateAfterLatestChangeDate() {
        UsagePoint usagePoint = createUsagePoint(UP_NAME, JANUARY_2014);

        // Business method
        usagePoint.setConnectionState(ConnectionState.CONNECTED, JANUARY_2014);
        usagePoint.setConnectionState(ConnectionState.CONNECTED, FEBRUARY_2014);

        // Asserts
        usagePoint = getMeteringService().findUsagePointByName(UP_NAME).get();
        assertThat(usagePoint.getCurrentConnectionState()).isPresent();
        UsagePointConnectionState currentState = usagePoint.getCurrentConnectionState().get();
        assertThat(currentState.getConnectionState()).isEqualTo(ConnectionState.CONNECTED);
        assertThat(currentState.getRange()).isEqualTo(Range.atLeast(usagePoint.getInstallationTime()));
        assertThat(currentState.getUsagePoint()).isEqualTo(usagePoint);
    }

    @Test
    @Transactional
    public void setNewConnectionStateThatOverlapsWithLatestStateEffectiveClosedInterval() {
        UsagePoint usagePoint = createUsagePoint(UP_NAME, JANUARY_2014);
        usagePoint.setConnectionState(ConnectionState.CONNECTED, JANUARY_2014);
        usagePoint.setConnectionState(ConnectionState.PHYSICALLY_DISCONNECTED, FEBRUARY_2014);

        exception.expect(ConnectionStateChangeException.class);
        exception.expectMessage(MessageSeeds.CONNECTION_STATE_CHANGE_BEFORE_LATEST_CHANGE.getDefaultFormat());

        // Business method
        usagePoint.setConnectionState(ConnectionState.LOGICALLY_DISCONNECTED, FEBRUARY_2014);
    }

    private UsagePoint createUsagePoint(String name, Instant installationTime) {
        getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get()
                .newUsagePoint(name, installationTime).create();
        return getMeteringService().findUsagePointByName(name)
                .orElseThrow(() -> new IllegalStateException("Just created usage point could not be found"));
    }

    private ServerMeteringService getMeteringService() {
        return inMemoryBootstrapModule.getMeteringService();
    }
}


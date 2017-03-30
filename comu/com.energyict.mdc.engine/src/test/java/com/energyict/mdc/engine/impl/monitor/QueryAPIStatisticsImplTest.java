/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.config.ComServer;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.monitor.QueryAPIStatisticsImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-06 (22:49)
 */
@RunWith(MockitoJUnitRunner.class)
public class QueryAPIStatisticsImplTest {

    @Mock
    private ComServer comServer;

    @Before
    public void initializeMocks () {
        when(this.comServer.getName()).thenReturn(this.getClass().getSimpleName());
    }

    @Test
    public void testCompositeDataItemTypes () {
        QueryAPIStatisticsImpl queryAPIStatistics = new QueryAPIStatisticsImpl(this.comServer);

        // Business method
        CompositeData compositeData = queryAPIStatistics.toCompositeData();

        // Asserts
        assertThat(compositeData.getCompositeType().getType(QueryAPIStatisticsImpl.NUMBER_OF_CLIENTS_ITEM_NAME)).isNotNull();
        assertThat(compositeData.getCompositeType().getType(QueryAPIStatisticsImpl.CALL_STATISTICS_ITEM_NAME)).isNotNull();
    }

    @Test
    public void testCompositeDataItemValuesForInitialState () {
        QueryAPIStatisticsImpl queryAPIStatistics = new QueryAPIStatisticsImpl(this.comServer);

        // Business method
        CompositeData compositeData = queryAPIStatistics.toCompositeData();

        // Asserts
        assertThat(compositeData.get(QueryAPIStatisticsImpl.NUMBER_OF_CLIENTS_ITEM_NAME)).isEqualTo(0);
        assertThat(compositeData.get(QueryAPIStatisticsImpl.CALL_STATISTICS_ITEM_NAME)).isNotNull();
    }

    @Test
    public void testCompositeDataItemValues () {
        QueryAPIStatisticsImpl queryAPIStatistics = new QueryAPIStatisticsImpl(this.comServer);
        int expectedNumberOfClients = 23;
        queryAPIStatistics.setNumberOfClients(expectedNumberOfClients);
        int expectedNumberOfFailures = 31;
        queryAPIStatistics.setNumberOfFailures(expectedNumberOfFailures);
        // Register four calls
        queryAPIStatistics.callCompleted(23);
        queryAPIStatistics.callCompleted(31);
        queryAPIStatistics.callCompleted(59);
        queryAPIStatistics.callCompleted(97);
        int expectedNumberOfQueries = 4;

        // Business method
        CompositeData compositeData = queryAPIStatistics.toCompositeData();

        // Asserts
        assertThat(compositeData.get(QueryAPIStatisticsImpl.NUMBER_OF_CLIENTS_ITEM_NAME)).isEqualTo(expectedNumberOfClients);
        assertThat(compositeData.get(QueryAPIStatisticsImpl.NUMBER_OF_FAILURES_ITEM_NAME)).isEqualTo(expectedNumberOfFailures);
        Object callStatisticsValue = compositeData.get(QueryAPIStatisticsImpl.CALL_STATISTICS_ITEM_NAME);
        assertThat(callStatisticsValue).isInstanceOf(CompositeDataSupport.class);
        CompositeDataSupport callStatistics = (CompositeDataSupport) callStatisticsValue;
        assertThat(callStatistics.get("count")).isEqualTo(expectedNumberOfQueries);
        assertThat(callStatistics.get("max")).isEqualTo(97L);
        assertThat(callStatistics.get("min")).isEqualTo(23L);
        assertThat(callStatistics.get("avg")).isEqualTo(52L);
    }

    @Test
    public void testClientRegistered () {
        QueryAPIStatisticsImpl queryAPIStatistics = new QueryAPIStatisticsImpl(this.comServer);
        int currentNumberOfClients = queryAPIStatistics.getNumberOfClients();

        // Business method
        queryAPIStatistics.clientRegistered();

        // Asserts
        assertThat(queryAPIStatistics.getNumberOfClients()).isEqualTo(currentNumberOfClients + 1);
    }

    @Test
    public void testClientUnregistered () {
        QueryAPIStatisticsImpl queryAPIStatistics = new QueryAPIStatisticsImpl(this.comServer);
        queryAPIStatistics.clientRegistered();
        queryAPIStatistics.clientRegistered();
        queryAPIStatistics.clientRegistered();
        int currentNumberOfClients = queryAPIStatistics.getNumberOfClients();

        // Business method
        queryAPIStatistics.clientUnregistered();

        // Asserts
        assertThat(queryAPIStatistics.getNumberOfClients()).isEqualTo(currentNumberOfClients - 1);
    }

    @Test
    public void testReset () {
        QueryAPIStatisticsImpl queryAPIStatistics = new QueryAPIStatisticsImpl(this.comServer);
        queryAPIStatistics.clientRegistered();
        queryAPIStatistics.clientRegistered();
        queryAPIStatistics.clientRegistered();
        queryAPIStatistics.setNumberOfClients(23);
        queryAPIStatistics.setNumberOfFailures(31);
        queryAPIStatistics.callCompleted(23);
        queryAPIStatistics.callCompleted(31);
        queryAPIStatistics.callCompleted(59);
        queryAPIStatistics.callCompleted(97);
        int currentNumberOfClients = queryAPIStatistics.getNumberOfClients();

        // Business method
        queryAPIStatistics.reset();

        // Asserts
        assertThat(queryAPIStatistics.getNumberOfClients()).isEqualTo(currentNumberOfClients);
        assertThat(queryAPIStatistics.getNumberOfFailures()).isZero();
        assertThat(queryAPIStatistics.getCallStatistics().getCount()).isZero();
        assertThat(queryAPIStatistics.getCallStatistics().getMin()).isZero();
        assertThat(queryAPIStatistics.getCallStatistics().getMax()).isZero();
        assertThat(queryAPIStatistics.getCallStatistics().getAvg()).isZero();
    }

    @Test
    public void testSingleCallFailed () {
        QueryAPIStatisticsImpl queryAPIStatistics = new QueryAPIStatisticsImpl(this.comServer);

        // Business method
        int singleCallDuration = 23;
        queryAPIStatistics.callFailed(singleCallDuration);

        // Asserts
        assertThat(queryAPIStatistics.getNumberOfFailures()).isEqualTo(1);
        assertThat(queryAPIStatistics.getCallStatistics().getCount()).isEqualTo(1);
        assertThat(queryAPIStatistics.getCallStatistics().getMin()).isEqualTo(singleCallDuration);
        assertThat(queryAPIStatistics.getCallStatistics().getMax()).isEqualTo(singleCallDuration);
        assertThat(queryAPIStatistics.getCallStatistics().getAvg()).isEqualTo(singleCallDuration);
    }

    @Test
    public void testMultipleCallsFailed () {
        QueryAPIStatisticsImpl queryAPIStatistics = new QueryAPIStatisticsImpl(this.comServer);

        // Business method
        queryAPIStatistics.callFailed(23);
        queryAPIStatistics.callFailed(31);
        queryAPIStatistics.callFailed(59);
        queryAPIStatistics.callFailed(97);

        // Asserts
        assertThat(queryAPIStatistics.getNumberOfFailures()).isEqualTo(4);
        assertThat(queryAPIStatistics.getCallStatistics().getCount()).isEqualTo(4);
        assertThat(queryAPIStatistics.getCallStatistics().getMin()).isEqualTo(23L);
        assertThat(queryAPIStatistics.getCallStatistics().getMax()).isEqualTo(97L);
        assertThat(queryAPIStatistics.getCallStatistics().getAvg()).isEqualTo(52L);
    }

    @Test
    public void testVariatyOfCalls () {
        QueryAPIStatisticsImpl queryAPIStatistics = new QueryAPIStatisticsImpl(this.comServer);

        // Business method
        queryAPIStatistics.callCompleted(17);
        queryAPIStatistics.callCompleted(23);
        queryAPIStatistics.callFailed(31);
        queryAPIStatistics.callCompleted(59);
        queryAPIStatistics.callFailed(97);

        // Asserts
        assertThat(queryAPIStatistics.getNumberOfFailures()).isEqualTo(2);
        assertThat(queryAPIStatistics.getCallStatistics().getCount()).isEqualTo(5);
        assertThat(queryAPIStatistics.getCallStatistics().getMin()).isEqualTo(17L);
        assertThat(queryAPIStatistics.getCallStatistics().getMax()).isEqualTo(97L);
        assertThat(queryAPIStatistics.getCallStatistics().getAvg()).isEqualTo(45L);
    }

}
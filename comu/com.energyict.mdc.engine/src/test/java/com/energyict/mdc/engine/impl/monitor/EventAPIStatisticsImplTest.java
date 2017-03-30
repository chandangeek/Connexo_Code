/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.monitor;

import javax.management.openmbean.CompositeData;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.monitor.EventAPIStatisticsImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-06 (22:49)
 */
public class EventAPIStatisticsImplTest {

    @Test
    public void testCompositeDataItemTypes () {
        EventAPIStatisticsImpl operationalStatistics = new EventAPIStatisticsImpl();

        // Business method
        CompositeData compositeData = operationalStatistics.toCompositeData();

        // Asserts
        assertThat(compositeData.getCompositeType().getType(EventAPIStatisticsImpl.NUMBER_OF_CLIENTS_ITEM_NAME)).isNotNull();
        assertThat(compositeData.getCompositeType().getType(EventAPIStatisticsImpl.NUMBER_OF_EVENTS_ITEM_NAME)).isNotNull();
    }

    @Test
    public void testCompositeDataItemValuesForInitialState () {
        EventAPIStatisticsImpl eventAPIStatistics = new EventAPIStatisticsImpl();

        // Business method
        CompositeData compositeData = eventAPIStatistics.toCompositeData();

        // Asserts
        assertThat(compositeData.get(EventAPIStatisticsImpl.NUMBER_OF_CLIENTS_ITEM_NAME)).isEqualTo(0);
        assertThat(compositeData.get(EventAPIStatisticsImpl.NUMBER_OF_EVENTS_ITEM_NAME)).isEqualTo(0L);
    }

    @Test
    public void testCompositeDataItemValues () {
        EventAPIStatisticsImpl eventAPIStatistics = new EventAPIStatisticsImpl();
        int expectedNumberOfClients = 23;
        long expectedNumberOfEvents = 97;
        eventAPIStatistics.setNumberOfClients(expectedNumberOfClients);
        eventAPIStatistics.setNumberOfEvents(expectedNumberOfEvents);

        // Business method
        CompositeData compositeData = eventAPIStatistics.toCompositeData();

        // Asserts
        assertThat(compositeData.get(EventAPIStatisticsImpl.NUMBER_OF_CLIENTS_ITEM_NAME)).isEqualTo(expectedNumberOfClients);
        assertThat(compositeData.get(EventAPIStatisticsImpl.NUMBER_OF_EVENTS_ITEM_NAME)).isEqualTo(expectedNumberOfEvents);
    }

    @Test
    public void testEventWasPublished () {
        EventAPIStatisticsImpl eventAPIStatistics = new EventAPIStatisticsImpl();
        long currentNumberOfEvents = eventAPIStatistics.getNumberOfEvents();

        // Business method
        eventAPIStatistics.eventWasPublished();

        // Asserts
        assertThat(eventAPIStatistics.getNumberOfEvents()).isEqualTo(currentNumberOfEvents + 1);
    }

    @Test
    public void testClientRegistered () {
        EventAPIStatisticsImpl eventAPIStatistics = new EventAPIStatisticsImpl();
        int currentNumberOfClients = eventAPIStatistics.getNumberOfClients();

        // Business method
        eventAPIStatistics.clientRegistered();

        // Asserts
        assertThat(eventAPIStatistics.getNumberOfClients()).isEqualTo(currentNumberOfClients + 1);
    }

    @Test
    public void testClientUnregistered () {
        EventAPIStatisticsImpl eventAPIStatistics = new EventAPIStatisticsImpl();
        eventAPIStatistics.clientRegistered();
        eventAPIStatistics.clientRegistered();
        eventAPIStatistics.clientRegistered();
        int currentNumberOfClients = eventAPIStatistics.getNumberOfClients();

        // Business method
        eventAPIStatistics.clientUnregistered();

        // Asserts
        assertThat(eventAPIStatistics.getNumberOfClients()).isEqualTo(currentNumberOfClients - 1);
    }

    @Test
    public void testReset () {
        EventAPIStatisticsImpl eventAPIStatistics = new EventAPIStatisticsImpl();
        eventAPIStatistics.clientRegistered();
        eventAPIStatistics.clientRegistered();
        eventAPIStatistics.clientRegistered();
        int currentNumberOfClients = eventAPIStatistics.getNumberOfClients();
        eventAPIStatistics.eventWasPublished();
        eventAPIStatistics.eventWasPublished();
        eventAPIStatistics.eventWasPublished();

        // Business method
        eventAPIStatistics.reset();

        // Asserts
        assertThat(eventAPIStatistics.getNumberOfClients()).isEqualTo(currentNumberOfClients);
        assertThat(eventAPIStatistics.getNumberOfEvents()).isZero();
    }

}
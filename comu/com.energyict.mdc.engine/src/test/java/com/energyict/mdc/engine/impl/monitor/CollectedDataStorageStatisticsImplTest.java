/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.monitor;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.RunningComServer;

import javax.management.openmbean.CompositeData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.monitor.CollectedDataStorageStatisticsImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-06 (22:39)
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedDataStorageStatisticsImplTest {

    private static final int CAPACITY = 97; // I somehow like this prime number
    private static final int CURRENT_SIZE = 23; // Another prime number
    private static final int LOAD_PERCENTAGE = (CAPACITY * 100) / CURRENT_SIZE;
    private static final int NUMBER_OF_THREADS = 3;
    private static final int THREAD_PRIORITY = 7;

    @Mock
    private ComServer comServer;
    @Mock
    private RunningComServer runningComServer;

    @Before
    public void initializeMocks () {
        when(this.runningComServer.getComServer()).thenReturn(this.comServer);
        when(this.comServer.getChangesInterPollDelay()).thenReturn(new TimeDuration(5, TimeDuration.TimeUnit.MINUTES));
        when(this.comServer.getChangesInterPollDelay()).thenReturn(new TimeDuration(5, TimeDuration.TimeUnit.MINUTES));
        when(this.comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.WARN);
        when(this.comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.TRACE);
        when(this.runningComServer.getCollectedDataStorageCapacity()).thenReturn(CAPACITY);
        when(this.runningComServer.getCurrentCollectedDataStorageSize()).thenReturn(CURRENT_SIZE);
        when(this.runningComServer.getCurrentCollectedDataStorageLoadPercentage()).thenReturn(LOAD_PERCENTAGE);
        when(this.runningComServer.getNumberOfCollectedDataStorageThreads()).thenReturn(NUMBER_OF_THREADS);
        when(this.runningComServer.getCollectedDataStorageThreadPriority()).thenReturn(THREAD_PRIORITY);
        when(this.runningComServer.getAcquiredTokenThreadNames()).thenReturn(Thread.currentThread().getName());
    }

    @Test
    public void testCompositeDataItemTypes () {
        CollectedDataStorageStatisticsImpl collectedDataStorageStatistics = new CollectedDataStorageStatisticsImpl(this.runningComServer);

        // Business method
        CompositeData compositeData = collectedDataStorageStatistics.toCompositeData();

        // Asserts
        assertThat(compositeData.getCompositeType().getType(CollectedDataStorageStatisticsImpl.SIZE_ITEM_NAME)).isNotNull();
        assertThat(compositeData.getCompositeType().getType(CollectedDataStorageStatisticsImpl.CAPACITY_ITEM_NAME)).isNotNull();
        assertThat(compositeData.getCompositeType().getType(CollectedDataStorageStatisticsImpl.LOAD_ITEM_PERCENTAGE_NAME)).isNotNull();
        assertThat(compositeData.getCompositeType().getType(CollectedDataStorageStatisticsImpl.NUMBER_OF_THREADS_ITEM_NAME)).isNotNull();
        assertThat(compositeData.getCompositeType().getType(CollectedDataStorageStatisticsImpl.THREAD_PRIORITY_ITEM_NAME)).isNotNull();
        assertThat(compositeData.getCompositeType().getType(CollectedDataStorageStatisticsImpl.THREAD_NAMES_ITEM_NAME)).isNotNull();
    }

    @Test
    public void testCompositeDataItemValues () {
        CollectedDataStorageStatisticsImpl collectedDataStorageStatistics = new CollectedDataStorageStatisticsImpl(this.runningComServer);

        // Business method
        CompositeData compositeData = collectedDataStorageStatistics.toCompositeData();

        // Asserts
        assertThat(compositeData.get(CollectedDataStorageStatisticsImpl.SIZE_ITEM_NAME)).isEqualTo(CURRENT_SIZE);
        assertThat(compositeData.get(CollectedDataStorageStatisticsImpl.CAPACITY_ITEM_NAME)).isEqualTo(CAPACITY);
        assertThat(compositeData.get(CollectedDataStorageStatisticsImpl.LOAD_ITEM_PERCENTAGE_NAME)).isEqualTo(LOAD_PERCENTAGE);
        assertThat(compositeData.get(CollectedDataStorageStatisticsImpl.NUMBER_OF_THREADS_ITEM_NAME)).isEqualTo(NUMBER_OF_THREADS);
        assertThat(compositeData.get(CollectedDataStorageStatisticsImpl.THREAD_PRIORITY_ITEM_NAME)).isEqualTo(THREAD_PRIORITY);
        assertThat(compositeData.get(CollectedDataStorageStatisticsImpl.THREAD_NAMES_ITEM_NAME)).isNotNull();
    }

}
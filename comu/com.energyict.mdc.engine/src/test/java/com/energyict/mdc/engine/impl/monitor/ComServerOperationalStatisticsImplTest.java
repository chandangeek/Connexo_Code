package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.exceptions.MessageSeeds;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.model.ComServer;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Clock;
import org.joda.time.DateTime;

import javax.management.openmbean.CompositeData;
import java.util.Date;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.monitor.ComServerOperationalStatisticsImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-06 (22:14)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComServerOperationalStatisticsImplTest {

    @Mock
    private ComServer comServer;
    @Mock
    private RunningComServer runningComServer;
    @Mock
    private Clock clock;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void initializeMocks () {
        when(this.runningComServer.getComServer()).thenReturn(this.comServer);
        when(this.comServer.getChangesInterPollDelay()).thenReturn(new TimeDuration(5, TimeDuration.MINUTES));
        when(this.comServer.getChangesInterPollDelay()).thenReturn(new TimeDuration(5, TimeDuration.MINUTES));
        when(this.comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.WARN);
        when(this.comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.TRACE);
    }

    @Before
    public void setupThesaurus () {
        when(this.thesaurus.getString(eq(MessageSeeds.PRETTY_PRINT_TIMEDURATION_YEAR_SINGULAR.getKey()), anyString())).thenReturn("{0} year");
        when(this.thesaurus.getString(eq(MessageSeeds.PRETTY_PRINT_TIMEDURATION_YEAR_PLURAL.getKey()), anyString())).thenReturn("{0} years");
        when(this.thesaurus.getString(eq(MessageSeeds.PRETTY_PRINT_TIMEDURATION_MONTH_SINGULAR.getKey()), anyString())).thenReturn("{0} month");
        when(this.thesaurus.getString(eq(MessageSeeds.PRETTY_PRINT_TIMEDURATION_MONTH_PLURAL.getKey()), anyString())).thenReturn("{0} months");
        when(this.thesaurus.getString(eq(MessageSeeds.PRETTY_PRINT_TIMEDURATION_DAY_SINGULAR.getKey()), anyString())).thenReturn("{0} day");
        when(this.thesaurus.getString(eq(MessageSeeds.PRETTY_PRINT_TIMEDURATION_DAY_PLURAL.getKey()), anyString())).thenReturn("{0} days");
        when(this.thesaurus.getString(eq(MessageSeeds.PRETTY_PRINT_TIMEDURATION_HOUR_SINGULAR.getKey()), anyString())).thenReturn("{0} hour");
        when(this.thesaurus.getString(eq(MessageSeeds.PRETTY_PRINT_TIMEDURATION_HOUR_PLURAL.getKey()), anyString())).thenReturn("{0} hours");
        when(this.thesaurus.getString(eq(MessageSeeds.PRETTY_PRINT_TIMEDURATION_MINUTE_SINGULAR.getKey()), anyString())).thenReturn("{0} minute");
        when(this.thesaurus.getString(eq(MessageSeeds.PRETTY_PRINT_TIMEDURATION_MINUTE_PLURAL.getKey()), anyString())).thenReturn("{0} minutes");
        when(this.thesaurus.getString(eq(MessageSeeds.PRETTY_PRINT_TIMEDURATION_SECOND_SINGULAR.getKey()), anyString())).thenReturn("{0} second");
        when(this.thesaurus.getString(eq(MessageSeeds.PRETTY_PRINT_TIMEDURATION_SECOND_PLURAL.getKey()), anyString())).thenReturn("{0} seconds");
        when(this.thesaurus.getString(eq(MessageSeeds.PRETTY_PRINT_TIMEDURATION_SEPARATOR.getKey()), anyString())).thenReturn(", ");
        when(this.thesaurus.getString(eq(MessageSeeds.PRETTY_PRINT_TIMEDURATION_LAST_SEPARATOR.getKey()), anyString())).thenReturn(" and ");
    }

    @Test
    public void testCompositeDataItemTypes () {
        when(this.clock.now()).thenReturn(new Date());
        ComServerOperationalStatisticsImpl operationalStatistics = new ComServerOperationalStatisticsImpl(this.runningComServer, this.clock, this.thesaurus);

        // Business method
        CompositeData compositeData = operationalStatistics.toCompositeData();

        // Asserts
        assertThat(compositeData.getCompositeType().getType(ComServerOperationalStatisticsImpl.START_TIMESTAMP_ITEM_NAME)).isNotNull();
        assertThat(compositeData.getCompositeType().getType(ComServerOperationalStatisticsImpl.RUNNING_TIME_ITEM_NAME)).isNotNull();
        assertThat(compositeData.getCompositeType().getType(ComServerOperationalStatisticsImpl.CHANGES_INTERPOLL_DELAY_ITEM_NAME)).isNotNull();
        assertThat(compositeData.getCompositeType().getType(ComServerOperationalStatisticsImpl.LAST_CHECK_FOR_CHANGES_ITEM_NAME)).isNotNull();
        assertThat(compositeData.getCompositeType().getType(ComServerOperationalStatisticsImpl.SERVER_LOG_LEVEL_ITEM_NAME)).isNotNull();
        assertThat(compositeData.getCompositeType().getType(ComServerOperationalStatisticsImpl.COMMUNICATION_LOG_LEVEL_ITEM_NAME)).isNotNull();
    }

    @Test
    public void testCompositeDataItemValues () {
        Date startTimestamp = new DateTime(2013, 4, 6, 22, 23, 4, 0).toDate();
        Date now = new DateTime(2013, 4, 6, 23, 24, 5, 0).toDate();
        when(this.clock.now()).thenReturn(startTimestamp, now);
        ComServerOperationalStatisticsImpl operationalStatistics = new ComServerOperationalStatisticsImpl(this.runningComServer, this.clock, this.thesaurus);

        // Business method
        CompositeData compositeData = operationalStatistics.toCompositeData();

        // Asserts
        assertThat(compositeData.get(ComServerOperationalStatisticsImpl.START_TIMESTAMP_ITEM_NAME)).isEqualTo(startTimestamp);
        assertThat(compositeData.get(ComServerOperationalStatisticsImpl.RUNNING_TIME_ITEM_NAME)).isEqualTo("1 hour, 1 minute and 1 second");
        assertThat(compositeData.get(ComServerOperationalStatisticsImpl.CHANGES_INTERPOLL_DELAY_ITEM_NAME)).isEqualTo("5 minutes");
        assertThat(compositeData.get(ComServerOperationalStatisticsImpl.LAST_CHECK_FOR_CHANGES_ITEM_NAME)).isNull();
        assertThat(compositeData.get(ComServerOperationalStatisticsImpl.SERVER_LOG_LEVEL_ITEM_NAME)).isEqualTo(ComServer.LogLevel.WARN.toString());
        assertThat(compositeData.get(ComServerOperationalStatisticsImpl.COMMUNICATION_LOG_LEVEL_ITEM_NAME)).isEqualTo(ComServer.LogLevel.TRACE.toString());
    }

}
package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.comserver.scheduling.RunningComServer;
import com.energyict.comserver.time.Clocks;
import com.energyict.comserver.time.FrozenClock;
import com.energyict.comserver.time.PredefinedTickingClock;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.engine.impl.monitor.ComServerOperationalStatisticsImpl;
import com.energyict.mdc.engine.model.ComServer;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.management.openmbean.CompositeData;
import java.util.Calendar;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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

    @Before
    public void initializeMocks () {
        when(this.runningComServer.getComServer()).thenReturn(this.comServer);
        when(this.comServer.getChangesInterPollDelay()).thenReturn(new TimeDuration(5, TimeDuration.MINUTES));
        when(this.comServer.getChangesInterPollDelay()).thenReturn(new TimeDuration(5, TimeDuration.MINUTES));
        when(this.comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.WARN);
        when(this.comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.TRACE);
    }

    @After
    public void restoreTimeFactory () {
        Clocks.resetAll();
    }

    @Before
    public void mockEnvironmentTranslations () {
        UserEnvironment userEnvironment = mock(UserEnvironment.class);
        when(userEnvironment.getTranslation("PrettyPrintTimeDuration.year.singular")).thenReturn("{0} year");
        when(userEnvironment.getTranslation("PrettyPrintTimeDuration.month.singular")).thenReturn("{0} month");
        when(userEnvironment.getTranslation("PrettyPrintTimeDuration.day.singular")).thenReturn("{0} day");
        when(userEnvironment.getTranslation("PrettyPrintTimeDuration.hour.singular")).thenReturn("{0} hour");
        when(userEnvironment.getTranslation("PrettyPrintTimeDuration.minute.singular")).thenReturn("{0} minute");
        when(userEnvironment.getTranslation("PrettyPrintTimeDuration.second.singular")).thenReturn("{0} second");
        when(userEnvironment.getTranslation("PrettyPrintTimeDuration.year.plural")).thenReturn("{0} years");
        when(userEnvironment.getTranslation("PrettyPrintTimeDuration.month.plural")).thenReturn("{0} months");
        when(userEnvironment.getTranslation("PrettyPrintTimeDuration.day.plural")).thenReturn("{0} days");
        when(userEnvironment.getTranslation("PrettyPrintTimeDuration.hour.plural")).thenReturn("{0} hours");
        when(userEnvironment.getTranslation("PrettyPrintTimeDuration.minute.plural")).thenReturn("{0} minutes");
        when(userEnvironment.getTranslation("PrettyPrintTimeDuration.second.plural")).thenReturn("{0} seconds");
        when(userEnvironment.getTranslation("PrettyPrintTimeDuration.separator")).thenReturn(", ");
        when(userEnvironment.getTranslation("PrettyPrintTimeDuration.lastSeparator")).thenReturn(" and ");
        UserEnvironment.setDefault(userEnvironment);
    }

    @After
    public void restoreUserEnvironment () {
        UserEnvironment.setDefault(null);
    }

    @Test
    public void testCompositeDataItemTypes () {
        ComServerOperationalStatisticsImpl operationalStatistics = new ComServerOperationalStatisticsImpl(this.runningComServer);

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
        FrozenClock startTimestamp = FrozenClock.frozenOn(2013, Calendar.APRIL, 6, 22, 23, 4, 0);
        FrozenClock now = FrozenClock.frozenOn(2013, Calendar.APRIL, 6, 23, 24, 5, 0);
        PredefinedTickingClock clock = new PredefinedTickingClock(startTimestamp, now);
        Clocks.setAppServerClock(clock);
        ComServerOperationalStatisticsImpl operationalStatistics = new ComServerOperationalStatisticsImpl(this.runningComServer);

        // Business method
        CompositeData compositeData = operationalStatistics.toCompositeData();

        // Asserts
        assertThat(compositeData.get(ComServerOperationalStatisticsImpl.START_TIMESTAMP_ITEM_NAME)).isEqualTo(startTimestamp.now());
        assertThat(compositeData.get(ComServerOperationalStatisticsImpl.RUNNING_TIME_ITEM_NAME)).isEqualTo("1 hour, 1 minute and 1 second");
        assertThat(compositeData.get(ComServerOperationalStatisticsImpl.CHANGES_INTERPOLL_DELAY_ITEM_NAME)).isEqualTo("5 minutes");
        assertThat(compositeData.get(ComServerOperationalStatisticsImpl.LAST_CHECK_FOR_CHANGES_ITEM_NAME)).isNull();
        assertThat(compositeData.get(ComServerOperationalStatisticsImpl.SERVER_LOG_LEVEL_ITEM_NAME)).isEqualTo(ComServer.LogLevel.WARN.toString());
        assertThat(compositeData.get(ComServerOperationalStatisticsImpl.COMMUNICATION_LOG_LEVEL_ITEM_NAME)).isEqualTo(ComServer.LogLevel.TRACE.toString());
    }

}
package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.RunningComServer;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import org.joda.time.DateTime;

import javax.management.openmbean.CompositeData;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
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
        when(this.comServer.getChangesInterPollDelay()).thenReturn(new TimeDuration(5, TimeDuration.TimeUnit.MINUTES));
        when(this.comServer.getChangesInterPollDelay()).thenReturn(new TimeDuration(5, TimeDuration.TimeUnit.MINUTES));
        when(this.comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.WARN);
        when(this.comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.TRACE);
    }

    @Before
    public void setupThesaurus () {
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.YEAR_SINGULAR)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.YEAR_SINGULAR));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.YEAR_PLURAL)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.YEAR_PLURAL));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.MONTH_SINGULAR)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.MONTH_SINGULAR));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.MONTH_PLURAL)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.MONTH_PLURAL));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.DAY_SINGULAR)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.DAY_SINGULAR));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.DAY_PLURAL)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.DAY_PLURAL));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.HOUR_SINGULAR)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.HOUR_SINGULAR));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.HOUR_PLURAL)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.HOUR_PLURAL));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.MINUTE_SINGULAR)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.MINUTE_SINGULAR));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.MINUTE_PLURAL)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.MINUTE_PLURAL));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.SECOND_SINGULAR)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.SECOND_SINGULAR));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.SECOND_PLURAL)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.SECOND_PLURAL));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.SEPARATOR)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.SEPARATOR));
        when(this.thesaurus.getFormat(PrettyPrintTimeDurationTranslationKeys.LAST_SEPARATOR)).thenReturn(new SimpleNlsMessageFormat(PrettyPrintTimeDurationTranslationKeys.LAST_SEPARATOR));
    }

    @Test
    public void testCompositeDataItemTypes () {
        when(this.clock.instant()).thenReturn(Instant.now());
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
        when(this.clock.instant()).thenReturn(startTimestamp.toInstant(), now.toInstant());
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
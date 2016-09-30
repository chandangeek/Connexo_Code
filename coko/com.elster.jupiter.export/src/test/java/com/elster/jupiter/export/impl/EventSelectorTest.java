package com.elster.jupiter.export.impl;

import com.elster.jupiter.devtools.persistence.test.TransactionVerifier;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.EventDataExportStrategy;
import com.elster.jupiter.export.EventDataSelector;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterEventData;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.Range;

import java.text.MessageFormat;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventSelectorTest {

    private static final ZonedDateTime eventTime = ZonedDateTime.of(2012, 11, 22, 0, 5, 13, 0, ZoneId.systemDefault());

    private TransactionService transactionService = new TransactionVerifier();

    @Mock
    private DataExportService dataExportService;
    @Mock
    private IDataExportOccurrence occurrence;
    @Mock
    private DataModel dataModel;
    @Mock
    private EventDataSelector selector;
    @Mock
    private EndDeviceGroup endDeviceGroup;
    @Mock
    private EndDeviceMembership membership1, membership2;
    @Mock
    private EndDevice endDevice1, endDevice2;
    @Mock
    private EndDeviceEventRecord event1, event2, event3, event4, event5, event6;
    @Mock
    private EventDataExportStrategy eventStrategy;
    @Mock
    private DefaultSelectorOccurrence defaultSelectorOccurrence;
    @Mock
    private Clock clock;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setUp() {
        when(dataModel.getInstance(EventSelector.class)).thenAnswer(invocation -> new EventSelector(transactionService, dataExportService, clock, thesaurus));
        when(dataExportService.forRoot(anyString())).thenAnswer(invocation -> DefaultStructureMarker.createRoot(clock, (String) invocation.getArguments()[0]));
        when(selector.getEndDeviceGroup()).thenReturn(endDeviceGroup);
        when(endDeviceGroup.getMembers(any(Range.class))).thenReturn(Arrays.asList(membership1, membership2));
        when(membership1.getEndDevice()).thenReturn(endDevice1);
        when(membership2.getEndDevice()).thenReturn(endDevice2);
        when(endDevice1.getDeviceEvents(any())).thenReturn(Arrays.asList(event1, event2, event3));
        when(endDevice2.getDeviceEvents(any())).thenReturn(Arrays.asList(event4, event5, event6));
        when(event1.getEventTypeCode()).thenReturn("4.11.15.0");
        when(event2.getEventTypeCode()).thenReturn("4.12.15.0");
        when(event3.getEventTypeCode()).thenReturn("4.13.15.0");
        when(event4.getEventTypeCode()).thenReturn("3.14.15.0");
        when(event5.getEventTypeCode()).thenReturn("3.15.15.0");
        when(event6.getEventTypeCode()).thenReturn("3.16.15.0");
        when(event1.getCreatedDateTime()).thenReturn(eventTime.plusMinutes(1).toInstant());
        when(event2.getCreatedDateTime()).thenReturn(eventTime.plusMinutes(2).toInstant());
        when(event3.getCreatedDateTime()).thenReturn(eventTime.plusMinutes(3).toInstant());
        when(event4.getCreatedDateTime()).thenReturn(eventTime.plusMinutes(4).toInstant());
        when(event5.getCreatedDateTime()).thenReturn(eventTime.plusMinutes(5).toInstant());
        when(event6.getCreatedDateTime()).thenReturn(eventTime.plusMinutes(6).toInstant());
        when(endDevice1.getMRID()).thenReturn("MRID1");
        when(endDevice2.getMRID()).thenReturn("MRID2");
        when(selector.getEventStrategy()).thenReturn(eventStrategy);
        when(eventStrategy.isExportContinuousData()).thenReturn(false);
        when(selector.getFilterPredicate()).thenReturn(event -> true);
        when(occurrence.getDefaultSelectorOccurrence()).thenReturn(Optional.of(defaultSelectorOccurrence));
        Answer<NlsMessageFormat> formatAnswer = invocation -> {
            NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
            when(messageFormat.format(anyVararg())).thenAnswer(invocation1 -> {
                String defaultFormat = invocation.getArguments()[0] instanceof MessageSeed ? ((MessageSeed) invocation.getArguments()[0]).getDefaultFormat()
                        : ((TranslationKey) invocation.getArguments()[0]).getDefaultFormat();
                return MessageFormat.format(defaultFormat, invocation1.getArguments());
            });
            return messageFormat;
        };
        when(thesaurus.getFormat(any(MessageSeed.class))).thenAnswer(formatAnswer);
        when(thesaurus.getFormat(any(TranslationKey.class))).thenAnswer(formatAnswer);
        RelativePeriod exportPeriod = mock(RelativePeriod.class);
        when(selector.getExportPeriod()).thenReturn(exportPeriod);
    }

    @Test
    public void testSelection() {
        EventSelector eventSelector = EventSelector.from(dataModel, selector, Logger.getAnonymousLogger());

        Stream<ExportData> exportDataStream = eventSelector.selectData(occurrence);

        List<ExportData> exportDatas = exportDataStream.collect(Collectors.toList());

        assertThat(exportDatas).hasSize(2);

        assertThat(exportDatas.get(0).getStructureMarker()).isEqualTo(DefaultStructureMarker.createRoot(clock, "MRID1"));
        assertThat(exportDatas.get(0)).isInstanceOf(MeterEventData.class);

        {
            MeterEventData meterEventData = (MeterEventData) exportDatas.get(0);

            List<EndDeviceEvent> events = meterEventData.getMeterReading().getEvents();
            assertThat(events).hasSize(3);
            assertThat(events.get(0).getEventTypeCode()).isEqualTo("4.11.15.0");
            assertThat(events.get(0).getCreatedDateTime()).isEqualTo(eventTime.plusMinutes(1).toInstant());
            assertThat(events.get(1).getEventTypeCode()).isEqualTo("4.12.15.0");
            assertThat(events.get(1).getCreatedDateTime()).isEqualTo(eventTime.plusMinutes(2).toInstant());
            assertThat(events.get(2).getEventTypeCode()).isEqualTo("4.13.15.0");
            assertThat(events.get(2).getCreatedDateTime()).isEqualTo(eventTime.plusMinutes(3).toInstant());
        }

        assertThat(exportDatas.get(1).getStructureMarker()).isEqualTo(DefaultStructureMarker.createRoot(clock, "MRID2"));
        assertThat(exportDatas.get(1)).isInstanceOf(MeterEventData.class);
        {
            MeterEventData meterEventData = (MeterEventData) exportDatas.get(1);

            List<EndDeviceEvent> events = meterEventData.getMeterReading().getEvents();
            assertThat(events).hasSize(3);
            assertThat(events.get(0).getEventTypeCode()).isEqualTo("3.14.15.0");
            assertThat(events.get(0).getCreatedDateTime()).isEqualTo(eventTime.plusMinutes(4).toInstant());
            assertThat(events.get(1).getEventTypeCode()).isEqualTo("3.15.15.0");
            assertThat(events.get(1).getCreatedDateTime()).isEqualTo(eventTime.plusMinutes(5).toInstant());
            assertThat(events.get(2).getEventTypeCode()).isEqualTo("3.16.15.0");
            assertThat(events.get(2).getCreatedDateTime()).isEqualTo(eventTime.plusMinutes(6).toInstant());
        }
    }

    @Test
    public void testSelectionWithFilter() {
        when(selector.getFilterPredicate()).thenReturn(event -> Checks.is(event).in(event1, event3, event5));

        EventSelector eventSelector = EventSelector.from(dataModel, selector, Logger.getAnonymousLogger());

        Stream<ExportData> exportDataStream = eventSelector.selectData(occurrence);

        List<ExportData> exportDatas = exportDataStream.collect(Collectors.toList());

        assertThat(exportDatas).hasSize(2);

        assertThat(exportDatas.get(0).getStructureMarker()).isEqualTo(DefaultStructureMarker.createRoot(clock, "MRID1"));
        assertThat(exportDatas.get(0)).isInstanceOf(MeterEventData.class);

        {
            MeterEventData meterEventData = (MeterEventData) exportDatas.get(0);

            List<EndDeviceEvent> events = meterEventData.getMeterReading().getEvents();
            assertThat(events).hasSize(2);
            assertThat(events.get(0).getEventTypeCode()).isEqualTo("4.11.15.0");
            assertThat(events.get(0).getCreatedDateTime()).isEqualTo(eventTime.plusMinutes(1).toInstant());
            assertThat(events.get(1).getEventTypeCode()).isEqualTo("4.13.15.0");
            assertThat(events.get(1).getCreatedDateTime()).isEqualTo(eventTime.plusMinutes(3).toInstant());
        }

        assertThat(exportDatas.get(1).getStructureMarker()).isEqualTo(DefaultStructureMarker.createRoot(clock, "MRID2"));
        assertThat(exportDatas.get(1)).isInstanceOf(MeterEventData.class);
        {
            MeterEventData meterEventData = (MeterEventData) exportDatas.get(1);

            List<EndDeviceEvent> events = meterEventData.getMeterReading().getEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0).getEventTypeCode()).isEqualTo("3.15.15.0");
            assertThat(events.get(0).getCreatedDateTime()).isEqualTo(eventTime.plusMinutes(5).toInstant());
        }
    }
}

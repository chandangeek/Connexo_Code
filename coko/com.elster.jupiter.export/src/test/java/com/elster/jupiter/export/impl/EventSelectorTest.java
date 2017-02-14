/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.devtools.persistence.test.TransactionVerifier;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.EventDataExportStrategy;
import com.elster.jupiter.export.EventSelectorConfig;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterEventData;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.Membership;
import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Checks;

import com.google.common.collect.Range;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
    private EventSelectorConfig eventSelectorConfig;
    @Mock
    private EndDeviceGroup endDeviceGroup;
    @Mock
    private Membership<EndDevice> membership1, membership2;
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
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;

    @Before
    public void setUp() {
        when(dataModel.getInstance(EventSelector.class)).thenAnswer(invocation -> new EventSelector(transactionService, dataExportService, clock, thesaurus));
        when(dataExportService.forRoot(anyString())).thenAnswer(invocation -> DefaultStructureMarker.createRoot(clock, (String) invocation.getArguments()[0]));
        when(eventSelectorConfig.getEndDeviceGroup()).thenReturn(endDeviceGroup);
        when(endDeviceGroup.getMembers(any(Range.class))).thenReturn(Arrays.asList(membership1, membership2));
        when(membership1.getMember()).thenReturn(endDevice1);
        when(membership2.getMember()).thenReturn(endDevice2);
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
        when(endDevice1.getName()).thenReturn("Device1");
        when(endDevice2.getName()).thenReturn("Device2");
        when(eventSelectorConfig.getStrategy()).thenReturn(eventStrategy);
        when(eventStrategy.isExportContinuousData()).thenReturn(false);
        when(eventSelectorConfig.getFilterPredicate()).thenReturn(event -> true);
        when(occurrence.getDefaultSelectorOccurrence()).thenReturn(Optional.of(defaultSelectorOccurrence));
        RelativePeriod exportPeriod = mock(RelativePeriod.class);
        when(eventSelectorConfig.getExportPeriod()).thenReturn(exportPeriod);
    }

    @Test
    public void testSelection() {
        EventSelector eventSelector = EventSelector.from(dataModel, eventSelectorConfig, Logger.getAnonymousLogger());

        Stream<ExportData> exportDataStream = eventSelector.selectData(occurrence);

        List<ExportData> exportDatas = exportDataStream.collect(Collectors.toList());

        assertThat(exportDatas).hasSize(2);

        assertThat(exportDatas.get(0).getStructureMarker()).isEqualTo(DefaultStructureMarker.createRoot(clock, "MRID1").child("Device1"));
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

        assertThat(exportDatas.get(1).getStructureMarker()).isEqualTo(DefaultStructureMarker.createRoot(clock, "MRID2").child("Device2"));
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
        when(eventSelectorConfig.getFilterPredicate()).thenReturn(event -> Checks.is(event).in(event1, event3, event5));

        EventSelector eventSelector = EventSelector.from(dataModel, eventSelectorConfig, Logger.getAnonymousLogger());

        Stream<ExportData> exportDataStream = eventSelector.selectData(occurrence);

        List<ExportData> exportDatas = exportDataStream.collect(Collectors.toList());

        assertThat(exportDatas).hasSize(2);

        assertThat(exportDatas.get(0).getStructureMarker()).isEqualTo(DefaultStructureMarker.createRoot(clock, "MRID1").child("Device1"));
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

        assertThat(exportDatas.get(1).getStructureMarker()).isEqualTo(DefaultStructureMarker.createRoot(clock, "MRID2").child("Device2"));
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

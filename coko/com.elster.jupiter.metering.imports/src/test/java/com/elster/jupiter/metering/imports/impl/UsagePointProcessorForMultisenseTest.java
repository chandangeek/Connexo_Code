/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.imports.impl.properties.SupportedNumberFormat;
import com.elster.jupiter.metering.imports.impl.usagepoint.UsagePointsImporterFactory;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointProcessorForMultisenseTest {

    public static final String TIME_OF_USE_CALENDAR_NAME = "TOU-CAL-NAME";
    public static final String WORKFORCE_OF_USE_CALENDAR_NAME = "WORKFORCE-CAL-NAME";
    public static final String COMMANDS_OF_USE_CALENDAR_NAME = "COMMANDS-CAL-NAME";
    @Mock
    private Clock clock;
    @Mock
    private Logger logger;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private UsagePoint.UsedCalendars usedCalendars;
    @Mock
    private Calendar touCalendar;
    @Mock
    private Calendar workForceCalendar;
    @Mock
    private Calendar commandsCalendar;
    @Mock
    private CalendarService calendarService;
    @Mock
    private UsagePointBuilder usagePointBuilder;
    @Mock
    private UsagePointDetailBuilder usagePointDetailBuilder;
    @Mock
    private UsagePointDetail usagePointDetail;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private MeteringService meteringService;
    @Mock
    private MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private LicenseService licenseService;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private License license;
    @Mock
    private ServiceCategory serviceCategoryOne;
    @Mock
    private ServiceCategory serviceCategoryTwo;
    @Mock
    private ServiceLocation servicelocation;
    @Mock
    private NlsMessageFormat nlsMessageFormat;
    @Mock
    private FileImportOccurrence fileImportOccurrenceCorrect;
    @Mock
    private FileImportOccurrence fileImportOccurrenceIncorrect;
    @Mock
    private FileImportOccurrence fileImportOccurrenceFail;
    @Mock
    private LocationTemplate locationTemplate;

    private MeteringDataImporterContext context;

    @Before
    public void initMocks() throws FileNotFoundException {
        when(threadPrincipalService.getLocale()).thenReturn(Locale.ENGLISH);
        when(meteringService.getLocationTemplate()).thenReturn(locationTemplate);
        when(meteringService.findUsagePointByMRID(anyString())).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByName(anyString())).thenReturn(Optional.empty());
        when(meteringService.getServiceCategory(Matchers.any(ServiceKind.class))).thenReturn(Optional.ofNullable(serviceCategoryTwo));
        when(meteringService.findServiceLocation(anyLong())).thenReturn(Optional.ofNullable(servicelocation));
        when(usagePointBuilder.create()).thenReturn(usagePoint);
        when(usagePointBuilder.validate()).thenReturn(usagePoint);
        when(usagePointBuilder.validate()).thenReturn(usagePoint);
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategoryTwo);
        when(usagePoint.getUsedCalendars()).thenReturn(usedCalendars);
        when(serviceCategoryTwo.newUsagePointDetail(any(), any())).thenReturn(usagePointDetail);
        when(serviceCategoryTwo.newUsagePoint(eq("DOA_UPS1_UP001"), any(Instant.class))).thenReturn(usagePointBuilder);
        when(serviceCategoryTwo.getKind()).thenReturn(ServiceKind.ELECTRICITY);
        when(serviceCategoryTwo.getId()).thenReturn(34L);
        when(thesaurus.getFormat((Matchers.any(MessageSeeds.class)))).thenReturn(nlsMessageFormat);
        when(licenseService.getLicensedApplicationKeys()).thenReturn(Collections.singletonList("MDC"));
        when(licenseService.getLicenseForApplication("INS")).thenReturn(Optional.empty());
        when(clock.instant()).thenReturn(Instant.EPOCH);
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(nlsMessageFormat);
        when(thesaurus.getFormat(any(TranslationKey.class))).thenReturn(nlsMessageFormat);
        when(nlsMessageFormat.format()).thenReturn("message");
        when(nlsMessageFormat.format(anyInt(), anyInt())).thenReturn("message");
        when(locationTemplate.getTemplateMembers()).thenReturn(Collections.emptyList());

        when(fileImportOccurrenceCorrect.getLogger()).thenReturn(logger);
        when(fileImportOccurrenceIncorrect.getLogger()).thenReturn(logger);
        when(fileImportOccurrenceFail.getLogger()).thenReturn(logger);
        when(fileImportOccurrenceCorrect.getContents()).thenReturn(new FileInputStream(getClass().getClassLoader().getResource("usagepoint_correct.csv").getPath()));
        when(fileImportOccurrenceIncorrect.getContents()).thenReturn(new FileInputStream(getClass().getClassLoader().getResource("usagepoint_incorrect.csv").getPath()));
        when(fileImportOccurrenceFail.getContents()).thenReturn(new FileInputStream(getClass().getClassLoader().getResource("usagepoint_fail.csv").getPath()));

        context = new MeteringDataImporterContext();
        context.setMeteringService(meteringService);
        context.setCustomPropertySetService(customPropertySetService);
        context.setLicenseService(licenseService);
        context.setPropertySpecService(propertySpecService);
        context.setThreadPrincipalService(threadPrincipalService);
        context.setClock(clock);
        context.setMetrologyConfigurationService(metrologyConfigurationService);
        context.setCalendarService(calendarService);
        context.setThesaurus(thesaurus);
    }

    @Before
    public void initializeCalendarMocks() {
        when(this.calendarService.findCalendarByName(TIME_OF_USE_CALENDAR_NAME)).thenReturn(Optional.of(this.touCalendar));
        when(this.calendarService.findCalendarByName(WORKFORCE_OF_USE_CALENDAR_NAME)).thenReturn(Optional.of(this.workForceCalendar));
        when(this.calendarService.findCalendarByName(COMMANDS_OF_USE_CALENDAR_NAME)).thenReturn(Optional.of(this.commandsCalendar));
    }

    @Test
    public void testProcessCorrectInfo() throws IOException {
        FileImporter importer = createUsagePointImporter();
        importer.process(fileImportOccurrenceCorrect);
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testProcessIncorrectInfo() throws IOException {
        FileImporter importer = createUsagePointImporter();
        importer.process(fileImportOccurrenceIncorrect);
        verify(fileImportOccurrenceIncorrect).markFailure("message");
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testProcessFail() throws IOException {
        FileImporter importer = createUsagePointImporter();
        importer.process(fileImportOccurrenceFail);
        verify(fileImportOccurrenceFail).markFailure("message");
        verify(logger, never()).info(Matchers.anyString());
    }

    @Test
    public void testCanSetMetrologyConfiguration() throws IOException {
        String content = "id;serviceKind;Created;MetrologyConfiguration;metrologyConfigurationTime\n" +
                "DOA_UPS1_UP001;ELECTRICITY;28/07/2016 00:00;SP10_DEMO_1;28/07/2016 00:00";
        FileImporter importer = createUsagePointImporter();
        FileImportOccurrence occurrence = mock(FileImportOccurrence.class);
        when(occurrence.getLogger()).thenReturn(logger);
        when(occurrence.getContents()).thenReturn(new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
        UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        when(metrologyConfiguration.isActive()).thenReturn(true);
        when(metrologyConfiguration.getServiceCategory()).thenReturn(serviceCategoryTwo);
        when(metrologyConfigurationService.findMetrologyConfiguration("SP10_DEMO_1")).thenReturn(Optional.of(metrologyConfiguration));

        importer.process(occurrence);

        verify(usagePoint).apply(eq(metrologyConfiguration), any(Instant.class));
    }

    @Test
    public void testNoMetrologyConfigurationForUpdate() throws IOException {
        String content = "id;serviceKind;Created;MetrologyConfiguration;metrologyConfigurationTime\n" +
                "DOA_UPS1_UP001;ELECTRICITY;28/07/2016 00:00;SP10_DEMO_1;28/07/2016 00:00";
        FileImporter importer = createUsagePointImporter();
        FileImportOccurrence occurrence = mock(FileImportOccurrence.class);
        when(occurrence.getLogger()).thenReturn(logger);
        when(occurrence.getContents()).thenReturn(new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
        when(meteringService.findUsagePointByName("DOA_UPS1_UP001")).thenReturn(Optional.of(usagePoint));

        importer.process(occurrence);

        verify(usagePoint, never()).apply(any(UsagePointMetrologyConfiguration.class), any(Instant.class));
    }

    @Test
    public void testFailSetMetrologyConfigurationDefferentServiceCategory() throws IOException {
        String content = "id;serviceKind;Created;MetrologyConfiguration;metrologyConfigurationTime\n" +
                "DOA_UPS1_UP001;ELECTRICITY;28/07/2016 00:00;SP10_DEMO_1;28/07/2016 00:00";
        FileImporter importer = createUsagePointImporter();
        FileImportOccurrence occurrence = mock(FileImportOccurrence.class);
        when(occurrence.getLogger()).thenReturn(logger);
        when(occurrence.getContents()).thenReturn(new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
        UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        when(metrologyConfiguration.isActive()).thenReturn(true);
        when(metrologyConfiguration.getServiceCategory()).thenReturn(serviceCategoryOne);
        when(metrologyConfigurationService.findMetrologyConfiguration("SP10_DEMO_1")).thenReturn(Optional.of(metrologyConfiguration));

        importer.process(occurrence);

        verify(usagePoint, never()).apply(eq(metrologyConfiguration), any(Instant.class));
        verify(logger, times(1)).warning(Matchers.anyString());
    }

    @Test
    public void testFailSetMetrologyConfigurationNoInstallationTime() throws IOException {
        String content = "id;serviceKind;Created;MetrologyConfiguration;metrologyConfigurationTime\n" +
                "DOA_UPS1_UP001;ELECTRICITY;28/07/2016 00:00;SP10_DEMO_1;";
        FileImporter importer = createUsagePointImporter();
        FileImportOccurrence occurrence = mock(FileImportOccurrence.class);
        when(occurrence.getLogger()).thenReturn(logger);
        when(occurrence.getContents()).thenReturn(new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
        UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        when(metrologyConfiguration.isActive()).thenReturn(true);
        when(metrologyConfiguration.getServiceCategory()).thenReturn(serviceCategoryOne);
        when(metrologyConfigurationService.findMetrologyConfiguration("SP10_DEMO_1")).thenReturn(Optional.of(metrologyConfiguration));

        importer.process(occurrence);

        verify(usagePoint, never()).apply(eq(metrologyConfiguration), any(Instant.class));
        verify(logger, times(1)).warning(Matchers.anyString());
    }

    @Test
    public void testFailSetInactiveMetrologyConfiguration() throws IOException {
        String content = "id;serviceKind;Created;MetrologyConfiguration;metrologyConfigurationTime\n" +
                "DOA_UPS1_UP001;ELECTRICITY;28/07/2016 00:00;SP10_DEMO_1;28/07/2016 00:00";
        FileImporter importer = createUsagePointImporter();
        FileImportOccurrence occurrence = mock(FileImportOccurrence.class);
        when(occurrence.getLogger()).thenReturn(logger);
        when(occurrence.getContents()).thenReturn(new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
        UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        when(metrologyConfiguration.isActive()).thenReturn(false);
        when(metrologyConfiguration.getServiceCategory()).thenReturn(serviceCategoryTwo);
        when(metrologyConfigurationService.findMetrologyConfiguration("SP10_DEMO_1")).thenReturn(Optional.of(metrologyConfiguration));

        importer.process(occurrence);

        verify(usagePoint, never()).apply(eq(metrologyConfiguration), any(Instant.class));
        verify(logger, times(1)).warning(Matchers.anyString());
    }

    @Test
    public void testFailSetUnexistingMetrologyConfiguration() throws IOException {
        String content = "id;serviceKind;Created;MetrologyConfiguration;metrologyConfigurationTime\n" +
                "DOA_UPS1_UP001;ELECTRICITY;28/07/2016 00:00;SP10_DEMO_1;28/07/2016 00:00";
        FileImporter importer = createUsagePointImporter();
        FileImportOccurrence occurrence = mock(FileImportOccurrence.class);
        when(occurrence.getLogger()).thenReturn(logger);
        when(occurrence.getContents()).thenReturn(new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
        when(metrologyConfigurationService.findMetrologyConfiguration("SP10_DEMO_1")).thenReturn(Optional.empty());

        importer.process(occurrence);

        verify(usagePoint, never()).apply(any(UsagePointMetrologyConfiguration.class), any(Instant.class));
        verify(logger, times(1)).warning(Matchers.anyString());
    }

    @Test
    public void testAllEmptyCalendarNames() throws IOException {
        String content = "id;serviceKind;Created;touCalendarName;touCalendarUsageStartTime;workForceCalendarName;workForceCalendarUsageStartTime;commandsCalendarName;commandsCalendarUsageStartTime\n" +
                "DOA_UPS1_UP001;ELECTRICITY;28/07/2016 00:00;;;;;;";
        FileImporter importer = createUsagePointImporter();
        FileImportOccurrence occurrence = mock(FileImportOccurrence.class);
        when(occurrence.getLogger()).thenReturn(this.logger);
        when(occurrence.getContents()).thenReturn(new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
        when(this.meteringService.findUsagePointByName("DOA_UPS1_UP001")).thenReturn(Optional.of(this.usagePoint));

        // Business method
        importer.process(occurrence);

        // Asserts
        verify(this.usagePoint, never()).getUsedCalendars();
        verify(this.calendarService, never()).findCalendarByName(anyString());
    }

    @Test
    public void createWithNonExistingTOUCalendar() throws IOException {
        String content = "id;serviceKind;Created;touCalendarName;touCalendarUsageStartTime\n" +
                "DOA_UPS1_UP001;ELECTRICITY;28/07/2016 00:00;BOGUS;28/07/2016 00:00";
        FileImporter importer = createUsagePointImporter();
        FileImportOccurrence occurrence = mock(FileImportOccurrence.class);
        when(occurrence.getLogger()).thenReturn(this.logger);
        when(occurrence.getContents()).thenReturn(new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
        when(this.meteringService.findUsagePointByName("DOA_UPS1_UP001")).thenReturn(Optional.empty());
        when(this.calendarService.findCalendarByName("BOGUS")).thenReturn(Optional.empty());

        // Business method
        importer.process(occurrence);

        // Asserts
        verify(this.usagePoint, never()).getUsedCalendars();
        verify(this.calendarService).findCalendarByName("BOGUS");
    }

    @Test
    public void createWithTOUCalendar() throws IOException {
        String content = "id;serviceKind;Created;touCalendarName;touCalendarUsageStartTime\n" +
                "DOA_UPS1_UP001;ELECTRICITY;28/07/2016 00:00;" + TIME_OF_USE_CALENDAR_NAME + ";28/07/2016 00:00";
        Instant expectedCalendarStart = LocalDateTime.of(2016, Month.JULY, 28, 0, 0, 0).atOffset(ZoneOffset.UTC).toInstant();
        FileImporter importer = createUsagePointImporter();
        FileImportOccurrence occurrence = mock(FileImportOccurrence.class);
        when(occurrence.getLogger()).thenReturn(this.logger);
        when(occurrence.getContents()).thenReturn(new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
        when(this.meteringService.findUsagePointByName("DOA_UPS1_UP001")).thenReturn(Optional.empty());

        // Business method
        importer.process(occurrence);

        // Asserts
        verify(this.usagePoint).getUsedCalendars();
        verify(this.calendarService).findCalendarByName(TIME_OF_USE_CALENDAR_NAME);
        verify(this.usedCalendars).addCalendar(this.touCalendar, expectedCalendarStart);
    }

    @Test
    public void createWithTOUCalendarWithoutStartTime() throws IOException {
        String content = "id;serviceKind;Created;touCalendarName;touCalendarUsageStartTime\n" +
                "DOA_UPS1_UP001;ELECTRICITY;28/07/2016 00:00;" + TIME_OF_USE_CALENDAR_NAME;
        FileImporter importer = createUsagePointImporter();
        FileImportOccurrence occurrence = mock(FileImportOccurrence.class);
        when(occurrence.getLogger()).thenReturn(this.logger);
        when(occurrence.getContents()).thenReturn(new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
        when(this.meteringService.findUsagePointByName("DOA_UPS1_UP001")).thenReturn(Optional.empty());

        // Business method
        importer.process(occurrence);

        // Asserts
        verify(this.usagePoint).getUsedCalendars();
        verify(this.calendarService).findCalendarByName(TIME_OF_USE_CALENDAR_NAME);
        verify(this.usedCalendars).addCalendar(this.touCalendar);
    }

    @Test
    public void createWithWorkForceCalendar() throws IOException {
        String content = "id;serviceKind;Created;workForceCalendarName;workForceCalendarUsageStartTime\n" +
                "DOA_UPS1_UP001;ELECTRICITY;28/07/2016 00:00;" + WORKFORCE_OF_USE_CALENDAR_NAME + ";28/07/2016 00:00";
        Instant expectedCalendarStart = LocalDateTime.of(2016, Month.JULY, 28, 0, 0, 0).atOffset(ZoneOffset.UTC).toInstant();
        FileImporter importer = createUsagePointImporter();
        FileImportOccurrence occurrence = mock(FileImportOccurrence.class);
        when(occurrence.getLogger()).thenReturn(this.logger);
        when(occurrence.getContents()).thenReturn(new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
        when(this.meteringService.findUsagePointByName("DOA_UPS1_UP001")).thenReturn(Optional.empty());

        // Business method
        importer.process(occurrence);

        // Asserts
        verify(this.usagePoint).getUsedCalendars();
        verify(this.calendarService).findCalendarByName(WORKFORCE_OF_USE_CALENDAR_NAME);
        verify(this.usedCalendars).addCalendar(this.workForceCalendar, expectedCalendarStart);
    }

    @Test
    public void createWithWorkForceCalendarWithoutStartTime() throws IOException {
        String content = "id;serviceKind;Created;workForceCalendarName;workForceCalendarUsageStartTime\n" +
                "DOA_UPS1_UP001;ELECTRICITY;28/07/2016 00:00;" + WORKFORCE_OF_USE_CALENDAR_NAME;
        FileImporter importer = createUsagePointImporter();
        FileImportOccurrence occurrence = mock(FileImportOccurrence.class);
        when(occurrence.getLogger()).thenReturn(this.logger);
        when(occurrence.getContents()).thenReturn(new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
        when(this.meteringService.findUsagePointByName("DOA_UPS1_UP001")).thenReturn(Optional.empty());

        // Business method
        importer.process(occurrence);

        // Asserts
        verify(this.usagePoint).getUsedCalendars();
        verify(this.calendarService).findCalendarByName(WORKFORCE_OF_USE_CALENDAR_NAME);
        verify(this.usedCalendars).addCalendar(this.workForceCalendar);
    }

    @Test
    public void createWithCommandsCalendar() throws IOException {
        String content = "id;serviceKind;Created;commandsCalendarName;commandsCalendarUsageStartTime\n" +
                "DOA_UPS1_UP001;ELECTRICITY;28/07/2016 00:00;" + COMMANDS_OF_USE_CALENDAR_NAME + ";28/07/2016 00:00";
        Instant expectedCalendarStart = LocalDateTime.of(2016, Month.JULY, 28, 0, 0, 0).atOffset(ZoneOffset.UTC).toInstant();
        FileImporter importer = createUsagePointImporter();
        FileImportOccurrence occurrence = mock(FileImportOccurrence.class);
        when(occurrence.getLogger()).thenReturn(this.logger);
        when(occurrence.getContents()).thenReturn(new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
        when(this.meteringService.findUsagePointByName("DOA_UPS1_UP001")).thenReturn(Optional.empty());

        // Business method
        importer.process(occurrence);

        // Asserts
        verify(this.usagePoint).getUsedCalendars();
        verify(this.calendarService).findCalendarByName(COMMANDS_OF_USE_CALENDAR_NAME);
        verify(this.usedCalendars).addCalendar(this.commandsCalendar, expectedCalendarStart);
    }

    @Test
    public void createWithCommandsCalendarWithoutStartTime() throws IOException {
        String content = "id;serviceKind;Created;commandsCalendarName;commandsCalendarUsageStartTime\n" +
                "DOA_UPS1_UP001;ELECTRICITY;28/07/2016 00:00;" + COMMANDS_OF_USE_CALENDAR_NAME;
        FileImporter importer = createUsagePointImporter();
        FileImportOccurrence occurrence = mock(FileImportOccurrence.class);
        when(occurrence.getLogger()).thenReturn(this.logger);
        when(occurrence.getContents()).thenReturn(new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
        when(this.meteringService.findUsagePointByName("DOA_UPS1_UP001")).thenReturn(Optional.empty());

        // Business method
        importer.process(occurrence);

        // Asserts
        verify(this.usagePoint).getUsedCalendars();
        verify(this.calendarService).findCalendarByName(COMMANDS_OF_USE_CALENDAR_NAME);
        verify(this.usedCalendars).addCalendar(this.commandsCalendar);
    }

    @Test
    public void createWithAllCalendars() throws IOException {
        String content = "id;serviceKind;Created;touCalendarName;touCalendarUsageStartTime;workForceCalendarName;workForceCalendarUsageStartTime;commandsCalendarName;commandsCalendarUsageStartTime\n" +
                "DOA_UPS1_UP001;ELECTRICITY;28/07/2016 00:00;" + TIME_OF_USE_CALENDAR_NAME + ";28/07/2016 00:00;" + WORKFORCE_OF_USE_CALENDAR_NAME + ";28/08/2016 00:00;" + COMMANDS_OF_USE_CALENDAR_NAME + ";28/09/2016 00:00";
        Instant expectedTimeOfUseCalendarStart = LocalDateTime.of(2016, Month.JULY, 28, 0, 0, 0).atOffset(ZoneOffset.UTC).toInstant();
        Instant expectedWorkForceCalendarStart = LocalDateTime.of(2016, Month.AUGUST, 28, 0, 0, 0).atOffset(ZoneOffset.UTC).toInstant();
        Instant expectedCommandsCalendarStart = LocalDateTime.of(2016, Month.SEPTEMBER, 28, 0, 0, 0).atOffset(ZoneOffset.UTC).toInstant();
        FileImporter importer = createUsagePointImporter();
        FileImportOccurrence occurrence = mock(FileImportOccurrence.class);
        when(occurrence.getLogger()).thenReturn(this.logger);
        when(occurrence.getContents()).thenReturn(new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
        when(this.meteringService.findUsagePointByName("DOA_UPS1_UP001")).thenReturn(Optional.empty());

        // Business method
        importer.process(occurrence);

        // Asserts
        verify(this.usagePoint, atLeast(3)).getUsedCalendars();
        verify(this.calendarService).findCalendarByName(TIME_OF_USE_CALENDAR_NAME);
        verify(this.calendarService).findCalendarByName(WORKFORCE_OF_USE_CALENDAR_NAME);
        verify(this.calendarService).findCalendarByName(COMMANDS_OF_USE_CALENDAR_NAME);
        verify(this.usedCalendars).addCalendar(this.touCalendar, expectedTimeOfUseCalendarStart);
        verify(this.usedCalendars).addCalendar(this.workForceCalendar, expectedWorkForceCalendarStart);
        verify(this.usedCalendars).addCalendar(this.commandsCalendar, expectedCommandsCalendarStart);
    }

    @Test
    public void createWithAllCalendarsWithoutStartTime() throws IOException {
        String content = "id;serviceKind;Created;touCalendarName;touCalendarUsageStartTime;workForceCalendarName;workForceCalendarUsageStartTime;commandsCalendarName;commandsCalendarUsageStartTime\n" +
                "DOA_UPS1_UP001;ELECTRICITY;28/07/2016 00:00;" + TIME_OF_USE_CALENDAR_NAME + ";;" + WORKFORCE_OF_USE_CALENDAR_NAME + ";;" + COMMANDS_OF_USE_CALENDAR_NAME + ";";
        FileImporter importer = createUsagePointImporter();
        FileImportOccurrence occurrence = mock(FileImportOccurrence.class);
        when(occurrence.getLogger()).thenReturn(this.logger);
        when(occurrence.getContents()).thenReturn(new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
        when(this.meteringService.findUsagePointByName("DOA_UPS1_UP001")).thenReturn(Optional.empty());

        // Business method
        importer.process(occurrence);

        // Asserts
        verify(this.usagePoint, atLeast(3)).getUsedCalendars();
        verify(this.calendarService).findCalendarByName(TIME_OF_USE_CALENDAR_NAME);
        verify(this.calendarService).findCalendarByName(WORKFORCE_OF_USE_CALENDAR_NAME);
        verify(this.calendarService).findCalendarByName(COMMANDS_OF_USE_CALENDAR_NAME);
        verify(this.usedCalendars).addCalendar(this.touCalendar);
        verify(this.usedCalendars).addCalendar(this.workForceCalendar);
        verify(this.usedCalendars).addCalendar(this.commandsCalendar);
    }

    private FileImporter createUsagePointImporter() {
        UsagePointsImporterFactory factory = new UsagePointsImporterFactory(context);
        Map<String, Object> properties = new HashMap<>();
        properties.put(DataImporterProperty.DELIMITER.getPropertyKey(), ";");
        properties.put(DataImporterProperty.DATE_FORMAT.getPropertyKey(), "dd/MM/yyyy HH:mm");
        properties.put(DataImporterProperty.TIME_ZONE.getPropertyKey(), "GMT+00:00");
        properties.put(
                DataImporterProperty.NUMBER_FORMAT.getPropertyKey(),
                new SupportedNumberFormat.SupportedNumberFormatValueFactory().fromStringValue("FORMAT4"));
        return factory.createImporter(properties);
    }

}
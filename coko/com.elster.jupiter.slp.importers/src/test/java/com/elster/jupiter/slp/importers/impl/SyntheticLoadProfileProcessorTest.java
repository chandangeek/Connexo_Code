/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.slp.SyntheticLoadProfile;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.slp.importers.impl.properties.SupportedNumberFormat;
import com.elster.jupiter.slp.importers.impl.syntheticloadprofile.SyntheticLoadProfileImporterFactory;
import com.elster.jupiter.util.exception.MessageSeed;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SyntheticLoadProfileProcessorTest {

    private final Instant DATE = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo()).toInstant();

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();

    @Mock
    Clock clock;
    @Mock
    PropertySpecService propertySpecService;
    @Mock
    LicenseService licenseService;
    @Mock
    ThreadPrincipalService threadPrincipalService;
    @Mock
    License license;
    @Mock
    NlsMessageFormat nlsMessageFormat;
    @Mock
    private Logger logger;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private FileImportOccurrence fileImportOccurrenceCorrect;
    @Mock
    private FileImportOccurrence fileImportOccurrenceIncorrectInterval;
    @Mock
    private FileImportOccurrence fileImportOccurrenceIncorrectDuration;
    @Mock
    private SyntheticLoadProfileService syntheticLoadProfileService;

    @Mock
    private SyntheticLoadProfile syntheticLoadProfile1, syntheticLoadProfile2, syntheticLoadProfile3;
    @Mock
    private ReadingType readingType1;

    private SyntheticLoadProfileDataImporterContext context;

    @Before
    public void initMocks() throws URISyntaxException {
        MockitoAnnotations.initMocks(this);

        when(threadPrincipalService.getLocale()).thenReturn(Locale.ENGLISH);
        when(thesaurus.getFormat((Matchers.any(MessageSeeds.class)))).thenReturn(nlsMessageFormat);
        when(licenseService.getLicensedApplicationKeys()).thenReturn(Collections.singletonList("INS"));
        when(licenseService.getLicenseForApplication("INS")).thenReturn(Optional.ofNullable(license));
        when(clock.instant()).thenReturn(Instant.EPOCH);
        when(clock.getZone()).thenReturn(TimeZoneNeutral.getMcMurdo());
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(nlsMessageFormat);
        when(thesaurus.getFormat(any(TranslationKey.class))).thenReturn(nlsMessageFormat);
        when(nlsMessageFormat.format()).thenReturn("message");
        when(nlsMessageFormat.format(anyInt(), anyInt())).thenReturn("message");

        when(syntheticLoadProfileService.findSyntheticLoadProfile("slp1")).thenReturn(Optional.of(syntheticLoadProfile1));
        when(syntheticLoadProfileService.findSyntheticLoadProfile("slp2")).thenReturn(Optional.of(syntheticLoadProfile2));
        when(syntheticLoadProfileService.findSyntheticLoadProfile("slp3")).thenReturn(Optional.of(syntheticLoadProfile3));

        when(syntheticLoadProfile1.getInterval()).thenReturn(Duration.ofMinutes(15));
        when(syntheticLoadProfile2.getInterval()).thenReturn(Duration.ofMinutes(15));
        when(syntheticLoadProfile3.getInterval()).thenReturn(Duration.ofMinutes(15));
        when(syntheticLoadProfile1.getDuration()).thenReturn(Period.ofDays(1));
        when(syntheticLoadProfile2.getDuration()).thenReturn(Period.ofDays(1));
        when(syntheticLoadProfile3.getDuration()).thenReturn(Period.ofDays(1));
        when(syntheticLoadProfile1.getStartTime()).thenReturn(DATE);
        when(syntheticLoadProfile2.getStartTime()).thenReturn(DATE);
        when(syntheticLoadProfile3.getStartTime()).thenReturn(DATE);
        when(syntheticLoadProfile1.getReadingType()).thenReturn(readingType1);
        when(syntheticLoadProfile2.getReadingType()).thenReturn(readingType1);
        when(syntheticLoadProfile3.getReadingType()).thenReturn(readingType1);
        when(readingType1.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);

        try {
            when(fileImportOccurrenceCorrect.getLogger()).thenReturn(logger);
            when(fileImportOccurrenceIncorrectInterval.getLogger()).thenReturn(logger);
            when(fileImportOccurrenceIncorrectDuration.getLogger()).thenReturn(logger);
            when(fileImportOccurrenceCorrect.getContents())
                    .thenReturn(new FileInputStream(new URI(getClass().getClassLoader().getResource("slp_syntheticloadprofile_correct.csv").getFile()).getPath()));
            when(fileImportOccurrenceIncorrectInterval.getContents())
                    .thenReturn(new FileInputStream(new URI(getClass().getClassLoader().getResource("slp_syntheticloadprofile_incorrectinterval.csv").getFile()).getPath()));
            when(fileImportOccurrenceIncorrectDuration.getContents())
                    .thenReturn(new FileInputStream(new URI(getClass().getClassLoader().getResource("slp_syntheticloadprofile_incorrectduration.csv").getFile()).getPath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        context = spy(new SyntheticLoadProfileDataImporterContext());
        context.setLicenseService(licenseService);
        context.setSyntheticLoadProfileService(syntheticLoadProfileService);
        context.setPropertySpecService(propertySpecService);
        context.setThreadPrincipalService(threadPrincipalService);
        context.setClock(clock);
        when(context.getThesaurus()).thenReturn(thesaurus);
    }

    @Test
    public void testProcessCorrectInfo() throws IOException {
        FileImporter importer = createSyntheticLoadProfileImporter();
        importer.process(fileImportOccurrenceCorrect);
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testProcessIncorrectIntervalInfo() throws IOException {
        FileImporter importer = createSyntheticLoadProfileImporter();
        importer.process(fileImportOccurrenceIncorrectInterval);
        verify(fileImportOccurrenceIncorrectInterval).markFailure("message");
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).severe(Matchers.anyString());
    }

    @Test
    public void testProcessIncorrectDurationInfo() throws IOException {
        FileImporter importer = createSyntheticLoadProfileImporter();
        importer.process(fileImportOccurrenceIncorrectDuration);
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).severe(Matchers.anyString());
    }

    private FileImporter createSyntheticLoadProfileImporter() {
        SyntheticLoadProfileImporterFactory factory = new SyntheticLoadProfileImporterFactory(context);
        Map<String, Object> properties = new HashMap<>();
        properties.put(DataImporterProperty.DELIMITER.getPropertyKey(), ";");
        properties.put(DataImporterProperty.DATE_FORMAT.getPropertyKey(), "dd/MM/yyyy HH:mm");
        properties.put(DataImporterProperty.TIME_ZONE.getPropertyKey(), "GMT+13:00");
        properties.put(DataImporterProperty.NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormat.SupportedNumberFormatValueFactory()
                .fromStringValue("FORMAT4"));
        return factory.createImporter(properties);
    }
}

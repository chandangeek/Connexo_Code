/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.slp.CorrectionFactor;
import com.elster.jupiter.slp.DurationAttribute;
import com.elster.jupiter.slp.IntervalAttribute;
import com.elster.jupiter.slp.SyntheticLoadProfileService;
import com.elster.jupiter.slp.importers.impl.correctionfactor.CorrectionFactorImporterFactory;
import com.elster.jupiter.slp.importers.impl.properties.SupportedNumberFormat;
import com.elster.jupiter.util.exception.MessageSeed;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
public class CorrectionFactorProcessorTest {

    private final Instant DATE = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();

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
    private CorrectionFactor correctionFactor1, correctionFactor2, correctionFactor3;

    private SyntheticLoadProfileDataImporterContext context;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);

        when(threadPrincipalService.getLocale()).thenReturn(Locale.ENGLISH);
        when(thesaurus.getFormat((Matchers.any(MessageSeeds.class)))).thenReturn(nlsMessageFormat);
        when(licenseService.getLicensedApplicationKeys()).thenReturn(Collections.singletonList("INS"));
        when(licenseService.getLicenseForApplication("INS")).thenReturn(Optional.ofNullable(license));
        when(clock.instant()).thenReturn(Instant.EPOCH);
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(nlsMessageFormat);
        when(thesaurus.getFormat(any(TranslationKey.class))).thenReturn(nlsMessageFormat);
        when(nlsMessageFormat.format()).thenReturn("message");
        when(nlsMessageFormat.format(anyInt(), anyInt())).thenReturn("message");

        when(syntheticLoadProfileService.findCorrectionFactor("corr1")).thenReturn(Optional.of(correctionFactor1));
        when(syntheticLoadProfileService.findCorrectionFactor("corr2")).thenReturn(Optional.of(correctionFactor2));
        when(syntheticLoadProfileService.findCorrectionFactor("corr3")).thenReturn(Optional.of(correctionFactor3));

        when(correctionFactor1.getInterval()).thenReturn(IntervalAttribute.MINUTE15);
        when(correctionFactor2.getInterval()).thenReturn(IntervalAttribute.MINUTE15);
        when(correctionFactor3.getInterval()).thenReturn(IntervalAttribute.MINUTE15);
        when(correctionFactor1.getDuration()).thenReturn(DurationAttribute.DAY1);
        when(correctionFactor2.getDuration()).thenReturn(DurationAttribute.DAY1);
        when(correctionFactor3.getDuration()).thenReturn(DurationAttribute.DAY1);
        when(correctionFactor1.getStartTime()).thenReturn(DATE);
        when(correctionFactor2.getStartTime()).thenReturn(DATE);
        when(correctionFactor3.getStartTime()).thenReturn(DATE);

        try {
            when(fileImportOccurrenceCorrect.getLogger()).thenReturn(logger);
            when(fileImportOccurrenceIncorrectInterval.getLogger()).thenReturn(logger);
            when(fileImportOccurrenceIncorrectDuration.getLogger()).thenReturn(logger);
            when(fileImportOccurrenceCorrect.getContents())
                    .thenReturn(new FileInputStream(getClass().getClassLoader().getResource("correctionfactor_correct.csv").getPath()));
            when(fileImportOccurrenceIncorrectInterval.getContents())
                    .thenReturn(new FileInputStream(getClass().getClassLoader().getResource("correctionfactor_incorrectinterval.csv").getPath()));
            when(fileImportOccurrenceIncorrectDuration.getContents())
                    .thenReturn(new FileInputStream(getClass().getClassLoader().getResource("correctionfactor_incorrectduration.csv").getPath()));
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
        FileImporter importer = createCorrectionFactorImporter();
        importer.process(fileImportOccurrenceCorrect);
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    public void testProcessIncorrectIntervalInfo() throws IOException {
        FileImporter importer = createCorrectionFactorImporter();
        importer.process(fileImportOccurrenceIncorrectInterval);
        verify(fileImportOccurrenceIncorrectInterval).markFailure("message");
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).severe(Matchers.anyString());
    }

    @Test
    public void testProcessIncorrectDurationInfo() throws IOException {
        FileImporter importer = createCorrectionFactorImporter();
        importer.process(fileImportOccurrenceIncorrectDuration);
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).severe(Matchers.anyString());
    }

    private FileImporter createCorrectionFactorImporter() {
        CorrectionFactorImporterFactory factory = new CorrectionFactorImporterFactory(context);
        Map<String, Object> properties = new HashMap<>();
        properties.put(DataImporterProperty.DELIMITER.getPropertyKey(), ";");
        properties.put(DataImporterProperty.DATE_FORMAT.getPropertyKey(), "dd/MM/yyyy HH:mm");
        properties.put(DataImporterProperty.TIME_ZONE.getPropertyKey(), "GMT+00:00");
        properties.put(DataImporterProperty.NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormat.SupportedNumberFormatValueFactory()
                .fromStringValue("FORMAT4"));
        return factory.createImporter(properties);
    }
}

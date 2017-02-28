/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointProcessorForMultisenseTest {

    @Mock
    Clock clock;
    @Mock
    private Logger logger;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private UsagePointBuilder usagePointBuilder;
    @Mock
    private UsagePointDetailBuilder usagePointDetailBuilder;
    @Mock
    private UsagePointDetail usagePointDetail;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    MeteringService meteringService;
    @Mock
    MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    CustomPropertySetService customPropertySetService;
    @Mock
    PropertySpecService propertySpecService;
    @Mock
    LicenseService licenseService;
    @Mock
    ThreadPrincipalService threadPrincipalService;
    @Mock
    License license;
    @Mock
    private ServiceCategory serviceCategoryOne;
    @Mock
    private ServiceCategory serviceCategoryTwo;
    @Mock
    private ServiceLocation servicelocation;
    @Mock
    NlsMessageFormat nlsMessageFormat;
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

        context = spy(new MeteringDataImporterContext());
        context.setMeteringService(meteringService);
        context.setCustomPropertySetService(customPropertySetService);
        context.setLicenseService(licenseService);
        context.setPropertySpecService(propertySpecService);
        context.setThreadPrincipalService(threadPrincipalService);
        context.setClock(clock);
        context.setMetrologyConfigurationService(metrologyConfigurationService);
        when(context.getThesaurus()).thenReturn(thesaurus);
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

    private FileImporter createUsagePointImporter() {
        UsagePointsImporterFactory factory = new UsagePointsImporterFactory(context);
        Map<String, Object> properties = new HashMap<>();
        properties.put(DataImporterProperty.DELIMITER.getPropertyKey(), ";");
        properties.put(DataImporterProperty.DATE_FORMAT.getPropertyKey(), "dd/MM/yyyy HH:mm");
        properties.put(DataImporterProperty.TIME_ZONE.getPropertyKey(), "GMT+00:00");
        properties.put(DataImporterProperty.NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormat.SupportedNumberFormatValueFactory()
                .fromStringValue("FORMAT4"));
        return factory.createImporter(properties);
    }
}

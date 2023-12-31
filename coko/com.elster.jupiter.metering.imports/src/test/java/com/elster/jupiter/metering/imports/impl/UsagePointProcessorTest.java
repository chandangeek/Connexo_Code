/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.LocationBuilder;
import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointMeterActivator;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.imports.impl.properties.SupportedNumberFormat;
import com.elster.jupiter.metering.imports.impl.usagepoint.UsagePointsImporterFactory;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.units.Quantity;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointProcessorTest {

    @Mock
    Clock clock;
    @Mock
    private Logger logger;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private UsagePointBuilder usagePointBuilder;
    @Mock
    private ElectricityDetailBuilder usagePointDetailBuilder;
    @Mock
    private ElectricityDetail usagePointDetail;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    MeteringService meteringService;
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
    UsagePointCustomPropertySetExtension usagePointCustomPropertySetExtension;
    @Mock
    private FileImportOccurrence fileImportOccurrenceCorrect;
    @Mock
    private FileImportOccurrence fileImportOccurrenceIncorrect;
    @Mock
    private LocationTemplate locationTemplate;
    @Mock
    private LocationTemplate.TemplateField templateFieldCountryCode;
    @Mock
    private LocationTemplate.TemplateField templateFieldCountryName;
    @Mock
    private LocationTemplate.TemplateField templateFieldAdministrativeArea;
    @Mock
    private LocationTemplate.TemplateField templateFieldEstablishmentType;
    @Mock
    private LocationTemplate.TemplateField templateFieldLocality;
    @Mock
    private LocationTemplate.TemplateField templateFieldSubLocality;
    @Mock
    private LocationTemplate.TemplateField templateFieldStreetType;
    @Mock
    private LocationTemplate.TemplateField templateFieldStreetName;
    @Mock
    private LocationTemplate.TemplateField templateFieldStreetNumber;
    @Mock
    private LocationTemplate.TemplateField templateFieldLocale;
    @Mock
    private LocationTemplate.TemplateField templateFieldEstablishmentName;
    @Mock
    private LocationTemplate.TemplateField templateFieldEstablishmentNumber;
    @Mock
    private LocationTemplate.TemplateField templateFieldAddressDetail;
    @Mock
    private LocationTemplate.TemplateField templateFieldZipCode;
    @Mock
    private LocationBuilder locationBuilder;
    @Mock
    private LocationBuilder.LocationMemberBuilder locationMemberBuilder;
    @Mock
    private MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    private MeterRole meterRole;
    @Mock
    private Meter meter;
    @Mock
    private UsagePointMetrologyConfiguration metrologyConfiguration;
    @Mock
    private UsagePointMeterActivator usagePointMeterActivator;
    @Mock
    private UsagePointLifeCycleService usagePointLifeCycleService;
    @Mock
    private UsagePointTransition usagePointTransition;
    @Mock
    private PropertyValueInfoService propertyValueInfoService;
    @Mock
    private UsagePointMeterActivator linker;
    @Mock
    private PropertySpec transitionSpec;
    @Mock
    private TransactionService transactionService;
    @Mock
    private TransactionContext transactionContext;

    private MeteringDataImporterContext context;

    @Before
    public void initMocks() throws FileNotFoundException, URISyntaxException {
        when(meteringService.getLocationTemplate()).thenReturn(locationTemplate);
        when(templateFieldZipCode.getName()).thenReturn("zipCode");
        when(templateFieldZipCode.isMandatory()).thenReturn(false);
        when(templateFieldZipCode.getRanking()).thenReturn(13);
        when(templateFieldAddressDetail.getName()).thenReturn("addressDetail");
        when(templateFieldAddressDetail.isMandatory()).thenReturn(false);
        when(templateFieldAddressDetail.getRanking()).thenReturn(12);
        when(templateFieldEstablishmentNumber.getName()).thenReturn("establishmentNumber");
        when(templateFieldEstablishmentNumber.isMandatory()).thenReturn(false);
        when(templateFieldEstablishmentNumber.getRanking()).thenReturn(11);
        when(templateFieldEstablishmentName.getName()).thenReturn("establishmentName");
        when(templateFieldEstablishmentName.isMandatory()).thenReturn(false);
        when(templateFieldEstablishmentName.getRanking()).thenReturn(10);
        when(templateFieldCountryCode.getName()).thenReturn("countryCode");
        when(templateFieldCountryCode.isMandatory()).thenReturn(false);
        when(templateFieldCountryCode.getRanking()).thenReturn(0);
        when(templateFieldCountryName.getName()).thenReturn("countryName");
        when(templateFieldCountryName.isMandatory()).thenReturn(false);
        when(templateFieldCountryName.getRanking()).thenReturn(1);
        when(templateFieldAdministrativeArea.getName()).thenReturn("administrativeArea");
        when(templateFieldAdministrativeArea.isMandatory()).thenReturn(false);
        when(templateFieldAdministrativeArea.getRanking()).thenReturn(2);
        when(templateFieldEstablishmentType.getName()).thenReturn("establishmentType");
        when(templateFieldEstablishmentType.isMandatory()).thenReturn(false);
        when(templateFieldEstablishmentType.getRanking()).thenReturn(8);
        when(templateFieldLocality.getName()).thenReturn("locality");
        when(templateFieldLocality.isMandatory()).thenReturn(false);
        when(templateFieldLocality.getRanking()).thenReturn(3);
        when(templateFieldSubLocality.getName()).thenReturn("subLocality");
        when(templateFieldSubLocality.isMandatory()).thenReturn(false);
        when(templateFieldSubLocality.getRanking()).thenReturn(4);
        when(templateFieldStreetType.getName()).thenReturn("streetType");
        when(templateFieldStreetType.isMandatory()).thenReturn(false);
        when(templateFieldStreetType.getRanking()).thenReturn(5);
        when(templateFieldStreetName.getName()).thenReturn("streetName");
        when(templateFieldStreetName.isMandatory()).thenReturn(false);
        when(templateFieldStreetName.getRanking()).thenReturn(6);
        when(templateFieldStreetNumber.getName()).thenReturn("streetNumber");
        when(templateFieldStreetNumber.isMandatory()).thenReturn(false);
        when(templateFieldStreetNumber.getRanking()).thenReturn(7);
        when(templateFieldLocale.getName()).thenReturn("locale");
        when(templateFieldLocale.isMandatory()).thenReturn(false);
        when(templateFieldLocale.getRanking()).thenReturn(9);
        when(locationTemplate.getTemplateMembers()).thenReturn(Arrays.asList(templateFieldCountryCode, templateFieldCountryName, templateFieldAdministrativeArea,
                templateFieldSubLocality, templateFieldLocality, templateFieldStreetType, templateFieldStreetName, templateFieldStreetNumber, templateFieldEstablishmentType, templateFieldLocale,
                templateFieldEstablishmentName, templateFieldEstablishmentNumber, templateFieldAddressDetail, templateFieldZipCode));
        when(locationBuilder.getMemberBuilder(anyString())).thenReturn(Optional.empty());
        when(locationBuilder.member()).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setCountryCode(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setCountryName(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setAdministrativeArea(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setLocality(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setSubLocality(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setStreetType(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setStreetName(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setStreetNumber(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setEstablishmentType(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setEstablishmentName(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setEstablishmentNumber(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setAddressDetail(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setZipCode(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setLocality(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.setLocale(anyString())).thenReturn(locationMemberBuilder);
        when(locationMemberBuilder.isDaultLocation(anyBoolean())).thenReturn(locationMemberBuilder);

        when(meteringService.findUsagePointByMRID(anyString())).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByName(anyString())).thenReturn(Optional.empty());
        when(meteringService.getServiceCategory(Matchers.any(ServiceKind.class))).thenReturn(Optional.ofNullable(serviceCategoryTwo));
        when(threadPrincipalService.getLocale()).thenReturn(Locale.ENGLISH);
        when(meteringService.findServiceLocation(anyLong())).thenReturn(Optional.ofNullable(servicelocation));
        when(usagePointBuilder.create()).thenReturn(usagePoint);
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategoryTwo);
        when(usagePoint.getDetail(any(Instant.class))).thenReturn(Optional.empty());
        when(serviceCategoryTwo.newUsagePointDetail(any(), any())).thenReturn(usagePointDetail);
        when(serviceCategoryTwo.newUsagePoint(eq("DOA_UPS1_UP001"), any(Instant.class))).thenReturn(usagePointBuilder);
        when(serviceCategoryTwo.getKind()).thenReturn(ServiceKind.ELECTRICITY);
        when(usagePointBuilder.validate()).thenReturn(usagePoint);
        when(usagePointBuilder.newLocationBuilder()).thenReturn(locationBuilder);
        when(serviceCategoryTwo.getId()).thenReturn(34L);
        when(thesaurus.getFormat((Matchers.any(MessageSeeds.class)))).thenReturn(nlsMessageFormat);
        when(licenseService.getLicensedApplicationKeys()).thenReturn(Collections.singletonList("INS"));
        when(licenseService.getLicenseForApplication("INS")).thenReturn(Optional.ofNullable(license));
        when(clock.instant()).thenReturn(Instant.EPOCH);
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(nlsMessageFormat);
        when(thesaurus.getFormat(any(TranslationKey.class))).thenReturn(nlsMessageFormat);
        when(nlsMessageFormat.format()).thenReturn("message");
        when(nlsMessageFormat.format(anyInt(), anyInt())).thenReturn("message");
        when(usagePoint.newElectricityDetailBuilder(any(Instant.class))).thenReturn(usagePointDetailBuilder);
        when(usagePointDetailBuilder.withCollar(any(YesNoAnswer.class))).thenReturn(usagePointDetailBuilder);
        when(usagePointDetailBuilder.withGrounded(any(YesNoAnswer.class))).thenReturn(usagePointDetailBuilder);
        when(usagePointDetailBuilder.withNominalServiceVoltage(any(Quantity.class))).thenReturn(usagePointDetailBuilder);
        when(usagePointDetailBuilder.withPhaseCode(any(PhaseCode.class))).thenReturn(usagePointDetailBuilder);
        when(usagePointDetailBuilder.withRatedCurrent(any(Quantity.class))).thenReturn(usagePointDetailBuilder);
        when(usagePointDetailBuilder.withRatedPower(any(Quantity.class))).thenReturn(usagePointDetailBuilder);
        when(usagePointDetailBuilder.withEstimatedLoad(any(Quantity.class))).thenReturn(usagePointDetailBuilder);
        when(usagePointDetailBuilder.withLimiter(any(YesNoAnswer.class))).thenReturn(usagePointDetailBuilder);
        when(usagePointDetailBuilder.withLoadLimit(any(Quantity.class))).thenReturn(usagePointDetailBuilder);
        when(usagePointDetailBuilder.withLoadLimiterType(any(String.class))).thenReturn(usagePointDetailBuilder);
        when(usagePointDetailBuilder.withInterruptible(any(YesNoAnswer.class))).thenReturn(usagePointDetailBuilder);
        when(usagePoint.forCustomProperties()).thenReturn(usagePointCustomPropertySetExtension);
        when(usagePointCustomPropertySetExtension.getAllPropertySets()).thenReturn(Collections.emptyList());
        when(metrologyConfigurationService.findMetrologyConfiguration(anyString())).thenReturn(Optional.of(metrologyConfiguration));
        when(metrologyConfiguration.isActive()).thenReturn(true);
        when(metrologyConfiguration.getServiceCategory()).thenReturn(serviceCategoryTwo);
        when(usagePoint.linkMeters()).thenReturn(usagePointMeterActivator);
        when(usagePoint.getCreateDate()).thenReturn(Instant.now());
        when(metrologyConfigurationService.findMeterRole(anyString())).thenReturn(Optional.of(meterRole));
        when(meteringService.findMeterByName(anyString())).thenReturn(Optional.of(meter));
        when(usagePointMeterActivator.activate(meter, meterRole)).thenReturn(usagePointMeterActivator);
        when(usagePoint.getState()).thenReturn(anyObject());

        List<UsagePointTransition> transitions = new ArrayList<>();
        transitions.add(usagePointTransition);

        when(usagePointLifeCycleService.getAvailableTransitions(anyObject(), "INS")).thenReturn(transitions);
        when(usagePointTransition.getName()).thenReturn("install active");
        when(transactionService.getContext()).thenReturn(transactionContext);

        when(fileImportOccurrenceCorrect.getLogger()).thenReturn(logger);
        when(fileImportOccurrenceIncorrect.getLogger()).thenReturn(logger);
        when(fileImportOccurrenceCorrect.getContents()).thenReturn(new FileInputStream(new URI(getClass().getClassLoader().getResource("usagepoint_correct.csv").getFile()).getPath()));
        when(fileImportOccurrenceIncorrect.getContents()).thenReturn(new FileInputStream(new URI(getClass().getClassLoader().getResource("usagepoint_incorrect.csv").getFile()).getPath()));

        context = spy(new MeteringDataImporterContext());
        context.setMeteringService(meteringService);
        context.setCustomPropertySetService(customPropertySetService);
        context.setMetrologyConfigurationService(metrologyConfigurationService);
        context.setUsagePointLifeCycleService(usagePointLifeCycleService);
        context.setPropertyValueInfoService(propertyValueInfoService);
        context.setLicenseService(licenseService);
        context.setPropertySpecService(propertySpecService);
        context.setThreadPrincipalService(threadPrincipalService);
        context.setClock(clock);
        context.setTransactionService(transactionService);
        when(context.getThesaurus()).thenReturn(thesaurus);
    }

    @Test
    @Ignore
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
    public void testProcessWithInactiveMetrologyConfiguration() {
        String content = "id;serviceKind;lifeCycle;Created;MetrologyConfiguration;metrologyConfigurationTime\n" +
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

        verify(usagePoint, never()).apply(any(UsagePointMetrologyConfiguration.class), any(Instant.class));
        verify(logger, times(1)).warning(Matchers.anyString());
    }

    @Test
    public void testFailSetUnexistingMetrologyConfiguration() {
        String content = "id;serviceKind;lifeCycle;Created;MetrologyConfiguration;metrologyConfigurationTime\n" +
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
    @Ignore
    public void testSetMeterActivation() {
        String csv = "id;serviceKind;lifeCycle;Created;MetrologyConfiguration;metrologyConfigurationTime;meter1;meterrole1;activationDate1\n" +
                "DOA_UPS1_UP001;ELECTRICITY;28/07/2016 00:00;SP10_DEMO_1;28/07/2016 00:00;meter;meter.role.default;28/07/2016 00:00";
        FileImporter importer = createUsagePointImporter();
        FileImportOccurrence occurrence = mock(FileImportOccurrence.class);

        when(occurrence.getLogger()).thenReturn(logger);
        when(occurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes(Charset.forName("UTF-8"))));
        when(metrologyConfigurationService.findMetrologyConfiguration("SP10_DEMO_1")).thenReturn(Optional.of(metrologyConfiguration));
        when(usagePoint.linkMeters()).thenReturn(linker);
        when(meteringService.findMeterByName("meter")).thenReturn(Optional.of(meter));
        when(metrologyConfigurationService.findMeterRole("meter.role.default")).thenReturn(Optional.of(meterRole));

        importer.process(occurrence);

        verify(metrologyConfigurationService, atLeastOnce()).findMetrologyConfiguration("SP10_DEMO_1");
        verify(usagePoint, times(2)).linkMeters();
        verify(meteringService, times(2)).findMeterByName("meter");
        verify(metrologyConfigurationService, times(2)).findMeterRole("meter.role.default");
        verify(linker, times(2)).activate(any(Instant.class), eq(meter), eq(meterRole));
    }

    @Test
    @Ignore
    public void testPerformTransition() {
        String csv = "id;serviceKind;lifeCycle;Created;MetrologyConfiguration;metrologyConfigurationTime;meter1;meterrole1;activationDate1;transition;transitionDate;transitionConnectionState\n" +
                "DOA_UPS1_UP001;ELECTRICITY;28/07/2016 00:00;SP10_DEMO_1;28/07/2016 00:00;meter;meter.role.default;28/07/2016 00:00;Install active;28/07/2016 00:00;Connected";
        FileImporter importer = createUsagePointImporter();
        FileImportOccurrence occurrence = mock(FileImportOccurrence.class);
        ValueFactory valueFactory = mock(ValueFactory.class);
        PropertyInfo propertyInfo = mock(PropertyInfo.class);

        mockMeterActivation(occurrence, csv);
        when(usagePointLifeCycleService.getAvailableTransitions(any(UsagePoint.class), eq("INS"))).thenReturn(Collections.singletonList(usagePointTransition));
        when(usagePointTransition.getName()).thenReturn("Install active");
        when(usagePointTransition.getChecks()).thenReturn(Collections.emptySet());
        when(usagePointTransition.getMicroActionsProperties()).thenReturn(Collections.singletonList(transitionSpec));
        when(transitionSpec.getDisplayName()).thenReturn("connectionState");
        when(transitionSpec.getName()).thenReturn("connected");
        when(transitionSpec.getValueFactory()).thenReturn(valueFactory);
        when(valueFactory.fromStringValue("CONNECTED")).thenReturn(ConnectionState.CONNECTED);
        when(propertyValueInfoService.getPropertyInfos(anyList(), anyMap())).thenReturn(Collections.singletonList(propertyInfo));
        when(propertyValueInfoService.findPropertyValue(anyObject(), anyList())).thenReturn(new Object());
        when(context.getUsagePointLifeCycleService()).thenReturn(usagePointLifeCycleService);

        importer.process(occurrence);

        verify(context, times(3)).getUsagePointLifeCycleService();
        verify(usagePointLifeCycleService).scheduleTransition(eq(usagePoint), eq(usagePointTransition), any(Instant.class), anyString(), anyMap());
    }

    private void mockMeterActivation(FileImportOccurrence occurrence, String csv) {
        when(occurrence.getLogger()).thenReturn(logger);
        when(occurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes(Charset.forName("UTF-8"))));
        when(metrologyConfigurationService.findMetrologyConfiguration("SP10_DEMO_1")).thenReturn(Optional.of(metrologyConfiguration));
        when(usagePoint.linkMeters()).thenReturn(linker);
        when(meteringService.findMeterByName("meter")).thenReturn(Optional.of(meter));
        when(metrologyConfigurationService.findMeterRole("meter.role.default")).thenReturn(Optional.of(meterRole));
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

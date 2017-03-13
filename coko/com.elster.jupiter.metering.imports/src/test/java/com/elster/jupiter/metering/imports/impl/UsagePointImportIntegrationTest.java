/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.imports.impl.properties.SupportedNumberFormat;
import com.elster.jupiter.metering.imports.impl.usagepoint.UsagePointsImporterFactory;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointImportIntegrationTest {
    @Rule
    public TestRule transactionalRule = new TransactionalRule(inMemoryPersistence.getTransactionService());

    private static InMemoryIntegrationPersistence inMemoryPersistence;
    private static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = new InMemoryIntegrationPersistence();
        initializeMocks();
        inMemoryPersistence.initializeDataBase();
    }

    @AfterClass
    public static void cleanUp() {
        inMemoryPersistence.cleanUpDataBase();
    }

    private static void initializeMocks() {
        when(inMemoryPersistence.getClock().getZone()).thenReturn(utcTimeZone.toZoneId());
        when(inMemoryPersistence.getClock().instant()).thenAnswer(invocationOnMock -> Instant.now());
    }

    @Test
    @Transactional
    public void testUsagePointImport() {
        configureServices();
        FileImporter importer = createUsagePointImporter();
        String csv = "id;created;serviceKind;countryCode;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;isSDP;isVirtual;phaseCode;countryName;administrativeArea;locality;metrologyConfiguration;metrologyConfigurationTime;meterRole1;meter1;activationdate1;transition;transitionDate;transitionConnectionState;allowUpdate\n" +
                "UP_TEST;01/12/2016 00:00;electricity;code;subLocality;streetType;streetName;streetNumber;establishmentType;establishmentName;establishmentNumber;addressDetail;zipCode;locale;TRUE;FALSE;S1;US;California;Los Angeles;Residential net metering (consumption);01/12/2017 00:00;meter.role.default;DEVICE;01/12/2017 00:00;Install active;02/12/2017 00:00;Connected;FALSE\n";
        when(inMemoryPersistence.getClock().instant()).thenReturn(LocalDate.of(2015, 8, 1).atStartOfDay().toInstant(ZoneOffset.UTC));
        FileImportOccurrence occurrence = mockFileImportOccurrence(csv);

        importer.process(occurrence);

        //assertions
        UsagePoint usagePoint = inMemoryPersistence.getService(MeteringService.class).findUsagePointByName("UP_TEST").get();
        assertThat(usagePoint.getDetails()).hasSize(1);
        checkImportedDetails(usagePoint.getDetails().get(0));
        checkLocation(usagePoint);
        assertThat(usagePoint.getEffectiveMetrologyConfigurations()).hasSize(1);
        assertThat(usagePoint.getEffectiveMetrologyConfigurations().get(0).getMetrologyConfiguration().getName())
                .isEqualTo("Residential net metering (consumption)");
        assertThat(usagePoint.getCurrentMeterActivations()).hasSize(1);
        checkMeterActivation(usagePoint.getMeterActivations().get(0));
        checkUsagePointTransition(usagePoint);
    }

    private void checkImportedDetails(UsagePointDetail usagePointDetail) {
        assertThat(usagePointDetail).isInstanceOf(ElectricityDetail.class);
        assertThat(((ElectricityDetail) usagePointDetail).getPhaseCode().getValue()).isEqualTo("s1");
    }

    private void checkLocation(UsagePoint usagePoint) {
        assertThat(usagePoint.getLocation().isPresent());
        Location location = usagePoint.getLocation().get();
        assertThat(location.getMembers()).hasSize(1);
        LocationMember member = location.getMembers().get(0);
        assertThat(member.getCountryName()).isEqualTo("US");
        assertThat(member.getAdministrativeArea()).isEqualTo("California");
        assertThat(member.getLocality()).isEqualTo("Los Angeles");
        assertThat(member.getSubLocality()).isEqualTo("subLocality");
        assertThat(member.getStreetType()).isEqualTo("streetType");
        assertThat(member.getStreetName()).isEqualTo("streetName");
        assertThat(member.getStreetNumber()).isEqualTo("streetNumber");
        assertThat(member.getEstablishmentType()).isEqualTo("establishmentType");
        assertThat(member.getEstablishmentName()).isEqualTo("establishmentName");
        assertThat(member.getEstablishmentNumber()).isEqualTo("establishmentNumber");
        assertThat(member.getAddressDetail()).isEqualTo("addressDetail");
        assertThat(member.getZipCode()).isEqualTo("zipCode");
        assertThat(member.getLocale()).isEqualTo("locale");
        assertThat(member.isDefaultLocation()).isEqualTo(true);
    }

    private void checkMeterActivation(MeterActivation meterActivation) {
        assertThat(meterActivation.getMeterRole().isPresent());
        assertThat(meterActivation.getMeterRole().get().getKey()).isEqualTo("meter.role.default");
        assertThat(meterActivation.getMeter()).isPresent();
        assertThat(meterActivation.getMeter().get().getName()).isEqualTo("DEVICE");
    }

    private void checkUsagePointTransition(UsagePoint usagePoint) {
        UsagePointLifeCycleService usagePointLifeCycleService = inMemoryPersistence.getService(UsagePointLifeCycleService.class);
        assertThat(usagePointLifeCycleService.getHistory(usagePoint)).hasSize(1);
        UsagePointStateChangeRequest history = usagePointLifeCycleService.getHistory(usagePoint).get(0);
        assertThat(history.getFromStateName()).isEqualTo("Under construction");
        assertThat(history.getToStateName()).isEqualTo("Active");
        assertThat(history.getStatus()).isEqualTo(UsagePointStateChangeRequest.Status.SCHEDULED);
    }

    private void configureServices() {
        MeteringService meteringService = inMemoryPersistence.getService(MeteringService.class);
        MetrologyConfigurationService metrologyConfigurationService = inMemoryPersistence.getService(MetrologyConfigurationService.class);
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        metrologyConfigurationService.newUsagePointMetrologyConfiguration("Residential net metering (consumption)", serviceCategory)
                .create();
        AmrSystem system = meteringService.findAmrSystem(1).get();
        system.newMeter("DEVICE", "DEVICE").create();
        inMemoryPersistence.getService(MetrologyConfigurationService.class).findMetrologyConfiguration("Residential net metering (consumption)").get().activate();
        ThreadPrincipalService threadPrincipalService = inMemoryPersistence.getService(ThreadPrincipalService.class);
        UserService userService = inMemoryPersistence.getService(UserService.class);
        threadPrincipalService.set(() -> "console");
        Group group = userService.findOrCreateGroup("testGroup");
        userService.grantGroupWithPrivilege(group.getName(), "INS", new String[]{UsagePointTransition.Level.FOUR.getPrivilege()});
        User user = userService.findOrCreateUser("test", "domain", "directoryType");
        user.join(group);
        threadPrincipalService.set(user);
    }

    private FileImportOccurrence mockFileImportOccurrence(String csv) {
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        when(importOccurrence.getLogger()).thenReturn(mock(Logger.class));
        return importOccurrence;
    }

    private FileImporter createUsagePointImporter() {
        UsagePointsImporterFactory factory = new UsagePointsImporterFactory(getContext());
        Map<String, Object> properties = new HashMap<>();
        properties.put(DataImporterProperty.DELIMITER.getPropertyKey(), ";");
        properties.put(DataImporterProperty.DATE_FORMAT.getPropertyKey(), "dd/MM/yyyy HH:mm");
        properties.put(DataImporterProperty.TIME_ZONE.getPropertyKey(), "GMT+00:00");
        properties.put(DataImporterProperty.NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormat.SupportedNumberFormatValueFactory().fromStringValue("FORMAT4"));
        return factory.createImporter(properties);
    }

    private MeteringDataImporterContext getContext() {
        MeteringDataImporterContext context = new MeteringDataImporterContext();
        context.setThreadPrincipalService(inMemoryPersistence.getService(ThreadPrincipalService.class));
        context.setLicenseService(inMemoryPersistence.getService(LicenseService.class));
        context.setMeteringService(inMemoryPersistence.getService(MeteringService.class));
        context.setCustomPropertySetService(inMemoryPersistence.getService(CustomPropertySetService.class));
        context.setClock(inMemoryPersistence.getClock());
        context.setMetrologyConfigurationService(inMemoryPersistence.getService(MetrologyConfigurationService.class));
        context.setUsagePointLifeCycleService(inMemoryPersistence.getService(UsagePointLifeCycleService.class));
        context.setNlsService(inMemoryPersistence.getService(NlsService.class));
        return context;
    }

}

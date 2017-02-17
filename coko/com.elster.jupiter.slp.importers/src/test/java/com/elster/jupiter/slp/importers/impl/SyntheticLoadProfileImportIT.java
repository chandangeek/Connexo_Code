/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileBuilder;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileService;
import com.elster.jupiter.slp.importers.impl.syntheticloadprofile.SyntheticLoadProfileImporterFactory;
import com.elster.jupiter.slp.importers.impl.properties.SupportedNumberFormat;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SyntheticLoadProfileImportIT {

    private final Instant DATE = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    @Mock
    LicenseService licenseService;
    @Mock
    License license;
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private Injector injector;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    private SyntheticLoadProfileService syntheticLoadProfileService;
    private TransactionService transactionService;
    @Mock
    private Logger logger;
    @Mock
    private TimeService timeService;
    @Mock
    private FileImportOccurrence fileImportOccurrenceCorrect;
    @Mock
    private FileImportOccurrence fileImportOccurrenceIncorrectInterval;
    @Mock
    private FileImportOccurrence fileImportOccurrenceIncorrectDuration;

    private SyntheticLoadProfileDataImporterContext context;

    @Before
    public void setUp() {
        when(licenseService.getLicensedApplicationKeys()).thenReturn(Collections.singletonList("INS"));
        when(licenseService.getLicenseForApplication("INS")).thenReturn(Optional.ofNullable(license));

        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new NlsModule(),
                    new DataVaultModule(),
                    new EventsModule(),
                    new UserModule(),
                    new FileImportModule(),
                    new BasicPropertiesModule(),
                    new FiniteStateMachineModule(),
                    new UsagePointLifeCycleConfigurationModule(),
                    new CustomPropertySetsModule(),
                    new PartyModule(),
                    new SearchModule(),
                    new MeteringModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute((Transaction<Void>) () -> {
            syntheticLoadProfileService = injector.getInstance(SyntheticLoadProfileService.class);
            context = injector.getInstance(SyntheticLoadProfileDataImporterContext.class);
            return null;
        });

        try {
            when(fileImportOccurrenceCorrect.getLogger()).thenReturn(logger);
            when(fileImportOccurrenceIncorrectInterval.getLogger()).thenReturn(logger);
            when(fileImportOccurrenceIncorrectDuration.getLogger()).thenReturn(logger);
            when(fileImportOccurrenceCorrect.getContents()).thenReturn(new FileInputStream(getClass().getClassLoader()
                    .getResource("slp_syntheticloadprofile_correct.csv")
                    .getPath()));
            when(fileImportOccurrenceIncorrectInterval.getContents()).thenReturn(new FileInputStream(getClass().getClassLoader()
                    .getResource("slp_syntheticloadprofile_incorrectinterval.csv")
                    .getPath()));
            when(fileImportOccurrenceIncorrectDuration.getContents()).thenReturn(new FileInputStream(getClass().getClassLoader()
                    .getResource("slp_syntheticloadprofile_incorrectduration.csv")
                    .getPath()));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try (TransactionContext context = transactionService.getContext()) {
            for (int i = 1; i < 4; i++) {
                SyntheticLoadProfileBuilder builder = syntheticLoadProfileService.newSyntheticLoadProfile("slp" + i);
                builder.withDescription("synthetic load profile description");
                builder.withInterval(Duration.ofMinutes(15));
                builder.withDuration(Period.ofDays(1));
                builder.withStartTime(DATE);
                builder.build();
            }
            context.commit();
        }
    }

    @Test
    public void testImportCorrect() {
        try (TransactionContext context = transactionService.getContext()) {
            FileImporter importer = createSyntheticLoadProfileImporter();
            importer.process(fileImportOccurrenceCorrect);
            verify(logger, never()).info(Matchers.anyString());
            verify(logger, never()).warning(Matchers.anyString());
            verify(logger, never()).severe(Matchers.anyString());
        }
    }

    @Test
    public void testImportIncorrectInterval() {
        try (TransactionContext context = transactionService.getContext()) {
            FileImporter importer = createSyntheticLoadProfileImporter();
            importer.process(fileImportOccurrenceIncorrectInterval);
            verify(fileImportOccurrenceIncorrectInterval).markFailure("Import failed.");
            verify(logger, never()).info(Matchers.anyString());
            verify(logger, times(1)).severe(Matchers.anyString());
        }
    }


    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    private FileImporter createSyntheticLoadProfileImporter() {
        SyntheticLoadProfileImporterFactory factory = new SyntheticLoadProfileImporterFactory(context);
        Map<String, Object> properties = new HashMap<>();
        properties.put(DataImporterProperty.DELIMITER.getPropertyKey(), ";");
        properties.put(DataImporterProperty.DATE_FORMAT.getPropertyKey(), "dd/MM/yyyy HH:mm");
        properties.put(DataImporterProperty.TIME_ZONE.getPropertyKey(), "GMT+00:00");
        properties.put(DataImporterProperty.NUMBER_FORMAT.getPropertyKey(), new SupportedNumberFormat.SupportedNumberFormatValueFactory()
                .fromStringValue("FORMAT4"));
        return factory.createImporter(properties);
    }

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(LicenseService.class).toInstance(licenseService);
            bind(TimeService.class).toInstance(timeService);
        }
    }

}

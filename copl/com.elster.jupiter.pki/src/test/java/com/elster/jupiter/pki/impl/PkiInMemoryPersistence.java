package com.elster.jupiter.pki.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.ftpclient.FtpClientService;
import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pki.PassphraseFactory;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.PrivateKeyFactory;
import com.elster.jupiter.pki.SymmetricKeyFactory;
import com.elster.jupiter.pki.impl.importers.csr.CSRImporterFactory;
import com.elster.jupiter.pki.impl.wrappers.asymmetric.DataVaultPrivateKeyFactory;
import com.elster.jupiter.pki.impl.wrappers.symmetric.DataVaultPassphraseFactory;
import com.elster.jupiter.pki.impl.wrappers.symmetric.DataVaultSymmetricKeyFactory;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.mockito.Mockito.mock;

public class PkiInMemoryPersistence {

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private Injector injector;
    public static final Instant EPOCH = ZonedDateTime.of(2017, 4, 4, 13, 0, 0, 0, ZoneId.of("UTC")).toInstant();
    private static Clock clock = Clock.fixed(EPOCH, ZoneId.of("UTC"));
    private SecurityManagementService securityManagementService;

    public void activate() {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new OrmModule(),
                new DataVaultModule(),
                new DomainUtilModule(),
                new NlsModule(),
                new UserModule(),
                new UtilModule(clock),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new EventsModule(),
                new TimeModule(),
                new BasicPropertiesModule(),
                new TransactionModule(false),
                new InMemoryMessagingModule(),
                new PkiModule(),
                new FileImportModule()
        );
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(ThreadPrincipalService.class);
            injector.getInstance(UserService.class);
            securityManagementService = injector.getInstance(SecurityManagementService.class);
            ctx.commit();
        }
    }

    public void deactivate() {
        inMemoryBootstrapModule.deactivate();
    }

    public TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    public ThreadPrincipalService getThreadPrincipalService() {
        return injector.getInstance(ThreadPrincipalService.class);
    }

    public SecurityManagementService getSecurityManagementService() {
        return securityManagementService;
    }

    public UserService getUserService() {
        return injector.getInstance(UserService.class);
    }

    public PrivateKeyFactory getDataVaultPrivateKeyFactory() {
        return injector.getInstance(DataVaultPrivateKeyFactory.class);
    }

    public DataVaultService getDataVaultService() {
        return injector.getInstance(DataVaultService.class);
    }

    public SymmetricKeyFactory getDataVaultSymmetricKeyFactory() {
        return injector.getInstance(DataVaultSymmetricKeyFactory.class);
    }

    public PassphraseFactory getDataVaultPassphraseFactory() {
        return injector.getInstance(DataVaultPassphraseFactory.class);
    }

    public FileImportService getFileImportService() {
        return injector.getInstance(FileImportService.class);
    }

    public CSRImporterFactory getCSRImporterFactory() {
        return injector.getInstance(CSRImporterFactory.class);
    }

    public Clock getClock() {
        return clock;
    }

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(Thesaurus.class).toInstance(mock(Thesaurus.class));
            bind(MessageInterpolator.class).toInstance(mock(Thesaurus.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(FtpClientService.class).toInstance(mock(FtpClientService.class));
            bind(HsmEnergyService.class).toInstance(mock(HsmEnergyService.class));
            bind(HsmEncryptionService.class).toInstance(mock(HsmEncryptionService.class));
//            bind(Clock.class).toInstance(clock);
        }
    }}

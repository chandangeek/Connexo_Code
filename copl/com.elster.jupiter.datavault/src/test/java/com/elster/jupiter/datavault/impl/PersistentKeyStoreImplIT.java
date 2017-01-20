package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.KeyStoreService;
import com.elster.jupiter.datavault.PersistentKeyStore;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.json.JsonService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.time.Clock;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration test for the {@link PersistentKeyStoreImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-20 (15:06)
 */
public class PersistentKeyStoreImplIT {

    private static final char[] PASSWORD = new char[]{'a', '&', ';', 'p', 'W', '9', '@', 'j', 'B', '>', 'R', 'q', '<', 'K', 'I', '_'};

    private static InMemoryBootstrapModule bootstrapModule;
    private static EventAdmin eventAdmin;
    private static BundleContext bundleContext;
    private static TransactionService transactionService;
    private static KeyStoreService keyStoreService;
    private static NlsService nlsService;
    private static Publisher publisher;
    private static BeanService beanService;
    private static JsonService jsonService;

    @Rule
    public final TestRule transactional = new TransactionalRule(transactionService);
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @BeforeClass
    public static void initializeDatabase() {
        initializeStaticMocks();
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(PersistentKeyStoreImplIT.class.getSimpleName());
        Injector injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(principal),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new TransactionModule(false),
                new OrmModule(),
                new DataVaultModule());
        transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            injector.getInstance(OrmService.class);
            keyStoreService = injector.getInstance(KeyStoreService.class);
            ctx.commit();
        }
    }

    @AfterClass
    public static void cleanUpDataBase() {
        bootstrapModule.deactivate();
    }

    private static void initializeStaticMocks() {
        eventAdmin = mock(EventAdmin.class);
        bundleContext = mock(BundleContext.class);
        bootstrapModule = new InMemoryBootstrapModule();
        Thesaurus thesaurus = mock(Thesaurus.class);
        nlsService = mock(NlsService.class);
        when(nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(thesaurus);
        publisher = mock(Publisher.class);
        when(publisher.addThreadSubscriber(any())).thenReturn(() -> {});
        beanService = mock(BeanService.class);
        jsonService = mock(JsonService.class);
    }

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(NlsService.class).toInstance(nlsService);
            bind(Publisher.class).toInstance(publisher);
            bind(BeanService.class).toInstance(beanService);
            bind(JsonService.class).toInstance(jsonService);
            bind(FileSystem.class).toInstance(FileSystems.getDefault());
            bind(Clock.class).toInstance(Clock.systemDefaultZone());
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @Transactional
    @Test
    public void findUserDefinedWhenNoneDefined() {
        // Business method + asserts
        assertThat(keyStoreService.findUserDefinedKeyStores()).isEmpty();
    }

    @Transactional
    @Test
    public void findNonExistingSystemDefinedWhenNoneDefined() {
        // Business method + asserts
        assertThat(keyStoreService.findSystemDefined("DoesNotExist")).isEmpty();
    }

    @Transactional
    @Test
    public void createUserDefined() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        // Business method
        String expectedName = PersistentKeyStoreImplIT.class.getSimpleName();
        PersistentKeyStore keyStore = keyStoreService
                .newUserDefinedKeyStore(expectedName, "PKCS12")
                .build(PASSWORD);

        // Asserts
        assertThat(keyStore).isNotNull();
        assertThat(keyStore.getId()).isGreaterThan(0);
        assertThat(keyStore.getName()).isEqualTo(expectedName);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{com.elster.jupiter.validation.unique}")
    public void createUserDefinedWithDuplicateName() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        String expectedName = PersistentKeyStoreImplIT.class.getSimpleName();
        PersistentKeyStore keyStore = keyStoreService
                .newUserDefinedKeyStore(expectedName, "PKCS12")
                .build(PASSWORD);

        // Business method
        keyStoreService
                .newUserDefinedKeyStore(expectedName, "PKCS12")
                .build(PASSWORD);

        // Asserts: see expected exception rule
    }

    @Transactional
    @Test
    public void createSystemDefined() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        // Business method
        String expectedName = PersistentKeyStoreImplIT.class.getSimpleName();
        PersistentKeyStore keyStore = keyStoreService
                .newSystemDefinedKeyStore(expectedName, "PKCS12")
                .build(PASSWORD);

        // Asserts
        assertThat(keyStore).isNotNull();
        assertThat(keyStore.getId()).isGreaterThan(0);
        assertThat(keyStore.getName()).isEqualTo(expectedName);
    }

    @Transactional
    @Test
    public void findSystemDefined() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        String expectedName = PersistentKeyStoreImplIT.class.getSimpleName();
        PersistentKeyStore keyStore = keyStoreService
                .newSystemDefinedKeyStore(expectedName, "PKCS12")
                .build(PASSWORD);

        // Business method
        Optional<PersistentKeyStore> found = keyStoreService.findSystemDefined(expectedName);

        // Asserts
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(keyStore);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{com.elster.jupiter.validation.unique}")
    public void createSystemDefinedWithDuplicateName() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        String expectedName = PersistentKeyStoreImplIT.class.getSimpleName();
        PersistentKeyStore keyStore = keyStoreService
                .newSystemDefinedKeyStore(expectedName, "PKCS12")
                .build(PASSWORD);

        // Business method
        keyStoreService
                .newSystemDefinedKeyStore(expectedName, "PKCS12")
                .build(PASSWORD);

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void createSystemAndUserDefinedWithSameName() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        String expectedName = PersistentKeyStoreImplIT.class.getSimpleName();

        // Business method
        PersistentKeyStore systemDefined = keyStoreService
                .newSystemDefinedKeyStore(expectedName, "PKCS12")
                .build(PASSWORD);
        PersistentKeyStore userDefined = keyStoreService
                .newUserDefinedKeyStore(expectedName, "PKCS12")
                .build(PASSWORD);

        // Asserts
        assertThat(systemDefined).isNotNull();
        assertThat(userDefined).isNotNull();
    }

}
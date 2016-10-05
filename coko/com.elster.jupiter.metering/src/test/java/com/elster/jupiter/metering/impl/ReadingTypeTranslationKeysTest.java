package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.nls.impl.NlsServiceImpl;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link ReadingTypeTranslationKeys} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeTranslationKeysTest {

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private FiniteStateMachineService finiteStateMachineService;
    @Mock
    private PartyService partyService;
    @Mock
    private IdsService idsService;

    private Injector injector;
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    @Before
    public void setUp() throws SQLException {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new MeteringModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new NlsModule(),
                new CustomPropertySetsModule()
        );
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testAddAllTranslations() {
        injector.getInstance(TransactionService.class).execute(() -> {
            NlsServiceImpl nlsService = (NlsServiceImpl) injector.getInstance(NlsService.class);

            // Business method
            nlsService.addTranslationKeyProvider(new ReadingTypeTranslationKeyProvider());
            return null;
        });

        // Asserts
        Thesaurus thesaurus = injector.getInstance(NlsService.class).getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN);
        ReadingTypeTranslationKeys.allKeys().forEach(translationKey ->
                assertThat(thesaurus.getFormat(translationKey).format())
                .as("Translation for " + translationKey.getKey() + " with default format " + translationKey.getDefaultFormat() + " is null or empty")
                .isNotEmpty());
    }

    private static class ReadingTypeTranslationKeyProvider implements TranslationKeyProvider {
        @Override
        public String getComponentName() {
            return MeteringService.COMPONENTNAME;
        }

        @Override
        public Layer getLayer() {
            return Layer.DOMAIN;
        }

        @Override
        public List<TranslationKey> getKeys() {
            return ReadingTypeTranslationKeys.allKeys();
        }
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(FiniteStateMachineService.class).toInstance(finiteStateMachineService);
            bind(PartyService.class).toInstance(partyService);
            bind(IdsService.class).toInstance(idsService);
            bind(SearchService.class).toInstance(mock(SearchService.class));
            bind(PropertySpecService.class).toInstance(mock(PropertySpecService.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

}
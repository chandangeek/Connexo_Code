/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.util.Locale;
import java.util.function.Function;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NlsServiceIT {

    private static final String COMPONENT_NAME = "DUM";
    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private DataModel dataModel;
    private InMemoryBootstrapModule inMemoryBootstrapModule;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(DataModel.class).toProvider(() -> dataModel);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @Before
    public void setUp() throws SQLException {
        inMemoryBootstrapModule = new InMemoryBootstrapModule();
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new NlsModule());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        injector.getInstance(TransactionService.class).execute(new VoidTransaction() {
            @Override
            public void doPerform() {
                NlsService nlsService = injector.getInstance(NlsService.class);
                dataModel = ((NlsServiceImpl) nlsService).getDataModel();
            }
        });
    }

    @Test
    public void testTranslateServiceUsingThreadPrincipal() {
        NlsService nlsService = injector.getInstance(NlsService.class);
        final Thesaurus thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);

        injector.getInstance(ThreadPrincipalService.class).set(null, null, null, Locale.FRENCH);
        injector.getInstance(TransactionService.class).execute(new VoidTransaction() {
            @Override
            public void doPerform() {
                final SimpleNlsKey nlsKey = SimpleNlsKey.key(COMPONENT_NAME, Layer.DOMAIN, "voltage.max").defaultMessage("Maximum voltage");
                nlsService
                        .translate(nlsKey)
                        .to(Locale.GERMAN, "Höchstspannungs")
                        .to(Locale.FRENCH, "tension maximale")
                        .add();
            }
        });

        Thesaurus thesaurus2 = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
        assertThat(thesaurus2.getString("voltage.max", "WRONG")).isEqualTo("tension maximale");
        injector.getInstance(ThreadPrincipalService.class).set(null, null, null, Locale.GERMAN);
        assertThat(thesaurus2.getString("voltage.max", "WRONG")).isEqualTo("Höchstspannungs");
        injector.getInstance(ThreadPrincipalService.class).set(null, null, null, Locale.ENGLISH);
        assertThat(thesaurus2.getString("voltage.max", "WRONG")).isEqualTo("Maximum voltage");
    }

    @Test
    public void testTranslateServiceUsingSpecific() {
        NlsService nlsService = injector.getInstance(NlsService.class);
        final Thesaurus thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);

        injector.getInstance(ThreadPrincipalService.class).set(null, null, null, Locale.FRENCH);
        injector.getInstance(TransactionService.class).execute(new VoidTransaction() {
            @Override
            public void doPerform() {
                final SimpleNlsKey nlsKey = SimpleNlsKey.key(COMPONENT_NAME, Layer.DOMAIN, "voltage.max").defaultMessage("Maximum voltage.");
                nlsService
                        .translate(nlsKey)
                        .to(Locale.GERMAN, "Höchstspannungs")
                        .to(Locale.FRENCH, "tension maximale")
                        .add();
            }
        });

        Thesaurus thesaurus2 = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
        assertThat(thesaurus2.getString("voltage.max", "WRONG")).isEqualTo("tension maximale");

    }

    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testTranslateServiceSpecific() {
        NlsService nlsService = injector.getInstance(NlsService.class);
        final Thesaurus thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);

        injector.getInstance(ThreadPrincipalService.class).set(null, null, null, Locale.CANADA_FRENCH);
        injector.getInstance(TransactionService.class).execute(new VoidTransaction() {
            @Override
            public void doPerform() {
                final SimpleNlsKey nlsKey = SimpleNlsKey.key(COMPONENT_NAME, Layer.DOMAIN, "voltage.max").defaultMessage("Maximum voltage.");
                nlsService
                        .translate(nlsKey)
                        .to(Locale.GERMAN, "Höchstspannungs")
                        .to(Locale.FRENCH, "tension maximale")
                        .add();
            }
        });

        Thesaurus thesaurus2 = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
        assertThat(thesaurus2.getString("voltage.max", "WRONG")).isEqualTo("tension maximale");

    }

    @Test
    public void testMessageFormat() {
        NlsService nlsService = injector.getInstance(NlsService.class);
        nlsService.getThesaurus(COMPONENT_NAME, Layer.UI);
        final Thesaurus thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
        injector.getInstance(ThreadPrincipalService.class).set(null, null, null, Locale.FRANCE);
        injector.getInstance(TransactionService.class).execute(new VoidTransaction() {
            @Override
            public void doPerform() {
                final SimpleNlsKey nlsKey = SimpleNlsKey.key(COMPONENT_NAME, Layer.DOMAIN, "voltage.max").defaultMessage("Maximum voltage. : {0} V");
                nlsService
                        .translate(nlsKey)
                        .to(Locale.GERMAN, "Höchstspannungs : {0} V")
                        .to(Locale.FRENCH, "tension maximale : {0} V")
                        .add();
            }
        });

        NlsMessageFormat format = thesaurus.getFormat(messageSeed(145, "voltage.max", "Maximum voltage. : {0} V", Level.INFO));

        assertThat(format).isNotNull();
        assertThat(format.format(4000)).isEqualTo("DUM0145I tension maximale : 4\u00A0000 V");
    }

    @Test
    public void getStringAfterCopy() {
        String key = "getStringAfterCopy";
        String expectedTranslation = "Translation for: getStringAfterCopy";
        NlsService nlsService = injector.getInstance(NlsService.class);
        injector.getInstance(TransactionService.class).execute(new VoidTransaction() {
            @Override
            public void doPerform() {
                final SimpleNlsKey nlsKey = SimpleNlsKey.key(COMPONENT_NAME, Layer.DOMAIN, "voltage.max").defaultMessage("Maximum voltage");
                nlsService
                        .translate(nlsKey)
                        .to(Locale.GERMAN, "Höchstspannungs")
                        .to(Locale.FRENCH, "Tension maximale")
                        .add();
            }
        });
        Thesaurus thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
        thesaurus.getString(key, "no translation yet");
        NlsKey nlsKey = mock(NlsKey.class);
        when(nlsKey.getComponent()).thenReturn(COMPONENT_NAME);
        when(nlsKey.getLayer()).thenReturn(Layer.DOMAIN);
        when(nlsKey.getKey()).thenReturn(key);
        when(nlsKey.getDefaultMessage()).thenReturn(expectedTranslation);
        injector.getInstance(TransactionService.class).execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                nlsService.copy(nlsKey, COMPONENT_NAME, Layer.DOMAIN, Function.identity());
            }
        });

        // Business method
        String translation = thesaurus.getString(key, "other default");

        // Asserts
        assertThat(translation).isEqualTo(expectedTranslation);
    }

    private MessageSeed messageSeed(final int number, final String key, final String defaultFormat, final Level level) {
        return new MessageSeed() {
            @Override
            public int getNumber() {
                return number;
            }

            @Override
            public String getKey() {
                return key;
            }

            @Override
            public String getDefaultFormat() {
                return defaultFormat;
            }

            @Override
            public Level getLevel() {
                return level;
            }

            @Override
            public String getModule() {
                return "DUM";
            }
        };
    }
}

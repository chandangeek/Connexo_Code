package com.elster.jupiter.nls.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class NlsKeyIT {

    private static final String COMPONENT = "DUM";
    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private DataModel dataModel;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(DataModel.class).toProvider(new Provider<DataModel>() {
                @Override
                public DataModel get() {
                    return dataModel;
                }
            });
        }
    }

    @Before
    public void setUp() throws SQLException {
        injector = Guice.createInjector(
                new MockModule(),
                new InMemoryBootstrapModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new NlsModule());
        injector.getInstance(TransactionService.class).execute(new VoidTransaction() {
            @Override
            public void doPerform() {
                NlsService nlsService = injector.getInstance(NlsService.class);
                dataModel = ((NlsServiceImpl) nlsService).getDataModel();
            }
        });
    }

    @Test
    public void testNlsKeyPersistence() {
        injector.getInstance(TransactionService.class).execute(new VoidTransaction() {
            @Override
            public void doPerform() {
                Provider<NlsKey> provider = injector.getProvider(NlsKey.class);

                NlsKey nlsKey = provider.get().init(COMPONENT, Layer.DOMAIN, "fiets");

                nlsKey.setDefaultMessage("defaultMsg");
                nlsKey.add(Locale.FRENCH, "bicyclette");
                nlsKey.add(Locale.GERMAN, "Fahrrad");
                nlsKey.save();

            }
        });
        List<NlsKey> nlsKeys = dataModel.mapper(NlsKey.class).find();
        assertThat(nlsKeys).hasSize(1);
        NlsKey nlsKey = nlsKeys.get(0);
        assertThat(nlsKey.getComponent()).isEqualTo(COMPONENT);
        assertThat(nlsKey.getLayer()).isEqualTo(Layer.DOMAIN);
        assertThat(nlsKey.getKey()).isEqualTo("fiets");
        assertThat(nlsKey.getDefaultMessage()).isEqualTo("defaultMsg");
        assertThat(nlsKey.translate(Locale.GERMAN)).contains("Fahrrad");
        assertThat(nlsKey.translate(Locale.FRENCH)).contains("bicyclette");
    }

}

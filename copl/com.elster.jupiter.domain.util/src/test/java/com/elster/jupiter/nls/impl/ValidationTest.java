/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ValidationTest {

    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private static final boolean printSql = false;

    @Mock
    private BundleContext bundleContext;

    @BeforeClass
    public static void setUp() {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(printSql),
                new NlsModule()
        );
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(NlsService.class);
            ctx.commit();
        }
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    private NlsService getNlsService() {
        return injector.getInstance(NlsService.class);
    }

    private TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    @Test
    public void testCrud() {
        NlsService nlsService = getNlsService();
        try (TransactionContext context = getTransactionService().getContext()) {
            SimpleNlsKey nlsKey = SimpleNlsKey.key("DUM", Layer.DOMAIN, "empty");
            nlsService.translate(nlsKey)
                    .to(Locale.FRANCE, "vide ({javax.validation.constraints.NotNull.message})")
                    .add();
            nlsKey = SimpleNlsKey.key("DUM", Layer.DOMAIN, "min.size");
            nlsService.translate(nlsKey)
                    .to(Locale.FRANCE, "svp ne laissez pas {DUM.empty}, valeur minimal: {min}")
                    .add();

            context.commit();
        }
        ThreadPrincipalService threadPrincipalService = injector.getInstance(ThreadPrincipalService.class);
        threadPrincipalService.set(threadPrincipalService.getPrincipal(), "module", "action", Locale.FRANCE);
        Validator validator = ((NlsServiceImpl) nlsService).getDataModel().getValidatorFactory().getValidator();
        for (ConstraintViolation<?> violation : validator.validate(new Bean())) {
            String message = nlsService.interpolate(violation);
            if (violation.getConstraintDescriptor().getAnnotation().annotationType().equals(NotNull.class)) {
                assertThat(message).isEqualTo("vide (ne peut pas \u00eatre nul)");
            } else {
                assertThat(message).isEqualTo("svp ne laissez pas vide (ne peut pas \u00eatre nul), valeur minimal: 10");
            }
        }
    }

    public class Bean {
        @NotNull(message = "{DUM.empty}")
        private String name;
        @Size(message = "{DUM.min.size}", min = 10)
        private String description = "N/A";
    }

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

}
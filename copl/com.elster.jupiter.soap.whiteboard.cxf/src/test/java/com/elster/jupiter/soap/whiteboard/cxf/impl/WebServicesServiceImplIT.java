/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.Transaction;
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
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import javax.validation.ConstraintViolationException;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class WebServicesServiceImplIT {

    private static final String IMPORTER_NAME = "someImporter";
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private final Instant now = ZonedDateTime.of(2016, 1, 8, 10, 0, 0, 0, ZoneId.of("UTC")).toInstant();
    private Injector injector;

    private NlsService nlsService;
    private TransactionService transactionService;

    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();

    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private LogService logService;
    @Mock
    private MessageInterpolator messageInterpolator;
    @Mock
    private DataVaultService dataVaultService;
    @Mock
    private HttpService httpService;

    private Clock clock;
    @Mock
    private Thesaurus thesaurus;

    private WebServicesService webServicesService;
    private EndPointConfigurationService endPointConfigurationService;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(MessageInterpolator.class).toInstance(messageInterpolator);
            bind(DataVaultService.class).toInstance(dataVaultService);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(HttpService.class).toInstance(httpService);
        }
    }

    @Before
    public void setUp() {
        clock = new ProgrammableClock(ZoneId.of("UTC"), now);
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(clock),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new NlsModule(),
                    new UserModule(),
                    new WebServicesModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(new Transaction<Void>() {


            private EventService eventService;

            @Override
            public Void perform() {
                nlsService = injector.getInstance(NlsService.class);
                thesaurus = nlsService.getThesaurus(WebServicesServiceImpl.COMPONENT_NAME, Layer.DOMAIN);
                eventService = injector.getInstance(EventService.class);
                injector.getInstance(UserService.class);
                webServicesService = injector.getInstance(WebServicesService.class);
                endPointConfigurationService = injector.getInstance(EndPointConfigurationService.class);
                return null;
            }
        });
    }

    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void findWebServices() {
        assertThat(webServicesService.getWebServices()).isEmpty();
    }

    @Test
    public void findEndPoints() {
        assertThat(endPointConfigurationService.findEndPointConfigurations().find()).isEmpty();
    }

    @Test
    public void testCreateInboundEndpoint() {
        try (TransactionContext context = transactionService.getContext()) {
            EndPointConfiguration endPointConfiguration = endPointConfigurationService.newInboundEndPointConfiguration("service", "webservice", "/srv")
                    .setAuthenticationMethod(EndPointAuthentication.NONE).logLevel(LogLevel.SEVERE).create();
            assertThat(endPointConfigurationService.findEndPointConfigurations().find()).hasSize(1);
        }
    }

    @Test
    public void testCreateOutboundEndpoint() {
        try (TransactionContext context = transactionService.getContext()) {
            EndPointConfiguration endPointConfiguration = endPointConfigurationService.newOutboundEndPointConfiguration("service", "webservice", "/srv")
                    .setAuthenticationMethod(EndPointAuthentication.NONE).logLevel(LogLevel.INFO).create();
            assertThat(endPointConfigurationService.findEndPointConfigurations().find()).hasSize(1);
        }
    }

    @Test
    @Expected(ConstraintViolationException.class)
    public void testCreateOutboundEndpointMissingUserName() {
        try (TransactionContext context = transactionService.getContext()) {

            EndPointConfiguration endPointConfiguration = endPointConfigurationService.newOutboundEndPointConfiguration("service", "webservice", "/srv")
                    .setAuthenticationMethod(EndPointAuthentication.BASIC_AUTHENTICATION)
                    .logLevel(LogLevel.INFO)
                    .password("pass")
                    .create();

        }
    }

    @Test
    @Expected(ConstraintViolationException.class)
    public void testCreateOutboundEndpointMissingPassword() {
        try (TransactionContext context = transactionService.getContext()) {

            EndPointConfiguration endPointConfiguration = endPointConfigurationService.newOutboundEndPointConfiguration("service", "webservice", "/srv")
                    .setAuthenticationMethod(EndPointAuthentication.BASIC_AUTHENTICATION).username("user").create();

        }
    }
}

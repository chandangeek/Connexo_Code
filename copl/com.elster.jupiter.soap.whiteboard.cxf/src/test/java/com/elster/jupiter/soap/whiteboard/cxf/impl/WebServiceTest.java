package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.Transaction;
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

import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.mockito.Mock;

public class WebServiceTest {

    static final String IMPORTER_NAME = "someImporter";
    InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    final Instant now = ZonedDateTime.of(2016, 1, 8, 10, 0, 0, 0, ZoneId.of("UTC")).toInstant();
    Injector injector;

    NlsService nlsService;
    TransactionService transactionService;

    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();

    @Mock
    BundleContext bundleContext;
    @Mock
    EventAdmin eventAdmin;
    @Mock
    LogService logService;
    @Mock
    MessageInterpolator messageInterpolator;
    @Mock
    DataVaultService dataVaultService;
    @Mock
    HttpService httpService;

    Clock clock;
    @Mock
    Thesaurus thesaurus;

    DataModel dataModel;

    WebServicesDataModelService webServicesDataModelService;

    WebServicesService webServicesService;
    EndPointConfigurationService endPointConfigurationService;
    WebServiceCallOccurrenceService webServiceCallOccurrenceService;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(MessageInterpolator.class).toInstance(messageInterpolator);
            bind(DataVaultService.class).toInstance(dataVaultService);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(HttpService.class).toInstance(httpService);
            bind(DataModel.class).toProvider(()-> dataModel);
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
                    new AuditServiceModule(),
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
                webServicesDataModelService = injector.getInstance(WebServicesDataModelService.class);
                dataModel = webServicesDataModelService.getDataModel();
                webServicesService = injector.getInstance(WebServicesService.class);
                endPointConfigurationService = injector.getInstance(EndPointConfigurationService.class);
                webServiceCallOccurrenceService = injector.getInstance(WebServiceCallOccurrenceService.class);
                return null;
            }
        });
    }

    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }
}

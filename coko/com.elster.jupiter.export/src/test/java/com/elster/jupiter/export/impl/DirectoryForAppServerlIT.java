package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.impl.AppServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataFormatter;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ftpclient.impl.FtpModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.mail.impl.MailModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.cron.impl.DefaultCronExpressionParser;
import com.elster.jupiter.validation.impl.ValidationModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import javax.validation.ValidatorFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Clock;
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

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryForAppServerlIT {

    private AppServer appServer;
    @Mock
    private Group group;
    @Mock
    private HttpService httpService;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(LogService.class).toInstance(logService);
            bind(HttpService.class).toInstance(httpService);
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    public static final String FORMATTER = "formatter";

    private static final ZonedDateTime NOW = ZonedDateTime.of(2012, 10, 12, 9, 46, 12, 241615214, TimeZoneNeutral.getMcMurdo());

    @Rule
    public TestRule veryColdHere = Using.timeZoneOfMcMurdo();
    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private PropertySpec min, max, consZero;
    @Mock
    private LogService logService;
    @Mock
    private DataFormatterFactory dataFormatterFactory;
    @Mock
    private DataFormatter dataFormatter;
    @Mock
    private PropertySpec propertySpec;
    @Mock
    private User user;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private DataExportServiceImpl dataExportService;
    private AppService appService;
    private TransactionService transactionService;

    @Before
    public void setUp() throws SQLException {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new FiniteStateMachineModule(),
                    new UsagePointLifeCycleConfigurationModule(),
                    new MeteringModule(),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(Clock.fixed(NOW.toInstant(), ZoneId.systemDefault())),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new NlsModule(),
                    new ExportModule(),
                    new TimeModule(),
                    new TaskModule(),
                    new MeteringGroupsModule(),
                    new SearchModule(),
                    new UserModule(),
                    new WebServicesModule(),
                    new AppServiceModule(),
                    new BasicPropertiesModule(),
                    new MailModule(),
                    new BpmModule(),
                    new KpiModule(),
                    new ValidationModule(),
                    new DataVaultModule(),
                    new FtpModule(),
                    new CustomPropertySetsModule(),
                    new FileImportModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        when(userService.createUser(any(), any())).thenReturn(user);
//        when(userService.createGroup(anyString(), anyString())).thenReturn(mock(Group.class));
//        when(userService.findGroup(anyString())).thenReturn(Optional.of(group));
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            injector.getInstance(FiniteStateMachineService.class);
            dataExportService = (DataExportServiceImpl) injector.getInstance(DataExportService.class);
            appService = injector.getInstance(AppService.class);
            appServer = appService.createAppServer("AppServer", new DefaultCronExpressionParser().parse("0 0 * * * ? *").get());
            return null;
        });
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testSetPathForAppServer() {
        Path path = Paths.get("/usr/export");

        try (TransactionContext context = transactionService.getContext()) {
            dataExportService.setExportDirectory(appServer, path);

            context.commit();
        }

        assertThat(dataExportService.getExportDirectory(appServer)).contains(path);
    }

    @Test
    public void testUpdatePathForAppServer() {
        Path path = Paths.get("/usr/export");

        try (TransactionContext context = transactionService.getContext()) {
            dataExportService.setExportDirectory(appServer, Paths.get("/usr/wrong"));

            context.commit();
        }

        try (TransactionContext context = transactionService.getContext()) {
            dataExportService.setExportDirectory(appServer, path);

            context.commit();
        }

        assertThat(dataExportService.getExportDirectory(appServer)).contains(path);
    }

    @Test
    public void testRemovePathForAppServer(){
        Path path = Paths.get("/usr/export");

        try (TransactionContext context = transactionService.getContext()) {
            dataExportService.setExportDirectory(appServer, path);
            context.commit();
        }

        try (TransactionContext context = transactionService.getContext()) {
            dataExportService.removeExportDirectory(appServer);
            context.commit();
        }

        assertThat(dataExportService.getExportDirectory(appServer).isPresent()).isFalse();
    }
}
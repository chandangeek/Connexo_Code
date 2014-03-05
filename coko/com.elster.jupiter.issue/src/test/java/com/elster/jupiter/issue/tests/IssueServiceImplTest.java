package com.elster.jupiter.issue.tests;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.*;
import com.elster.jupiter.issue.impl.IssueImpl;
import com.elster.jupiter.issue.impl.IssueServiceImpl;
import com.elster.jupiter.issue.impl.IssueStatusImpl;
import com.elster.jupiter.issue.module.IssueModule;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.*;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IssueServiceImplTest {

    private static Injector injector;

    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    @Mock
    private static BundleContext bundleContext;
    @Mock
    private static EventAdmin eventAdmin;
    @Mock
    private DataModel dataModel;
    @Mock
    private DataMapper<HistoricalIssue> dataModelFactory;
    @Mock
    private QueryService queryService;
    @Mock
    private AppServer appServer;
    @Mock
    private AppService appService;
    @Mock
    private CronExpressionParser cronExpressionParser;

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {

            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(AppService.class).toInstance(appService);
            bind(CronExpressionParser.class).toInstance(cronExpressionParser);

        }
    }
    @Before
    public void setUp() throws SQLException {

        when(appService.getAppServer()).thenReturn(Optional.of(appServer));
        when(appService.createAppServer("issueAppServer", cronExpressionParser.parse("0 0 * * * ? *"))).thenReturn(appServer);

        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new IdsModule(),
                new MeteringModule(),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new NlsModule(),
                new UserModule(),
                new IssueModule()
        );

        when(dataModel.mapper(HistoricalIssue.class)).thenReturn(dataModelFactory);

        try (TransactionContext ctx = getTransactionService().getContext() ) {
            injector.getInstance(IssueService.class);
            ctx.commit();
        }
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    private IssueService getIssueService() {
        return injector.getInstance(IssueService.class);
    }

    private TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    @Test
    public void testIssueStatus()  {
        try (TransactionContext context = getTransactionService().getContext()) {
            IssueService issueService = getIssueService();
            issueService.createIssueStatus("TestStatus");
            IssueStatus status = issueService.getIssueStatusById(5).orNull();
            assertThat(status).isNotNull();
            assertThat(status.getName()).isEqualTo("TestStatus");
            context.commit();
        }
    }

    @Test
    public void testIssueReason()  {
        try (TransactionContext context = getTransactionService().getContext()) {
            IssueService issueService = getIssueService();
            issueService.createIssueReason("TestReason", "TestTopic");
            IssueReason reason = issueService.getIssueReasonById(5).orNull();
            assertThat(reason).isNotNull();
            assertThat(reason.getName()).isEqualTo("TestReason");
            context.commit();
        }
    }

    @Test
    public void testIssueStatusFromString()  {
        try (TransactionContext context = getTransactionService().getContext()) {
            IssueService issueService = getIssueService();
            IssueStatus statusCorrect = issueService.getIssueStatusFromString("Open");
            assertThat(statusCorrect).isNotNull();
            assertThat(statusCorrect.getName()).isEqualTo("Open");
            statusCorrect = issueService.getIssueStatusFromString("oPEn");
            assertThat(statusCorrect.getName()).isEqualTo("Open");
            IssueStatus statusNonexistent = issueService.getIssueStatusFromString("NonexistentStatus");
            assertThat(statusNonexistent).isNull();
            IssueStatus statusEmptyString = issueService.getIssueStatusFromString("");
            assertThat(statusEmptyString).isNull();
            context.commit();
        }
    }

    @Test
    public void testCreateIssue()  {
        try (TransactionContext context = getTransactionService().getContext()) {
            IssueService issueService = getIssueService();
            Issue issueNull = issueService.getIssueById(1).orNull();
            assertThat(issueNull).isNull();
            Map<String, String> map = new HashMap<String, String>();
            String topic = "com/energyict/mdc/isu/comtask/FAILURE";
            String device = "1";
            map.put("timestamp", "1");
            map.put("event.topics", topic);
            map.put("deviceIdentifier", device);
            issueService.createIssue(map);
            IssueStatus status = issueService.getIssueStatusFromString("Open");
            Issue issueCreate = issueService.getIssueById(1).orNull();
            assertThat(issueCreate).isNotNull();
            assertThat(issueCreate.getReason().getTopic()).isEqualTo(topic);
            assertThat(issueCreate.getStatus().getName()).isEqualTo("Open");
        }
    }

    @Test
    public void testCloseIssue()  {
        try (TransactionContext context = getTransactionService().getContext()) {
            IssueService issueService = getIssueService();
            Map<String, String> map = new HashMap<String, String>();
            map.put("timestamp", "1");
            map.put("event.topics", "com/energyict/mdc/isu/comtask/FAILURE");
            map.put("deviceIdentifier", "1");
            issueService.createIssue(map);
            IssueStatus status = issueService.getIssueStatusFromString("Open");
            Issue issueForClose = issueService.getIssueById(1).orNull();
            assertThat(issueForClose).isNotNull();
            IssueImpl.class.cast(issueForClose).setVersion(5);
            issueService.closeIssue(issueForClose.getId(), 1, status, "");
            issueForClose = issueService.getIssueById(issueForClose.getId()).orNull();
            assertThat(issueForClose).isNull();
        }
    }

}

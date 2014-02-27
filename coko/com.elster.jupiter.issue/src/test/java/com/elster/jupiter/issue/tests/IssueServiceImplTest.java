package com.elster.jupiter.issue.tests;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.IssueReason;
import com.elster.jupiter.issue.IssueService;
import com.elster.jupiter.issue.IssueStatus;
import com.elster.jupiter.issue.module.IssueModule;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
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

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class IssueServiceImplTest {

    private static Injector injector;

    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    @Mock
    private static BundleContext bundleContext;

    @Mock
    private static EventAdmin eventAdmin;

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {

            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
        }
    }
    @Before
    public void setUp() throws SQLException {

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
            issueService.createIssueReason("TestReason");
            IssueReason reason = issueService.getIssueReasonById(5).orNull();
            assertThat(reason).isNotNull();
            assertThat(reason.getName()).isEqualTo("TestReason");
            context.commit();
        }
    }

}

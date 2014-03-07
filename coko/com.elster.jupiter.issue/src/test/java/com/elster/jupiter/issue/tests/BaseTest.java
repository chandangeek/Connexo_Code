package com.elster.jupiter.issue.tests;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.impl.module.IssueModule;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.service.IssueMainService;
import com.elster.jupiter.issue.share.service.IssueMappingService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;

public class BaseTest {
    protected static Injector injector;

    protected static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    @Mock
    protected static BundleContext bundleContext;
    @Mock
    protected static EventAdmin eventAdmin;
    @Mock
    protected DataModel dataModel;
    @Mock
    protected DataMapper<HistoricalIssue> dataModelFactory;
    @Mock
    protected QueryService queryService;

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
            getInstallService();
            ctx.commit();
        }
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }
    private InstallService getInstallService(){
        return injector.getInstance(InstallService.class);
    }
    protected IssueService getIssueService() {
        return injector.getInstance(IssueService.class);
    }
    protected IssueMainService getIssueMainService(){
        return injector.getInstance(IssueMainService.class);
    }
    protected IssueMappingService getIssueInternalService(){
        return injector.getInstance(IssueMappingService.class);
    }
    protected TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }
}

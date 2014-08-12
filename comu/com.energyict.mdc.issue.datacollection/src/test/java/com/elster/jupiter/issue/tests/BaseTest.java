package com.elster.jupiter.issue.tests;

import com.energyict.mdc.device.data.DeviceDataService;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.datacollection.impl.IssueDataCollectionModule;
import com.elster.jupiter.issue.datacollection.impl.install.InstallServiceImpl;
import com.elster.jupiter.issue.impl.module.IssueModule;
import com.elster.jupiter.issue.impl.service.IssueCreationServiceImpl;
import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueMappingService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.json.JsonService;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.kie.api.io.KieResources;
import org.kie.internal.KnowledgeBaseFactoryService;
import org.kie.internal.builder.KnowledgeBuilderFactoryService;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import javax.validation.MessageInterpolator;

import org.junit.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore("Base functionality for all tests")
public class BaseTest {
    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));

            bind(KieResources.class).toInstance(mock(KieResources.class));
            bind(KnowledgeBaseFactoryService.class).toInstance(mock(KnowledgeBaseFactoryService.class));
            bind(KnowledgeBuilderFactoryService.class).toInstance(mock(KnowledgeBuilderFactoryService.class));
            bind(DeviceDataService.class).toInstance(mock(DeviceDataService.class));

            Thesaurus thesaurus = mock(Thesaurus.class);
            bind(Thesaurus.class).toInstance(thesaurus);
            bind(MessageInterpolator.class).toInstance(thesaurus);

            //TODO think about including this lines into IssueModule class
            TaskService taskService = mock(TaskService.class);
            bind(TaskService.class).toInstance(taskService);

            RecurrentTaskBuilder builder = mock(RecurrentTaskBuilder.class);
            when(taskService.newBuilder()).thenReturn(builder);
            when(builder.build()).thenReturn(mock(RecurrentTask.class));
        }
    }

    @BeforeClass
    public static void setEnvironment(){
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
                new IssueModule(),
                new IssueDataCollectionModule()
        );

        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            // initialize Issue tables
            injector.getInstance(com.elster.jupiter.issue.impl.service.InstallServiceImpl.class);
            injector.getInstance(InstallServiceImpl.class);
            ctx.commit();
        }
    }

    @AfterClass
    public static void deactivateEnvironment(){
        inMemoryBootstrapModule.deactivate();
    }

    protected TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }
    protected TransactionContext getContext(){
        return getTransactionService().getContext();
    }

    protected IssueService getIssueService() {
        return injector.getInstance(IssueService.class);
    }
    protected IssueMappingService getIssueMappingService(){
        return injector.getInstance(IssueMappingService.class);
    }
    protected IssueCreationService getIssueCreationService(){
        return injector.getInstance(IssueCreationService.class);
    }
    protected IssueActionService getIssueActionService(){
        return injector.getInstance(IssueActionService.class);
    }
    protected JsonService getJsonService() {
        return injector.getInstance(JsonService.class);
    }
    protected MeteringService getMeteringService() {
        return injector.getInstance(MeteringService.class);
    }
    protected DeviceDataService getDeviceDataService(){
        return injector.getInstance(DeviceDataService.class);
    }
    protected OrmService getOrmService(){
        return injector.getInstance(OrmService.class);
    }
    protected Thesaurus getThesaurus(){
        return injector.getInstance(Thesaurus.class);
    }
    protected Message getMockMessage(String payload) {
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());
        return message;
    }

    protected IssueCreationService getMockIssueCreationService() {
        return new MockIssueCreationService();
    }

    protected class MockIssueCreationService extends IssueCreationServiceImpl {
        @Override
        public void dispatchCreationEvent(IssueEvent event){
            throw new DispatchCreationEventException("processed!");
        }
    }

    protected static class DispatchCreationEventException extends RuntimeException{
        public DispatchCreationEventException(String message) {
            super(message);
        }
    }
}

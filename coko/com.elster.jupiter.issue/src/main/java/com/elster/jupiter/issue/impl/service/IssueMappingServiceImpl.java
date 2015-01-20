package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.impl.database.TableSpecs;
import com.elster.jupiter.issue.impl.module.Installer;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueMappingService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.google.inject.AbstractModule;
import org.kie.api.io.KieResources;
import org.kie.internal.KnowledgeBaseFactoryService;
import org.kie.internal.builder.KnowledgeBuilderFactoryService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component(
        name = "com.elster.jupiter.issue.mapping",
        service = {IssueMappingService.class, InstallService.class, TranslationKeyProvider.class},
        property = "name=" + IssueService.COMPONENT_NAME,
        immediate = true)
public class IssueMappingServiceImpl implements IssueMappingService, InstallService, TranslationKeyProvider {
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;
    private volatile MessageService messageService;
    private volatile MeteringService meteringService;
    private volatile UserService userService;
    private volatile TaskService taskService;
    private volatile QueryService queryService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;

    private volatile KnowledgeBuilderFactoryService knowledgeBuilderFactoryService;
    private volatile KnowledgeBaseFactoryService knowledgeBaseFactoryService;
    private volatile KieResources resourceFactoryService;

    private volatile IssueService issueService;
    private volatile IssueAssignmentService issueAssignmentService;
    private volatile IssueCreationService issueCreationService;
    private volatile IssueActionService issueActionService;

    private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();

    public IssueMappingServiceImpl(){}

    @Inject
    public IssueMappingServiceImpl(
            BundleContext bundleContext,
            OrmService ormService,
            NlsService nlsService,
            MessageService messageService,
            MeteringService meteringService,
            UserService userService,
            TaskService taskService,
            QueryService queryService,
            TransactionService transactionService,
            ThreadPrincipalService threadPrincipalService,
            KnowledgeBuilderFactoryService knowledgeBuilderFactoryService,
            KnowledgeBaseFactoryService knowledgeBaseFactoryService,
            KieResources resourceFactoryService
    ){

        setOrmService(ormService);
        setNlsService(nlsService);
        setMessageService(messageService);
        setMeteringService(meteringService);
        setUserService(userService);
        setTaskService(taskService);
        setQueryService(queryService);
        setTransactionService(transactionService);
        setThreadPrincipalService(threadPrincipalService);

        setKnowledgeBaseFactoryService(knowledgeBaseFactoryService);
        setKnowledgeBuilderFactoryService(knowledgeBuilderFactoryService);
        setResourceFactoryService(resourceFactoryService);

        this.activate(bundleContext);
        if (!dataModel.isInstalled()){
           install();
        }
    }

    @Activate
    public void activate(BundleContext bundleContext){
        // add tables
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
        createServices();
        registerDataModel();
        registerServices(bundleContext);
    }

    private final void registerDataModel(){
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(MessageService.class).toInstance(messageService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(UserService.class).toInstance(userService);
                bind(TaskService.class).toInstance(taskService);
                bind(NlsService.class).toInstance(nlsService);
                bind(QueryService.class).toInstance(queryService);

                bind(IssueService.class).toInstance(issueService);
                bind(IssueAssignmentService.class).toInstance(issueAssignmentService);
                bind(IssueCreationService.class).toInstance(issueCreationService);
                bind(IssueActionService.class).toInstance(issueActionService);
            }
        });
    }

    private final void createServices(){
        this.issueService = new IssueServiceImpl(queryService, userService, this, nlsService);
        this.issueActionService = new IssueActionServiceImpl(queryService, this, transactionService, nlsService);
        this.issueCreationService = new IssueCreationServiceImpl(nlsService, queryService, this, this.issueActionService, knowledgeBuilderFactoryService, knowledgeBaseFactoryService, resourceFactoryService);
        this.issueAssignmentService = new IssueAssignmentServiceImpl(knowledgeBuilderFactoryService, knowledgeBaseFactoryService, resourceFactoryService, nlsService, queryService, threadPrincipalService, transactionService, this);
    }

    private final void registerServices(BundleContext bundleContext){
        this.serviceRegistrations.add(bundleContext.registerService(IssueService.class, this.issueService, null));
        this.serviceRegistrations.add(bundleContext.registerService(IssueActionService.class, this.issueActionService, null));
        this.serviceRegistrations.add(bundleContext.registerService(IssueCreationService.class, this.issueCreationService, null));
        this.serviceRegistrations.add(bundleContext.registerService(IssueAssignmentService.class, this.issueAssignmentService, null));
    }

    @Deactivate
    public void deactivate() throws Exception {
        for (ServiceRegistration serviceRegistration : this.serviceRegistrations) {
            serviceRegistration.unregister();
        }
    }

    @Override
    public final void install() {
        dataModel.getInstance(Installer.class).install(true);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(
                UserService.COMPONENTNAME,
                TaskService.COMPONENTNAME,
                MessageService.COMPONENTNAME,
                OrmService.COMPONENTNAME,
                NlsService.COMPONENTNAME,
                MeteringService.COMPONENTNAME);
    }

    @Override
    public String getComponentName() {
        return IssueService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(IssueService.COMPONENT_NAME, "Issue Management");
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public final void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public final void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public final void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public final void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public final void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public final void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public final void setKnowledgeBuilderFactoryService(KnowledgeBuilderFactoryService knowledgeBuilderFactoryService) {
        this.knowledgeBuilderFactoryService = knowledgeBuilderFactoryService;
    }

    @Reference
    public final void setKnowledgeBaseFactoryService(KnowledgeBaseFactoryService knowledgeBaseFactoryService) {
        this.knowledgeBaseFactoryService = knowledgeBaseFactoryService;
    }

    @Reference
    public final void setResourceFactoryService(KieResources resourceFactoryService) {
        this.resourceFactoryService = resourceFactoryService;
    }

    @Reference
    public final void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    public DataModel getDataModel() {
        return this.dataModel;
    }
}

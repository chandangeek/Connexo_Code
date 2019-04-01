/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.impl;

import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionFactory;
import com.elster.jupiter.issue.share.entity.IssueActionClassLoadFailedException;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.issue.task.impl.actions.CloseIssueAction;
import com.elster.jupiter.tasks.TaskService;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.MessageInterpolator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.issue.task.actions.factory", service = IssueActionFactory.class, immediate = true)
public class TaskIssueActionsFactory implements IssueActionFactory {
    private static final Logger LOG = Logger.getLogger(TaskIssueActionsFactory.class.getName());
    public static final String ID = TaskIssueActionsFactory.class.getName();

    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile IssueService issueService;
    private volatile PropertySpecService propertySpecService;
    private volatile DataModel dataModel;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TaskService taskService;

    private Injector injector;
    private Map<String, Provider<? extends IssueAction>> actionProviders = new HashMap<>();

    // For OSGi purposes
    public TaskIssueActionsFactory() {
        super();
    }

    // For unit testing purposes
    @Inject
    public TaskIssueActionsFactory(OrmService ormService,
                                   NlsService nlsService,
                                   IssueService issueService,
                                   PropertySpecService propertySpecService,
                                   ThreadPrincipalService threadPrincipalService, TaskService taskService) {
        this();
        setOrmService(ormService);
        setThesaurus(nlsService);
        setIssueService(issueService);
        setPropertySpecService(propertySpecService);
        setThreadPrincipalService(threadPrincipalService);
        setTaskService(taskService);
        activate();
    }

    @Activate
    public void activate() {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(NlsService.class).toInstance(nlsService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(TaskService.class).toInstance(taskService);
                bind(IssueService.class).toInstance(issueService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
            }
        });

        addDefaultActions();
    }

    public IssueAction createIssueAction(String issueActionClassName) {
        Provider<? extends IssueAction> provider = actionProviders.get(issueActionClassName);
        if (provider == null) {
            throw new IssueActionClassLoadFailedException(thesaurus, issueActionClassName);
        }
        return provider.get();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Reference
    public final void setThesaurus(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.getDataModel(IssueService.COMPONENT_NAME).orElse(null);
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    private void addDefaultActions() {
        try {
            actionProviders.put(CloseIssueAction.class.getName(), injector.getProvider(CloseIssueAction.class));
            //TODO: Retry if requested - pending
        } catch (ConfigurationException | ProvisionException e) {
            LOG.warning(e.getMessage());
        }
    }
}

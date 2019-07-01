/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl;

import com.elster.jupiter.bpm.BpmService;
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
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.issue.impl.action.FailedAction;
import com.elster.jupiter.servicecall.issue.impl.action.PartialSucceedAction;
import com.elster.jupiter.servicecall.issue.impl.action.StartProcessAction;

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

@Component(name = "com.elster.jupiter.servicecall.issue.actions.factory", service = IssueActionFactory.class, immediate = true)
public class ServiceCallIssueActionsFactory implements IssueActionFactory {
    private static final Logger LOG = Logger.getLogger(ServiceCallIssueActionsFactory.class.getName());
    public static final String ID = ServiceCallIssueActionsFactory.class.getName();

    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile IssueService issueService;
    private volatile PropertySpecService propertySpecService;
    private volatile DataModel dataModel;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile ServiceCallService serviceCallService;
    private volatile BpmService bpmService;

    private Injector injector;
    private Map<String, Provider<? extends IssueAction>> actionProviders = new HashMap<>();

    // For OSGi purposes
    public ServiceCallIssueActionsFactory() {
        super();
    }

    // For unit testing purposes
    @Inject
    public ServiceCallIssueActionsFactory(OrmService ormService,
                                          NlsService nlsService,
                                          IssueService issueService,
                                          PropertySpecService propertySpecService,
                                          ThreadPrincipalService threadPrincipalService, ServiceCallService serviceCallService, BpmService bpmService) {
        this();
        setOrmService(ormService);
        setThesaurus(nlsService);
        setIssueService(issueService);
        setPropertySpecService(propertySpecService);
        setThreadPrincipalService(threadPrincipalService);
        setServiceCallService(serviceCallService);
        setBpmService(bpmService);
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
                bind(ServiceCallService.class).toInstance(serviceCallService);
                bind(IssueService.class).toInstance(issueService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(BpmService.class).toInstance(bpmService);
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

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    private void addDefaultActions() {
        try {
            actionProviders.put(StartProcessAction.class.getName(), injector.getProvider(StartProcessAction.class));
            actionProviders.put(FailedAction.class.getName(), injector.getProvider(FailedAction.class));
            actionProviders.put(PartialSucceedAction.class.getName(), injector.getProvider(PartialSucceedAction.class));
        } catch (ConfigurationException | ProvisionException e) {
            LOG.warning(e.getMessage());
        }
    }
}

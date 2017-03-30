/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl;

import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionFactory;
import com.elster.jupiter.issue.share.entity.IssueActionClassLoadFailedException;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskReportService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datacollection.impl.actions.CloseIssueAction;
import com.energyict.mdc.issue.datacollection.impl.actions.RetryCommunicationTaskAction;
import com.energyict.mdc.issue.datacollection.impl.actions.RetryCommunicationTaskNowAction;
import com.energyict.mdc.issue.datacollection.impl.actions.RetryConnectionTaskAction;

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

@Component(name = "com.energyict.mdc.issue.datacollection.actions.factory", service = IssueActionFactory.class, immediate = true)
public class DataCollectionActionsFactory implements IssueActionFactory {
    private static final Logger LOG = Logger.getLogger(DataCollectionActionsFactory.class.getName());
    public static final String ID = DataCollectionActionsFactory.class.getName();

    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile ConnectionTaskService connectionTaskService;
    private volatile CommunicationTaskReportService communicationTaskReportService;
    private volatile IssueService issueService;
    private volatile PropertySpecService propertySpecService;
    private volatile DataModel dataModel;
    private volatile ThreadPrincipalService threadPrincipalService;

    private Injector injector;
    private Map<String, Provider<? extends IssueAction>> actionProviders = new HashMap<>();

    // For OSGi purposes
    public DataCollectionActionsFactory() {
        super();
    }

    // For unit testing purposes
    @Inject
    public DataCollectionActionsFactory(OrmService ormService,
                                        NlsService nlsService,
                                        ConnectionTaskService connectionTaskService,
                                        CommunicationTaskReportService communicationTaskReportService,
                                        IssueService issueService,
                                        PropertySpecService propertySpecService,
                                        ThreadPrincipalService threadPrincipalService) {
        this();
        setOrmService(ormService);
        setThesaurus(nlsService);
        setConnectionTaskService(connectionTaskService);
        setCommunicationTaskReportService(communicationTaskReportService);
        setIssueService(issueService);
        setPropertySpecService(propertySpecService);
        setThreadPrincipalService(threadPrincipalService);

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
                bind(ConnectionTaskService.class).toInstance(connectionTaskService);
                bind(CommunicationTaskReportService.class).toInstance(communicationTaskReportService);
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
    public final void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    public final void setCommunicationTaskReportService(CommunicationTaskReportService communicationTaskReportService) {
        this.communicationTaskReportService = communicationTaskReportService;
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

    private void addDefaultActions() {
        try {
            actionProviders.put(CloseIssueAction.class.getName(), injector.getProvider(CloseIssueAction.class));
            actionProviders.put(RetryConnectionTaskAction.class.getName(), injector.getProvider(RetryConnectionTaskAction.class));
            actionProviders.put(RetryCommunicationTaskAction.class.getName(), injector.getProvider(RetryCommunicationTaskAction.class));
            actionProviders.put(RetryCommunicationTaskNowAction.class.getName(), injector.getProvider(RetryCommunicationTaskNowAction.class));
        } catch (ConfigurationException | ProvisionException e) {
            LOG.warning(e.getMessage());
        }
    }
}

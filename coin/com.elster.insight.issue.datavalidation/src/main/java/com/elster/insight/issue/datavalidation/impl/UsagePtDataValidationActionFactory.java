/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl;

import com.elster.jupiter.estimation.EstimationService;
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
import com.elster.insight.issue.datavalidation.impl.actions.CloseUsagePointIssueAction;
import com.elster.insight.issue.datavalidation.impl.actions.UsagePointRetryEstimationAction;

import com.google.inject.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.MessageInterpolator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Component(name = "com.elster.insight.issue.datavalidation.actions.factory", service = IssueActionFactory.class, immediate = true)
public class UsagePtDataValidationActionFactory implements IssueActionFactory {
    private static final Logger LOG = Logger.getLogger(UsagePtDataValidationActionFactory.class.getName());
    public static final String ID = UsagePtDataValidationActionFactory.class.getName();

    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile IssueService issueService;
    private volatile PropertySpecService propertySpecService;
    private volatile DataModel dataModel;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile EstimationService estimationService;

    private Injector injector;
    private Map<String, Provider<? extends IssueAction>> actionProviders = new HashMap<>();

    // For OSGi purposes
    public UsagePtDataValidationActionFactory() {
        super();
    }

    // For unit testing purposes
    @Inject
    public UsagePtDataValidationActionFactory(OrmService ormService,
                                              NlsService nlsService,
                                              IssueService issueService,
                                              PropertySpecService propertySpecService,
                                              ThreadPrincipalService threadPrincipalService,
                                              EstimationService estimationService) {
        this();
        setOrmService(ormService);
        setThesaurus(nlsService);
        setIssueService(issueService);
        setPropertySpecService(propertySpecService);
        setThreadPrincipalService(threadPrincipalService);
        setEstimationService(estimationService);
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
                bind(IssueService.class).toInstance(issueService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(EstimationService.class).toInstance(estimationService);
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
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    private void addDefaultActions() {
        try {
            actionProviders.put(CloseUsagePointIssueAction.class.getName(), injector.getProvider(CloseUsagePointIssueAction.class));
            actionProviders.put(UsagePointRetryEstimationAction.class.getName(), injector.getProvider(UsagePointRetryEstimationAction.class));
        } catch (ConfigurationException | ProvisionException e) {
            LOG.warning(e.getMessage());
        }
    }
}

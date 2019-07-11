/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl.actions;

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
import com.elster.jupiter.webservice.issue.WebServiceIssueService;

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

@Component(name = "com.elster.jupiter.webservice.issue.impl.actions.WebServiceIssueActionsFactory", service = IssueActionFactory.class, immediate = true)
public class WebServiceIssueActionsFactory implements IssueActionFactory {
    public static final String ID = WebServiceIssueActionsFactory.class.getName();
    private static final Logger LOG = Logger.getLogger(ID);

    private volatile DataModel dataModel;
    private volatile OrmService ormService;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile IssueService issueService;
    private volatile PropertySpecService propertySpecService;
    private volatile BpmService bpmService;
    private volatile ThreadPrincipalService threadPrincipalService;

    private Injector injector;
    private Map<String, Provider<? extends IssueAction>> actionProviders = new HashMap<>();

    public WebServiceIssueActionsFactory() {
        // For OSGi purposes
    }

    // For unit testing purposes
    @Inject
    public WebServiceIssueActionsFactory(OrmService ormService,
                                         NlsService nlsService,
                                         IssueService issueService,
                                         PropertySpecService propertySpecService,
                                         BpmService bpmService,
                                         ThreadPrincipalService threadPrincipalService) {
        this();
        setOrmService(ormService);
        setThesaurus(nlsService);
        setIssueService(issueService);
        setPropertySpecService(propertySpecService);
        setBpmService(bpmService);
        setThreadPrincipalService(threadPrincipalService);
        activate();
    }

    @Activate
    public void activate() {
        dataModel = ormService.getDataModel(IssueService.COMPONENT_NAME).orElse(null);
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(NlsService.class).toInstance(nlsService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(IssueService.class).toInstance(issueService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(BpmService.class).toInstance(bpmService);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
            }
        });

        addDefaultActions();
    }

    @Override
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
        this.thesaurus = nlsService.getThesaurus(WebServiceIssueService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
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
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    private void addDefaultActions() {
        try {
            actionProviders.put(CloseIssueAction.class.getName(), injector.getProvider(CloseIssueAction.class));
            actionProviders.put(StartProcessWebServiceIssueAction.class.getName(), injector.getProvider(StartProcessWebServiceIssueAction.class));
        } catch (ConfigurationException | ProvisionException e) {
            LOG.warning(e.getMessage());
        }
    }
}

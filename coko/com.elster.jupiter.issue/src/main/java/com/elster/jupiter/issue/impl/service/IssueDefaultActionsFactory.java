/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.issue.impl.actions.AssignIssueAction;
import com.elster.jupiter.issue.impl.actions.CommentIssueAction;
import com.elster.jupiter.issue.impl.actions.MailIssueAction;
import com.elster.jupiter.issue.impl.actions.ProcessAction;
import com.elster.jupiter.issue.impl.actions.WebServiceNotificationAction;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionFactory;
import com.elster.jupiter.issue.share.PropertyFactoriesProvider;
import com.elster.jupiter.issue.share.entity.IssueActionClassLoadFailedException;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.users.UserService;
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

@Component(name = "com.elster.jupiter.issue.actions.factory", service = IssueActionFactory.class, immediate = true)
public class IssueDefaultActionsFactory implements IssueActionFactory {
    private static final Logger LOG = Logger.getLogger(IssueDefaultActionsFactory.class.getName());
    public static final String ID = IssueDefaultActionsFactory.class.getName();

    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile UserService userService;
    private volatile IssueService issueService;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile PropertySpecService propertySpecService;
    private volatile DataModel dataModel;
    private volatile PropertyFactoriesProvider propertyFactoriesProvider;
    private volatile BpmService bpmService;

    private Injector injector;
    private Map<String, Provider<? extends IssueAction>> actionProviders = new HashMap<>();

    public IssueDefaultActionsFactory() {
    }

    @Inject
    public IssueDefaultActionsFactory(NlsService nlsService, UserService userService, IssueService issueService, ThreadPrincipalService threadPrincipalService, OrmService ormService, PropertySpecService propertySpecService, EndPointConfigurationService endPointConfigurationService, PropertyFactoriesProvider propertyFactoriesProvider, BpmService bpmService) {
        setThesaurus(nlsService);
        setUserService(userService);
        setIssueService(issueService);
        setThreadPrincipalService(threadPrincipalService);
        setOrmService(ormService);
        setPropertySpecService(propertySpecService);
        setEndPointConfigurationService(endPointConfigurationService);
        setPropertyFactoriesProvider(propertyFactoriesProvider);
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
                bind(UserService.class).toInstance(userService);
                bind(EndPointConfigurationService.class).toInstance(endPointConfigurationService);
                bind(IssueService.class).toInstance(issueService);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(PropertyFactoriesProvider.class).toInstance(propertyFactoriesProvider);
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
    public final void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public final void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference
    public void setPropertyFactoriesProvider(final PropertyFactoriesProvider propertyFactoriesProvider) {
        this.propertyFactoriesProvider = propertyFactoriesProvider;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.getDataModel(IssueService.COMPONENT_NAME).orElse(null);
    }

    @Reference
    public void setBpmService(final BpmService bpmService) {
        this.bpmService = bpmService;
    }

    private void addDefaultActions() {
        try {
            actionProviders.put(CommentIssueAction.class.getName(), injector.getProvider(CommentIssueAction.class));
            actionProviders.put(AssignIssueAction.class.getName(), injector.getProvider(AssignIssueAction.class));
            actionProviders.put(WebServiceNotificationAction.class.getName(), injector.getProvider(WebServiceNotificationAction.class));
            actionProviders.put(ProcessAction.class.getName(), injector.getProvider(ProcessAction.class));
            actionProviders.put(MailIssueAction.class.getName(), injector.getProvider(MailIssueAction.class));
        } catch (ConfigurationException | ProvisionException e) {
            LOG.warning(e.getMessage());
        }
    }
}
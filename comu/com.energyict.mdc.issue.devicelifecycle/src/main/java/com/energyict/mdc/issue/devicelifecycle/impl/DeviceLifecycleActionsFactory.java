/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl;

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
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.devicelifecycle.impl.actions.CloseIssueAction;
import com.energyict.mdc.issue.devicelifecycle.impl.actions.RetryTransitionAction;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


@Component(name = "com.energyict.mdc.issue.devicelifecycle.actions.factory", service = IssueActionFactory.class, immediate = true)
public class DeviceLifecycleActionsFactory implements IssueActionFactory {
    private static final Logger LOG = Logger.getLogger(DeviceLifecycleActionsFactory.class.getName());
    public static final String ID = DeviceLifecycleActionsFactory.class.getName();

    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile IssueService issueService;
    private volatile PropertySpecService propertySpecService;
    private volatile DataModel dataModel;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile DeviceService deviceService;
    private volatile DeviceLifeCycleService deviceLifecycleService;
    private volatile Clock clock;

    private Injector injector;
    private Map<String, Provider<? extends IssueAction>> actionProviders = new HashMap<>();

    // For OSGi purposes
    public DeviceLifecycleActionsFactory() {
        super();
    }

    // For unit testing purposes
    @Inject
    public DeviceLifecycleActionsFactory(OrmService ormService,
                                         NlsService nlsService,
                                         IssueService issueService,
                                         PropertySpecService propertySpecService,
                                         ThreadPrincipalService threadPrincipalService,
                                         DeviceService deviceService,
                                         DeviceLifeCycleService deviceLifecycleService,
                                         Clock clock) {
        this();
        setOrmService(ormService);
        setThesaurus(nlsService);
        setIssueService(issueService);
        setPropertySpecService(propertySpecService);
        setThreadPrincipalService(threadPrincipalService);
        setDeviceService(deviceService);
        setDeviceLifeCycleService(deviceLifecycleService);
        setClock(clock);
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
                bind(DeviceService.class).toInstance(deviceService);
                bind(DeviceLifeCycleService.class).toInstance(deviceLifecycleService);
                bind(Clock.class).toInstance(clock);
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
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setDeviceLifeCycleService(DeviceLifeCycleService deviceLifecycleService) {
        this.deviceLifecycleService = deviceLifecycleService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    private void addDefaultActions() {
        try {
            actionProviders.put(CloseIssueAction.class.getName(), injector.getProvider(CloseIssueAction.class));
            actionProviders.put(RetryTransitionAction.class.getName(), injector.getProvider(RetryTransitionAction.class));
        } catch (ConfigurationException | ProvisionException e) {
            LOG.warning(e.getMessage());
        }
    }
}

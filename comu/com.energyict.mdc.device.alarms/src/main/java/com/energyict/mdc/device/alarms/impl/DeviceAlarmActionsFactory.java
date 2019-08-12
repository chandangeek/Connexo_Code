/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl;

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
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.impl.actions.AssignDeviceAlarmAction;
import com.energyict.mdc.device.alarms.impl.actions.CloseDeviceAlarmAction;
import com.energyict.mdc.device.alarms.impl.actions.StartProcessAlarmAction;
import com.energyict.mdc.device.alarms.impl.actions.WebServiceNotificationAlarmAction;
import com.energyict.mdc.dynamic.PropertySpecService;

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

@Component(name = "com.energyict.mdc.device.alarms.actions.factory", service = IssueActionFactory.class, immediate = true)
public class DeviceAlarmActionsFactory implements IssueActionFactory {
    private static final Logger LOG = Logger.getLogger(DeviceAlarmActionsFactory.class.getName());
    public static final String ID = DeviceAlarmActionsFactory.class.getName();

    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile IssueService issueService;
    private volatile DeviceAlarmService deviceAlarmService;
    private volatile PropertySpecService propertySpecService;
    private volatile UserService userService;
    private volatile DataModel dataModel;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile BpmService bpmService;
    private volatile EndPointConfigurationService endPointConfigurationService;

    private Injector injector;
    private Map<String, Provider<? extends IssueAction>> actionProviders = new HashMap<>();

    // For OSGi purposes
    public DeviceAlarmActionsFactory() {
        super();
    }

    // For unit testing purposes
    @Inject
    public DeviceAlarmActionsFactory(OrmService ormService,
                                     NlsService nlsService,
                                     IssueService issueService,
                                     DeviceAlarmService deviceAlarmService,
                                     PropertySpecService propertySpecService,
                                     UserService userService,
                                     BpmService bpmService,
                                     ThreadPrincipalService threadPrincipalService,
                                     EndPointConfigurationService endPointConfigurationService) {
        this();
        setOrmService(ormService);
        setThesaurus(nlsService);
        setIssueService(issueService);
        setDeviceAlarmService(deviceAlarmService);
        setPropertySpecService(propertySpecService);
        setUserService(userService);
        setThreadPrincipalService(threadPrincipalService);
        setBpmService(bpmService);
        setEndPointConfigurationService(endPointConfigurationService);

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
                bind(DeviceAlarmService.class).toInstance(deviceAlarmService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(UserService.class).toInstance(userService);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(BpmService.class).toInstance(bpmService);
                bind(EndPointConfigurationService.class).toInstance(endPointConfigurationService);
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
        //this.thesaurus = nlsService.getThesaurus(IssueService.TASK_SERVICE_COMPONENT_NAME, Layer.DOMAIN);
        this.thesaurus = nlsService.getThesaurus(DeviceAlarmService.COMPONENT_NAME, Layer.DOMAIN);  // CONM-294 - close alarm
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public final void setDeviceAlarmService(DeviceAlarmService deviceAlarmService) {
        this.deviceAlarmService = deviceAlarmService;
    }

    @Reference
    public final void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
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
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    private void addDefaultActions() {
        try {
            actionProviders.put(StartProcessAlarmAction.class.getName(), injector.getProvider(StartProcessAlarmAction.class));
            actionProviders.put(AssignDeviceAlarmAction.class.getName(), injector.getProvider(AssignDeviceAlarmAction.class));
            actionProviders.put(CloseDeviceAlarmAction.class.getName(), injector.getProvider(CloseDeviceAlarmAction.class));
            actionProviders.put(WebServiceNotificationAlarmAction.class.getName(), injector.getProvider(WebServiceNotificationAlarmAction.class));
        } catch (ConfigurationException | ProvisionException e) {
            LOG.warning(e.getMessage());
        }
    }
}

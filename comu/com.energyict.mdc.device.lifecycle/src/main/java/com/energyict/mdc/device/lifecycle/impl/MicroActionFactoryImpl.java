/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.ActivateConnectionTasks;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.CancelAllServiceCalls;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.CloseAllIssues;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.CloseMeterActivation;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.DetachSlaveFromMaster;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.DisableCommunication;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.DisableEstimation;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.DisableValidation;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.EnableEstimation;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.EnableValidation;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.ForceValidationAndEstimation;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.LinkToUsagePoint;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.ActivateAllRecurringCommunications;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.RemoveDevice;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.RemoveDeviceFromStaticGroups;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.RemoveLocation;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.SetLastReading;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.SetMultiplier;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.ActivateAllCommunication;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.StartCommunication;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.StartRecurringCommunication;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link ServerMicroActionFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-10 (17:28)
 */
@Component(name = "com.energyict.device.lifecycle.micro.action.factory", service = ServerMicroActionFactory.class)
@SuppressWarnings("unused")
public class MicroActionFactoryImpl implements ServerMicroActionFactory {

    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile TopologyService topologyService;
    private volatile ValidationService validationService;
    private volatile EstimationService estimationService;
    private volatile IssueService issueService;
    private volatile IssueDataCollectionService issueDataCollectionService;
    private volatile ServiceCallService serviceCallService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile DeviceService deviceService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile ConnectionTaskService connectionTaskService;

    // For OSGi purposes only
    public MicroActionFactoryImpl() {
        super();
    }

    // For unit testing purposes
    @Inject
    public MicroActionFactoryImpl(NlsService nlsService,
                                  MeteringService meteringService,
                                  MeteringGroupsService meteringGroupsService,
                                  TopologyService topologyService,
                                  ValidationService validationService,
                                  EstimationService estimationService,
                                  IssueService issueService,
                                  MetrologyConfigurationService metrologyConfigurationService,
                                  DeviceService deviceService,
                                  CommunicationTaskService communicationTaskService,
                                  ConnectionTaskService connectionTaskService) {
        this();
        this.setNlsService(nlsService);
        this.setMeteringService(meteringService);
        this.setMeteringGroupsService(meteringGroupsService);
        this.setTopologyService(topologyService);
        this.setValidationService(validationService);
        this.setEstimationService(estimationService);
        this.setIssueService(issueService);
        this.setMetrologyConfigurationService(metrologyConfigurationService);
        this.setDeviceService(deviceService);
        this.setCommunicationTaskService(communicationTaskService);
        this.setConnectionTaskService(connectionTaskService);
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceLifeCycleService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Reference
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Override
    public ServerMicroAction from(MicroAction microAction) {
        switch (microAction) {
            case SET_LAST_READING: {
                return new SetLastReading(thesaurus);
            }
            case ENABLE_VALIDATION: {
                return new EnableValidation(thesaurus);
            }
            case DISABLE_VALIDATION: {
                return new DisableValidation(thesaurus);
            }
            case ACTIVATE_CONNECTION_TASKS_IN_USE: {
                return new ActivateConnectionTasks(thesaurus);
            }
            case CLOSE_METER_ACTIVATION: {
                return new CloseMeterActivation(thesaurus);
            }
            case START_COMMUNICATION: {
                return new StartCommunication(thesaurus);
            }
            case DISABLE_COMMUNICATION: {
                return new DisableCommunication(thesaurus, deviceService, this.communicationTaskService, this.connectionTaskService);
            }
            case DETACH_SLAVE_FROM_MASTER: {
                return new DetachSlaveFromMaster(thesaurus, this.topologyService);
            }
            case REMOVE_DEVICE_FROM_STATIC_GROUPS: {
                return new RemoveDeviceFromStaticGroups(thesaurus, this.meteringService, this.meteringGroupsService);
            }
            case ENABLE_ESTIMATION: {
                return new EnableEstimation(thesaurus);
            }
            case DISABLE_ESTIMATION: {
                return new DisableEstimation(thesaurus);
            }
            case FORCE_VALIDATION_AND_ESTIMATION: {
                return new ForceValidationAndEstimation(thesaurus, this.validationService, this.estimationService);
            }
            case START_RECURRING_COMMUNICATION: {
                return new StartRecurringCommunication(thesaurus, this.communicationTaskService, this.connectionTaskService);
            }
            case CLOSE_ALL_ISSUES: {
                return new CloseAllIssues(thesaurus, issueService);
            }
            case REMOVE_DEVICE: {
                return new RemoveDevice(thesaurus);
            }
            case SET_MULTIPLIER: {
                return new SetMultiplier(thesaurus);
            }
            case REMOVE_LOCATION: {
                return new RemoveLocation(thesaurus);
            }
            case CANCEL_ALL_SERVICE_CALLS: {
                return new CancelAllServiceCalls(thesaurus, serviceCallService);
            }
            case LINK_TO_USAGE_POINT: {
                return new LinkToUsagePoint(thesaurus, metrologyConfigurationService);
            }
            case ACTIVATE_ALL_COMMUNICATION: {
                return new ActivateAllCommunication(thesaurus, deviceService, this.communicationTaskService, this.connectionTaskService);
            }
            case ACTIVATE_ALL_RECURRING_COMMUNICATION: {
                return new ActivateAllRecurringCommunications(thesaurus, deviceService, this.communicationTaskService, this.connectionTaskService);
            }
            default: {
                throw new IllegalArgumentException("Unknown or unsupported MicroAction " + microAction.name());
            }
        }
    }
}
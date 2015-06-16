package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.*;
import com.energyict.mdc.device.topology.TopologyService;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
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

    private volatile MeteringService meteringService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile TopologyService topologyService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;
    private volatile ValidationService validationService;
    private volatile EstimationService estimationService;

    // For OSGi purposes only
    public MicroActionFactoryImpl() {
        super();
    }

    // For unit testing purposes
    @Inject
    public MicroActionFactoryImpl(MeteringService meteringService, MeteringGroupsService meteringGroupsService, TopologyService topologyService) {
        this();
        this.setMeteringService(meteringService);
        this.setMeteringGroupsService(meteringGroupsService);
        this.setTopologyService(topologyService);
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
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @Override
    public ServerMicroAction from(MicroAction microAction) {
        switch (microAction) {
            case SET_LAST_READING: {
                return new SetLastReading();
            }
            case ENABLE_VALIDATION: {
                return new EnableValidation();
            }
            case DISABLE_VALIDATION: {
                return new DisableValidation();
            }
            case ACTIVATE_CONNECTION_TASKS: {
                return new ActivateConnectionTasks();
            }
            case CREATE_METER_ACTIVATION: {
                return new CreateMeterActivation();
            }
            case CLOSE_METER_ACTIVATION: {
                return new CloseMeterActivation();
            }
            case START_COMMUNICATION: {
                return new StartCommunication();
            }
            case DISABLE_COMMUNICATION: {
                return new DisableCommunication();
            }
            case DETACH_SLAVE_FROM_MASTER: {
                return new DetachSlaveFromMaster(this.topologyService);
            }
            case REMOVE_DEVICE_FROM_STATIC_GROUPS: {
                return new RemoveDeviceFromStaticGroups(this.meteringService, this.meteringGroupsService);
            }
            case ENABLE_ESTIMATION: {
                return new EnableEstimation();
            }
            case FORCE_VALIDATION_AND_ESTIMATION:
                return new ForceValidationAndEstimation(this.threadPrincipalService, this.transactionService, this.validationService, this.estimationService);
            default: {
                throw new IllegalArgumentException("Unknown or unsupported MicroAction " + microAction.name());
            }
        }
    }

}
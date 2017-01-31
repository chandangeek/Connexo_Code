/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.ActiveConnectionAvailable;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.AllDataValid;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.AllDataValidated;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.AllIssuesAreClosed;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.AllLoadProfileDataCollected;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.ConnectionPropertiesAreValid;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.DefaultConnectionTaskAvailable;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.DeviceIsLinkedWithUsagePoint;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.GeneralProtocolPropertiesAreValid;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.NoActiveServiceCalls;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.ProtocolDialectPropertiesAreValid;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.ScheduledCommunicationTaskAvailable;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.SecurityPropertiesAreValid;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.SharedScheduledCommunicationTaskAvailable;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.SlaveDeviceHasGateway;
import com.energyict.mdc.device.topology.TopologyService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link ServerMicroCheckFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-10 (17:28)
 */
@Component(name = "com.energyict.device.lifecycle.micro.check.factory", service = ServerMicroCheckFactory.class)
@SuppressWarnings("unused")
public class MicroCheckFactoryImpl implements ServerMicroCheckFactory {

    private volatile Thesaurus thesaurus;
    private volatile TopologyService topologyService;
    private volatile ValidationService validationService;
    private volatile MeteringService meteringService;
    private volatile ServiceCallService serviceCallService;

    // For OSGi purposes
    public MicroCheckFactoryImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public MicroCheckFactoryImpl(NlsService nlsService, TopologyService topologyService, ValidationService validationService, MeteringService meteringService) {
        this();
        this.setNlsService(nlsService);
        this.setTopologyService(topologyService);
        this.setValidationService(validationService);
        this.setMeteringService(meteringService);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceLifeCycleService.COMPONENT_NAME, Layer.DOMAIN);
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
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Override
    public ServerMicroCheck from(MicroCheck check) {
        switch (check) {
            case DEFAULT_CONNECTION_AVAILABLE: {
                return new DefaultConnectionTaskAvailable(this.thesaurus);
            }
            case AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE: {
                return new ScheduledCommunicationTaskAvailable(this.thesaurus);
            }
            case AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE: {
                return new SharedScheduledCommunicationTaskAvailable(this.thesaurus);
            }
            case ALL_LOAD_PROFILE_DATA_COLLECTED: {
                return new AllLoadProfileDataCollected(this.thesaurus, meteringService);
            }
            case ALL_DATA_VALID: {
                return new AllDataValid(this.validationService, this.thesaurus);
            }
            case PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID: {
                return new ProtocolDialectPropertiesAreValid(this.thesaurus);
            }
            case GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID: {
                return new GeneralProtocolPropertiesAreValid(this.thesaurus);
            }
            case SECURITY_PROPERTIES_ARE_ALL_VALID: {
                return new SecurityPropertiesAreValid(this.thesaurus);
            }
            case CONNECTION_PROPERTIES_ARE_ALL_VALID: {
                return new ConnectionPropertiesAreValid(this.thesaurus);
            }
            case SLAVE_DEVICE_HAS_GATEWAY: {
                return new SlaveDeviceHasGateway(this.thesaurus, this.topologyService);
            }
            case LINKED_WITH_USAGE_POINT: {
                return new DeviceIsLinkedWithUsagePoint(this.thesaurus);
            }
            case ALL_ISSUES_AND_ALARMS_ARE_CLOSED: {
                return new AllIssuesAreClosed(this.thesaurus);
            }
            case ALL_DATA_VALIDATED: {
                return new AllDataValidated(this.validationService, this.thesaurus);
            }
            case AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE: {
                return new ActiveConnectionAvailable(this.thesaurus);
            }
            case NO_ACTIVE_SERVICE_CALLS: {
                return new NoActiveServiceCalls(thesaurus, serviceCallService);
            }
            default: {
                throw new IllegalArgumentException("Unknown or unsupported MicroCheck: " + check);
            }
        }
    }

}
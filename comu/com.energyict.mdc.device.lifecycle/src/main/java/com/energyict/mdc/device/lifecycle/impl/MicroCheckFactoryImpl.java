package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.AllIssuesAreClosed;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.ConnectionPropertiesAreValid;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.DefaultConnectionTaskAvailable;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.DeviceIsLinkedWithUsagePoint;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.GeneralProtocolPropertiesAreValid;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.LastReadingTimestampSet;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.ProtocolDialectPropertiesAreValid;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.ManuallyScheduledCommunicationTaskAvailable;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.ScheduledCommunicationTaskAvailable;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.SecurityPropertiesAreValid;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.SlaveDeviceHasGateway;
import com.energyict.mdc.device.topology.TopologyService;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
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

    private Thesaurus thesaurus;
    private TopologyService topologyService;

    // For OSGi purposes
    public MicroCheckFactoryImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public MicroCheckFactoryImpl(NlsService nlsService, TopologyService topologyService) {
        this();
        this.setNlsService(nlsService);
        this.setTopologyService(topologyService);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceLifeCycleService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Override
    public ServerMicroCheck from(MicroCheck check) {
        switch (check) {
            case DEFAULT_CONNECTION_AVAILABLE: {
                return new DefaultConnectionTaskAvailable(this.thesaurus);
            }
            case AT_LEAST_ONE_MANUALLY_SCHEDULED_COMMUNICATION_TASK_AVAILABLE: {
                return new ManuallyScheduledCommunicationTaskAvailable(this.thesaurus);
            }
            case AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE: {
                return new ScheduledCommunicationTaskAvailable(this.thesaurus);
            }
            case LAST_READING_TIMESTAMP_SET: {
                return new LastReadingTimestampSet(this.thesaurus);
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
            default: {
                throw new IllegalArgumentException("Unknown or unsupported MicroCheck: " + check);
            }
        }
    }

}
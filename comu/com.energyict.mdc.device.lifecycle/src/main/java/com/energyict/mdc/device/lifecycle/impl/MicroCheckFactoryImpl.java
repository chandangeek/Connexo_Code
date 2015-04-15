package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.DefaultConnectionTaskAvailable;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.LastReadingTimestampSet;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.ScheduledCommunicationTaskAvailable;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
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

    // For OSGi purposes
    public MicroCheckFactoryImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public MicroCheckFactoryImpl(NlsService nlsService, ThreadPrincipalService threadPrincipalService, BpmService bpmService, ServerMicroCheckFactory microCheckFactory, ServerMicroActionFactory microActionFactory, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this();
        this.setNlsService(nlsService);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceLifeCycleService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public ServerMicroCheck from(MicroCheck check) {
        switch (check) {
            case DEFAULT_CONNECTION_AVAILABLE: {
                return new DefaultConnectionTaskAvailable(this.thesaurus);
            }
            case AT_LEAST_ONE_COMMUNICATION_TASK_SCHEDULED: {
                return new ScheduledCommunicationTaskAvailable(this.thesaurus);
            }
            case LAST_READING_TIMESTAMP_SET: {
                return new LastReadingTimestampSet(this.thesaurus);
            }
            default: {
                return null;
            }
        }
    }

}
package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;

/**
 * Provides an implementation for the {@link ConnectionInitiationTask} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-16 (14:19)
 */
public class ConnectionInitiationTaskImpl extends OutboundConnectionTaskImpl<PartialConnectionInitiationTask> implements ConnectionInitiationTask {

    @Inject
    protected ConnectionInitiationTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, DeviceDataService deviceDataService, Provider<ConnectionMethodImpl> connectionMethodProvider) {
        super(dataModel, eventService, thesaurus, clock, deviceDataService, connectionMethodProvider);
    }

    @Override
    public ComChannel connect(ComPort comPort) throws ConnectionException {
        List<ConnectionProperty> connectionTaskProperties = this.toConnectionProperties(this.getProperties());
        return this.getConnectionType().connect(connectionTaskProperties);
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        this.getConnectionType().disconnect(comChannel);
    }

    @Override
    public int getMaxNumberOfTries() {
        return DEFAULT_MAX_NUMBER_OF_TRIES;
    }

    @Override
    public ConnectionTaskProperty getProperty(String name) {
        return this.getPropertyByName(name);
    }

    @Override
    protected Class<PartialConnectionInitiationTask> getPartialConnectionTaskType () {
        return PartialConnectionInitiationTask.class;
    }

    @Override
    public void scheduledComTaskRescheduled (ComTaskExecution comTask) {
        // No implementation required
    }

    @Override
    public void scheduledComTaskChangedPriority (ComTaskExecution comTask) {
        // No implementation required
    }


    public abstract static class AbstractConnectionInitiationTaskBuilder implements Device.ConnectionInitiationTaskBuilder {

        private final ConnectionInitiationTaskImpl connectionInitiationTask;

        public AbstractConnectionInitiationTaskBuilder(ConnectionInitiationTaskImpl connectionInitiationTask) {
            this.connectionInitiationTask = connectionInitiationTask;
        }

        protected ConnectionInitiationTaskImpl getConnectionInitiationTask() {
            return connectionInitiationTask;
        }

        @Override
        public Device.ConnectionInitiationTaskBuilder setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus status) {
            this.connectionInitiationTask.setStatus(status);
            return this;
        }
    }
}
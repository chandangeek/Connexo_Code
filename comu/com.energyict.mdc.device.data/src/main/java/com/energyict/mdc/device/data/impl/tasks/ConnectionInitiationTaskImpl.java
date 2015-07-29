package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import java.time.Clock;import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import java.util.List;

/**
 * Provides an implementation for the {@link ConnectionInitiationTask} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-16 (14:19)
 */
public class ConnectionInitiationTaskImpl extends OutboundConnectionTaskImpl<PartialConnectionInitiationTask> implements ConnectionInitiationTask {

    @Inject
    protected ConnectionInitiationTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ServerConnectionTaskService connectionTaskService, ServerCommunicationTaskService communicationTaskService, DeviceService deviceService, ProtocolPluggableService protocolPluggableService, RelationService relationService) {
        super(dataModel, eventService, thesaurus, clock, connectionTaskService, communicationTaskService, protocolPluggableService);
    }

    @Override
    public ComChannel connect(ComPort comPort) throws ConnectionException {
        return this.connect(this.getProperties(), new TrustingConnectionTaskPropertyValidator());
    }

    @Override
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {
        return this.connect(properties, new MistrustingConnectionTaskPropertyValidator());
    }

    private ComChannel connect(List<ConnectionTaskProperty> properties, ConnectionTaskPropertyValidator validator) throws ConnectionException {
        validator.validate(properties);
        ConnectionType connectionType = this.getConnectionType();
        List<ConnectionProperty> connectionProperties = this.toConnectionProperties(this.getProperties());
        return connectionType.connect(connectionProperties);
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
    protected Class<PartialConnectionInitiationTask> getPartialConnectionTaskType() {
        return PartialConnectionInitiationTask.class;
    }

    @Override
    public void scheduledComTaskRescheduled(ComTaskExecution comTask) {
        // No implementation required
    }

    @Override
    public void scheduledComTaskChangedPriority(ComTaskExecution comTask) {
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
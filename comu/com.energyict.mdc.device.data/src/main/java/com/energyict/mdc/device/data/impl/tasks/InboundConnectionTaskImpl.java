package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import java.time.Clock;import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.data.ConnectionTaskFields;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link InboundConnectionTask} interface.
 * <p>
 * Copyrights EnergyICT
 * Date: 25/09/12
 * Time: 9:49
 */
@UniqueInboundComPortPoolPerDevice(groups = {Save.Create.class, Save.Update.class})
public class InboundConnectionTaskImpl extends ConnectionTaskImpl<PartialInboundConnectionTask, InboundComPortPool> implements InboundConnectionTask {

    @Inject
    protected InboundConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ServerConnectionTaskService connectionTaskService, ServerCommunicationTaskService communicationTaskService, ProtocolPluggableService protocolPluggableService) {
        super(dataModel, eventService, thesaurus, clock, connectionTaskService, communicationTaskService, protocolPluggableService);
    }

    @Override
    protected Class<PartialInboundConnectionTask> getPartialConnectionTaskType() {
        return PartialInboundConnectionTask.class;
    }

    @Override
    public void executionFailed() {
        this.setExecutingComServer(null);
        this.update(ConnectionTaskFields.COM_SERVER.fieldName());
    }

    @Override
    public void scheduledComTaskRescheduled(ComTaskExecution comTask) {
        // No implementation required
    }

    @Override
    public void scheduledComTaskChangedPriority(ComTaskExecution comTask) {
        // No implementation required
    }

    public abstract static class AbstractInboundConnectionTaskBuilder implements Device.InboundConnectionTaskBuilder {

        private final InboundConnectionTaskImpl inboundConnectionTask;

        public AbstractInboundConnectionTaskBuilder(InboundConnectionTaskImpl inboundConnectionTask) {
            this.inboundConnectionTask = inboundConnectionTask;
        }

        protected InboundConnectionTaskImpl getInboundConnectionTask() {
            return inboundConnectionTask;
        }

        @Override
        public Device.InboundConnectionTaskBuilder setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus status) {
            this.inboundConnectionTask.setStatus(status);
            return this;
        }
    }

}
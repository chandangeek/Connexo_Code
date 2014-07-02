package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.engine.model.InboundComPortPool;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Provides an implementation for the {@link InboundConnectionTask} interface.
 * <p/>
 * Copyrights EnergyICT
 * Date: 25/09/12
 * Time: 9:49
 */
@UniqueInboundComPortPoolPerDevice(groups = {Save.Create.class, Save.Update.class})
public class InboundConnectionTaskImpl extends ConnectionTaskImpl<PartialInboundConnectionTask, InboundComPortPool> implements InboundConnectionTask {

    @Inject
    protected InboundConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, DeviceDataService deviceDataService, Provider<ConnectionMethodImpl> connectionMethodProvider) {
        super(dataModel, eventService, thesaurus, clock, deviceDataService, connectionMethodProvider);
    }

    @Override
    protected Class<PartialInboundConnectionTask> getPartialConnectionTaskType () {
        return PartialInboundConnectionTask.class;
    }

    @Override
    public void executionFailed() {
        this.setExecutingComServer(null);
        this.post();
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

        public final InboundConnectionTaskImpl inboundConnectionTask;

        public AbstractInboundConnectionTaskBuilder(InboundConnectionTaskImpl inboundConnectionTask) {
            this.inboundConnectionTask = inboundConnectionTask;
        }

        @Override
        public Device.InboundConnectionTaskBuilder setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus status) {
            this.inboundConnectionTask.setStatus(status);
            return this;
        }
    }

}
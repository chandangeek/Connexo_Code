package com.energyict.mdc.engine.impl.core.aspects.statistics;

import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannelImpl;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.model.EngineModelService;

import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.StopWatch;

/**
 * Defines pointcuts and advice to monitor the execution time
 * of {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-15 (14:49)
 */
public abstract aspect AbstractCommunicationStatisticsMonitor {

    StopWatch ComPortRelatedComChannelImpl.talking = new StopWatch(false);  // No cpu required;
    Counters ComPortRelatedComChannelImpl.sessionCounters = new Counters();
    Counters ComPortRelatedComChannelImpl.taskSessionCounters = new Counters();

    public StopWatch getComChannelTalkCounter (ComPortRelatedComChannelImpl comChannel) {
        return comChannel.talking;
    }

    public Counters getComChannelSessionCounters (ComPortRelatedComChannelImpl comChannel) {
        return comChannel.sessionCounters;
    }

    public Counters getComChannelTaskSessionCounters (ComPortRelatedComChannelImpl comChannel) {
        return comChannel.taskSessionCounters;
    }

}
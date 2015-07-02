package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.engine.config.InboundComPortPool;

import aQute.bnd.annotation.ProviderType;

/**
 * Models a {@link ConnectionTask} that is used for Inbound communication.
 * An InboundConnectionTask does NOT establish any connection.
 * Instead, it is triggered by an external physical Device that
 * establishes the connection, connecting to a port that is contained
 * in the related {@link InboundComPortPool}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 6/09/12
 * Time: 15:19
 */
@ProviderType
public interface InboundConnectionTask extends ConnectionTask<InboundComPortPool, PartialInboundConnectionTask> {
}
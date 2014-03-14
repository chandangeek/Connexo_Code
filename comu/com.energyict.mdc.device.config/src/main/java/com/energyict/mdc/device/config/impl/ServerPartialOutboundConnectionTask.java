package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.engine.model.OutboundComPortPool;

/**
 * Adds behavior to {@link PartialOutboundConnectionTask} that is private
 * to the server side implementation.
 *
 * @author sva
 * @since 23/01/13 - 8:52
 */
public interface ServerPartialOutboundConnectionTask extends ServerPartialConnectionTask<OutboundComPortPool>, PartialOutboundConnectionTask {

}

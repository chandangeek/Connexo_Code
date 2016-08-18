package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.engine.impl.events.EventPublisher;

/**
 * Copyrights EnergyICT
 * Date: 11/08/2016
 * Time: 13:41
 */
public class PingRequest implements Request {
    @Override
    public void applyTo(EventPublisher eventPublisher) {
       eventPublisher.answerPing();
    }
}

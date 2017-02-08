/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.engine.impl.events.EventPublisher;

class PingRequest implements Request {
    @Override
    public void applyTo(EventPublisher eventPublisher) {
       eventPublisher.answerPing();
    }
}

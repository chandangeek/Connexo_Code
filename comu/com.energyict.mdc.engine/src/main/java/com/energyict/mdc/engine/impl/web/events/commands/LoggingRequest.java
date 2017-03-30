/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.logging.LogLevel;

import java.util.EnumSet;
import java.util.Set;

/**
 * Provides an implementation for the {@link Request} interface
 * that represents a request to register interest in events
 * that relate to a set of {@link Category logging event categories}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (09:48)
 */
class LoggingRequest implements Request {

    private LogLevel level;
    private EnumSet<Category> categories;

    LoggingRequest(LogLevel level, Set<Category> categories) {
        super();
        this.level = level;
        this.categories = EnumSet.copyOf(categories);
    }

    public LogLevel getLevel () {
        return level;
    }

    Set<Category> getCategories() {
        return categories;
    }

    @Override
    public void applyTo (EventPublisher eventPublisher) {
        eventPublisher.narrowInterestToCategories(null, this.categories);
        eventPublisher.narrowInterestToLogLevel(null, this.level);
    }

}
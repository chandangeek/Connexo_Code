/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;

public enum TranslationKeys implements TranslationKey {

    LIFE_CYCLE_NAME("usage.point.life.cycle.standard.name", "Standard usage point life cycle"),
    QUEUE_SUBSCRIBER(ServerUsagePointLifeCycleService.QUEUE_SUBSCRIBER, "Handle usage point life cycle changes"),
    USAGE_POINT_STATE_CHANGE_REQUEST_STATUS_COMPLETED(Keys.CHANGE_REQUEST_STATUS_PREFIX + UsagePointStateChangeRequest.Status.COMPLETED, "Completed"),
    USAGE_POINT_STATE_CHANGE_REQUEST_STATUS_FAILED(Keys.CHANGE_REQUEST_STATUS_PREFIX + UsagePointStateChangeRequest.Status.FAILED, "Failed"),
    USAGE_POINT_STATE_CHANGE_REQUEST_STATUS_SCHEDULED(Keys.CHANGE_REQUEST_STATUS_PREFIX + UsagePointStateChangeRequest.Status.SCHEDULED, "Planned"),
    USAGE_POINT_STATE_CHANGE_REQUEST_STATUS_CANCELLED(Keys.CHANGE_REQUEST_STATUS_PREFIX + UsagePointStateChangeRequest.Status.CANCELLED, "Aborted"),
    USAGE_POINT_STATE_CHANGE_REQUEST_TYPE_STATE(Keys.CHANGE_REQUEST_TYPE_PREFIX + UsagePointStateChangeRequest.Type.STATE_CHANGE, "State change"),;

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    static class Keys {
        private Keys() {
        }

        public static final String CHANGE_REQUEST_STATUS_PREFIX = "usage.point.state.change.request.status.";
        public static final String CHANGE_REQUEST_TYPE_PREFIX = "usage.point.state.change.request.type.";
    }
}

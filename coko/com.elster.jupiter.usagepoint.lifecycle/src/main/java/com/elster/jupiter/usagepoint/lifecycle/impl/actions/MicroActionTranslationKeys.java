/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.nls.TranslationKey;

public enum MicroActionTranslationKeys implements TranslationKey {
    SET_CONNECTION_STATE_NAME(Keys.NAME_PREFIX + SetConnectionStateAction.class.getSimpleName(), "Set connection state"),
    SET_CONNECTION_STATE_DESCRIPTION(Keys.DESCRIPTION_PREFIX + SetConnectionStateAction.class.getSimpleName(), "Set connection state to one of the available states."),
    SET_CONNECTION_STATE_PROPERTY_NAME("set.connection.state.property.name", "Connection state"),
    SET_CONNECTION_STATE_PROPERTY_MESSAGE("set.connection.state.property.message", "Incorrect value for ''Connection state''"),
    RESET_VALIDATION_RESULTS_NAME(Keys.NAME_PREFIX + ResetValidationResultsAction.class.getSimpleName(), "Reset validation results"),
    RESET_VALIDATION_RESULTS_DESCRIPTION(Keys.DESCRIPTION_PREFIX + ResetValidationResultsAction.class.getSimpleName(),
            "Shift last validation date on all the outputs of a usage point to the point in time equal to the transition date."),
    REMOVE_USAGE_POINT_FROM_STATIC_GROUP_NAME(Keys.NAME_PREFIX + RemoveUsagePointFromStaticGroup.class.getSimpleName(), "Remove usage point from static group"),
    REMOVE_USAGE_POINT_FROM_STATIC_GROUP_DESCRIPTION(Keys.DESCRIPTION_PREFIX + RemoveUsagePointFromStaticGroup.class.getSimpleName(), "Remove usage point from static group"),
    CANCEL_ALL_SERVICE_CALLS_NAME(Keys.NAME_PREFIX + CancelAllServiceCalls.class.getSimpleName(), "Cancel all service calls"),
    CANCEL_ALL_SERVICE_CALLS_DESCRIPTION(Keys.DESCRIPTION_PREFIX + CancelAllServiceCalls.class.getSimpleName(),
            "Triggers a cancel request for all active service calls on the given usage point.");


    private final String key;
    private final String defaultFormat;

    MicroActionTranslationKeys(String key, String defaultFormat) {
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

    public static class Keys {
        static String NAME_PREFIX = "usage.point.micro.action.name.";
        static String DESCRIPTION_PREFIX = "usage.point.micro.action.description.";

        private Keys() {
        }
    }
}

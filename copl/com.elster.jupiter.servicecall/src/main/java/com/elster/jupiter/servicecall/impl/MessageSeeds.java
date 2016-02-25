package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Created by bvn on 2/4/16.
 */
public enum MessageSeeds implements MessageSeed {
    CANNOT_REMOVE_STATE_EXCEPTION(1, "canNotRemoveState", "Default state {0} can not be removed from the state diagram."),
    NO_PATH_TO_SUCCESS_FROM(2, "NoPathToSuccess", "Cannot get to Successful state from {0}"),
    NO_PATH_FROM_CREATED_TO(3, "NoPathFromCreated", "Cannot get to {0} state from Created."),
    NO_NAME_FOR_HANDLER(4, "NoNameForHandler", "Handler can not be registered: it does not have a name. Add @Component field property='name=xxx'"),
    INVALID_CPS_TYPE(5, Constants.INVALID_TYPE, "Custom property set {0} does not have the required domain class of service call type");
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;

    MessageSeeds(int number, String key, String defaultFormat) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getModule() {
        return ServiceCallService.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

    public static class Constants {
        public static final String INVALID_TYPE = "com.elster.jupiter.servicecall.invalidType";
    }
}

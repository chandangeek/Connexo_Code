package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed, TranslationKey {

    IMPOSSIBLE_TO_SET_MASTER_DEVICE(1, "ImpossibleToSetMasterDevice", "Device {0} is directly addressable. It is not possible to set master device"),
    NO_SUCH_DEVICE_LIFE_CYCLE_ACTION(2, "NoSuchDeviceLifeCycleAction" , "No device life cycle action with id = {0}"),
    THIS_FIELD_IS_REQUIRED(3, "ThisFieldIsRequired" , "This field is required"),
    CAN_NOT_HANDLE_ACTION(4, "CanNotHandleAction", "The requested device life cycle action action can not be handled"),
    NOT_FOUND(5, "NotFound", "The resource could not be found"),
    ;
    private final int number;
    private final String key;
    private final String format;

    private MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = key;
        this.format = format;
    }

    @Override
    public String getModule() {
        return PublicRestApplication.COMPONENT_NAME;
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
        return format;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

}

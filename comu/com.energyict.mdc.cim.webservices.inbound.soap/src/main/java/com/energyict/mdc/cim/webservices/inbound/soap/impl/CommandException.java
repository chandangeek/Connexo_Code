/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.function.Supplier;

public class CommandException extends LocalizedException implements Supplier<CommandException> {

    protected CommandException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    @Override
    public CommandException get() {
        return this;
    }

    public static CommandException endDeviceControlTypeWithCIMNotFound(Thesaurus thesaurus, String cim) {
        return new CommandException(thesaurus, MessageSeeds.NO_SUCH_END_DEVICE_CONTROL_TYPE_WITH_CIM, cim);
    }

    public static CommandException unsupportedEndDeviceControlType(Thesaurus thesaurus, String cim) {
        return new CommandException(thesaurus, MessageSeeds.UNSUPPORTED_END_DEVICE_CONTROL_TYPE, cim);
    }

    public static CommandException inappropriateCommandAttributes(Thesaurus thesaurus, String cim) {
        return new CommandException(thesaurus, MessageSeeds.INAPPROPRIATE_COMMAND_ATTRIBUTES, cim);
    }

    public static CommandException noHeadEndInterface(Thesaurus thesaurus, String mrid) {
        return new CommandException(thesaurus, MessageSeeds.NO_HEAD_END_INTERFACE, mrid);
    }
}
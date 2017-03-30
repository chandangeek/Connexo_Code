/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.engine.impl.commands.collect.ComCommand;

/**
 * Enum containing an entry for all possible {@link ComCommand DeviceCommands}.
 * This enum can be used to provide a readable description for each of the {@link ComCommand DeviceCommands}
 * </p>
 *
 * This enum can be used to provide a readable description for each of the ComCommands
 *
 * @author sva
 * @since 5/12/13 - 15:19
 */
public enum ComCommandDescriptionTitle {

    DeviceProtocolSetCacheCommand               ("Load the device protocol cache"),
    InitializeLoggerCommand                     ("Initialize the protocol logger"),
    AddPropertiesCommand                        ("Load the device protocol properties"),
    DeviceProtocolInitializeCommand             ("Initialize the device protocol"),
    HandHeldUnitEnablerCommand                  ("Hand-held unit sign-on"),
    LogOnCommand                                ("Log on to the device"),
    DaisyChainedLogOnCommand                    ("Daisy chained log on"),
    DaisyChainedLogOffCommand                   ("Daisy chained log of"),
    LogOffCommand                               ("Log off from the device"),
    DeviceProtocolTerminateCommand              ("Terminate the protocol session"),
    DeviceProtocolUpdateCacheCommand            ("Update the stored device cache"),

    ClockCommandImpl                            ("Executed clock protocol task"),
    SetClockCommandImpl                         ("Set the device time"),
    SynchronizeClockCommandImpl                 ("Synchronize the device time"),
    ForceClockCommandImpl                       ("Force set the device time"),
    UnKnownClockTaskTypeCommand                 ("Dummy clock command"),
    BasicCheckCommandImpl                       ("Executed basic check protocol task"),
    TimeDifferenceCommandImpl                   ("Read out the device time difference"),
    VerifyTimeDifferenceCommandImpl             ("Verify the device time difference"),
    VerifySerialNumberCommandImpl               ("Verify the device serial number"),
    LoadProfileCommandImpl                      ("Executed load profile protocol task"),
    VerifyLoadProfilesCommandImpl               ("Read out and verify the load profile configuration"),
    ReadLoadProfileDataCommandImpl              ("Read out the load profiles"),
    MarkIntervalsAsBadTimeCommandImpl           ("Mark load profile intervals as bad time"),
    CreateMeterEventsFromStatusFlagsCommandImpl ("Create meter events from load profile reading qualities"),
    LegacyLoadProfileLogBooksCommandImpl        ("Executed load profile and logbook protocol tasks"),
    ReadLegacyLoadProfileLogBooksDataCommandImpl("Read out load profile and logbook of legacy protocol"),
    RegisterCommandImpl                         ("Executed register protocol task"),
    ReadRegistersCommandImpl                    ("Read out the device registers"),
    LogBooksCommandImpl                         ("Executed logbook protocol task"),
    ReadLogBooksCommandImpl                     ("Read out the device logbooks"),
    StatusInformationCommandImpl                ("Read out the device status information"),
    TopologyCommandImpl                         ("Executed topology protocol task"),
    MessagesCommandImpl                         ("Handle all device messages"),

    InboundCollectedRegisterCommandImpl         ("Collect inbound register data"),
    InboundCollectedMessageListCommandImpl      ("Collect inbound message data"),
    InboundCollectedLogBookCommandImpl          ("Collect inbound logbook data"),
    InboundCollectedLoadProfileCommandImpl      ("Collect inbound load profile data"),

    CommandRootImpl                             ("All commands are executed"),
    ComTaskExecutionComCommandImpl              ("Execute commands for a single communication task"),
    CompositeComCommandImpl                     ("Aggregated command"),
    NoopCommandImpl                             ("No operations command"),
    AlreadyExecutedComCommandImpl               ("Command already executed as part of other task"),
    GroupedDeviceCommand                        ("Groups commands per device and securityset"),
    UnknownCommand                              ("Unknown ComCommand"), ;

    private String description;

    private ComCommandDescriptionTitle(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static  <T extends ComCommand> ComCommandDescriptionTitle getComCommandDescriptionTitleFor(Class<T> commandClass) {
        for (ComCommandDescriptionTitle each : values()) {
            if (each.name().equals(commandClass.getSimpleName())) {
                return each;
            }
        }
        return UnknownCommand;
    }
}
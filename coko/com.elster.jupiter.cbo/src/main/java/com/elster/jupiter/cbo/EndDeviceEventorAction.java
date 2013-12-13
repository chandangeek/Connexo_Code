package com.elster.jupiter.cbo;

public enum EndDeviceEventorAction {
    NA("NA", 0, "Not applicable. Use when a domain is not needed. This should rarely be used."),
    ABORTED("Aborted", 1, "An event that occurs when some intervention causes the item (identified by the EndDeviceDomain/EndDeviceSubdomain) to stop."),
    ACCESSED("Accessed", 2, "Typically a security event that occurs when physical access or access to data has been obtained (whether permitted or not)."),
    ACKNOWLEDGED("Acknowledged", 3, "An event that indicates the receipt of the item (identified by the EndDeviceDomain/EndDeviceSubdomain)."),
    ACTIVATED("Activated", 4, "An event that indicates that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) that was inactive is now active."),
    ARMED_FOR_CLOSURE("ArmedForClosure", 11, "An event that indicates that an ArmForClosure command has been completed successfully."),
    ARMED_FOR_OPEN("ArmedForOpen", 12, "An event that indicates that an ArmForOpen command has been completed successfully."),
    ARM_FOR_CLOSURE("ArmForClosure", 5, "A command to indicate a request to arm a switch for closure."),
    ARM_FOR_CLOSUREFAILED("ArmForClosureFailed", 226, "An event that indicates that an ArmForClosure has failed."),
    ARM_FOR_OPEN("ArmForOpen", 6, "A command to indicate a request to arm a switch for open."),
    ARM_FOR_OPENFAILED("ArmForOpenFailed", 222, "An event that indicates that an ArmFor Open has failed."),
    ATTEMPTED("Attempted", 7, "An event that indicates that the item (identified by the EndDeviceDomain/EndDeviceSubdomain), based on the EndDeviceDomain and EndDeviceSubdomain combination, has been tried."),
    CALCULATED("Calculated", 21, "An event that indicates that the item (identified by the EndDeviceDomain/EndDeviceSubdomain), based on the EndDeviceDomain and EndDeviceSubdomain combination, has been computed."),
    CANCEL("Cancel", 8, "A command to indicate a request to terminate a prior issued command."),
    CANCEL_FAILED("CancelFailed", 86, "An event that indicates that a Cancel has failed."),
    CANCELLED("Cancelled", 10, "An event that indicates that a prior issued command or set of commands was terminated successfully."),
    CHANGE("Change", 13, "A command to indicate a request to make modifications."),
    CHANGED("Changed", 24, "An event that indicates that a related Change request has completed successfully."),
    CHANGE_OUT("ChangeOut", 27, "A command to request that a device is replaced by a new device of the same kind."),
    CHANGE_PENDING("ChangePending", 14, "An event that indicates that an update has not yet been performed."),
    CHARGED("Charged", 15, "An event that can indicate a billing-related state or in the form of being electrically charged."),
    CLEARED("Cleared", 28, "An event that indicates that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) on the device has been either resolved or emptied."),
    CLOSED("Closed", 16, "An event that indicates the item (identified by the EndDeviceDomain/EndDeviceSubdomain) on the device that had been open is not open anymore."),
    COLD_STARTED("ColdStarted", 31, "An event that indicates the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has been started from a stopped state (as opposed to a WarmStart which implies that it was started from an already started state)."),
    CONFIRMED("Confirmed", 17, "An event that indicates the receipt and agreement of the item (identified by the EndDeviceDomain/EndDeviceSubdomain)."),
    CONNECT("Connect", 18, "A command to request that a device be put into service."),
    CONNECTED("Connected", 42, "An event to indicate that a device has been put into service."),
    CONNECT_FAILED("ConnectFailed", 67, "An event that indicates a Connect request has failed."),
    CORRUPTED("Corrupted", 43, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has been altered from a correct state to an incorrect state."),
    CREATE("Create", 82, "A command to request that something be created."),
    CREATED("Created", 83, "An event that indicates that a Create request succeeded."),
    CREATE_FAILED("CreateFailed", 84, "An event that indicates that a Create request failed."),
    CROSS_PHASE_CLEARED("CrossPhaseCleared", 70, "An event that indicates that instability due to cross-phase modulation has been corrected."),
    CROSS_PHASE_DETECTED("CrossPhaseDetected", 45, "An event that indicates instability due to cross-phase modulation."),
    DEACTIVATED("Deactivated", 19, "An event that indicates that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) on the device that had previously been in an active state is no longer active."),
    DECREASED("Decreased", 57, "An event that indicates that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has a lower value or magnitude."),
    DELAYED("Delayed", 20, "An event that indicates the item (identified by the EndDeviceDomain/EndDeviceSubdomain) is slower than expected or will complete later than expected."),
    DISABLE("Disable", 22, "A command to request that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) be rendered incapable."),
    DISABLED("Disabled", 66, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) was successfully rendered incapable."),
    DISABLE_FAILED("DisableFailed", 220, "An event that indicates that a Disable request has failed."),
    DISALLOWED("Disallowed", 161, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) was not allowed."),
    DISCONNECT("Disconnect", 23, "A command to request that a device be pulled from service; can also mean a request to sever connection to the item (identified by the EndDeviceDomain/EndDeviceSubdomain)."),
    DISCONNECTED("Disconnected", 68, "An event to indicate that a device was successfully pulled from service; can also mean that connection to the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has been severed or terminated."),
    DISCONNECT_FAILED("DisconnectFailed", 84, "An event that indicates that a Disconnect request has failed."),
    DISPLAY("Display", 77, "A command to request the display of something (as in a TextMessage)."),
    DISPLAYED("Displayed", 78, "An event that indicates that a Display request completed successfully."),
    DISPLAY_FAILED("DisplayFailed", 87, "An event that indicates that a Display request failed."),
    DISTORTED("Distorted", 91, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has been altered from its expected state."),
    DOWNLOADED("Downloaded", 25, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) transmitted from the network to the device."),
    ENABLE("Enable", 26, "A command to request that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) be rendered capable."),
    ENABLED("Enabled", 76, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) was successfully rendered capable."),
    ENABLE_FAILED("EnableFailed", 221, "An event that indicates that an Enable request failed."),
    ERROR("Error", 79, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) did not complete successfully."),
    ESTABLISHED("Established", 29, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has been recognized."),
    EXCEEDED("Exceeded", 139, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has gone higher than its expected value."),
    EXECUTE("Execute", 30, "A command to request that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) be performed."),
    EXPIRED("Expired", 31, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has come to an end, typically by date or time."),
    FAILED("Failed", 85, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has not succeeded."),
    FROZEN("Frozen", 88, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) is in a static state."),
    FULL("Full", 32, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) is at capacity."),
    HIGH_DISTORTION("HighDistortion", 69, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has had an undesired change in the waveform of a signal."),
    HIGH_DISTORTION_CLEARED("HighDistortionCleared", 71, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) is back to normal after having been in a HighDistortion state."),
    IMBALANCE_CLEARED("ImbalanceCleared", 75, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) is back in balance after having been in an imbalanced state."),
    IMBALANCED("Imbalanced", 98, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) is not balanced."),
    INACTIVE("Inactive", 100, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) is at a dormant state."),
    INACTIVE_CLEARED("InactiveCleared", 72, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) is now in an active state after having been in a dormant state."),
    INCREASED("Increased", 102, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has gotten larger."),
    INITIALIZED("Initialized", 33, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has been set to starting values."),
    IN_PROGRESS("InProgress", 34, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) is currently advancing toward a goal or an end."),
    INSTALLED("Installed", 105, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has been prepared for use."),
    INVALID("Invalid", 35, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) is in a faulty state."),
    LOADED("Loaded", 36, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) is now engaged."),
    LOSS_DETECTED("LossDetected", 47, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has experienced a deprived condition. This is typically used in relation to power, voltage, or current."),
    MAX_LIMIT_REACHED("MaxLimitReached", 93, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has reached a maximum acceptable value."),
    MAX_LIMIT_REACHED_CLEARED("MaxLimitReachedCleared", 73, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has returned to an acceptable state after having been as a MaxLimitReached state."),
    MIN_LIMIT_REACHED("MinLimitReached", 150, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has reached a minimum acceptable value."),
    MISMATCHED("Mismatched", 159, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) is not compatible with itself or something within its environment."),
    NORMAL("Normal", 37, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) is in its typical state (or norm)."),
    NOT_AUTHORIZED("NotAuthorized", 38, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has been accessed without permission."),
    NOT_FOUND("NotFound", 160, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) is lost or missing."),
    OPENED("Opened", 39, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) is accessible."),
    OPTED_IN("Opted-In", 80, "An event that indicates that a consumer has agreed to join a program."),
    OPTED_OUT("Opted-Out", 81, "An event that indicates that a consumer does not want to join a program."),
    OUT_OF_RANGE("OutofRange", 40, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has gone outside of acceptable values."),
    OUT_OF_RANGE_CLEARED("OutofRangeCleared", 74, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has gone back to acceptable values."),
    OVERFLOW("Overflow", 177, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has exceeded its size or volume."),
    PREEMPTED("Preempted", 41, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) been replaced by another that has precedence over it."),
    PROCESSED("Processed", 44, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has been successfully been put through the steps of a prescribed procedure."),
    READ("Read", 46, "This can be an event (if treated as the past-tense of the verb, read) or a command (if treated as the verb, read)."),
    READY("Ready", 48, "An event that indicates that a ready condition has been reached on a device."),
    RE_ESTABLISHED("Re-established", 49, "An event that indicates that a condition, typically a connection, has achieved after having been lost."),
    REGISTERED("Registered", 50, "An event that indicates that a device or condition of a device has been recorded."),
    RELEASED("Released", 51, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has been freed."),
    REMOVED("Removed", 212, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has been pulled out of service."),
    REPLACED("Replaced", 52, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) replaced by an new item, usually as a consequence of being old or worn out."),
    REPROGRAMMED("Reprogrammed", 213, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has had a change to its directions or program."),
    RESET("Reset", 214, "A command to request that the item (identified by the EndDeviceDomain.EndDeviceSubdomain) should be set back to a zero-state or original-state."),
    RESET_FAILED("ResetFailed", 65, "An event that indicates that a Reset request has failed."),
    RESET_OCCURRED("ResetOccurred", 215, "An event that indicates that a Reset request has completed successfully."),
    RESTARTED("Restarted", 53, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has started again typically from an already started state."),
    RESTORED("Restored", 216, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has been put back to its prior state."),
    REVERSED("Reversed", 219, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has changed to be the opposite of its normal state. This is typically used for rotation."),
    SAG_STARTED("SagStarted", 223, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has dipped or shrunk from its expected state."),
    SAG_STOPPED("SagStopped", 224, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has stopped sagging. Typically used in conjunction with the SagStarted event."),
    SCHEDULED("Scheduled", 225, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has been set to execute at a future date."),
    SEALED("Sealed", 227, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) is in an airtight enclosure or cannot be accessed directly."),
    START("Start", 54, "A command to request that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) be triggered or begun."),
    STARTED("Started", 242, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has started."),
    START_FAILED("StartFailed", 217, "An event that indicates that a Start request has failed."),
    STOP("Stop", 55, "A command to request that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) be shut down normally."),
    STOP_FAILED("StopFailed", 218, "An event that indicates that a Stop request has failed."),
    STOPPED("Stopped", 243, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has ceased. When this has resulted from a Stop command, it is assumed that things have stopped normally (ie. with no errors)."),
    SUBSTITUTED("Substituted", 56, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) replaced by an alternate item."),
    SUCCEEDED("Succeeded", 58, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) was accomplished."),
    SWELL_STARTED("SwellStarted", 248, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has grown from its expected state."),
    SWELL_STOPPED("SwellStopped", 249, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has stopped swelling. Typically used in conjunction with the SwellStarted event."),
    TAMPER_DETECTED("TamperDetected", 257, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has been secretly modified or altered. These events are typically associated with security or billing."),
    TERMINATED("Terminated", 59, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has ended abruptly."),
    TILTED("Tilted", 263, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has been caused to lean, incline, slope, or slant."),
    UNINITIALIZED("Uninitialized", 61, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has all its values in their starting state."),
    UNLOCKED("Unlocked", 62, "An event to indicate that the item’s (identified by the EndDeviceDomain/EndDeviceSubdomain) lock, physical or software, is undone."),
    UNSEALED("Unsealed", 269, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) is not sealed."),
    UNSECURE("Unsecure", 63, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) is in an unprotected state."),
    UNSTABLE("Unstable", 270, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) is in an irregular state."),
    UPLOADED("Uploaded", 60, "An event to indicate that the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has been transmitted from the device to the network."),
    WARM_STARTED("WarmStarted", 278, "An event that indicates the item (identified by the EndDeviceDomain/EndDeviceSubdomain) has been started from an already started state (as opposed to a ColdStart which implies that it was started from a stopped state)."),;

    private final String mnemonic;
    private final int value;
    private final String description;

    EndDeviceEventorAction(String mnemonic, int value, String description) {
        this.mnemonic = mnemonic;
        this.value = value;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public int getValue() {
        return value;
    }

    public boolean isApplicable() {
        return NA != this;
    }
}

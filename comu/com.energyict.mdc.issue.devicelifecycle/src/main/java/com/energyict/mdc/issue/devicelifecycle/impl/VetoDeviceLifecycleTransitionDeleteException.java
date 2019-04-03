/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl;


import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;




public class VetoDeviceLifecycleTransitionDeleteException extends LocalizedException {
    public VetoDeviceLifecycleTransitionDeleteException(Thesaurus thesaurus, String stateTransition) {
        super(thesaurus, MessageSeeds.DEVICE_LIFECYCLE_TRANSITION_IN_USE, stateTransition);
    }
}

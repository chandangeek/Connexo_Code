/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.common.composedobjects;

import com.energyict.dlms.DLMSAttribute;

public class ComposedProfileConfig {

    private final DLMSAttribute loadProfileInterval;
    private final DLMSAttribute loadProfileCapturedObjects;

    public ComposedProfileConfig(DLMSAttribute loadProfileInterval, DLMSAttribute loadProfileCapturedObjects) {
        this.loadProfileInterval = loadProfileInterval;
        this.loadProfileCapturedObjects = loadProfileCapturedObjects;
    }

    public DLMSAttribute getLoadProfileInterval() {
        return loadProfileInterval;
    }

    public DLMSAttribute getLoadProfileCapturedObjects() {
        return loadProfileCapturedObjects;
    }
}

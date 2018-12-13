package com.energyict.protocolimplv2.common.composedobjects;

import com.energyict.dlms.DLMSAttribute;

/**
 * The ComposedProfileConfig object is just a ValueObject that holds the {@link com.energyict.dlms.DLMSAttribute} for the ProfileGeneric his interval and capturedObjects
 *
 * <pre>
 * Copyrights EnergyICT
 * Date: 4-mrt-2011
 * Time: 9:23:49
 * </pre>
 */
public class ComposedProfileConfig implements ComposedObject {

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

package com.energyict.protocolimplv2.common.composedobjects;

import com.energyict.dlms.DLMSAttribute;

/**
 * The ComposedData is just a ValueObject that holds the {@link com.energyict.dlms.DLMSAttribute} from a data object
 */
public class ComposedData implements ComposedObject {

    private final DLMSAttribute dataValue;

    public ComposedData(DLMSAttribute dataValue) {
        this.dataValue = dataValue;
    }

    public DLMSAttribute getDataValueAttribute() {
        return dataValue;
    }
}
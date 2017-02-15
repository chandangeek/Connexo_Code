/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.common.composedobjects;

import com.energyict.dlms.DLMSAttribute;

/**
 * The ComposedData is just a ValueObject that holds the {@link com.energyict.dlms.DLMSAttribute} from a data object
 */
public class ComposedData {

    private final DLMSAttribute dataValue;

    public ComposedData(DLMSAttribute dataValue) {
        this.dataValue = dataValue;
    }

    public ComposedData(DLMSAttribute dataValue, DLMSAttribute registerUnit) {
        this.dataValue = dataValue;
    }

    public DLMSAttribute getDataValueAttribute() {
        return dataValue;
    }
}

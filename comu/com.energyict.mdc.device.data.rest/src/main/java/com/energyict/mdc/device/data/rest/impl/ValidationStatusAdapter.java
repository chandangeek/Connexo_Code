/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

public class ValidationStatusAdapter extends MapBasedXmlAdapter<ValidationStatus> {

    public ValidationStatusAdapter() {
        register(ValidationStatus.OK.getNameKey(), ValidationStatus.OK);
        register(ValidationStatus.SUSPECT.getNameKey(), ValidationStatus.SUSPECT);
        register(ValidationStatus.NOT_VALIDATED.getNameKey(), ValidationStatus.NOT_VALIDATED);
    }
}

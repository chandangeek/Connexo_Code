/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.common.rest.MapBasedXmlAdapter;

public class ValidationStatusAdapter extends MapBasedXmlAdapter<ValidationStatus> {

    public ValidationStatusAdapter() {
        register(ValidationStatus.OK.getNameKey(), ValidationStatus.OK);
        register(ValidationStatus.SUSPECT.getNameKey(), ValidationStatus.SUSPECT);
        register(ValidationStatus.NOT_VALIDATED.getNameKey(), ValidationStatus.NOT_VALIDATED);
    }
}

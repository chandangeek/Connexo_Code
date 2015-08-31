package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.insight.common.rest.MapBasedXmlAdapter;

public class ValidationStatusAdapter extends MapBasedXmlAdapter<ValidationStatus> {

    public ValidationStatusAdapter() {
        register(ValidationStatus.OK.getNameKey(), ValidationStatus.OK);
        register(ValidationStatus.SUSPECT.getNameKey(), ValidationStatus.SUSPECT);
        register(ValidationStatus.NOT_VALIDATED.getNameKey(), ValidationStatus.NOT_VALIDATED);
    }
}

package com.energyict.mdc.device.data.rest;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

public class ValueModificationFlagAdapter extends MapBasedXmlAdapter<ValueModificationFlag> {

    public ValueModificationFlagAdapter() {
        register("", null);
        register("EDITED", ValueModificationFlag.EDITED);
        register("ESTIMATED", ValueModificationFlag.ESTIMATED);
    }
}


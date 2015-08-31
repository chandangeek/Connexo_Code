package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.insight.common.rest.MapBasedXmlAdapter;


public class ReadingModificationFlagAdapter extends MapBasedXmlAdapter<ReadingModificationFlag> {

    public ReadingModificationFlagAdapter() {
        register("", null);
        register("ADDED", ReadingModificationFlag.ADDED);
        register("EDITED", ReadingModificationFlag.EDITED);
        register("REMOVED", ReadingModificationFlag.REMOVED);
    }
}

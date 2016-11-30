package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.common.rest.MapBasedXmlAdapter;

public class ReadingModificationFlagAdapter extends MapBasedXmlAdapter<ReadingModificationFlag> {

    public ReadingModificationFlagAdapter() {
        register("", null);
        register("ADDED", ReadingModificationFlag.ADDED);
        register("EDITED", ReadingModificationFlag.EDITED);
        register("REMOVED", ReadingModificationFlag.REMOVED);
        register("RESET", ReadingModificationFlag.RESET);
    }
}

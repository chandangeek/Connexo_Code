/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;


public class ReadingModificationFlagAdapter extends MapBasedXmlAdapter<ReadingModificationFlag> {

    public ReadingModificationFlagAdapter() {
        register("", null);
        register("ADDED", ReadingModificationFlag.ADDED);
        register("EDITED", ReadingModificationFlag.EDITED);
        register("REMOVED", ReadingModificationFlag.REMOVED);
    }
}

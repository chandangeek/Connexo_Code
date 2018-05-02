/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm;

import com.atos.worldline.jss.api.JSSRuntimeControl;
import com.atos.worldline.jss.configuration.RawConfiguration;

public class HsmModule {

    public void init(RawConfiguration cfg) {
        JSSRuntimeControl.newConfiguration(cfg);
    }

}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.io.FlowControl;

public class FlowControlAdapter extends MapBasedXmlAdapter<FlowControl> {

    public FlowControlAdapter() {
        register(ComServerFieldTranslationKeys.FLOW_CONTROL_DTRDSR.getKey(), FlowControl.DTRDSR);
        register(ComServerFieldTranslationKeys.FLOW_CONTROL_XONXOFF.getKey(), FlowControl.XONXOFF);
        register(ComServerFieldTranslationKeys.FLOW_CONTROL_RTSCTS.getKey(), FlowControl.RTSCTS);
        register(ComServerFieldTranslationKeys.FLOW_CONTROL_NONE.getKey(), FlowControl.NONE);
        register("", null);
    }
}

package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.channels.serial.FlowControl;

public class FlowControlAdapter extends MapBasedXmlAdapter<FlowControl> {

    public FlowControlAdapter() {
        register("Dtr/Dsr", FlowControl.DTRDSR);
        register("Xon/Xoff", FlowControl.XONXOFF);
        register("Rts/Cts", FlowControl.RTSCTS);
        register("None", FlowControl.NONE);
        register("", null);
    }
}

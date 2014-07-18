package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.protocol.api.channels.serial.FlowControl;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

public class FlowControlAdapter extends MapBasedXmlAdapter<FlowControl> {

    public FlowControlAdapter() {
        register("DTR/DSR", FlowControl.DTRDSR);
        register("Xon/Xoff", FlowControl.XONXOFF);
        register("RTS/CTS", FlowControl.RTSCTS);
        register("No flow control", FlowControl.NONE);
        register("", null);
    }
}

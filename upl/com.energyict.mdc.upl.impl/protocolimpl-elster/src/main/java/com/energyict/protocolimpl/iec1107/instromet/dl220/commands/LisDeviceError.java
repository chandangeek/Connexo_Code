package com.energyict.protocolimpl.iec1107.instromet.dl220.commands;

import java.io.IOException;

/**
 * Created by heuckeg on 05.02.2015.
 */
public class LisDeviceError extends IOException {

    public LisDeviceError() {
        super();
    }

    public LisDeviceError(String s) {
        super(s);
    }
}

package com.elster.protocolimpl.lis200.commands;

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

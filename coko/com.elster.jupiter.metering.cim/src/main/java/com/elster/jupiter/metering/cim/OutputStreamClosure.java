package com.elster.jupiter.metering.cim;

import java.io.OutputStream;

public interface OutputStreamClosure {

    void using(OutputStream out);
}

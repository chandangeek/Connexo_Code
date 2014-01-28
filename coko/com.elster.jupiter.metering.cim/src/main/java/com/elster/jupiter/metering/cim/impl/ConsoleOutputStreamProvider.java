package com.elster.jupiter.metering.cim.impl;

import com.elster.jupiter.metering.cim.OutputStreamClosure;
import com.elster.jupiter.metering.cim.OutputStreamProvider;

public class ConsoleOutputStreamProvider implements OutputStreamProvider {

    @Override
    public void writeTo(OutputStreamClosure outputStreamClosure) {
        outputStreamClosure.using(System.out);
    }
}

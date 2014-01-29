package com.elster.jupiter.metering.cim;

public class ConsoleOutputStreamProvider implements OutputStreamProvider {

    @Override
    public void writeTo(OutputStreamClosure outputStreamClosure) {
        outputStreamClosure.using(System.out);
    }
}

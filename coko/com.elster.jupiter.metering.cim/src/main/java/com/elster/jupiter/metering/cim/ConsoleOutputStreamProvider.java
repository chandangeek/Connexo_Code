/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cim;

public class ConsoleOutputStreamProvider implements OutputStreamProvider {

    @Override
    public void writeTo(OutputStreamClosure outputStreamClosure) {
        outputStreamClosure.using(System.out);
    }
}

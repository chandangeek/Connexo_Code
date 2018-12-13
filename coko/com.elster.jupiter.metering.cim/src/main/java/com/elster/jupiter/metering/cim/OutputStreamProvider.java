/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cim;

public interface OutputStreamProvider {

    void writeTo(OutputStreamClosure outputStreamClosure);
}

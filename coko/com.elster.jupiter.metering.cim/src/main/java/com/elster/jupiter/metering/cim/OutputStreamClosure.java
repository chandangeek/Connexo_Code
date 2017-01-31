/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cim;

import java.io.OutputStream;

public interface OutputStreamClosure {

    void using(OutputStream out);
}

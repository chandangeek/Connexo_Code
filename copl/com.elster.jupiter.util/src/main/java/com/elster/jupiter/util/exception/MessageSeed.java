/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.exception;

import java.util.logging.Level;

//
// intended to be implemented by enum
//
public interface MessageSeed {

    /**
     * @return three letter code that identifies the module, which defines this ExceptionType.
     */
    String getModule();

    int getNumber();

    String getKey();

    String getDefaultFormat();

    Level getLevel();
}

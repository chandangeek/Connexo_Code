package com.elster.jupiter.util.exception;

import java.util.logging.Level;

//
// intended to be implemented by enum
//
public interface MessageSeed {

    int getNumber();

    String getKey();

    String getDefaultFormat();

    Level getLevel();
}

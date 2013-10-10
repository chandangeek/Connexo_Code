package com.elster.jupiter.fileimport;

import com.elster.jupiter.fileimport.impl.Bus;
import com.elster.jupiter.util.exception.ExceptionType;

/**
 * Enumeration of all the FIM module exception types.
 */
enum ExceptionTypes implements ExceptionType {
    FILE_IO(1001);

    private final int number;

    ExceptionTypes(int number) {
        this.number = number;
    }

    @Override
    public String getModule() {
        return Bus.COMPONENTNAME;
    }

    @Override
    public int getNumber() {
        return number;
    }
}

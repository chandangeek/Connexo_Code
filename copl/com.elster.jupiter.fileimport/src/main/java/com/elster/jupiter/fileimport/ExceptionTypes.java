package com.elster.jupiter.fileimport;

import com.elster.jupiter.orm.plumbing.Bus;
import com.elster.jupiter.util.exception.ExceptionType;

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

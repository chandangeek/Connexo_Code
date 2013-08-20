package com.elster.jupiter.orm;

import com.elster.jupiter.orm.plumbing.Bus;
import com.elster.jupiter.util.exception.ExceptionType;

enum ExceptionTypes implements ExceptionType {
    DOES_NOT_EXIST(1001),
    NOT_UNIQUE(1002),
    SQL(1003),
    OPTIMISTIC_LOCK(1004),
    TRANSACTION_REQUIRED(1005),
    MAPPING_INTROSPECTION_FAILED(1006),
    MAPPING_MISMATCH(1007),
    MAPPING_NO_DISCRIMINATOR_COLUMN(1008),
    UNEXPECTED_NUMBER_OF_UPDATES(1009);

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

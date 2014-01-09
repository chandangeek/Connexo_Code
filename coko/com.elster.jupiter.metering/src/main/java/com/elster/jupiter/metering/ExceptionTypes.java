package com.elster.jupiter.metering;

import com.elster.jupiter.util.exception.ExceptionType;

public enum ExceptionTypes implements ExceptionType {
    ILLEGAL_MRID_FORMAT(1001), ILLEGAL_CURRENCY_CODE(1002);

    private final int number;

    ExceptionTypes(int number) {
        this.number = number;
    }

    @Override
    public String getModule() {
        return MeteringService.COMPONENTNAME;
    }

    @Override
    public int getNumber() {
        return number;
    }
}

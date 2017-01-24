package com.energyict.protocolimpl.landisgyr.maxsys2510;

import java.util.stream.Stream;

enum Bus {

    MTR_INPUT_BUS('I'),
    TOTALIZATION_BUS('T'),
    KOMPENSATION_BUS('K'),
    XFORM_BUS('X'),
    RATE_BUS('R'),
    CONTROL_TOU_BUS('C'),
    LOAD_CNTRL_BUS('L'),
    DISPLAY_BUS('D'),
    MISC_FUNC_BUS('M'),
    SUMM_CH_BUS('S');

    static Bus get(char id) {
        return Stream.of(values()).filter(each -> each.getId() == id).findFirst().orElse(null);
    }

    private char id;

    Bus(char id) {
        this.id = id;
    }

    char getId() {
        return id;
    }

    String getDescription() {
        return this.name();
    }

    public String toString() {
        return "Bus[ " + this.getId() + " " + this.getDescription() + "]";
    }

}
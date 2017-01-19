package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.io.FlowControl;
import com.energyict.mdc.io.Parities;
import com.energyict.mdc.ports.ComPortType;

public enum ComServerFieldTranslationKeys implements TranslationKey {

    COM_PORT_TYPE_SERVLET(ComPortType.SERVLET.toString(), "SERVLET"),
    COM_PORT_TYPE_TCP(ComPortType.TCP.toString(), "TCP"),
    COM_PORT_TYPE_UDP(ComPortType.UDP.toString(), "UDP"),
    COM_PORT_TYPE_SERIAL(ComPortType.SERIAL.toString(), "SERIAL"),

    FLOW_CONTROL_DTRDSR(FlowControl.DTRDSR.value(), "DTR/DSR"),
    FLOW_CONTROL_XONXOFF(FlowControl.XONXOFF.value(), "XON/XOFF"),
    FLOW_CONTROL_RTSCTS(FlowControl.RTSCTS.value(), "RTS/CTS"),
    FLOW_CONTROL_NONE(FlowControl.NONE.value(), "No flow control"),

    PARITY_EVEN(Parities.EVEN.value(), "Even Parity"),
    PARITY_ODD(Parities.ODD.value(), "Odd Parity"),
    PARITY_MARK(Parities.MARK.value(), "Mark Parity"),
    PARITY_SPACE(Parities.SPACE.value(), "Space Parity"),
    PARITY_NONE(Parities.NONE.value(), "No Parity"),
    ;

    private final String key;
    private final String format;

    ComServerFieldTranslationKeys(String key, String format) {
        this.key = key;
        this.format = format;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return format;
    }
}

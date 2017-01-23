package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.channels.serial.Parities;
import com.energyict.mdc.ports.ComPortType;

public enum ComServerFieldTranslationKeys implements TranslationKey {

    COM_PORT_TYPE_SERVLET(ComPortType.SERVLET.toString(), "SERVLET"),
    COM_PORT_TYPE_TCP(ComPortType.TCP.toString(), "TCP"),
    COM_PORT_TYPE_UDP(ComPortType.UDP.toString(), "UDP"),
    COM_PORT_TYPE_SERIAL(ComPortType.SERIAL.toString(), "SERIAL"),

    FLOW_CONTROL_DTRDSR(FlowControl.DTRDSR.getFlowControl(), "DTR/DSR"),
    FLOW_CONTROL_XONXOFF(FlowControl.XONXOFF.getFlowControl(), "XON/XOFF"),
    FLOW_CONTROL_RTSCTS(FlowControl.RTSCTS.getFlowControl(), "RTS/CTS"),
    FLOW_CONTROL_NONE(FlowControl.NONE.getFlowControl(), "No flow control"),

    PARITY_EVEN(Parities.EVEN.getParity(), "Even Parity"),
    PARITY_ODD(Parities.ODD.getParity(), "Odd Parity"),
    PARITY_MARK(Parities.MARK.getParity(), "Mark Parity"),
    PARITY_SPACE(Parities.SPACE.getParity(), "Space Parity"),
    PARITY_NONE(Parities.NONE.getParity(), "No Parity"),
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

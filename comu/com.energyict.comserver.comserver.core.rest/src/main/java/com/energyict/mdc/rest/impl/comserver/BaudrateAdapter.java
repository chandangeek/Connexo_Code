package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.channels.serial.BaudrateValue;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

public class BaudrateAdapter extends MapBasedXmlAdapter<BaudrateValue> {
    public BaudrateAdapter() {
        register("", null);
        register("150", BaudrateValue.BAUDRATE_150);
        register("300", BaudrateValue.BAUDRATE_300);
        register("600", BaudrateValue.BAUDRATE_600);
        register("1200", BaudrateValue.BAUDRATE_1200);
        register("1800", BaudrateValue.BAUDRATE_1800);
        register("2400", BaudrateValue.BAUDRATE_2400);
        register("4800", BaudrateValue.BAUDRATE_4800);
        register("7200", BaudrateValue.BAUDRATE_7200);
        register("9600", BaudrateValue.BAUDRATE_9600);
        register("14400", BaudrateValue.BAUDRATE_14400);
        register("19200", BaudrateValue.BAUDRATE_19200);
        register("28800", BaudrateValue.BAUDRATE_28800);
        register("38400", BaudrateValue.BAUDRATE_38400);
        register("56000", BaudrateValue.BAUDRATE_56000);
        register("57600", BaudrateValue.BAUDRATE_57600);
        register("76800", BaudrateValue.BAUDRATE_76800);
        register("115200", BaudrateValue.BAUDRATE_115200);
        register("230400", BaudrateValue.BAUDRATE_230400);
        register("460800", BaudrateValue.BAUDRATE_460800);
    }
}

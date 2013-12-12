package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.channels.serial.BaudrateValue;

/**
 * Converts BaudRateValue to String and vice-versa
 */
public class BaudRateValueConverter {

    public String fromServerValue(BaudrateValue baudrateValue) {
        if (baudrateValue!=null) {
            return ""+baudrateValue.getBaudrate().intValue();
        } else {
            return null;
        }
    }

    public BaudrateValue toServerValue(String baudrate) {
        BaudrateValue mappedValue=null;
        if (baudrate!=null && !baudrate.isEmpty()) {
            int jsonBaudrate = Integer.parseInt(baudrate);
            for (BaudrateValue baudrateValue : BaudrateValue.values()) {
                if (baudrateValue.getBaudrate().intValue()==jsonBaudrate) {
                    mappedValue = baudrateValue;
                }
            }
            if (mappedValue==null) {
                throw new IllegalArgumentException("Unable to map baudrate "+baudrate+" to an existing baudrate");
            }
        }
        return mappedValue;
    }
}

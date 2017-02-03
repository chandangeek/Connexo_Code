package com.energyict.mdc.upl.meterdata;

import com.energyict.obis.ObisCode;

import java.util.Date;

public interface LogBook {

    /**
     * This is the <i>generic</i> ObisCode that will be used for migrating <i>old</i> devices.
     */
    ObisCode GENERIC_LOGBOOK_TYPE_OBISCODE = ObisCode.fromString("0.0.99.98.0.255");

    Date getLastReading();

}
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.generic;


import com.energyict.mdc.common.ObisCode;

public interface CommonObisCodeProvider {

    /**
     * @return the obisCode for the ClockObject
     */
    ObisCode getClockObisCode();

    /**
     * @return the obisCode for the <i>default</i> LoadProfile
     */
    ObisCode getDefaultLoadProfileObisCode();

    /**
     * @return the obisCode fro the <i>Daily</i> LoadProfile
     */
    ObisCode getDailyLoadProfileObisCode();

    /**
     * @return the obisCode for the SerialNumber
     */
    ObisCode getSerialNumberObisCode();

    /**
     * @return the obisCode for the AssociationLn object
     */
    ObisCode getAssociationLnObisCode();

    /**
     * @return the obisCode for the Firmware Version
     */
    ObisCode getFirmwareVersion();
}

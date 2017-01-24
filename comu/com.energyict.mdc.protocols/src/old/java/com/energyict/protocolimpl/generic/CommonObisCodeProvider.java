package com.energyict.protocolimpl.generic;


import com.energyict.obis.ObisCode;

/**
 * Common getters for ObisCodes
 *
 * <pre>
 * Copyrights EnergyICT
 * Date: 23-nov-2010
 * Time: 16:30:45
 * </pre>
 */
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

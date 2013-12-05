package com.energyict.mdc.protocol.api.device.offline;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Offline;
import com.energyict.mdc.common.Unit;

/**
 * Represents an Offline version of a register.
 *
 * @author gna
 * @since 12/06/12 - 11:48
 */
public interface OfflineRegister extends Offline {

    /**
     * @return the ID of the Register
     */
    public int getRegisterId ();

    /**
     * Returns the ObisCode for this Register.<br/>
     * (actually the ObisCode from the RegisterMapping)
     *
     * @return the ObisCode
     */
    public ObisCode getObisCode();


    /**
     * Get the business Id of the RegisterGroup where this registers belongs to.
     *
     * @return the ID of the RegisterGroup
     */
    public int getRegisterGroupId();

    /**
     * The {@link Unit} corresponding with this register
     *
     * @return the unit of this register
     */
    public Unit getUnit();

    /**
     * Returns the unique identifier of the
     * device owning this OfflineRegister.
     *
     * @return The Device's id
     */
    public int getDeviceId ();

    /**
     * The serialNumber of the {@link OfflineDevice} owning this OfflineRegister.
     *
     * @return the serialNumber of the Device owning this Register
     */
    public String getSerialNumber();

}
package com.energyict.mdc.protocol.api.device.offline;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Offline;
import com.energyict.mdc.common.Unit;

import java.math.BigDecimal;

/**
 * Represents an Offline version of a Channel in a specific LoadProfile
 *
 * @author gna
 * @since 30/05/12 - 9:49
 */
public interface OfflineLoadProfileChannel extends Offline {

    /**
     * Returns the {@link ObisCode} for this Channel in the LoadProfile
     *
     * @return the {@link ObisCode}
     */
    public ObisCode getObisCode();

    /**
     * Returns the ID of the Device for the LoadProfile object.
     *
     * @return the ID of the Device.
     */
    public int getRtuId();

    /**
     * Returns the ID of the LoadProfile
     *
     * @return the ID of the LoadProfile.
     */
    public int getLoadProfileId();

    /**
     * Returns the receiver's configured unit.
     *
     * @return the configured unit.
     */
    public Unit getUnit();

    /**
     * Indication whether we should store data for this channel
     *
     * @return true if we should store data for this channel, false otherwise
     */
    public boolean isStoreData();

    /**
     * Returns the SerialNumber of the Device
     *
     * @return the SerialNumber of the Device
     */
    public String getMasterSerialNumber();

    /**
     * Returns the ReadingType of the Kore channel that will store the data
     *
     * @return the ReadingType
     */
    public ReadingType getReadingType();

    /**
     * Gets the configured overflow value for this channel
     *
     * @return the configured overflow value
     */
    public BigDecimal getOverflow();
}
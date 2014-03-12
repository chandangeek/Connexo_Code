package com.energyict.mdc.device.config;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import java.util.Date;

/**
 * Represents a register definition.
 *
 * @author Geert
 */
public interface RegisterMapping {

    /**
     * Returns number that uniquely identifies this RegisterMapping.
     *
     * @return the id
     */
    public long getId();

    /**
     * Returns the name that uniquely identifies this RegisterMapping.
     *
     * @return the name
     */
    public String getName();

    public void setName (String newName);

    /**
     * Returns the obis code for this mapping
     *
     * @return the obis code
     */
    public ObisCode getObisCode();

    public void setObisCode(ObisCode obisCode);

    /**
     * Test if this mapping represents a cumulative reading
     *
     * @return true if cumulative , false otherwise
     */
    public boolean isCumulative();

    public void setCumulative(boolean cumulative);

    /**
     * Returns the mapping's unit
     *
     * @return the mapping's unit
     */
    public Unit getUnit();

    public void setUnit(Unit unit);

    public ReadingType getReadingType ();

    public void setReadingType(ReadingType readingType);

    /**
     * Returns the <code>RegisterGroup</code> the receiver belongs to
     *
     * @return the <code>RegisterGroup</code> the receiver belongs to
     */
    public RegisterGroup getRegisterGroup();

    public void setRegisterGroup(RegisterGroup registerGroup);

    /**
     * Returns the receiver's description
     *
     * @return the receiver's description
     */
    public String getDescription();

    public void setDescription(String newDescription);

    /**
     * Returns the receiver's last modification date
     *
     * @return the last modification date
     */
    public Date getModificationDate();

    /**
     * Indicates that this register mapping is used by a channel spec and/or register spec
     * @return true if in use
     */
    boolean isInUse();

    public void save ();

    public void delete ();

    public int getTimeOfUse();

    public void setTimeOfUse(int timeOfUse);

    /**
     * Indicates that this register mapping has been linked to a device type or not
     * @return boolean
     */
    public boolean isLinkedByDeviceType();
}

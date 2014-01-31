package com.energyict.mdc.device.config;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.MultiplierMode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Represents a register specification
 *
 * @author Geert
 */
public interface RegisterSpec {

    public long getId();

    /**
     * Return the spec's <code>DeviceConfiguration</code>
     *
     * @return the DeviceConfiguration
     */
    public DeviceConfiguration getDeviceConfiguration();


    /**
     * Returns the register mapping for this spec
     *
     * @return the register mapping
     */
    public RegisterMapping getRegisterMapping();


    /**
     * Tests if this is a cumulative register mapping
     *
     * @return true if cumulative, false otherwise.
     */
    public boolean isCumulative();

    /**
     * Returns the spec's unit
     *
     * @return the unit
     */
    public Unit getUnit();

    /**
     * Returns the spec's obis code
     *
     * @return the obis code
     */
    public ObisCode getObisCode();


    /**
     * Returns the obis code of the device.
     *
     * @return the obis code of the device
     */
    public ObisCode getDeviceObisCode();


    /**
     * Returns the receiver's last modification date
     *
     * @return the last modification date
     */
    public Date getModificationDate();

    /**
     * @return the default configuration, which may be changed per individual Register of this RegisterSpec
     */
    public RegisterConfiguration getRegisterConfiguration();

    /**
     * @return the linked ChannelSpec in case of a Prime register
     */
    public ChannelSpec getLinkedChannelSpec();

    public void save ();

    public void delete ();

    void setDeviceConfig(DeviceConfiguration deviceConfig);

    void setRegisterMapping(RegisterMapping registerMapping);

    void setLinkedChannelSpec(ChannelSpec linkedChannelSpec);

    void setNumberOfDigits(int numberOfDigits);

    void setNumberOfFractionDigits(int numberOfFractionDigits);

    void setOverruledObisCode(ObisCode overruledObisCode);

    void setOverflow(BigDecimal overflow);

    void setMultiplier(BigDecimal multiplier);

    void setMultiplierMode(MultiplierMode multiplierMode);
}
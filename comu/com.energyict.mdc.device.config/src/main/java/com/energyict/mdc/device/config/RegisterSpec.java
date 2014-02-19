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
public interface RegisterSpec extends HasId {

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
     * @return the linked ChannelSpec in case of a Prime register
     */
    public ChannelSpec getLinkedChannelSpec();

    /**
     * Returns the number of digits for this spec
     *
     * @return the number of digits
     */
    public int getNumberOfDigits();

    /**
     * Returns the number of fraction digits for this spec
     *
     * @return the number of fraction digits
     */
    public int getNumberOfFractionDigits();

    /**
     * Returns the configured multiplier.
     *
     * @return the receiver's multiplier.
     */
    BigDecimal getMultiplier();

    /**
     * Returns the configured multiplier mode.
     *
     * @return the receiver's multiplier mode.
     */
    MultiplierMode getMultiplierMode();

    /**
     * Returns the overflow value
     *
     * @return the overflow value
     */
    public BigDecimal getOverflowValue();

    void setDeviceConfig(DeviceConfiguration deviceConfig);

    void setRegisterMapping(RegisterMapping registerMapping);

    void setLinkedChannelSpec(ChannelSpec linkedChannelSpec);

    void setNumberOfDigits(int numberOfDigits);

    void setNumberOfFractionDigits(int numberOfFractionDigits);

    void setOverruledObisCode(ObisCode overruledObisCode);

    void setOverflow(BigDecimal overflow);

    /**
     * Set the Multiplier of the RegisterSpec to the given <i>multiplier</i>.
     * <p/>
     * <b>Note:</b> By setting the Multiplier, you automatically set the MultiplierMode to {@link MultiplierMode#CONFIGURED_ON_OBJECT}
     *
     * @param multiplier the multiplier to set
     */
    void setMultiplier(BigDecimal multiplier);

    /**
     * Set the MultiplierMode to the given mode.
     * <p/>
     * <b>Note:</b> By setting the mode to either {@link MultiplierMode#VERSIONED} or {@link MultiplierMode#NONE},
     * you automatically set a previously given Multiplier back to {@link BigDecimal#ONE}
     *
     * @param multiplierMode the given MultiplierMode
     */
    void setMultiplierMode(MultiplierMode multiplierMode);

    ChannelSpecLinkType getChannelSpecLinkType();

    void setChannelSpecLinkType(ChannelSpecLinkType channelSpecLinkType);

    void validateDelete();

    void validateUpdate();

    /**
     * Defines a Builder interface to construct a {@link RegisterSpec}
     */
    interface RegisterSpecBuilder {

        RegisterSpecBuilder setRegisterMapping(RegisterMapping registerMapping);

        RegisterSpecBuilder setLinkedChannelSpec(ChannelSpec linkedChannelSpec);

        RegisterSpecBuilder setNumberOfDigits(int numberOfDigits);

        RegisterSpecBuilder setNumberOfFractionDigits(int numberOfFractionDigits);

        RegisterSpecBuilder setOverruledObisCode(ObisCode overruledObisCode);

        RegisterSpecBuilder setOverflow(BigDecimal overflow);

        /**
         * Set the Multiplier of the RegisterSpec to the given <i>multiplier</i>.
         * <p/>
         * <b>Note:</b> By setting the Multiplier, you automatically set the MultiplierMode to {@link MultiplierMode#CONFIGURED_ON_OBJECT}
         *
         * @param multiplier the multiplier to set
         */
        RegisterSpecBuilder setMultiplier(BigDecimal multiplier);

        /**
         * Set the MultiplierMode to the given mode.
         * <p/>
         * <b>Note:</b> By setting the mode to either {@link MultiplierMode#VERSIONED} or {@link MultiplierMode#NONE},
         * you automatically set a previously given Multiplier back to {@link BigDecimal#ONE}
         *
         * @param multiplierMode the given MultiplierMode
         */
        RegisterSpecBuilder setMultiplierMode(MultiplierMode multiplierMode);

        RegisterSpecBuilder setChannelSpecLinkType(ChannelSpecLinkType channelSpecLinkType);

        /**
         * Does final validation and <i>creates</i> the {@link RegisterSpec}
         *
         * @return the RegisterSpec
         */
        RegisterSpec add();
    }

    /**
     * Defines a updater component to update a {@link RegisterSpec}
     */
    interface RegisterSpecUpdater {

        RegisterSpecUpdater setRegisterMapping(RegisterMapping registerMapping);

        RegisterSpecUpdater setLinkedChannelSpec(ChannelSpec linkedChannelSpec);

        RegisterSpecUpdater setNumberOfDigits(int numberOfDigits);

        RegisterSpecUpdater setNumberOfFractionDigits(int numberOfFractionDigits);

        RegisterSpecUpdater setOverruledObisCode(ObisCode overruledObisCode);

        RegisterSpecUpdater setOverflow(BigDecimal overflow);

        /**
         * Set the Multiplier of the RegisterSpec to the given <i>multiplier</i>.
         * <p/>
         * <b>Note:</b> By setting the Multiplier, you automatically set the MultiplierMode to {@link MultiplierMode#CONFIGURED_ON_OBJECT}
         *
         * @param multiplier the multiplier to set
         */
        RegisterSpecUpdater setMultiplier(BigDecimal multiplier);

        /**
         * Set the MultiplierMode to the given mode.
         * <p/>
         * <b>Note:</b> By setting the mode to either {@link MultiplierMode#VERSIONED} or {@link MultiplierMode#NONE},
         * you automatically set a previously given Multiplier back to {@link BigDecimal#ONE}
         *
         * @param multiplierMode the given MultiplierMode
         */
        RegisterSpecUpdater setMultiplierMode(MultiplierMode multiplierMode);

        RegisterSpecUpdater setChannelSpecLinkType(ChannelSpecLinkType channelSpecLinkType);

        /**
         * Updates the RegisterSpec, preferably via his DeviceConfiguration
         */
        void update();
    }
}
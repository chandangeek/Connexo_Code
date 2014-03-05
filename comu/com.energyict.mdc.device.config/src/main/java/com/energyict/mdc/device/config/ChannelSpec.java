package com.energyict.mdc.device.config;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import com.energyict.mdc.protocol.api.device.ReadingMethod;
import com.energyict.mdc.protocol.api.device.ValueCalculationMethod;
import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 7/11/12
 * Time: 13:16
 */
public interface ChannelSpec extends HasId {

    /**
     * Returns the object's unique id
     *
     * @return the id
     */
    public long getId();

    /**
     * Returns the object's name
     *
     * @return the name
     */
    public String getName();

    public void setName(String name);

    RegisterMapping getRegisterMapping();

    ObisCode getDeviceObisCode();

    ObisCode getObisCode();

    int getNbrOfFractionDigits();

    BigDecimal getOverflow();

    Phenomenon getPhenomenon();

    ReadingMethod getReadingMethod();

    MultiplierMode getMultiplierMode();

    BigDecimal getMultiplier();

    ValueCalculationMethod getValueCalculationMethod();

    LoadProfileSpec getLoadProfileSpec();

    TimeDuration getInterval();

    DeviceConfiguration getDeviceConfiguration();

    void setDeviceConfiguration(DeviceConfiguration deviceConfiguration);

    void setRegisterMapping(RegisterMapping registerMapping);

    void setOverruledObisCode(ObisCode overruledObisCode);

    void setNbrOfFractionDigits(int nbrOfFractionDigits);

    void setOverflow(BigDecimal overflow);

    void setPhenomenon(Phenomenon phenomenon);

    void setReadingMethod(ReadingMethod readingMethod);

    void setMultiplierMode(MultiplierMode multiplierMode);

    void setMultiplier(BigDecimal multiplier);

    void setValueCalculationMethod(ValueCalculationMethod valueCalculationMethod);

    void setLoadProfileSpec(LoadProfileSpec loadProfileSpec);

    void setInterval(TimeDuration interval);

    void setProductSpec(ProductSpec productSpec);

    ProductSpec getProductSpec();

    void validateDelete();

    void save();

    /**
     * Defines a Builder interface to construct a {@link ChannelSpec}
     */
    interface ChannelSpecBuilder extends LoadProfileSpec.BuildingCompletionListener {

        ChannelSpecBuilder setName(String channelSpecName);

        ChannelSpecBuilder setOverruledObisCode(ObisCode overruledObisCode);

        ChannelSpecBuilder setNbrOfFractionDigits(int nbrOfFractionDigits);

        ChannelSpecBuilder setOverflow(BigDecimal overflow);

        ChannelSpecBuilder setReadingMethod(ReadingMethod readingMethod);

        ChannelSpecBuilder setMultiplierMode(MultiplierMode multiplierMode);

        ChannelSpecBuilder setMultiplier(BigDecimal multiplier);

        ChannelSpecBuilder setValueCalculationMethod(ValueCalculationMethod valueCalculationMethod);

        ChannelSpecBuilder setInterval(TimeDuration interval);

        /**
         * Does final validation and <i>creates</i> the {@link ChannelSpec}
         * @return the ChannelSpec
         */
        ChannelSpec add();
    }

    interface ChannelSpecUpdater {

        ChannelSpecUpdater setOverruledObisCode(ObisCode overruledObisCode);

        ChannelSpecUpdater setNbrOfFractionDigits(int nbrOfFractionDigits);

        ChannelSpecUpdater setOverflow(BigDecimal overflow);

        ChannelSpecUpdater setReadingMethod(ReadingMethod readingMethod);

        ChannelSpecUpdater setMultiplierMode(MultiplierMode multiplierMode);

        ChannelSpecUpdater setMultiplier(BigDecimal multiplier);

        ChannelSpecUpdater setValueCalculationMethod(ValueCalculationMethod valueCalculationMethod);

        /**
         * Updates the ChannelSpec, preferably via his DeviceConfiguration
         */
        void update();
    }
}

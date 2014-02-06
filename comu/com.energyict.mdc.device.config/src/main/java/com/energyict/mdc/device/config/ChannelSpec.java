package com.energyict.mdc.device.config;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import com.energyict.mdc.protocol.api.device.ReadingMethod;
import com.energyict.mdc.protocol.api.device.ValueCalculationMethod;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 7/11/12
 * Time: 13:16
 */
public interface ChannelSpec {

    /**
     * Returns the object's unique id
     *
     * @return the id
     */
    public int getId();

    /**
     * Returns the object's name
     *
     * @return the name
     */
    public String getName();

    RegisterMapping getRtuRegisterMapping();

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

    DeviceConfiguration getDeviceConfig();
}

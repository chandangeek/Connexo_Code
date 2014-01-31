package com.energyict.mdc.device.config;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Phenomenon;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.NamedBusinessObject;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import com.energyict.mdc.protocol.api.device.ReadingMethod;
import com.energyict.mdc.protocol.api.device.ValueCalculationMethod;
import com.energyict.mdw.shadow.ChannelShadow;
import com.energyict.mdw.shadow.ChannelSpecShadow;
import com.energyict.mdw.xml.Exportable;
import com.energyict.mdc.common.ObisCode;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Copyrights EnergyICT
 * Date: 7/11/12
 * Time: 13:16
 */
public interface ChannelSpec extends NamedBusinessObject, Exportable<ChannelSpec> {

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

    ChannelSpecShadow getShadow();

    void update(ChannelSpecShadow shadow) throws BusinessException, SQLException;

    ChannelShadow newChannelShadow();
}

package com.energyict.mdc.device.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.validation.ValidationRule;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.protocol.api.device.ReadingMethod;
import com.energyict.mdc.protocol.api.device.ValueCalculationMethod;

import java.math.BigDecimal;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 7/11/12
 * Time: 13:16
 */
@ProviderType
public interface ChannelSpec extends HasId {

    ChannelType getChannelType();

    ObisCode getDeviceObisCode();

    ObisCode getObisCode();

    int getNbrOfFractionDigits();

    BigDecimal getOverflow();

    ReadingMethod getReadingMethod();

    ValueCalculationMethod getValueCalculationMethod();

    LoadProfileSpec getLoadProfileSpec();

    TimeDuration getInterval();

    DeviceConfiguration getDeviceConfiguration();

    void setChannelType(ChannelType channelType);

    void setOverruledObisCode(ObisCode overruledObisCode);

    void setNbrOfFractionDigits(int nbrOfFractionDigits);

    void setOverflow(BigDecimal overflow);

    void setReadingMethod(ReadingMethod readingMethod);

    void setValueCalculationMethod(ValueCalculationMethod valueCalculationMethod);

    void setInterval(TimeDuration interval);

    ReadingType getReadingType();

    void validateDelete();

    void save();

    List<ValidationRule> getValidationRules();

    /**
     * Defines a Builder interface to construct a {@link ChannelSpec}.
     */
    interface ChannelSpecBuilder {

        ChannelSpecBuilder setOverruledObisCode(ObisCode overruledObisCode);

        ChannelSpecBuilder setNbrOfFractionDigits(int nbrOfFractionDigits);

        ChannelSpecBuilder setOverflow(BigDecimal overflow);

        ChannelSpecBuilder setReadingMethod(ReadingMethod readingMethod);

        ChannelSpecBuilder setValueCalculationMethod(ValueCalculationMethod valueCalculationMethod);

        ChannelSpecBuilder setInterval(TimeDuration interval);

        /**
         * Does final validation and <i>creates</i> the {@link ChannelSpec}.
         * @return the ChannelSpec
         */
        ChannelSpec add();
    }

    interface ChannelSpecUpdater {

        ChannelSpecUpdater setOverruledObisCode(ObisCode overruledObisCode);

        ChannelSpecUpdater setNbrOfFractionDigits(int nbrOfFractionDigits);

        ChannelSpecUpdater setOverflow(BigDecimal overflow);

        ChannelSpecUpdater setReadingMethod(ReadingMethod readingMethod);

        ChannelSpecUpdater setValueCalculationMethod(ValueCalculationMethod valueCalculationMethod);

        /**
         * Updates the ChannelSpec.
         */
        void update();
    }

}
package com.energyict.mdc.device.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.protocol.api.device.ReadingMethod;
import com.energyict.mdc.protocol.api.device.ValueCalculationMethod;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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

    boolean isUseMultiplier();

    void setUseMultiplier(boolean useMultiplier);

    Optional<ReadingType> getCalculatedReadingType();

    void setCalculatedReadingType(ReadingType calculatedReadingType);

    ReadingType getReadingType();

    void validateDelete();

    void save();

    List<ValidationRule> getValidationRules();

    long getVersion();
    /**
     * Defines a Builder interface to construct a {@link ChannelSpec}.
     */
    interface ChannelSpecBuilder {

        ChannelSpecBuilder overruledObisCode(ObisCode overruledObisCode);

        ChannelSpecBuilder nbrOfFractionDigits(int nbrOfFractionDigits);

        ChannelSpecBuilder overflow(BigDecimal overflow);

        ChannelSpecBuilder readingMethod(ReadingMethod readingMethod);

        ChannelSpecBuilder valueCalculationMethod(ValueCalculationMethod valueCalculationMethod);

        ChannelSpecBuilder interval(TimeDuration interval);

        ChannelSpecBuilder useMultiplier(boolean useMultiplier);

        ChannelSpecBuilder calculatedReadingType(ReadingType calculatedReadingType);
        /**
         * Does final validation and <i>creates</i> the {@link ChannelSpec}.
         * @return the ChannelSpec
         */
        ChannelSpec add();
    }

    interface ChannelSpecUpdater {

        ChannelSpecUpdater overruledObisCode(ObisCode overruledObisCode);

        ChannelSpecUpdater nbrOfFractionDigits(int nbrOfFractionDigits);

        ChannelSpecUpdater overflow(BigDecimal overflow);

        ChannelSpecUpdater readingMethod(ReadingMethod readingMethod);

        ChannelSpecUpdater valueCalculationMethod(ValueCalculationMethod valueCalculationMethod);

        ChannelSpecUpdater useMultiplier(boolean useMultiplier);

        ChannelSpecUpdater calculatedReadingType(ReadingType calculatedReadingType);

        /**
         * Updates the ChannelSpec.
         */
        void update();
    }

}
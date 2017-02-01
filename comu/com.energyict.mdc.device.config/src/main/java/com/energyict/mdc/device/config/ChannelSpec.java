/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.validation.ValidationRule;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.masterdata.ChannelType;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface ChannelSpec extends HasId {

    ChannelType getChannelType();

    ObisCode getDeviceObisCode();

    ObisCode getObisCode();

    int getNbrOfFractionDigits();

    Optional<BigDecimal> getOverflow();

    LoadProfileSpec getLoadProfileSpec();

    TimeDuration getInterval();

    DeviceConfiguration getDeviceConfiguration();

    boolean isUseMultiplier();

    Optional<ReadingType> getCalculatedReadingType();

    ReadingType getReadingType();

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

        ChannelSpecBuilder interval(TimeDuration interval);

        ChannelSpecBuilder useMultiplierWithCalculatedReadingType(ReadingType calculatedReadingType);

        ChannelSpecBuilder noMultiplier();
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

        ChannelSpecUpdater useMultiplierWithCalculatedReadingType(ReadingType calculatedReadingType);

        ChannelSpecUpdater noMultiplier();

        /**
         * Updates the ChannelSpec.
         */
        void update();
    }

}
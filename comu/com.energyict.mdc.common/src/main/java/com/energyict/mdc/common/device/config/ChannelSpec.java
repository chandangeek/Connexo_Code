/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.validation.ValidationRule;
import com.energyict.mdc.common.masterdata.ChannelType;

import aQute.bnd.annotation.ConsumerType;
import com.energyict.obis.ObisCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ConsumerType
public interface ChannelSpec extends HasId {

    ChannelType getChannelType();

    ObisCode getDeviceObisCode();

    ObisCode getObisCode();

    int getNbrOfFractionDigits();

    Optional<BigDecimal> getOverflow();

    long getOffset();

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

        ChannelSpecBuilder offset(long offset);

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

        ChannelSpecUpdater offset(long offset);

        ChannelSpecUpdater noMultiplier();

        /**
         * Updates the ChannelSpec.
         */
        void update();
    }

}
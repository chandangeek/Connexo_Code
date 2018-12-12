/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.validation.ValidationRule;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.obis.ObisCode;

import java.util.List;

@ProviderType
public interface LoadProfileSpec extends HasId {

    LoadProfileType getLoadProfileType();

    DeviceConfiguration getDeviceConfiguration();

    ObisCode getDeviceObisCode();

    ObisCode getObisCode();

    TimeDuration getInterval();

    void setOverruledObisCode(ObisCode overruledObisCode);

    void validateDelete();

    void prepareDelete();

    void delete();

    void save();

    List<ValidationRule> getValidationRules();

    List<ChannelSpec> getChannelSpecs();

    long getVersion();

    /**
     * Defines a Builder interface to construct a {@link LoadProfileSpec}.
     */
    interface LoadProfileSpecBuilder {

        LoadProfileSpecBuilder setOverruledObisCode(ObisCode overruledObisCode);

        /**
         * Does final validation and <i>creates</i> the {@link LoadProfileSpec}.
         * @return the LoadProfileSpec
         */
        LoadProfileSpec add();
    }

    /**
     * Defines an <i>update</i> component to update a {@link LoadProfileSpec} implementation.
     */
    interface LoadProfileSpecUpdater {

        LoadProfileSpecUpdater setOverruledObisCode(ObisCode overruledObisCode);

        /**
         * Updates the LoadProfileSpec.
         */
        void update();
    }

}
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.validation.ValidationRule;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.masterdata.LoadProfileType;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface LoadProfileSpec extends HasId {

    public LoadProfileType getLoadProfileType();

    public DeviceConfiguration getDeviceConfiguration();

    public ObisCode getDeviceObisCode();

    public ObisCode getObisCode();

    public TimeDuration getInterval();

    public void setOverruledObisCode(ObisCode overruledObisCode);

    void validateDelete();

    void prepareDelete();

    void delete();

    void save();

    public List<ValidationRule> getValidationRules();

    public List<ChannelSpec> getChannelSpecs();

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
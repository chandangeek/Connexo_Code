/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Range;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.masterdata.RegisterType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Optional;

@ValidOverFlowAndNumberOfFractionDigits(groups = {Save.Create.class, Save.Update.class})
@ValidRegisterSpecMultiplierConfiguration(groups = {Save.Create.class, Save.Update.class})
@ValidateUpdatableRegisterSpecFields(groups = {Save.Update.class})
@RegisterOverflowValueValidation(groups = {Save.Create.class, Save.Update.class})
public class NumericalRegisterSpecImpl extends RegisterSpecImpl<NumericalRegisterSpec> implements NumericalRegisterSpec {

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.REGISTER_SPEC_INVALID_NUMBER_OF_FRACTION_DIGITS + "}")
    @Range(min = 0, max = 6, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.REGISTER_SPEC_INVALID_NUMBER_OF_FRACTION_DIGITS + "}")
    private Integer numberOfFractionDigits;
    private BigDecimal overflow;

    private boolean useMultiplier;
    private Reference<ReadingType> calculatedReadingType = ValueReference.absent();

    @Inject
    public NumericalRegisterSpecImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(NumericalRegisterSpec.class, dataModel, eventService, thesaurus);
    }

    protected NumericalRegisterSpecImpl initialize(DeviceConfiguration configuration, RegisterType registerType) {
        super.initialize(configuration, registerType);
        return this;
    }

    @Override
    public boolean isCumulative() {
        return getRegisterType().isCumulative();
    }

    public int getNumberOfFractionDigits() {
        return numberOfFractionDigits;
    }

    protected boolean hasNumberOfFractionDigits() {
        return numberOfFractionDigits != null;
    }

    public void setNumberOfFractionDigits(int numberOfFractionDigits) {
        this.numberOfFractionDigits = numberOfFractionDigits;
    }

    public Optional<BigDecimal> getOverflowValue() {
        return Optional.ofNullable(overflow);
    }

    public void setOverflowValue(BigDecimal overflowValue) {
        this.overflow = overflowValue;
    }

    public boolean isUseMultiplier() {
        return useMultiplier;
    }

    public void setUseMultiplier(boolean useMultiplier) {
        this.useMultiplier = useMultiplier;
    }

    public Optional<ReadingType> getCalculatedReadingType() {
        return calculatedReadingType.getOptional();
    }

    public void setCalculatedReadingType(ReadingType calculatedReadingType) {
        this.calculatedReadingType.set(calculatedReadingType);
    }

    protected void validate() {
        super.validate();
    }

    abstract static class AbstractBuilder implements Builder {

        private final NumericalRegisterSpecImpl registerSpec;

        AbstractBuilder(Provider<NumericalRegisterSpecImpl> registerSpecProvider, DeviceConfiguration deviceConfiguration, RegisterType registerType) {
            super();
            this.registerSpec = registerSpecProvider.get().initialize(deviceConfiguration, registerType);
        }

        @Override
        public Builder overruledObisCode(ObisCode overruledObisCode) {
            this.registerSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }

        @Override
        public Builder numberOfFractionDigits(int numberOfFractionDigits) {
            this.registerSpec.setNumberOfFractionDigits(numberOfFractionDigits);
            return this;
        }

        @Override
        public Builder overflowValue(BigDecimal overflowValue) {
            this.registerSpec.setOverflowValue(overflowValue);
            return this;
        }

        @Override
        public Builder noMultiplier() {
            this.registerSpec.setUseMultiplier(false);
            this.registerSpec.setCalculatedReadingType(null);
            return this;
        }

        @Override
        public Builder useMultiplierWithCalculatedReadingType(ReadingType calculatedReadingType) {
            this.registerSpec.setUseMultiplier(true);
            this.registerSpec.setCalculatedReadingType(calculatedReadingType);
            return this;
        }

        @Override
        public NumericalRegisterSpec add() {
            this.registerSpec.validateBeforeAddToConfiguration();
            return this.registerSpec;
        }
    }

    abstract static class AbstractUpdater implements Updater {

        private final NumericalRegisterSpecImpl registerSpec;

        AbstractUpdater(NumericalRegisterSpecImpl registerSpec) {
            super();
            this.registerSpec = registerSpec;
        }

        protected NumericalRegisterSpec updateTarget() {
            return this.registerSpec;
        }

        @Override
        public AbstractUpdater overruledObisCode(ObisCode overruledObisCode) {
            this.registerSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }

        @Override
        public Updater numberOfFractionDigits(int numberOfFractionDigits) {
            this.registerSpec.setNumberOfFractionDigits(numberOfFractionDigits);
            return this;
        }

        @Override
        public Updater overflowValue(BigDecimal overflowValue) {
            this.registerSpec.setOverflowValue(overflowValue);
            return this;
        }

        @Override
        public Updater noMultiplier() {
            this.registerSpec.setUseMultiplier(false);
            this.registerSpec.setCalculatedReadingType(null);
            return this;
        }

        @Override
        public Updater useMultiplierWithCalculatedReadingType(ReadingType calculatedReadingType) {
            this.registerSpec.setUseMultiplier(true);
            this.registerSpec.setCalculatedReadingType(calculatedReadingType);
            return this;
        }

        @Override
        public void update() {
            this.registerSpec.validateUpdate();
            this.registerSpec.save();
        }
    }

    @Override
    public RegisterSpec cloneForDeviceConfig(DeviceConfiguration deviceConfiguration) {
        Builder builder = deviceConfiguration.createNumericalRegisterSpec(getRegisterType());
        builder.numberOfFractionDigits(getNumberOfFractionDigits());
        getOverflowValue().ifPresent(builder::overflowValue);
        builder.overruledObisCode(getObisCode().equals(getDeviceObisCode()) ? null : getDeviceObisCode());
        if(isUseMultiplier()){
            builder.useMultiplierWithCalculatedReadingType(getCalculatedReadingType().get());
        } else {
            builder.noMultiplier();
        }
        return builder.add();
    }
}

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.exceptions.OverFlowValueCanNotExceedNumberOfDigitsException;
import com.energyict.mdc.device.config.exceptions.OverFlowValueHasIncorrectFractionDigitsException;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.device.MultiplierMode;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import org.hibernate.validator.constraints.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@ValidNumericalRegisterSpec(groups = { Save.Update.class })
public class NumericalRegisterSpecImpl extends RegisterSpecImpl<NumericalRegisterSpec> implements NumericalRegisterSpec {

    @Range(min = 1, max = 20, groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.REGISTER_SPEC_INVALID_NUMBER_OF_DIGITS + "}")
    private int numberOfDigits;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.REGISTER_SPEC_INVALID_NUMBER_OF_FRACTION_DIGITS + "}")
    @Range(min=0, max = 6, groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.REGISTER_SPEC_INVALID_NUMBER_OF_FRACTION_DIGITS + "}")
    private Integer numberOfFractionDigits;
    @Min(value=1, groups = { Save.Create.class, Save.Update.class }, message = "{"+MessageSeeds.Keys.REGISTER_SPEC_INVALID_OVERFLOW_VALUE + "}")
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+MessageSeeds.Keys.REGISTER_SPEC_OVERFLOW_IS_REQUIRED + "}")
    private BigDecimal overflowValue;
    @Min(value=1, groups = { Save.Create.class, Save.Update.class }, message = "{"+MessageSeeds.Keys.REGISTER_SPEC_INVALID_MULTIPLIER_VALUE + "}")
    private BigDecimal multiplier = BigDecimal.ONE;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+MessageSeeds.Keys.REGISTER_SPEC_MULTIPLIER_MODE_IS_REQUIRED + "}")
    private MultiplierMode multiplierMode = MultiplierMode.CONFIGURED_ON_OBJECT;

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

    public int getNumberOfDigits() {
        return numberOfDigits;
    }

    @Override
    public void setNumberOfDigits(int numberOfDigits) {
        this.numberOfDigits = numberOfDigits;
    }

    public int getNumberOfFractionDigits() {
        return numberOfFractionDigits;
    }

    @Override
    public void setNumberOfFractionDigits(int numberOfFractionDigits) {
        this.numberOfFractionDigits = numberOfFractionDigits;
    }

    public BigDecimal getOverflowValue() {
        return overflowValue;
    }

    @Override
    public void setOverflowValue(BigDecimal overflowValue) {
        this.overflowValue = overflowValue;
    }

    public MultiplierMode getMultiplierMode() {
        return multiplierMode;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    protected void validate() {
        this.validateOverFlowAndNumberOfDigits();
        this.validateNumberOfFractionDigitsOfOverFlowValue();
        super.validate();
    }

    private void validateNumberOfFractionDigitsOfOverFlowValue() {
        if (this.overflowValue != null) {
            int scale = this.overflowValue.scale();
            if (scale > this.numberOfFractionDigits) {
                throw new OverFlowValueHasIncorrectFractionDigitsException(this.thesaurus, this.overflowValue, scale, this.numberOfFractionDigits);
            }
        }
    }

    /**
     * We need to validate the OverFlow value and the NumberOfDigits together
     */
    private void validateOverFlowAndNumberOfDigits() {
        if (this.overflowValue != null && this.numberOfDigits > 0) {
            if (this.overflowValue.compareTo(BigDecimal.valueOf(10).pow(numberOfDigits))==1) {
                throw new OverFlowValueCanNotExceedNumberOfDigitsException(this.thesaurus, this.overflowValue, Math.pow(10, this.numberOfDigits), this.numberOfDigits);
            }
            // should be covered by field validation
            //else if (this.overflowValue.compareTo(BigDecimal.ZERO) <= 0) {
             //   throw InvalidValueException.registerSpecOverFlowValueShouldBeLargerThanZero(this.thesaurus, this.overflowValue);
            //}
        }
    }

    @Override
    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
        this.multiplierMode = MultiplierMode.CONFIGURED_ON_OBJECT;
    }

    @Override
    public void setMultiplierMode(MultiplierMode multiplierMode) {
        this.multiplierMode = multiplierMode;
        if(!this.multiplierMode.equals(MultiplierMode.CONFIGURED_ON_OBJECT)){
            this.multiplier = BigDecimal.ONE;
        }
    }

    abstract static class AbstractBuilder implements Builder {

        private static final BigDecimal DEFAULT_MULTIPLIER = BigDecimal.ONE;
        private final NumericalRegisterSpecImpl registerSpec;

        AbstractBuilder(Provider<NumericalRegisterSpecImpl> registerSpecProvider, DeviceConfiguration deviceConfiguration, RegisterType registerType) {
            super();
            this.registerSpec = registerSpecProvider.get().initialize(deviceConfiguration, registerType);
            this.registerSpec.setMultiplier(DEFAULT_MULTIPLIER);
        }

        @Override
        public Builder setRegisterType(RegisterType registerType) {
            this.registerSpec.setRegisterType(registerType);
            return this;
        }

        @Override
        public Builder setOverruledObisCode(ObisCode overruledObisCode) {
            this.registerSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }

        @Override
        public Builder setNumberOfDigits(int numberOfDigits) {
            this.registerSpec.setNumberOfDigits(numberOfDigits);
            return this;
        }

        @Override
        public Builder setNumberOfFractionDigits(int numberOfFractionDigits) {
            this.registerSpec.setNumberOfFractionDigits(numberOfFractionDigits);
            return this;
        }

        @Override
        public Builder setOverflowValue(BigDecimal overflowValue) {
            this.registerSpec.setOverflowValue(overflowValue);
            return this;
        }

        @Override
        public Builder setMultiplier(BigDecimal multiplier) {
            this.registerSpec.setMultiplier(multiplier);
            return this;
        }

        @Override
        public Builder setMultiplierMode(MultiplierMode multiplierMode) {
            this.registerSpec.setMultiplierMode(multiplierMode);
            return this;
        }

        @Override
        public NumericalRegisterSpec add() {
            this.applyDefaultsIfApplicable();
            this.registerSpec.validateBeforeAddToConfiguration();
            return this.registerSpec;
        }

        private void applyDefaultsIfApplicable() {
            if (this.registerSpec.getMultiplier()==null) {
                registerSpec.setMultiplier(DEFAULT_MULTIPLIER);
            }
            if (this.registerSpec.getOverflowValue()==null && registerSpec.getNumberOfDigits()>0) {
                registerSpec.setOverflowValue(BigDecimal.TEN.pow(registerSpec.getNumberOfDigits()));
            }
        }
    }

    abstract static class AbstractUpdater implements Updater {

        private final NumericalRegisterSpec registerSpec;

        AbstractUpdater(NumericalRegisterSpec registerSpec) {
            super();
            this.registerSpec = registerSpec;
        }

        protected NumericalRegisterSpec updateTarget() {
            return this.registerSpec;
        }

        @Override
        public AbstractUpdater setOverruledObisCode(ObisCode overruledObisCode) {
            this.registerSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }
        @Override
        public Updater setNumberOfDigits(int numberOfDigits) {
            this.registerSpec.setNumberOfDigits(numberOfDigits);
            return this;
        }

        @Override
        public Updater setNumberOfFractionDigits(int numberOfFractionDigits) {
            this.registerSpec.setNumberOfFractionDigits(numberOfFractionDigits);
            return this;
        }

        @Override
        public Updater setOverflowValue(BigDecimal overflowValue) {
            this.registerSpec.setOverflowValue(overflowValue);
            return this;
        }

        @Override
        public Updater setMultiplier(BigDecimal multiplier) {
            this.registerSpec.setMultiplier(multiplier);
            return this;
        }

        @Override
        public Updater setMultiplierMode(MultiplierMode multiplierMode) {
            this.registerSpec.setMultiplierMode(multiplierMode);
            return this;
        }

        @Override
        public void update() {
            this.registerSpec.validateUpdate();
            this.registerSpec.save();
        }

    }

}

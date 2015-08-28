package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Range;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.masterdata.RegisterType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@ValidOverFlowAndNumberOfDigits(groups = {Save.Create.class, Save.Update.class})
@ValidNumericalRegisterSpec(groups = {Save.Update.class})
public class NumericalRegisterSpecImpl extends RegisterSpecImpl<NumericalRegisterSpec> implements NumericalRegisterSpec {

    @Range(min = 1, max = 20, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.REGISTER_SPEC_INVALID_NUMBER_OF_DIGITS + "}")
    private int numberOfDigits;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.REGISTER_SPEC_INVALID_NUMBER_OF_FRACTION_DIGITS + "}")
    @Range(min = 0, max = 6, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.REGISTER_SPEC_INVALID_NUMBER_OF_FRACTION_DIGITS + "}")
    private Integer numberOfFractionDigits;
    @Min(value = 1, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.REGISTER_SPEC_INVALID_OVERFLOW_VALUE + "}")
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.REGISTER_SPEC_OVERFLOW_IS_REQUIRED + "}")
    private BigDecimal overflow;

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

    protected boolean hasNumberOfFractionDigits() {
        return numberOfFractionDigits != null;
    }

    @Override
    public void setNumberOfFractionDigits(int numberOfFractionDigits) {
        this.numberOfFractionDigits = numberOfFractionDigits;
    }

    public BigDecimal getOverflowValue() {
        return overflow;
    }

    @Override
    public void setOverflowValue(BigDecimal overflowValue) {
        this.overflow = overflowValue;
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
        public NumericalRegisterSpec add() {
            this.applyDefaultsIfApplicable();
            this.registerSpec.validateBeforeAddToConfiguration();
            return this.registerSpec;
        }

        private void applyDefaultsIfApplicable() {
            if (this.registerSpec.getOverflowValue() == null && registerSpec.getNumberOfDigits() > 0) {
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
        public void update() {
            this.registerSpec.validateUpdate();
            this.registerSpec.save();
        }

    }

    @Override
    public RegisterSpec cloneForDeviceConfig(DeviceConfiguration deviceConfiguration) {
        Builder builder = deviceConfiguration.createNumericalRegisterSpec(getRegisterType());
        builder.setNumberOfDigits(getNumberOfDigits());
        builder.setNumberOfFractionDigits(getNumberOfFractionDigits());
        builder.setOverflowValue(getOverflowValue());
        builder.setOverruledObisCode(getObisCode().equals(getDeviceObisCode()) ? null : getDeviceObisCode());
        return builder.add();
    }
}

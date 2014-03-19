package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.InvalidValueException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.exceptions.OverFlowValueCanNotExceedNumberOfDigitsException;
import com.energyict.mdc.device.config.exceptions.OverFlowValueHasIncorrectFractionDigitsException;
import com.energyict.mdc.device.config.exceptions.RegisterMappingIsNotConfiguredOnDeviceTypeException;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import java.math.BigDecimal;
import java.util.Date;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.constraints.Min;

@ValidRegisterSpec(groups = { Save.Update.class })
public class RegisterSpecImpl extends PersistentIdObject<RegisterSpec> implements RegisterSpec {

    private final Reference<DeviceConfiguration> deviceConfig = ValueReference.absent();
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.REGISTER_SPEC_REGISTER_MAPPING_IS_REQUIRED_KEY + "}")
    private final Reference<RegisterMapping> registerMapping = ValueReference.absent(); static final String REGISTER_MAPPING = "registerMapping";
    @Min(value = 1, groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.REGISTER_SPEC_INVALID_NUMBER_OF_DIGITS + "}")
    private int numberOfDigits; static final String NUMBER_OF_DIGITS="numberOfDigits";
    private int numberOfFractionDigits; static final String NUMBER_OF_FRACTION_DIGITS="numberOfFractionDigits";
    private String overruledObisCodeString;
    private ObisCode overruledObisCode;
    private BigDecimal overflow;
    private BigDecimal multiplier = BigDecimal.ONE;
    private MultiplierMode multiplierMode = MultiplierMode.CONFIGURED_ON_OBJECT;

    private Date modificationDate;

    @Inject
    public RegisterSpecImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(RegisterSpec.class, dataModel, eventService, thesaurus);
    }

    private RegisterSpecImpl initialize(DeviceConfiguration deviceConfig, RegisterMapping registerMapping) {
        this.setDeviceConfig(deviceConfig);
        this.setRegisterMapping(registerMapping);
        return this;
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        return deviceConfig.get();
    }

    @Override
    public RegisterMapping getRegisterMapping() {
        return registerMapping.get();
    }

    @Override
    public ObisCode getObisCode() {
        return getRegisterMapping().getObisCode();
    }

    @Override
    public boolean isCumulative() {
        return getRegisterMapping().isCumulative();
    }

    @Override
    public Unit getUnit() {
        return getRegisterMapping().getUnit();
    }

    @Override
    public ObisCode getDeviceObisCode() {
        if (!Checks.is(this.overruledObisCodeString).empty()) {
            this.overruledObisCode = ObisCode.fromString(this.overruledObisCodeString);
            return overruledObisCode;
        }
        return getObisCode();
    }

    @Override
    public Date getModificationDate() {
        return modificationDate;
    }

    public int getNumberOfDigits() {
        return numberOfDigits;
    }

    public int getNumberOfFractionDigits() {
        return numberOfFractionDigits;
    }

    public BigDecimal getOverflowValue() {
        return overflow;
    }

    public MultiplierMode getMultiplierMode() {
        return multiplierMode;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    private void validateBeforeAddToConfiguration() {
        Save.CREATE.validate(this.dataModel.getValidatorFactory().getValidator(), this);
        this.validate();
    }

    private void validate() {
        validateOverFlowAndNumberOfDigits();
        validateNumberOfFractionDigitsOfOverFlowValue();
        validateDeviceTypeContainsRegisterMapping();
    }

    private void validateDeviceTypeContainsRegisterMapping() {
        DeviceType deviceType = getDeviceConfiguration().getDeviceType();
        boolean found = false;
        for (RegisterMapping mapping : deviceType.getRegisterMappings()) {
            if (mapping.getId()==getRegisterMapping().getId()) {
                found=true;
            }
        }

        if (!found) {
            throw new RegisterMappingIsNotConfiguredOnDeviceTypeException(this.thesaurus, getRegisterMapping());
        }
    }

    @Override
    protected void doDelete() {
        // TODO Check that the EISRTUREGISTERREADINGS and EISRTUREGISTERS get deleted via cascading ...
        this.getDeviceConfiguration().deleteRegisterSpec(this);
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.REGISTERSPEC;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.REGISTERSPEC;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.REGISTERSPEC;
    }

    @Override
    public void validateDelete() {
        // the configuration must validate the 'active' state
    }

    @Override
    public void validateUpdate() {
        Save.UPDATE.validate(this.dataModel.getValidatorFactory().getValidator(), this);
        this.validate();
    }

    public String toString() {
        return getDeviceConfiguration().getName() + " - " + getRegisterMapping().getName();
    }

    public void setDeviceConfig(DeviceConfiguration deviceConfig) {
        this.deviceConfig.set(deviceConfig);
    }

    @Override
    public void setRegisterMapping(RegisterMapping registerMapping) {
        this.registerMapping.set(registerMapping);
    }

    @Override
    public void setNumberOfDigits(int numberOfDigits) {
        this.numberOfDigits = numberOfDigits;
    }

    @Override
    public void setNumberOfFractionDigits(int numberOfFractionDigits) {
        this.numberOfFractionDigits = numberOfFractionDigits;
    }

    @Override
    public void setOverruledObisCode(ObisCode overruledObisCode) {
        this.overruledObisCode = overruledObisCode;
        this.overruledObisCodeString = overruledObisCode==null?null:overruledObisCode.toString();
    }

    @Override
    public void setOverflow(BigDecimal overflow) {
        this.overflow = overflow;
    }

    private void validateNumberOfFractionDigitsOfOverFlowValue() {
        if (this.overflow != null) {
            int scale = this.overflow.scale();
            if (scale > this.numberOfFractionDigits) {
                throw new OverFlowValueHasIncorrectFractionDigitsException(this.thesaurus, this.overflow, scale, this.numberOfFractionDigits);
            }
        }
    }

    /**
     * We need to validate the OverFlow value and the NumberOfDigits together
     */
    private void validateOverFlowAndNumberOfDigits() {
        if (this.overflow != null && this.numberOfDigits > 0) {
            if (!(this.overflow.intValue() <= Math.pow(10, this.numberOfDigits))) {
                throw new OverFlowValueCanNotExceedNumberOfDigitsException(this.thesaurus, this.overflow, Math.pow(10, this.numberOfDigits), this.numberOfDigits);
            } else if (this.overflow.intValue() <= 0) {
                throw InvalidValueException.registerSpecOverFlowValueShouldBeLargerThanZero(this.thesaurus, this.overflow);
            }
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

    public void setModDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    abstract static class RegisterSpecBuilder implements RegisterSpec.RegisterSpecBuilder {

        final RegisterSpecImpl registerSpec;

        RegisterSpecBuilder(Provider<RegisterSpecImpl> registerSpecProvider, DeviceConfiguration deviceConfiguration, RegisterMapping registerMapping) {
            registerSpec = registerSpecProvider.get().initialize(deviceConfiguration, registerMapping);
        }

        @Override
        public RegisterSpec.RegisterSpecBuilder setRegisterMapping(RegisterMapping registerMapping) {
            this.registerSpec.setRegisterMapping(registerMapping);
            return this;
        }

        @Override
        public RegisterSpec.RegisterSpecBuilder setNumberOfDigits(int numberOfDigits) {
            this.registerSpec.setNumberOfDigits(numberOfDigits);
            return this;
        }

        @Override
        public RegisterSpec.RegisterSpecBuilder setNumberOfFractionDigits(int numberOfFractionDigits) {
            this.registerSpec.setNumberOfFractionDigits(numberOfFractionDigits);
            return this;
        }

        @Override
        public RegisterSpec.RegisterSpecBuilder setOverruledObisCode(ObisCode overruledObisCode) {
            this.registerSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }

        @Override
        public RegisterSpec.RegisterSpecBuilder setOverflow(BigDecimal overflow) {
            this.registerSpec.setOverflow(overflow);
            return this;
        }

        @Override
        public RegisterSpec.RegisterSpecBuilder setMultiplier(BigDecimal multiplier) {
            this.registerSpec.setMultiplier(multiplier);
            return this;
        }

        @Override
        public RegisterSpec.RegisterSpecBuilder setMultiplierMode(MultiplierMode multiplierMode) {
            this.registerSpec.setMultiplierMode(multiplierMode);
            return this;
        }

        @Override
        public RegisterSpec add() {
            this.registerSpec.validateBeforeAddToConfiguration();
            return this.registerSpec;
        }
    }

    abstract static class RegisterSpecUpdater implements RegisterSpec.RegisterSpecUpdater {

        final RegisterSpec registerSpec;

        RegisterSpecUpdater(RegisterSpec registerSpec) {
            this.registerSpec = registerSpec;
        }

        @Override
        public RegisterSpec.RegisterSpecUpdater setRegisterMapping(RegisterMapping registerMapping) {
            this.registerSpec.setRegisterMapping(registerMapping);
            return this;
        }

        @Override
        public RegisterSpec.RegisterSpecUpdater setNumberOfDigits(int numberOfDigits) {
            this.registerSpec.setNumberOfDigits(numberOfDigits);
            return this;
        }

        @Override
        public RegisterSpec.RegisterSpecUpdater setNumberOfFractionDigits(int numberOfFractionDigits) {
            this.registerSpec.setNumberOfFractionDigits(numberOfFractionDigits);
            return this;
        }

        @Override
        public RegisterSpec.RegisterSpecUpdater setOverruledObisCode(ObisCode overruledObisCode) {
            this.registerSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }

        @Override
        public RegisterSpec.RegisterSpecUpdater setOverflow(BigDecimal overflow) {
            this.registerSpec.setOverflow(overflow);
            return this;
        }

        @Override
        public RegisterSpec.RegisterSpecUpdater setMultiplier(BigDecimal multiplier) {
            this.registerSpec.setMultiplier(multiplier);
            return this;
        }

        @Override
        public RegisterSpec.RegisterSpecUpdater setMultiplierMode(MultiplierMode multiplierMode) {
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

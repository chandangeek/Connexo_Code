package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.ChannelSpecLinkType;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.DeviceConfigIsRequiredException;
import com.energyict.mdc.device.config.exceptions.DeviceConfigurationIsActiveException;
import com.energyict.mdc.device.config.exceptions.DuplicatePrimeRegisterSpecException;
import com.energyict.mdc.device.config.exceptions.InCorrectDeviceConfigOfChannelSpecException;
import com.energyict.mdc.device.config.exceptions.InvalidValueException;
import com.energyict.mdc.device.config.exceptions.OverFlowValueCanNotExceedNumberOfDigitsException;
import com.energyict.mdc.device.config.exceptions.OverFlowValueHasIncorrectFractionDigitsException;
import com.energyict.mdc.device.config.exceptions.RegisterMappingIsRequiredException;
import com.energyict.mdc.protocol.api.device.MultiplierMode;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

public class RegisterSpecImpl extends PersistentIdObject<RegisterSpec> implements RegisterSpec {

    private DeviceConfiguration deviceConfig;
    private RegisterMapping registerMapping;
    private ChannelSpec linkedChannelSpec;
    private int numberOfDigits;
    private int numberOfFractionDigits;
    private String overruledObisCodeString;
    private ObisCode overruledObisCode;
    private BigDecimal overflow;
    private BigDecimal multiplier;
    private MultiplierMode multiplierMode;
    private ChannelSpecLinkType channelSpecLinkType;

    private Date modificationDate;
    private Clock clock;

    @Inject
    public RegisterSpecImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock) {
        super(RegisterSpec.class, dataModel, eventService, thesaurus);
        this.clock = clock;
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        return deviceConfig;
    }

    @Override
    public RegisterMapping getRegisterMapping() {
        return registerMapping;
    }

    @Override
    public ObisCode getObisCode() {
        return getRegisterMapping().getObisCode();
    }

    @Override
    public ChannelSpec getLinkedChannelSpec() {
        return linkedChannelSpec;
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
        if (!"".equals(this.overruledObisCodeString) && this.overruledObisCodeString != null) {
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

    @Override
    public void save() {
        this.modificationDate = this.clock.now();
        validateDependentAttributes();
        super.save();
    }

    private void validateDependentAttributes() {
        validateLinkedChannelSpec();
        validateOverFlowAndNumberOfDigits();
        validateNumberOfFractionDigitsOfOverFlowValue();
    }

    @Override
    protected void doDelete() {
        // TODO Check that the EISRTUREGISTERREADINGS and EISRTUREGISTERS get deleted via cascading ...
        this.getDataMapper().remove(this);
    }

    @Override
    protected void validateDelete() {
        // we should rely on the 'Activation' state of the Configuration. If the Configuration is not active, then we must be able to delete them.
        if(getDeviceConfiguration().getActive()){
            throw DeviceConfigurationIsActiveException.canNotDeleteRegisterSpec(this.thesaurus);
        }
    }

    @Override
    protected void postNew() {
        this.getDataMapper().update(this);
    }

    @Override
    protected void post() {
        this.getDataMapper().persist(this);
    }

//    protected void deleteDependents() throws SQLException, BusinessException {
//        super.deleteDependents();
//        StringBuffer buffer = new StringBuffer("delete FROM eisrturegisterreading WHERE ");
//        buffer.append("rturegisterid IN ");
//        buffer.append("(select id FROM eisrturegister WHERE rturegspecid = ?)");
//        bindAndExecute(buffer.toString(), getId());
//        buffer = new StringBuffer("delete from eisrturegister where rturegspecid = ?");
//        bindAndExecute(buffer.toString(), getId());
//    }

    public String toString() {
        return getDeviceConfiguration().getName() + " - " + getRegisterMapping().getName();
    }
//
//    @Override
//    public Command<RegisterSpec> createConstructor() {
//        return new RtuRegisterSpecCommand(this);
//    }


    @Override
    public void setDeviceConfig(DeviceConfiguration deviceConfig) {
        validateDeviceConfiguration(deviceConfig);
        this.deviceConfig = deviceConfig;
    }

    private void validateDeviceConfiguration(DeviceConfiguration deviceConfig) {
        if (deviceConfig == null) {
            throw DeviceConfigIsRequiredException.registerSpecRequiresDeviceConfig(this.thesaurus);
        }
    }

    @Override
    public void setRegisterMapping(RegisterMapping registerMapping) {
        validateRegisterMapping(registerMapping);
        this.registerMapping = registerMapping;
    }

    private void validateRegisterMapping(RegisterMapping registerMapping) {
        if (registerMapping == null) {
            throw RegisterMappingIsRequiredException.registerSpecRequiresRegisterMapping(this.thesaurus);
        }
    }

    @Override
    public void setLinkedChannelSpec(ChannelSpec linkedChannelSpec) {
        this.linkedChannelSpec = linkedChannelSpec;
    }

    private void validateLinkedChannelSpec() {
        if (this.linkedChannelSpec != null) {
            if (getDeviceConfiguration() != null && !linkedChannelSpec.getDeviceConfig().equals(getDeviceConfiguration())) {
                throw new InCorrectDeviceConfigOfChannelSpecException(this.thesaurus, linkedChannelSpec, linkedChannelSpec.getDeviceConfig(), getDeviceConfiguration());
            }
        }
    }

    @Override
    public void setNumberOfDigits(int numberOfDigits) {
        validateNumberOfDigits(numberOfDigits);
        this.numberOfDigits = numberOfDigits;
    }

    private void validateNumberOfDigits(int numberOfDigits) {
        if (numberOfDigits < 1) {
            throw InvalidValueException.registerSpecNumberOfDigitsShouldBeLargerThanOne(this.thesaurus);
        }
    }

    @Override
    public void setNumberOfFractionDigits(int numberOfFractionDigits) {
        this.numberOfFractionDigits = numberOfFractionDigits;
    }

    @Override
    public void setOverruledObisCode(ObisCode overruledObisCode) {
        this.overruledObisCodeString = overruledObisCode.toString();
        this.overruledObisCode = null;
    }

    @Override
    public void setOverflow(BigDecimal overflow) {
        this.overflow = overflow;
    }

    private void validateNumberOfFractionDigitsOfOverFlowValue() {
        if (this.overflow != null) {
            int scale = this.overflow.scale();
            if (scale > this.numberOfFractionDigits) {
                throw new OverFlowValueHasIncorrectFractionDigitsException(this.thesaurus, this.overflow, scale,this. numberOfFractionDigits);
            }
        }
    }

    /**
     * We need to validate the OverFlow value and the NumberOfDigits together
     */
    private void validateOverFlowAndNumberOfDigits() {
        if (this.overflow != null && this.numberOfDigits > 0) {
            if (!(this.overflow.intValue() <= Math.pow(10, this.numberOfDigits))) {
                DecimalFormat df = Environment.DEFAULT.get().getFormatPreferences().getNumberFormat(this.numberOfDigits, true);
                throw new OverFlowValueCanNotExceedNumberOfDigitsException(this.thesaurus, this.overflow, df.format(Math.pow(10, this.numberOfDigits)), this.numberOfDigits);
            } else if (this.overflow.intValue() <= 0) {
                throw InvalidValueException.registerSpecOverFlowValueShouldBeLargerThanZero(this.thesaurus, this.overflow);
            }
        }
    }

    @Override
    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public void setMultiplierMode(MultiplierMode multiplierMode) {
        this.multiplierMode = multiplierMode;
    }

    public void setModDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    @Override
    public ChannelSpecLinkType getChannelSpecLinkType() {
        return channelSpecLinkType;
    }

    @Override
    public void setChannelSpecLinkType(ChannelSpecLinkType channelSpecLinkType) {
        validateChannelSpecLinkType(channelSpecLinkType, getLinkedChannelSpec());
        this.channelSpecLinkType = channelSpecLinkType;
    }

    private void validateChannelSpecLinkType(ChannelSpecLinkType channelSpecLinkType, ChannelSpec channelSpec) {
        if (channelSpec != null && channelSpecLinkType != null) {
            List<RegisterSpec> registerSpecs = this.mapper(RegisterSpec.class).find("channelspecid", channelSpec.getId(), "class", ChannelSpecLinkType.PRIME);
            RegisterSpec currentPrimeRegisterSpec = registerSpecs.get(0);
            if (currentPrimeRegisterSpec != null && channelSpecLinkType == ChannelSpecLinkType.PRIME) {
                throw new DuplicatePrimeRegisterSpecException(this.thesaurus, channelSpec, currentPrimeRegisterSpec);
            }
        }
    }
}

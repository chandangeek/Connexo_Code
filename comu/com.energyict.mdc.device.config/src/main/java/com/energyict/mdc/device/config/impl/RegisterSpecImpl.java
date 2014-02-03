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

    private DataModel dataModel;
    private EventService eventService;
    private Thesaurus thesaurus;
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
        super.save();
    }

    @Override
    protected void doDelete() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void validateDelete() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void postNew() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void post() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void validateNew(RegisterSpecShadow shadow) throws BusinessException {

    }

    protected void validateUpdate(RegisterSpecShadow shadow) throws BusinessException {
        validate(shadow);
        int linkedChannelSpecId = shadow.getLinkedChannelSpecId() == 0 && shadow.getLinkedChannelSpecShadow() != null ? shadow.getLinkedChannelSpecShadow().getId() : shadow.getLinkedChannelSpecId();
        if (linkedChannelSpecId > 0) {
            RegisterSpec currentPrimeRegisterSpec = RegisterSpecFactoryProvider.instance.get().getRegisterSpecFactory().findByChannelSpecAndType(linkedChannelSpecId, ChannelSpecLinkType.PRIME);
            if (currentPrimeRegisterSpec != null && currentPrimeRegisterSpec.getId() != shadow.getId() && shadow.getLinkedChannelSpecType() == ChannelSpecLinkType.PRIME) {
                throw new BusinessException("duplicatePrimeRegisterSpecForChannelSpec", "Linked channel spec (id={0,number}) already has a PRIME register spec (id={1,number})", shadow.getLinkedChannelSpecId(), currentPrimeRegisterSpec.getId());
            }
        }
    }

    protected void deleteDependents() throws SQLException, BusinessException {
        super.deleteDependents();
        StringBuffer buffer = new StringBuffer("delete FROM eisrturegisterreading WHERE ");
        buffer.append("rturegisterid IN ");
        buffer.append("(select id FROM eisrturegister WHERE rturegspecid = ?)");
        bindAndExecute(buffer.toString(), getId());
        buffer = new StringBuffer("delete from eisrturegister where rturegspecid = ?");
        bindAndExecute(buffer.toString(), getId());
    }

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
        validateLinkedChannelSpec(linkedChannelSpec);
        this.linkedChannelSpec = linkedChannelSpec;
    }

    private void validateLinkedChannelSpec(ChannelSpec linkedChannelSpec) {
        if (linkedChannelSpec != null) {
            if (getDeviceConfiguration() != null && !linkedChannelSpec.getDeviceConfig().equals(getDeviceConfiguration())) {
                throw new InCorrectDeviceConfigOfChannelSpecException(this.thesaurus, linkedChannelSpec, linkedChannelSpec.getDeviceConfig(), getDeviceConfiguration());
            }
        }
    }

    @Override
    public void setNumberOfDigits(int numberOfDigits) {
        validateNumberOfDigits(numberOfDigits);
        validateOverFlowAndNumberOfDigits(getOverflowValue(), numberOfDigits);
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
        this.overruledObisCode = overruledObisCode;
    }

    @Override
    public void setOverflow(BigDecimal overflow) {
        validateOverFlowAndNumberOfDigits(overflow, getNumberOfDigits());
        validateNumberOfFractionDigitsOfOverFlowValue(overflow, getNumberOfFractionDigits());
        this.overflow = overflow;
    }

    private void validateNumberOfFractionDigitsOfOverFlowValue(BigDecimal overflow, int numberOfFractionDigits) {
        if (overflow != null) {
            int scale = overflow.scale();
            DecimalFormat format = Environment.DEFAULT.get().getFormatPreferences().getNumberFormat(scale, true);
            if (scale > numberOfFractionDigits) {
                throw new OverFlowValueHasIncorrectFractionDigitsException(this.thesaurus, overflow, scale, numberOfFractionDigits);
            }
        }
    }

    /**
     * We need to validate the OverFlow value and the NumberOfDigits together
     *
     * @param overflow       the OverFlow value
     * @param numberOfDigits the Number of digits of this RegisterSpec
     */
    private void validateOverFlowAndNumberOfDigits(BigDecimal overflow, int numberOfDigits) {
        if (overflow != null && numberOfDigits > 0) {
            if (!(overflow.intValue() <= Math.pow(10, numberOfDigits))) {
                DecimalFormat df = Environment.DEFAULT.get().getFormatPreferences().getNumberFormat(numberOfDigits, true);
                throw new OverFlowValueCanNotExceedNumberOfDigitsException(this.thesaurus, overflow, df.format(Math.pow(10, numberOfDigits)), numberOfDigits);
            } else if (overflow.intValue() <= 0) {
                throw InvalidValueException.registerSpecOverFlowValueShouldBeLargerThanZero(this.thesaurus, overflow);
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
            RegisterSpec currentPrimeRegisterSpec = RegisterSpecFactoryProvider.instance.get().getRegisterSpecFactory().findByChannelSpecAndType(linkedChannelSpecId, ChannelSpecLinkType.PRIME);
            if (currentPrimeRegisterSpec != null && shadow.getLinkedChannelSpecType() == ChannelSpecLinkType.PRIME) {
                throw new BusinessException("duplicatePrimeRegisterSpecForChannelSpec", "Linked channel spec (id={0,number}) already has a PRIME register spec (id={1,number})", shadow.getLinkedChannelSpecId(), currentPrimeRegisterSpec.getId());
            }
        }
    }
}

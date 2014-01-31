package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.RegisterConfiguration;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.InvalidValueException;
import com.energyict.mdc.protocol.api.device.MultiplierMode;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;

public class RegisterSpecImpl implements RegisterSpec {

    private long id;
    private DeviceConfiguration deviceConfig;
    private RegisterMapping registerMapping;
    private ChannelSpec linkedChannelSpec = null;
    private int numberOfDigits;
    private int numberOfFractionDigits;
    private String overruledObisCodeString;
    private ObisCode overruledObisCode;
    private BigDecimal overflow;
    private BigDecimal multiplier;
    private MultiplierMode multiplierMode;

    private Date modificationDate;

    private DataModel dataModel;
    private EventService eventService;
    private Thesaurus thesaurus;
    private Clock clock;

    @Inject
    public RegisterSpecImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
        this.clock = clock;
    }

    @Override
    public long getId() {
        return this.id;
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

    @Override
    public void save() {
        this.modificationDate = this.clock.now();
        if (this.id > 0) {
            this.post();
        } else {

        }
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Saves this object for the first time.
     */
    protected void postNew() {
        this.getDataMapper().persist(this);
    }

    /**
     * Updates the changes made to this object.
     */
    protected void post() {
        this.getDataMapper().update(this);
    }

    private DataMapper<RegisterMapping> getDataMapper() {
        return this.dataModel.mapper(RegisterMapping.class);
    }

    @Override
    public void delete() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void validateNew(RegisterSpecShadow shadow) throws BusinessException {
        validate(shadow);
        int linkedChannelSpecId = shadow.getLinkedChannelSpecId()==0 && shadow.getLinkedChannelSpecShadow()!=null ? shadow.getLinkedChannelSpecShadow().getId() : shadow.getLinkedChannelSpecId();
        if (linkedChannelSpecId>0) {
            RegisterSpec currentPrimeRegisterSpec = RegisterSpecFactoryProvider.instance.get().getRegisterSpecFactory().findByChannelSpecAndType(linkedChannelSpecId, ChannelSpecLinkType.PRIME);
            if (currentPrimeRegisterSpec!=null && shadow.getLinkedChannelSpecType()==ChannelSpecLinkType.PRIME) {
                throw new BusinessException("duplicatePrimeRegisterSpecForChannelSpec", "Linked channel spec (id={0,number}) already has a PRIME register spec (id={1,number})", shadow.getLinkedChannelSpecId(), currentPrimeRegisterSpec.getId());
            }
        }
    }

    protected void checkDigitsOverflowValue(RegisterSpecShadow shadow) throws BusinessException {
        BigDecimal overflowValue = shadow.getOverflowValue();
        if (overflowValue != null) {
            int numberOfFractionDigits = shadow.getNumberOfFractionDigits();
            int scale = overflowValue.scale();
            DecimalFormat format = Environment.DEFAULT.get().getFormatPreferences().getNumberFormat(scale, true);
            if (scale > numberOfFractionDigits) {
                throw new BusinessException("overflowValueXHasInvalidNumberOfFractionDigitsY",
                        "Overflow value '{0}' has too much fraction digits, only {1} allowed", format.format(overflowValue), numberOfFractionDigits);
            }
        }
    }

    @SuppressWarnings(value = {"unchecked"})
    protected void validate(RegisterSpecShadow shadow) throws BusinessException {
        DeviceConfiguration deviceConfiguration = getDeviceConfigFactory().find(shadow.getDeviceConfigId());

        if (getRtuRegisterMappingFactory().find(shadow.getRegisterMappingId()) == null) {
            throw new BusinessException("invalidRegisterMappingIdX", "Invalid device register mapping (id={0,number})", shadow.getRegisterMappingId());
        }
        int linkedChannelSpecId = shadow.getLinkedChannelSpecId()==0 && shadow.getLinkedChannelSpecShadow()!=null ? shadow.getLinkedChannelSpecShadow().getId() : shadow.getLinkedChannelSpecId();
        if (linkedChannelSpecId>0) {
            ChannelSpec spec = getChannelSpecFactory().find(shadow.getLinkedChannelSpecId());
            if (spec == null) {
                throw new BusinessException("invalidLinkedChannelSpecNotExisting", "Non existing linked channel spec (id={0,number})", shadow.getLinkedChannelSpecId());
            } else {
                if (!deviceConfiguration.equals(spec.getDeviceConfig())) {
                    throw new BusinessException("invalidLinkedChannelSpecOtherConfig", "Linked channel spec (id={0,number}) is of different device configuration", shadow.getLinkedChannelSpecId());
                }
            }
            if (shadow.getLinkedChannelSpecType()==null) {
                throw new BusinessException("linkedChannelSpecTypeCannotBeNull", "Linked channel spec type cannot be null if linked channel spec is specified");
            }
        } else {
            if (shadow.getLinkedChannelSpecType()!=null) {
                throw new BusinessException("linkedChannelSpecTypeCannotBeSpecified", "Linked channel spec type cannot be specified if linked channel spec is not specified");
            }
        }

        validateOverflow(shadow);
        checkDigitsOverflowValue(shadow);
    }

    private DeviceConfigurationFactory getDeviceConfigFactory() {
        return DeviceConfigurationFactoryProvider.instance.get().getDeviceConfigurationFactory();
    }

    private RegisterMappingFactory getRtuRegisterMappingFactory() {
        return RegisterMappingFactoryProvider.instance.get().getRegisterMappingFactory();
    }

    private ChannelSpecFactory getChannelSpecFactory() {
        return ChannelSpecFactoryProvider.instance.get().getChannelSpecFactory();
    }

    private void validateOverflow(RegisterSpecShadow shadow) throws BusinessException {
        if (shadow.getOverflowValue() == null) {
            return;
        }
        double overflow = shadow.getOverflowValue().doubleValue();
        int numberOfDigits = shadow.getNumberOfDigits();
        if (!(overflow <= Math.pow(10, numberOfDigits))) {
            DecimalFormat df = Environment.DEFAULT.get().getFormatPreferences().getNumberFormat(shadow.getNumberOfFractionDigits(), true);
            throw new BusinessException("overflowValueCannotExceedX",
                    "The overflow value cannot exceed {0}", df.format(Math.pow(10, shadow.getNumberOfDigits())));
        } else if (overflow <= 0.0) {
            throw new BusinessException("XcannotBeEqualOrLessThanZero", "{0} should have a value greater than 0",
                    UserEnvironment.getDefault().getTranslation("overflowValue"));
        }
    }

    protected void validateUpdate(RegisterSpecShadow shadow) throws BusinessException {
        validate(shadow);
        int linkedChannelSpecId = shadow.getLinkedChannelSpecId()==0 && shadow.getLinkedChannelSpecShadow()!=null ? shadow.getLinkedChannelSpecShadow().getId() : shadow.getLinkedChannelSpecId();
        if (linkedChannelSpecId>0) {
            RegisterSpec currentPrimeRegisterSpec = RegisterSpecFactoryProvider.instance.get().getRegisterSpecFactory().findByChannelSpecAndType(linkedChannelSpecId, ChannelSpecLinkType.PRIME);
            if (currentPrimeRegisterSpec!=null && currentPrimeRegisterSpec.getId()!=shadow.getId() && shadow.getLinkedChannelSpecType()==ChannelSpecLinkType.PRIME) {
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
        if (deviceConfig == null) {
            throw new BusinessException("invalidDeviceConfigurationIdX", "Invalid device configuration (id={0,number})", shadow.getDeviceConfigId());
        }
        this.deviceConfig = deviceConfig;
    }

    @Override
    public void setRegisterMapping(RegisterMapping registerMapping) {
        this.registerMapping = registerMapping;
    }

    @Override
    public void setLinkedChannelSpec(ChannelSpec linkedChannelSpec) {
        this.linkedChannelSpec = linkedChannelSpec;
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
        this.overruledObisCode = overruledObisCode;
    }

    @Override
    public void setOverflow(BigDecimal overflow) {
        this.overflow = overflow;
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
    public RegisterConfiguration getRegisterConfiguration() {
        return new RegisterConfiguration() {
            @Override
            public int getNumberOfDigits() {
                return numberOfDigits;
            }

            @Override
            public int getNumberOfFractionDigits() {
                return numberOfFractionDigits;
            }

            @Override
            public BigDecimal getMultiplier() {
                return multiplier;
            }

            @Override
            public MultiplierMode getMultiplierMode() {
                return multiplierMode;
            }

            @Override
            public BigDecimal getOverflowValue() {
                return overflow;
            }

            @Override
            public boolean isMultiplierModeOverruled() {
                return false;
            }

            @Override
            public boolean isMultiplierOverruled() {
                return false;
            }

            @Override
            public boolean isNumberOfDigitsOverruled() {
                return false;
            }

            @Override
            public boolean isNumberOfFractionDigitsOverruled() {
                return false;
            }

            @Override
            public boolean isOverflowOverruled() {
                return false;
            }
        };
    }
}

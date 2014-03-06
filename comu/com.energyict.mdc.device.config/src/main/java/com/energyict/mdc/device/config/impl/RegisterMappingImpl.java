package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterGroup;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenRegisterMappingIsInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateProductSpecWhenRegisterMappingIsInUseException;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.DuplicateObisCodeException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import static com.elster.jupiter.util.Checks.is;

public class RegisterMappingImpl extends PersistentNamedObject<RegisterMapping> implements RegisterMapping {

    private final DeviceConfigurationService deviceConfigurationService;
    private final MeteringService meteringService;
    private final MdcReadingTypeUtilService mdcReadingTypeUtilService;

    private ObisCode obisCode;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.REGISTER_MAPPING_OBIS_CODE_IS_REQUIRED_KEY + "}")
    private String obisCodeString;
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.PRODUCT_SPEC_IS_REQUIRED_KEY + "}")
    private Reference<ProductSpec> productSpec = ValueReference.absent();
    private boolean cumulative;
    private Reference<RegisterGroup> registerGroup = ValueReference.absent();
    private String description;
    private Date modificationDate;

    private Clock clock;

    @Inject
    public RegisterMappingImpl(DataModel dataModel, EventService eventService, DeviceConfigurationService deviceConfigurationService, Thesaurus thesaurus, Clock clock, MeteringService meteringService, MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        super(RegisterMapping.class, dataModel, eventService, thesaurus);
        this.clock = clock;
        this.deviceConfigurationService = deviceConfigurationService;
        this.meteringService = meteringService;
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
    }

    RegisterMappingImpl initialize(String name, ObisCode obisCode, ProductSpec productSpec) {
        this.setName(name);
        this.setObisCode(obisCode);
        this.setProductSpec(productSpec);
        return this;
    }

    static RegisterMappingImpl from (DataModel dataModel, String name, ObisCode obisCode, ProductSpec productSpec) {
        return dataModel.getInstance(RegisterMappingImpl.class).initialize(name, obisCode, productSpec);
    }

    @Override
    public void save () {
        validateUniqueObisCodeAndRegisterMapping();
        this.modificationDate = this.clock.now();
        super.save();
    }

    private void validateUniqueObisCodeAndRegisterMapping() {
        if (this.productSpec.isPresent() && this.obisCode != null) {
            RegisterMapping otherRegisterMapping = this.findOtherByObisCodeAndProductSpec();
            if (otherRegisterMapping != null) {
                throw DuplicateObisCodeException.forRegisterMapping(this.getThesaurus(), obisCode, otherRegisterMapping);
            }
        }
    }

    protected void post() {
        this.validateDeviceConfigurations();
        super.post();
    }

    private void validateDeviceConfigurations() {
        List<DeviceConfiguration> deviceConfigurations = this.deviceConfigurationService.findDeviceConfigurationsUsingRegisterMapping(this);
        for (DeviceConfiguration each : deviceConfigurations) {
            ServerDeviceConfiguration deviceConfiguration = (ServerDeviceConfiguration) each;
            deviceConfiguration.validateUpdateRegisterMapping(this);
        }
    }

    @Override
    protected void doDelete() {
        this.getDataMapper().remove(this);
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.REGISTERMAPPING;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.REGISTERMAPPING;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.REGISTERMAPPING;
    }

    @Override
    protected DuplicateNameException duplicateNameException(Thesaurus thesaurus, String name) {
        return DuplicateNameException.registerMappingAlreadyExists(this.getThesaurus(), name);
    }

    public ObisCode getObisCode() {
        if (this.obisCode == null && !is(this.obisCodeString).empty()) {
            this.obisCode = ObisCode.fromString(this.obisCodeString);
        }
        return this.obisCode;
    }

    @Override
    public void setObisCode(ObisCode obisCode) {
        if (obisCode == null) {
            // javax.validation will throw ConstraintValidationException in the end
            this.obisCodeString = null;
            this.obisCode = null;
        }
        else if (this.obisCodeChanged(obisCode)) {
            if (this.isInUse()) {
                throw new CannotUpdateObisCodeWhenRegisterMappingIsInUseException(this.getThesaurus(), this);
            }
            this.obisCodeString = obisCode.toString();
            this.obisCode = obisCode;
        }
    }

    private boolean obisCodeChanged(ObisCode obisCode) {
        return !is(this.obisCode).equalTo(obisCode);
    }

    private RegisterMapping findOtherByObisCodeAndProductSpec() {
        RegisterMapping registerMapping = this.getDataMapper().getUnique("obisCodeString", obisCode.toString(), "productSpec", this.getProductSpec()).orNull();
        if (registerMapping != null && this.getId() > 0 && registerMapping.getId() == this.getId()) {
            // The RegisterMapping that was found is the one we are updating so ignore it
            return null;
        }
        else {
            // No other RegisterMapping found or this RegisterMapping is not peristent yet or the other is really different because the id does not match
            return registerMapping;
        }
    }

    @Override
    public RegisterGroup getRegisterGroup() {
        return this.registerGroup.orNull();
    }

    @Override
    public void setRegisterGroup(RegisterGroup registerGroup) {
        this.registerGroup.set(registerGroup);
    }

    @Override
    public ProductSpec getProductSpec() {
        return this.productSpec.get();
    }

    @Override
    public void setProductSpec(ProductSpec productSpec) {
        if (productSpec == null) {
            // javax.validation will throw ConstraintValidationException in the end
            this.productSpec.set(null);
        }
        else if (this.productSpecChanged(productSpec)) {
            if (this.isInUse()) {
                throw new CannotUpdateProductSpecWhenRegisterMappingIsInUseException(this.getThesaurus(), this);
            }
            this.productSpec.set(productSpec);
        }
    }

    private boolean productSpecChanged(ProductSpec productSpec) {
        return ((!this.productSpec.isPresent() && productSpec != null)
            || (productSpec != null && (this.getProductSpec().getId() != productSpec.getId())));
    }

    @Override
    public ReadingType getReadingType() {
        return getProductSpec().getReadingType();
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String newDescription) {
        this.description = newDescription;
    }

    protected void validateDelete() {
        this.validateNotUsedByRegisterSpecs();
        this.validateNotUsedByChannelSpecs();
        this.validateNotUsedByLoadProfileTypes();
        this.validateNotUsedByDeviceTypes();
    }

    private void validateNotUsedByRegisterSpecs() {
        List<RegisterSpec> registerSpecs = this.mapper(RegisterSpec.class).find("registerMapping", this);
        if (!registerSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUseByRegisterSpecs(this.getThesaurus(), this, registerSpecs);
        }
    }

    private void validateNotUsedByChannelSpecs() {
        List<ChannelSpec> channelSpecs = this.mapper(ChannelSpec.class).find("registerMapping", this);
        if (!channelSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUseByChannelSpecs(this.getThesaurus(), this, channelSpecs);
        }
    }

    private void validateNotUsedByLoadProfileTypes() {
        List<LoadProfileTypeRegisterMappingUsage> loadProfileTypeUsages = this.mapper(LoadProfileTypeRegisterMappingUsage.class).find("registerMapping", this);
        if (!loadProfileTypeUsages.isEmpty()) {
            Set<LoadProfileType> loadProfileTypes = new HashSet<>();
            for (LoadProfileTypeRegisterMappingUsage loadProfileTypeUsage : loadProfileTypeUsages) {
                loadProfileTypes.add(loadProfileTypeUsage.getLoadProfileType());
            }
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUseByLoadprofileTypes(this.getThesaurus(), this, new ArrayList<>(loadProfileTypes));
        }
    }

    private void validateNotUsedByDeviceTypes() {
        List<DeviceTypeRegisterMappingUsage> deviceTypeUsages = this.mapper(DeviceTypeRegisterMappingUsage.class).find("registerMapping", this);
        if (!deviceTypeUsages.isEmpty()) {
            Set<DeviceType> deviceTypes = new HashSet<>();
            for (DeviceTypeRegisterMappingUsage deviceTypeUsage : deviceTypeUsages) {
                deviceTypes.add(deviceTypeUsage.getDeviceType());
            }
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUseByDeviceTypes(this.getThesaurus(), this, new ArrayList<>(deviceTypes));
        }
    }

    @Override
    public boolean isInUse() {
        return this.usedByChannelSpecs() || this.usedByRegisterSpecs();

    }

    private boolean usedByChannelSpecs() {
        List<ChannelSpec> channelSpecs = this.mapper(ChannelSpec.class).find("registerMapping", this);
        return !channelSpecs.isEmpty();
    }

    private boolean usedByRegisterSpecs() {
        List<RegisterSpec> registerSpecs = this.mapper(RegisterSpec.class).find("registerMapping", this);
        return !registerSpecs.isEmpty();
    }

    @Override
    public boolean isCumulative() {
        return this.cumulative;
    }

    @Override
    public void setCumulative(boolean cumulative) {
        this.cumulative = cumulative;
    }

    public Unit getUnit() {
        return getProductSpec().getUnit();
    }

    public Date getModificationDate() {
        return this.modificationDate;
    }

}
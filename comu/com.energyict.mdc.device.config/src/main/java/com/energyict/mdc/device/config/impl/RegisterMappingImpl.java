package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterGroup;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenRegisterMappingIsInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateProductSpecWhenRegisterMappingIsInUseException;
import com.energyict.mdc.device.config.exceptions.DuplicateObisCodeException;
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;
import com.energyict.mdc.device.config.exceptions.ObisCodeIsRequiredException;
import com.energyict.mdc.device.config.exceptions.ProductSpecIsRequiredException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegisterMappingImpl extends PersistentNamedObject<RegisterMapping> implements RegisterMapping {

    private String obisCodeString;
    private ObisCode obisCode;
    private ProductSpec productSpec;
    private boolean cumulative;
    private RegisterGroup registerGroup;
    private String description;
    private Date modificationDate;

    private Clock clock;

    @Inject
    public RegisterMappingImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock) {
        super(RegisterMapping.class, dataModel, eventService, thesaurus);
        this.clock = clock;
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
        this.modificationDate = this.clock.now();
        super.save();
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
        this.validateDeviceConfigurations();
        this.getDataMapper().update(this);
    }

    private void validateDeviceConfigurations() {
        /* Todo: find all DeviceConfigurations that use this mapping via ChannelSpec or RegisterSpec
         *       and validate that the changes applied to this RegisterMapping
         *       do not violate any device configuration business constraints. */
    }

    @Override
    protected void doDelete() {
        this.getDataMapper().remove(this);
    }

    @Override
    protected NameIsRequiredException nameIsRequiredException(Thesaurus thesaurus) {
        return NameIsRequiredException.registerMappingNameIsRequired(thesaurus);
    }

    public ObisCode getObisCode() {
        if (this.obisCode == null) {
            this.obisCode = ObisCode.fromString(this.obisCodeString);
        }
        return this.obisCode;
    }

    @Override
    public void setObisCode(ObisCode obisCode) {
        if (obisCode == null) {
            throw ObisCodeIsRequiredException.registerMappingRequiresObisCode(this.getThesaurus());
        }
        if (!this.getObisCode().equals(obisCode)) {
            if (this.isInUse()) {
                throw new CannotUpdateObisCodeWhenRegisterMappingIsInUseException(this.getThesaurus(), this);
            }
            RegisterMapping otherRegisterMapping = this.findOtherByObisCode(obisCode);
            if (otherRegisterMapping != null) {
                throw new DuplicateObisCodeException(this.getThesaurus(), obisCode, otherRegisterMapping);
            }
            this.obisCodeString = obisCode.toString();
            this.obisCode = obisCode;
        }
    }

    private RegisterMapping findOtherByObisCode(ObisCode obisCode) {
        return this.getDataMapper().getUnique("obisCodeString", obisCode.toString()).orNull();
    }

    @Override
    public RegisterGroup getRegisterGroup() {
        return this.registerGroup;
    }

    @Override
    public void setRegisterGroup(RegisterGroup registerGroup) {
        this.registerGroup = registerGroup;
    }

    @Override
    public ProductSpec getProductSpec() {
        return this.productSpec;
    }

    @Override
    public void setProductSpec(ProductSpec productSpec) {
        if (productSpec == null) {
            throw new ProductSpecIsRequiredException(this.getThesaurus());
        }
        if (this.productSpec.getId() != productSpec.getId()) {
            if (this.isInUse()) {
                throw new CannotUpdateProductSpecWhenRegisterMappingIsInUseException(this.getThesaurus(), this);
            }
            this.productSpec = productSpec;
        }
    }

    @Override
    public ReadingType getReadingType() {
        // Todo: use the information from the ProductSpec
        return null;
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
                loadProfileTypes.add(loadProfileTypeUsage.loadProfileType);
            }
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUseByLoadprofileTypes(this.getThesaurus(), this, new ArrayList<>(loadProfileTypes));
        }
    }

    private void validateNotUsedByDeviceTypes() {
        List<DeviceTypeRegisterMappingUsage> deviceTypeUsages = this.mapper(DeviceTypeRegisterMappingUsage.class).find("registerMapping", this);
        if (!deviceTypeUsages.isEmpty()) {
            Set<DeviceType> deviceTypes = new HashSet<>();
            for (DeviceTypeRegisterMappingUsage deviceTypeUsage : deviceTypeUsages) {
                deviceTypes.add(deviceTypeUsage.deviceType);
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
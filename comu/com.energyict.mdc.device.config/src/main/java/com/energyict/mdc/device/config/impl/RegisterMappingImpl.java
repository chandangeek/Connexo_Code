package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterGroup;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenRegisterMappingIsInUseException;
import com.energyict.mdc.device.config.exceptions.CannotUpdateProductSpecWhenRegisterMappingIsInUseException;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.DuplicateObisCodeException;
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;
import com.energyict.mdc.device.config.exceptions.ObisCodeIsRequiredException;
import com.energyict.mdc.device.config.exceptions.ProductSpecIsRequiredException;
import com.energyict.mdc.pluggable.impl.EventType;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegisterMappingImpl implements RegisterMapping {

    private long id;
    @NotNull
    private String name;
    private String obisCodeString;
    private ObisCode obisCode;
    private ProductSpec productSpec;
    private boolean cumulative;
    private RegisterGroup registerGroup;
    private String description;
    private Date modificationDate;

    private DataModel dataModel;
    private EventService eventService;
    private Thesaurus thesaurus;
    private Clock clock;

    @Inject
    public RegisterMappingImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
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

    private DataMapper<RegisterMapping> getDataMapper() {
        return this.dataModel.mapper(RegisterMapping.class);
    }

    @Override
    public void save () {
        this.modificationDate = this.clock.now();
        if (this.id > 0) {
            this.post();
        }
        else {
            this.postNew();
        }
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

    public void delete() {
        this.validateDelete();
        this.notifyDependents();
        this.getDataMapper().remove(this);
    }

    private void notifyDependents() {
        this.eventService.postEvent(EventType.DELETED.topic(), this);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.validateName(name);
        if (!name.equals(this.getName())) {
            this.validateUniqueName(name);
        }
        this.name = name;
    }

    private void validateName(String newName) {
        if (newName == null) {
            throw NameIsRequiredException.registerMappingNameIsRequired(this.thesaurus);
        }
        if (newName.trim().isEmpty()) {
            throw NameIsRequiredException.registerMappingNameIsRequired(this.thesaurus);
        }
    }

    private void validateUniqueName(String name) {
        if (this.findOtherByName(name) != null) {
            throw DuplicateNameException.registerMappingAlreadyExists(this.thesaurus, name);
        }
    }

    private RegisterMapping findOtherByName(String name) {
        return this.getDataMapper().getUnique("name", name).orNull();
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
            throw new ObisCodeIsRequiredException(this.thesaurus);
        }
        if (!this.getObisCode().equals(obisCode)) {
            if (this.isInUse()) {
                throw new CannotUpdateObisCodeWhenRegisterMappingIsInUseException(this.thesaurus, this);
            }
            RegisterMapping otherRegisterMapping = this.findOtherByObisCode(obisCode);
            if (otherRegisterMapping != null) {
                throw new DuplicateObisCodeException(this.thesaurus, obisCode, otherRegisterMapping);
            }
            this.obisCodeString = obisCode.toString();
            this.obisCode = obisCode;
        }
    }

    private RegisterMapping findOtherByObisCode(ObisCode obisCode) {
        return this.dataModel.mapper(RegisterMapping.class).getUnique("obisCodeString", obisCode.toString()).orNull();
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
            throw new ProductSpecIsRequiredException(this.thesaurus);
        }
        if (this.productSpec.getId() != productSpec.getId()) {
            if (this.isInUse()) {
                throw new CannotUpdateProductSpecWhenRegisterMappingIsInUseException(this.thesaurus, this);
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
        List<RegisterSpec> registerSpecs = this.dataModel.mapper(RegisterSpec.class).find("registerMapping", this);
        if (!registerSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUse(this.thesaurus, this, registerSpecs);
        }
    }

    private void validateNotUsedByChannelSpecs() {
        List<ChannelSpec> channelSpecs = this.dataModel.mapper(ChannelSpec.class).find("registerMapping", this);
        if (!channelSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUse(this.thesaurus, this, channelSpecs);
        }
    }

    private void validateNotUsedByLoadProfileTypes() {
        List<LoadProfileTypeRegisterMappingUsage> loadProfileTypeUsages = this.dataModel.mapper(LoadProfileTypeRegisterMappingUsage.class).find("registerMapping", this);
        if (!loadProfileTypeUsages.isEmpty()) {
            Set<LoadProfileType> loadProfileTypes = new HashSet<>();
            for (LoadProfileTypeRegisterMappingUsage loadProfileTypeUsage : loadProfileTypeUsages) {
                loadProfileTypes.add(loadProfileTypeUsage.loadProfileType);
            }
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUse(this.thesaurus, this, new ArrayList<>(loadProfileTypes));
        }
    }

    private void validateNotUsedByDeviceTypes() {
        List<DeviceTypeRegisterMappingUsage> deviceTypeUsages = this.dataModel.mapper(DeviceTypeRegisterMappingUsage.class).find("registerMapping", this);
        if (!deviceTypeUsages.isEmpty()) {
            Set<DeviceType> deviceTypes = new HashSet<>();
            for (DeviceTypeRegisterMappingUsage deviceTypeUsage : deviceTypeUsages) {
                deviceTypes.add(deviceTypeUsage.deviceType);
            }
            throw CannotDeleteBecauseStillInUseException.registerMappingIsStillInUse(this.thesaurus, this, deviceTypes);
        }
    }

    @Override
    public boolean isInUse() {
        return this.usedByChannelSpecs() || this.usedByRegisterSpecs();

    }

    private boolean usedByChannelSpecs() {
        List<ChannelSpec> channelSpecs = this.dataModel.mapper(ChannelSpec.class).find("registerMapping", this);
        return !channelSpecs.isEmpty();
    }

    private boolean usedByRegisterSpecs() {
        List<RegisterSpec> registerSpecs = this.dataModel.mapper(RegisterSpec.class).find("registerMapping", this);
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
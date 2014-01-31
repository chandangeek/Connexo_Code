package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.exceptions.CannotUpdateObisCodeWhenLogBookTypeIsInUseException;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.NameIsRequiredException;
import com.energyict.mdc.device.config.exceptions.ObisCodeIsRequiredException;

import javax.inject.Inject;

import static com.elster.jupiter.util.Checks.is;

/**
 * Copyrights EnergyICT
 * Date: 24/10/12
 * Time: 10:35
 */
public class LogBookTypeImpl implements LogBookType {

    private long id;
    private String name;
    private String obisCodeString;
    private ObisCode obisCode;
    private String description;

    private DataModel dataModel;
    private EventService eventService;
    private Thesaurus thesaurus;

    @Inject
    public LogBookTypeImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
    }

    LogBookTypeImpl initialize(String name, ObisCode obisCode) {
        this.setName(name);
        this.setObisCode(obisCode);
        return this;
    }

    static LogBookTypeImpl from (DataModel dataModel, String name, ObisCode obisCode) {
        return dataModel.getInstance(LogBookTypeImpl.class).initialize(name, obisCode);
    }

    private DataMapper<LogBookType> getDataMapper() {
        return this.dataModel.mapper(LogBookType.class);
    }

    @Override
    public void save () {
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
        this.eventService.postEvent(EventType.UPDATED.topic(), this);
    }

    private void validateDeviceConfigurations() {
        /* Todo: find all DeviceConfigurations that use this mapping via LogBookSpec
         *       and validate that the changes applied to this LogBookType
         *       do not violate any device configuration business constraints. */
    }

    public void delete() {
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
            throw NameIsRequiredException.logBookTypeNameIsRequired(this.thesaurus);
        }
        if (newName.trim().isEmpty()) {
            throw NameIsRequiredException.logBookTypeNameIsRequired(this.thesaurus);
        }
    }

    private void validateUniqueName(String name) {
        if (this.findOtherByName(name) != null) {
            throw DuplicateNameException.logBookTypeAlreadyExists(this.thesaurus, name);
        }
    }

    private LogBookType findOtherByName(String name) {
        return this.getDataMapper().getUnique("name", name).orNull();
    }

    @Override
    public ObisCode getObisCode() {
        if (this.obisCode == null) {
            this.obisCode = ObisCode.fromString(this.obisCodeString);
        }
        return this.obisCode;
    }

    @Override
    public void setObisCode(ObisCode obisCode) {
        if (obisCode == null) {
            throw ObisCodeIsRequiredException.logBookTypeRequiresObisCode(this.thesaurus);
        }
        if (!is(this.obisCodeString).equalTo(obisCode.toString())) {
            if (!canChangeObisCode()) {
                throw new CannotUpdateObisCodeWhenLogBookTypeIsInUseException(this.thesaurus, this);
            }
        }
        this.obisCodeString = obisCode.toString();
        this.obisCode = obisCode;
    }

    private boolean canChangeObisCode() {
        // Todo: Check that no LogBookSpec is using this LogBookType
        return true;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

}



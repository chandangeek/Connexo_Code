/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.obis.ObisCode;

import com.energyict.mdc.common.ValidObisCode;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class LogBookTypeImpl extends PersistentNamedObject<LogBookType> implements LogBookType {

    enum Fields {
        OBIS_CODE("obisCode");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null) {
            name = name.trim();
        }
        this.name = name;
    }

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private String obisCode;
    @ValidObisCode(groups = { Save.Create.class, Save.Update.class })
    private ObisCode obisCodeCached;
    private String oldObisCode;
    @Size(max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String description;

    @Inject
    public LogBookTypeImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(LogBookType.class, dataModel, eventService, thesaurus);
    }

    LogBookTypeImpl initialize(String name, ObisCode obisCode) {
        this.setName(name);
        this.setObisCode(obisCode);
        return this;
    }

    static LogBookTypeImpl from(DataModel dataModel, String name, ObisCode obisCode) {
        return dataModel.getInstance(LogBookTypeImpl.class).initialize(name, obisCode);
    }

    @Override
    protected void validateDelete() {
        this.eventService.postEvent(EventType.LOGBOOKTYPE_VALIDATEDELETE.topic(), this);
    }

    protected void doDelete() {
        this.getDataMapper().remove(this);
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.LOGBOOKTYPE;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.LOGBOOKTYPE;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.LOGBOOKTYPE;
    }

    @Override
    public ObisCode getObisCode() {
        if (this.obisCodeCached == null) {
            this.obisCodeCached = ObisCode.fromString(this.obisCode);
        }
        return this.obisCodeCached;
    }

    @Override
    public void setObisCode(ObisCode obisCode) {
        if (obisCode == null) {
            // javax.validation will throw ConstraintValidationException in the end
            this.obisCode = null;
            this.obisCodeCached = null;
        } else {
            this.oldObisCode = this.obisCode;
            this.obisCode = obisCode.toString();
            this.obisCodeCached = obisCode;
        }
    }

    public String getOldObisCode() {
        return oldObisCode;
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
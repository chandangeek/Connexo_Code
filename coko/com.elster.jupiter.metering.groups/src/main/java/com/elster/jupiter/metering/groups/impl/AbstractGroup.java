/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.metering.groups.Group;
import com.elster.jupiter.metering.groups.GroupEventData;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.HasId;

import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;

@UniqueName(groups = {Save.Create.class, Save.Update.class},
        // passing here all interfaces extending Group that refer to different DB tables.
        groupApisToCheck = {EndDeviceGroup.class, UsagePointGroup.class},
        message = "{" + MessageSeeds.Constants.DUPLICATE_NAME + "}")
abstract class AbstractGroup<T extends HasId & IdentifiedObject> implements Group<T> {

    private final DataModel dataModel;
    private final EventService eventService;

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(max= Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String name;
    private String mRID;
    private String description;
    private String aliasName;
    private String type;
    private String label;

    //audit columns
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    AbstractGroup(EventService eventService, DataModel dataModel) {
        this.eventService = eventService;
        this.dataModel = dataModel;
    }

    void validate() {
        Save.CREATE.validate(dataModel, this);
    }

    @Override
    public void delete() {
        this.eventService.postEvent(onDeleteAttempt().topic(), new GroupEventData(this));
        doDeleteAs(this.getClass());
    }

    private <C extends AbstractGroup<T>> void doDeleteAs(Class<C> apiClass) {
        dataModel.mapper(apiClass).remove(apiClass.cast(this));
    }

    final DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public String getAliasName() {
        return aliasName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getMRID() {
        return mRID;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setMRID(String mrid) {
        this.mRID = mrid;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractGroup<?>)) {
            return false;
        }
        AbstractGroup<?> that = (AbstractGroup<?>) o;
        return getParameterApiClass() == that.getParameterApiClass() && id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getParameterApiClass(), id);
    }

    abstract Class<T> getParameterApiClass();

    abstract EventType onDeleteAttempt();

    abstract Supplier<Query<T>> getBasicQuerySupplier();
}

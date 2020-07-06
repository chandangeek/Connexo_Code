/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.domain.util.AllowedChars;
import com.elster.jupiter.domain.util.HasOnlyWhiteListedCharacters;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.ZoneType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Objects;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.ZONE_TYPE_NAME_NOT_UNIQUE + "}")
class ZoneTypeImpl implements ZoneType {

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    @NotNull(message = "{" + MessageSeeds.Constants.ZONE_TYPE_APP_REQUIRED + "}")
    @Size(min = 1, max = 10, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_10 + "}")
    private String application;
    @NotNull(message = "{" + MessageSeeds.Constants.ZONE_TYPE_NAME_REQUIRED + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    @HasOnlyWhiteListedCharacters(whitelistRegex = AllowedChars.Constant.ALLOWED_CHARS_WITH_SPACE)
    private String typeName;
    private final DataModel dataModel;
    private final MeteringZoneService meteringZoneService;

    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    @Inject
    ZoneTypeImpl(DataModel dataModel, MeteringZoneService meteringZoneService) {
        this.dataModel = dataModel;
        this.meteringZoneService = meteringZoneService;
    }

    ZoneTypeImpl init(String typeName, String application) {
        this.typeName = typeName;
        this.application = application;
        return this;
    }

    static ZoneTypeImpl from(DataModel dataModel, String typeName, String application) {
        return dataModel.getInstance(ZoneTypeImpl.class).init(typeName, application);
    }

    @Override
    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    @Override
    public void save() {
        if (id == 0) {
            Save.CREATE.save(dataModel, this);
        } else {
            Save.UPDATE.save(dataModel, this);
        }
    }

    @Override
    public String getName() {
        return typeName;
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public Instant getModTime() {
        return modTime;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ZoneTypeImpl)) {
            return false;
        }
        ZoneTypeImpl that = (ZoneTypeImpl) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String getApplication() {
        return application;
    }

    @Override
    public void setName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public void setApplication(String application) {
        this.application = application;
    }

}
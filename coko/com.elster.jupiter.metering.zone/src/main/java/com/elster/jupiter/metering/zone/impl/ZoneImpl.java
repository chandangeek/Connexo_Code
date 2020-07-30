/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.domain.util.HasNoBlacklistedCharacters;
import com.elster.jupiter.domain.util.HasNotAllowedChars;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.Zone;
import com.elster.jupiter.metering.zone.ZoneType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Objects;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.ZONE_NAME_NOT_UNIQUE + "}")
class ZoneImpl implements Zone {

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    @NotNull(message = "{" + MessageSeeds.Constants.ZONE_NAME_REQUIRED + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    @HasNoBlacklistedCharacters(balcklistedCharRegEx = HasNotAllowedChars.Constant.SPECIAL_CHARS)
    private String name;
    @HasNoBlacklistedCharacters(balcklistedCharRegEx = HasNotAllowedChars.Constant.SCRIPT_CHARS)
    private String zoneTypeName;
    private long zoneTypeId;
    private final Reference<ZoneType> zoneType = Reference.empty();
    private final DataModel dataModel;
    private final MeteringZoneService meteringZoneService;
    private final Thesaurus thesaurus;

    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    @Inject
    ZoneImpl(DataModel dataModel, MeteringZoneService meteringZoneService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.meteringZoneService = meteringZoneService;
        this.thesaurus = thesaurus;
    }

    ZoneImpl init(String name, ZoneType zoneType) {
        this.name = name;
        this.zoneType.set(zoneType);
        return this;
    }

    static ZoneImpl from(DataModel dataModel, String name, ZoneType zoneType) {
        return dataModel.getInstance(ZoneImpl.class).init(name, zoneType);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public ZoneType getZoneType() {
        return zoneType.get();
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
    public void delete() {
        if (meteringZoneService.isZoneInUse(this.id))
            throw new ZoneInUseLocalizedException(thesaurus, this);

        dataModel.mapper(Zone.class).remove(this);
    }

    @Override
    public String getName() {
        return name;
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
        if (!(o instanceof ZoneImpl)) {
            return false;
        }
        ZoneImpl that = (ZoneImpl) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String getApplication() {
        return zoneType.map(ZoneType::getApplication).orElse("");
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setZoneType(ZoneType zoneType) {
        this.zoneType.set(zoneType);
        this.zoneTypeId = zoneType.getId();
    }

}
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Optional;

public class MetrologyPurposeImpl implements MetrologyPurpose {

    public enum Fields {
        ID("id"),
        NAME("name"),
        DESCRIPTION("description"),
        TRANSLATABLE("translatable"),
        DEFAULT_PURPOSE("defaultPurpose"),;

        private String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final DataModel dataModel;
    private final EventService eventService;
    private final Thesaurus thesaurus;

    private long id;
    @NotEmpty(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String name;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String description;
    private boolean translatable;
    private DefaultMetrologyPurpose defaultPurpose;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    public MetrologyPurposeImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
    }

    public MetrologyPurposeImpl init(DefaultMetrologyPurpose defaultMetrologyPurpose) {
        this.defaultPurpose = defaultMetrologyPurpose;
        return this.init(defaultMetrologyPurpose.getName().getKey(), defaultMetrologyPurpose.getDescription().getKey(), true);
    }

    public MetrologyPurposeImpl init(String name, String description, boolean translatable) {
        this.name = name;
        this.description = description;
        this.translatable = translatable;
        return this;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        if (this.translatable) {
            return this.thesaurus.getFormat(new SimpleTranslationKey(this.name, this.name)).format();
        }
        return this.name;
    }

    @Override
    public String getDescription() {
        if (this.translatable) {
            return this.thesaurus.getFormat(new SimpleTranslationKey(this.description, this.description)).format();
        }
        return this.description;
    }

    public Optional<DefaultMetrologyPurpose> getDefaultMetrologyPurpose() {
        return Optional.ofNullable(this.defaultPurpose);
    }

    public void save() {
        Save.action(getId()).save(this.dataModel, this);
    }

    @Override
    public void delete() {
        this.eventService.postEvent(EventType.METROLOGY_PURPOSE_DELETED.topic(), this);
        // Event should be sent before actual deletion or SQL exception will be thrown if the metrology purpose is in use
        // see MetrologyPurposeDeletionVetoEventHandler class
        this.dataModel.remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetrologyPurposeImpl that = (MetrologyPurposeImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(getId());
    }
}

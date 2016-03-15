package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.util.Optional;

public class MetrologyPurposeImpl implements MetrologyPurpose {

    public enum Fields {
        ID("id"),
        NAME("name"),
        DEFAULT_PURPOSE("defaultPurpose"),
        DESCRIPTION("description"),;

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
    private String name;
    private String description;
    private DefaultMetrologyPurpose defaultPurpose;

    @Inject
    public MetrologyPurposeImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
    }

    public MetrologyPurposeImpl init(DefaultMetrologyPurpose defaultMetrologyPurpose) {
        this.defaultPurpose = defaultMetrologyPurpose;
        this.name = defaultMetrologyPurpose.getNameTranslationKey().getDefaultFormat();
        this.description = defaultMetrologyPurpose.getDescriptionTranslationKey().getDefaultFormat();
        return this;
    }

    public MetrologyPurposeImpl init(String name, String description) {
        this.name = name;
        this.description = description;
        return this;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        if (this.defaultPurpose != null) {
            return this.thesaurus.getFormat(this.defaultPurpose.getNameTranslationKey()).format();
        }
        return this.name;
    }

    @Override
    public String getDescription() {
        if (this.defaultPurpose != null) {
            return this.thesaurus.getFormat(this.defaultPurpose.getDescriptionTranslationKey()).format();
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

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableFilter;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}")
public class ReadingTypeDeliverableImpl implements ReadingTypeDeliverable, HasUniqueName {
    public enum Fields {
        ID("id"),
        NAME("name"),
        METROLOGY_CONTRACT("metrologyContract"),
        READING_TYPE("readingType"),
        FORMULA("formula"),;

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
    private final MetrologyConfigurationService metrologyConfigurationService;

    private long id;
    @NotEmpty(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String name;
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<MetrologyContract> metrologyContract = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<ReadingType> readingType = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<Formula> formula = ValueReference.absent();

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    @Inject
    public ReadingTypeDeliverableImpl(DataModel dataModel, EventService eventService, MetrologyConfigurationService metrologyConfigurationService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    public ReadingTypeDeliverableImpl init(String name, MetrologyContract metrologyContract, ReadingType readingType, Formula formula) {
        this.name = name;
        this.metrologyContract.set(metrologyContract);
        this.readingType.set(readingType);
        this.formula.set(formula);
        return this;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public MetrologyContract getMetrologyContract() {
        return this.metrologyContract.orNull();
    }

    @Override
    public Formula getFormula() {
        return this.formula.orNull();
    }

    @Override
    public ReadingType getReadingType() {
        return this.readingType.orNull();
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setReadingType(ReadingType readingType) {
        this.readingType.set(readingType);
    }

    @Override
    public void setFormula(Formula formula) {
        this.formula.set(formula);
    }

    public void save() {
        Save.action(getId()).save(this.dataModel, this);
        this.eventService.postEvent(EventType.READING_TYPE_DELIVERABLE_CREATED.topic(), this);
    }

    @Override
    public void update() {
        Save.action(getId()).save(this.dataModel, this);
        this.eventService.postEvent(EventType.READING_TYPE_DELIVERABLE_UPDATED.topic(), this);
    }

    @Override
    public void delete() {
        dataModel.remove(this);
        this.eventService.postEvent(EventType.READING_TYPE_DELIVERABLE_DELETED.topic(), this);
    }

    @Override
    public boolean validateName() {
        ReadingTypeDeliverableFilter filter = new ReadingTypeDeliverableFilter().withMetrologyContracts(getMetrologyContract());
        List<ReadingTypeDeliverable> deliverables = this.metrologyConfigurationService.findReadingTypeDeliverable(filter);
        return deliverables.isEmpty() || deliverables.size() == 1 && deliverables.get(0).getId() == getId();
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReadingTypeDeliverableImpl that = (ReadingTypeDeliverableImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(getId());
    }
}

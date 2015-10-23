package com.elster.insight.usagepoint.config.impl;

import static com.elster.jupiter.domain.util.Save.action;
import static com.google.common.base.MoreObjects.toStringHelper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.constraints.Size;

import com.elster.insight.usagepoint.config.MetrologyConfValidationRuleSetUsage;
import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.domain.util.Unique;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

@Unique(fields = "name", groups = Save.Create.class)
public final class MetrologyConfigurationImpl implements MetrologyConfiguration {
    private long id;
    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;
    private List<MetrologyConfValidationRuleSetUsageImpl> metrologyConfValidationRuleSetUsages = new ArrayList<MetrologyConfValidationRuleSetUsageImpl>();
    
    @NotEmpty
    @Size(max = 80)
    private String name;

    private final DataModel dataModel;
    private final EventService eventService;
    private final ValidationService validationService;

    @Inject
    MetrologyConfigurationImpl(DataModel dataModel, EventService eventService, ValidationService validationService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.validationService = validationService;
    }

    MetrologyConfigurationImpl init(String name) {
        if (name != null) {
            setName(name.trim());
        }
        return this;
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
        this.name = name;
    }

    @Override
    public MetrologyConfValidationRuleSetUsage addValidationRuleSet(ValidationRuleSet validationRuleSet) {
        MetrologyConfValidationRuleSetUsageImpl usage =new MetrologyConfValidationRuleSetUsageImpl(dataModel, eventService, validationService);
        usage.init(this, validationRuleSet);
        metrologyConfValidationRuleSetUsages.add(usage);
        getDataModel().touch(this);
        return usage;
    }
    
    @Override
    public List<ValidationRuleSet> getValidationRuleSets() {
        return this.metrologyConfValidationRuleSetUsages
                .stream()
                .map(MetrologyConfValidationRuleSetUsage::getValidationRuleSet)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    public void setModTime(Instant modTime) {
        this.modTime = modTime;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public void update() {
        Save s = action(getId());
        s.save(dataModel, this);
        if (s == Save.CREATE) {
            eventService.postEvent(EventType.METROLOGYCONFIGURATION_CREATED.topic(), this);
        } else {
            eventService.postEvent(EventType.METROLOGYCONFIGURATION_UPDATED.topic(), this);
        }
    }

    @Override
    public void delete() {
        dataModel.remove(this);
        eventService.postEvent(EventType.METROLOGYCONFIGURATION_DELETED.topic(), this);
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MetrologyConfiguration)) {
            return false;
        }
        MetrologyConfiguration party = (MetrologyConfiguration) o;
        return id == party.getId();
    }

    protected DataModel getDataModel() {
        return dataModel;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return toStringHelper(this).omitNullValues().add("id", id).add("name", name).toString();
    }

}

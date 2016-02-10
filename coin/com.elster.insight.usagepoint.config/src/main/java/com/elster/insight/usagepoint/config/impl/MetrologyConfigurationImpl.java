package com.elster.insight.usagepoint.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.domain.util.Unique;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.MetrologyConfigurationValidationRuleSetUsage;
import com.elster.insight.usagepoint.config.MetrologyContract;
import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;
import com.elster.insight.usagepoint.config.ReadingTypeRequirement;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.elster.jupiter.domain.util.Save.action;
import static com.google.common.base.MoreObjects.toStringHelper;

@Unique(fields = "name", groups = {Save.Create.class, Save.Update.class})
public final class MetrologyConfigurationImpl implements MetrologyConfiguration {
    private long id;
    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;
    private List<MetrologyConfigurationValidationRuleSetUsage> metrologyConfValidationRuleSetUsages = new ArrayList<>();

    @NotEmpty
    @Size(max = Table.NAME_LENGTH)
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
        setName(name);
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
    public void updateName(String name) {
        setName(name);
        this.update();
    }

    public List<MetrologyConfigurationValidationRuleSetUsage> getMetrologyConfValidationRuleSetUsages() {
        return metrologyConfValidationRuleSetUsages;
    }

    @Override
    public MetrologyConfigurationValidationRuleSetUsage addValidationRuleSet(ValidationRuleSet validationRuleSet) {
        MetrologyConfigurationValidationRuleSetUsageImpl usage =new MetrologyConfigurationValidationRuleSetUsageImpl(dataModel, eventService, validationService);
        usage.init(this, validationRuleSet);
        metrologyConfValidationRuleSetUsages.add(usage);
        getDataModel().touch(this);
        return usage;
    }

    private void setName(String name) {
        if (name != null) {
            this.name = name.trim();
        }
    }

    private MetrologyConfigurationValidationRuleSetUsage getUsage(ValidationRuleSet validationRuleSet) {
        List<MetrologyConfigurationValidationRuleSetUsage> usages = this.getMetrologyConfValidationRuleSetUsages();
        for (MetrologyConfigurationValidationRuleSetUsage usage : usages) {
            if (usage.getValidationRuleSet().getId() == validationRuleSet.getId()) {
                return usage;
            }
        }
        return null;
    }

    @Override
    public void removeValidationRuleSet(ValidationRuleSet validationRuleSet) {
        MetrologyConfigurationValidationRuleSetUsage usage = getUsage(validationRuleSet);
        metrologyConfValidationRuleSetUsages.remove(usage);
        getDataModel().touch(this);
    }

    @Override
    public List<ValidationRuleSet> getValidationRuleSets() {
        return this.metrologyConfValidationRuleSetUsages
                .stream()
                .map(MetrologyConfigurationValidationRuleSetUsage::getValidationRuleSet)
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
    public List<MetrologyContract> getContracts() {
        return Collections.emptyList();
    }

    @Override
    public List<ReadingTypeRequirement> getRequirements() {
        return Collections.emptyList();
    }

    @Override
    public List<ReadingTypeDeliverable> getDeliverables() {
        return Collections.emptyList();
    }

    @Override
    public long getVersion() {
        return version;
    }

    private DataModel getDataModel() {
        return dataModel;
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

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .omitNullValues()
                .add("id", id)
                .add("name", name)
                .add("validationRuleSets", metrologyConfValidationRuleSetUsages.size() ==0?null:metrologyConfValidationRuleSetUsages.stream().map(vrs -> vrs.getValidationRuleSet().getName()).collect(java.util.stream.Collectors.joining(","))).toString();
    }

}

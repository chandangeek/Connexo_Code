package com.elster.insight.usagepoint.config.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.MetrologyConfigurationCustomPropertySetUsages;
import com.elster.insight.usagepoint.config.MetrologyConfigurationValidationRuleSetUsage;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.domain.util.Unique;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.elster.jupiter.domain.util.Save.action;
import static com.google.common.base.MoreObjects.toStringHelper;

@Unique(fields = "name", groups = {Save.Create.class, Save.Update.class})
public final class MetrologyConfigurationImpl implements MetrologyConfiguration {

    public enum Fields {
        NAME("name"),
        VALIDATION_RULE_SETS("metrologyConfValidationRuleSetUsages"),
        CUSTOM_PROPERTY_SETS("customPropertySets"),;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final DataModel dataModel;
    private final EventService eventService;
    private final ValidationService validationService;

    private long id;
    @NotEmpty
    @Size(max = Table.NAME_LENGTH)
    private String name;
    private List<MetrologyConfigurationValidationRuleSetUsage> metrologyConfValidationRuleSetUsages = new ArrayList<>();
    private List<MetrologyConfigurationCustomPropertySetUsages> customPropertySets = new ArrayList<>();

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

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

    protected void setName(String name) {
        if (name != null) {
            this.name = name.trim();
        }
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
        MetrologyConfigurationValidationRuleSetUsageImpl usage = new MetrologyConfigurationValidationRuleSetUsageImpl(dataModel, eventService, validationService);
        usage.init(this, validationRuleSet);
        metrologyConfValidationRuleSetUsages.add(usage);
        getDataModel().touch(this);
        return usage;
    }

    protected MetrologyConfigurationValidationRuleSetUsage getUsage(ValidationRuleSet validationRuleSet) {
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

    @Override
    public List<RegisteredCustomPropertySet> getCustomPropertySets() {
        return customPropertySets
                .stream()
                .map(MetrologyConfigurationCustomPropertySetUsages::getRegisteredCustomPropertySet)
                .collect(Collectors.toList());
    }

    @Override
    public void addCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet) {
        if (this.customPropertySets.stream()
                .noneMatch(cpsUsage -> cpsUsage.getRegisteredCustomPropertySet().getId() == registeredCustomPropertySet.getId())){
            MetrologyConfigurationCustomPropertySetUsagesImpl newCpsUsage = getDataModel().getInstance(MetrologyConfigurationCustomPropertySetUsagesImpl.class)
                    .init(this, registeredCustomPropertySet);
            this.customPropertySets.add(newCpsUsage);
            this.dataModel.touch(this);
        }
    }

    @Override
    public void removeCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet) {
        this.customPropertySets.stream()
                .filter(cpsUsage -> cpsUsage.getRegisteredCustomPropertySet().getId() == registeredCustomPropertySet.getId())
                .findAny()
                .ifPresent(cpsUsage -> {
                    customPropertySets.remove(cpsUsage);
                    dataModel.touch(MetrologyConfigurationImpl.this);
                });

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
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof MetrologyConfiguration)) {
            return false;
        }
        MetrologyConfiguration that = (MetrologyConfiguration) o;
        return id == that.getId();
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
        return toStringHelper(this)
                .omitNullValues()
                .add("id", id)
                .add("name", name)
                .add("validationRuleSets", metrologyConfValidationRuleSetUsages.size() == 0 ? null : metrologyConfValidationRuleSetUsages.stream().map(vrs -> vrs.getValidationRuleSet().getName()).collect(java.util.stream.Collectors.joining(","))).toString();
    }
}

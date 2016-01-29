package com.elster.insight.usagepoint.config.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.MetrologyConfigurationCustomPropertySetUsages;
import com.elster.insight.usagepoint.config.MetrologyConfigurationValidationRuleSetUsage;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.insight.usagepoint.config.impl.errors.CannotManageCPSOnActiveMetrologyConfig;
import com.elster.insight.usagepoint.config.impl.errors.MessageSeeds;
import com.elster.insight.usagepoint.config.impl.validation.HasUniqueName;
import com.elster.insight.usagepoint.config.impl.validation.UniqueName;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.domain.util.Save.action;
import static com.google.common.base.MoreObjects.toStringHelper;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.OBJ_MUST_HAVE_UNIQUE_NAME + "}")
public final class MetrologyConfigurationImpl implements MetrologyConfiguration, HasUniqueName {
    public enum Fields {
        NAME("name"),
        ACTIVE("active"),
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
    private final Thesaurus thesaurus;
    private final UsagePointConfigurationService usagePointConfigurationService;

    private long id;
    @NotEmpty
    @Size(max = Table.NAME_LENGTH)
    private String name;
    private boolean active;
    private List<MetrologyConfigurationValidationRuleSetUsage> metrologyConfValidationRuleSetUsages = new ArrayList<>();
    private List<MetrologyConfigurationCustomPropertySetUsages> customPropertySets = new ArrayList<>();

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    @Inject
    MetrologyConfigurationImpl(DataModel dataModel, EventService eventService, ValidationService validationService, Thesaurus thesaurus, UsagePointConfigurationService usagePointConfigurationService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.validationService = validationService;
        this.thesaurus = thesaurus;
        this.usagePointConfigurationService = usagePointConfigurationService;
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

    @Override
    public boolean validateName() {
        Optional<MetrologyConfiguration> other = this.usagePointConfigurationService.findMetrologyConfiguration(getName());
        return !other.isPresent() || other.get().getId() == getId();
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

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void activate() {
        if (!this.active) {
            this.active = true;
            update();
        }
    }

    @Override
    public void deactivate() {
        if (this.active) {
            this.active = false;
            update();
        }
    }

    @Override
    public List<RegisteredCustomPropertySet> getCustomPropertySets() {
        return customPropertySets
                .stream()
                .map(MetrologyConfigurationCustomPropertySetUsages::getRegisteredCustomPropertySet)
                .collect(Collectors.toList());
    }

    private void checkCanManageCps() {
        if (isActive()) {
            throw new CannotManageCPSOnActiveMetrologyConfig(this.thesaurus);
        }
    }

    @Override
    public void addCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet) {
        checkCanManageCps();
        if (this.customPropertySets.stream()
                .noneMatch(cpsUsage -> cpsUsage.getRegisteredCustomPropertySet().getId() == registeredCustomPropertySet.getId())) {
            MetrologyConfigurationCustomPropertySetUsagesImpl newCpsUsage = getDataModel().getInstance(MetrologyConfigurationCustomPropertySetUsagesImpl.class)
                    .init(this, registeredCustomPropertySet);
            this.customPropertySets.add(newCpsUsage);
            this.dataModel.touch(this);
        }
    }

    @Override
    public void removeCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet) {
        checkCanManageCps();
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

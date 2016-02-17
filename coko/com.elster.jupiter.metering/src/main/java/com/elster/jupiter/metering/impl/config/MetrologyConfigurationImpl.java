package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.domain.util.Save.action;
import static com.google.common.base.MoreObjects.toStringHelper;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}")
public final class MetrologyConfigurationImpl implements MetrologyConfiguration, HasUniqueName {
    public enum Fields {
        NAME("name"),
        ACTIVE("active"),
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
    private final Thesaurus thesaurus;
    private final MetrologyConfigurationService metrologyConfigurationService;

    @SuppressWarnings("unused")
    private long id;
    @NotEmpty
    @Size(max = Table.NAME_LENGTH)
    private String name;
    private boolean active;
    private List<MetrologyConfigurationCustomPropertySetUsage> customPropertySets = new ArrayList<>();

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    MetrologyConfigurationImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, MetrologyConfigurationService metrologyConfigurationService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
        this.metrologyConfigurationService = metrologyConfigurationService;
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

    void setName(String name) {
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
        Optional<MetrologyConfiguration> other = this.metrologyConfigurationService.findMetrologyConfiguration(getName());
        return !other.isPresent() || other.get().getId() == getId();
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
                .map(MetrologyConfigurationCustomPropertySetUsage::getRegisteredCustomPropertySet)
                .collect(Collectors.toList());
    }

    private void checkCanManageCps() {
        if (isActive()) {
            throw new CannotManageCustomPropertySetOnActiveMetrologyConfiguration(this.thesaurus);
        }
    }

    @Override
    public void addCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet) {
        checkCanManageCps();
        if (this.customPropertySets.stream()
                .noneMatch(cpsUsage -> cpsUsage.getRegisteredCustomPropertySet().getId() == registeredCustomPropertySet.getId())) {
            MetrologyConfigurationCustomPropertySetUsageImpl newCpsUsage =
                    getDataModel()
                            .getInstance(MetrologyConfigurationCustomPropertySetUsageImpl.class)
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

    private DataModel getDataModel() {
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
                .add("name", name).toString();
    }

}
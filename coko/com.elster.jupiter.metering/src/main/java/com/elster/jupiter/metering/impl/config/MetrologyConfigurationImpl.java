package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyConfigurationStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.domain.util.Save.CREATE;
import static com.elster.jupiter.domain.util.Save.UPDATE;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}")
public final class MetrologyConfigurationImpl implements MetrologyConfiguration, HasUniqueName {
    public enum Fields {
        NAME("name"),
        DESCRIPTION("description"),
        STATUS("status"),
        SERVICECATEGORY("serviceCategory"),
        CUSTOM_PROPERTY_SETS("customPropertySets"),
        ;

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
    @NotEmpty(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String name;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String description;
    @NotNull
    private MetrologyConfigurationStatus status = MetrologyConfigurationStatus.INACTIVE;
    @IsPresent(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<ServiceCategory> serviceCategory = ValueReference.absent();
    @Valid
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

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    @Override
    public MetrologyConfigurationStatus getStatus() {
        return status;
    }

    @Override
    public boolean isActive() {
        return MetrologyConfigurationStatus.ACTIVE == status;
    }

    @Override
    public void activate() {
        if (MetrologyConfigurationStatus.INACTIVE == status) {
            this.status = MetrologyConfigurationStatus.ACTIVE;
            update();
        }
    }

    @Override
    public ServiceCategory getServiceCategory() {
        return serviceCategory.get();
    }

    void setServiceCategory(ServiceCategory serviceCategory) {
        this.serviceCategory.set(serviceCategory);
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
                .noneMatch(cpsUsage -> cpsUsage.getRegisteredCustomPropertySet()
                        .getId() == registeredCustomPropertySet.getId())) {
            MetrologyConfigurationCustomPropertySetUsageImpl newCpsUsage =
                    dataModel
                            .getInstance(MetrologyConfigurationCustomPropertySetUsageImpl.class)
                            .init(this, registeredCustomPropertySet);
            customPropertySets.add(newCpsUsage);
            dataModel.touch(this);
        }
    }

    @Override
    public void removeCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet) {
        checkCanManageCps();
        customPropertySets.stream()
                .filter(cpsUsage -> cpsUsage.getRegisteredCustomPropertySet()
                        .getId() == registeredCustomPropertySet.getId())
                .findAny()
                .ifPresent(cpsUsage -> {
                    customPropertySets.remove(cpsUsage);
                    dataModel.touch(MetrologyConfigurationImpl.this);
                });
    }

    void create() {
        CREATE.save(dataModel, this);
        eventService.postEvent(EventType.METROLOGYCONFIGURATION_CREATED.topic(), this);
    }

    void update() {
        UPDATE.save(dataModel, this);
        eventService.postEvent(EventType.METROLOGYCONFIGURATION_UPDATED.topic(), this);
    }

    @Override
    public void delete() {
        dataModel.remove(this);
        eventService.postEvent(EventType.METROLOGYCONFIGURATION_DELETED.topic(), this);
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

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public boolean validateName() {
        Optional<MetrologyConfiguration> other = metrologyConfigurationService.findMetrologyConfiguration(getName());
        return !other.isPresent() || other.get().getId() == getId();
    }
}
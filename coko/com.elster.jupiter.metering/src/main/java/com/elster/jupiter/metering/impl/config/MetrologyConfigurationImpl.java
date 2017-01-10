package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.config.DeliverableType;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationStatus;
import com.elster.jupiter.metering.config.MetrologyConfigurationUpdater;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementsCollector;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.domain.util.Save.CREATE;
import static com.elster.jupiter.domain.util.Save.UPDATE;
import static com.elster.jupiter.util.conditions.Where.where;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}")
public class MetrologyConfigurationImpl implements ServerMetrologyConfiguration, HasUniqueName {
    public static final String TYPE_IDENTIFIER = "B";

    public static final Map<String, Class<? extends MetrologyConfiguration>> IMPLEMENTERS = ImmutableMap.of(
            MetrologyConfigurationImpl.TYPE_IDENTIFIER, MetrologyConfigurationImpl.class,
            UsagePointMetrologyConfigurationImpl.TYPE_IDENTIFIER, UsagePointMetrologyConfigurationImpl.class);

    public enum Fields {
        NAME("name"),
        DESCRIPTION("description"),
        STATUS("status"),
        SERVICECATEGORY("serviceCategory"),
        CUSTOM_PROPERTY_SETS("customPropertySets"),
        RT_REQUIREMENTS("readingTypeRequirements"),
        METROLOGY_CONTRACTS("metrologyContracts"),
        METER_ROLES("meterRoles"),
        DELIVERABLES("deliverables"),
        REQUIREMENT_TO_ROLE_REFERENCES("requirementToRoleUsages"),
        USAGE_POINT_REQUIREMENTS("usagePointRequirements"),;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    private final EventService eventService;
    private final CustomPropertySetService customPropertySetService;

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
    private List<ReadingTypeRequirement> readingTypeRequirements = new ArrayList<>();
    private List<MetrologyContract> metrologyContracts = new ArrayList<>();
    private List<ReadingTypeDeliverable> deliverables = new ArrayList<>();

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    MetrologyConfigurationImpl(ServerMetrologyConfigurationService metrologyConfigurationService, EventService eventService, CustomPropertySetService customPropertySetService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.eventService = eventService;
        this.customPropertySetService = customPropertySetService;
    }

    protected ServerMetrologyConfigurationService getMetrologyConfigurationService() {
        return metrologyConfigurationService;
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
    public MetrologyConfigurationUpdater startUpdate() {
        return new MetrologyConfigurationUpdaterImpl(this);
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

    private void checkLinkedUsagePoints() {
        if (!metrologyConfigurationService.getDataModel()
                .query(EffectiveMetrologyConfigurationOnUsagePoint.class, MetrologyConfiguration.class)
                .select(Where.where("metrologyConfiguration").isEqualTo(this), Order.NOORDER, false, null, 1, 1)
                .isEmpty()) {
            throw new CannotDeactivateMetrologyConfiguration(this.metrologyConfigurationService.getThesaurus());
        }
    }

    @Override
    public void activate() {
        if (MetrologyConfigurationStatus.INACTIVE == status) {
            this.status = MetrologyConfigurationStatus.ACTIVE;
            this.update();
        }
    }

    @Override
    public void deactivate() {
        if (MetrologyConfigurationStatus.ACTIVE == status) {
            checkLinkedUsagePoints();
            this.status = MetrologyConfigurationStatus.INACTIVE;
            this.update();
        }
    }

    @Override
    public void deprecate() {
        if (MetrologyConfigurationStatus.ACTIVE == status) {
            this.status = MetrologyConfigurationStatus.DEPRECATED;
            this.update();
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
            throw new CannotManageCustomPropertySetOnActiveMetrologyConfiguration(this.metrologyConfigurationService.getThesaurus());
        }
    }

    @Override
    public void addCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet) {
        checkCanManageCps();
        if (this.customPropertySets.stream()
                .noneMatch(cpsUsage -> cpsUsage.getRegisteredCustomPropertySet()
                        .getId() == registeredCustomPropertySet.getId())) {
            MetrologyConfigurationCustomPropertySetUsageImpl newCpsUsage =
                    this.metrologyConfigurationService.getDataModel()
                            .getInstance(MetrologyConfigurationCustomPropertySetUsageImpl.class)
                            .init(this, registeredCustomPropertySet);
            customPropertySets.add(newCpsUsage);
            touch();
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
                    touch();
                });
    }

    @Override
    public List<MetrologyContract> getContracts() {
        return Collections.unmodifiableList(new ArrayList<>(this.metrologyContracts));
    }

    @Override
    public MetrologyContract addMandatoryMetrologyContract(MetrologyPurpose metrologyPurpose) {
        return addMetrologyContract(metrologyPurpose, true);
    }

    @Override
    public MetrologyContract addMetrologyContract(MetrologyPurpose metrologyPurpose) {
        return addMetrologyContract(metrologyPurpose, false);
    }

    private MetrologyContract addMetrologyContract(MetrologyPurpose metrologyPurpose, boolean mandatory) {
        return getContracts()
                .stream()
                .filter(metrologyContract -> metrologyContract.getMetrologyPurpose().equals(metrologyPurpose))
                .findAny()
                .orElseGet(() -> createMetrologyContract(metrologyPurpose, mandatory));
    }

    private MetrologyContract createMetrologyContract(MetrologyPurpose metrologyPurpose, boolean mandatory) {
        MetrologyContractImpl metrologyContract = this.metrologyConfigurationService.getDataModel().getInstance(MetrologyContractImpl.class)
                .init(this, metrologyPurpose);
        metrologyContract.setMandatory(mandatory);
        Save.CREATE.validate(this.metrologyConfigurationService.getDataModel(), metrologyContract);
        this.metrologyContracts.add(metrologyContract);
        touch();
        return metrologyContract;
    }

    @Override
    public void removeMetrologyContract(MetrologyContract metrologyContract) {
        ((MetrologyContractImpl) metrologyContract).prepareDelete();
        this.eventService.postEvent(EventType.METROLOGY_CONTRACT_DELETED.topic(), metrologyContract);
        if (this.metrologyContracts.remove(metrologyContract)) {
            touch();
        }
    }

    @Override
    public List<ReadingTypeRequirement> getRequirements() {
        return Collections.unmodifiableList(new ArrayList<>(this.readingTypeRequirements));
    }

    @Override
    public MetrologyConfigurationReadingTypeRequirementBuilder newReadingTypeRequirement(String name) {
        return new MetrologyConfigurationReadingTypeRequirementBuilderImpl(this.metrologyConfigurationService, this, name);
    }

    @Override
    public void removeReadingTypeRequirement(ReadingTypeRequirement readingTypeRequirement) {
        if (this.readingTypeRequirements.remove(readingTypeRequirement)) {
            touch();
        }
    }

    @Override
    public ReadingTypeDeliverableBuilderImpl newReadingTypeDeliverable(String name, ReadingType readingType, Formula.Mode mode) {
        return new ReadingTypeDeliverableBuilderImpl(
                this,
                name,
                DeliverableType.NUMERICAL,
                readingType,
                mode,
                this.customPropertySetService,
                this.metrologyConfigurationService.getDataModel(),
                this.metrologyConfigurationService.getThesaurus());
    }

    @Override
    public ReadingTypeDeliverableBuilderImpl newReadingTypeDeliverable(String name, DeliverableType deliverableType, ReadingType readingType, Formula.Mode mode) {
        return new ReadingTypeDeliverableBuilderImpl(
                this,
                name,
                deliverableType,
                readingType,
                mode,
                this.customPropertySetService,
                this.metrologyConfigurationService.getDataModel(),
                this.metrologyConfigurationService.getThesaurus());
    }

    @Override
    public ReadingTypeDeliverable addReadingTypeDeliverable(String name, DeliverableType deliverableType, ReadingType readingType, Formula formula) {
        ReadingTypeDeliverableImpl deliverable =
                this.metrologyConfigurationService.getDataModel()
                        .getInstance(ReadingTypeDeliverableImpl.class)
                        .init(this, name, deliverableType, readingType, (ServerFormula) formula);
        Save.CREATE.validate(this.metrologyConfigurationService.getDataModel(), deliverable);
        this.deliverables.add(deliverable);
        touch();
        return deliverable;
    }


    @Override
    public void removeReadingTypeDeliverable(ReadingTypeDeliverable deliverable) {
        if (!metrologyConfigurationService.getDataModel()
                .query(ReadingTypeDeliverableNodeImpl.class)
                .select(where("readingTypeDeliverable").isEqualTo(deliverable))
                .isEmpty()) {
            throw new CannotDeleteReadingTypeDeliverableException(metrologyConfigurationService.getThesaurus(), deliverable.getName());
        }
        ((ReadingTypeDeliverableImpl) deliverable).prepareDelete();
        if (this.deliverables.remove(deliverable)) {
            this.eventService.postEvent(EventType.READING_TYPE_DELIVERABLE_DELETED.topic(), deliverable);
            touch();
        }
    }

    @Override
    public List<ReadingTypeDeliverable> getDeliverables() {
        return Collections.unmodifiableList(new ArrayList<>(this.deliverables));
    }

    void create() {
        CREATE.save(this.metrologyConfigurationService.getDataModel(), this);
        eventService.postEvent(EventType.METROLOGYCONFIGURATION_CREATED.topic(), this);
    }

    void update() {
        UPDATE.save(this.metrologyConfigurationService.getDataModel(), this);
        eventService.postEvent(EventType.METROLOGYCONFIGURATION_UPDATED.topic(), this);
    }

    void touch() {
        this.metrologyConfigurationService.getDataModel().touch(this);
    }

    @Override
    public void deliverableUpdated(ReadingTypeDeliverableImpl deliverable) {
        this.touch();
    }

    @Override
    public void contractUpdated(MetrologyContractImpl contract) {
        this.touch();
    }

    void addReadingTypeRequirement(ReadingTypeRequirement readingTypeRequirement) {
        Save.CREATE.validate(this.metrologyConfigurationService.getDataModel(), readingTypeRequirement);
        this.readingTypeRequirements.add(readingTypeRequirement);
        touch();
    }

    @Override
    public void delete() {
        getContracts().forEach(this::removeMetrologyContract);
        getDeliverables().forEach(this::removeReadingTypeDeliverable);
        readingTypeRequirements.clear();
        customPropertySets.clear();
        this.metrologyConfigurationService.getDataModel().remove(this);
        eventService.postEvent(EventType.METROLOGYCONFIGURATION_DELETED.topic(), this);
    }

    @Override
    public final boolean equals(Object o) {
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
    public final int hashCode() {
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

    @Override
    public List<ReadingTypeRequirement> getMandatoryReadingTypeRequirements() {
        ReadingTypeRequirementsCollector requirementsCollector = new ReadingTypeRequirementsCollector();
        this.getContracts()
                .stream()
                .filter(MetrologyContract::isMandatory)
                .map(MetrologyContract::getDeliverables)
                .flatMap(Collection::stream)
                .map(ReadingTypeDeliverable::getFormula)
                .map(Formula::getExpressionNode)
                .forEach(expressionNode -> expressionNode.accept(requirementsCollector));
        return requirementsCollector.getReadingTypeRequirements();
    }
}
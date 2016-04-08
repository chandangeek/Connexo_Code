package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationStatus;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.metering.impl.aggregation.IntervalLength;
import com.elster.jupiter.metering.impl.aggregation.UnitConversionSupport;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.domain.util.Save.CREATE;
import static com.elster.jupiter.domain.util.Save.UPDATE;

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
    MetrologyConfigurationImpl(ServerMetrologyConfigurationService metrologyConfigurationService, EventService eventService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.eventService = eventService;
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
    public void updateName(String name) {
        this.setName(name);
        this.update();
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
            this.status = MetrologyConfigurationStatus.INACTIVE;
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
        return Collections.unmodifiableList(this.metrologyContracts);
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
        if (this.metrologyContracts.remove(metrologyContract)) {
            touch();
        }
    }

    @Override
    public List<ReadingTypeRequirement> getRequirements() {
        return Collections.unmodifiableList(this.readingTypeRequirements);
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
        return new ReadingTypeDeliverableBuilderImpl(this, name, readingType, mode, this.metrologyConfigurationService.getDataModel(), this.metrologyConfigurationService.getThesaurus());
    }

    @Override
    public ReadingTypeDeliverable addReadingTypeDeliverable(String name, ReadingType readingType, Formula formula) {
        /*if ((readingType != null) && (!readingType.isRegular())) {
            throw new InvalidNodeException(metrologyConfigurationService.getThesaurus(), MessageSeeds.IRREGULAR_READINGTYPE_IN_DELIVERABLE);
        }
        if (readingType != null && formula.getMode().equals(Formula.Mode.AUTO) &&  !UnitConversionSupport.isAssignable(readingType, formula.getExpressionNode().getDimension())) {
            throw new InvalidNodeException(metrologyConfigurationService.getThesaurus(), MessageSeeds.READINGTYPE_OF_DELIVERABLE_IS_NOT_COMPATIBLE_WITH_FORMULA);
        }
        if (readingType != null) {
            IntervalLength intervalLengthOfReadingType = IntervalLength.from(readingType);
            IntervalLength intervalLengthOfFormula = ((ServerFormula) formula).getIntervalLength();
            //if no wildcards on interval in the requirements of the fomula
            if (!intervalLengthOfFormula.equals(IntervalLength.NOT_SUPPORTED)) {
                if (intervalLengthOfReadingType.ordinal() < intervalLengthOfFormula.ordinal()) {
                    throw new InvalidNodeException(metrologyConfigurationService.getThesaurus(), MessageSeeds.INTERVAL_OF_READINGTYPE_SHOULD_BE_GREATER_OR_EQUAL_TO_INTERVAL_OF_REQUIREMENTS);
                }
            }
        }*/
        ReadingTypeDeliverableImpl deliverable = this.metrologyConfigurationService.getDataModel().getInstance(ReadingTypeDeliverableImpl.class)
                .init(this, name, readingType, formula);
        Save.CREATE.validate(this.metrologyConfigurationService.getDataModel(), deliverable);
        this.deliverables.add(deliverable);
        touch();
        return deliverable;
    }


    @Override
    public void removeReadingTypeDeliverable(ReadingTypeDeliverable deliverable) {
        if (this.deliverables.remove(deliverable)) {
            ((ServerFormula) deliverable.getFormula()).delete();
            touch();
        }
    }

    @Override
    public List<ReadingTypeDeliverable> getDeliverables() {
        return Collections.unmodifiableList(this.deliverables);
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

    void addReadingTypeRequirement(ReadingTypeRequirement readingTypeRequirement) {
        Save.CREATE.validate(this.metrologyConfigurationService.getDataModel(), readingTypeRequirement);
        this.readingTypeRequirements.add(readingTypeRequirement);
        touch();
    }

    @Override
    public void delete() {
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

}
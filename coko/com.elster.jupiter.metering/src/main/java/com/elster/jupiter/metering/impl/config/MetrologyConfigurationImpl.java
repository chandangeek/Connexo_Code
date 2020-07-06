/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.AllowedChars;
import com.elster.jupiter.domain.util.HasOnlyWhiteListedCharacters;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
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
import com.elster.jupiter.metering.impl.PrivateMessageSeeds;
import com.elster.jupiter.metering.impl.TableSpecs;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.InvalidateCacheRequest;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.util.ShouldHaveUniqueName;
import com.elster.jupiter.util.UniqueName;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.domain.util.Save.CREATE;
import static com.elster.jupiter.domain.util.Save.UPDATE;
import static com.elster.jupiter.util.conditions.Where.where;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + PrivateMessageSeeds.Constants.OBJECT_MUST_HAVE_UNIQUE_NAME + "}")
@DeliverableTimeOfUseBucketsBackedByEventSet(groups = MetrologyConfigurationImpl.Activation.class)
public class MetrologyConfigurationImpl implements ServerMetrologyConfiguration, ShouldHaveUniqueName {
    public static final String TYPE_IDENTIFIER = "B";

    public static final Map<String, Class<? extends MetrologyConfiguration>> IMPLEMENTERS = ImmutableMap.of(
            MetrologyConfigurationImpl.TYPE_IDENTIFIER, MetrologyConfigurationImpl.class,
            UsagePointMetrologyConfigurationImpl.TYPE_IDENTIFIER, UsagePointMetrologyConfigurationImpl.class);

    // Marker interface for javax.validation group
    public interface Activation {}

    public enum Fields {
        NAME("name"),
        DESCRIPTION("description"),
        ALLOW_GAPS("gapsAllowed"),
        STATUS("status"),
        SERVICECATEGORY("serviceCategory"),
        CUSTOM_PROPERTY_SETS("customPropertySets"),
        RT_REQUIREMENTS("readingTypeRequirements"),
        METROLOGY_CONTRACTS("metrologyContracts"),
        METER_ROLES("meterRoles"),
        DELIVERABLES("deliverables"),
        REQUIREMENT_TO_ROLE_REFERENCES("requirementToRoleUsages"),
        USAGE_POINT_REQUIREMENTS("usagePointRequirements"),
        OBSOLETETIME("obsoleteTime"),;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final DataModel dataModel;
    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    private final EventService eventService;
    private final Clock clock;
    private final Publisher publisher;

    @SuppressWarnings("unused")
    private long id;
    @NotEmpty(message = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + PrivateMessageSeeds.Constants.FIELD_TOO_LONG + "}")
    @HasOnlyWhiteListedCharacters(whitelistRegex = AllowedChars.Constant.ALLOWED_CHARS_WITH_SPACE)
    private String name;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{" + PrivateMessageSeeds.Constants.FIELD_TOO_LONG + "}")
    @HasOnlyWhiteListedCharacters(whitelistRegex = AllowedChars.Constant.ALLOWED_SPECIAL_CHARS)
    private String description;
    private boolean gapsAllowed;
    @NotNull
    private MetrologyConfigurationStatus status = MetrologyConfigurationStatus.INACTIVE;
    @IsPresent(message = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    private Reference<ServiceCategory> serviceCategory = ValueReference.absent();
    @Valid
    private List<MetrologyConfigurationCustomPropertySetUsage> customPropertySets = new ArrayList<>();
    private List<ReadingTypeRequirement> readingTypeRequirements = new ArrayList<>();
    private List<ServerMetrologyContract> metrologyContracts = new ArrayList<>();
    private List<EventSetOnMetrologyConfiguration> eventSets = new ArrayList<>();
    @Deprecated // up to version 10.3
    private List<ReadingTypeDeliverable> deliverables;

    private Instant obsoleteTime;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private static final Logger LOG = Logger.getLogger(MetrologyConfigurationImpl.class.getName());

    @Inject
    MetrologyConfigurationImpl(DataModel dataModel, ServerMetrologyConfigurationService metrologyConfigurationService, EventService eventService, Clock clock, Publisher publisher) {
        this.dataModel = dataModel;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.eventService = eventService;
        this.clock = clock;
        this.publisher = publisher;
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
    public boolean areGapsAllowed() {
        return gapsAllowed;
    }

    public void setGapsAllowed(boolean gapAllowed){
        this.gapsAllowed = gapAllowed;
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
        Optional<EffectiveMetrologyConfigurationOnUsagePoint> linkedUsagePoint = metrologyConfigurationService.getDataModel()
                .query(EffectiveMetrologyConfigurationOnUsagePoint.class, MetrologyConfiguration.class)
                .select(where("metrologyConfiguration").isEqualTo(this)
                        .and(where("interval").isEffective(Range.atLeast(clock.instant()))))
                .stream()
                .filter(emc -> !emc.getRange().hasUpperBound() || !emc.getRange().lowerEndpoint().equals(emc.getRange().upperEndpoint()))
                .findAny();
        if (linkedUsagePoint.isPresent()) {
            throw new CannotDeactivateMetrologyConfiguration(this.metrologyConfigurationService.getThesaurus());
        }
    }

    @Override
    public void activate() {
        if (MetrologyConfigurationStatus.INACTIVE == status) {
            this.status = MetrologyConfigurationStatus.ACTIVE;
            Save.UPDATE.validate(this.dataModel, this, Activation.class);
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
                .noneMatch(cpsUsage -> cpsUsage.getRegisteredCustomPropertySet().getId() == registeredCustomPropertySet.getId())) {
            MetrologyConfigurationCustomPropertySetUsageImpl newCpsUsage =
                    this.metrologyConfigurationService.getDataModel()
                            .getInstance(MetrologyConfigurationCustomPropertySetUsageImpl.class)
                            .init(this, registeredCustomPropertySet);
            customPropertySets.add(newCpsUsage);
            if (this.id > 0) {
                touch();
            }
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
        MetrologyContractImpl metrologyContract = this.metrologyConfigurationService.getDataModel()
                .getInstance(MetrologyContractImpl.class)
                .init(this, metrologyPurpose);
        metrologyContract.setMandatory(mandatory);
        Save.CREATE.validate(this.metrologyConfigurationService.getDataModel(), metrologyContract);
        this.doAddMetrologyContract(metrologyContract);
        touch();
        return metrologyContract;
    }

    void doAddMetrologyContract(ServerMetrologyContract contract) {
        this.metrologyContracts.add(contract);
    }

    @Override
    public void removeMetrologyContract(MetrologyContract metrologyContract) {
        ((ServerMetrologyContract) metrologyContract).delete();
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
        this.metrologyContracts.forEach(ServerMetrologyContract::delete);
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
    public boolean hasUniqueName() {
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

    @Override
    public void makeObsolete() {
        if (isActive()) {
            deactivate();
        }
        this.obsoleteTime = this.clock.instant();
        this.dataModel.update(this, "obsoleteTime");
        invalidateCache();
    }

    @Override
    public Optional<Instant> getObsoleteTime() {
        return Optional.ofNullable(this.obsoleteTime);
    }

    @Override
    public void invalidateCache() {
        this.publisher.publish(new InvalidateCacheRequest(MeteringService.COMPONENTNAME, TableSpecs.MTR_METROLOGYCONFIG.name()));
    }

    @Override
    public List<EventSet> getEventSets() {
        return this.eventSets
                .stream()
                .map(EventSetOnMetrologyConfiguration::getEventSet)
                .collect(Collectors.toList());
    }

    @Override
    public void addEventSet(EventSet eventSet) {
        this.doAddEventSet(EventSetOnMetrologyConfigurationImpl.from(this.dataModel, this, eventSet));
    }

    void doAddEventSet(EventSetOnMetrologyConfiguration eventSet) {
        this.eventSets.add(eventSet);
    }

    @Override
    public void removeEventSet(EventSet eventSet) {
        this.eventSets
                .stream()
                .filter(each -> each.getEventSet().equals(eventSet))
                .findAny()
                .ifPresent(this.eventSets::remove);
    }

}

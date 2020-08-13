/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.DeliverableType;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementsCollector;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.PrivateMessageSeeds;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class MetrologyContractImpl implements ServerMetrologyContract {

    public enum Fields {
        METROLOGY_CONFIG("metrologyConfiguration"),
        METROLOGY_PURPOSE("metrologyPurpose"),
        MANDATORY("mandatory"),
        DELIVERABLES("deliverables"),;

        private String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    private final CustomPropertySetService customPropertySetService;
    private final EventService eventService;

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    @IsPresent(message = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    private final Reference<ServerMetrologyConfiguration> metrologyConfiguration = ValueReference.absent();
    @IsPresent(message = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    private final Reference<MetrologyPurpose> metrologyPurpose = ValueReference.absent();
    private boolean mandatory;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    private List<ServerReadingTypeDeliverable> deliverables = new ArrayList<>();

    @Inject
    public MetrologyContractImpl(ServerMetrologyConfigurationService metrologyConfigurationService, CustomPropertySetService customPropertySetService, EventService eventService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.customPropertySetService = customPropertySetService;
        this.eventService = eventService;
    }

    public MetrologyContractImpl init(ServerMetrologyConfiguration meterConfiguration, MetrologyPurpose metrologyPurpose) {
        this.metrologyConfiguration.set(meterConfiguration);
        this.metrologyPurpose.set(metrologyPurpose);
        return this;
    }

    private void touch() {
        this.metrologyConfigurationService.getDataModel().touch(this.getMetrologyConfiguration());
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public MetrologyConfiguration getMetrologyConfiguration() {
        return this.metrologyConfiguration.orNull();
    }

    @Override
    public ReadingTypeDeliverableBuilder newReadingTypeDeliverable(String name, ReadingType readingType, Formula.Mode mode) {
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
    public ReadingTypeDeliverableBuilder newReadingTypeDeliverable(String name, DeliverableType type, ReadingType readingType, Formula.Mode mode) {
        return new ReadingTypeDeliverableBuilderImpl(
                this,
                name,
                type,
                readingType,
                mode,
                this.customPropertySetService,
                this.metrologyConfigurationService.getDataModel(),
                this.metrologyConfigurationService.getThesaurus());
    }

    @Override
    public ReadingTypeDeliverable addDeliverable(String name, DeliverableType deliverableType, ReadingType readingType, Formula formula) {
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
    public void removeDeliverable(ReadingTypeDeliverable deliverableForRemove) {
        if (this.doRemoveDeliverable((ServerReadingTypeDeliverable) deliverableForRemove)) {
            this.touch();
        }
    }

    public boolean doRemoveDeliverable(ServerReadingTypeDeliverable deliverableForRemove) {
        if (!metrologyConfigurationService.getDataModel()
                .query(ReadingTypeDeliverableNodeImpl.class)
                .select(where("readingTypeDeliverable").isEqualTo(deliverableForRemove))
                .isEmpty()) {
            throw new CannotDeleteReadingTypeDeliverableException(metrologyConfigurationService.getThesaurus(), deliverableForRemove.getName());
        }
        if (this.deliverables.contains(deliverableForRemove)) {
            deliverableForRemove.prepareDelete();
            if (this.deliverables.remove(deliverableForRemove)) {
                this.eventService.postEvent(EventType.READING_TYPE_DELIVERABLE_DELETED.topic(), deliverableForRemove);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ReadingTypeDeliverable> getDeliverables() {
        return ImmutableList.copyOf(this.deliverables);
    }

    @Override
    public MetrologyPurpose getMetrologyPurpose() {
        return this.metrologyPurpose.orNull();
    }

    @Override
    public boolean isMandatory() {
        return this.mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void update() {
        if (this.getId() > 0) {
            this.metrologyConfigurationService.getDataModel().touch(this);
        }
    }

    @Override
    public void delete() {
        if (this.getId() > 0) {
            new ArrayList<>(this.deliverables).forEach(this::doRemoveDeliverable);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetrologyContractImpl that = (MetrologyContractImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.id);
    }

    @Override
    public Set<ReadingTypeRequirement> getRequirements() {
        ReadingTypeRequirementsCollector readingTypeRequirementsCollector = new ReadingTypeRequirementsCollector();
        getDeliverables().stream()
                .map(ReadingTypeDeliverable::getFormula)
                .map(Formula::getExpressionNode)
                .forEach(expressionNode -> expressionNode.accept(readingTypeRequirementsCollector));
        return readingTypeRequirementsCollector.getReadingTypeRequirements().stream().collect(Collectors.toSet());
    }

    @Override
    public Collection<Set<ReadingType>> sortReadingTypesByDependencyLevel() {
        List<ReadingTypeDeliverable> deliverables = getDeliverables();
        Map<ReadingType, Integer> readingTypesWithDependencyLevels = new HashMap<>(deliverables.size(), 1);
        deliverables.forEach(deliverable -> readingTypesWithDependencyLevels.computeIfAbsent(deliverable.getReadingType(),
                readingType -> deliverable.getFormula().getExpressionNode()
                        .accept(new DeliverableDependencyLevelRetriever(readingTypesWithDependencyLevels))));
        return readingTypesWithDependencyLevels.entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue, TreeMap::new, Collectors.mapping(Map.Entry::getKey, Collectors.toSet())))
                .values();
    }

    @Override
    public List<ReadingType> sortReadingTypesByDependency() {
        return DependencyAnalyzer.forAnalysisOf(this)
                .getDeliverables().stream()
                .map(ReadingTypeDeliverable::getReadingType)
                .collect(Collectors.toList());
    }

    void deliverableUpdated(ReadingTypeDeliverableImpl deliverable) {
        this.touch();
    }

}
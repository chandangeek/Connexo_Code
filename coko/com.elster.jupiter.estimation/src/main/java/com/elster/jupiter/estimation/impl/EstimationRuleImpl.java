package com.elster.jupiter.estimation.impl;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.estimation.EstimationRuleProperties;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.MessageSeeds.Constants;
import com.elster.jupiter.estimation.ReadingTypeInEstimationRule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.google.common.collect.ImmutableMap;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.DUPLICATE_ESTIMATION_RULE + "}")
@HasValidProperties(groups = {Save.Create.class, Save.Update.class})
class EstimationRuleImpl implements IEstimationRule {
    private long id;

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.NAME_REQUIRED_KEY + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;
    private boolean active;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.NAME_REQUIRED_KEY + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    @ExistingEstimator(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.NO_SUCH_ESTIMATOR + "}")
    private String implementation; //estimator class name
    private Instant obsoleteTime;

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;
    // associations
    @Valid
    @Size(min=1, groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.NAME_REQUIRED_KEY + "}")
    private List<ReadingTypeInEstimationRule> readingTypesInRule = new ArrayList<>();

    private Reference<EstimationRuleSet> ruleSet = ValueReference.absent();

    @SuppressWarnings("unused")
    private int position;
    private transient Estimator templateEstimator;

    private List<EstimationRuleProperties> properties = new ArrayList<>();

    private final DataModel dataModel;
    private final EstimatorCreator estimatorCreator;
    private final Thesaurus thesaurus;
    private final MeteringService meteringService;
    private final EventService eventService;
    private final Provider<ReadingTypeInEstimationRuleImpl> readingTypeInRuleProvider;

    @Inject
    EstimationRuleImpl(DataModel dataModel, EstimatorCreator estimatorCreator, Thesaurus thesaurus, MeteringService meteringService, EventService eventService, Provider<ReadingTypeInEstimationRuleImpl> readingTypeInRuleProvider) {
        this.dataModel = dataModel;
        this.estimatorCreator = estimatorCreator;
        this.thesaurus = thesaurus;
        this.meteringService = meteringService;
        this.eventService = eventService;
        this.readingTypeInRuleProvider = readingTypeInRuleProvider;
    }

    EstimationRuleImpl init(EstimationRuleSet ruleSet, String implementation, String name) {
        this.ruleSet.set(ruleSet);
        this.implementation = implementation;
        this.name = name != null ? name.trim() : name;
        this.active = false;
        return this;
    }

    @Override
    public void activate() {
        setActive(true);
    }

    @Override
    public EstimationRuleProperties addProperty(String name, Object value) {
        EstimationRulePropertiesImpl newProperty = new EstimationRulePropertiesImpl().init(this, name, value);
        properties.add(newProperty);
        return newProperty;
    }

    public void setProperties(Map<String, Object> propertyMap) {
        DiffList<EstimationRuleProperties> entryDiff = ArrayDiffList.fromOriginal(getProperties());
        entryDiff.clear();
        List<EstimationRuleProperties> newProperties = new ArrayList<>();
        for (Map.Entry<String, Object> property : propertyMap.entrySet()) {
            EstimationRulePropertiesImpl newProperty = new EstimationRulePropertiesImpl().init(this, property.getKey(), property.getValue());
            newProperties.add(newProperty);
        }

        entryDiff.addAll(newProperties);
        for (EstimationRuleProperties property : entryDiff.getRemovals()) {
            properties.remove(property);
        }

        for (EstimationRuleProperties property : entryDiff.getRemaining()) {
            property.setValue(propertyMap.get(property.getName()));
            rulePropertiesFactory().update(property);
            properties.stream().filter(p -> p.getName().equals(property.getName())).findFirst().ifPresent(p -> p.setValue(propertyMap.get(property.getName())));
        }
        for (EstimationRuleProperties property : entryDiff.getAdditions()) {
            properties.add(property);
        }
    }

    @Override
    public ReadingTypeInEstimationRule addReadingType(ReadingType readingType) {
        ReadingTypeInEstimationRuleImpl readingTypeInEstimationRule = readingTypeInRuleProvider.get().init(this, readingType);
        readingTypesInRule.add(readingTypeInEstimationRule);
        return readingTypeInEstimationRule;
    }

    @Override
    public ReadingTypeInEstimationRule addReadingType(String mRID) {
        return meteringService.getReadingType(mRID)
                .map(this::addReadingType)
                .orElseGet(() -> readingTypeInEstimationRuleFor(mRID));
    }

    private ReadingTypeInEstimationRule readingTypeInEstimationRuleFor(String mRID) {
        ReadingTypeInEstimationRuleImpl empty = readingTypeInRuleProvider.get().init(this, mRID);
        if (getId() != 0) {
            Save.UPDATE.validate(dataModel, empty);
        }
        readingTypesInRule.add(empty);
        return empty;
    }

    @Override
    public void deactivate() {
        setActive(false);
    }

    public void delete() {
        this.setObsoleteTime(Instant.now()); // mark obsolete
        doUpdate();
//        eventService.postEvent(EventType.ESTIMATIONRULE_DELETED.topic(), this);
    }

    void deleteProperty(EstimationRuleProperties property) {
        properties.remove(property);
    }

    @Override
    public void deleteReadingType(ReadingType readingType) {
        getReadingTypeInRule(readingType).ifPresent(readingTypesInRule::remove);
    }

    @Override
    public boolean isRequired(final String propertyKey) {
        return getEstimator().getPropertySpecs().stream()
                .filter(PropertySpec::isRequired)
                .anyMatch(spec -> propertyKey.equals(spec.getName()));
    }

    private Estimator getEstimator() {
        if (templateEstimator == null) {
            templateEstimator = estimatorCreator.getTemplateEstimator(this.implementation);
        }
        return templateEstimator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return id == ((EstimationRuleImpl) o).id;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getImplementation() {
        return implementation;
    }

    @Override
    public String getDisplayName() {
        return getEstimator().getDisplayName();
    }

    @Override
    public List<EstimationRuleProperties> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    @Override
    public Map<String, Object> getProps() {
        return getProperties().stream()
                .filter(rule -> rule.getValue() != null)
                .collect(Collectors.toMap(EstimationRuleProperties::getName, EstimationRuleProperties::getValue));
    }

    private Optional<ReadingTypeInEstimationRule> getReadingTypeInRule(ReadingType readingType) {
        return readingTypesInRule.stream()
                .filter(inRule -> inRule.getReadingType().equals(readingType))
                .findFirst();
    }

    @Override
    public Set<ReadingType> getReadingTypes() {
        return readingTypesInRule.stream()
                .map(ReadingTypeInEstimationRule::getReadingType)
                .collect(Collectors.toSet());
    }

    @Override
    public EstimationRuleSet getRuleSet() {
        return ruleSet.get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return getEstimator().getPropertySpecs();
    }

    public PropertySpec getPropertySpec(final String name) {
        return getEstimator().getPropertySpecs().stream()
                .filter(p -> name.equals(p.getName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean isObsolete() {
        return getObsoleteDate() != null;
    }

    public void save() {
        if (getId() == 0) {
            this.getEstimator().validateProperties(getPropertiesAsMap(this.properties));
            doPersist();
            return;
        }
        Save.UPDATE.validate(dataModel, this);
        this.getEstimator().validateProperties(getPropertiesAsMap(this.properties));
        doUpdate();
    }

    public void rename(String name) {
        this.name = name != null ? name.trim() : name;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getImplementation()).append(' ').append(isActive());
        properties.forEach( p -> builder.append(p.toString()).append('\n'));
        return builder.toString();
    }

    @Override
    public void toggleActivation() {
        if (active) {
            deactivate();
        } else {
            activate();
        }
    }

    @Override
    public void clearReadingTypes() {
        readingTypesInRule.clear();
    }

    @Override
    public Instant getObsoleteDate() {
        return getObsoleteTime() != null ? getObsoleteTime() : null;
    }

    private Instant getObsoleteTime() {
        return this.obsoleteTime;
    }

    private void setObsoleteTime(Instant obsoleteTime) {
        this.obsoleteTime = obsoleteTime;
    }

    @Override
    public long getVersion() {
        return version;
    }

    Instant getCreateTime() {
        return createTime;
    }

    Instant getModTime() {
        return modTime;
    }

    String getUserName() {
        return userName;
    }

    @Override
    public Estimator createNewEstimator() {
        return new ReadingQualityTypedEstimator(createBaseEstimator(), getReadingQualityType());
    }

    private Estimator createBaseEstimator() {
        return estimatorCreator.getEstimator(this.implementation, getProps());
    }

    private ReadingQualityType getReadingQualityType() {
        return ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, (int) getId());
    }

    private void doPersist() {
        Save.CREATE.save(dataModel, this);
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
    }

    private DataMapper<EstimationRuleProperties> rulePropertiesFactory() {
        return dataModel.mapper(EstimationRuleProperties.class);
    }

    private void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDisplayName(String name) {
        return getEstimator().getDisplayName(name);
    }

    @Override
    public boolean appliesTo(Channel channel) {
        return isActive() && getReadingTypes().stream().anyMatch(channel::hasReadingType);
    }

    private Map<String, Object> getPropertiesAsMap(List<EstimationRuleProperties> properties) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        properties.stream().forEach(property -> builder.put(property.getName(), property.getValue()));
        return builder.build();
    }

}

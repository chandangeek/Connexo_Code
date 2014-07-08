package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ProcesStatus;
import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ReadingTypeInValidationRule;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorNotFoundException;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.elster.jupiter.validation.MessageSeeds.Constants;

@XmlRootElement
@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.DUPLICATE_VALIDATION_RULE + "}")
public final class ValidationRuleImpl implements ValidationRule, IValidationRule {
    private long id;

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.NAME_REQUIRED_KEY + "}")
    @Size(min = 1, max = 80, groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;
    private boolean active;
    private ValidationAction action;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.NAME_REQUIRED_KEY + "}")
    @Size(min = 1, max = 80, groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    @ExistingValidator(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.NO_SUCH_VALIDATOR + "}")
    private String implementation; //validator classname
    private UtcInstant obsoleteTime;

    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    private String userName;
    // associations
    @Valid
    private List<ReadingTypeInValidationRule> readingTypesInRule = new ArrayList<>();

    private long ruleSetId;

    @SuppressWarnings("unused")
    private int position;
    private transient ValidationRuleSet ruleSet;
    private transient Validator validator;

    private List<ValidationRuleProperties> properties = new ArrayList<>();
    private final DataModel dataModel;
    private final ValidatorCreator validatorCreator;
    private final Thesaurus thesaurus;
    private final MeteringService meteringService;
    private final EventService eventService;

    @Inject
    ValidationRuleImpl(DataModel dataModel, ValidatorCreator validatorCreator, Thesaurus thesaurus, MeteringService meteringService, EventService eventService) {
        this.dataModel = dataModel;
        this.validatorCreator = validatorCreator;
        this.thesaurus = thesaurus;
        this.meteringService = meteringService;
        this.eventService = eventService;
    }

    ValidationRuleImpl init(ValidationRuleSet ruleSet, ValidationAction action, String implementation, int position, String name) {
        this.ruleSet = ruleSet;
        this.action = action;
        this.implementation = implementation;
        this.position = position;
        this.ruleSetId = ruleSet.getId();
        this.name = name.trim();
        this.active = false;
        return this;
    }

    static ValidationRuleImpl from(DataModel dataModel, ValidationRuleSet ruleSet, ValidationAction action, String implementation, int position, String name) {
        return dataModel.getInstance(ValidationRuleImpl.class).init(ruleSet, action, implementation, position, name);
    }

    @Override
    public void activate() {
        setActive(true);
    }

    @Override
    public ValidationRuleProperties addProperty(String name, Quantity value) {
        ValidationRulePropertiesImpl newProperty = ValidationRulePropertiesImpl.from(dataModel, this, name, value);
        properties.add(newProperty);
        return newProperty;
    }

    @Override
    public void setProperties(Map<String, Quantity> propertyMap) {
        DiffList<ValidationRuleProperties> entryDiff = ArrayDiffList.fromOriginal(getProperties());
        entryDiff.clear();
        List<ValidationRuleProperties> newProperties = new ArrayList<>();
        for (Map.Entry<String, Quantity> property : propertyMap.entrySet()) {
            ValidationRulePropertiesImpl newProperty = ValidationRulePropertiesImpl.from(dataModel, this, property.getKey(), property.getValue());
            newProperties.add(newProperty);
        }

        entryDiff.addAll(newProperties);
        for (ValidationRuleProperties property : entryDiff.getRemovals()) {
            properties.remove(property);
        }

        for (ValidationRuleProperties property : entryDiff.getRemaining()) {
            property.setValue(propertyMap.get(property.getName()));
            rulePropertiesFactory().update(property);
        }
        for (ValidationRuleProperties property : entryDiff.getAdditions()) {
            properties.add(property);
        }
    }

    @Override
    public ReadingTypeInValidationRule addReadingType(ReadingType readingType) {
        ReadingTypeInValidationRuleImpl readingTypeInValidationRule = ReadingTypeInValidationRuleImpl.from(dataModel, this, readingType);
        readingTypesInRule.add(readingTypeInValidationRule);
        return readingTypeInValidationRule;
    }

    @Override
    public ReadingTypeInValidationRule addReadingType(String mRID) {
        Optional<ReadingType> optional = meteringService.getReadingType(mRID);
        if (!optional.isPresent()) {
            ReadingTypeInValidationRuleImpl empty = ReadingTypeInValidationRuleImpl.from(dataModel, this, mRID);
            if (getId() != 0) {
                Save.UPDATE.validate(dataModel, empty);
            }
            readingTypesInRule.add(empty);
            return empty;
        }
        return addReadingType(optional.get());
    }

    @Override
    public void deactivate() {
        setActive(false);
    }

    public void delete() {
        this.setObsoleteTime(new UtcInstant(new Date())); // mark obsolete
        ruleFactory().update(this);
        eventService.postEvent(EventType.VALIDATIONRULE_DELETED.topic(), this);
    }

    @Override
    public void deleteProperty(ValidationRuleProperties property) {
        properties.remove(property);
    }

    @Override
    public void deleteReadingType(ReadingType readingType) {
        ReadingTypeInValidationRule readingTypeInValidationRule = getReadingTypeInRule(readingType);
        if (readingTypeInValidationRule != null) {
            readingTypesInRule.remove(readingTypeInValidationRule);
        }
    }

    @Override
    public boolean isRequired(String propertyKey) {
        return getValidator().getRequiredKeys().contains(propertyKey);
    }

    private Validator getValidator() {
        if (validator == null) {
            validator = validatorCreator.getTemplateValidator(this.implementation);
        }
        return validator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return id == ((ValidationRuleImpl) o).id;
    }

    @Override
    public ValidationAction getAction() {
        return action;
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
        return getValidator().getDisplayName();
    }

    @Override
    public List<ValidationRuleProperties> getProperties() {
        List<ValidationRuleProperties> propertyList = new ArrayList<>();
        for (ValidationRuleProperties validationRuleProperties : properties) {
            propertyList.add(validationRuleProperties);
        }
        return propertyList;
        //return Collections.unmodifiableList(properties);
    }

    @Override
    public Map<String, Quantity> getProps() {
        ImmutableMap.Builder<String, Quantity> builder = ImmutableMap.builder();
        for (ValidationRuleProperties validationRuleProperties : getProperties()) {
            builder.put(validationRuleProperties.getName(), validationRuleProperties.getValue());
        }
        return builder.build();
    }

    private ReadingTypeInValidationRule getReadingTypeInRule(ReadingType readingType) {
        for (ReadingTypeInValidationRule readingTypeInValidationRule : readingTypesInRule) {
            if (readingTypeInValidationRule.getReadingType().equals(readingType)) {
                return readingTypeInValidationRule;
            }
        }
        return null;
    }

    @Override
    public Set<ReadingType> getReadingTypes() {
        Set<ReadingType> result = new HashSet<>();
        for (ReadingTypeInValidationRule readingTypeInRule : readingTypesInRule) {
            result.add(readingTypeInRule.getReadingType());
        }
        return result;
    }

    @Override
    public ValidationRuleSet getRuleSet() {
        if (ruleSet == null) {
            ruleSet = dataModel.mapper(ValidationRuleSet.class).getOptional(ruleSetId).get();
        }
        return ruleSet;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void save() {
        if (getId() == 0) {
            doPersist();
            return;
        }
        doUpdate();
    }

    @Override
    public void rename(String name) {
        this.name = name.trim();
    }

    @Override
    public void setAction(ValidationAction action) {
        this.action = action;
    }

    @Override
    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    @Override
    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getImplementation()).append(' ').append(getAction().name()).append(' ').append(isActive());
        for (ValidationRuleProperties property : properties) {
            builder.append(property.toString()).append('\n');
        }
        return builder.toString();
    }

    @Override
    public Date validateChannel(Channel channel, Interval interval) {
        if (!active) {
            return null;
        }
        return validateReadings(channel, interval);
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
    public Date getObsoleteDate() {
        return getObsoleteTime() != null ? getObsoleteTime().toDate() : null;
    }

    private UtcInstant getObsoleteTime() {
        return this.obsoleteTime;
    }

    private void setObsoleteTime(UtcInstant obsoleteTime) {
        this.obsoleteTime = obsoleteTime;
    }

    @Override
    public long getVersion() {
        return version;
    }

    UtcInstant getCreateTime() {
        return createTime;
    }

    UtcInstant getModTime() {
        return modTime;
    }

    String getUserName() {
        return userName;
    }

    void setRuleSetId(long ruleSetId) {
        this.ruleSetId = ruleSetId;
    }

    private Validator createNewValidator() {
        Validator validator = validatorCreator.getValidator(this.implementation, getProps());
        if (validator == null) {
            throw new ValidatorNotFoundException(thesaurus, implementation);
        }
        return validator;
    }

    private ReadingQualityType defaultReadingQualityType() {
        return new ReadingQualityType("3.6." + getId());
    }

    private void doPersist() {
        ruleFactory().persist(this);
    }

    private void doUpdate() {
        ruleFactory().update(this);
    }

    private Validator newValidator(Channel channel, Interval interval, ReadingType channelReadingType) {
        Validator validator = createNewValidator();
        validator.init(channel, channelReadingType, interval);
        return validator;
    }

    private DataMapper<ReadingTypeInValidationRule> readingTypesInRuleFactory() {
        return dataModel.mapper(ReadingTypeInValidationRule.class);
    }

    private DataMapper<IValidationRule> ruleFactory() {
        return dataModel.mapper(IValidationRule.class);
    }

    private DataMapper<ValidationRuleProperties> rulePropertiesFactory() {
        return dataModel.mapper(ValidationRuleProperties.class);
    }

    private void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String getName() {
        return name;
    }

    private Date validateReadings(Channel channel, Interval interval) {
        Date lastChecked = null;
        ListMultimap<Date, ReadingQuality> existingReadingQualities = getExistingReadingQualities(channel, interval);
        for (ReadingType channelReadingType : channel.getReadingTypes()) {
            if (getReadingTypes().contains(channelReadingType)) {
                Validator validator = newValidator(channel, interval, channelReadingType);

                ReadingQualityType readingQualityType = validator.getReadingQualityTypeCode().or(defaultReadingQualityType());
                if (channel.isRegular()) {
                    Interval intervalToRequest = interval.withStart(new Date(interval.getStart().getTime() - 1));
                    for (IntervalReadingRecord intervalReading : channel.getIntervalReadings(channelReadingType, intervalToRequest)) {
                        ValidationResult result = validator.validate(intervalReading);
                        lastChecked = handleValidationResult(result, channel, lastChecked, existingReadingQualities, readingQualityType, intervalReading);
                    }
                } else {
                    for (ReadingRecord readingRecord : channel.getRegisterReadings(channelReadingType, interval)) {
                        ValidationResult result = validator.validate(readingRecord);
                        lastChecked = handleValidationResult(result, channel, lastChecked, existingReadingQualities, readingQualityType, readingRecord);
                    }
                }
            }
        }
        return lastChecked;
    }

    private Date handleValidationResult(ValidationResult result, Channel channel, Date lastChecked, ListMultimap<Date, ReadingQuality> existingReadingQualities,
                                        ReadingQualityType readingQualityType, BaseReadingRecord readingRecord) {
        Date newLastChecked = lastChecked;
        Optional<ReadingQuality> existingQualityForType = getExistingReadingQualitiesForType(existingReadingQualities, readingQualityType, readingRecord.getTimeStamp());
        if (ValidationResult.SUSPECT.equals(result) && !existingQualityForType.isPresent()) {
            saveNewReadingQuality(channel, readingRecord, readingQualityType);
            readingRecord.setProcessingFlags(ProcesStatus.Flag.SUSPECT);
        }
        if (ValidationResult.PASS.equals(result) && existingQualityForType.isPresent()) {
            existingQualityForType.get().delete();
        }
        if (!ValidationResult.SKIPPED.equals(result)) {
            newLastChecked = lastChecked == null ? readingRecord.getTimeStamp() : Ordering.natural().max(lastChecked, readingRecord.getTimeStamp());
        }
        return newLastChecked;
    }

    private Optional<ReadingQuality> getExistingReadingQualitiesForType(ListMultimap<Date, ReadingQuality> existingReadingQualities, final ReadingQualityType readingQualityType, Date timeStamp) {
        List<ReadingQuality> iterable = existingReadingQualities.get(timeStamp);
        return iterable == null ? Optional.<ReadingQuality>absent() : Iterables.tryFind(iterable, new Predicate<ReadingQuality>() {
            @Override
            public boolean apply(ReadingQuality input) {
                return readingQualityType.equals(input.getType());
            }
        });
    }

    private ListMultimap<Date, ReadingQuality> getExistingReadingQualities(Channel channel, Interval interval) {
        List<ReadingQuality> readingQualities = channel.findReadingQuality(interval);
        return Multimaps.index(readingQualities, new Function<ReadingQuality, Date>() {
            @Override
            public Date apply(ReadingQuality input) {
                return input.getReadingTimestamp();
            }
        });
    }

    private void saveNewReadingQuality(Channel channel, BaseReadingRecord reading, ReadingQualityType readingQualityType) {
        ReadingQuality readingQuality = channel.createReadingQuality(readingQualityType, reading);
        readingQuality.save();
    }


    public String getDisplayName(String name) {
        return getValidator().getDisplayName(name);
    }
}

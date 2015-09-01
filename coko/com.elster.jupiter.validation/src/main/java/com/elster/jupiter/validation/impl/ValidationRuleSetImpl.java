package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.validation.MessageSeeds.Constants;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

@XmlRootElement
@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.DUPLICATE_VALIDATION_RULE_SET + "}")
public final class ValidationRuleSetImpl implements IValidationRuleSet {

    static final String OBSOLETE_TIME_FIELD = "obsoleteTime";

    private long id;
    private String mRID;

    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.NAME_REQUIRED_KEY + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;
    @Size(min = 0, max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String aliasName;
    @Size(min = 0, max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.FIELD_SIZE_BETWEEN_1_AND_4000 + "}")
    private String description;
    private Instant obsoleteTime;

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    @Valid
    private List<IValidationRuleSetVersion> versions = new ArrayList<>();
    private List<IValidationRule> rules = new ArrayList<>();
    private List<IValidationRuleSetVersion> versionToSave = new ArrayList<>();

    private final EventService eventService;
    private final DataModel dataModel;
    private final Provider<ValidationRuleSetVersionImpl> validationRuleSetVersionProvider;

    @Inject
    ValidationRuleSetImpl(DataModel dataModel, EventService eventService,  Provider<ValidationRuleSetVersionImpl> validationRuleSetValidationProvider) {
        // for persistence
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.validationRuleSetVersionProvider = validationRuleSetValidationProvider;
    }

    ValidationRuleSetImpl init(String name) {
    	return init(name,null);
    }

    ValidationRuleSetImpl init(String name, String description) {
        this.name = Checks.is(name).emptyOrOnlyWhiteSpace() ? null : name.trim();
        this.description = description;
        return this;
    }

    @Override
    public String getAliasName() {
        return aliasName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getMRID() {
        return mRID;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setMRID(String mRID) {
        this.mRID = mRID;
    }

    @Override
    public void setName(String name) {
        this.name = name != null ? name.trim() : name;
    }

    @Override
    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ValidationRuleSet)) {
            return false;
        }

        ValidationRuleSet validationRuleSet = (ValidationRuleSet) o;

        return id == validationRuleSet.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void save() {
        addNewVersion();
        if (getId() == 0) {
            doPersist();
        } else {
            doUpdate();
        }
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
        doGetVersions().forEach( version -> version.save());
        eventService.postEvent(EventType.VALIDATIONRULESET_UPDATED.topic(), this);
    }

    private void doPersist() {
        Save.CREATE.save(dataModel, this);
        eventService.postEvent(EventType.VALIDATIONRULESET_CREATED.topic(), this);
    }

    @Override
    public void delete() {
        this.setObsoleteTime(Instant.now()); // mark obsolete
        doGetVersions().forEach(version -> version.delete());
        validationRuleSetFactory().update(this);
        eventService.postEvent(EventType.VALIDATIONRULESET_DELETED.topic(), this);
    }

    @Override
    public List<IValidationRule> getRules() {
        return doGetRules()
        	.sorted(Comparator.comparing(rule -> rule.getName().toUpperCase()))
        	.collect(Collectors.toList());
    }

    @Override
    public List<IValidationRuleSetVersion> getRuleSetVersions() {
        List<IValidationRuleSetVersion> versions = doGetVersions()
                .sorted(Comparator.comparing(ver -> ver.getNotNullStartDate()))
                .collect(Collectors.toList());
        updateVersionsEndDate(versions);
        return versions;
    }

    private Stream<IValidationRuleSetVersion> doGetVersions() {
        return versions.stream().filter(version -> !version.isObsolete());
    }

    private void addNewVersion() {
        versionToSave.forEach( newVersion -> {
            newVersion.save();
            versions.add(newVersion);
        });
        versionToSave.clear();
    }

    private Stream<IValidationRule> doGetRules() {
        return getRuleSetVersions()
                .stream()
                .filter(v -> !v.isObsolete())
                .flatMap(v1 -> v1.getRules()
                        .stream()
                        .filter(r -> !r.isObsolete()));
    }


    @Override
    public List<IValidationRuleSetVersion> getRuleSetVersions(int start, int limit) {
        List<IValidationRuleSetVersion> versions = getRuleSetVersionQuery().select(
                Where.where("ruleSet").isEqualTo(this),
                new Order[]{Order.ascending("startDate").toUpperCase().nullsFirst()},
                false,
                new String[]{},
                start + 1,
                start + limit);
        updateVersionsEndDate(versions);

        return Collections.unmodifiableList(
                versions.stream()
                        .sorted(Comparator.comparing(IValidationRuleSetVersion::getNotNullStartDate).reversed())
                        .collect(Collectors.toList()));
    }

    private void updateVersionsEndDate(List<IValidationRuleSetVersion> versions) {
        versions.stream()
            .sequential()
                .reduce((a, b) -> {
                    a.setEndDate(b.getStartDate()); // set End Date;
                    return b;
                });
    }

    private QueryExecutor<IValidationRuleSetVersion> getRuleSetVersionQuery() {
        QueryExecutor<IValidationRuleSetVersion> ruleQuery = dataModel.query(IValidationRuleSetVersion.class);
        ruleQuery.setRestriction(where("obsoleteTime").isNull());
        return ruleQuery;
    }


    @Override
    public IValidationRuleSetVersion addRuleSetVersion(String description, Instant startDate) {
        ValidationRuleSetVersionImpl newRuleSetVersion = validationRuleSetVersionProvider.get().init(this, description, startDate);
        versionToSave.add(newRuleSetVersion);
        return newRuleSetVersion;
    }

    @Override
    public IValidationRuleSetVersion updateRuleSetVersion(long id, String description, Instant startDate) {
        IValidationRuleSetVersion version = getExistingVersion(id);
        return doUpdateVersion(version, description, startDate);
    }

    @Override
    public void deleteRuleSetVersion(ValidationRuleSetVersion version) {
        IValidationRuleSetVersion iVersion = (IValidationRuleSetVersion) version;
        if (doGetVersions().anyMatch( candidate -> candidate.equals(iVersion))) {
            iVersion.delete();
        } else {
            throw new IllegalArgumentException("The ruleset " + this.getId() + " doesn't contain provided rule set version Id: " + version.getId());
        }
    }
    @Override
    public ValidationRuleSetVersion cloneRuleSetVersion(long ruleSetVersionId, String description, Instant startDate){

        IValidationRuleSetVersion existingVersion = doGetVersions()
                .filter(v -> v.getId() == ruleSetVersionId)
                .findFirst().orElseThrow(() ->
                        new IllegalArgumentException("The ruleset " + this.getId() + " doesn't contain provided rule set version Id: " + ruleSetVersionId));

        IValidationRuleSetVersion clonedVersion = addRuleSetVersion(description, startDate);
        existingVersion
            .getRules()
            .stream()
            .forEach(clonedVersion::cloneRule);
        return clonedVersion;
    }


    private IValidationRuleSetVersion doUpdateVersion(IValidationRuleSetVersion version, String description, Instant startDate) {
        version.setDescription(description);
        version.setStartDate(startDate);
        return version;
    }

    private IValidationRuleSetVersion getExistingVersion(long id) {
        return doGetVersions()
                .filter(version -> version.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The ruleset " + this.getId() + " doesn't contain provided rule set version Id: " + id));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getName());
        builder.append('\n');
        doGetRules().forEach(rule -> builder.append(rule.toString()).append('\n'));
        return builder.toString();
    }

    private DataMapper<IValidationRuleSet> validationRuleSetFactory() {
        return dataModel.mapper(IValidationRuleSet.class);
    }

    public List<ValidationRule> getRules(Iterable<? extends ReadingType> readingTypes) {
        List<ValidationRule> result = new ArrayList<>();
        List<IValidationRule> rules = getRules();
        for (ValidationRule rule : rules) {
            Set<ReadingType> readingTypesForRule = rule.getReadingTypes();
            for (ReadingType readingtype : readingTypes) {
                if (readingTypesForRule.contains(readingtype)) {
                    result.add(rule);
                }
            }
        }
        return result;
    }

    @Override
    public Instant getObsoleteDate() {
        return this.obsoleteTime;
    }

    private void setObsoleteTime(Instant obsoleteTime) {
        this.obsoleteTime = obsoleteTime;
    }


}

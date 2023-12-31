/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.HasNoBlacklistedCharacters;
import com.elster.jupiter.domain.util.HasNotAllowedChars;
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
import com.elster.jupiter.validation.EventType;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

@XmlRootElement
@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DUPLICATE_VALIDATION_RULE_SET + "}")
public final class ValidationRuleSetImpl implements IValidationRuleSet {

    static final String OBSOLETE_TIME_FIELD = "obsoleteTime";

    private long id;
    private String mRID;

    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    @HasNoBlacklistedCharacters(balcklistedCharRegEx = HasNotAllowedChars.Constant.SPECIAL_CHARS)
    private String name;
    @Size(min = 0, max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String aliasName;
    @Size(min = 0, max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_4000 + "}")
    @HasNoBlacklistedCharacters(balcklistedCharRegEx = HasNotAllowedChars.Constant.SCRIPT_CHARS)
    private String description;
    private Instant obsoleteTime;
    @NotNull(groups = {Save.Create.class, Save.Update.class})
    private QualityCodeSystem qualityCodeSystem;

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    @Valid
    private List<IValidationRuleSetVersion> versions = new ArrayList<>();

    private final EventService eventService;
    private final DataModel dataModel;
    private final Provider<ValidationRuleSetVersionImpl> validationRuleSetVersionProvider;
    private final Clock clock;

    @Inject
    ValidationRuleSetImpl(DataModel dataModel, EventService eventService, Provider<ValidationRuleSetVersionImpl> validationRuleSetValidationProvider, Clock clock) {
        // for persistence
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.validationRuleSetVersionProvider = validationRuleSetValidationProvider;
        this.clock = clock;
    }

    ValidationRuleSetImpl init(String name, QualityCodeSystem qualityCodeSystem) {
        return init(name, qualityCodeSystem, null);
    }

    ValidationRuleSetImpl init(String name, QualityCodeSystem qualityCodeSystem, String description) {
        this.name = Checks.is(name).emptyOrOnlyWhiteSpace() ? null : name.trim();
        this.qualityCodeSystem = qualityCodeSystem;
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
    public QualityCodeSystem getQualityCodeSystem() {
        return this.qualityCodeSystem;
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
        this.name = name != null ? name.trim() : null;
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
        if (getId() == 0) {
            doPersist();
        } else {
            doUpdate();
        }
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
        doGetVersions().forEach(ValidationRuleSetVersion::save);
        eventService.postEvent(EventType.VALIDATIONRULESET_UPDATED.topic(), this);
    }

    private void doPersist() {
        Save.CREATE.save(dataModel, this);
        eventService.postEvent(EventType.VALIDATIONRULESET_CREATED.topic(), this);
    }

    @Override
    public void delete() {
        this.setObsoleteTime(Instant.now(clock)); // mark obsolete
        doGetVersions().forEach(ValidationRuleSetVersion::delete);
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
                .sorted(Comparator.comparing(IValidationRuleSetVersion::getNotNullStartDate))
                .collect(Collectors.toList());
        updateVersionsEndDate(versions);
        return versions;
    }

    private Stream<IValidationRuleSetVersion> doGetVersions() {
        return versions.stream().filter(version -> !version.isObsolete());
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
                start + limit + 1);
        updateVersionsEndDate(versions);

        return Collections.unmodifiableList(
                versions.stream()
                        .sorted(Comparator.comparing(IValidationRuleSetVersion::getNotNullStartDate).reversed())
                        .collect(Collectors.toList()));
    }

    private void updateVersionsEndDate(List<IValidationRuleSetVersion> versions) {
        versions.stream()
                .sorted(Comparator.comparing(IValidationRuleSetVersion::getNotNullStartDate))
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
        versions.add(newRuleSetVersion);
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
        if (doGetVersions().anyMatch(candidate -> candidate.equals(iVersion))) {
            iVersion.delete();
            dataModel.touch(this);
        } else {
            throw new IllegalArgumentException("The ruleset " + this.getId() + " doesn't contain provided rule set version Id: " + version.getId());
        }
    }

    @Override
    public ValidationRuleSetVersion cloneRuleSetVersion(long ruleSetVersionId, String description, Instant startDate) {

        IValidationRuleSetVersion existingVersion = doGetVersions()
                .filter(v -> v.getId() == ruleSetVersionId)
                .findFirst().orElseThrow(() ->
                        new IllegalArgumentException("The ruleset " + this.getId() + " doesn't contain provided rule set version Id: " + ruleSetVersionId));

        IValidationRuleSetVersion clonedVersion = addRuleSetVersion(description, startDate);
        existingVersion
                .getRules()
                .stream()
                .forEach(clonedVersion::cloneRule);
        clonedVersion.save();
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

    public List<ValidationRule> getRules(Collection<? extends ReadingType> readingTypes) {
        return getRules()
                .stream()
                .filter(rule -> readingTypes.stream().anyMatch(rule::appliesTo))
                .collect(Collectors.toList());
    }

    @Override
    public Instant getObsoleteDate() {
        return this.obsoleteTime;
    }

    private void setObsoleteTime(Instant obsoleteTime) {
        this.obsoleteTime = obsoleteTime;
    }
}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.ReadingTypeInEstimationRule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Objects;

class ReadingTypeInEstimationRuleImpl implements ReadingTypeInEstimationRule {

//    @ValidReadingType(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NO_SUCH_READINGTYPE + "}")
    private String readingTypeMRID;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    private ReadingType readingType;
    private Reference<EstimationRule> rule = ValueReference.absent();
    private final MeteringService meteringService;

    @Inject
    ReadingTypeInEstimationRuleImpl(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    ReadingTypeInEstimationRuleImpl init(EstimationRule rule, ReadingType readingType) {
        this.rule.set(rule);
        this.readingType = readingType;
        this.readingTypeMRID = readingType.getMRID();
        return this;
    }

    ReadingTypeInEstimationRuleImpl init(EstimationRule rule, String readingTypeMRID) {
        this.rule.set(rule);
        this.readingTypeMRID = readingTypeMRID;
        return this;
    }

    static ReadingTypeInEstimationRuleImpl from(DataModel dataModel, EstimationRule rule, ReadingType readingType) {
        return dataModel.getInstance(ReadingTypeInEstimationRuleImpl.class).init(rule, readingType);
    }

    static ReadingTypeInEstimationRuleImpl from(DataModel dataModel, EstimationRule rule, String readingTypeMRID) {
        return dataModel.getInstance(ReadingTypeInEstimationRuleImpl.class).init(rule, readingTypeMRID);
    }

    @Override
    public EstimationRule getRule() {
        return rule.get();
    }

    @Override
    public ReadingType getReadingType() {
        if (readingType == null) {
            readingType = meteringService.getReadingType(readingTypeMRID).get();
        }
        return readingType;
    }

    @Override
    public String toString() {
        return "ReadingTypeInEstimationRule{" +
                "rule=" + rule.get() +
                ", readingType=" + readingTypeMRID +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ReadingTypeInEstimationRuleImpl that = (ReadingTypeInEstimationRuleImpl) o;

        return rule.get().getId() == that.rule.get().getId() && readingTypeMRID.equals(that.readingTypeMRID);

    }

    @Override
    public int hashCode() {
        return Objects.hash(rule.get().getId(), readingTypeMRID);
    }
}

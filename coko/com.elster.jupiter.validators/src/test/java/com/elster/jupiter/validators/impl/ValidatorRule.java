/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.properties.NoneOrBigDecimal;
import com.elster.jupiter.properties.TwoValuesDifference;

import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.Map;

class ValidatorRule {
    MetrologyPurpose checkPurpose;
    MetrologyPurpose notExistingCheckPurpose;
    TwoValuesDifference twoValuesDifference;
    NoneOrBigDecimal minThreshold;
    boolean passIfNoData;
    boolean useValidatedData;
    boolean noCheckChannel;

    ValidatorRule withCheckPurpose(MetrologyPurpose checkPurpose) {
        this.checkPurpose = checkPurpose;
        return this;
    }

    ValidatorRule withNotExistingCheckPurpose(MetrologyPurpose notExistingCheckPurpose) {
        this.notExistingCheckPurpose = notExistingCheckPurpose;
        return this;
    }

    ValidatorRule withNotExistingCheckChannel(){
        noCheckChannel = true;
        return this;
    }

    ValidatorRule withValuedDifference(BigDecimal value) {
        this.twoValuesDifference = new TwoValuesDifference(TwoValuesDifference.Type.ABSOLUTE, value);
        return this;
    }

    ValidatorRule withPercentDifference(Double percent) {
        this.twoValuesDifference = new TwoValuesDifference(TwoValuesDifference.Type.RELATIVE, new BigDecimal(percent));
        return this;
    }

    ValidatorRule passIfNoRefData(boolean passIfNoData) {
        this.passIfNoData = passIfNoData;
        return this;
    }

    ValidatorRule useValidatedData(boolean useValidatedData) {
        this.useValidatedData = useValidatedData;
        return this;
    }

    ValidatorRule withNoMinThreshold() {
        this.minThreshold = NoneOrBigDecimal.none();
        return this;
    }

    ValidatorRule withMinThreshold(BigDecimal minThreshold) {
        this.minThreshold = NoneOrBigDecimal.of(minThreshold);
        return this;
    }

    Map<String, Object> createProperties() {
        return ImmutableMap.of(MainCheckValidator.CHECK_PURPOSE, notExistingCheckPurpose==null?checkPurpose:notExistingCheckPurpose,
                MainCheckValidator.MAX_ABSOLUTE_DIFF, twoValuesDifference,
                MainCheckValidator.MIN_THRESHOLD, minThreshold,
                MainCheckValidator.PASS_IF_NO_REF_DATA, passIfNoData,
                MainCheckValidator.USE_VALIDATED_DATA, useValidatedData);
    }
}

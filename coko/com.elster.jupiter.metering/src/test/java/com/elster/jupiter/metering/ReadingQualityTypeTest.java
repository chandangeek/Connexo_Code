/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.EqualsContractTest;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadingQualityTypeTest extends EqualsContractTest {

    private static final String CODE = "6.1";
    private ReadingQualityType a;

    @Override
    protected Object getInstanceA() {
        if (a == null) {
            a = new ReadingQualityType(CODE);
        }
        return a;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new ReadingQualityType(CODE);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(new ReadingQualityType("6.2"));
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
    
    @Test
    public void testUsageBelow() {
    	ReadingQualityType qualityType = new ReadingQualityType("2.6.4");
    	assertThat(qualityType.system().get()).isEqualTo(QualityCodeSystem.MDC);
    	assertThat(qualityType.category().get()).isEqualTo(QualityCodeCategory.VALIDATION);
    	assertThat(qualityType.qualityIndex().get()).isEqualTo(QualityCodeIndex.USAGEBELOW);
    	assertThat(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.USAGEBELOW)).isEqualTo(qualityType);
    }
    
    @Test
    public void testCustomValidation() {
    	ReadingQualityType qualityType = new ReadingQualityType("2.6.1004");
    	assertThat(qualityType.system().get()).isEqualTo(QualityCodeSystem.MDC);
    	assertThat(qualityType.category().get()).isEqualTo(QualityCodeCategory.VALIDATION);
        assertThat(qualityType.qualityIndex().get()).isEqualTo(QualityCodeIndex.VALIDATIONGENERIC);
    	assertThat(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.VALIDATION,1004)).isEqualTo(qualityType);
    }
}

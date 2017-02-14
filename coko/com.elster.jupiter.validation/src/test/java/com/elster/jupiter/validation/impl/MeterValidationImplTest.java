/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.orm.DataModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MeterValidationImplTest {

    MeterValidationImpl meterValidation;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataModel dataModel;

    @Mock
    private Meter meter;

    @Before
    public void setUp() {
        meterValidation = new MeterValidationImpl(dataModel).init(meter);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSetActivationStatus() {
        assertThat(meterValidation.getActivationStatus()).isEqualTo(false);
        meterValidation.setActivationStatus(true);
        assertThat(meterValidation.getActivationStatus()).isEqualTo(true);

    }
}

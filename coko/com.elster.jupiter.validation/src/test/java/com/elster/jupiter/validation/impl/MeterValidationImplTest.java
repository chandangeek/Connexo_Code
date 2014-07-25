package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.orm.DataModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MeterValidationImplTest {

    MeterValidationImpl meterValidation;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataModel dataModel;

    @Mock
    private MeterActivation meterActivation;

    @Before
    public void setUp() {
        meterValidation = new MeterValidationImpl(dataModel).init(meterActivation);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSetActivationStatus() {
        assertThat(meterValidation.getActivationStatus()).isEqualTo(true);
        meterValidation.setActivationStatus(false);
        assertThat(meterValidation.getActivationStatus()).isEqualTo(false);

    }
}

/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.orm.DataModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.reflect.core.Reflection.field;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChannelsContainerEstimationTest {

    private ChannelsContainerEstimation estimation;

    @Mock
    private DataModel dataModel;
    @Mock
    private javax.validation.ValidatorFactory validatorFactory;
    @Mock
    private javax.validation.Validator javaxValidator;

    @Before
    public void setUp(){
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(dataModel.getValidatorFactory().getValidator()).thenReturn(javaxValidator);
        estimation = new ChannelsContainerEstimationImpl(dataModel);
    }

    @Test
    public void checkActivationStatusIsFalseByDefault(){
        assertFalse(estimation.isActive());
    }

    @Test
    public void setActivationStatus(){
        estimation.setActive(true);
        assertTrue(estimation.isActive());
    }

    @Test
    public void save(){
        estimation.save();
        verify(dataModel).persist(estimation);
    }

    @Test
    public void update() {
        simulateSavedData();
        estimation.save();
        verify(dataModel).update(estimation);
    }

    private void simulateSavedData(){
        field("id").ofType(Long.TYPE).in(estimation).set(1L);
    }
}

/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.associations.Reference;

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
public class PurposeValidationImplTest {

    private PurposeValidationImpl purposeValidation;

    @Mock
    private DataModel dataModel;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private javax.validation.ValidatorFactory validatorFactory;
    @Mock
    private javax.validation.Validator javaxValidator;


    @Before
    public void setUp(){
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(dataModel.getValidatorFactory().getValidator()).thenReturn(javaxValidator);
        purposeValidation = new PurposeValidationImpl(dataModel);

    }

    @Test
    public void setActivationStatus(){
        assertFalse(purposeValidation.getActivationStatus());
        purposeValidation.setActivationStatus(true);
        assertTrue(purposeValidation.getActivationStatus());
    }

    @Test
    public void save(){
        purposeValidation.init(channelsContainer);

        purposeValidation.save();

        verify(dataModel).persist(purposeValidation);
    }

    @Test
    public void update(){
        simulateSavedData();

        purposeValidation.save();

        verify(dataModel).update(purposeValidation);
    }

    private void simulateSavedData(){
        Reference<ChannelsContainer> reference = ValueReference.absent();
        reference.set(channelsContainer);
        field("channelsContainer").ofType(Reference.class).in(purposeValidation).set(reference);
    }

}

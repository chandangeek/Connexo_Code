/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.config.MetrologyContract;
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
public class MetrologyContractValidationImplTest {

    private MetrologyContractValidationImpl metrologyContractValidation;

    @Mock
    private DataModel dataModel;
    @Mock
    private MetrologyContract metrologyContract;
    @Mock
    private javax.validation.ValidatorFactory validatorFactory;
    @Mock
    private javax.validation.Validator javaxValidator;


    @Before
    public void setUp(){
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(dataModel.getValidatorFactory().getValidator()).thenReturn(javaxValidator);
        metrologyContractValidation = new MetrologyContractValidationImpl(dataModel);

    }

    @Test
    public void setActivationStatus(){
        assertFalse(metrologyContractValidation.getActivationStatus());
        metrologyContractValidation.setActivationStatus(true);
        assertTrue(metrologyContractValidation.getActivationStatus());
    }

    @Test
    public void save(){
        metrologyContractValidation.init(metrologyContract);

        metrologyContractValidation.save();

        verify(dataModel).persist(metrologyContractValidation);
    }

    @Test
    public void update(){
        simulateSavedData();

        metrologyContractValidation.save();

        verify(dataModel).update(metrologyContractValidation);
    }

    private void simulateSavedData(){
        Reference<MetrologyContract> reference = ValueReference.absent();
        reference.set(metrologyContract);
        field("metrologyContract").ofType(Reference.class).in(metrologyContractValidation).set(reference);
    }

}

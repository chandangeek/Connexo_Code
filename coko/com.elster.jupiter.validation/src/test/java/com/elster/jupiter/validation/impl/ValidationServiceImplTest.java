package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import com.elster.jupiter.validation.ValidatorNotFoundException;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationServiceImplTest {

    private ValidationServiceImpl validationService;

    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private ComponentCache componentCache;
    @Mock
    private EventService eventService;
    @Mock
    private OrmClient ormClient;
    @Mock
    private ValidatorFactory factory;
    @Mock
    private Validator validator;

    @Before
    public void setUp() {
        validationService = new ValidationServiceImpl();
        when(serviceLocator.getComponentCache()).thenReturn(componentCache);
        when(serviceLocator.getEventService()).thenReturn(eventService);
        when(serviceLocator.getValidationService()).thenReturn(validationService);
        when(serviceLocator.getOrmClient()).thenReturn(ormClient);
        when(factory.available()).thenReturn(Arrays.asList(validator.getClass().getName()));
        when(factory.create(validator.getClass().getName())).thenReturn(validator);

        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.clearServiceLocator(serviceLocator);
    }

    @Test
    public void testGetImplementation() {
        validationService.addResource(factory);

        Validator found = validationService.getValidator(validator.getClass().getName());

        assertThat(found).isNotNull().isEqualTo(validator);
    }

    @Test(expected = ValidatorNotFoundException.class)
    public void testGetValidatorThrowsNotFoundExceptionIfNoFactoryProvidesImplementation() {
        validationService.getValidator(validator.getClass().getName());
    }
}

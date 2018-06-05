/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.orm.DataMapper;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EstimationStatusActivationTest extends EstimationActivationTest{

    private static final long ID = 1L;

    private PurposeEstimationImpl purposeEstimation;

    @Mock
    private DataMapper<PurposeEstimationImpl> purposeEstimationFactory;

    @Before
    public void setUp(){
        super.setUp();
        when(dataModel.mapper(PurposeEstimationImpl.class)).thenReturn(purposeEstimationFactory);
        when(channelsContainer.getId()).thenReturn(ID);
        purposeEstimation = new PurposeEstimationImpl(dataModel);
        when(purposeEstimationFactory.getOptional(ID)).thenReturn(Optional.of(purposeEstimation));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isEstimationActiveWithNullArgument() {
        estimationService.isEstimationActive(null);
    }

    @Test
    public void whenNoPurposeEstimation_thenReturnInactive(){
        when(purposeEstimationFactory.getOptional(ID)).thenReturn(Optional.empty());

        boolean status = estimationService.isEstimationActive(channelsContainer);

        assertFalse(status);
    }

    @Test
    public void whenPurposeEstimation_thenReturnStatus(){
        purposeEstimation.setActivationStatus(true);

        assertTrue(estimationService.isEstimationActive(channelsContainer));
    }

    @Test(expected = IllegalArgumentException.class)
    public void activateEstimationWithNullArgument() {
        estimationService.activateEstimation(null);
    }

    @Test
    public void activate_whenNoEstimation_thenCreateNewAndActivate() {
        when(purposeEstimationFactory.getOptional(ID)).thenReturn(Optional.empty());

        estimationService.activateEstimation(channelsContainer);

        verify(dataModel).persist(any(PurposeEstimationImpl.class));
    }

    @Test
    public void activate_whenEstimationIsInactive_thenActivate() {
        purposeEstimation.setActivationStatus(false);

        estimationService.activateEstimation(channelsContainer);

        assertTrue(purposeEstimation.isActive());
        verify(dataModel).update(purposeEstimation);
    }

    @Test
    public void activate_whenEstimationIsActive_thenDoNothing(){
        purposeEstimation.setActivationStatus(true);

        estimationService.activateEstimation(channelsContainer);

        assertTrue(purposeEstimation.isActive());
        verify(dataModel, times(0)).update(purposeEstimation);
    }


    @Test(expected = IllegalArgumentException.class)
    public void deactivateEstimationWithNullArgument() {
        estimationService.deactivateEstimation(null);
    }

    @Test
    public void deactivate_whenNoEstimation_thenDoNothing() {
        when(purposeEstimationFactory.getOptional(ID)).thenReturn(Optional.empty());

        estimationService.deactivateEstimation(channelsContainer);

        verify(dataModel, times(0)).persist(any(PurposeEstimationImpl.class));
    }

    @Test
    public void deactivate_whenEstimationIsActive_thenInactivate() {
        purposeEstimation.setActivationStatus(true);

        estimationService.deactivateEstimation(channelsContainer);

        assertFalse(purposeEstimation.isActive());
        verify(dataModel).update(purposeEstimation);
    }

    @Test
    public void deactivate_whenEstimationIsInactive_thenDoNothing(){
        purposeEstimation.setActivationStatus(false);

        estimationService.deactivateEstimation(channelsContainer);

        assertFalse(purposeEstimation.isActive());
        verify(dataModel, times(0)).update(purposeEstimation);
    }
}

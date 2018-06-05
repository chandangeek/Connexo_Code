/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.util.conditions.Condition;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EstimationRuleSetActivationTest extends EstimationActivationTest{


    @Test(expected = IllegalArgumentException.class)
    public void activeRuleSetsWithNullArgument() {
        estimationService.activeRuleSets(null);
    }

    @Test
    public void whenNoActiveRuleSets_thenEmptyList() {
        when(query.select(any(Condition.class))).thenReturn(Collections.emptyList());

        List<EstimationRuleSet> estimationRuleSetList = estimationService.activeRuleSets(channelsContainer);

        assertThat(estimationRuleSetList).isEmpty();
    }

    @Test
    public void getActiveRuleSets() {
        ChannelsContainerEstimation activeEstimation = mockChannelsContainerEstimation(true);
        when(query.select(any(Condition.class))).thenReturn(Collections.singletonList(activeEstimation));

        List<EstimationRuleSet> estimationRuleSetList = estimationService.activeRuleSets(channelsContainer);

        assertThat(estimationRuleSetList).hasSize(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void activateRuleSetsWithNullContainer() {
        estimationService.activate(null, estimationRuleSet);
    }

    @Test(expected = IllegalArgumentException.class)
    public void activateRuleSetsWithNullRuleSet() {
        estimationService.activate(channelsContainer, null);
    }

    @Test
    public void whenRuleSetNotFound_thenCreateActivation(){
        when(query.select(any(Condition.class))).thenReturn(Collections.emptyList());

        estimationService.activate(channelsContainer, estimationRuleSet);

        verify(dataModel).persist(any(ChannelsContainerEstimation.class));
    }

    @Test
    public void whenRuleSetFoundAndActive_thenDoNothing(){
        ChannelsContainerEstimation activeEstimation = mockChannelsContainerEstimation(true);
        when(query.select(any(Condition.class))).thenReturn(Collections.singletonList(activeEstimation));

        estimationService.activate(channelsContainer, estimationRuleSet);

        verify(dataModel, times(0)).persist(any(ChannelsContainerEstimation.class));
    }

    @Test
    public void whenRuleSetFoundAndInactive_thenActivate(){
        ChannelsContainerEstimation activeEstimation = mockChannelsContainerEstimation(false);
        when(query.select(any(Condition.class))).thenReturn(Collections.singletonList(activeEstimation));

        estimationService.activate(channelsContainer, estimationRuleSet);

        assertTrue(activeEstimation.isActive());
    }

    @Test(expected = IllegalArgumentException.class)
    public void deactivateRuleSetWithNullContainer() {
        estimationService.deactivate(null, estimationRuleSet);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deactivateRuleSetsWithNullRuleSet() {
        estimationService.deactivate(channelsContainer, null);
    }

    @Test
    public void whenRuleSetFoundButInactive_thenDoNothing(){
        ChannelsContainerEstimation activeEstimation = mockChannelsContainerEstimation(false);
        when(query.select(any(Condition.class))).thenReturn(Collections.singletonList(activeEstimation));

        estimationService.deactivate(channelsContainer, estimationRuleSet);

        verify(dataModel, times(0)).persist(any(ChannelsContainerEstimation.class));
    }

    @Test
    public void whenRuleSetFound_thenDeactivate(){
        ChannelsContainerEstimation activeEstimation = mockChannelsContainerEstimation(true);
        when(query.select(any(Condition.class))).thenReturn(Collections.singletonList(activeEstimation));

        estimationService.deactivate(channelsContainer, estimationRuleSet);

        assertFalse(activeEstimation.isActive());
    }

    private ChannelsContainerEstimation mockChannelsContainerEstimation(boolean active) {
        ChannelsContainerEstimation estimation = new ChannelsContainerEstimationImpl(dataModel).init(channelsContainer, estimationRuleSet);
        estimation.setActive(active);
        return estimation;
    }
}

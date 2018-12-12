/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.groups.GroupEventData;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.Thesaurus;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointGroupDeletionVetoEventHandlerTest {

    private UsagePointGroupDeletionVetoEventHandler usagePointGroupDeletionVetoEventHandler;
    @Mock
    private LocalEvent localEvent;
    @Mock
    private GroupEventData eventSource;
    @Mock
    private IEstimationService estimationService;
    @Mock
    private EstimationTask estimationTask;
    @Mock
    private UsagePointGroup usagePointGroup;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setUp() {
        usagePointGroupDeletionVetoEventHandler = new UsagePointGroupDeletionVetoEventHandler(estimationService, thesaurus);

        when(localEvent.getSource()).thenReturn(eventSource);
        doReturn(Collections.singletonList(estimationTask)).when(estimationService).findEstimationTasks(QualityCodeSystem.MDM);
        doReturn(usagePointGroup).when(eventSource).getGroup();
    }

    @Test(expected = VetoDeleteUsagePointGroupException.class)
    public void catchVetoDeleteUsagePointGroupException() {
        when(estimationTask.getUsagePointGroup()).thenReturn(Optional.of(usagePointGroup));

        usagePointGroupDeletionVetoEventHandler.handle(localEvent);
    }

    @Test
    public void noVetoDeleteUsagePointGroupException() {
        when(estimationTask.getUsagePointGroup()).thenReturn(Optional.empty());

        usagePointGroupDeletionVetoEventHandler.handle(localEvent);

        // Asserts
        // no exception
    }
}
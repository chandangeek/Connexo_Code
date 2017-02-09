/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.groups.GroupEventData;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.ValidationService;

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
    private ValidationService validationService;
    @Mock
    private DataValidationTask dataValidationTask;
    @Mock
    private UsagePointGroup usagePointGroup;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setUp() {
        usagePointGroupDeletionVetoEventHandler = new UsagePointGroupDeletionVetoEventHandler(validationService, thesaurus);

        when(localEvent.getSource()).thenReturn(eventSource);
        when(validationService.findValidationTasks()).thenReturn(Collections.singletonList(dataValidationTask));
        doReturn(usagePointGroup).when(eventSource).getGroup();
    }

    @Test(expected = VetoDeleteUsagePointGroupException.class)
    public void catchVetoDeleteUsagePointGroupException() {
        when(dataValidationTask.getUsagePointGroup()).thenReturn(Optional.of(usagePointGroup));

        usagePointGroupDeletionVetoEventHandler.handle(localEvent);
    }

    @Test
    public void noVetoDeleteUsagePointGroupException() {
        when(dataValidationTask.getUsagePointGroup()).thenReturn(Optional.empty());

        usagePointGroupDeletionVetoEventHandler.handle(localEvent);

        // Asserts
        // no exception
    }
}
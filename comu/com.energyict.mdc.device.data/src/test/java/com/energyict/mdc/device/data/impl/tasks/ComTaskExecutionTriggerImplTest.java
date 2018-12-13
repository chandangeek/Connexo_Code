/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionTrigger;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 29/06/2016 - 16:32
 */
@RunWith(MockitoJUnitRunner.class)
public class ComTaskExecutionTriggerImplTest {

    @Mock
    ComTaskExecution comTaskExecution;
    @Mock
    DataModel dataModel;
    @Mock
    EventService eventService;
    @Mock
    Thesaurus thesaurus;

    @Before
    public void setUp() throws Exception {
        when(dataModel.getInstance(ComTaskExecutionTriggerImpl.class)).thenReturn(
                new ComTaskExecutionTriggerImpl(dataModel, eventService, thesaurus),
                new ComTaskExecutionTriggerImpl(dataModel, eventService, thesaurus)
        );
    }

    @Test
    public void testFrom() throws Exception {
        Instant triggerTimeStamp = Instant.now();

        // Business method
        ComTaskExecutionTrigger comTaskExecutionTrigger = ComTaskExecutionTriggerImpl.from(dataModel, comTaskExecution, triggerTimeStamp);

        // Asserts
        assertEquals(comTaskExecution, comTaskExecutionTrigger.getComTaskExecution());
        assertEquals(triggerTimeStamp, comTaskExecutionTrigger.getTriggerTimeStamp());
    }

    @Test
    @Transactional
    public void testEquals() throws Exception {
        Instant triggerTimeStamp = Instant.now();
        ComTaskExecutionTrigger comTaskExecutionTrigger_1 = ComTaskExecutionTriggerImpl.from(dataModel, comTaskExecution, triggerTimeStamp);
        ComTaskExecutionTrigger comTaskExecutionTrigger_2 = ComTaskExecutionTriggerImpl.from(dataModel, comTaskExecution, triggerTimeStamp);

        // Business method
        assertTrue(comTaskExecutionTrigger_1.equals(comTaskExecutionTrigger_2));
    }
}
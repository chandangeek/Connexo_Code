/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.keyrenewal;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.energyict.mdc.device.data.impl.pki.PropertyValueRequiredException;

import org.osgi.framework.BundleContext;

import java.time.Clock;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeyRenewalHandlerFactoryTest {

    private static final String KEY_RENEWAL_PROCESS_DEFINITION_PROPERTY = "com.energyict.mdc.device.data.pki.keyrenewal.bpmprocess";
    private static final String KEY_DAYS_TILL_EXPIRATION_PROPERTY = "com.energyict.mdc.device.data.pki.keyrenewal.expirationdays";

    private static final String KEY_RENEWAL_PROCESS_DEFINITION_VALUE = "DeviceProcesses.KeyRenewal";
    private static final String KEY_DAYS_TILL_EXPIRATION_VALUE = "1";

    private static final String KEY_RENEWAL_TASK_NAME = "Key Renewal Task";

    private KeyRenewalHandlerFactory keyRenewalHandlerFactory;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private TaskService taskService;
    @Mock
    private OrmService ormService;
    @Mock
    private EventService eventService;
    @Mock
    private BpmService bpmService;
    @Mock
    private NlsService nlsService;
    @Mock
    private RecurrentTask recurrentTask;
    @Mock
    private KeyRenewalTaskExecutor keyRenewalTaskExecutor;
    @Mock
    private TaskOccurrence taskOccurrence;
    @Mock
    private Clock clock;

    @Before
    public void setUp() throws Exception {
        when(bundleContext.getProperty(KEY_RENEWAL_PROCESS_DEFINITION_PROPERTY)).thenReturn(KEY_RENEWAL_PROCESS_DEFINITION_VALUE);
        when(bundleContext.getProperty(KEY_DAYS_TILL_EXPIRATION_PROPERTY)).thenReturn(KEY_DAYS_TILL_EXPIRATION_VALUE);
        when(taskService.getRecurrentTask(KEY_RENEWAL_TASK_NAME)).thenReturn(Optional.of(recurrentTask));
        when(recurrentTask.runNow(keyRenewalTaskExecutor)).thenReturn(taskOccurrence);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetKeyRenewalProcessDefinitionProperty() {
        keyRenewalHandlerFactory = new KeyRenewalHandlerFactory(bundleContext, taskService, ormService, bpmService, clock, nlsService, eventService);
        verify(bundleContext, times(1)).getProperty(KEY_RENEWAL_PROCESS_DEFINITION_PROPERTY);
    }

    @Test (expected = PropertyValueRequiredException.class)
    public void testNoKeyRenewalProcessDefinitionProperty() {
        when(bundleContext.getProperty(KEY_RENEWAL_PROCESS_DEFINITION_PROPERTY)).thenReturn(null);
        keyRenewalHandlerFactory = new KeyRenewalHandlerFactory(bundleContext, taskService, ormService, bpmService, clock, nlsService, eventService);
        verify(bundleContext, times(1)).getProperty(KEY_RENEWAL_PROCESS_DEFINITION_PROPERTY);
    }

    @Test
    public void testGetKeyRenewalDaysTillExpirationProperty() {
        keyRenewalHandlerFactory = new KeyRenewalHandlerFactory(bundleContext, taskService, ormService, bpmService, clock, nlsService, eventService);
        verify(bundleContext, times(1)).getProperty(KEY_DAYS_TILL_EXPIRATION_PROPERTY);
    }

    @Test (expected = PropertyValueRequiredException.class)
    public void testNoKeyRenewalDaysTillExpirationProperty() {
        when(bundleContext.getProperty(KEY_DAYS_TILL_EXPIRATION_PROPERTY)).thenReturn(null);
        keyRenewalHandlerFactory = new KeyRenewalHandlerFactory(bundleContext, taskService, ormService, bpmService, clock, nlsService, eventService);
        verify(bundleContext, times(1)).getProperty(KEY_DAYS_TILL_EXPIRATION_PROPERTY);
    }

    @Test
    public void testGetKeyRenewalTask() {
        keyRenewalHandlerFactory = new KeyRenewalHandlerFactory(bundleContext, taskService, ormService, bpmService, clock, nlsService, eventService);
        keyRenewalHandlerFactory.getTask();
        verify(taskService, times(1)).getRecurrentTask(KEY_RENEWAL_TASK_NAME);
    }

    @Test
    public void testRunKeyRenewalTask() {
        keyRenewalHandlerFactory = new KeyRenewalHandlerFactory(bundleContext, taskService, ormService, bpmService, clock, nlsService, eventService);
        keyRenewalHandlerFactory.runNow();
        verify(recurrentTask, times(1)).runNow(any(KeyRenewalTaskExecutor.class));
    }

    @Test
    public void testKeyRenewalTaskDeactivate() {
        keyRenewalHandlerFactory = new KeyRenewalHandlerFactory(bundleContext, taskService, ormService, bpmService, clock, nlsService, eventService);
        keyRenewalHandlerFactory.deactivate();
        assertThat(keyRenewalHandlerFactory.getKeyRenewalBpmProcessDefinitionId()).isNull();
        assertThat(keyRenewalHandlerFactory.getKeyRenewalExpitationDays()).isNull();
    }

    @Test
    public void testKeyRenewalTaskActivate() {
        keyRenewalHandlerFactory = new KeyRenewalHandlerFactory(bundleContext, taskService, ormService, bpmService, clock, nlsService, eventService);
        assertThat(keyRenewalHandlerFactory.getKeyRenewalBpmProcessDefinitionId()).isEqualTo(KEY_RENEWAL_PROCESS_DEFINITION_VALUE);
        assertThat(keyRenewalHandlerFactory.getKeyRenewalExpitationDays()).isEqualTo(Integer.parseInt(KEY_DAYS_TILL_EXPIRATION_VALUE));
    }
}

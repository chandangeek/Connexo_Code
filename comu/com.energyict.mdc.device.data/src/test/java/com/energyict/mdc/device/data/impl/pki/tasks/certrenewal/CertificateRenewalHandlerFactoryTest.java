/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.certrenewal;

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
public class CertificateRenewalHandlerFactoryTest {
    private static final String CERTIFICATE_RENEWAL_PROCESS_DEFINITION_PROPERTY = "com.energyict.mdc.device.data.pki.certrenewal.bpmprocess";
    private static final String CERTIFICATE_DAYS_TILL_EXPIRATION_PROPERTY = "com.energyict.mdc.device.data.pki.certrenewal.expirationdays";

    private static final String CERTIFICATE_RENEWAL_PROCESS_DEFINITION_VALUE = "DeviceProcesses.CertificateRenewal";
    private static final String CERTIFICATE_DAYS_TILL_EXPIRATION_VALUE = "1";

    private static final String CERTIFICATE_RENEWAL_TASK_NAME = "Certificate Renewal Task";

    private CertificateRenewalHandlerFactory certificateRenewalHandlerFactory;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private TaskService taskService;
    @Mock
    private OrmService ormService;
    @Mock
    private BpmService bpmService;
    @Mock
    private NlsService nlsService;
    @Mock
    private RecurrentTask recurrentTask;
    @Mock
    private CertificateRenewalTaskExecutor certificateRenewalTaskExecutor;
    @Mock
    private TaskOccurrence taskOccurrence;
    @Mock
    private EventService eventService;
    @Mock
    private Clock clock;

    @Before
    public void setUp() throws Exception {
        when(bundleContext.getProperty(CERTIFICATE_RENEWAL_PROCESS_DEFINITION_PROPERTY)).thenReturn(CERTIFICATE_RENEWAL_PROCESS_DEFINITION_VALUE);
        when(bundleContext.getProperty(CERTIFICATE_DAYS_TILL_EXPIRATION_PROPERTY)).thenReturn(CERTIFICATE_DAYS_TILL_EXPIRATION_VALUE);
        when(taskService.getRecurrentTask(CERTIFICATE_RENEWAL_TASK_NAME)).thenReturn(Optional.of(recurrentTask));
        when(recurrentTask.runNow(certificateRenewalTaskExecutor)).thenReturn(taskOccurrence);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetCertificateRenewalProcessDefinitionProperty() {
        certificateRenewalHandlerFactory = new CertificateRenewalHandlerFactory(bundleContext, taskService, ormService, bpmService, clock, nlsService, eventService);
        verify(bundleContext, times(1)).getProperty(CERTIFICATE_RENEWAL_PROCESS_DEFINITION_PROPERTY);
    }

    @Test (expected = PropertyValueRequiredException.class)
    public void testNoCertificateRenewalProcessDefinitionProperty() {
        when(bundleContext.getProperty(CERTIFICATE_RENEWAL_PROCESS_DEFINITION_PROPERTY)).thenReturn(null);
        certificateRenewalHandlerFactory = new CertificateRenewalHandlerFactory(bundleContext, taskService, ormService, bpmService, clock, nlsService, eventService);
        verify(bundleContext, times(1)).getProperty(CERTIFICATE_RENEWAL_PROCESS_DEFINITION_PROPERTY);
    }

    @Test
    public void testGetCertificateRenewalDaysTillExpirationProperty() {
        certificateRenewalHandlerFactory = new CertificateRenewalHandlerFactory(bundleContext, taskService, ormService, bpmService, clock, nlsService, eventService);
        verify(bundleContext, times(1)).getProperty(CERTIFICATE_DAYS_TILL_EXPIRATION_PROPERTY);
    }

    @Test (expected = PropertyValueRequiredException.class)
    public void testNoCertificateRenewalDaysTillExpirationProperty() {
        when(bundleContext.getProperty(CERTIFICATE_DAYS_TILL_EXPIRATION_PROPERTY)).thenReturn(null);
        certificateRenewalHandlerFactory = new CertificateRenewalHandlerFactory(bundleContext, taskService, ormService, bpmService, clock, nlsService, eventService);
        verify(bundleContext, times(1)).getProperty(CERTIFICATE_DAYS_TILL_EXPIRATION_PROPERTY);
    }

    @Test
    public void testGetCertificateRenewalTask() {
        certificateRenewalHandlerFactory = new CertificateRenewalHandlerFactory(bundleContext, taskService, ormService, bpmService, clock, nlsService, eventService);
        certificateRenewalHandlerFactory.getTask();
        verify(taskService, times(1)).getRecurrentTask(CERTIFICATE_RENEWAL_TASK_NAME);
    }

    @Test
    public void testRunCertificateRenewalTask() {
        certificateRenewalHandlerFactory = new CertificateRenewalHandlerFactory(bundleContext, taskService, ormService, bpmService, clock, nlsService, eventService);
        certificateRenewalHandlerFactory.runNow();
        verify(recurrentTask, times(1)).runNow(any(CertificateRenewalTaskExecutor.class));
    }

    @Test
    public void testCertificateRenewalTaskDeactivate() {
        certificateRenewalHandlerFactory = new CertificateRenewalHandlerFactory(bundleContext, taskService, ormService, bpmService, clock, nlsService, eventService);
        certificateRenewalHandlerFactory.deactivate();
        assertThat(certificateRenewalHandlerFactory.getCertRenewalBpmProcessDefinitionId()).isNull();
        assertThat(certificateRenewalHandlerFactory.getCertRenewalExpitationDays()).isNull();
    }

    @Test
    public void testCertificateRenewalTaskActivate() {
        certificateRenewalHandlerFactory = new CertificateRenewalHandlerFactory(bundleContext, taskService, ormService, bpmService, clock, nlsService, eventService);
        assertThat(certificateRenewalHandlerFactory.getCertRenewalBpmProcessDefinitionId()).isEqualTo(CERTIFICATE_RENEWAL_PROCESS_DEFINITION_VALUE);
        assertThat(certificateRenewalHandlerFactory.getCertRenewalExpitationDays()).isEqualTo(Integer.parseInt(CERTIFICATE_DAYS_TILL_EXPIRATION_VALUE));
    }

}

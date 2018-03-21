/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.crlrequest;

import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskLogHandler;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskPropertiesService;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Handler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CrlRequestHandlerFactoryTest {
    private CrlRequestHandlerFactory crlRequestHandlerFactory;
    private static final String CA_NAME = "test_ca";

    @Mock
    private TaskService taskService;
    @Mock
    private CaService caService;
    @Mock
    private CrlRequestTaskPropertiesService crlRequestTaskPropertiesService;
    @Mock
    private SecurityManagementService securityManagementService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private RecurrentTask recurrentTask;
    @Mock
    private CrlRequestTaskExecutor crlRequestTaskExecutor;
    @Mock
    private TaskOccurrence taskOccurrence;
    @Mock
    private TaskLogHandler taskLogHandler;
    @Mock
    private Handler handler;
    @Mock
    private Clock clock;
    @Mock
    private CrlRequestTaskProperty crlRequestTaskProperty;
    @Mock
    private SecurityAccessor securityAccessor;
    @Mock
    private CertificateWrapper certificateWrapper;
    @Mock
    private X509Certificate x509Certificate;
    @Mock
    private PublicKey publicKey;

    @Before
    public void setUp() throws Exception {
        List<String> caNames = new ArrayList<>();
        caNames.add(CA_NAME);
        when(taskService.getRecurrentTask(CrlRequestHandlerFactory.CRL_REQUEST_TASK_NAME)).thenReturn(Optional.of(recurrentTask));
        when(recurrentTask.runNow(crlRequestTaskExecutor)).thenReturn(taskOccurrence);
        when(taskOccurrence.createTaskLogHandler()).thenReturn(taskLogHandler);
        when(taskLogHandler.asHandler()).thenReturn(handler);
        when(crlRequestTaskPropertiesService.findCrlRequestTaskProperties()).thenReturn(Optional.of(crlRequestTaskProperty));
        when(caService.isConfigured()).thenReturn(true);
        when(caService.getPkiCaNames()).thenReturn(caNames);
        when(crlRequestTaskProperty.getCaName()).thenReturn(CA_NAME);
        when(crlRequestTaskProperty.getSecurityAccessor()).thenReturn(securityAccessor);
        when(securityAccessor.getActualValue()).thenReturn(Optional.of(certificateWrapper));
        when(certificateWrapper.getCertificate()).thenReturn(Optional.of(x509Certificate));
        when(x509Certificate.getNotAfter()).thenReturn(new Date());
        when(x509Certificate.getPublicKey()).thenReturn(publicKey);
        when(clock.instant()).thenReturn(Instant.now());
        when(caService.getLatestCRL(any(String.class))).thenReturn(Optional.empty());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetRecurrentTask() {
        crlRequestHandlerFactory = new CrlRequestHandlerFactory(taskService, caService, crlRequestTaskPropertiesService, securityManagementService, deviceService, clock);
        crlRequestHandlerFactory.getTask();
        verify(taskService).getRecurrentTask(CrlRequestHandlerFactory.CRL_REQUEST_TASK_NAME);
    }

    @Test
    public void testRunRecurrentTask() {
        crlRequestHandlerFactory = new CrlRequestHandlerFactory(taskService, caService, crlRequestTaskPropertiesService, securityManagementService, deviceService, clock);
        crlRequestHandlerFactory.runNow();
        verify(recurrentTask).runNow(any(CrlRequestTaskExecutor.class));
    }

    @Test
    public void testFindCrlRequestTaskProperties() {
        CrlRequestTaskExecutor executor = new CrlRequestTaskExecutor(caService, crlRequestTaskPropertiesService, securityManagementService, deviceService, clock);
        executor.execute(taskOccurrence);
        verify(crlRequestTaskPropertiesService).findCrlRequestTaskProperties();
    }

    @Test
    public void testCaIsConfigured() {
        CrlRequestTaskExecutor executor = new CrlRequestTaskExecutor(caService, crlRequestTaskPropertiesService, securityManagementService, deviceService, clock);
        executor.execute(taskOccurrence);
        verify(caService).isConfigured();
    }

    @Test
    public void testGetCaName() {
        CrlRequestTaskExecutor executor = new CrlRequestTaskExecutor(caService, crlRequestTaskPropertiesService, securityManagementService, deviceService, clock);
        executor.execute(taskOccurrence);
        verify(crlRequestTaskProperty).getCaName();
    }

    @Test
    public void testGetSecurityAccessor() {
        CrlRequestTaskExecutor executor = new CrlRequestTaskExecutor(caService, crlRequestTaskPropertiesService, securityManagementService, deviceService, clock);
        executor.execute(taskOccurrence);
        verify(crlRequestTaskProperty).getSecurityAccessor();
    }

    @Test
    public void testGetPkiCaNames() {
        CrlRequestTaskExecutor executor = new CrlRequestTaskExecutor(caService, crlRequestTaskPropertiesService, securityManagementService, deviceService, clock);
        executor.execute(taskOccurrence);
        verify(caService).getPkiCaNames();
    }

    @Test
    public void testGetLatestCrl() {
        CrlRequestTaskExecutor executor = new CrlRequestTaskExecutor(caService, crlRequestTaskPropertiesService, securityManagementService, deviceService, clock);
        executor.execute(taskOccurrence);
        verify(caService).getLatestCRL(any(String.class));
    }

}

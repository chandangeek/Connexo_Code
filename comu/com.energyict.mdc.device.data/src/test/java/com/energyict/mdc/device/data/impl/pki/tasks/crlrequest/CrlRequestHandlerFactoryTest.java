/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.crlrequest;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskLogHandler;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
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
    private static final String CA_NAME = "test_ca";
    private CrlRequestHandlerFactory crlRequestHandlerFactory;
    private CrlRequestTaskExecutor executor;

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
    @Mock
    private NlsService nlsService;
    @Mock
    private EventService eventService;

    private TransactionService transactionService = TransactionModule.FakeTransactionService.INSTANCE;
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;

    @Before
    public void setUp() throws Exception {
        List<String> caNames = new ArrayList<>();
        caNames.add(CA_NAME);
        when(taskService.getRecurrentTask(CrlRequestHandlerFactory.CRL_REQUEST_TASK_NAME)).thenReturn(Optional.of(recurrentTask));
        when(recurrentTask.runNow(crlRequestTaskExecutor)).thenReturn(taskOccurrence);
        when(taskOccurrence.createTaskLogHandler()).thenReturn(taskLogHandler);
        when(taskOccurrence.getRecurrentTask()).thenReturn(recurrentTask);
        when(taskLogHandler.asHandler()).thenReturn(handler);
        when(crlRequestTaskPropertiesService.getCrlRequestTaskPropertiesForCa(recurrentTask)).thenReturn(Optional.of(crlRequestTaskProperty));
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
        crlRequestHandlerFactory = new CrlRequestHandlerFactory(taskService, caService, crlRequestTaskPropertiesService, securityManagementService, deviceService, clock, nlsService, transactionService);
        executor = new CrlRequestTaskExecutor(caService, crlRequestTaskPropertiesService, securityManagementService, deviceService, clock, thesaurus, transactionService, eventService);
    }

    @Test
    public void testGetRecurrentTask() {
        crlRequestHandlerFactory.getTask();
        verify(taskService).getRecurrentTask(CrlRequestHandlerFactory.CRL_REQUEST_TASK_NAME);
    }

    @Test
    public void testRunRecurrentTask() {
        crlRequestHandlerFactory.runNow();
        verify(recurrentTask).runNow(any(CrlRequestTaskExecutor.class));
    }

    @Test
    public void testFindCrlRequestTaskProperties() {
        executor.execute(taskOccurrence);
        executor.postExecute(taskOccurrence);
        verify(crlRequestTaskPropertiesService).getCrlRequestTaskPropertiesForCa(recurrentTask);
    }

    @Test
    public void testGetCaName() {
        executor.execute(taskOccurrence);
        executor.postExecute(taskOccurrence);
        verify(crlRequestTaskProperty).getCaName();
    }

    @Test
    public void testGetSecurityAccessor() {
        executor.execute(taskOccurrence);
        executor.postExecute(taskOccurrence);
        verify(crlRequestTaskProperty).getSecurityAccessor();
    }

    @Test
    public void testGetPkiCaNames() {
        executor.execute(taskOccurrence);
        executor.postExecute(taskOccurrence);
        verify(caService).getPkiCaNames();
    }

    @Test
    public void testGetLatestCrl() {
        executor.execute(taskOccurrence);
        executor.postExecute(taskOccurrence);
        verify(caService).getLatestCRL(any(String.class));
    }

}

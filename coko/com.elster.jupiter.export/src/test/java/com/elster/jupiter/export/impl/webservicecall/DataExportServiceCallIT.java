/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl.webservicecall;

import com.elster.jupiter.appserver.impl.AppServiceModule;
import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.impl.ExportModule;
import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.export.webservicecall.ServiceCallStatus;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fsm.StateTransitionPropertiesProvider;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.fsm.impl.StateTransitionTriggerEventTopicHandler;
import com.elster.jupiter.ftpclient.impl.FtpModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.mail.impl.MailModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.impl.ServiceCallModule;
import com.elster.jupiter.servicecall.impl.ServiceCallStateChangeTopicHandler;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.impl.ValidationModule;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataExportServiceCallIT {
    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(HttpService.class).toInstance(mock(HttpService.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(StateTransitionPropertiesProvider.class).toInstance(mock(StateTransitionPropertiesProvider.class));
        }
    }

    private static final long TIMEOUT = 1000;
    private static final String UUID = "facade3e-c632-11e8-a355-529269fb1459";
    private static final String ANOTHER_UUID = "4ff58443-1234-4aed-ae6e-d1cd4db81f21";
    private static Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static Injector injector;
    private static ServiceCallService serviceCallService;
    private static DataExportServiceCallType dataExportServiceCallType;
    private static ServiceCallHandler serviceCallHandler;
    private static CustomPropertySet<ServiceCall, WebServiceDataExportDomainExtension> serviceCallCPS;
    private static TransactionService transactionService;

    @Rule
    public TestRule transactional = new TransactionalRule(transactionService);

    @BeforeClass
    public static void setUp() {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new FiniteStateMachineModule(),
                    new UsagePointLifeCycleConfigurationModule(),
                    new CalendarModule(),
                    new MeteringModule("0.0.5.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0"),
                    new PartyModule(),
                    new DataVaultModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new NlsModule(),
                    new ExportModule(),
                    new TimeModule(),
                    new TaskModule(),
                    new MeteringGroupsModule(),
                    new SearchModule(),
                    new WebServicesModule(),
                    new AuditServiceModule(),
                    new AppServiceModule(),
                    new BasicPropertiesModule(),
                    new MailModule(),
                    new KpiModule(),
                    new ValidationModule(),
                    new BpmModule(),
                    new DataVaultModule(),
                    new FtpModule(),
                    new UserModule(),
                    new CustomPropertySetsModule(),
                    new FileImportModule(),
                    new ServiceCallModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            EventServiceImpl eventService = injector.getInstance(EventServiceImpl.class);
            // add transition topic handlers to make fsm transitions work
            eventService.addTopicHandler(injector.getInstance(StateTransitionTriggerEventTopicHandler.class));
            eventService.addTopicHandler(injector.getInstance(ServiceCallStateChangeTopicHandler.class));
            serviceCallService = injector.getInstance(ServiceCallService.class);
            PropertySpecService propertySpecService = injector.getInstance(PropertySpecService.class);
            serviceCallCPS = new WebServiceDataExportCustomPropertySet(thesaurus, propertySpecService);
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(serviceCallCPS);
            dataExportServiceCallType = injector.getInstance(DataExportService.class).getDataExportServiceCallType();
            serviceCallHandler = new WebServiceDataExportServiceCallHandler(thesaurus, dataExportServiceCallType, serviceCallCPS);
            serviceCallService.addServiceCallHandler(serviceCallHandler, ImmutableMap.of("name", WebServiceDataExportServiceCallHandler.NAME));
            return null;
        });
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void testStart() {
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, TIMEOUT);

        assertThat(serviceCall.getOrigin()).contains("Pulse");
        assertProperties(serviceCall, DefaultState.ONGOING, UUID, TIMEOUT, null);
    }

    @Test
    @Transactional
    public void testStartAsync() {
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCallAsync(UUID, TIMEOUT);

        assertThat(serviceCall.getOrigin()).contains("Pulse");
        assertProperties(serviceCall, DefaultState.ONGOING, UUID, TIMEOUT, null);
    }

    @Test
    @Transactional
    public void testFindServiceCall() {
        assertThat(dataExportServiceCallType.findServiceCall(UUID)).isEmpty();

        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, TIMEOUT);

        assertThat(dataExportServiceCallType.findServiceCall(UUID)).contains(serviceCall);
        assertThat(dataExportServiceCallType.findServiceCall(ANOTHER_UUID)).isEmpty();
    }

    @Test
    @Transactional
    public void testFailServiceCall() {
        String error = "Errorrrr";
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, TIMEOUT);
        dataExportServiceCallType.tryFailingServiceCall(serviceCall, error);

        assertProperties(serviceCall, DefaultState.FAILED, UUID, TIMEOUT, error);

        // check that further operations don't change the state
        dataExportServiceCallType.tryPassingServiceCall(serviceCall);
        assertProperties(serviceCall, DefaultState.FAILED, UUID, TIMEOUT, error);

        dataExportServiceCallType.tryFailingServiceCall(serviceCall, "Another error!");
        assertProperties(serviceCall, DefaultState.FAILED, UUID, TIMEOUT, error);
    }

    @Test
    @Transactional
    public void testPassServiceCall() {
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, TIMEOUT);
        dataExportServiceCallType.tryPassingServiceCall(serviceCall);

        assertProperties(serviceCall, DefaultState.SUCCESSFUL, UUID, TIMEOUT, null);

        // check that further operations don't change the state
        dataExportServiceCallType.tryPassingServiceCall(serviceCall);

        dataExportServiceCallType.tryFailingServiceCall(serviceCall, "Another error!");
        assertProperties(serviceCall, DefaultState.SUCCESSFUL, UUID, TIMEOUT, null);
    }

    @Test
    @Transactional
    public void testGetOngoingStatus() {
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, TIMEOUT);
        ServiceCallStatus status = dataExportServiceCallType.getStatus(serviceCall);

        assertThat(status.getServiceCall()).isEqualTo(serviceCall);
        assertThat(status.getState()).isSameAs(DefaultState.ONGOING);
        assertThat(status.isOpen()).isTrue();
        assertThat(status.isFailed()).isFalse();
        assertThat(status.isSuccessful()).isFalse();
        assertThat(status.getErrorMessage()).isEmpty();
    }

    @Test
    @Transactional
    public void testGetSuccessfulStatus() {
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, TIMEOUT);
        dataExportServiceCallType.tryPassingServiceCall(serviceCall);
        ServiceCallStatus status = dataExportServiceCallType.getStatus(serviceCall);

        assertThat(status.getServiceCall()).isEqualTo(serviceCall);
        assertThat(status.getState()).isSameAs(DefaultState.SUCCESSFUL);
        assertThat(status.isOpen()).isFalse();
        assertThat(status.isFailed()).isFalse();
        assertThat(status.isSuccessful()).isTrue();
        assertThat(status.getErrorMessage()).isEmpty();
    }

    @Test
    @Transactional
    public void testGetFailedStatus() {
        String error = "myError";
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, TIMEOUT);
        dataExportServiceCallType.tryFailingServiceCall(serviceCall, error);
        ServiceCallStatus status = dataExportServiceCallType.getStatus(serviceCall);

        assertThat(status.getServiceCall()).isEqualTo(serviceCall);
        assertThat(status.getState()).isSameAs(DefaultState.FAILED);
        assertThat(status.isOpen()).isFalse();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isSuccessful()).isFalse();
        assertThat(status.getErrorMessage()).contains(error);
    }

    @Test
    @Transactional
    public void testGetAnotherStatus() {
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, TIMEOUT);
        serviceCall.requestTransition(DefaultState.PAUSED);
        ServiceCallStatus status = dataExportServiceCallType.getStatus(serviceCall);

        assertThat(status.getServiceCall()).isEqualTo(serviceCall);
        assertThat(status.getState()).isSameAs(DefaultState.PAUSED);
        assertThat(status.isOpen()).isTrue();
        assertThat(status.isFailed()).isFalse();
        assertThat(status.isSuccessful()).isFalse();
        assertThat(status.getErrorMessage()).isEmpty();
    }

    @Test
    @Transactional
    public void testServiceCallHandlerDirectExceptionFailsServiceCallWithoutExtension() {
        ServiceCall serviceCall = serviceCallService.getServiceCallTypes().find().get(0).newServiceCall().create();
        serviceCall.requestTransition(DefaultState.PENDING);
        serviceCall.requestTransition(DefaultState.ONGOING);
        serviceCallHandler.onStateChange(serviceCall, DefaultState.PENDING, DefaultState.ONGOING);
        ServiceCallStatus status = dataExportServiceCallType.getStatus(serviceCall);

        assertThat(status.getServiceCall()).isEqualTo(serviceCall);
        assertThat(status.getState()).isSameAs(DefaultState.FAILED);
        assertThat(status.isOpen()).isFalse();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isSuccessful()).isFalse();
        assertThat(status.getErrorMessage()).isEmpty();
    }

    @Test
    @Transactional
    public void testServiceCallHandlerExecutionExceptionFailsServiceCall() {
        long timeout = WebServiceDataExportServiceCallHandler.CHECK_PAUSE_IN_SECONDS * 1500; // 1.5 times the pause before status check
        String error = "Error mess";
        ServiceCall realServiceCall = dataExportServiceCallType.startServiceCall(UUID, timeout);
        ServiceCall serviceCall = mock(ServiceCall.class);
        WebServiceDataExportDomainExtension extension = mock(WebServiceDataExportDomainExtension.class);
        when(serviceCall.getExtensionFor(serviceCallCPS)).thenReturn(Optional.of(extension));
        when(extension.getTimeout()).thenReturn(timeout);
        when(serviceCall.getId()).thenAnswer(new Answer<Long>() {
            private boolean notTheFirstTime;

            @Override
            public Long answer(InvocationOnMock invocationOnMock) throws Throwable {
                if (notTheFirstTime) {
                    return realServiceCall.getId(); // let the results be kept for realServiceCall
                }
                notTheFirstTime = true;
                throw new UnsupportedOperationException(error); // but make serviceCallType.getStatus(serviceCall) fail
            }
        });
        serviceCallHandler.onStateChange(serviceCall, DefaultState.PENDING, DefaultState.ONGOING);

        assertProperties(realServiceCall, DefaultState.FAILED, UUID, timeout, "Failure while waiting for data export confirmation: " + error);
    }

    @Test
    @Transactional
    public void testServiceCallHandlerTimeoutExceptionFailsServiceCall() {
        long timeout = 1; // less than the pause before status check
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, timeout);
        serviceCallHandler.onStateChange(serviceCall, DefaultState.PENDING, DefaultState.ONGOING);

        assertProperties(serviceCall, DefaultState.FAILED, UUID, timeout, "No data export confirmation has been received in the configured timeout.");
    }

    @Test
    @Transactional
    public void testServiceCallHandlerPasses() {
        long timeout = WebServiceDataExportServiceCallHandler.CHECK_PAUSE_IN_SECONDS * 1500; // 1.5 times the pause before status check
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, timeout);
        serviceCall.requestTransition(DefaultState.SUCCESSFUL);
        serviceCallHandler.onStateChange(serviceCall, DefaultState.PENDING, DefaultState.ONGOING);

        assertProperties(serviceCall, DefaultState.SUCCESSFUL, UUID, timeout, null);
    }

    @Test
    @Transactional
    public void testServiceCallHandlerDoesNothing() {
        serviceCallHandler.onStateChange(null, DefaultState.CREATED, DefaultState.PENDING);
        serviceCallHandler.onStateChange(null, DefaultState.ONGOING, DefaultState.SUCCESSFUL);
        serviceCallHandler.onStateChange(null, DefaultState.ONGOING, DefaultState.FAILED);
        serviceCallHandler.onStateChange(null, DefaultState.ONGOING, DefaultState.CANCELLED);
    }

    private static void assertProperties(ServiceCall serviceCall, DefaultState state, String uuid, long timeout, String error) {
        serviceCall = serviceCallService.getServiceCall(serviceCall.getId())
                .orElseThrow(() -> new AssertionError("Just started service call isn't found in system."));
        assertThat(serviceCall.getState()).isSameAs(state);
        WebServiceDataExportDomainExtension extension = serviceCall.getExtension(WebServiceDataExportDomainExtension.class)
                .orElseThrow(() -> new AssertionError("No custom properties are saved for the service call."));
        assertThat(extension.getErrorMessage()).isEqualTo(error);
        assertThat(extension.getUuid()).isEqualTo(uuid);
        assertThat(extension.getTimeout()).isEqualTo(timeout);
    }
}

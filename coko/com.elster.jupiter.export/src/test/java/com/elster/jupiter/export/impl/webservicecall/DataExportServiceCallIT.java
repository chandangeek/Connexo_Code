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
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.impl.DataExportServiceImpl;
import com.elster.jupiter.export.impl.ExportModule;
import com.elster.jupiter.export.impl.NullDataFormatterFactory;
import com.elster.jupiter.export.impl.ReadingDataSelectorConfigImpl;
import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.export.webservicecall.ServiceCallStatus;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fsm.StateTransitionPropertiesProvider;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.fsm.impl.StateTransitionTriggerEventTopicHandler;
import com.elster.jupiter.ftpclient.impl.FtpModule;
import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.mail.impl.MailModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.impl.ServiceCallModule;
import com.elster.jupiter.servicecall.impl.ServiceCallStateChangeTopicHandler;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativeField;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.validation.impl.ValidationModule;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

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
            bind(Thesaurus.class).toInstance(NlsModule.FakeThesaurus.INSTANCE);
            bind(TokenService.class).toInstance(mock(TokenService.class));
        }
    }

    private static final long TIMEOUT = 1000;
    private static final String UUID = "facade3e-c632-11e8-a355-529269fb1459";
    private static final String ANOTHER_UUID = "4ff58443-1234-4aed-ae6e-d1cd4db81f21";
    private static final String DEVICE_NAME_1 = "DEVICE_1";
    private static final String MRID_1 = "0.0.5.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String DEVICE_NAME_2 = "DEVICE_2";
    private static final String MRID_2 = "0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0";
    private static Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static Injector injector;
    private static ServiceCallService serviceCallService;
    private static DataExportServiceCallType dataExportServiceCallType;
    private static CustomPropertySet<ServiceCall, WebServiceDataExportDomainExtension> serviceCallCPS;
    private static TransactionService transactionService;
    private static long dataSourceId1;
    private static long dataSourceId2;
    private static List<ReadingTypeDataExportItem> itemList;
    private static Map<ReadingTypeDataExportItem, String> data;

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
                    new MeteringModule(MRID_1, MRID_2),
                    new PartyModule(),
                    new DataVaultModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new H2OrmModule(),
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
            DataExportServiceImpl dataExportService = injector.getInstance(DataExportServiceImpl.class);
            serviceCallCPS = new WebServiceDataExportCustomPropertySet(thesaurus, propertySpecService, dataExportService);
            injector.getInstance(CustomPropertySetService.class).addCustomPropertySet(serviceCallCPS);
            dataExportServiceCallType = dataExportService.getDataExportServiceCallType();

            MeteringService meteringService = injector.getInstance(MeteringService.class);
            AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
            Meter meter1 = amrSystem.newMeter(DEVICE_NAME_1, DEVICE_NAME_1).create();
            Meter meter2 = amrSystem.newMeter(DEVICE_NAME_2, DEVICE_NAME_2).create();
            ReadingType readingType1 = meteringService.getReadingType(MRID_1).get();
            ReadingType readingType2 = meteringService.getReadingType(MRID_2).get();
            meter1.activate(Instant.now()).getChannelsContainer().createChannel(readingType1);
            meter2.activate(Instant.now()).getChannelsContainer().createChannel(readingType2);
            EndDeviceGroup group = injector.getInstance(MeteringGroupsService.class).createEnumeratedEndDeviceGroup(meter1, meter2).setName("group").create();
            DataFormatterFactory nullFormatterFactory = injector.getInstance(NullDataFormatterFactory.class);
            dataExportService.addFormatter(nullFormatterFactory, ImmutableMap.of(DataExportService.DATA_TYPE_PROPERTY, DataExportService.STANDARD_READING_DATA_TYPE));
            TimeService timeService = injector.getInstance(TimeService.class);
            RelativePeriod relativePeriod = timeService
                    .createRelativePeriod("Name", RelativeDate.NOW.with(RelativeField.MONTH.minus(1)), RelativeDate.NOW, timeService.getRelativePeriodCategories());
            ExportTask exportTask = dataExportService.newBuilder()
                    .setName("Export task")
                    .setApplication("Admin")
                    .setScheduleExpression(Never.NEVER)
                    .selectingMeterReadings()
                    .fromExportPeriod(relativePeriod)
                    .fromEndDeviceGroup(group)
                    .fromReadingType(readingType1)
                    .fromReadingType(readingType2)
                    .endSelection()
                    .setDataFormatterFactoryName(nullFormatterFactory.getName())
                    .create();
            ReadingDataSelectorConfigImpl selectorConfig = (ReadingDataSelectorConfigImpl) exportTask.getReadingDataSelectorConfig().get();
            selectorConfig.addExportItem(meter1, readingType1);
            selectorConfig.addExportItem(meter2, readingType2);
            selectorConfig.save();

            dataSourceId1 = selectorConfig.getExportItems().stream()
                    .filter(item -> item.getReadingType().getMRID().equals(MRID_1))
                    .findAny()
                    .get()
                    .getId();
            dataSourceId2 = selectorConfig.getExportItems().stream()
                    .filter(item -> item.getReadingType().getMRID().equals(MRID_2))
                    .findAny()
                    .get()
                    .getId();

            itemList = selectorConfig.getExportItems();
            data = new HashMap<>();
            for (ReadingTypeDataExportItem item : itemList) {
                data.put(item, "");
            }

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
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, TIMEOUT, data);

        List<ServiceCall> srvCallChild = serviceCall.findChildren().stream().collect(Collectors.toList());

        assertThat(serviceCall.getOrigin()).contains("Pulse");
        assertProperties(serviceCall, DefaultState.ONGOING, UUID, TIMEOUT, null);
        assertThat(srvCallChild.get(0).getState()).isEqualTo(DefaultState.ONGOING);
        assertThat(srvCallChild.get(1).getState()).isEqualTo(DefaultState.ONGOING);

        assertThat(srvCallChild.stream().filter(srvCallChd -> {
            WebServiceDataExportChildDomainExtension properties = srvCallChd.getExtension(WebServiceDataExportChildDomainExtension.class).get();
            return properties.getDeviceName().equals(DEVICE_NAME_1) &&
                    properties.getReadingTypeMRID().equals(MRID_1) &&
                    properties.getDataSourceId() == dataSourceId1;
        }).findFirst()).isPresent();

        assertThat(srvCallChild.stream().filter(srvCallChd -> {
            WebServiceDataExportChildDomainExtension properties = srvCallChd.getExtension(WebServiceDataExportChildDomainExtension.class).get();
            return properties.getDeviceName().equals(DEVICE_NAME_2) &&
                    properties.getReadingTypeMRID().equals(MRID_2) &&
                    properties.getDataSourceId() == dataSourceId2;
        }).findFirst()).isPresent();
    }

    @Test
    @Transactional
    public void testStartAsync() {
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCallAsync(UUID, TIMEOUT, data);

        assertThat(serviceCall.getOrigin()).contains("Pulse");
        List<ServiceCall> srvCallChild = serviceCall.findChildren().stream().collect(Collectors.toList());

        assertProperties(serviceCall, DefaultState.ONGOING, UUID, TIMEOUT, null);
        assertThat(srvCallChild.get(0).getState()).isEqualTo(DefaultState.ONGOING);
        assertThat(srvCallChild.get(1).getState()).isEqualTo(DefaultState.ONGOING);

        assertThat(srvCallChild.stream().filter(srvCallChd -> {
            WebServiceDataExportChildDomainExtension properties = srvCallChd.getExtension(WebServiceDataExportChildDomainExtension.class).get();
            return properties.getDeviceName().equals(DEVICE_NAME_1) &&
                    properties.getReadingTypeMRID().equals(MRID_1) &&
                    properties.getDataSourceId() == dataSourceId1;
        }).findFirst()).isPresent();

        assertThat(srvCallChild.stream().filter(srvCallChd -> {
            WebServiceDataExportChildDomainExtension properties = srvCallChd.getExtension(WebServiceDataExportChildDomainExtension.class).get();
            return properties.getDeviceName().equals(DEVICE_NAME_2) &&
                    properties.getReadingTypeMRID().equals(MRID_2) &&
                    properties.getDataSourceId() == dataSourceId2;
        }).findFirst()).isPresent();

        // clear what was created in another transaction
        transactionService.runInIndependentTransaction(serviceCall::delete);
    }

    @Test
    @Transactional
    public void testStartAsyncWithNoDataSources() {
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCallAsync(UUID, TIMEOUT, Collections.emptyMap());

        assertThat(serviceCall.getOrigin()).contains("Pulse");
        assertThat(serviceCall.findChildren().stream().collect(Collectors.toList())).isEmpty();

        assertProperties(serviceCall, DefaultState.ONGOING, UUID, TIMEOUT, null);

        // clear what was created in another transaction
        transactionService.runInIndependentTransaction(serviceCall::delete);
    }

    @Test
    @Transactional
    public void testFindServiceCall() {
        assertThat(dataExportServiceCallType.findServiceCall(UUID)).isEmpty();

        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, TIMEOUT, data);

        List<ServiceCall> srvCallChild = serviceCall.findChildren().stream().collect(Collectors.toList());

        assertThat(dataExportServiceCallType.findServiceCall(UUID)).contains(serviceCall);
        assertThat(dataExportServiceCallType.findServiceCall(ANOTHER_UUID)).isEmpty();

        assertThat(srvCallChild.get(0).getState()).isEqualTo(DefaultState.ONGOING);
        assertThat(srvCallChild.get(1).getState()).isEqualTo(DefaultState.ONGOING);

        assertThat(srvCallChild.stream().filter(srvCallChd -> {
            WebServiceDataExportChildDomainExtension properties = srvCallChd.getExtension(WebServiceDataExportChildDomainExtension.class).get();
            return properties.getDeviceName().equals(DEVICE_NAME_1) &&
                    properties.getReadingTypeMRID().equals(MRID_1) &&
                    properties.getDataSourceId() == dataSourceId1;
        }).findFirst()).isPresent();

        assertThat(srvCallChild.stream().filter(srvCallChd -> {
            WebServiceDataExportChildDomainExtension properties = srvCallChd.getExtension(WebServiceDataExportChildDomainExtension.class).get();
            return properties.getDeviceName().equals(DEVICE_NAME_2) &&
                    properties.getReadingTypeMRID().equals(MRID_2) &&
                    properties.getDataSourceId() == dataSourceId2;
        }).findFirst()).isPresent();
    }

    @Test
    @Transactional
    public void testFailServiceCall() {
        String error = "Errorrrr";
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, TIMEOUT, data);
        dataExportServiceCallType.tryFailingServiceCall(serviceCall, error);
        List<ServiceCall> srvCallChild = serviceCall.findChildren().stream().collect(Collectors.toList());

        assertProperties(serviceCall, DefaultState.FAILED, UUID, TIMEOUT, error);

        // check that further operations don't change the state
        dataExportServiceCallType.tryPassingServiceCall(serviceCall);
        assertProperties(serviceCall, DefaultState.FAILED, UUID, TIMEOUT, error);

        dataExportServiceCallType.tryFailingServiceCall(serviceCall, "Another error!");
        assertProperties(serviceCall, DefaultState.FAILED, UUID, TIMEOUT, error);

        assertThat(srvCallChild.get(0).getState()).isEqualTo(DefaultState.FAILED);
        assertThat(srvCallChild.get(1).getState()).isEqualTo(DefaultState.FAILED);
        assertThat(srvCallChild.stream().filter(srvCallChd -> {
            WebServiceDataExportChildDomainExtension properties = srvCallChd.getExtension(WebServiceDataExportChildDomainExtension.class).get();
            return properties.getDeviceName().equals(DEVICE_NAME_1) &&
                    properties.getReadingTypeMRID().equals(MRID_1) &&
                    properties.getDataSourceId() == dataSourceId1;
        }).findFirst()).isPresent();

        assertThat(srvCallChild.stream().filter(srvCallChd -> {
            WebServiceDataExportChildDomainExtension properties = srvCallChd.getExtension(WebServiceDataExportChildDomainExtension.class).get();
            return properties.getDeviceName().equals(DEVICE_NAME_2) &&
                    properties.getReadingTypeMRID().equals(MRID_2) &&
                    properties.getDataSourceId() == dataSourceId2;
        }).findFirst()).isPresent();
    }

    @Test
    @Transactional
    public void testPartiallyFailServiceCall() {
        String error = "Errorrrr";
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, TIMEOUT, data);
        List<ServiceCall> srvCallChildren = serviceCall.findChildren().stream().collect(Collectors.toList());
        assertThat(srvCallChildren).hasSize(2);
        dataExportServiceCallType.tryPartiallyPassingServiceCall(serviceCall, Collections.singletonList(srvCallChildren.get(0)), error);

        assertProperties(serviceCall, DefaultState.PARTIAL_SUCCESS, UUID, TIMEOUT, error);

        // check that further operations don't change the state
        dataExportServiceCallType.tryPassingServiceCall(serviceCall);
        assertProperties(serviceCall, DefaultState.PARTIAL_SUCCESS, UUID, TIMEOUT, error);

        dataExportServiceCallType.tryFailingServiceCall(serviceCall, "Another error!");
        assertProperties(serviceCall, DefaultState.PARTIAL_SUCCESS, UUID, TIMEOUT, error);

        assertThat(srvCallChildren.stream().filter(srvCallChd -> {
            WebServiceDataExportChildDomainExtension properties = srvCallChd.getExtension(WebServiceDataExportChildDomainExtension.class).get();
            return properties.getDeviceName().equals(DEVICE_NAME_1) &&
                    properties.getReadingTypeMRID().equals(MRID_1) &&
                    properties.getDataSourceId() == dataSourceId1;
        }).findFirst()).isPresent();

        assertThat(srvCallChildren.stream().filter(srvCallChd -> {
            WebServiceDataExportChildDomainExtension properties = srvCallChd.getExtension(WebServiceDataExportChildDomainExtension.class).get();
            return properties.getDeviceName().equals(DEVICE_NAME_2) &&
                    properties.getReadingTypeMRID().equals(MRID_2) &&
                    properties.getDataSourceId() == dataSourceId2;
        }).findFirst()).isPresent();
        assertThat(dataExportServiceCallType.getStatus(srvCallChildren.get(0)).getState()).isEqualTo(DefaultState.SUCCESSFUL);
        assertThat(dataExportServiceCallType.getStatus(srvCallChildren.get(1)).getState()).isEqualTo(DefaultState.FAILED);
    }

    @Test
    @Transactional
    public void testPassServiceCall() {
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, TIMEOUT, data);
        dataExportServiceCallType.tryPassingServiceCall(serviceCall);

        List<ServiceCall> srvCallChild = serviceCall.findChildren().stream().collect(Collectors.toList());

        assertProperties(serviceCall, DefaultState.SUCCESSFUL, UUID, TIMEOUT, null);

        // check that further operations don't change the state
        dataExportServiceCallType.tryPassingServiceCall(serviceCall);

        dataExportServiceCallType.tryFailingServiceCall(serviceCall, "Another error!");
        assertProperties(serviceCall, DefaultState.SUCCESSFUL, UUID, TIMEOUT, null);

        assertThat(srvCallChild.get(0).getState()).isEqualTo(DefaultState.SUCCESSFUL);
        assertThat(srvCallChild.get(1).getState()).isEqualTo(DefaultState.SUCCESSFUL);
        assertThat(srvCallChild.stream().filter(srvCallChd -> {
            WebServiceDataExportChildDomainExtension properties = srvCallChd.getExtension(WebServiceDataExportChildDomainExtension.class).get();
            return properties.getDeviceName().equals(DEVICE_NAME_1) &&
                    properties.getReadingTypeMRID().equals(MRID_1) &&
                    properties.getDataSourceId() == dataSourceId1;
        }).findFirst()).isPresent();

        assertThat(srvCallChild.stream().filter(srvCallChd -> {
            WebServiceDataExportChildDomainExtension properties = srvCallChd.getExtension(WebServiceDataExportChildDomainExtension.class).get();
            return properties.getDeviceName().equals(DEVICE_NAME_2) &&
                    properties.getReadingTypeMRID().equals(MRID_2) &&
                    properties.getDataSourceId() == dataSourceId2;
        }).findFirst()).isPresent();
    }

    @Test
    @Transactional
    public void testGetOngoingStatus() {
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, TIMEOUT, data);
        ServiceCallStatus status = dataExportServiceCallType.getStatus(serviceCall);

        List<ServiceCall> srvCallChild = serviceCall.findChildren().stream().collect(Collectors.toList());

        assertThat(status.getServiceCall()).isEqualTo(serviceCall);
        assertThat(status.getState()).isSameAs(DefaultState.ONGOING);
        assertThat(status.isOpen()).isTrue();
        assertThat(status.isFailed()).isFalse();
        assertThat(status.isSuccessful()).isFalse();
        assertThat(status.getErrorMessage()).isEmpty();

        assertThat(srvCallChild.get(0).getState()).isEqualTo(DefaultState.ONGOING);
        assertThat(srvCallChild.get(1).getState()).isEqualTo(DefaultState.ONGOING);
        assertThat(srvCallChild.stream().filter(srvCallChd -> {
            WebServiceDataExportChildDomainExtension properties = srvCallChd.getExtension(WebServiceDataExportChildDomainExtension.class).get();
            return properties.getDeviceName().equals(DEVICE_NAME_1) &&
                    properties.getReadingTypeMRID().equals(MRID_1) &&
                    properties.getDataSourceId() == dataSourceId1;
        }).findFirst()).isPresent();

        assertThat(srvCallChild.stream().filter(srvCallChd -> {
            WebServiceDataExportChildDomainExtension properties = srvCallChd.getExtension(WebServiceDataExportChildDomainExtension.class).get();
            return properties.getDeviceName().equals(DEVICE_NAME_2) &&
                    properties.getReadingTypeMRID().equals(MRID_2) &&
                    properties.getDataSourceId() == dataSourceId2;
        }).findFirst()).isPresent();
    }

    @Test
    @Transactional
    public void testGetSuccessfulStatus() {
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, TIMEOUT, data);
        dataExportServiceCallType.tryPassingServiceCall(serviceCall);
        ServiceCallStatus status = dataExportServiceCallType.getStatus(serviceCall);

        List<ServiceCall> srvCallChild = serviceCall.findChildren().stream().collect(Collectors.toList());

        assertThat(status.getServiceCall()).isEqualTo(serviceCall);
        assertThat(status.getState()).isSameAs(DefaultState.SUCCESSFUL);
        assertThat(status.isOpen()).isFalse();
        assertThat(status.isFailed()).isFalse();
        assertThat(status.isSuccessful()).isTrue();
        assertThat(status.getErrorMessage()).isEmpty();

        assertThat(srvCallChild.get(0).getState()).isEqualTo(DefaultState.SUCCESSFUL);
        assertThat(srvCallChild.get(1).getState()).isEqualTo(DefaultState.SUCCESSFUL);
        assertThat(srvCallChild.stream().filter(srvCallChd -> {
            WebServiceDataExportChildDomainExtension properties = srvCallChd.getExtension(WebServiceDataExportChildDomainExtension.class).get();
            return properties.getDeviceName().equals(DEVICE_NAME_1) &&
                    properties.getReadingTypeMRID().equals(MRID_1) &&
                    properties.getDataSourceId() == dataSourceId1;
        }).findFirst()).isPresent();

        assertThat(srvCallChild.stream().filter(srvCallChd -> {
            WebServiceDataExportChildDomainExtension properties = srvCallChd.getExtension(WebServiceDataExportChildDomainExtension.class).get();
            return properties.getDeviceName().equals(DEVICE_NAME_2) &&
                    properties.getReadingTypeMRID().equals(MRID_2) &&
                    properties.getDataSourceId() == dataSourceId2;
        }).findFirst()).isPresent();
    }

    @Test
    @Transactional
    public void testGetFailedStatus() {
        String error = "myError";
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, TIMEOUT, data);
        dataExportServiceCallType.tryFailingServiceCall(serviceCall, error);
        ServiceCallStatus status = dataExportServiceCallType.getStatus(serviceCall);
        List<ServiceCall> srvCallChild = serviceCall.findChildren().stream().collect(Collectors.toList());

        assertThat(status.getServiceCall()).isEqualTo(serviceCall);
        assertThat(status.getState()).isSameAs(DefaultState.FAILED);
        assertThat(status.isOpen()).isFalse();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isSuccessful()).isFalse();
        assertThat(status.getErrorMessage()).contains(error);

        assertThat(srvCallChild.get(0).getState()).isEqualTo(DefaultState.FAILED);
        assertThat(srvCallChild.get(1).getState()).isEqualTo(DefaultState.FAILED);
        assertThat(srvCallChild.stream().filter(srvCallChd -> {
            WebServiceDataExportChildDomainExtension properties = srvCallChd.getExtension(WebServiceDataExportChildDomainExtension.class).get();
            return properties.getDeviceName().equals(DEVICE_NAME_1) &&
                    properties.getReadingTypeMRID().equals(MRID_1) &&
                    properties.getDataSourceId() == dataSourceId1;
        }).findFirst()).isPresent();

        assertThat(srvCallChild.stream().filter(srvCallChd -> {
            WebServiceDataExportChildDomainExtension properties = srvCallChd.getExtension(WebServiceDataExportChildDomainExtension.class).get();
            return properties.getDeviceName().equals(DEVICE_NAME_2) &&
                    properties.getReadingTypeMRID().equals(MRID_2) &&
                    properties.getDataSourceId() == dataSourceId2;
        }).findFirst()).isPresent();
    }

    @Test
    @Transactional
    public void testGetAnotherStatus() {
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, TIMEOUT, data);
        serviceCall.requestTransition(DefaultState.PAUSED);
        ServiceCallStatus status = dataExportServiceCallType.getStatus(serviceCall);
        List<ServiceCall> srvCallChild = serviceCall.findChildren().stream().collect(Collectors.toList());

        assertThat(status.getServiceCall()).isEqualTo(serviceCall);
        assertThat(status.getState()).isSameAs(DefaultState.PAUSED);
        assertThat(status.isOpen()).isTrue();
        assertThat(status.isFailed()).isFalse();
        assertThat(status.isSuccessful()).isFalse();
        assertThat(status.getErrorMessage()).isEmpty();

        assertThat(srvCallChild.get(0).getState()).isEqualTo(DefaultState.ONGOING);
        assertThat(srvCallChild.get(1).getState()).isEqualTo(DefaultState.ONGOING);
    }

    @Test
    @Transactional
    public void testGetStatuses() {
        ServiceCall serviceCall1 = dataExportServiceCallType.startServiceCall(UUID, TIMEOUT, data);
        dataExportServiceCallType.tryFailingServiceCall(serviceCall1, "Fail!");
        ServiceCall serviceCall2 = dataExportServiceCallType.startServiceCall(ANOTHER_UUID, TIMEOUT, data);
        Map<ServiceCall, ServiceCallStatus> statuses = dataExportServiceCallType.getStatuses(ImmutableSet.of(serviceCall1, serviceCall2)).stream()
                .collect(Collectors.toMap(ServiceCallStatus::getServiceCall, Function.identity()));

        assertThat(statuses.keySet()).containsOnly(serviceCall1, serviceCall2);
        ServiceCallStatus status = statuses.get(serviceCall1);
        assertThat(status).isNotNull();
        assertThat(status.getState()).isSameAs(DefaultState.FAILED);
        assertThat(status.isOpen()).isFalse();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isSuccessful()).isFalse();
        assertThat(status.getErrorMessage()).contains("Fail!");
        status = statuses.get(serviceCall2);
        assertThat(status).isNotNull();
        assertThat(status.getState()).isSameAs(DefaultState.ONGOING);
        assertThat(status.isOpen()).isTrue();
        assertThat(status.isFailed()).isFalse();
        assertThat(status.isSuccessful()).isFalse();
        assertThat(status.getErrorMessage()).isEmpty();
    }

    @Test
    @Transactional
    public void testGetDataSources() {
        ServiceCall serviceCall = dataExportServiceCallType.startServiceCall(UUID, TIMEOUT, data);
        assertThat(dataExportServiceCallType.getDataSources(serviceCall.findChildren()
                .stream()
                .collect(Collectors.toList()))).containsOnly(itemList.toArray(new ReadingTypeDataExportItem[itemList.size()]));
        serviceCall = dataExportServiceCallType.startServiceCall(ANOTHER_UUID, TIMEOUT, Collections.emptyMap());
        assertThat(dataExportServiceCallType.getDataSources(serviceCall.findChildren().stream().collect(Collectors.toList()))).isEmpty();
        serviceCall = dataExportServiceCallType.startServiceCall("UUID", TIMEOUT, ImmutableMap.of(itemList.get(0), ""));
        assertThat(dataExportServiceCallType.getDataSources(serviceCall.findChildren().stream().collect(Collectors.toList()))).containsOnly(itemList.get(0));
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

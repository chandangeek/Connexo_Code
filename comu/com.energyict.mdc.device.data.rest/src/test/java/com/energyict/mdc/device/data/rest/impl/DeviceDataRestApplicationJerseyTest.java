package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.rest.DeviceStateAccessFeature;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.favorites.FavoritesService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.SystemComTask;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.*;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 9/19/14.
 */
public class DeviceDataRestApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    static long firmwareComTaskId = 445632136865L;

    @Mock(extraInterfaces = ComTask.class)
    SystemComTask firmwareComTask;
    @Mock
    ConnectionTaskService connectionTaskService;
    @Mock
    DeviceService deviceService;
    @Mock
    TopologyService topologyService;
    @Mock
    BatchService batchService;
    @Mock
    DeviceConfigurationService deviceConfigurationService;
    @Mock
    EngineConfigurationService engineConfigurationService;
    @Mock
    IssueService issueService;
    @Mock
    IssueDataValidationService issueDataValidationService;
    @Mock
    SchedulingService schedulingService;
    @Mock
    ValidationService validationService;
    @Mock
    EstimationService estimationService;
    @Mock
    Clock clock;
    @Mock
    MasterDataService masterDataService;
    @Mock
    JsonService jsonService;
    @Mock
    ProtocolPluggableService protocolPluggableService;
    @Mock
    MeteringGroupsService meteringGroupService;
    @Mock
    MeteringService meteringService;
    @Mock
    RestQueryService restQueryService;
    @Mock
    DeviceMessageSpecificationService deviceMessageSpecificationService;
    @Mock
    TaskService taskService;
    @Mock
    CommunicationTaskService communicationTaskService;
    @Mock
    PropertySpecService propertySpecService;
    @Mock
    FavoritesService favoritesService;
    @Mock
    DataCollectionKpiService dataCollectionKpiService;
    @Mock
    YellowfinGroupsService yellowfinGroupsService;
    @Mock
    FirmwareService firmwareService;
    @Mock
    DeviceLifeCycleService deviceLifeCycleService;
    @Mock
    AppService appService;
    @Mock
    MessageService messageService;

    @Before
    public void setup() {
        when(thesaurus.getStringBeyondComponent(any(String.class), any(String.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[1]);
        when(taskService.findComTask(anyLong())).thenReturn(Optional.empty());
        when(taskService.findComTask(firmwareComTaskId)).thenReturn(Optional.of(firmwareComTask));
        when(firmwareComTask.isSystemComTask()).thenReturn(true);
        when(firmwareComTask.isUserComTask()).thenReturn(false);
    }

    protected boolean disableDeviceConstraintsBasedOnDeviceState(){
        return true;
    }

    @Override
    protected Application getApplication() {
        DeviceApplication application = new DeviceApplication(){
            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> classes = new HashSet<>(super.getClasses());
                if (disableDeviceConstraintsBasedOnDeviceState()){
                    classes.remove(DeviceStateAccessFeature.class);
                }
                return classes;
            }
        };
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setMasterDataService(masterDataService);
        application.setDeviceConfigurationService(deviceConfigurationService);
        application.setJsonService(jsonService);
        application.setProtocolPluggableService(protocolPluggableService);
        application.setClockService(clock);
        application.setConnectionTaskService(connectionTaskService);
        application.setDeviceService(deviceService);
        application.setTopologyService(topologyService);
        application.setBatchService(batchService);
        application.setEngineConfigurationService(engineConfigurationService);
        application.setIssueService(issueService);
        application.setIssueDataValidationService(issueDataValidationService);
        application.setMeteringGroupsService(meteringGroupService);
        application.setMeteringService(meteringService);
        application.setSchedulingService(schedulingService);
        application.setValidationService(validationService);
        application.setEstimationService(estimationService);
        application.setRestQueryService(restQueryService);
        application.setTaskService(taskService);
        application.setCommunicationTaskService(communicationTaskService);
        application.setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        application.setFavoritesService(favoritesService);
        application.setDataCollectionKpiService(dataCollectionKpiService);
        application.setYellowfinGroupsService(yellowfinGroupsService);
        application.setFirmwareService(firmwareService);
        application.setDeviceLifeCycleService(deviceLifeCycleService);
        application.setAppService(appService);
        application.setMessageService(messageService);
        return application;
    }

    public ReadingType mockReadingType(String mrid){
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(mrid);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        when(readingType.getAggregate()).thenReturn(Aggregate.AVERAGE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK1MIN);
        when(readingType.getAccumulation()).thenReturn(Accumulation.BULKQUANTITY);
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.FORWARD);
        when(readingType.getCommodity()).thenReturn(Commodity.AIR);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ACVOLTAGEPEAK);
        when(readingType.getInterharmonic()).thenReturn(new RationalNumber(1,2));
        when(readingType.getArgument()).thenReturn(new RationalNumber(1,2));
        when(readingType.getTou()).thenReturn(3);
        when(readingType.getCpp()).thenReturn(4);
        when(readingType.getConsumptionTier()).thenReturn(5);
        when(readingType.getPhases()).thenReturn(Phase.PHASEA);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.CENTI);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(readingType.getCurrency()).thenReturn(Currency.getInstance("EUR"));
        return readingType;
    }

    AppServer mockAppServers(String ...name) {
        AppServer appServer = mock(AppServer.class);
        List<SubscriberExecutionSpec> execSpecs = new ArrayList<>();
        for (String specName: name) {
            SubscriberExecutionSpec subscriberExecutionSpec = mock(SubscriberExecutionSpec.class);
            SubscriberSpec spec = mock(SubscriberSpec.class);
            when(subscriberExecutionSpec.getSubscriberSpec()).thenReturn(spec);
            DestinationSpec destinationSpec = mock(DestinationSpec.class);
            when(spec.getDestination()).thenReturn(destinationSpec);
            when(destinationSpec.getName()).thenReturn(specName);
            when(destinationSpec.isActive()).thenReturn(true);
            List<SubscriberSpec> list = mock(List.class);
            when(list.isEmpty()).thenReturn(false);
            when(destinationSpec.getSubscribers()).thenReturn(list);
            execSpecs.add(subscriberExecutionSpec);
        }
        doReturn(execSpecs).when(appServer).getSubscriberExecutionSpecs();
        when(appServer.isActive()).thenReturn(true);
        when(appService.findAppServers()).thenReturn(Arrays.asList(appServer));
        return appServer;
    }

    <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }

}
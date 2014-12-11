package com.energyict.mdc.dashboard.rest;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.JsonMappingExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.ExceptionLogger;
import com.energyict.mdc.common.rest.Installer;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.rest.status.ComServerStatusInfoFactory;
import com.energyict.mdc.dashboard.rest.status.ComServerStatusResource;
import com.energyict.mdc.dashboard.rest.status.ComServerStatusSummaryResource;
import com.energyict.mdc.dashboard.rest.status.impl.BreakdownFactory;
import com.energyict.mdc.dashboard.rest.status.impl.ComTaskExecutionInfoFactory;
import com.energyict.mdc.dashboard.rest.status.impl.ComTaskExecutionSessionInfoFactory;
import com.energyict.mdc.dashboard.rest.status.impl.CommunicationHeatMapResource;
import com.energyict.mdc.dashboard.rest.status.impl.CommunicationOverviewInfoFactory;
import com.energyict.mdc.dashboard.rest.status.impl.CommunicationOverviewResource;
import com.energyict.mdc.dashboard.rest.status.impl.CommunicationResource;
import com.energyict.mdc.dashboard.rest.status.impl.ConnectionHeatMapResource;
import com.energyict.mdc.dashboard.rest.status.impl.ConnectionOverviewInfoFactory;
import com.energyict.mdc.dashboard.rest.status.impl.ConnectionOverviewResource;
import com.energyict.mdc.dashboard.rest.status.impl.ConnectionResource;
import com.energyict.mdc.dashboard.rest.status.impl.ConnectionTaskInfoFactory;
import com.energyict.mdc.dashboard.rest.status.impl.DashboardFieldResource;
import com.energyict.mdc.dashboard.rest.status.impl.FavoriteDeviceGroupResource;
import com.energyict.mdc.dashboard.rest.status.impl.IssuesResource;
import com.energyict.mdc.dashboard.rest.status.impl.KpiScoreFactory;
import com.energyict.mdc.dashboard.rest.status.impl.LabeledDeviceResource;
import com.energyict.mdc.dashboard.rest.status.impl.MessageSeeds;
import com.energyict.mdc.dashboard.rest.status.impl.OverviewFactory;
import com.energyict.mdc.dashboard.rest.status.impl.SummaryInfoFactory;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.status.StatusService;
import com.energyict.mdc.favorites.FavoritesService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.google.common.collect.ImmutableSet;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (10:32)
 */
@Component(name = "com.energyict.mdc.dashboard.rest", service = {Application.class, InstallService.class}, immediate = true, property = {"alias=/dsr", "app=MDC", "name=" + DashboardApplication.COMPONENT_NAME})
public class DashboardApplication extends Application implements InstallService {
    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "DSR";

    private volatile StatusService statusService;
    private volatile EngineModelService engineModelService;
    private volatile NlsService nlsService;
    private volatile DashboardService dashboardService;
    private volatile Thesaurus thesaurus;
    private volatile ConnectionTaskService connectionTaskService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile DeviceService deviceService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile SchedulingService schedulingService;
    private volatile TaskService taskService;
    private volatile TransactionService transactionService;
    private volatile DataCollectionKpiService dataCollectionKpiService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile IssueService issueService;
    private volatile IssueDataCollectionService issueDataCollectionService;
    private volatile FavoritesService favoritesService;
    private volatile License license;
    private Clock clock = Clock.systemDefaultZone();

    @Reference
    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    @Reference
    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setDashboardService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Reference
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setSchedulingService(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setDataCollectionKpiService(DataCollectionKpiService dataCollectionKpiService) {
        this.dataCollectionKpiService = dataCollectionKpiService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setIssueDataCollectionService(IssueDataCollectionService issueDataCollectionService) {
        this.issueDataCollectionService = issueDataCollectionService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }
    
    @Reference
    public void setFavoritesService(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;
    }

    @Reference(target="(com.elster.jupiter.license.rest.key=" + APP_KEY  + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    // Only for testing purposes
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                TransactionWrapper.class,
                LocalizedFieldValidationExceptionMapper.class,
                ConstraintViolationExceptionMapper.class,
                JsonMappingExceptionMapper.class,
                LocalizedExceptionMapper.class,
                ExceptionLogger.class,
                ComServerStatusResource.class,
                ComServerStatusSummaryResource.class,
                ConnectionOverviewResource.class,
                DashboardFieldResource.class,
                ConnectionResource.class,
                ConnectionHeatMapResource.class,
                CommunicationResource.class,
                CommunicationOverviewResource.class,
                CommunicationHeatMapResource.class,
                DeviceConfigurationService.class, // This service is here intentionally: needed for the ComServerStatusResource apparently: this will create an osgi warning: A provider com.energyict.mdc.device.config.DeviceConfigurationService registered in SERVER runtime does not implement any provider interfaces applicable in the SERVER runtime. Due to constraint configuration problems the provider com.energyict.mdc.device.config.DeviceConfigurationService will be ignored.
                IssuesResource.class,
                LabeledDeviceResource.class,
                FavoriteDeviceGroupResource.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Override
    public void install() {
        Installer installer = new Installer();
        installer.createTranslations(COMPONENT_NAME, nlsService.getThesaurus(COMPONENT_NAME, Layer.REST), Layer.REST, MessageSeeds.values());
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("NLS");
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(statusService).to(StatusService.class);
            bind(engineModelService).to(EngineModelService.class);
            bind(nlsService).to(NlsService.class);
            bind(dashboardService).to(DashboardService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(communicationTaskService).to(CommunicationTaskService.class);
            bind(connectionTaskService).to(ConnectionTaskService.class);
            bind(deviceService).to(DeviceService.class);
            bind(deviceConfigurationService).to(DeviceConfigurationService.class);
            bind(protocolPluggableService).to(ProtocolPluggableService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(transactionService).to(TransactionService.class);
            bind(schedulingService).to(SchedulingService.class);
            bind(dataCollectionKpiService).to(DataCollectionKpiService.class);
            bind(taskService).to(TaskService.class);
            bind(issueDataCollectionService).to(IssueDataCollectionService.class);
            bind(issueService).to(IssueService.class);
            bind(BreakdownFactory.class).to(BreakdownFactory.class);
            bind(OverviewFactory.class).to(OverviewFactory.class);
            bind(ConnectionTaskInfoFactory.class).to(ConnectionTaskInfoFactory.class);
            bind(ComTaskExecutionInfoFactory.class).to(ComTaskExecutionInfoFactory.class);
            bind(ComTaskExecutionSessionInfoFactory.class).to(ComTaskExecutionSessionInfoFactory.class);
            bind(SummaryInfoFactory.class).to(SummaryInfoFactory.class);
            bind(ConnectionOverviewInfoFactory.class).to(ConnectionOverviewInfoFactory.class);
            bind(CommunicationOverviewInfoFactory.class).to(CommunicationOverviewInfoFactory.class);
            bind(ComServerStatusInfoFactory.class).to(ComServerStatusInfoFactory.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(KpiScoreFactory.class).to(KpiScoreFactory.class);
            bind(meteringGroupsService).to(MeteringGroupsService.class);
            bind(favoritesService).to(FavoritesService.class);
            bind(clock).to(Clock.class);
        }
    }

}
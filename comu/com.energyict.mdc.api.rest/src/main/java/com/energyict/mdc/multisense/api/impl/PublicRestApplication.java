package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.rest.ExceptionLogger;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.multisense.api.impl.utils.DeviceLifeCycleActionViolationExceptionMapper;
import com.energyict.mdc.multisense.api.impl.utils.ResourceHelper;
import com.energyict.mdc.multisense.api.impl.utils.RestExceptionMapper;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Singleton;
import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Component(name = "com.energyict.multisense.public.rest",
        service = {Application.class, TranslationKeyProvider.class},
        immediate = true,
        property = {"alias=/comu", "app=MDC", "name=" + PublicRestApplication.COMPONENT_NAME, "version=v2.0"})
public class PublicRestApplication extends Application implements TranslationKeyProvider {

    private final Logger logger = Logger.getLogger(PublicRestApplication.class.getName());

    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "MRA"; // Mdc Rest Api

    private volatile DeviceService deviceService;
    private volatile BatchService batchService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile TopologyService topologyService;
    private volatile IssueService issueService;
    private volatile DeviceLifeCycleService deviceLifeCycleService;
    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;
    private volatile TransactionService transactionService;
    private volatile ConnectionTaskService connectionTaskService;
    private volatile EngineConfigurationService engineConfigurationService;
    private volatile TaskService taskService;
    private volatile SchedulingService schedulingService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile Clock clock;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile DeviceMessageService deviceMessageService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                ExceptionLogger.class,

                AuthenticationDeviceAccessLevelResource.class,
                ComPortPoolResource.class,
                ComScheduleResource.class,
                ComTaskResource.class,
                ComTaskEnablementResource.class,
                ComTaskExecutionResource.class,
                ConfigurationSecurityPropertySetResource.class,
                ConnectionTaskResource.class,
                DeviceConfigurationResource.class,
                DeviceContactorResource.class,
                DeviceLifecycleActionResource.class,
                DeviceMessageCategoryResource.class,
                DeviceMessageEnablementResource.class,
                DeviceMessageSpecificationResource.class,
                DeviceMessageResource.class,
                DeviceProtocolPluggableClassResource.class,
                DeviceResource.class,
                DeviceTypeResource.class,
                EncryptionDeviceAccessLevelResource.class,
                PartialConnectionTaskResource.class,
                ProtocolDialectConfigurationPropertiesResource.class,
                ProtocolTaskResource.class,

                RestExceptionMapper.class,
                DeviceLifeCycleActionViolationExceptionMapper.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setBatchService(BatchService batchService) {
        this.batchService = batchService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setFiniteStateMachineService(FiniteStateMachineService finiteStateMachineService) {
        this.finiteStateMachineService = finiteStateMachineService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    public void setDeviceMessageService(DeviceMessageService deviceMessageService) {
        this.deviceMessageService = deviceMessageService;
    }

    @Reference
    public void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Reference
    public void setSchedulingService(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Collections.emptyList();
    }

    @Reference(target = "(com.elster.jupiter.license.rest.key=" + APP_KEY + ")")
    public void setLicense(License license) {
    }

    @Reference
    public void setDeviceLifeCycleService(DeviceLifeCycleService deviceLifeCycleService) {
        this.deviceLifeCycleService = deviceLifeCycleService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(deviceService).to(DeviceService.class);
            bind(batchService).to(BatchService.class);
            bind(deviceConfigurationService).to(DeviceConfigurationService.class);
            bind(issueService).to(IssueService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(nlsService).to(NlsService.class);
            bind(topologyService).to(TopologyService.class);
            bind(finiteStateMachineService).to(FiniteStateMachineService.class);
            bind(deviceLifeCycleService).to(DeviceLifeCycleService.class);
            bind(transactionService).to(TransactionService.class);
            bind(connectionTaskService).to(ConnectionTaskService.class);
            bind(engineConfigurationService).to(EngineConfigurationService.class);
            bind(taskService).to(TaskService.class);
            bind(deviceMessageSpecificationService).to(DeviceMessageSpecificationService.class);
            bind(clock).to(Clock.class);
            bind(protocolPluggableService).to(ProtocolPluggableService.class);
            bind(schedulingService).to(SchedulingService.class);
            bind(deviceMessageService).to(DeviceMessageService.class);

            bind(MdcPropertyUtils.class).to(MdcPropertyUtils.class).in(Singleton.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class).in(Singleton.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class).in(Singleton.class);
            bind(ResourceHelper.class).to(ResourceHelper.class).in(Singleton.class);

            bind(DeviceInfoFactory.class).to(DeviceInfoFactory.class).in(Singleton.class);
            bind(DeviceLifecycleActionInfoFactory.class).to(DeviceLifecycleActionInfoFactory.class).in(Singleton.class);
            bind(DeviceTypeInfoFactory.class).to(DeviceTypeInfoFactory.class).in(Singleton.class);
            bind(DeviceConfigurationInfoFactory.class).to(DeviceConfigurationInfoFactory.class).in(Singleton.class);
            bind(ConnectionTaskInfoFactory.class).to(ConnectionTaskInfoFactory.class).in(Singleton.class);
            bind(ComPortPoolInfoFactory.class).to(ComPortPoolInfoFactory.class).in(Singleton.class);
            bind(PartialConnectionTaskInfoFactory.class).to(PartialConnectionTaskInfoFactory.class).in(Singleton.class);
            bind(ComTaskInfoFactory.class).to(ComTaskInfoFactory.class).in(Singleton.class);
            bind(DeviceMessageCategoryInfoFactory.class).to(DeviceMessageCategoryInfoFactory.class).in(Singleton.class);
            bind(ProtocolTaskInfoFactory.class).to(ProtocolTaskInfoFactory.class).in(Singleton.class);
            bind(DeviceProtocolPluggableClassInfoFactory.class).to(DeviceProtocolPluggableClassInfoFactory.class).in(Singleton.class);
            bind(AuthenticationDeviceAccessLevelInfoFactory.class).to(AuthenticationDeviceAccessLevelInfoFactory.class).in(Singleton.class);
            bind(EncryptionDeviceAccessLevelInfoFactory.class).to(EncryptionDeviceAccessLevelInfoFactory.class).in(Singleton.class);
            bind(ConfigurationSecurityPropertySetFactory.class).to(ConfigurationSecurityPropertySetFactory.class).in(Singleton.class);
            bind(ComTaskExecutionInfoFactory.class).to(ComTaskExecutionInfoFactory.class).in(Singleton.class);
            bind(ComTaskEnablementInfoFactory.class).to(ComTaskEnablementInfoFactory.class).in(Singleton.class);
            bind(ProtocolDialectConfigurationPropertiesInfoFactory.class).to(ProtocolDialectConfigurationPropertiesInfoFactory.class).in(Singleton.class);
            bind(DeviceMessageInfoFactory.class).to(DeviceMessageInfoFactory.class).in(Singleton.class);
            bind(ComScheduleInfoFactory.class).to(ComScheduleInfoFactory.class).in(Singleton.class);
            bind(DeviceMessageSpecificationInfoFactory.class).to(DeviceMessageSpecificationInfoFactory.class).in(Singleton.class);
            bind(DeviceMessageEnablementInfoFactory.class).to(DeviceMessageEnablementInfoFactory.class).in(Singleton.class);
        }
    }

}
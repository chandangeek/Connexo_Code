/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kore.api.v2.issue.DeviceShortInfoFactory;
import com.elster.jupiter.kore.api.v2.issue.IssueAssigneeInfoFactory;
import com.elster.jupiter.kore.api.v2.issue.IssuePriorityInfoFactory;
import com.elster.jupiter.kore.api.v2.issue.IssueReasonInfoFactory;
import com.elster.jupiter.kore.api.v2.issue.LocationShortInfoFactory;
import com.elster.jupiter.kore.api.v2.issue.UsagePointShortInfoFactory;
import com.elster.jupiter.license.License;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.api.util.v1.hypermedia.ConstraintViolationInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.RestExceptionMapper;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.rest.ExceptionLogger;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.multisense.api.impl.utils.DeviceLifeCycleActionViolationExceptionMapper;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.ResourceHelper;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.hibernate.validator.HibernateValidator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.spi.ValidationProvider;
import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.energyict.multisense.public.rest",
        service = {Application.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        immediate = true,
        property = {"alias=/comu", "app=MDC", "name=" + PublicRestApplication.COMPONENT_NAME, "version=v2.0"})
public class PublicRestApplication extends Application implements TranslationKeyProvider, MessageSeedProvider {

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
    private volatile CommunicationTaskService communicationTaskService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile MeteringService meteringService;
    private volatile PropertyValueInfoService propertyValueInfoService;
    private volatile DeviceAlarmService deviceAlarmService;
    private volatile ThreadPrincipalService threadPrincipalService;

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
                DeviceMessageFileResource.class,
                DeviceMessageCategoryResource.class,
                DeviceMessageEnablementResource.class,
                DeviceMessageSpecificationResource.class,
                DeviceMessageResource.class,
                DeviceProtocolPluggableClassResource.class,
                DeviceResource.class,
                DeviceSecurityPropertySetResource.class,
                DeviceTypeResource.class,
                EncryptionDeviceAccessLevelResource.class,
                PartialConnectionTaskResource.class,
                ProtocolDialectConfigurationPropertiesResource.class,
                ProtocolTaskResource.class,
                DeviceAlarmResource.class,

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
    public void setDeviceAlarmService(DeviceAlarmService deviceAlarmService) {
        this.deviceAlarmService = deviceAlarmService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
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
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
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
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
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

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    private Factory<Validator> getValidatorFactory() {
        return new Factory<Validator>() {
            private final ValidatorFactory validatorFactory = Validation.byDefaultProvider()
                    .providerResolver(() -> ImmutableList.<ValidationProvider<?>>of(new HibernateValidator()))
                    .configure()
//                .constraintValidatorFactory(getConstraintValidatorFactory())
                    .messageInterpolator(thesaurus)
                    .buildValidatorFactory();

            @Override
            public Validator provide() {
                return validatorFactory.getValidator();
            }

            @Override
            public void dispose(Validator validator) {

            }
        };
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
            bind(communicationTaskService).to(CommunicationTaskService.class);
            bind(customPropertySetService).to(CustomPropertySetService.class);
            bind(meteringService).to(MeteringService.class);
            bind(metrologyConfigurationService).to(MetrologyConfigurationService.class);
            bind(customPropertySetService).to(CustomPropertySetService.class);
            bind(propertyValueInfoService).to(PropertyValueInfoService.class);
            bind(deviceAlarmService).to(DeviceAlarmService.class);
            bind(threadPrincipalService).to(ThreadPrincipalService.class);
            bindFactory(getValidatorFactory()).to(Validator.class);

            bind(MdcPropertyUtils.class).to(MdcPropertyUtils.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(ResourceHelper.class).to(ResourceHelper.class);

            bind(DeviceInfoFactory.class).to(DeviceInfoFactory.class);
            bind(DeviceLifecycleActionInfoFactory.class).to(DeviceLifecycleActionInfoFactory.class);
            bind(DeviceTypeInfoFactory.class).to(DeviceTypeInfoFactory.class);
            bind(DeviceConfigurationInfoFactory.class).to(DeviceConfigurationInfoFactory.class);
            bind(DeviceMessageFileInfoFactory.class).to(DeviceMessageFileInfoFactory.class);
            bind(ConnectionTaskInfoFactory.class).to(ConnectionTaskInfoFactory.class);
            bind(ComPortPoolInfoFactory.class).to(ComPortPoolInfoFactory.class);
            bind(PartialConnectionTaskInfoFactory.class).to(PartialConnectionTaskInfoFactory.class);
            bind(ComTaskInfoFactory.class).to(ComTaskInfoFactory.class);
            bind(DeviceMessageCategoryInfoFactory.class).to(DeviceMessageCategoryInfoFactory.class);
            bind(ProtocolTaskInfoFactory.class).to(ProtocolTaskInfoFactory.class);
            bind(DeviceProtocolPluggableClassInfoFactory.class).to(DeviceProtocolPluggableClassInfoFactory.class);
            bind(AuthenticationDeviceAccessLevelInfoFactory.class).to(AuthenticationDeviceAccessLevelInfoFactory.class);
            bind(EncryptionDeviceAccessLevelInfoFactory.class).to(EncryptionDeviceAccessLevelInfoFactory.class);
            bind(ConfigurationSecurityPropertySetInfoFactory.class).to(ConfigurationSecurityPropertySetInfoFactory.class);
            bind(ComTaskExecutionInfoFactory.class).to(ComTaskExecutionInfoFactory.class);
            bind(ComTaskEnablementInfoFactory.class).to(ComTaskEnablementInfoFactory.class);
            bind(ProtocolDialectConfigurationPropertiesInfoFactory.class).to(ProtocolDialectConfigurationPropertiesInfoFactory.class);
            bind(DeviceMessageInfoFactory.class).to(DeviceMessageInfoFactory.class);
            bind(ComScheduleInfoFactory.class).to(ComScheduleInfoFactory.class);
            bind(DeviceMessageSpecificationInfoFactory.class).to(DeviceMessageSpecificationInfoFactory.class);
            bind(DeviceMessageEnablementInfoFactory.class).to(DeviceMessageEnablementInfoFactory.class);
            bind(DeviceSecurityPropertySetInfoFactory.class).to(DeviceSecurityPropertySetInfoFactory.class);
            bind(CustomPropertySetInfoFactory.class).to(CustomPropertySetInfoFactory.class);
            bind(DeviceAlarmStatusInfoFactory.class).to(DeviceAlarmStatusInfoFactory.class);
            bind(DeviceAlarmInfoFactory.class).to(DeviceAlarmInfoFactory.class);
            bind(IssueAssigneeInfoFactory.class).to(IssueAssigneeInfoFactory.class);
            bind(IssueReasonInfoFactory.class).to(IssueReasonInfoFactory.class);
            bind(IssuePriorityInfoFactory.class).to(IssuePriorityInfoFactory.class);
            bind(DeviceShortInfoFactory.class).to(DeviceShortInfoFactory.class);
            bind(UsagePointShortInfoFactory.class).to(UsagePointShortInfoFactory.class);
            bind(LocationShortInfoFactory.class).to(LocationShortInfoFactory.class);
            bind(DeviceAlarmShortInfoFactory.class).to(DeviceAlarmShortInfoFactory.class);
        }
    }

}
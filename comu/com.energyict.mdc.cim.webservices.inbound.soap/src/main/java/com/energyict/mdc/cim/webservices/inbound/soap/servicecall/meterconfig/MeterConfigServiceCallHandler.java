/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.cim.webservices.inbound.soap.InboundCIMWebServiceExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.MeterInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.SecurityHelper;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.SecurityKeyInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.customattributeset.CasHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.customattributeset.CasInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.DeviceBuilder;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.DeviceDeleter;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.DeviceFinder;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.ErrorMessage;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigFaultMessageFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigPingUtils;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterStatusSource;
import com.energyict.mdc.cim.webservices.outbound.soap.OperationEnum;
import com.energyict.mdc.cim.webservices.outbound.soap.PingResult;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.topology.TopologyService;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.schema.message.ErrorType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link ServiceCallHandler} interface which handles the different steps for CIM WS MeterConfig
 */
@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.servicecall.MeterConfigServiceCallHandler", service = ServiceCallHandler.class, immediate = true, property = "name="
        + MeterConfigServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
public class MeterConfigServiceCallHandler implements ServiceCallHandler {
    public static final String SERVICE_CALL_HANDLER_NAME = "MeterConfigServiceCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private volatile BatchService batchService;
    private volatile Clock clock;
    private volatile DeviceLifeCycleService deviceLifeCycleService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceService deviceService;
    private volatile JsonService jsonService;
    private volatile Thesaurus thesaurus;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile SecurityManagementService securityManagementService;
    private volatile HsmEnergyService hsmEnergyService;
    private volatile MeteringTranslationService meteringTranslationService;
    private volatile TransactionService transactionService;
    private volatile TopologyService topologyService;
    private volatile ServiceCallService serviceCallService;

    private ReplyTypeFactory replyTypeFactory;
    private MeterConfigFaultMessageFactory messageFactory;
    private DeviceBuilder deviceBuilder;
    private DeviceFinder deviceFinder;
    private DeviceDeleter deviceDeleter;
    private Optional<InboundCIMWebServiceExtension> webServiceExtension = Optional.empty();
    private CasHandler casHandler;
    private SecurityHelper securityHelper;
    private MeterConfigPingUtils meterConfigPingUtils;

    public MeterConfigServiceCallHandler() {

    }

    @Inject
    public MeterConfigServiceCallHandler(Thesaurus thesaurus, Clock clock, BatchService batchService,
                                         DeviceLifeCycleService deviceLifeCycleService, DeviceConfigurationService deviceConfigurationService,
                                         DeviceService deviceService, JsonService jsonService, CustomPropertySetService customPropertySetService,
                                         SecurityManagementService securityManagementService, HsmEnergyService hsmEnergyService,
                                         MeteringTranslationService meteringTranslationService, TransactionService transactionService,
                                         TopologyService topologyService) {
        this.batchService = batchService;
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.clock = clock;
        this.deviceService = deviceService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.jsonService = jsonService;
        this.thesaurus = thesaurus;
        this.customPropertySetService = customPropertySetService;
        this.securityManagementService = securityManagementService;
        this.hsmEnergyService = hsmEnergyService;
        this.meteringTranslationService = meteringTranslationService;
        this.transactionService = transactionService;
        this.topologyService = topologyService;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case ONGOING:
                if (oldState.equals(DefaultState.PENDING)) {
                    processMeterConfigServiceCall(serviceCall);
                }
                break;
            case SUCCESSFUL:
                break;
            case FAILED:
                break;
            case CANCELLED:
                setCancelled(serviceCall);
                break;
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void addInboundCIMWebServiceExtension(InboundCIMWebServiceExtension webServiceExtension) {
        this.webServiceExtension = Optional.of(webServiceExtension);
    }

    public void removeInboundCIMWebServiceExtension(InboundCIMWebServiceExtension webServiceExtension) {
        this.webServiceExtension = Optional.empty();
    }

    private void processMeterConfigServiceCall(ServiceCall serviceCall) {
        MeterConfigDomainExtension extensionFor = serviceCall.getExtensionFor(new MeterConfigCustomPropertySet()).get();
        OperationEnum operation = OperationEnum.getFromString(extensionFor.getOperation());
        final MeterInfo meterInfo = (OperationEnum.GET.equals(operation) || OperationEnum.DELETE.equals(operation)) ? null : jsonService.deserialize(extensionFor.getMeter(), MeterInfo.class);
        try {
            transactionService.runInIndependentTransaction(() -> {
                Device device;
                switch (operation) {
                    case CREATE:
                        device = getDeviceBuilder().prepareCreateFrom(meterInfo).build();
                        processDevice(serviceCall, meterInfo, device);
                        serviceCall.transitionWithLockIfPossible(DefaultState.SUCCESSFUL);
                        break;
                    case UPDATE:
                        device = getDeviceBuilder().prepareChangeFrom(meterInfo).build();
                        processDevice(serviceCall, meterInfo, device);
                        serviceCall.transitionWithLockIfPossible(DefaultState.SUCCESSFUL);
                        break;
                    case GET:
                        device = getDeviceFinder().findDevice(extensionFor.getMeterMrid(), extensionFor.getMeterName());
                        processMeterStatus(device, serviceCall);
                        break;
                    case DELETE:
                        device = getDeviceFinder().findDevice(extensionFor.getMeterMrid(), extensionFor.getMeterName());
                        getDeviceDeleter().delete(device);
                        serviceCall.transitionWithLockIfPossible(DefaultState.SUCCESSFUL);
                        break;
                    default:
                        serviceCall.transitionWithLockIfPossible(DefaultState.FAILED);
                        break;
                }
            });
        } catch (Exception faultMessage) {
            handleException(serviceCall, faultMessage);
        }
    }

    private void processMeterStatus(Device device, ServiceCall serviceCall) throws FaultMessage {
        ServiceCall parent = serviceCall.getParent().orElseThrow(() -> new IllegalStateException("Unable to find parent service call."));
        MeterConfigMasterDomainExtension parentExtension = parent.getExtension(MeterConfigMasterDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call."));
        if (MeterStatusSource.METER.getSource().equalsIgnoreCase(parentExtension.getMeterStatusSource())) {
            Optional<ComTaskExecution> statusInformationTask = getStatusInformationTask(device);
            if (statusInformationTask.isPresent()) {
                executeStatusInformationTask(statusInformationTask.get(), serviceCall);
            } else {
                ServiceCall lockedServiceCall = serviceCallService.lockServiceCall(serviceCall.getId()).orElseThrow(() -> new IllegalStateException("Unable to lock service call."));
                MeterConfigDomainExtension extension = lockedServiceCall.getExtension(MeterConfigDomainExtension.class)
                        .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call."));
                extension.setErrorMessage(thesaurus.getSimpleFormat(MessageSeeds.TASK_FOR_METER_STATUS_IS_MISSING).format(device.getName()));
                extension.setErrorCode(MessageSeeds.TASK_FOR_METER_STATUS_IS_MISSING.getErrorCode());
                lockedServiceCall.update(extension);
                lockedServiceCall.requestTransition(DefaultState.SUCCESSFUL);
            }
        } else if (parentExtension.needsPing()) {
            ServiceCall lockedServiceCall = serviceCallService.lockServiceCall(serviceCall.getId()).orElseThrow(() -> new IllegalStateException("Unable to lock service call."));
            MeterConfigDomainExtension extension = lockedServiceCall.getExtension(MeterConfigDomainExtension.class)
                    .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call."));

            Optional<ErrorMessage> errorMessageOptional = getMeterConfigPingUtils().ping(device);
            if (errorMessageOptional.isPresent()) {
                extension.setPingResult(PingResult.FAILED.getName());
                extension.setErrorMessage(errorMessageOptional.get().getMessage());
                extension.setErrorCode(errorMessageOptional.get().getCode());
            } else {
                extension.setPingResult(PingResult.SUCCESSFUL.getName());
            }
            lockedServiceCall.update(extension);
            lockedServiceCall.requestTransition(DefaultState.SUCCESSFUL);
        } else {
            serviceCall.transitionWithLockIfPossible(DefaultState.SUCCESSFUL);
        }
    }

    private void executeStatusInformationTask(ComTaskExecution statusInformationTask, ServiceCall serviceCall) {
        ServiceCall lockedServiceCall = serviceCallService.lockServiceCall(serviceCall.getId()).orElseThrow(() -> new IllegalStateException("Unable to lock service call."));
        MeterConfigDomainExtension extension = lockedServiceCall.getExtension(MeterConfigDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call."));
        extension.setCommunicationTask(statusInformationTask.getComTask());
        lockedServiceCall.setTargetObject(statusInformationTask.getDevice());
        lockedServiceCall.update(extension);
        lockedServiceCall.requestTransition(DefaultState.WAITING);
        statusInformationTask.schedule(clock.instant());
    }

    private Optional<ComTaskExecution> getStatusInformationTask(Device device) {
        return device.getDeviceConfiguration().getComTaskEnablements().stream()
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().isManualSystemTask())
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().getProtocolTasks().stream()
                        .anyMatch(task -> task instanceof StatusInformationTask))
                .filter(comTaskEnablement -> !comTaskEnablement.isSuspended())
                .findAny()
                .map(comTaskEnablement -> device.getComTaskExecutions().stream()
                        .filter(comTaskExecution -> comTaskExecution.getComTask().equals(comTaskEnablement.getComTask()))
                        .findAny()
                        .orElseGet(() -> device.newAdHocComTaskExecution(comTaskEnablement).add()));

    }

    private void setCancelled(ServiceCall serviceCall) {
        ServiceCall lockedServiceCall = serviceCallService.lockServiceCall(serviceCall.getId()).orElseThrow(() -> new IllegalStateException("Unable to lock service call."));
        MeterConfigDomainExtension extension = lockedServiceCall.getExtension(MeterConfigDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call."));
        extension.setErrorMessage(thesaurus.getSimpleFormat(MessageSeeds.SERVICE_CALL_IS_CANCELLED).format(serviceCall.getNumber()));
        extension.setErrorCode(MessageSeeds.SERVICE_CALL_IS_CANCELLED.getErrorCode());
        lockedServiceCall.update(extension);
    }

    private void handleException(ServiceCall serviceCall, Exception faultMessage) {
        ServiceCall lockedServiceCall = serviceCallService.lockServiceCall(serviceCall.getId()).orElseThrow(() -> new IllegalStateException("Unable to lock service call."));
        MeterConfigDomainExtension extension = lockedServiceCall.getExtension(MeterConfigDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call."));
        extension.setErrorCode(OperationEnum.getFromString(extension.getOperation()).getDefaultErrorCode());
        if (faultMessage instanceof FaultMessage) {
            Optional<ErrorType> errorType = ((FaultMessage) faultMessage).getFaultInfo().getReply().getError()
                    .stream().findFirst();
            if (errorType.isPresent()) {
                extension.setErrorMessage(errorType.get().getDetails());
                extension.setErrorCode(errorType.get().getCode());
            } else {
                extension.setErrorMessage(faultMessage.getLocalizedMessage());
            }
        } else if (faultMessage instanceof ConstraintViolationException) {
            extension.setErrorMessage(((ConstraintViolationException) faultMessage).getConstraintViolations()
                    .stream().findFirst().map(ConstraintViolation::getMessage).orElseGet(faultMessage::getMessage));
        } else {
            extension.setErrorMessage(faultMessage.getLocalizedMessage());
        }
        lockedServiceCall.update(extension);
        lockedServiceCall.requestTransition(DefaultState.FAILED);
    }

    private void processDevice(ServiceCall serviceCall, MeterInfo meterInfo, Device device) throws FaultMessage {
        List<FaultMessage> faultsForCreate = processCustomAttributeSets(serviceCall, device, meterInfo);
        if (!faultsForCreate.isEmpty()) {
            throw faultsForCreate.get(0);
        }
        faultsForCreate = processSecurityKeys(serviceCall, device, meterInfo);
        if (!faultsForCreate.isEmpty()) {
            throw faultsForCreate.get(0);
        }
        postProcessDevice(device, meterInfo);
    }

    /**
     * Sets values for CustomPropertySets on specific device logging detailed error messages if possible
     *
     * @param serviceCall
     * @param device
     * @param meterInfo
     * @return
     */
    private List<FaultMessage> processCustomAttributeSets(ServiceCall serviceCall, Device device, MeterInfo meterInfo) {
        List<CasInfo> customAttributeSets = meterInfo.getCustomAttributeSets();
        if (customAttributeSets.isEmpty()) {
            return Collections.emptyList();
        }
        return getCasHandler().addCustomPropertySetsData(device, customAttributeSets, serviceCall);

    }

    private List<FaultMessage> processSecurityKeys(ServiceCall serviceCall, Device device, MeterInfo meterInfo) {
        List<SecurityKeyInfo> securityInfos = meterInfo.getSecurityInfo().getSecurityKeys();
        if (securityInfos.isEmpty()) {
            return Collections.emptyList();
        }
        return getSecurityHelper().addSecurityKeys(device, securityInfos, serviceCall);
    }

    @Reference
    public void setBatchService(BatchService batchService) {
        this.batchService = batchService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
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
    public void setDeviceLifeCycleService(DeviceLifeCycleService deviceLifeCycleService) {
        this.deviceLifeCycleService = deviceLifeCycleService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(InboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP);
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public void setHsmEnergyService(HsmEnergyService hsmEnergyService) {
        this.hsmEnergyService = hsmEnergyService;
    }

    @Reference
    public void setMeteringTranslationService(MeteringTranslationService meteringTranslationService) {
        this.meteringTranslationService = meteringTranslationService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    private void postProcessDevice(Device device, MeterInfo meterInfo) {
        webServiceExtension.ifPresent(
                inboundCIMWebServiceExtension -> inboundCIMWebServiceExtension.extendMeterInfo(device, meterInfo));
    }

    private DeviceFinder getDeviceFinder() {
        if (deviceFinder == null) {
            deviceFinder = new DeviceFinder(deviceService, getMessageFactory());
        }
        return deviceFinder;
    }

    private DeviceDeleter getDeviceDeleter() {
        if (deviceDeleter == null) {
            deviceDeleter = new DeviceDeleter(topologyService, getMessageFactory());
        }
        return deviceDeleter;
    }

    private DeviceBuilder getDeviceBuilder() {
        if (deviceBuilder == null) {
            deviceBuilder = new DeviceBuilder(batchService, clock, deviceLifeCycleService, deviceConfigurationService,
                    deviceService, getMessageFactory(), meteringTranslationService);
        }
        return deviceBuilder;
    }

    private MeterConfigFaultMessageFactory getMessageFactory() {
        if (messageFactory == null) {
            messageFactory = new MeterConfigFaultMessageFactory(thesaurus, getReplyTypeFactory());
        }
        return messageFactory;
    }

    private ReplyTypeFactory getReplyTypeFactory() {
        if (replyTypeFactory == null) {
            replyTypeFactory = new ReplyTypeFactory(thesaurus);
        }
        return replyTypeFactory;
    }

    private CasHandler getCasHandler() {
        if (casHandler == null) {
            casHandler = new CasHandler(customPropertySetService, thesaurus,
                    getMessageFactory(), clock);
        }
        return casHandler;
    }

    private SecurityHelper getSecurityHelper() {
        if (securityHelper == null) {
            securityHelper = new SecurityHelper(hsmEnergyService, securityManagementService, getMessageFactory(),
                    thesaurus);
        }
        return securityHelper;
    }

    private MeterConfigPingUtils getMeterConfigPingUtils() {
        if (meterConfigPingUtils == null) {
            meterConfigPingUtils = new MeterConfigPingUtils(thesaurus);
        }
        return meterConfigPingUtils;
    }
}
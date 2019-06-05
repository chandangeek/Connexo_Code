/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.cim.webservices.outbound.soap.OperationEnum;
import com.energyict.mdc.cim.webservices.inbound.soap.MeterInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.getenddeviceevents.EndDeviceEventsBuilder;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigFaultMessageFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigParser;
import com.energyict.mdc.cim.webservices.inbound.soap.meterreadings.MeterReadingFaultMessageFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.meterreadings.ReadingSourceEnum;
import com.energyict.mdc.cim.webservices.inbound.soap.meterreadings.ScheduleStrategyEnum;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getenddeviceevents.GetEndDeviceEventsCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getenddeviceevents.GetEndDeviceEventsDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getenddeviceevents.GetEndDeviceEventsServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ChildGetMeterReadingsCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ChildGetMeterReadingsDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ComTaskExecutionServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.DeviceMessageServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.ParentGetMeterReadingsServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.SubParentGetMeterReadingsCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.SubParentGetMeterReadingsDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings.SubParentGetMeterReadingsServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigMasterCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigMasterDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigMasterServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigServiceCallHandler;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.NoSuchElementException;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.RegistersTask;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.getmeterreadings.DateTimeInterval;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ServiceCallCommands {

    public enum ServiceCallTypes {
        MASTER_METER_CONFIG(MeterConfigMasterServiceCallHandler.SERVICE_CALL_HANDLER_NAME, MeterConfigMasterServiceCallHandler.VERSION, MeterConfigMasterCustomPropertySet.class.getName()),
        METER_CONFIG(MeterConfigServiceCallHandler.SERVICE_CALL_HANDLER_NAME, MeterConfigServiceCallHandler.VERSION, MeterConfigCustomPropertySet.class.getName()),
        GET_END_DEVICE_EVENTS(GetEndDeviceEventsServiceCallHandler.SERVICE_CALL_HANDLER_NAME, GetEndDeviceEventsServiceCallHandler.VERSION, GetEndDeviceEventsCustomPropertySet.class.getName()),
        PARENT_GET_METER_READINGS(ParentGetMeterReadingsServiceCallHandler.SERVICE_CALL_HANDLER_NAME, ParentGetMeterReadingsServiceCallHandler.VERSION, ParentGetMeterReadingsCustomPropertySet.class.getName()),
        SUBPARENT_GET_METER_READINGS(SubParentGetMeterReadingsServiceCallHandler.SERVICE_CALL_HANDLER_NAME, SubParentGetMeterReadingsServiceCallHandler.VERSION, SubParentGetMeterReadingsCustomPropertySet .class.getName()),
        DEVICE_MESSAGE_GET_METER_READINGS(DeviceMessageServiceCallHandler.SERVICE_CALL_HANDLER_NAME, DeviceMessageServiceCallHandler.VERSION, ChildGetMeterReadingsCustomPropertySet.class.getName()),
        COMTASK_EXECUTION_GET_METER_READINGS(ComTaskExecutionServiceCallHandler.SERVICE_CALL_HANDLER_NAME, ComTaskExecutionServiceCallHandler.VERSION, ChildGetMeterReadingsCustomPropertySet .class.getName());

        private final String typeName;
        private final String typeVersion;
        private final String customPropertySetClass;

        ServiceCallTypes(String typeName, String typeVersion, String customPropertySetClass) {
            this.typeName = typeName;
            this.typeVersion = typeVersion;
            this.customPropertySetClass = customPropertySetClass;
        }

        public String getTypeName() {
            return typeName;
        }

        public String getTypeVersion() {
            return typeVersion;
        }

        public String getCustomPropertySetClass() {
            return customPropertySetClass;
        }
    }

    private final DeviceService deviceService;
    private final JsonService jsonService;
    private final EndDeviceEventsBuilder endDeviceEventsBuilder;
    private final MeterConfigParser meterConfigParser;
    private final MeterConfigFaultMessageFactory meterConfigFaultMessageFactory;
    private final ServiceCallService serviceCallService;
    private final Thesaurus thesaurus;
    private final MeterReadingFaultMessageFactory faultMessageFactory;
    private final MessageService messageService;
    private final Clock clock;

    @Inject
    public ServiceCallCommands(DeviceService deviceService, JsonService jsonService,
                               MeterConfigParser meterConfigParser, MeterConfigFaultMessageFactory meterConfigFaultMessageFactory,
                               ServiceCallService serviceCallService, EndDeviceEventsBuilder endDeviceEventsBuilder,
                               Thesaurus thesaurus, MeterReadingFaultMessageFactory faultMessageFactory,
                               MessageService messageService, Clock clock) {
        this.deviceService = deviceService;
        this.jsonService = jsonService;
        this.endDeviceEventsBuilder = endDeviceEventsBuilder;
        this.meterConfigParser = meterConfigParser;
        this.meterConfigFaultMessageFactory = meterConfigFaultMessageFactory;
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
        this.faultMessageFactory = faultMessageFactory;
        this.messageService = messageService;
        this.clock = clock;

    }

    @TransactionRequired
    public ServiceCall createMeterConfigMasterServiceCall(MeterConfig meterConfig, EndPointConfiguration outboundEndPointConfiguration,
                                                          OperationEnum operation) throws FaultMessage {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.MASTER_METER_CONFIG);

        MeterConfigMasterDomainExtension meterConfigMasterDomainExtension = new MeterConfigMasterDomainExtension();
        meterConfigMasterDomainExtension.setActualNumberOfSuccessfulCalls(0l);
        meterConfigMasterDomainExtension.setActualNumberOfFailedCalls(0l);
        meterConfigMasterDomainExtension.setExpectedNumberOfCalls(Long.valueOf(meterConfig.getMeter().size()));
        setCallBackUrl(meterConfigMasterDomainExtension, outboundEndPointConfiguration);

        ServiceCallBuilder serviceCallBuilder = serviceCallType.newServiceCall()
                .origin("MultiSense")
                .extendedWith(meterConfigMasterDomainExtension);
        ServiceCall parentServiceCall = serviceCallBuilder.create();

        for (Meter meter : meterConfig.getMeter()) {
            createMeterConfigChildCall(parentServiceCall, operation, meter, meterConfig.getSimpleEndDeviceFunction());
        }

        return parentServiceCall;
    }

    private void setCallBackUrl(MeterConfigMasterDomainExtension meterConfigMasterDomainExtension,
            EndPointConfiguration outboundEndPointConfiguration) {
        if (outboundEndPointConfiguration != null) {
            meterConfigMasterDomainExtension.setCallbackURL(outboundEndPointConfiguration.getUrl());
        }
    }

    private ServiceCall createMeterConfigChildCall(ServiceCall parent, OperationEnum operation,
                                                   Meter meter, List<SimpleEndDeviceFunction> simpleEndDeviceFunction) throws FaultMessage {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.METER_CONFIG);

        MeterConfigDomainExtension meterConfigDomainExtension = new MeterConfigDomainExtension();
        meterConfigDomainExtension.setParentServiceCallId(BigDecimal.valueOf(parent.getId()));
        MeterInfo meterInfo;
        if (OperationEnum.GET.equals(operation)) {
            meterInfo = meterConfigParser.asMeterInfo(meter);
            meterConfigDomainExtension.setMeter(null);
        } else {
            meterInfo = meterConfigParser.asMeterInfo(meter, simpleEndDeviceFunction, operation);
            meterConfigDomainExtension.setMeter(jsonService.serialize(meterInfo));
        }
        meterConfigDomainExtension.setMeterMrid(meter.getMRID());
        String deviceName = meterConfigParser.extractName(meter.getNames()).orElse(null);
        meterConfigDomainExtension.setMeterName(deviceName);
        meterConfigDomainExtension.setOperation(operation.getOperation());
        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(meterConfigDomainExtension);
        if (operation == OperationEnum.UPDATE) {
            serviceCallBuilder.targetObject(findDevice(meterInfo));
        }
        return serviceCallBuilder.create();
    }

    @TransactionRequired
    public ServiceCall createGetEndDeviceEventsMasterServiceCall(List<ch.iec.tc57._2011.getenddeviceevents.Meter> meters,
                                                                 Range<Instant> interval, EndPointConfiguration outboundEndPointConfiguration)
            throws ch.iec.tc57._2011.getenddeviceevents.FaultMessage {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.GET_END_DEVICE_EVENTS);

        GetEndDeviceEventsDomainExtension domainExtension = new GetEndDeviceEventsDomainExtension();

        String meterIdentifiers = "";
        for (String identifier : endDeviceEventsBuilder.getMeterIdentifiers(meters)) {
            meterIdentifiers = identifier + "," + meterIdentifiers;
        }

        domainExtension.setMeter(meterIdentifiers);
        domainExtension.setFromDate(interval.lowerEndpoint());
        domainExtension.setToDate(interval.upperEndpoint());
        domainExtension.setCallbackURL(outboundEndPointConfiguration.getUrl());

        ServiceCallBuilder serviceCallBuilder = serviceCallType.newServiceCall()
                .origin("MultiSense")
                .extendedWith(domainExtension);
        return serviceCallBuilder.create();
    }

    @TransactionRequired
    public void requestTransition(ServiceCall serviceCall, DefaultState newState) {
        serviceCall.requestTransition(newState);
    }

    private ServiceCallType getServiceCallType(ServiceCallTypes serviceCallType) {
        return serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE)
                        .format(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())));
    }

    private Device findDevice(MeterInfo meterInfo) throws FaultMessage {
        if (meterInfo.getmRID() != null) {
            return deviceService.findDeviceByMrid(meterInfo.getmRID())
                    .orElseThrow(meterConfigFaultMessageFactory.meterConfigFaultMessageSupplier(meterInfo.getDeviceName(), MessageSeeds.NO_DEVICE_WITH_MRID, meterInfo.getmRID()));
        } else {
            return deviceService.findDeviceByName(meterInfo.getDeviceName())
                    .orElseThrow(meterConfigFaultMessageFactory.meterConfigFaultMessageSupplier(meterInfo.getDeviceName(), MessageSeeds.NO_DEVICE_WITH_NAME, meterInfo.getDeviceName()));
        }
    }

    @TransactionRequired
    public ServiceCall createParentGetMeterReadingsServiceCall(String source, String replyAddress, String corelationId,
                                                               DateTimeInterval timePeriod,
                                                               List<EndDevice> existedEndDevices,
                                                               List<ReadingType> existedReadingTypes,
                                                               List<String> existedLoadProfiles,
                                                               List<String> existedRegisterGroups,
                                                               String connectionMethod,
                                                               ScheduleStrategyEnum scheduleStrategy) throws
            ch.iec.tc57._2011.getmeterreadings.FaultMessage {

        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.PARENT_GET_METER_READINGS);
        ParentGetMeterReadingsDomainExtension parentGetMeterReadingsDomainExtension = new ParentGetMeterReadingsDomainExtension();
        parentGetMeterReadingsDomainExtension.setSource(source);
        parentGetMeterReadingsDomainExtension.setCallbackUrl(replyAddress);
        parentGetMeterReadingsDomainExtension.setCorelationId(corelationId);
        parentGetMeterReadingsDomainExtension.setTimePeriodStart(timePeriod.getStart());
        parentGetMeterReadingsDomainExtension.setTimePeriodEnd(timePeriod.getEnd());
        parentGetMeterReadingsDomainExtension.setReadingTypes(getReadingTypesString(existedReadingTypes));
        parentGetMeterReadingsDomainExtension.setLoadProfiles(getCommaSeparatedStringFromList(existedLoadProfiles));
        parentGetMeterReadingsDomainExtension.setRegisterGroups(getCommaSeparatedStringFromList(existedRegisterGroups));
        parentGetMeterReadingsDomainExtension.setConnectionMethod(connectionMethod);
        parentGetMeterReadingsDomainExtension.setScheduleStrategy(scheduleStrategy.getName());


        ServiceCallBuilder serviceCallBuilder = serviceCallType.newServiceCall()
                .origin("MultiSense")
                .extendedWith(parentGetMeterReadingsDomainExtension);
        /// TODO do not create Parent till all checks are done
        ServiceCall parentServiceCall = serviceCallBuilder.create();
        parentServiceCall.requestTransition(DefaultState.PENDING);
        parentServiceCall.requestTransition(DefaultState.ONGOING);
        /// TODO add check like checkConnectionMethodExists() here
//        if (ReadingSourceEnum.SYSTEM.getSource().equals(source)) {
//            initiateReading(parentServiceCall);
//            return parentServiceCall;
//        }
        boolean meterReadingRunning = false;
        for (EndDevice endDevice: existedEndDevices) {
            if (endDevice instanceof com.elster.jupiter.metering.Meter) {
                com.elster.jupiter.metering.Meter meter = (com.elster.jupiter.metering.Meter)endDevice;
                Device device = findDeviceForEndDevice(meter);
                if (!checkConnectionMethodExists(device, connectionMethod)) {
                    /// TODO throw exception like WrongCommunicationMethod (see confluence)
                    return parentServiceCall; // or even better return null
                }
                ServiceCall subParentServiceCall = createSubParentServiceCall(parentServiceCall, meter);

                if (isMeterReadingRequired(source, meter, existedReadingTypes,  timePeriod.getEnd())) {
                    subParentServiceCall.requestTransition(DefaultState.PENDING);
                    subParentServiceCall.requestTransition(DefaultState.ONGOING);
//                    readMeter(subParentServiceCall, meter, existedReadingTypes);
                    scheduleReading(subParentServiceCall, meter, existedReadingTypes, clock.instant());
                    subParentServiceCall.requestTransition(DefaultState.WAITING);
                    meterReadingRunning = true;
                }
            }
        }
        if (!meterReadingRunning) {
            initiateReading(parentServiceCall);
            return parentServiceCall;
        }
        parentServiceCall.requestTransition(DefaultState.WAITING);
        return parentServiceCall;
    }

    private ServiceCall createSubParentServiceCall(ServiceCall parent, com.elster.jupiter.metering.Meter meter) {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.SUBPARENT_GET_METER_READINGS);

        SubParentGetMeterReadingsDomainExtension subParentDomainExtension = new SubParentGetMeterReadingsDomainExtension();
        subParentDomainExtension.setEndDevice(meter.getMRID());

        return parent.newChildCall(serviceCallType)
                .extendedWith(subParentDomainExtension)
                .create();
    }

    private ServiceCall createComTaskExecutionServiceCall(ServiceCall subParentServiceCall, ServiceCallTypes childServiceCallType,
                                                          String comTaskName, Instant triggerDate, Device device) {
        ServiceCallType serviceCallType = getServiceCallType(childServiceCallType);

        ChildGetMeterReadingsDomainExtension childDomainExtension = new ChildGetMeterReadingsDomainExtension();
        childDomainExtension.setCommunicationTask(comTaskName);
        childDomainExtension.setTriggerDate(new BigDecimal(triggerDate.toEpochMilli()));

        return subParentServiceCall.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension)
                .targetObject(device)
                .create();
    }

    /// TODO check thrown NoSuchElementException
    private Device findDeviceForEndDevice(com.elster.jupiter.metering.Meter meter) {
        long deviceId = Long.parseLong(meter.getAmrId());
        return deviceService.findDeviceById(deviceId).orElseThrow(NoSuchElementException.deviceWithIdNotFound(thesaurus, deviceId));
    }

    private boolean checkConnectionMethodExists(Device device, String connectionMethod) {
        return device.getComTaskExecutions().stream()
                /// TODO check optional
                .map(cte -> cte.getConnectionTask().get().getPartialConnectionTask().getName())
//                .map(ct -> ct.getPartialConnectionTask())
//                .map(pct -> pct.getName())
                .anyMatch(taskName -> taskName.equalsIgnoreCase(connectionMethod));
    }

    private void initiateReading(ServiceCall serviceCall) {
        serviceCall.requestTransition(DefaultState.PAUSED);
        serviceCall.requestTransition(DefaultState.ONGOING);
    }

    private void scheduleReading(ServiceCall subParentServiceCall, com.elster.jupiter.metering.Meter meter, List<ReadingType> readingTypes,
                                 Instant triggerDate) throws ch.iec.tc57._2011.getmeterreadings.FaultMessage {
        Device multiSenseDevice = findDeviceForEndDevice(meter);
        Map<ComTaskExecution, List<ReadingType>> comTaskExecReadingTypeMap = getSupportedReadingTypeExecutionMapping(multiSenseDevice, readingTypes);
        Set<ReadingType> supportedReadingTypes = comTaskExecReadingTypeMap.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        if (supportedReadingTypes.size() < readingTypes.size()) {
            /// TODO think how to deal with that
            subParentServiceCall.log(LogLevel.SEVERE, "Some reading types are not supported");
            subParentServiceCall.requestTransition(DefaultState.FAILED);
        } else {
            /// TODO calculate trigger time
            comTaskExecReadingTypeMap.keySet().forEach(cte -> {
                ServiceCall serviceCall = createComTaskExecutionServiceCall(subParentServiceCall,
                        ServiceCallTypes.COMTASK_EXECUTION_GET_METER_READINGS, cte.getComTask().getName(), triggerDate, multiSenseDevice);
                serviceCall.requestTransition(DefaultState.PENDING);
                serviceCall.requestTransition(DefaultState.ONGOING);
                scheduleComTaskExecution(cte, triggerDate);
            } );
        }
    }

    private Map<ComTaskExecution, List<ReadingType>> getSupportedReadingTypeExecutionMapping(Device device,
                                                                                             Collection<ReadingType> readingTypes)
            throws ch.iec.tc57._2011.getmeterreadings.FaultMessage {
        List<ComTaskExecution> comTaskExecutions = device.getComTaskExecutions();
        Map<ComTaskExecution, List<ReadingType>> comTaskExecReadingTypeMap = getSupportedReadingTypeExecutionMapping(comTaskExecutions, readingTypes);
        Set<ReadingType> readingTypesWithExecutions = comTaskExecReadingTypeMap.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        if (readingTypesWithExecutions.size() < readingTypes.size()) {
            for (ComTaskEnablement comTaskEnablement : device.getDeviceConfiguration().getComTaskEnablements()) {
                Optional<ComTaskExecution> existingComTaskExecution = device.getComTaskExecutions().stream()
                        .filter(cte -> cte.getComTask().getId() == comTaskEnablement.getComTask().getId())
                        .findFirst();
                comTaskExecutions.add(existingComTaskExecution.orElseGet(() -> createAdHocComTaskExecution(device, comTaskEnablement)));
            }

            getSupportedReadingTypeExecutionMapping(
                    device.getDeviceConfiguration()
                            .getComTaskEnablements()
                            .stream()
                            .filter(comTaskEnablement -> !comTaskExecutions.stream()
                                    .anyMatch(cte -> cte.getComTask().getId() == comTaskEnablement.getComTask().getId()))
                            .map(comTaskEnablement -> createAdHocComTaskExecution(device, comTaskEnablement))
                            .collect(Collectors.toList()), readingTypes)
                            .forEach((key, value) -> {
                                    if (comTaskExecReadingTypeMap.containsKey(key)) {
                                        comTaskExecReadingTypeMap.get(key).addAll(value);
                                    } else {
                                        comTaskExecReadingTypeMap.put(key,value);
                                    }
                             });

        }
        return comTaskExecReadingTypeMap;
    }

    private ComTaskExecution createAdHocComTaskExecution(Device device, ComTaskEnablement comTaskEnablement) {
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        if (comTaskEnablement.hasPartialConnectionTask()) {
            device.getConnectionTasks().stream()
                    .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == comTaskEnablement.getPartialConnectionTask().get().getId())
                    .findFirst()
                    .ifPresent(comTaskExecutionBuilder::connectionTask);
        }
        ComTaskExecution manuallyScheduledComTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        return manuallyScheduledComTaskExecution;
    }

    private Map<ComTaskExecution, List<ReadingType>> getSupportedReadingTypeExecutionMapping(Collection<ComTaskExecution> comTaskExecutions,
                                                                                             Collection<ReadingType> readingTypes) throws ch.iec.tc57._2011.getmeterreadings.FaultMessage {
        Map<ComTaskExecution, List<ReadingType>> comTaskExecReadingTypeMap = new HashMap<>();
        ComTaskExecution regularExecution = getComTaskExecutionForReadingTypes(comTaskExecutions, readingTypes, true);
        if (regularExecution != null) {
            comTaskExecReadingTypeMap.put(regularExecution, readingTypes.stream()
                    .filter(ReadingType::isRegular)
                    .collect(Collectors.toList()));
        }
        ComTaskExecution irRegularExecution = getComTaskExecutionForReadingTypes(comTaskExecutions, readingTypes, false);
        if (irRegularExecution != null) {
            comTaskExecReadingTypeMap.put(irRegularExecution, readingTypes.stream()
                    .filter(readingType -> !readingType.isRegular())
                    .collect(Collectors.toList()));
        }
        return comTaskExecReadingTypeMap;
    }

    private ComTaskExecution getComTaskExecutionForReadingTypes(Collection<ComTaskExecution> comTaskExecutions, Collection<ReadingType> readingTypes,
                                                                boolean isRegular) throws ch.iec.tc57._2011.getmeterreadings.FaultMessage {
        final Class<?> clazz;
        if (isRegular) {
            clazz = LoadProfilesTask.class;
        } else {
            clazz = RegistersTask.class;
        }
        if (readingTypes.stream()
                .anyMatch(readingType -> readingType.isRegular() == isRegular)) {
            return comTaskExecutions.stream()
                    .filter(comTaskExecution -> comTaskExecution.getProtocolTasks().stream()
                            .anyMatch(protocolTask -> clazz.isInstance(protocolTask)))
                            /// TODO add proper exception description
                            .findAny().orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_READING_TYPES));

        }
        return null;


    }

    private void scheduleComTaskExecution(ComTaskExecution comTaskExecution, Instant instant) {
        comTaskExecution.addNewComTaskExecutionTrigger(instant);
        comTaskExecution.updateNextExecutionTimestamp();
    }

    private boolean isMeterReadingRequired(String source, com.elster.jupiter.metering.Meter meter, List<ReadingType> readingTypes, Instant endTime) {
        if (ReadingSourceEnum.METER.getSource().equals(source)) {
            return true;
        }
        if (ReadingSourceEnum.HYBRID.getSource().equals(source)) {
            return meter.getChannelsContainers().stream().
                    anyMatch(container -> isChannelContainerReadOutRequired(container, readingTypes, endTime));
        }
        return false;
    }

    private boolean isChannelContainerReadOutRequired(ChannelsContainer channelsContainer, List<ReadingType> readingTypes, Instant endTime) {
        Set<String> readingTypeMRIDs = readingTypes.stream().map(ert -> ert.getMRID()).collect(Collectors.toSet());
        Range<Instant> range = channelsContainer.getInterval().toOpenClosedRange();
        if (!range.lowerEndpoint().isBefore(endTime)) {
            return false;
        }
        if (range.hasUpperBound()) {
            endTime = endTime.isBefore(range.upperEndpoint()) ? endTime : range.upperEndpoint();
        }
        for (Channel channel : channelsContainer.getChannels()) {
            Instant endInstant = channel.truncateToIntervalLength(endTime);
            for (ReadingType readingType : channel.getReadingTypes()) {
                if (readingTypeMRIDs.contains(readingType.getMRID())) {
                    Instant instant = channel.getLastDateTime();
                    if (instant == null || instant.isBefore(endInstant)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String getReadingTypesString(List<ReadingType> existedReadingTypes) {
        return existedReadingTypes == null ? null : existedReadingTypes.stream()
                .map(ert -> ert.getMRID())
                .collect(Collectors.joining(";"));
    }

    private String getCommaSeparatedStringFromList(List<String> strings) {
        return strings == null ? null : strings.stream()
                .collect(Collectors.joining(";"));
    }

    private ServiceCallType getServiceCallType(String handlerName, String version) {
        return serviceCallService.findServiceCallType(handlerName, version)
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE)
                        .format(handlerName, version)));
    }
}
/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.XsdDateTimeConverter;
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
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.exceptions.NoSuchElementException;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.RegistersTask;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.getmeterreadings.DateTimeInterval;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;
import com.google.common.collect.Range;
import org.osgi.framework.BundleContext;

import java.time.temporal.ChronoUnit;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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

    private static final String RECURENT_TASK_READ_OUT_DELAY = "com.energyict.mdc.cim.webservices.inbound.soap.readoutdelay";

    private final DeviceService deviceService;
    private final JsonService jsonService;
    private final EndDeviceEventsBuilder endDeviceEventsBuilder;
    private final MeterConfigParser meterConfigParser;
    private final MeterConfigFaultMessageFactory meterConfigFaultMessageFactory;
    private final ServiceCallService serviceCallService;
    private final Thesaurus thesaurus;
    private final MeterReadingFaultMessageFactory faultMessageFactory;
    private final Clock clock;
    private final BundleContext bundleContext;
    private final MasterDataService masterDataService;

    @Inject
    public ServiceCallCommands(DeviceService deviceService, JsonService jsonService,
                               MeterConfigParser meterConfigParser, MeterConfigFaultMessageFactory meterConfigFaultMessageFactory,
                               ServiceCallService serviceCallService, EndDeviceEventsBuilder endDeviceEventsBuilder,
                               Thesaurus thesaurus, MeterReadingFaultMessageFactory faultMessageFactory, Clock clock,
                               BundleContext bundleContext, MasterDataService masterDataService) {
        this.deviceService = deviceService;
        this.jsonService = jsonService;
        this.endDeviceEventsBuilder = endDeviceEventsBuilder;
        this.meterConfigParser = meterConfigParser;
        this.meterConfigFaultMessageFactory = meterConfigFaultMessageFactory;
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
        this.faultMessageFactory = faultMessageFactory;
        this.clock = clock;
        this.bundleContext = bundleContext;
        this.masterDataService = masterDataService;

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
                                                               List<com.elster.jupiter.metering.Meter> existedMeters,
                                                               List<ReadingType> existedReadingTypes,
                                                               List<String> existedLoadProfiles,
                                                               List<String> existedRegisterGroups,
                                                               String connectionMethod,
                                                               ScheduleStrategyEnum scheduleStrategy) throws
            ch.iec.tc57._2011.getmeterreadings.FaultMessage {

        Instant start = timePeriod.getStart();
        Instant end = timePeriod.getEnd();
        String property = bundleContext.getProperty(RECURENT_TASK_READ_OUT_DELAY);
        long delay = property == null ? 1 : Long.parseLong(property);
        Instant now = clock.instant();

        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.PARENT_GET_METER_READINGS);
        ParentGetMeterReadingsDomainExtension parentGetMeterReadingsDomainExtension = new ParentGetMeterReadingsDomainExtension();
        parentGetMeterReadingsDomainExtension.setSource(source);
        parentGetMeterReadingsDomainExtension.setCallbackUrl(replyAddress);
        parentGetMeterReadingsDomainExtension.setCorelationId(corelationId);
        parentGetMeterReadingsDomainExtension.setTimePeriodStart(start);
        parentGetMeterReadingsDomainExtension.setTimePeriodEnd(end);
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
        if (ReadingSourceEnum.SYSTEM.getSource().equals(source)) {
            createSubParentServiceCallsAndTransitToSuccessfull(existedMeters, parentServiceCall);
            initiateReading(parentServiceCall);
            return parentServiceCall;
        }
        boolean meterReadingRunning = false;
        for (com.elster.jupiter.metering.Meter meter: existedMeters) {
            Device device = findDeviceForEndDevice(meter);
            ServiceCall subParentServiceCall = createSubParentServiceCall(parentServiceCall, meter);

            List<LoadProfile> loadProfiles = getExistedOnDeviceLoadProfiles(device, existedLoadProfiles);
            ComTaskExecution deviceMessagesComTaskExecution = null;

            if (loadProfiles != null && !loadProfiles.isEmpty()) {
                if (start != null && end != null) { // load profiles case
                    deviceMessagesComTaskExecution = findComTaskExecutionForDeviceMessages(device);
                    Instant trigger = getTriggerDate(end, delay, deviceMessagesComTaskExecution, scheduleStrategy);
                    createDeviceMessages(device, loadProfiles, trigger, start, end);
                    if (scheduleStrategy == ScheduleStrategyEnum.RUN_NOW) {
                        if (trigger.isAfter(now)) { // use recurrent task
                            processComTaskExectionByRecurrentTask(subParentServiceCall, deviceMessagesComTaskExecution, trigger,
                                    end, start, ServiceCallTypes.DEVICE_MESSAGE_GET_METER_READINGS);
                        } else { // run now
                            scheduleOrRunNowComTaskExecution(subParentServiceCall, device, deviceMessagesComTaskExecution, trigger,
                                    start, end, ServiceCallTypes.DEVICE_MESSAGE_GET_METER_READINGS, true);
                        }
                    } else { // use schedule
                        scheduleOrRunNowComTaskExecution(subParentServiceCall, device, deviceMessagesComTaskExecution, trigger,
                                start, end, ServiceCallTypes.DEVICE_MESSAGE_GET_METER_READINGS, false);
                    }
                } else {
                    /// TODO throw exception that start and end date is required for load profile message
                }
            }

            // TODO check Hybrid
            if (isMeterReadingRequired(source, meter, existedReadingTypes, timePeriod.getEnd())) {
                subParentServiceCall.requestTransition(DefaultState.PENDING);
                subParentServiceCall.requestTransition(DefaultState.ONGOING);

                //****

                List<ComTaskExecution> existedComTaskExecutions = findComTaskExecutions(device, existedReadingTypes,
                        existedRegisterGroups);
                for (ComTaskExecution comTaskExecution : existedComTaskExecutions) {
                    if (!checkCommectionMethodForComTaskExecution(comTaskExecution, connectionMethod)) {
                        // TODO error?
                    }
                    Instant actualStart = getActualStart(start, comTaskExecution);
                    Instant actualEnd = getActualEnd(end, now);
                    /// TODO proper exception
                    if (!actualEnd.isAfter(actualStart)) {
                        throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                                MessageSeeds.INVALID_OR_EMPTY_TIME_PERIOD,
                                XsdDateTimeConverter.marshalDateTime(actualStart),
                                XsdDateTimeConverter.marshalDateTime(actualEnd)).get();
                    }
                    Instant trigger = getTriggerDate(actualEnd, delay, comTaskExecution, scheduleStrategy);


                    if (comTaskExecutionRequired(start, end, comTaskExecution)) {
                        // run now
                        if (scheduleStrategy == ScheduleStrategyEnum.RUN_NOW) {
                            if (start == null && end == null) {
                                processComTaskExectionByRecurrentTask(subParentServiceCall, comTaskExecution, trigger,
                                        actualStart, actualEnd, ServiceCallTypes.COMTASK_EXECUTION_GET_METER_READINGS);
                            } else if (start != null && end == null) { // shift the 'next reading block start' to the 'time period start'
                                setLastReading(existedReadingTypes, device, start);
                                processComTaskExectionByRecurrentTask(subParentServiceCall, comTaskExecution, trigger,
                                        actualStart, actualEnd, ServiceCallTypes.COMTASK_EXECUTION_GET_METER_READINGS);
                            } else if (end != null && !trigger.isAfter(now)) {
                                scheduleOrRunNowComTaskExecution(subParentServiceCall, device, comTaskExecution, trigger,
                                        actualStart, actualEnd, ServiceCallTypes.COMTASK_EXECUTION_GET_METER_READINGS, true);
                            } else if (end != null && trigger.isAfter(now)) {
                                processComTaskExectionByRecurrentTask(subParentServiceCall, comTaskExecution, trigger,
                                        actualStart, actualEnd, ServiceCallTypes.COMTASK_EXECUTION_GET_METER_READINGS);
                            }
                        } else { // use schedule
                            /// TODO what if start/end dates not specified?
                            scheduleOrRunNowComTaskExecution(subParentServiceCall, device, comTaskExecution, trigger,
                                    actualStart, actualEnd, ServiceCallTypes.COMTASK_EXECUTION_GET_METER_READINGS, false);
                            // wait next task execution
                        }
                    }
                }

                //***

                subParentServiceCall.requestTransition(DefaultState.WAITING);
                meterReadingRunning = true;
            }
        }
        if (!meterReadingRunning) {
            initiateReading(parentServiceCall);
            return parentServiceCall;
        }
        parentServiceCall.requestTransition(DefaultState.WAITING);
        return parentServiceCall;
    }

    private void createSubParentServiceCallsAndTransitToSuccessfull(List<com.elster.jupiter.metering.Meter> existedMeters, ServiceCall parentServiceCall) {
        existedMeters.forEach(meter -> {
            ServiceCall subParentServiceCall = createSubParentServiceCall(parentServiceCall, meter);
            subParentServiceCall.requestTransition(DefaultState.PENDING);
            subParentServiceCall.requestTransition(DefaultState.ONGOING);
        });
    }

    // channels without concrete start or end date / registers with start and end dates
    private boolean comTaskExecutionRequired(Instant start, Instant end, ComTaskExecution comTaskExecution) {
        return (comTaskExecution.getProtocolTasks().stream()
                .anyMatch(protocolTask -> LoadProfile.class.isInstance(protocolTask))
                && (start == null || end == null))
            || (comTaskExecution.getProtocolTasks().stream()
                .anyMatch(protocolTask -> RegistersTask.class.isInstance(protocolTask))
                && start != null && end != null);
    }

    private Instant getTriggerDate(Instant actualEnd, long delay, ComTaskExecution comTaskExecution,
                                   ScheduleStrategyEnum scheduleStrategy) {
        Instant trigger;
        if (scheduleStrategy == ScheduleStrategyEnum.RUN_NOW) {
            trigger = actualEnd.plus(delay, ChronoUnit.MINUTES);
        } else { // USE_SCHEDULE
            Instant next = comTaskExecution.getNextExecutionTimestamp();
            if (next.isBefore(actualEnd)) {
                trigger = actualEnd;
            } else {
                trigger = next;
            }
        }
        return trigger;
    }

    private void setLastReading(List<ReadingType> readingTypes, Device device, Instant start) {
        Set<String> mrids = readingTypes.stream()
                .map(readingType -> readingType.getMRID())
                .collect(Collectors.toSet());

        device.getLoadProfiles().stream()
                .filter(loadProfile ->  loadProfile.getChannels().stream()
                            .anyMatch(channel -> mrids.contains(channel.getReadingType().getMRID()))
                )
                .forEach(loadProfile -> device.getLoadProfileUpdaterFor(loadProfile).setLastReading(start).update());
    }

    /// FIXME use load profile next block read start
    private Instant getActualStart(Instant start, ComTaskExecution comTaskExecution){
        if (start == null) {
            return comTaskExecution.getLastSuccessfulCompletionTimestamp();
        }
        return start;
    }

    private Instant getActualEnd(Instant end, Instant now) {
        if (end == null) {
            return now;
        }
        return end;
    }

    private List<ComTaskExecution> findComTaskExecutions(Device device, List<ReadingType> existedReadingTypes,
                                                         List<String> existedRegisterGroups) throws
            ch.iec.tc57._2011.getmeterreadings.FaultMessage {
        List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
        if (existedReadingTypes != null && !existedReadingTypes.isEmpty()) {
            comTaskExecutions.addAll(getSupportedReadingTypeExecutionMapping(device, existedReadingTypes).keySet());
        }
        if (existedRegisterGroups != null && !existedRegisterGroups.isEmpty()) {
            comTaskExecutions.addAll(getSupportedReadingTypeExecutionMapping(device, masterDataService.findAllRegisterGroups().stream()
                    .filter(rg -> existedRegisterGroups.contains(rg.getName()))
                    .map(RegisterGroup::getRegisterTypes)
                    .flatMap(Collection::stream)
                    .map(registerType -> registerType.getReadingType())
                    .collect(Collectors.toList()))
                    .keySet());
        }
        return comTaskExecutions;
    }

    private ComTaskExecution findComTaskExecutionForDeviceMessages(Device device)
            throws ch.iec.tc57._2011.getmeterreadings.FaultMessage {
        Set<ComTaskExecution> comTaskExecutions = new HashSet<>();
        comTaskExecutions.addAll(device.getComTaskExecutions());

        comTaskExecutions.addAll(device.getDeviceConfiguration().getComTaskEnablements().stream()
                .filter(comTaskEnablement -> !comTaskExecutions.stream()
                        .anyMatch(cte -> cte.getComTask().getId() == comTaskEnablement.getComTask().getId()))
                .map(comTaskEnablement -> createAdHocComTaskExecution(device, comTaskEnablement))
                .collect(Collectors.toSet()));

        return comTaskExecutions.stream()
                .filter(comTaskExecution -> comTaskExecution.getProtocolTasks().stream()
                        .anyMatch(protocolTask -> MessagesTask.class.isInstance(protocolTask)))
                /// TODO add proper exception description
                .findAny().orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_READING_TYPES));
    }

    private void processComTaskExectionByRecurrentTask(ServiceCall subParentServiceCall,
                                                       ComTaskExecution comTaskExecution, Instant trigger,
                                                       Instant actualStart, Instant actualEnd,
                                                       ServiceCallTypes serviceCallTypes) {
        ServiceCall childServiceCall = createChildGetMeterReadingServiceCall(subParentServiceCall,
                serviceCallTypes, comTaskExecution.getComTask().getName(), trigger, actualStart, actualEnd,
                comTaskExecution.getDevice());
        // recurrent task will check childServiceCall in state SCHEDULED
        childServiceCall.requestTransition(DefaultState.SCHEDULED);
    }

    private void scheduleOrRunNowComTaskExecution(ServiceCall subParentServiceCall, Device device,
                                                  ComTaskExecution comTaskExecution, Instant trigger,
                                                  Instant actualStart, Instant actualEnd,
                                                  ServiceCallTypes serviceCallTypes, boolean runNow) {
        ServiceCall childServiceCall = createChildGetMeterReadingServiceCall(subParentServiceCall,
                serviceCallTypes, comTaskExecution.getComTask().getName(), trigger,
                actualStart, actualEnd, device);
        childServiceCall.requestTransition(DefaultState.PENDING);
        childServiceCall.requestTransition(DefaultState.ONGOING);
        childServiceCall.requestTransition(DefaultState.WAITING);
        if (runNow) {
            comTaskExecution.runNow();
        }
    }


    private ServiceCall createSubParentServiceCall(ServiceCall parent, com.elster.jupiter.metering.Meter meter) {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.SUBPARENT_GET_METER_READINGS);

        SubParentGetMeterReadingsDomainExtension subParentDomainExtension = new SubParentGetMeterReadingsDomainExtension();
        subParentDomainExtension.setEndDevice(meter.getMRID());

        return parent.newChildCall(serviceCallType)
                .extendedWith(subParentDomainExtension)
                .create();
    }

    private ServiceCall createChildGetMeterReadingServiceCall(ServiceCall subParentServiceCall, ServiceCallTypes childServiceCallType,
                                                              String comTaskName, Instant triggerDate, Instant actualStart,
                                                              Instant actualEnd, Device device) {
        ServiceCallType serviceCallType = getServiceCallType(childServiceCallType);

        ChildGetMeterReadingsDomainExtension childDomainExtension = new ChildGetMeterReadingsDomainExtension();
        childDomainExtension.setCommunicationTask(comTaskName);
        childDomainExtension.setTriggerDate(triggerDate);
        childDomainExtension.setActialStartDate(actualStart);
        childDomainExtension.setActialEndDate(actualEnd);

        return subParentServiceCall.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension)
                .targetObject(device)
                .create();
    }

    private Device findDeviceForEndDevice(com.elster.jupiter.metering.Meter meter) {
        long deviceId = Long.parseLong(meter.getAmrId());
        return deviceService.findDeviceById(deviceId).orElseThrow(NoSuchElementException.deviceWithIdNotFound(thesaurus, deviceId));
    }

    private boolean checkCommectionMethodForComTaskExecution(ComTaskExecution comTaskExecution, String connectionMethod) throws
            ch.iec.tc57._2011.getmeterreadings.FaultMessage {
        return comTaskExecution.getConnectionTask()
                .orElseThrow(faultMessageFactory.createMeterReadingFaultMessageSupplier(MessageSeeds.NO_CONNECTION_TASK,
                        comTaskExecution.getComTask().getName())).getPartialConnectionTask().getName().equalsIgnoreCase(connectionMethod);
    }

    private void initiateReading(ServiceCall serviceCall) {
        serviceCall.requestTransition(DefaultState.PAUSED);
        serviceCall.requestTransition(DefaultState.ONGOING);
    }

    private List<LoadProfile> getExistedOnDeviceLoadProfiles(Device device, List<String> existedLoadProfiles) {
        if (existedLoadProfiles != null) {
            return device.getLoadProfiles().stream()
                    .filter(loadProfile -> existedLoadProfiles.contains(loadProfile.getLoadProfileSpec().getLoadProfileType().getName()))
                    .collect(Collectors.toList());
        }
        return null;
    }

    private List<DeviceMessage> createDeviceMessages(Device device, List<LoadProfile> loadProfiles,
                                                     Instant releaseDate, Instant start, Instant end) {
        return loadProfiles.stream()
                .map(loadProfile -> createLoadProfilMessage(device, loadProfile, releaseDate, start, end, DeviceMessageId.LOAD_PROFILE_PARTIAL_REQUEST))
                .collect(Collectors.toList());
    }

    private DeviceMessage createLoadProfilMessage(Device device, LoadProfile loadProfile, Instant releaseDate,
                                                  Instant start, Instant end, DeviceMessageId deviceMessageId) {
        Device.DeviceMessageBuilder deviceMessageBuilder = device.newDeviceMessage(deviceMessageId)
//                .setTrackingId(Long.toString(serviceCall.getId()))
                .setReleaseDate(releaseDate);

        // for DeviceMessageId.LOAD_PROFILE_PARTIAL_REQUEST
        // LOAD_PROFILE_PARTIAL_REQUEST(13001)
        deviceMessageBuilder.addProperty("load profile", loadProfile);//type is: com.energyict.mdc.upl.meterdata.LoadProfile
        deviceMessageBuilder.addProperty("from", Date.from(start)); // java.util.Date
        deviceMessageBuilder.addProperty("to", Date.from(end)); // java.util.Date
        return deviceMessageBuilder.add();
    }

    private Map<ComTaskExecution, List<ReadingType>> getSupportedReadingTypeExecutionMapping(Device device,
                                                                                             Collection<ReadingType> readingTypes)
            throws ch.iec.tc57._2011.getmeterreadings.FaultMessage {
        List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
        comTaskExecutions.addAll(device.getComTaskExecutions());
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
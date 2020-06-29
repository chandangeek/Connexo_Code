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
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.cim.webservices.inbound.soap.MeterInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.getenddeviceevents.EndDeviceEventsBuilder;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.TranslationKeys;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.XsdDateTimeConverter;
import com.energyict.mdc.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageAction;
import com.energyict.mdc.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageFaultMessageFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigFaultMessageFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigParser;
import com.energyict.mdc.cim.webservices.inbound.soap.meterreadings.MeterReadingFaultMessageFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.meterreadings.ReadingSourceEnum;
import com.energyict.mdc.cim.webservices.inbound.soap.meterreadings.ScheduleStrategy;
import com.energyict.mdc.cim.webservices.inbound.soap.meterreadings.SyncReplyIssue;
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
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigMasterServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.MasterDataLinkageConfigServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.ConfigEventInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.EndDeviceInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.UsagePointInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigMasterCustomPropertySet;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigMasterDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigMasterServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig.MeterConfigServiceCallHandler;
import com.energyict.mdc.cim.webservices.outbound.soap.OperationEnum;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LoadProfile;
import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.NoSuchElementException;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.masterdata.MasterDataService;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.getmeterreadings.DateTimeInterval;
import ch.iec.tc57._2011.getmeterreadings.Reading;
import ch.iec.tc57._2011.masterdatalinkageconfig.ConfigurationEvent;
import ch.iec.tc57._2011.masterdatalinkageconfig.EndDevice;
import ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigRequestMessageType;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;
import ch.iec.tc57._2011.schema.message.HeaderType;
import com.google.common.collect.Range;
import org.apache.commons.collections4.CollectionUtils;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public class ServiceCallCommands {

    public enum ServiceCallTypes {
        MASTER_METER_CONFIG(MeterConfigMasterServiceCallHandler.SERVICE_CALL_HANDLER_NAME, MeterConfigMasterServiceCallHandler.VERSION, MeterConfigMasterServiceCallHandler.APPLICATION, MeterConfigMasterCustomPropertySet.class
                .getName()),
        METER_CONFIG(MeterConfigServiceCallHandler.SERVICE_CALL_HANDLER_NAME, MeterConfigServiceCallHandler.VERSION, MeterConfigServiceCallHandler.APPLICATION, MeterConfigCustomPropertySet.class.getName()),
        GET_END_DEVICE_EVENTS(GetEndDeviceEventsServiceCallHandler.SERVICE_CALL_HANDLER_NAME, GetEndDeviceEventsServiceCallHandler.VERSION, GetEndDeviceEventsServiceCallHandler.APPLICATION, GetEndDeviceEventsCustomPropertySet.class
                .getName()),
        PARENT_GET_METER_READINGS(ParentGetMeterReadingsServiceCallHandler.SERVICE_CALL_HANDLER_NAME, ParentGetMeterReadingsServiceCallHandler.VERSION, ParentGetMeterReadingsServiceCallHandler.APPLICATION, ParentGetMeterReadingsCustomPropertySet.class
                .getName()),
        SUBPARENT_GET_METER_READINGS(SubParentGetMeterReadingsServiceCallHandler.SERVICE_CALL_HANDLER_NAME, SubParentGetMeterReadingsServiceCallHandler.VERSION, SubParentGetMeterReadingsServiceCallHandler.APPLICATION, SubParentGetMeterReadingsCustomPropertySet.class
                .getName()),
        DEVICE_MESSAGE_GET_METER_READINGS(DeviceMessageServiceCallHandler.SERVICE_CALL_HANDLER_NAME, DeviceMessageServiceCallHandler.VERSION, DeviceMessageServiceCallHandler.APPLICATION, ChildGetMeterReadingsCustomPropertySet.class
                .getName()),
        COMTASK_EXECUTION_GET_METER_READINGS(ComTaskExecutionServiceCallHandler.SERVICE_CALL_HANDLER_NAME, ComTaskExecutionServiceCallHandler.VERSION, ComTaskExecutionServiceCallHandler.APPLICATION, ChildGetMeterReadingsCustomPropertySet.class
                .getName()),
        MASTER_DATA_LINKAGE_CONFIG(MasterDataLinkageConfigMasterServiceCallHandler.SERVICE_CALL_HANDLER_NAME, MasterDataLinkageConfigMasterServiceCallHandler.VERSION, MasterDataLinkageConfigMasterServiceCallHandler.APPLICATION, MasterDataLinkageConfigMasterCustomPropertySet.class.getName()),
        DATA_LINKAGE_CONFIG(MasterDataLinkageConfigServiceCallHandler.SERVICE_CALL_HANDLER_NAME, MasterDataLinkageConfigServiceCallHandler.VERSION, MasterDataLinkageConfigServiceCallHandler.APPLICATION, MasterDataLinkageConfigCustomPropertySet.class.getName());


        private final String typeName;
        private final String typeVersion;
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private final String reservedByApplication;
        private final String customPropertySetClass;

        ServiceCallTypes(String typeName, String typeVersion, String application, String customPropertySetClass) {
            this.typeName = typeName;
            this.typeVersion = typeVersion;
            this.reservedByApplication = application;
            this.customPropertySetClass = customPropertySetClass;
        }

        public String getTypeName() {
            return typeName;
        }

        public String getTypeVersion() {
            return typeVersion;
        }

        public Optional<String> getApplication() {
            return Optional.ofNullable(reservedByApplication);
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
    private final Clock clock;
    private final MasterDataService masterDataService;
    private final CommunicationTaskService communicationTaskService;

    @Inject
    public ServiceCallCommands(DeviceService deviceService, JsonService jsonService,
                               MeterConfigParser meterConfigParser, MeterConfigFaultMessageFactory meterConfigFaultMessageFactory,
                               ServiceCallService serviceCallService, EndDeviceEventsBuilder endDeviceEventsBuilder,
                               Thesaurus thesaurus, MeterReadingFaultMessageFactory faultMessageFactory, Clock clock,
                               MasterDataService masterDataService, CommunicationTaskService communicationTaskService) {
        this.deviceService = deviceService;
        this.jsonService = jsonService;
        this.endDeviceEventsBuilder = endDeviceEventsBuilder;
        this.meterConfigParser = meterConfigParser;
        this.meterConfigFaultMessageFactory = meterConfigFaultMessageFactory;
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
        this.faultMessageFactory = faultMessageFactory;
        this.clock = clock;
        this.masterDataService = masterDataService;
        this.communicationTaskService = communicationTaskService;
    }

    @TransactionRequired
    public ServiceCall createMeterConfigMasterServiceCall(MeterConfig meterConfig, EndPointConfiguration outboundEndPointConfiguration,
                                                          OperationEnum operation, String correlationId) throws FaultMessage {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.MASTER_METER_CONFIG);

        MeterConfigMasterDomainExtension meterConfigMasterDomainExtension = new MeterConfigMasterDomainExtension();
        meterConfigMasterDomainExtension.setActualNumberOfSuccessfulCalls(0l);
        meterConfigMasterDomainExtension.setActualNumberOfFailedCalls(0l);
        meterConfigMasterDomainExtension.setExpectedNumberOfCalls(Long.valueOf(meterConfig.getMeter().size()));
        meterConfigMasterDomainExtension.setCorrelationId(correlationId);
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
        if (OperationEnum.GET.equals(operation) || OperationEnum.DELETE.equals(operation)) {
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
                                                                 Range<Instant> interval, EndPointConfiguration outboundEndPointConfiguration,
                                                                 String correlationId)
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
        domainExtension.setCorrelationId(correlationId);

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
    public ServiceCall createParentGetMeterReadingsServiceCallWithChildren(HeaderType header, Reading reading,
                                                                           int index, SyncReplyIssue syncReplyIssue,
                                                                           Set<ReadingType> combinedReadingTypes) throws
            ch.iec.tc57._2011.getmeterreadings.FaultMessage {

        DateTimeInterval timePeriod = reading.getTimePeriod();
        String source = reading.getSource();
        String connectionMethod = reading.getConnectionMethod();
        ScheduleStrategy scheduleStrategy = getScheduleStrategy(reading.getScheduleStrategy());

        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.PARENT_GET_METER_READINGS);
        ParentGetMeterReadingsDomainExtension parentGetMeterReadingsDomainExtension = new ParentGetMeterReadingsDomainExtension();
        parentGetMeterReadingsDomainExtension.setSource(source);
        parentGetMeterReadingsDomainExtension.setCallbackUrl(header.getReplyAddress());
        parentGetMeterReadingsDomainExtension.setCorrelationId(header.getCorrelationID());
        parentGetMeterReadingsDomainExtension.setTimePeriodStart(timePeriod == null ? null : timePeriod.getStart());
        parentGetMeterReadingsDomainExtension.setTimePeriodEnd(timePeriod == null ? null : timePeriod.getEnd());
        if (ReadingSourceEnum.SYSTEM.getSource().equals(source) && parentGetMeterReadingsDomainExtension.getTimePeriodEnd() == null) {
            parentGetMeterReadingsDomainExtension.setTimePeriodEnd(clock.instant());
        }
        parentGetMeterReadingsDomainExtension.setReadingTypes(getReadingTypesString(syncReplyIssue.getExistedReadingTypes()));
        parentGetMeterReadingsDomainExtension.setLoadProfiles(getSemicolonSeparatedStringFromSet(syncReplyIssue.getReadingExistedLoadProfilesMap().get(index)));
        parentGetMeterReadingsDomainExtension.setRegisterGroups(getSemicolonSeparatedStringFromSet(syncReplyIssue.getReadingExistedRegisterGroupsMap().get(index)));
        parentGetMeterReadingsDomainExtension.setConnectionMethod(connectionMethod);
        parentGetMeterReadingsDomainExtension.setScheduleStrategy(scheduleStrategy.getName());
        parentGetMeterReadingsDomainExtension.setResponseStatus(ParentGetMeterReadingsDomainExtension.ResponseStatus.NOT_SENT.getName());

        ServiceCallBuilder serviceCallBuilder = serviceCallType.newServiceCall()
                .origin("MultiSense")
                .extendedWith(parentGetMeterReadingsDomainExtension);
        ServiceCall parentServiceCall = serviceCallBuilder.create();
        parentServiceCall.requestTransition(DefaultState.PENDING);
        parentServiceCall.requestTransition(DefaultState.ONGOING);

        if (ReadingSourceEnum.SYSTEM.getSource().equals(source)) {
            syncReplyIssue.getExistedMeters().forEach(meter -> createSubParentServiceCall(parentServiceCall, meter));
            initiateReading(parentServiceCall);
            return parentServiceCall;
        }

        boolean meterReadingRunning = false;
        for (com.elster.jupiter.metering.Meter meter : syncReplyIssue.getExistedMeters()) {
            boolean isOk = processSubParentServiceCallWithChildren(meter, parentServiceCall, timePeriod, reading,
                    index, syncReplyIssue, combinedReadingTypes, scheduleStrategy);
            if (isOk) {
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

    @TransactionRequired
    public ServiceCall createMasterDataLinkageConfigMasterServiceCall(MasterDataLinkageConfigRequestMessageType config,
                                                                      Optional<EndPointConfiguration> endPointConfiguration, MasterDataLinkageAction action,
                                                                      MasterDataLinkageFaultMessageFactory faultMessageFactory) throws ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.MASTER_DATA_LINKAGE_CONFIG);
        MasterDataLinkageConfigMasterDomainExtension domainExtension = new MasterDataLinkageConfigMasterDomainExtension();
        domainExtension.setActualNumberOfSuccessfulCalls(BigDecimal.ZERO);
        domainExtension.setActualNumberOfFailedCalls(BigDecimal.ZERO);
        domainExtension.setExpectedNumberOfCalls(getExpectedNumberOfCallsForLinkage(config));
        String correlationId = config.getHeader() == null ? null : config.getHeader().getCorrelationID();
        domainExtension.setCorrelationId(correlationId);

        if (endPointConfiguration.isPresent()) {
            domainExtension.setCallbackURL(endPointConfiguration.get().getUrl());
        }
        ServiceCallBuilder serviceCallBuilder = serviceCallType.newServiceCall()
                .origin("MultiSense").extendedWith(domainExtension);
        ServiceCall parentServiceCall = serviceCallBuilder.create();
        final List<EndDevice> endDevices = config.getPayload().getMasterDataLinkageConfig().getEndDevice();
        final List<UsagePoint> usagePoints = config.getPayload().getMasterDataLinkageConfig().getUsagePoint();
        final List<ch.iec.tc57._2011.masterdatalinkageconfig.Meter> meters = config.getPayload()
                .getMasterDataLinkageConfig().getMeter();
        final ConfigurationEvent configurationEvent = config.getPayload().getMasterDataLinkageConfig().getConfigurationEvent();
        if (endDevices.isEmpty() && usagePoints.isEmpty()) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(action,
                    MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT, thesaurus.getFormat(TranslationKeys.END_DEVICE_OR_USAGE_POINT).format());
        }
        if (meters.isEmpty()) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(action,
                    MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT, thesaurus.getFormat(TranslationKeys.METER).format());
        }
        if (!usagePoints.isEmpty() && usagePoints.size() != meters.size()) {
            throw faultMessageFactory.createMasterDataLinkageFaultMessage(action,
                    MessageSeeds.DIFFERENT_NUMBER_OF_METERS_AND_USAGE_POINTS, meters.size(), usagePoints.size());
        }
        for (int i = 0; i < usagePoints.size(); i++) {
            createMasterDataLinkageChildServiceCall(parentServiceCall, action, null, usagePoints.get(i), meters.get(i),
                    configurationEvent);
        }
        for (int i = 0; i < endDevices.size(); i++) {
            createMasterDataLinkageChildServiceCall(parentServiceCall, action, endDevices.get(i), null, meters.get(0),
                    configurationEvent);
        }
        return parentServiceCall;
    }

    private ServiceCall createMasterDataLinkageChildServiceCall(ServiceCall parentServiceCall,
                                                                MasterDataLinkageAction action,
                                                                EndDevice endDevice,
                                                                UsagePoint usagePoint,
                                                                ch.iec.tc57._2011.masterdatalinkageconfig.Meter meter,
                                                                ConfigurationEvent configurationEvent) {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.DATA_LINKAGE_CONFIG);
        MasterDataLinkageConfigDomainExtension domainExtension = new MasterDataLinkageConfigDomainExtension();
        domainExtension.setParentServiceCallId(BigDecimal.valueOf(parentServiceCall.getId()));
        domainExtension.setMeter(jsonService.serialize(new com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.MeterInfo(meter)));
        if (endDevice != null) {
            domainExtension.setEndDevice(jsonService.serialize(new EndDeviceInfo(endDevice)));
        } else if (usagePoint != null) {
            domainExtension.setUsagePoint(jsonService.serialize(new UsagePointInfo(usagePoint)));
            domainExtension.setConfigurationEvent(jsonService.serialize(new ConfigEventInfo(configurationEvent)));
        }
        domainExtension.setOperation(action.name());
        ServiceCallBuilder serviceCallBuilder = parentServiceCall.newChildCall(serviceCallType)
                .extendedWith(domainExtension);
        return serviceCallBuilder.create();
    }

    private BigDecimal getExpectedNumberOfCallsForLinkage(MasterDataLinkageConfigRequestMessageType config) {
        return BigDecimal.valueOf(config.getPayload().getMasterDataLinkageConfig().getUsagePoint().size()
                + config.getPayload().getMasterDataLinkageConfig().getEndDevice().size());
    }

    private boolean processSubParentServiceCallWithChildren(com.elster.jupiter.metering.Meter meter,
                                                            ServiceCall parentServiceCall,
                                                            DateTimeInterval timePeriod, Reading reading,
                                                            int index, SyncReplyIssue syncReplyIssue,
                                                            Set<ReadingType> combinedReadingTypes,
                                                            ScheduleStrategy scheduleStrategy) throws ch.iec.tc57._2011.getmeterreadings.FaultMessage {
        boolean meterReadingRunning = false;
        Instant now = clock.instant();
        Instant start = null;
        Instant end = null;
        if (timePeriod != null) {
            start = timePeriod.getStart();
            end = timePeriod.getEnd();
        }
        Instant actualEnd = getActualEnd(end, now);
        Device device = findDeviceForEndDevice(meter);
        ServiceCall subParentServiceCall = createSubParentServiceCall(parentServiceCall, meter);

        Set<String> existedLoadProfiles = syncReplyIssue.getReadingExistedLoadProfilesMap().get(index);
        if (start != null && end != null) {
            if (CollectionUtils.isNotEmpty(existedLoadProfiles)
                    || (CollectionUtils.isNotEmpty(syncReplyIssue.getExistedReadingTypes()))) {
                processLoadProfiles(subParentServiceCall, device, index, syncReplyIssue, start, end, now,
                        InboundSoapEndpointsActivator.actualRecurrentTaskReadOutDelay, scheduleStrategy, reading.getSource(), meter);
            }
        } else if (CollectionUtils.isNotEmpty(existedLoadProfiles)) {
            Set<ReadingType> readingTypes = masterDataService.findAllLoadProfileTypes().stream()
                    .filter(lp -> existedLoadProfiles.contains(lp.getName()))
                    .map(LoadProfileType::getChannelTypes)
                    .flatMap(Collection::stream)
                    .map(channelType -> channelType.getReadingType())
                    .collect(Collectors.toSet());
            syncReplyIssue.addExistedReadingTypes(readingTypes);
            combinedReadingTypes.addAll(readingTypes);
        }

        if (isMeterReadingRequired(reading.getSource(), meter, combinedReadingTypes, actualEnd, now, InboundSoapEndpointsActivator.actualRecurrentTaskReadOutDelay)) {
            Set<ComTaskExecution> existedComTaskExecutions = getComTaskExecutions(meter, start, end, combinedReadingTypes, syncReplyIssue);
            for (ComTaskExecution comTaskExecution : existedComTaskExecutions) {
                if (start != null && actualEnd.isBefore(start)) {
                    throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                            MessageSeeds.INVALID_OR_EMPTY_TIME_PERIOD,
                            XsdDateTimeConverter.marshalDateTime(start),
                            XsdDateTimeConverter.marshalDateTime(actualEnd)).get();
                }
                Instant trigger = getTriggerDate(actualEnd, InboundSoapEndpointsActivator.actualRecurrentTaskReadOutDelay, comTaskExecution, scheduleStrategy);

                // run now
                if (scheduleStrategy == ScheduleStrategy.RUN_NOW) {
                    if (start == null && end == null) {
                        processComTaskExecutionByRecurrentTask(subParentServiceCall, comTaskExecution, trigger,
                                start, actualEnd, ServiceCallTypes.COMTASK_EXECUTION_GET_METER_READINGS);
                    } else if (start != null && end == null) { // shift the 'next reading block start' to the 'time period start'
                        updateLoadProfileNextRedingBlockStart(syncReplyIssue.getExistedReadingTypes(), device, start);
                        processComTaskExecutionByRecurrentTask(subParentServiceCall, comTaskExecution, trigger,
                                start, actualEnd, ServiceCallTypes.COMTASK_EXECUTION_GET_METER_READINGS);
                    } else if (!trigger.isAfter(now)) {
                        scheduleOrRunNowComTaskExecution(subParentServiceCall, comTaskExecution, trigger,
                                start, actualEnd, ServiceCallTypes.COMTASK_EXECUTION_GET_METER_READINGS, true);
                    } else if (trigger.isAfter(now)) {
                        processComTaskExecutionByRecurrentTask(subParentServiceCall, comTaskExecution, trigger,
                                start, actualEnd, ServiceCallTypes.COMTASK_EXECUTION_GET_METER_READINGS);
                    }
                } else { // use schedule
                    scheduleOrRunNowComTaskExecution(subParentServiceCall, comTaskExecution, trigger,
                            start, actualEnd, ServiceCallTypes.COMTASK_EXECUTION_GET_METER_READINGS, false);
                    // wait next task execution
                }
            }
        }
        if (!subParentServiceCall.findChildren().paged(0, 0).find().isEmpty()) {
            subParentServiceCall.requestTransition(DefaultState.WAITING);
            meterReadingRunning = true;
        } else if (isMeterReadingRequired(reading.getSource(), meter, combinedReadingTypes, actualEnd, now, InboundSoapEndpointsActivator.actualRecurrentTaskReadOutDelay)) {
            subParentServiceCall.requestTransition(DefaultState.FAILED);
            subParentServiceCall.log(LogLevel.SEVERE, "No child service calls have been created.");
        }
        return meterReadingRunning;
    }

    private void processLoadProfiles(ServiceCall subParentServiceCall, Device device, int index, SyncReplyIssue syncReplyIssue,
                                     Instant start, Instant end, Instant now, int delay, ScheduleStrategy scheduleStrategy, String source, com.elster.jupiter.metering.Meter meter) {
        Set<LoadProfile> loadProfiles = getExistedOnDeviceLoadProfiles(device, index, syncReplyIssue);
        Set<ReadingType> readingTypes = getExistedOnDeviceReadingTypes(device, syncReplyIssue);
        loadProfiles.addAll(getLoadProfilesForReadingTypes(device, readingTypes));

        if (CollectionUtils.isNotEmpty(loadProfiles)) {
            Set<ReadingType> lpReadingTypes = masterDataService.findAllLoadProfileTypes().stream()
                    .filter(lp -> loadProfiles.stream().map(LoadProfile::getLoadProfileTypeId).collect(Collectors.toSet()).contains(lp.getId()))
                    .map(LoadProfileType::getChannelTypes)
                    .flatMap(Collection::stream)
                    .map(channelType -> channelType.getReadingType())
                    .collect(Collectors.toSet());


            if (isMeterReadingRequired(source, meter, lpReadingTypes, end, now, delay)) {
                ComTaskExecution deviceMessagesComTaskExecution = syncReplyIssue.getDeviceMessagesComTaskExecutionMap()
                        .get(device.getId());
                if (deviceMessagesComTaskExecution == null) {
                    syncReplyIssue.addErrorType(syncReplyIssue.getReplyTypeFactory()
                            .errorType(MessageSeeds.NO_COM_TASK_EXECUTION_FOR_LOAD_PROFILES, null,
                                    device.getName()));
                    return;
                }
                Instant trigger = getTriggerDate(end, delay, deviceMessagesComTaskExecution, scheduleStrategy);
                loadProfiles.forEach(loadProfile -> {
                    if (loadProfile.getLastReading() == null || start.isBefore(loadProfile.getLastReading().toInstant()))  {
                        device.getLoadProfileUpdaterFor(loadProfile).setLastReading(start).update();
                    }
                    ServiceCall childServiceCall = createChildGetMeterReadingServiceCall(subParentServiceCall,
                            ServiceCallTypes.DEVICE_MESSAGE_GET_METER_READINGS, deviceMessagesComTaskExecution, trigger, start, end);
                    DeviceMessage deviceMessage = createDeviceMessage(device, childServiceCall, loadProfile, trigger, start, end);
                    childServiceCall.log(LogLevel.FINE, String.format("Device message '%s'(id: %d, release date: %s) is linked to service call",
                            deviceMessage.getSpecification().getName(), deviceMessage.getId(), trigger));
                    if (scheduleStrategy == ScheduleStrategy.RUN_NOW) {
                        if (trigger.isAfter(now)) { // use recurrent task
                            childServiceCall.requestTransition(DefaultState.SCHEDULED);
                        } else { // run now
                            childServiceCall.requestTransition(DefaultState.PENDING);
                            childServiceCall.requestTransition(DefaultState.ONGOING);
                            childServiceCall.requestTransition(DefaultState.WAITING);
                            // avoid OptimisticLockException
                            communicationTaskService.findComTaskExecution(deviceMessagesComTaskExecution.getId()).ifPresent(ComTaskExecution::runNow);
                        }
                    } else { // use schedule
                        childServiceCall.requestTransition(DefaultState.PENDING);
                        childServiceCall.requestTransition(DefaultState.ONGOING);
                        childServiceCall.requestTransition(DefaultState.WAITING);
                    }
                });
            }
        }
    }

    private Set<ComTaskExecution> getComTaskExecutions(com.elster.jupiter.metering.Meter meter, Instant start, Instant end,
                                                       Set<ReadingType> combinedReadingTypes, SyncReplyIssue syncReplyIssue) {
        Set<ComTaskExecution> existedComTaskExecutions = new HashSet<>();
        if (comTaskExecutionRequired(start, end, combinedReadingTypes, true)) {
            fillComTaskExecutions(existedComTaskExecutions, meter, combinedReadingTypes, syncReplyIssue, true);
        }
        if (comTaskExecutionRequired(start, end, combinedReadingTypes, false)) {
            fillComTaskExecutions(existedComTaskExecutions, meter, combinedReadingTypes, syncReplyIssue, false);
        }
        return existedComTaskExecutions;
    }

    private void fillComTaskExecutions(Set<ComTaskExecution> existedComTaskExecutions, com.elster.jupiter.metering.Meter meter,
                                       Set<ReadingType> combinedReadingTypes, SyncReplyIssue syncReplyIssue, boolean isRegular) {
        Set<ComTaskExecution> comTaskExecution;
        if (isRegular) {
            comTaskExecution = syncReplyIssue.getDeviceRegularComTaskExecutionMap().get(Long.parseLong(meter.getAmrId()));
        } else {
            comTaskExecution = syncReplyIssue.getDeviceIrregularComTaskExecutionMap().get(Long.parseLong(meter.getAmrId()));
        }

        if (comTaskExecution != null) {
            existedComTaskExecutions.addAll(comTaskExecution);
        }
    }

    private boolean comTaskExecutionRequired(Instant start, Instant end, Set<ReadingType> combinedReadingTypes, boolean isRegular) {
        if (combinedReadingTypes.stream()
                .anyMatch(readingType -> readingType.isRegular() == isRegular)) {
            if (isRegular) {
                return end == null;
            } else {
                return start != null;
            }
        }
        return false;
    }

    private Instant getTriggerDate(Instant actualEnd, int delay, ComTaskExecution comTaskExecution,
                                   ScheduleStrategy scheduleStrategy) {
        Instant trigger = actualEnd.plus(delay, ChronoUnit.MINUTES);
        Instant next = comTaskExecution.getNextExecutionTimestamp();
        if (scheduleStrategy == ScheduleStrategy.USE_SCHEDULE && next != null) {
            if (next.isBefore(actualEnd)) {
                trigger = actualEnd;
            } else {
                trigger = next;
            }
        }
        return trigger;
    }

    private void updateLoadProfileNextRedingBlockStart(Set<ReadingType> readingTypes, Device device, Instant start) {
        device.getLoadProfiles().stream()
                .filter(loadProfile -> loadProfile.getChannels().stream()
                        .anyMatch(channel -> readingTypes.contains(channel.getReadingType()))
                )
                .forEach(loadProfile -> device.getLoadProfileUpdaterFor(loadProfile).setLastReading(start).update());
    }

    private Instant getActualEnd(Instant end, Instant now) {
        if (end == null) {
            return now;
        }
        return end;
    }

    private void processComTaskExecutionByRecurrentTask(ServiceCall subParentServiceCall,
                                                        ComTaskExecution comTaskExecution, Instant trigger,
                                                        Instant actualStart, Instant actualEnd,
                                                        ServiceCallTypes serviceCallTypes) {
        ServiceCall childServiceCall = createChildGetMeterReadingServiceCall(subParentServiceCall,
                serviceCallTypes, comTaskExecution, trigger, actualStart, actualEnd);
        // recurrent task will check childServiceCall in state SCHEDULED
        childServiceCall.requestTransition(DefaultState.SCHEDULED);
    }

    private void scheduleOrRunNowComTaskExecution(ServiceCall subParentServiceCall,
                                                  ComTaskExecution comTaskExecution, Instant trigger,
                                                  Instant actualStart, Instant actualEnd,
                                                  ServiceCallTypes serviceCallTypes, boolean runNow) {
        ServiceCall childServiceCall = createChildGetMeterReadingServiceCall(subParentServiceCall,
                serviceCallTypes, comTaskExecution, trigger, actualStart, actualEnd);
        childServiceCall.requestTransition(DefaultState.PENDING);
        childServiceCall.requestTransition(DefaultState.ONGOING);
        childServiceCall.requestTransition(DefaultState.WAITING);
        if (runNow) {
            // avoid OptimisticLockException
            communicationTaskService.findComTaskExecution(comTaskExecution.getId()).ifPresent(ComTaskExecution::runNow);
        }
    }

    private ServiceCall createSubParentServiceCall(ServiceCall parent, com.elster.jupiter.metering.Meter meter) {
        ServiceCallType serviceCallType = getServiceCallType(ServiceCallTypes.SUBPARENT_GET_METER_READINGS);
        SubParentGetMeterReadingsDomainExtension subParentDomainExtension = new SubParentGetMeterReadingsDomainExtension();
        subParentDomainExtension.setEndDeviceName(meter.getName());
        subParentDomainExtension.setEndDeviceMrid(meter.getMRID());

        ServiceCall subParentServiceCall = parent.newChildCall(serviceCallType).extendedWith(subParentDomainExtension)
                .create();
        subParentServiceCall.requestTransition(DefaultState.PENDING);
        subParentServiceCall.requestTransition(DefaultState.ONGOING);
        return subParentServiceCall;
    }

    private ServiceCall createChildGetMeterReadingServiceCall(ServiceCall subParentServiceCall, ServiceCallTypes childServiceCallType,
                                                              ComTaskExecution comTaskExecution, Instant triggerDate, Instant actualStart,
                                                              Instant actualEnd) {
        ServiceCallType serviceCallType = getServiceCallType(childServiceCallType);

        ChildGetMeterReadingsDomainExtension childDomainExtension = new ChildGetMeterReadingsDomainExtension();
        childDomainExtension.setCommunicationTask(comTaskExecution.getComTask().getName());
        childDomainExtension.setTriggerDate(triggerDate);
        childDomainExtension.setActualStartDate(actualStart);
        childDomainExtension.setActualEndDate(actualEnd);

        return subParentServiceCall.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension)
                .targetObject(comTaskExecution.getDevice())
                .create();
    }

    private Device findDeviceForEndDevice(com.elster.jupiter.metering.Meter meter) {
        long deviceId = Long.parseLong(meter.getAmrId());
        return deviceService.findAndLockDeviceById(deviceId).orElseThrow(NoSuchElementException.deviceWithIdNotFound(thesaurus, deviceId));
    }

    private void initiateReading(ServiceCall serviceCall) {
        serviceCall.requestTransition(DefaultState.PAUSED);
        serviceCall.requestTransition(DefaultState.ONGOING);
    }

    private Set<LoadProfile> getExistedOnDeviceLoadProfiles(Device device, int index, SyncReplyIssue syncReplyIssue) {
        Set<LoadProfile> existedOnDeviceLoadProfiles = new HashSet<>();
        Set<String> notFoundNames = new HashSet<>();
        Set<String> loadProfilesNames = syncReplyIssue.getReadingExistedLoadProfilesMap().get(index);
        if (loadProfilesNames != null) {
            Map<String, LoadProfile> allDeviceLoadProfileNames = device.getLoadProfiles().stream()
                    .collect(Collectors.toMap(lp -> lp.getLoadProfileSpec()
                            .getLoadProfileType()
                            .getName(), lp -> lp, (a, b) -> a));
            loadProfilesNames.forEach(lpName -> {
                LoadProfile loadProfile = allDeviceLoadProfileNames.get(lpName);
                if (loadProfile != null) {
                    existedOnDeviceLoadProfiles.add(loadProfile);
                } else {
                    notFoundNames.add(lpName);
                }
            });
            if (!notFoundNames.isEmpty()) {
                syncReplyIssue.addNotFoundOnDeviceLoadProfiles(device.getName(), notFoundNames);
            }
        }
        return existedOnDeviceLoadProfiles;
    }

    private Set<LoadProfile> getLoadProfilesForReadingTypes(Device device, Set<ReadingType> readingTypes) {
        return device.getLoadProfiles().stream()
                .filter(lp -> lp.getLoadProfileSpec().getChannelSpecs().stream()
                        .anyMatch(c -> readingTypes.contains(c.getReadingType())
                                || (c.getReadingType().getCalculatedReadingType().isPresent()
                                && readingTypes.contains(c.getReadingType().getCalculatedReadingType().get())))
                )
                .collect(Collectors.toSet());
    }

    private Set<ReadingType> getExistedOnDeviceReadingTypes(Device device, SyncReplyIssue syncReplyIssue) {
        Set<String> notFoundReadingTypesMrids = syncReplyIssue.getNotFoundReadingTypesOnDevices().get(device.getName());
        if (CollectionUtils.isEmpty(notFoundReadingTypesMrids)) {
            return syncReplyIssue.getExistedReadingTypes();
        }
        return syncReplyIssue.getExistedReadingTypes().stream()
                .filter(rt -> !notFoundReadingTypesMrids.contains(rt.getMRID()))
                .collect(Collectors.toSet());
    }

    private DeviceMessage createDeviceMessage(Device device, ServiceCall childServiceCall, LoadProfile loadProfile, Instant releaseDate,
                                              Instant start, Instant end) {
        Device.DeviceMessageBuilder deviceMessageBuilder = device.newDeviceMessage(DeviceMessageId.LOAD_PROFILE_PARTIAL_REQUEST)
                .setTrackingId(Long.toString(childServiceCall.getId()))
                .setReleaseDate(releaseDate);

        deviceMessageBuilder.addProperty("load profile", loadProfile);
        deviceMessageBuilder.addProperty("from", Date.from(start));
        deviceMessageBuilder.addProperty("to", Date.from(end));
        return deviceMessageBuilder.add();
    }

    private boolean isMeterReadingRequired(String source, com.elster.jupiter.metering.Meter meter,
                                           Set<ReadingType> readingTypes, Instant endTime, Instant now, int delay) {
        if (ReadingSourceEnum.METER.getSource().equals(source)) {
            return true;
        }
        if (ReadingSourceEnum.HYBRID.getSource().equals(source)) {
            boolean inFutureReading = endTime.plus(delay, ChronoUnit.MINUTES).isAfter(now);
            return inFutureReading || meter.getChannelsContainers().stream()
                    .anyMatch(container -> isChannelContainerReadOutRequired(container, readingTypes, endTime));
        }
        return false;
    }

    private boolean isChannelContainerReadOutRequired(ChannelsContainer channelsContainer, Set<ReadingType> readingTypes,
                                                      Instant endTime) {
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
                if (readingTypes.contains(readingType)
                        || (readingType.getCalculatedReadingType().isPresent()
                        && readingTypes.contains(readingType.getCalculatedReadingType().get()))) {
                    Instant instant = channel.getLastDateTime();
                    if (instant == null || instant.isBefore(endInstant)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String getReadingTypesString(Set<ReadingType> existedReadingTypes) {
        return existedReadingTypes == null ? null : existedReadingTypes.stream()
                .map(ert -> ert.getMRID())
                .collect(Collectors.joining(";"));
    }

    private String getSemicolonSeparatedStringFromSet(Set<String> strings) {
        return strings == null ? null : strings.stream()
                .collect(Collectors.joining(";"));
    }

    private ScheduleStrategy getScheduleStrategy(String scheduleStrategy) throws
            ch.iec.tc57._2011.getmeterreadings.FaultMessage {
        ScheduleStrategy strategy = ScheduleStrategy.RUN_NOW;
        if (scheduleStrategy != null) {
            ScheduleStrategy scheduleStrategyEnum = ScheduleStrategy.getByName(scheduleStrategy);
            if (scheduleStrategyEnum != null) {
                return scheduleStrategyEnum;
            }
            throw faultMessageFactory.createMeterReadingFaultMessageSupplier(
                    MessageSeeds.SCHEDULE_STRATEGY_NOT_SUPPORTED,
                    scheduleStrategy).get();
        }
        return strategy;
    }
}
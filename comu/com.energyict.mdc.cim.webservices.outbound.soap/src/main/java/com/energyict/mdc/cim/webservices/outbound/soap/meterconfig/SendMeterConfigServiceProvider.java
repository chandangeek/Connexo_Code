/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.cim.webservices.outbound.soap.meterconfig;

import com.elster.jupiter.fsm.StateTransitionWebServiceClient;
import com.elster.jupiter.issue.share.IssueWebServiceClient;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.BulkWebServiceCallResult;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.energyict.mdc.cim.webservices.outbound.soap.FailedMeterOperation;
import com.energyict.mdc.cim.webservices.outbound.soap.MeterConfigExtendedDataFactory;
import com.energyict.mdc.cim.webservices.outbound.soap.MeterConfigFactory;
import com.energyict.mdc.cim.webservices.outbound.soap.OperationEnum;
import com.energyict.mdc.cim.webservices.outbound.soap.PingResult;
import com.energyict.mdc.cim.webservices.outbound.soap.SendMeterConfigWebService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigEventMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigPayloadType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigResponseMessageType;
import ch.iec.tc57._2011.sendmeterconfig.MeterConfigPort;
import ch.iec.tc57._2011.sendmeterconfig.SendMeterConfig;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.Name;
import ch.iec.tc57._2011.schema.message.ObjectType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.xml.ws.Service;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.sendmeterconfig.provider",
        service = {IssueWebServiceClient.class, SendMeterConfigWebService.class, OutboundSoapEndPointProvider.class, StateTransitionWebServiceClient.class},
        immediate = true,
        property = {"name=" + SendMeterConfigWebService.NAME})
public class SendMeterConfigServiceProvider extends AbstractOutboundEndPointProvider<MeterConfigPort>
        implements IssueWebServiceClient, SendMeterConfigWebService, OutboundSoapEndPointProvider, ApplicationSpecific, StateTransitionWebServiceClient {
    private static final String NOUN = "MeterConfig";
    private static final String RESOURCE_WSDL = "/wsdl/meterconfig/SendMeterConfig.wsdl";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.meterconfigmessage.ObjectFactory meterConfigMessageObjectFactory = new ch.iec.tc57._2011.meterconfigmessage.ObjectFactory();
    private final List<MeterConfigExtendedDataFactory> meterConfigExtendedDataFactories = new CopyOnWriteArrayList<>();
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private volatile MeterConfigFactory meterConfigFactory;
    private volatile DeviceService deviceService;

    public SendMeterConfigServiceProvider() {
        // for OSGi purposes
    }

    @Inject
    public SendMeterConfigServiceProvider(DeviceService deviceService,
                                          MeterConfigFactory meterConfigFactory,
                                          WebServicesService webServicesService) {
        // for tests
        this();
        setDeviceService(deviceService);
        setMeterConfigFactory(meterConfigFactory);
        setWebServicesService(webServicesService);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterConfigPort(MeterConfigPort meterConfigPort, Map<String, Object> properties) {
        super.doAddEndpoint(meterConfigPort, properties);
    }

    public void removeMeterConfigPort(MeterConfigPort meterConfigPort) {
        super.doRemoveEndpoint(meterConfigPort);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterConfigExtendedDataFactory(MeterConfigExtendedDataFactory meterConfigExtendedDataFactory) {
        meterConfigExtendedDataFactories.add(meterConfigExtendedDataFactory);
    }

    public void removeMeterConfigExtendedDataFactory(MeterConfigExtendedDataFactory meterConfigExtendedDataFactory) {
        meterConfigExtendedDataFactories.remove(meterConfigExtendedDataFactory);
    }

    public List<MeterConfigExtendedDataFactory> getMeterConfigExtendedDataFactories() {
        return Collections.unmodifiableList(meterConfigExtendedDataFactories);
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setMeterConfigFactory(MeterConfigFactory meterConfigFactory) {
        this.meterConfigFactory = meterConfigFactory;
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService) {
        // Just to inject WebServicesService
    }

    @Override
    public Service get() {
        return new SendMeterConfig(this.getClass().getResource(RESOURCE_WSDL));
    }

    @Override
    public Class<MeterConfigPort> getService() {
        return MeterConfigPort.class;
    }

    @Override
    public String getWebServiceName() {
        return getName();
    }

    @Override
    protected String getName() {
        return SendMeterConfigWebService.NAME;
    }

    /**
     * StateTransitionWebServiceClient implementation
     */
    @Override
    public void call(long id, Set<EndPointConfiguration> endPointConfigurations, String state, Instant effectiveDate) {
        Optional<Device> device = deviceService.findDeviceByMeterId(id);
        if (device.isPresent()) {
            logger.info("Handling notifications for SendMeterConfig called by StateTransition for device " + device.get().getSerialNumber() + " and state=" + state);
            if (!triggerCall(device.get(), endPointConfigurations, effectiveDate)) {
                logger.warning("Failed to send MeterConfig notification about a state transition to some of the configured web service endpoints.");
            }
        } else {
            logger.warning("Couldn't send MeterConfig notification about a state transition: device with meter-id=" + id + " wasn't found!");
        }
    }

    @Override
    public boolean call(Issue issue, EndPointConfiguration endPointConfiguration) {
        return deviceService.findDeviceById(Long.parseLong(issue.getDevice().getAmrId()))
                .map(device -> triggerCall(device, Collections.singleton(endPointConfiguration), issue.getCreateDateTime()))
                .orElse(true);
    }

    private boolean triggerCall(Device device, Collection<EndPointConfiguration> endPointConfigurations, Instant effectiveDate) {
        MeterConfigEventMessageType message = createInfoResponseMessage(createMeterConfig(Collections.singletonList(device)), HeaderType.Verb.CHANGED, null);
        logger.info("Calling changedMeterConfig on endpoints: "
                + endPointConfigurations.stream().map(EndPointConfiguration::getName).collect(Collectors.joining(", ", "[", "]")));
        SetMultimap<String, String> attributes = ImmutableSetMultimap.of(
                CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), device.getName(),
                CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), device.getmRID(),
                CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), device.getSerialNumber()
        );
        BulkWebServiceCallResult result = using("changedMeterConfig")
                .toEndpoints(endPointConfigurations)
                .withRelatedAttributes(attributes)
                .send(message);
        logger.info("Call was sent, checking reply message");
        return result.isSuccessful() // make sure all the notifications are sent and answered
                && result.getOccurrencesWithResponses().entrySet().stream()
                .map(callAndResponse -> checkReplyMessage(callAndResponse.getKey(), (MeterConfigResponseMessageType) callAndResponse.getValue()))
                .reduce(Boolean::logicalAnd) // make sure all the notifications are successful
                .orElse(true); // no endpoint to send the notification, so positively relax
    }

    private boolean checkReplyMessage(WebServiceCallOccurrence webServiceCallOccurrence, MeterConfigResponseMessageType replyMessage) {
        if (replyMessage == null) {
            webServiceCallOccurrence.log(LogLevel.FINE, "Empty response received");
            return true;
        }

        if (replyMessage.getReply() == null) {
            webServiceCallOccurrence.log(LogLevel.FINE, "Empty reply information received");
            return true;
        }

        if (replyMessage.getReply().getResult() == null) {
            webServiceCallOccurrence.log(LogLevel.FINE, "Empty reply result received");
            return true;
        }

        switch (replyMessage.getReply().getResult()) {
            case OK:
                webServiceCallOccurrence.log(LogLevel.INFO, "Response status is OK");
                return true;
            case PARTIAL:
            case FAILED:
            default:
                replyMessage.getReply().getError().forEach(error ->
                        webServiceCallOccurrence.log(convertLevel(error.getLevel()), "Received error: " + error.getCode() + ' ' + error.getDetails()));
                webServiceCallOccurrence.log(LogLevel.SEVERE, "Response status is " + replyMessage.getReply().getResult().name());
                return false;
        }
    }

    private LogLevel convertLevel(ErrorType.Level level) {
        if (level == null) {
            return LogLevel.SEVERE;
        }
        switch (level) {
            case INFORM:
                return LogLevel.INFO;
            case WARNING:
                return LogLevel.WARNING;
            case FATAL:
            case CATASTROPHIC:
            default:
                return LogLevel.SEVERE;
        }
    }

    @Override
    public void call(EndPointConfiguration endPointConfiguration, OperationEnum operation, Map<Device, PingResult> successfulDevices,
                     List<FailedMeterOperation> failedDevices, List<FailedMeterOperation> devicesWithWarnings, long expectedNumberOfCalls,
                     boolean meterStatusRequired, String correlationId) {
        String method;
        MeterConfigEventMessageType message;
        switch (operation) {
            case CREATE:
                method = "createdMeterConfig";
                message = createStatusResponseMessage(createMeterConfig(successfulDevices.keySet()), failedDevices,
                        devicesWithWarnings, expectedNumberOfCalls, HeaderType.Verb.CREATED, correlationId);
                break;
            case UPDATE:
                method = "changedMeterConfig";
                message = createStatusResponseMessage(createMeterConfig(successfulDevices.keySet()), failedDevices,
                        devicesWithWarnings, expectedNumberOfCalls, HeaderType.Verb.CHANGED, correlationId);
                break;
            case GET:
                method = "replyMeterConfig";
                message = createStatusResponseMessage(getMeterConfig(successfulDevices, meterStatusRequired),
                        failedDevices, devicesWithWarnings, expectedNumberOfCalls, HeaderType.Verb.REPLY, correlationId);
                break;
            case DELETE:
                method = "deletedMeterConfig";
                message = createStatusResponseMessage(getEmptyMeterConfig(), failedDevices, devicesWithWarnings,
                        expectedNumberOfCalls, HeaderType.Verb.DELETED, correlationId);
                break;
            default:
                throw new UnsupportedOperationException(OperationEnum.class.getSimpleName() + '#' + operation.name() + " isn't supported.");
        }

        SetMultimap<String, String> values = HashMultimap.create();

        successfulDevices.keySet().forEach(device -> {
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), device.getName());
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), device.getmRID());
            values.put(CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), device.getSerialNumber());
        });

        failedDevices.forEach(meterOperation -> {
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), meterOperation.getMeterName());
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), meterOperation.getmRID());
        });

        using(method)
                .toEndpoints(endPointConfiguration)
                .withRelatedAttributes(values)
                .send(message);
    }

    private MeterConfig createMeterConfig(Collection<Device> devices) {
        MeterConfig meterConfig = meterConfigFactory.asMeterConfig(devices);
        getMeterConfigExtendedDataFactories()
                .forEach(meterConfigExtendedDataFactory -> meterConfigExtendedDataFactory.extendData(devices, meterConfig));
        return meterConfig;
    }

    private MeterConfig getEmptyMeterConfig() {
        return new MeterConfig();
    }

    private MeterConfig getMeterConfig(Map<Device, PingResult> devicesAndPingResult, boolean isMeterStatusRequired) {
        return meterConfigFactory.asGetMeterConfig(devicesAndPingResult, isMeterStatusRequired);
    }

    private MeterConfigEventMessageType createInfoResponseMessage(MeterConfig meterConfig, HeaderType.Verb verb, String correlationId) {
        MeterConfigEventMessageType meterConfigEventMessageType = meterConfigMessageObjectFactory.createMeterConfigEventMessageType();

        // set header
        addHeader(meterConfigEventMessageType, verb, correlationId);

        // set payload
        MeterConfigPayloadType payloadType = meterConfigMessageObjectFactory.createMeterConfigPayloadType();
        payloadType.setMeterConfig(meterConfig);
        meterConfigEventMessageType.setPayload(payloadType);

        return meterConfigEventMessageType;
    }

    private MeterConfigEventMessageType createStatusResponseMessage(MeterConfig meterConfig, List<FailedMeterOperation> failedDevices, List<FailedMeterOperation> devicesWithWarnings, long expectedNumberOfCalls, HeaderType.Verb verb, String correlationId) {
        MeterConfigEventMessageType meterConfigEventMessageType = meterConfigMessageObjectFactory.createMeterConfigEventMessageType();

        // set header
        addHeader(meterConfigEventMessageType, verb, correlationId);

        // set reply
        ReplyType replyType = cimMessageObjectFactory.createReplyType();
        if (failedDevices.isEmpty()) {
            replyType.setResult(ReplyType.Result.OK);
        } else if (expectedNumberOfCalls == failedDevices.size()) {
            replyType.setResult(ReplyType.Result.FAILED);
        } else {
            replyType.setResult(ReplyType.Result.PARTIAL);
        }

        // set errors
        failedDevices.forEach(failedMeterOperation -> replyType.getError().add(convertToErrorType(failedMeterOperation, ErrorType.Level.FATAL)));

        // set warnings
        devicesWithWarnings.forEach(warningMeterOperation -> replyType.getError().add(convertToErrorType(warningMeterOperation, ErrorType.Level.WARNING)));

        meterConfigEventMessageType.setReply(replyType);

        // set payload
        MeterConfigPayloadType payloadType = meterConfigMessageObjectFactory.createMeterConfigPayloadType();
        payloadType.setMeterConfig(meterConfig);
        meterConfigEventMessageType.setPayload(payloadType);

        return meterConfigEventMessageType;
    }

    private ErrorType convertToErrorType(FailedMeterOperation meterOperation, ErrorType.Level level) {
        ErrorType errorType = new ErrorType();
        errorType.setLevel(level);
        errorType.setCode(meterOperation.getErrorCode());
        errorType.setDetails(meterOperation.getErrorMessage());
        ObjectType objectType = new ObjectType();
        objectType.setMRID(meterOperation.getmRID());
        objectType.setObjectType("EndDevice");
        Name name = new Name();
        name.setName(meterOperation.getMeterName());
        objectType.getName().add(name);
        errorType.setObject(objectType);
        return errorType;
    }

    private void addHeader(MeterConfigEventMessageType message, HeaderType.Verb verb, String correlationId) {
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(verb);
        header.setCorrelationID(correlationId);
        message.setHeader(header);
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}

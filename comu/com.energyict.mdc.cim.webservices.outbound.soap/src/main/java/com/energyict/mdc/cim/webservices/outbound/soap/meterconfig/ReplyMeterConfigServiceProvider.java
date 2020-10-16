/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.meterconfig;

import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigEventMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigPayloadType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigResponseMessageType;
import ch.iec.tc57._2011.replymeterconfig.FaultMessage;
import ch.iec.tc57._2011.replymeterconfig.MeterConfigPort;
import ch.iec.tc57._2011.replymeterconfig.ReplyMeterConfig;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.Name;
import ch.iec.tc57._2011.schema.message.ObjectType;
import ch.iec.tc57._2011.schema.message.ReplyType;

import com.elster.jupiter.fsm.StateTransitionWebServiceClient;
import com.elster.jupiter.issue.share.IssueWebServiceClient;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.energyict.mdc.cim.webservices.outbound.soap.FailedMeterOperation;
import com.energyict.mdc.cim.webservices.outbound.soap.MeterConfigExtendedDataFactory;
import com.energyict.mdc.cim.webservices.outbound.soap.MeterConfigFactory;
import com.energyict.mdc.cim.webservices.outbound.soap.OperationEnum;
import com.energyict.mdc.cim.webservices.outbound.soap.PingResult;
import com.energyict.mdc.cim.webservices.outbound.soap.ReplyMeterConfigWebService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.replymeterconfig.provider",
        service = {IssueWebServiceClient.class, ReplyMeterConfigWebService.class, OutboundSoapEndPointProvider.class, StateTransitionWebServiceClient.class},
        immediate = true,
        property = {"name=" + ReplyMeterConfigWebService.NAME})
public class ReplyMeterConfigServiceProvider extends AbstractOutboundEndPointProvider<MeterConfigPort> implements IssueWebServiceClient, ReplyMeterConfigWebService, OutboundSoapEndPointProvider, ApplicationSpecific, StateTransitionWebServiceClient{

    private static final String NOUN = "MeterConfig";
    private static final String RESOURCE_WSDL = "/wsdl/meterconfig/ReplyMeterConfig.wsdl";
    private final Map<String, MeterConfigPort> meterConfigPorts = new ConcurrentHashMap<>();

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.meterconfigmessage.ObjectFactory meterConfigMessageObjectFactory = new ch.iec.tc57._2011.meterconfigmessage.ObjectFactory();
    private final List<MeterConfigExtendedDataFactory> meterConfigExtendedDataFactories = new ArrayList<>();

    private volatile MeterConfigFactory meterConfigFactory;
    private volatile DeviceService deviceService;
    private volatile WebServicesService webServicesService;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile MeteringService meteringService;

    private Logger logger = Logger.getLogger(this.getClass().getName());

    public ReplyMeterConfigServiceProvider() {
        // for OSGi purposes
    }

    @Inject
    public ReplyMeterConfigServiceProvider(DeviceService deviceService,
                                           WebServicesService webServicesService,
                                           EndPointConfigurationService endPointConfigurationService,
                                           MeteringService  meteringService) {
        this();
        setDeviceService(deviceService);
        setMeterConfigFactory(meterConfigFactory);
        setWebServicesService(webServicesService);
        setEndPointConfigurationService(endPointConfigurationService);
        setMeteringService(meteringService);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterConfigPort(MeterConfigPort meterConfigPort, Map<String, Object> properties) {
        super.doAddEndpoint(meterConfigPort, properties);
    }

    public void removeMeterConfigPort(MeterConfigPort meterConfigPort) {
        super.doRemoveEndpoint(meterConfigPort);
    }

    @Reference
    public void addWebServicesService(WebServicesService webServicesService) {
        // Just to inject WebServicesService
    }

    public Map<String, MeterConfigPort> getMeterConfigPorts() {
        return Collections.unmodifiableMap(meterConfigPorts);
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService) {
        this.webServicesService = webServicesService;
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
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public Service get() {
        return new ReplyMeterConfig(this.getClass().getResource(RESOURCE_WSDL));
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
        return ReplyMeterConfigWebService.NAME;
    }
    private List<EndPointConfiguration> getEndPointConfigurationByIds(List<Long> endPointConfigurationIds) {
        return endPointConfigurationIds.stream()
                .map(id -> endPointConfigurationService.getEndPointConfiguration(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /** StateTransitionWebServiceClient implementation */
    @Override
    public void call(long id, List<Long> endPointConfigurationIds, String state, Instant effectiveDate) {
        Optional<Device> device = deviceService.findDeviceByMeterId(id);
        if (device.isPresent()){
            logger.info("Handling notifications for ReplyMeterConfig called by StateTransition for device "+device.get().getSerialNumber()+" and state="+state);

            getEndPointConfigurationByIds(endPointConfigurationIds)
                    .stream()
                    .filter(EndPointConfiguration::isActive)
                    .forEach(endPointConfiguration ->
                            callStateTransition(device.get(), endPointConfiguration, state, effectiveDate)
                    );
        } else {
            logger.warning("Could not call ReplyMeterConfig due to state-transition: device with meter-id="+id+" was not found!");
        }
    }

    private void callStateTransition(Device device, EndPointConfiguration endPointConfiguration, String state, Instant effectiveDate) {
        publish(endPointConfiguration);
        triggerCall(device, endPointConfiguration, false);
    }

    @Override
    public boolean call(Issue issue, EndPointConfiguration endPointConfiguration) {
        publish(endPointConfiguration);
        deviceService.findDeviceById(Long.parseLong(issue.getDevice().getAmrId()))
                .ifPresent(device -> triggerCall(device, endPointConfiguration, true));
        return true;
    }

    private void publish(EndPointConfiguration endPointConfiguration) {
        if (endPointConfiguration.isActive() && !webServicesService.isPublished(endPointConfiguration)) {
            webServicesService.publishEndPoint(endPointConfiguration);
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

    private void triggerCall(Device device, EndPointConfiguration endPointConfiguration, boolean isReply) {
        try {
            Optional.ofNullable(getMeterConfigPorts().get(endPointConfiguration.getUrl()))
                    .ifPresent(meterConfigPortService -> {
                        MeterConfigPort replyMeterConfigPort = setReplyAddress(meterConfigPortService, endPointConfiguration.getUrl());
                        logger.info("Calling changed(MeterConfig) on "+
                                ((BindingProvider)replyMeterConfigPort).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));
                        try {
                            MeterConfigEventMessageType responseMessage = createInfoResponseMessage(createMeterConfig(Collections
                                    .singletonList(device)), HeaderType.Verb.CHANGED,null);

                            // we're not replying to a request, it's a pushed state-change notification
                            if (!isReply){
                                responseMessage.setReply(null);
                            }

                            MeterConfigResponseMessageType replyMessage = replyMeterConfigPort.changedMeterConfig(responseMessage);
                            logger.info("Call was sent, checking reply message");
                            checkReplyMessage(endPointConfiguration, replyMessage);
                        } catch (FaultMessage faultMessage) {
                            String error = faultMessage.getLocalizedMessage();
                            if (faultMessage.getCause() != null){
                                error += " " + faultMessage.getCause().getLocalizedMessage();
                            }
                            logger.severe(error);
                            endPointConfiguration.log(error, faultMessage);
                        }
                    });
        } catch (RuntimeException ex) {
            String error = ex.getLocalizedMessage();
            if (ex.getCause() != null){
                error += " " + ex.getCause().getLocalizedMessage();
            }
            logger.severe(error);
            endPointConfiguration.log(LogLevel.SEVERE, error); }

    }

    private void checkReplyMessage(EndPointConfiguration endPointConfiguration, MeterConfigResponseMessageType replyMessage) {
        if (replyMessage==null) {
            logger.info("Empty response received");
            endPointConfiguration.log(LogLevel.FINE,"Empty response received");
            return;
        }

        if (replyMessage.getReply() == null){
            logger.info("Empty <Reply> block received");
            endPointConfiguration.log(LogLevel.FINE,"Empty reply information received");
            return;
        }

        if (replyMessage.getReply().getResult()==null){
            logger.info("Empty or un-parsable <Result> block received");
            endPointConfiguration.log(LogLevel.FINE,"Empty reply result received");
            return;

        }

        logger.info("Result is "+replyMessage.getReply().getResult().toString());
        switch (replyMessage.getReply().getResult()){
            case OK:
                endPointConfiguration.log(LogLevel.INFO,"Reply response is OK");
                return;
            case PARTIAL:
            case FAILED:
                String error = extractErrors(replyMessage.getReply().getError());
                String message = "Reply response is " + replyMessage.getReply().getResult().toString()+" "+error;
                logger.severe(message);
                throw new WebServiceException(message);
        }

    }

    private String extractErrors(List<ErrorType> error) {
        StringBuilder message = new StringBuilder();

        if (error!=null) {
            for (ErrorType err : error) {
                message.append(err.getCode()).append(" ");
                message.append(err.getDetails()).append("; ");
            }
        }

        return message.toString();
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

    /**
     * Setting the endpoint address property of the Response-Context.
     * On OpenJDK it was observed that this address is by default the namespace address, not the configured url.
     */
    private MeterConfigPort setReplyAddress(MeterConfigPort meterConfigPort, String urlAddress) {
        try {
            java.net.URL wsdlUrl = new URL(urlAddress);

            BindingProvider bindingProvider = (BindingProvider) meterConfigPort;

            String endPointAddress = (String) bindingProvider.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

            if (urlAddress.equals(endPointAddress)){
                logger.fine("Endpoint validated.");
            } else {
                logger.fine("Setting response URL to: "+ wsdlUrl.toExternalForm());
                bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wsdlUrl.toExternalForm());
            }
        } catch (MalformedURLException e) {
            logger.warning("URL " + urlAddress+" is malformed: "+e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return meterConfigPort;
    }
}

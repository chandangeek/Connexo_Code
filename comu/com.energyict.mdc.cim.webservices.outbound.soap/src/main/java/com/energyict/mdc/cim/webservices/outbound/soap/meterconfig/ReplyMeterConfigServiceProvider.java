/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.meterconfig;

import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigEventMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigPayloadType;
import ch.iec.tc57._2011.replymeterconfig.MeterConfigPort;
import ch.iec.tc57._2011.replymeterconfig.ReplyMeterConfig;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.Name;
import ch.iec.tc57._2011.schema.message.ObjectType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.elster.jupiter.issue.share.IssueWebServiceClient;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
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
import javax.xml.ws.Service;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.replymeterconfig.provider",
        service = {IssueWebServiceClient.class, ReplyMeterConfigWebService.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + ReplyMeterConfigWebService.NAME})
public class ReplyMeterConfigServiceProvider extends AbstractOutboundEndPointProvider<MeterConfigPort> implements IssueWebServiceClient, ReplyMeterConfigWebService, OutboundSoapEndPointProvider, ApplicationSpecific {

    private static final String NOUN = "MeterConfig";
    private static final String RESOURCE_WSDL = "/wsdl/meterconfig/ReplyMeterConfig.wsdl";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.meterconfigmessage.ObjectFactory meterConfigMessageObjectFactory = new ch.iec.tc57._2011.meterconfigmessage.ObjectFactory();
    private final List<MeterConfigExtendedDataFactory> meterConfigExtendedDataFactories = new ArrayList<>();

    private volatile MeterConfigFactory meterConfigFactory;
    private volatile DeviceService deviceService;

    public ReplyMeterConfigServiceProvider() {
        // for OSGi purposes
    }

    @Inject
    public ReplyMeterConfigServiceProvider(DeviceService deviceService,
                                           MeterConfigFactory meterConfigFactory) {
        setDeviceService(deviceService);
        setMeterConfigFactory(meterConfigFactory);
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

    @Override
    public boolean call(Issue issue, EndPointConfiguration endPointConfiguration) {
        deviceService.findDeviceById(Long.parseLong(issue.getDevice().getAmrId())).ifPresent(device -> {
            MeterConfigEventMessageType message = createInfoResponseMessage(createMeterConfig(Collections.singletonList(device)), HeaderType.Verb.CHANGED, null);
            using("changedMeterConfig")
                    .toEndpoints(endPointConfiguration)
                    .send(message);
        });
        return true;
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

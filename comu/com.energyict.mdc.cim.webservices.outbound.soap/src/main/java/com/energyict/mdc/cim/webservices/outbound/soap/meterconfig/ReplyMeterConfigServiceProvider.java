/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.meterconfig;

import com.elster.jupiter.issue.share.IssueWebServiceClient;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.energyict.mdc.cim.webservices.outbound.soap.FailedMeterOperation;
import com.energyict.mdc.cim.webservices.outbound.soap.MeterConfigExtendedDataFactory;
import com.energyict.mdc.cim.webservices.outbound.soap.MeterConfigFactory;
import com.energyict.mdc.cim.webservices.outbound.soap.OperationEnum;
import com.energyict.mdc.cim.webservices.outbound.soap.ReplyMeterConfigWebService;
import com.energyict.mdc.cim.webservices.outbound.soap.impl.TranslationKeys;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.xml.ws.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.replymeterconfig.provider",
        service = {IssueWebServiceClient.class, ReplyMeterConfigWebService.class, OutboundSoapEndPointProvider.class, TranslationKeyProvider.class},
        immediate = true,
        property = {"name=" + ReplyMeterConfigWebService.NAME})
public class ReplyMeterConfigServiceProvider extends AbstractOutboundEndPointProvider<MeterConfigPort> implements IssueWebServiceClient, ReplyMeterConfigWebService, OutboundSoapEndPointProvider, TranslationKeyProvider, ApplicationSpecific {

    private static final String COMPONENT_NAME = "SIM";
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
            MeterConfigEventMessageType message = createResponseMessage(createMeterConfig(Collections.singletonList(device)), HeaderType.Verb.CHANGED);
            using("changedMeterConfig")
                    .toEndpoints(endPointConfiguration)
                    .send(message);
        });
        return true;
    }


    @Override
    public void call(EndPointConfiguration endPointConfiguration, OperationEnum operation,
                     List<Device> successfulDevices, List<FailedMeterOperation> failedDevices, long expectedNumberOfCalls) {
        String method;
        MeterConfigEventMessageType message;
        switch (operation) {
            case CREATE:
                method = "createdMeterConfig";
                message = createResponseMessage(createMeterConfig(successfulDevices), failedDevices, expectedNumberOfCalls, HeaderType.Verb.CREATED);
                break;
            case UPDATE:
                method = "changedMeterConfig";
                message = createResponseMessage(createMeterConfig(successfulDevices), failedDevices, expectedNumberOfCalls, HeaderType.Verb.CHANGED);
                break;
            case GET:
                method = "replyMeterConfig";
                message = createResponseMessage(getMeterConfig(successfulDevices), failedDevices, expectedNumberOfCalls, HeaderType.Verb.REPLY);
                break;
            default:
                throw new UnsupportedOperationException(OperationEnum.class.getSimpleName() + '#' + operation.name() + " isn't supported.");
        }
        using(method)
                .toEndpoints(endPointConfiguration)
                .send(message);
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.SOAP;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>();
        translationKeys.addAll(Arrays.asList(TranslationKeys.values()));
        return translationKeys;
    }

    private MeterConfig createMeterConfig(List<Device> devices) {
        MeterConfig meterConfig = meterConfigFactory.asMeterConfig(devices);
        getMeterConfigExtendedDataFactories().forEach(meterConfigExtendedDataFactory -> {
            meterConfigExtendedDataFactory.extendData(devices, meterConfig);
        });
        return meterConfig;
    }

    private MeterConfig getMeterConfig(List<Device> devices) {
        return meterConfigFactory.asGetMeterConfig(devices);
    }

    private MeterConfigEventMessageType createResponseMessage(MeterConfig meterConfig, HeaderType.Verb verb) {
        MeterConfigEventMessageType meterConfigEventMessageType = new MeterConfigEventMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(verb);
        meterConfigEventMessageType.setHeader(header);

        // set reply
        ReplyType replyType = cimMessageObjectFactory.createReplyType();
        replyType.setResult(ReplyType.Result.OK);
        meterConfigEventMessageType.setReply(replyType);

        // set payload
        MeterConfigPayloadType payloadType = meterConfigMessageObjectFactory.createMeterConfigPayloadType();
        meterConfigEventMessageType.setPayload(payloadType);
        payloadType.setMeterConfig(meterConfig);
        meterConfigEventMessageType.setPayload(payloadType);

        return meterConfigEventMessageType;
    }

    private MeterConfigEventMessageType createResponseMessage(MeterConfig meterConfig, List<FailedMeterOperation> failedDevices, long expectedNumberOfCalls, HeaderType.Verb verb) {
        MeterConfigEventMessageType meterConfigEventMessageType = createResponseMessage(meterConfig, verb);

        // set reply
        ReplyType replyType = cimMessageObjectFactory.createReplyType();
        if (expectedNumberOfCalls == meterConfig.getMeter().size()) {
            replyType.setResult(ReplyType.Result.OK);
        } else if (expectedNumberOfCalls == failedDevices.size()) {
            replyType.setResult(ReplyType.Result.FAILED);
        } else {
            replyType.setResult(ReplyType.Result.PARTIAL);
        }

        // set errors
        failedDevices.forEach(failedMeterOperation -> {
            ErrorType errorType = new ErrorType();
            errorType.setCode(failedMeterOperation.getErrorCode());
            errorType.setDetails(failedMeterOperation.getErrorMessage());
            ObjectType objectType = new ObjectType();
            objectType.setMRID(failedMeterOperation.getmRID());
            objectType.setObjectType("EndDevice");
            Name name = new Name();
            name.setName(failedMeterOperation.getMeterName());
            objectType.getName().add(name);
            errorType.setObject(objectType);
            replyType.getError().add(errorType);
        });

        meterConfigEventMessageType.setReply(replyType);

        return meterConfigEventMessageType;
    }

    @Override
    public String getApplication(){
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}
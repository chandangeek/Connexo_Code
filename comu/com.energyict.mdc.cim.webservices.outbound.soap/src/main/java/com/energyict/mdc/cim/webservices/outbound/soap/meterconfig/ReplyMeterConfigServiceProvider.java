/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.meterconfig;

import com.elster.jupiter.issue.share.IssueWebServiceClient;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.energyict.mdc.cim.webservices.inbound.soap.FailedMeterOperation;
import com.energyict.mdc.cim.webservices.inbound.soap.MeterConfigFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.OperationEnum;
import com.energyict.mdc.cim.webservices.inbound.soap.ReplyMeterConfigWebService;
import com.energyict.mdc.cim.webservices.outbound.soap.MeterConfigExtendedDataFactory;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigEventMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigPayloadType;
import ch.iec.tc57._2011.replymeterconfig.FaultMessage;
import ch.iec.tc57._2011.replymeterconfig.MeterConfigPort;
import ch.iec.tc57._2011.replymeterconfig.ReplyMeterConfig;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.Name;
import ch.iec.tc57._2011.schema.message.ObjectType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import org.apache.cxf.jaxws.JaxWsClientProxy;
import org.apache.cxf.message.Message;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.replymeterconfig.provider",
        service = {IssueWebServiceClient.class, ReplyMeterConfigWebService.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + ReplyMeterConfigWebService.NAME})
public class ReplyMeterConfigServiceProvider implements IssueWebServiceClient, ReplyMeterConfigWebService, OutboundSoapEndPointProvider {

    private static final String COMPONENT_NAME = "SIM";
    private static final String NOUN = "MeterConfig";
    private static final String URL = "url";
    private static final String RESOURCE_WSDL = "/meterconfig/ReplyMeterConfig.wsdl";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.meterconfigmessage.ObjectFactory meterConfigMessageObjectFactory = new ch.iec.tc57._2011.meterconfigmessage.ObjectFactory();
    private final Map<String, MeterConfigPort> meterConfigPorts = new ConcurrentHashMap<>();
    private final List<MeterConfigExtendedDataFactory> meterConfigExtendedDataFactories = new ArrayList<>();

    private volatile MeterConfigFactory meterConfigFactory;
    private volatile DeviceService deviceService;
    private volatile WebServicesService webServicesService;

    public ReplyMeterConfigServiceProvider() {
        // for OSGI purposes
    }

    public ReplyMeterConfigServiceProvider(DeviceService deviceService, WebServicesService webServicesService, MeterConfigFactory meterConfigFactory) {
        this();
        setDeviceService(deviceService);
        setWebServicesService(webServicesService);
        setMeterConfigFactory(meterConfigFactory);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterConfigPort(MeterConfigPort meterConfigPort, Map<String, Object> properties) {
        meterConfigPorts.put(properties.get(URL).toString(), meterConfigPort);
    }

    public void removeMeterConfigPort(MeterConfigPort meterConfigPort) {
        meterConfigPorts.values().removeIf(meterConfigPort1 -> meterConfigPort1 == meterConfigPort);
    }

    public Map<String, MeterConfigPort> getMeterConfigPorts() {
        return Collections.unmodifiableMap(meterConfigPorts);
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
    public void setWebServicesService(WebServicesService webServicesService) {
        this.webServicesService = webServicesService;
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
    public Class getService() {
        return MeterConfigPort.class;
    }

    @Override
    public String getWebServiceName() {
        return ReplyMeterConfigWebService.NAME;
    }

    @Override
    public boolean call(Issue issue, EndPointConfiguration endPointConfiguration) {
        publish(endPointConfiguration);
        deviceService.findDeviceById(Long.parseLong(issue.getDevice().getAmrId())).ifPresent(device -> {
            try {
                Optional.ofNullable(getMeterConfigPorts().get(endPointConfiguration.getUrl()))
                        .filter(meterConfigPort -> isValidMeterConfigPortService(meterConfigPort))
                        .ifPresent(meterConfigPortService -> {
                            try {
                                meterConfigPortService.changedMeterConfig(createResponseMessage(createMeterConfig(Collections
                                        .singletonList(device)), HeaderType.Verb.CHANGED));
                            } catch (FaultMessage faultMessage) {
                                endPointConfiguration.log(faultMessage.getMessage(), faultMessage);
                            }
                        });
            } catch (RuntimeException ex) {
                endPointConfiguration.log(LogLevel.SEVERE, ex.getMessage());
            }
        });
        return true;
    }

    @Override
    public void call(EndPointConfiguration endPointConfiguration, OperationEnum operation,
                     List<Device> successfulDevices, List<FailedMeterOperation> failedDevices, long expectedNumberOfCalls) {
        publish(endPointConfiguration);
        try {
            Optional.ofNullable(getMeterConfigPorts().get(endPointConfiguration.getUrl()))
                    .filter(meterConfigPort -> isValidMeterConfigPortService(meterConfigPort))
                    .ifPresent(meterConfigPortService -> {
                        try {
                            switch (operation) {
                                case CREATE:
                                    meterConfigPortService.createdMeterConfig(createResponseMessage(createMeterConfig(successfulDevices), failedDevices, expectedNumberOfCalls, HeaderType.Verb.CREATED));
                                    break;
                                case UPDATE:
                                    meterConfigPortService.changedMeterConfig(createResponseMessage(createMeterConfig(successfulDevices), failedDevices, expectedNumberOfCalls, HeaderType.Verb.CHANGED));
                                    break;
                                case GET:
                                    meterConfigPortService.replyMeterConfig(createResponseMessage(getMeterConfig(successfulDevices), failedDevices, expectedNumberOfCalls, HeaderType.Verb.REPLY));
                                    break;
                            }
                        } catch (FaultMessage faultMessage) {
                            endPointConfiguration.log(faultMessage.getMessage(), faultMessage);
                        }
                    });
        } catch (RuntimeException ex) {
            endPointConfiguration.log(LogLevel.SEVERE, ex.getMessage());
        }
    }

    private void publish(EndPointConfiguration endPointConfiguration) {
        if (endPointConfiguration.isActive() && !webServicesService.isPublished(endPointConfiguration)) {
            webServicesService.publishEndPoint(endPointConfiguration);
        }
    }

    private MeterConfig createMeterConfig(List<Device> devices) {
        MeterConfig meterConfig = meterConfigFactory.asMeterConfig(devices);
        getMeterConfigExtendedDataFactories().forEach(meterConfigExtendedDataFactory -> {
            meterConfigExtendedDataFactory.extendData(devices, meterConfig);
        });
        return meterConfig;
    }

    private MeterConfig getMeterConfig(List<Device> devices) {
        MeterConfig meterConfig = meterConfigFactory.asGetMeterConfig(devices);
        return meterConfig;
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

    private boolean isValidMeterConfigPortService(MeterConfigPort meterConfigPort) {
        return ((JaxWsClientProxy) (Proxy.getInvocationHandler(meterConfigPort))).getRequestContext()
                .containsKey(Message.ENDPOINT_ADDRESS);
    }

}
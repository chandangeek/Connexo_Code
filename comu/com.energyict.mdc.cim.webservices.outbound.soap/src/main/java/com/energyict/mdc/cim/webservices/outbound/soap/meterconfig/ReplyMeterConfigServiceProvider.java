/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.meterconfig;

import com.elster.jupiter.issue.share.IssueWebServiceClient;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
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
import ch.iec.tc57._2011.schema.message.ReplyType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.replymeterconfig.provider",
        service = {IssueWebServiceClient.class, ReplyMeterConfigWebService.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + ReplyMeterConfigWebService.NAME})
public class ReplyMeterConfigServiceProvider implements IssueWebServiceClient, ReplyMeterConfigWebService, OutboundSoapEndPointProvider {

    private static final String NOUN = "ReplyMeterConfig";
    private static final String RESOURCE_WSDL = "/meterconfig/ReplyMeterConfig.wsdl";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.meterconfigmessage.ObjectFactory meterConfigMessageObjectFactory = new ch.iec.tc57._2011.meterconfigmessage.ObjectFactory();
    private final List<MeterConfigPort> meterConfigPorts = new ArrayList<>();
    private final List<MeterConfigExtendedDataFactory> meterConfigExtendedDataFactories = new ArrayList<>();
    private final MeterConfigFactory meterConfigFactory = new MeterConfigFactory();

    private volatile DeviceService deviceService;

    public ReplyMeterConfigServiceProvider() {
        // for OSGI purposes
    }

    public ReplyMeterConfigServiceProvider(DeviceService deviceService) {
        setDeviceService(deviceService);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterConfigPort(MeterConfigPort meterConfigPort) {
        meterConfigPorts.add(meterConfigPort);
    }

    public void removeMeterConfigPort(MeterConfigPort meterConfigPort) {
        meterConfigPorts.remove(meterConfigPort);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterConfigExtendedDataFactory(MeterConfigExtendedDataFactory meterConfigExtendedDataFactory) {
        meterConfigExtendedDataFactories.add(meterConfigExtendedDataFactory);
    }

    public void removeMeterConfigExtendedDataFactory(MeterConfigExtendedDataFactory meterConfigExtendedDataFactory) {
        meterConfigExtendedDataFactories.remove(meterConfigExtendedDataFactory);
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
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
        deviceService.findDeviceById(Long.parseLong(issue.getDevice().getAmrId())).ifPresent(device -> {
            try {
                meterConfigPorts
                        .forEach(meterConfigPortService -> {
                            try {
                                meterConfigPortService.changedMeterConfig(createResponseMessage(createMeterConfig(Collections.singletonList(device)), HeaderType.Verb.CHANGED));
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
                     List<Device> successfulDevices, Map<String, String> failedDevices, BigDecimal expectedNumberOfCalls) {
        try {
            meterConfigPorts.forEach(meterConfigPortService -> {
                try {
                    switch (operation) {
                        case CREATE:
                            meterConfigPortService.createdMeterConfig(createResponseMessage(createMeterConfig(successfulDevices), failedDevices, expectedNumberOfCalls, HeaderType.Verb.CREATED));
                            break;
                        case UPDATE:
                            meterConfigPortService.changedMeterConfig(createResponseMessage(createMeterConfig(successfulDevices), failedDevices, expectedNumberOfCalls, HeaderType.Verb.CHANGED));
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

    private MeterConfig createMeterConfig(List<Device> devices) {
        MeterConfig meterConfig = meterConfigFactory.asMeterConfig(devices);
        meterConfigExtendedDataFactories.forEach(meterConfigExtendedDataFactory -> {
            meterConfigExtendedDataFactory.extendData(devices, meterConfig);
        });
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

    private MeterConfigEventMessageType createResponseMessage(MeterConfig meterConfig, Map<String, String> failedDevices, BigDecimal expectedNumberOfCalls, HeaderType.Verb verb) {
        MeterConfigEventMessageType meterConfigEventMessageType = createResponseMessage(meterConfig, verb);

        // set reply
        ReplyType replyType = cimMessageObjectFactory.createReplyType();
        if (expectedNumberOfCalls.compareTo(BigDecimal.valueOf(meterConfig.getMeter().size())) == 0) {
            replyType.setResult(ReplyType.Result.OK);
        } else if (expectedNumberOfCalls.compareTo(BigDecimal.valueOf(failedDevices.size())) == 0) {
            replyType.setResult(ReplyType.Result.FAILED);
        } else {
            replyType.setResult(ReplyType.Result.PARTIAL);
        }

        // set errors
        failedDevices.forEach((key, value) -> {
            ErrorType errorType = new ErrorType();
            errorType.setCode(key);
            errorType.setDetails(value);
            replyType.getError().add(errorType);
        });

        meterConfigEventMessageType.setReply(replyType);

        return meterConfigEventMessageType;
    }
}
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.impl;

import com.elster.jupiter.cim.webservices.outbound.soap.enddeviceconfig.EndDeviceFactory;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.EndPointConfigurationReference;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.fsm.StateTransitionWebServiceClient;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.HasName;

import org.apache.cxf.jaxws.JaxWsClientProxy;
import org.apache.cxf.message.Message;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.replyenddeviceconfig.FaultMessage;
import ch.iec.tc57._2011.replyenddeviceconfig.EndDeviceConfigPort;
import ch.iec.tc57._2011.replyenddeviceconfig.ReplyEndDeviceConfig;
import ch.iec.tc57._2011.enddeviceconfig.EndDeviceConfig;
import ch.iec.tc57._2011.enddeviceconfigmessage.EndDeviceConfigEventMessageType;
import ch.iec.tc57._2011.enddeviceconfigmessage.EndDeviceConfigPayloadType;

import javax.inject.Inject;
import javax.xml.ws.Service;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.cim.webservices.outbound.soap.enddeviceconfig.provider",
        service = {TopicHandler.class, StateTransitionWebServiceClient.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + StateTransitionWebServiceClient.NAME})
public class EndDeviceConfigServiceProvider implements TopicHandler, StateTransitionWebServiceClient, OutboundSoapEndPointProvider {

    private final String RESOURCE_WSDL = "/enddeviceconfig/ReplyEndDeviceConfig.wsdl";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.enddeviceconfigmessage.ObjectFactory endDeviceConfigMessageObjectFactory = new ch.iec.tc57._2011.enddeviceconfigmessage.ObjectFactory();

    private final List<EndDeviceConfigPort> stateEndDeviceConfigPortServices = new ArrayList<>();
    private final EndDeviceFactory endDeviceFactory = new EndDeviceFactory();

    private volatile MeteringService meteringService;
    private volatile EndPointConfigurationService endPointConfigurationService;

    public EndDeviceConfigServiceProvider() {
        // for OSGI purposes
    }

    @Inject
    public EndDeviceConfigServiceProvider(MeteringService meteringService, EndPointConfigurationService endPointConfigurationService) {
        this();
        this.meteringService = meteringService;
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterConfigPortService(EndDeviceConfigPort endDeviceConfigPort) {
        stateEndDeviceConfigPortServices.add(endDeviceConfigPort);
    }

    public void removeMeterConfigPortService(EndDeviceConfigPort endDeviceConfigPort) {
        stateEndDeviceConfigPortServices.remove(endDeviceConfigPort);
    }

    public List<EndDeviceConfigPort> getStateTransitionWebServiceClients() {
        return Collections.unmodifiableList(this.stateEndDeviceConfigPortServices);
    }

    @Override
    public Service get() {
        return new ReplyEndDeviceConfig(this.getClass().getResource(RESOURCE_WSDL));
    }

    @Override
    public Class getService() {
        return EndDeviceConfigPort.class;
    }

    @Override
    public String getWebServiceName() {
        return StateTransitionWebServiceClient.NAME;
    }

    @Override
    public void call(long id, List<Long> endPointConfigurationIds, String state, Instant effectiveDate) {
        meteringService.findEndDeviceById(id).ifPresent(endDevice -> {
            call(endDevice, getEndPointConfigurationByIds(endPointConfigurationIds), state, effectiveDate, false);
        });
    }

    @Override
    public void handle(LocalEvent localEvent) {
        meteringService.findEndDeviceByName(((HasName) localEvent.getSource()).getName()).ifPresent(endDevice -> {
            endDevice.getFiniteStateMachine().ifPresent(finiteStateMachine -> {
                List<Long> endPointConfigurationIds = finiteStateMachine.getInitialState().getOnEntryEndPointConfigurations().stream()
                        .map(EndPointConfigurationReference::getStateChangeEndPointConfiguration)
                        .map(EndPointConfiguration::getId)
                        .collect(Collectors.toList());
                endDevice.getLifecycleDates().getReceivedDate().ifPresent(effectiveDate -> {
                    call(endDevice, getEndPointConfigurationByIds(endPointConfigurationIds), finiteStateMachine.getInitialState().getName(), effectiveDate, true);
                });
            });
        });
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/data/device/CREATED";
    }

    private void call(EndDevice endDevice, List<EndPointConfiguration> endPointConfigurations, String state, Instant effectiveDate, boolean isCreated) {
        endPointConfigurations.forEach(endPointConfiguration -> {
            try {
                stateEndDeviceConfigPortServices.stream()
                        .filter(endDeviceConfigPort -> isValidReplyMeterConfigService(endDeviceConfigPort, endPointConfiguration))
                        .findFirst()
                        .ifPresent(endDeviceConfigPortService -> {
                            try {
                                if (isCreated) {
                                    endDeviceConfigPortService.createdEndDeviceConfig(createResponseMessage(endDeviceFactory.asEndDevice(endDevice, state, effectiveDate), HeaderType.Verb.CREATED));
                                } else {
                                    endDeviceConfigPortService.changedEndDeviceConfig(createResponseMessage(endDeviceFactory.asEndDevice(endDevice, state, effectiveDate), HeaderType.Verb.CHANGED));
                                }
                                endPointConfiguration.log(LogLevel.INFO, (isCreated ? "Created" : "Changed") + " state " + state + " on " + endDevice.getName() + " message was sent");
                            } catch (FaultMessage faultMessage) {
                                endPointConfiguration.log(faultMessage.getMessage(), faultMessage);
                            }
                        });
            } catch (Exception ex) {
                endPointConfiguration.log(LogLevel.SEVERE, ex.getMessage());
            }
        });
    }

    private List<EndDeviceConfigPort> findActualEndDeviceConfigPortServices(List<Long> endPointConfigurationIds) {
        List<EndDeviceConfigPort> endDeviceConfigPorts = new ArrayList<>();
        endPointConfigurationIds.stream()
                .map(id -> endPointConfigurationService.getEndPointConfiguration(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(endPointConfiguration -> {
                    stateEndDeviceConfigPortServices.stream()
                            .filter(endDeviceConfigPort -> isValidReplyMeterConfigService(endDeviceConfigPort, endPointConfiguration))
                            .findFirst()
                            .ifPresent(endDeviceConfigPorts::add);
                });
        return endDeviceConfigPorts;
    }

    private List<EndPointConfiguration> getEndPointConfigurationByIds(List<Long> endPointConfigurationIds) {
        return endPointConfigurationIds.stream()
                .map(id -> endPointConfigurationService.getEndPointConfiguration(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private boolean isValidReplyMeterConfigService(EndDeviceConfigPort endDeviceConfigPort, EndPointConfiguration endPointConfiguration) {
        return endPointConfiguration.getUrl().toLowerCase().contains(((String) ((JaxWsClientProxy) (Proxy.getInvocationHandler(endDeviceConfigPort))).getRequestContext().get(Message.ENDPOINT_ADDRESS)).toLowerCase());
    }

    private EndDeviceConfigEventMessageType createResponseMessage(EndDeviceConfig endDeviceConfig, HeaderType.Verb verb) {
        EndDeviceConfigEventMessageType endDeviceConfigEventMessageType = new EndDeviceConfigEventMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(getWebServiceName());
        header.setVerb(verb);
        endDeviceConfigEventMessageType.setHeader(header);

        // set reply
        ReplyType replyType = cimMessageObjectFactory.createReplyType();
        replyType.setResult(ReplyType.Result.OK);
        endDeviceConfigEventMessageType.setReply(replyType);

        // set payload
        EndDeviceConfigPayloadType endDeviceConfigPayloadType = endDeviceConfigMessageObjectFactory.createEndDeviceConfigPayloadType();
        endDeviceConfigPayloadType.setEndDeviceConfig(endDeviceConfig);
        endDeviceConfigEventMessageType.setPayload(endDeviceConfigPayloadType);

        return endDeviceConfigEventMessageType;
    }
}
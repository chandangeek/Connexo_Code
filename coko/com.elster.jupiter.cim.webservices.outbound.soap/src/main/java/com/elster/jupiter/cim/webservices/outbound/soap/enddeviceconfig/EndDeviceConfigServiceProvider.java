/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.enddeviceconfig;

import com.elster.jupiter.cim.webservices.outbound.soap.EndDeviceConfigExtendedDataFactory;
import com.elster.jupiter.metering.EndDeviceAttributesProvider;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.EndPointConfigurationReference;
import com.elster.jupiter.fsm.StateTransitionWebServiceClient;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.replyenddeviceconfig.EndDeviceConfigPort;
import ch.iec.tc57._2011.replyenddeviceconfig.ReplyEndDeviceConfig;
import ch.iec.tc57._2011.enddeviceconfig.EndDeviceConfig;
import ch.iec.tc57._2011.enddeviceconfigmessage.EndDeviceConfigEventMessageType;
import ch.iec.tc57._2011.enddeviceconfigmessage.EndDeviceConfigPayloadType;

import javax.inject.Inject;
import javax.xml.ws.Service;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.cim.webservices.outbound.soap.enddeviceconfig.provider",
        service = {TopicHandler.class, StateTransitionWebServiceClient.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + StateTransitionWebServiceClient.NAME})
public class EndDeviceConfigServiceProvider extends AbstractOutboundEndPointProvider<EndDeviceConfigPort> implements TopicHandler, StateTransitionWebServiceClient, OutboundSoapEndPointProvider, ApplicationSpecific {
    private static final String NOUN = "EndDeviceConfig";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.enddeviceconfigmessage.ObjectFactory endDeviceConfigMessageObjectFactory = new ch.iec.tc57._2011.enddeviceconfigmessage.ObjectFactory();

    private final List<EndDeviceConfigPort> stateEndDeviceConfigPortServices = new ArrayList<>();
    private final List<EndDeviceConfigExtendedDataFactory> endDeviceConfigExtendedDataFactories = new ArrayList<>();
    private final EndDeviceConfigDataFactory endDeviceConfigDataFactory = new EndDeviceConfigDataFactory();

    private volatile MeteringService meteringService;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private List<EndDeviceAttributesProvider> endDeviceAttributesProviders = new ArrayList<>();

    public EndDeviceConfigServiceProvider() {
        // for OSGI purposes
    }

    @Inject
    public EndDeviceConfigServiceProvider(MeteringService meteringService,
                                          EndPointConfigurationService endPointConfigurationService) {
        this();
        setMeteringService(meteringService);
        setEndPointConfigurationService(endPointConfigurationService);
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
    public void addMeterConfigPortService(EndDeviceConfigPort endDeviceConfigPort, Map<String, Object> properties) {
        super.doAddEndpoint(endDeviceConfigPort, properties);
    }

    public void removeMeterConfigPortService(EndDeviceConfigPort endDeviceConfigPort) {
        super.doRemoveEndpoint(endDeviceConfigPort);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addEndDeviceAttributesProvider(EndDeviceAttributesProvider endDeviceAttributesProvider) {
        this.endDeviceAttributesProviders.add(endDeviceAttributesProvider);
    }

    @Reference
    public void addWebServicesService(WebServicesService webServicesService) {
        // Just to inject WebServicesService
    }

    public void removeEndDeviceAttributesProvider(EndDeviceAttributesProvider endDeviceAttributesProvider) {
        endDeviceAttributesProviders.stream()
                .filter(e -> e.getClass().equals(endDeviceAttributesProvider.getClass()))
                .forEach(e -> endDeviceAttributesProviders.remove(e));
    }


    public List<EndDeviceConfigPort> getStateTransitionWebServiceClients() {
        return Collections.unmodifiableList(this.stateEndDeviceConfigPortServices);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addEndDeviceConfigExtendedDataFactory(EndDeviceConfigExtendedDataFactory endDeviceConfigExtendedDataFactory) {
        endDeviceConfigExtendedDataFactories.add(endDeviceConfigExtendedDataFactory);
    }

    public void removeEndDeviceConfigExtendedDataFactory(EndDeviceConfigExtendedDataFactory endDeviceConfigExtendedDataFactory) {
        endDeviceConfigExtendedDataFactories.remove(endDeviceConfigExtendedDataFactory);
    }

    public List<EndDeviceConfigExtendedDataFactory> getEndDeviceConfigExtendedDataFactories() {
        return Collections.unmodifiableList(this.endDeviceConfigExtendedDataFactories);
    }

    @Override
    public Service get() {
        return new ReplyEndDeviceConfig(this.getClass().getResource("/enddeviceconfig/ReplyEndDeviceConfig.wsdl"));
    }

    @Override
    public Class<EndDeviceConfigPort> getService() {
        return EndDeviceConfigPort.class;
    }

    @Override
    protected String getName() {
        return StateTransitionWebServiceClient.NAME;
    }

    @Override
    public String getWebServiceName() {
        return getName();
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
            EndDeviceConfig endDeviceConfig = endDeviceConfigDataFactory.asEndDevice(endDevice, state, effectiveDate, endDeviceAttributesProviders);
            getEndDeviceConfigExtendedDataFactories().forEach(endDeviceConfigExtendedDataFactory -> {
                endDeviceConfigExtendedDataFactory.extendData(endDevice, endDeviceConfig);
            });
            EndDeviceConfigEventMessageType message = createResponseMessage(endDeviceConfig, HeaderType.Verb.CHANGED);
            String methodName;
            if (isCreated) {
                methodName = "createdEndDeviceConfig";
            } else {
                methodName = "changedEndDeviceConfig";
            }
            using(methodName)
                    .toEndpoints(endPointConfiguration)
                    .send(message);
        });
    }

    private List<EndPointConfiguration> getEndPointConfigurationByIds(List<Long> endPointConfigurationIds) {
        return endPointConfigurationIds.stream()
                .map(id -> endPointConfigurationService.getEndPointConfiguration(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private EndDeviceConfigEventMessageType createResponseMessage(EndDeviceConfig endDeviceConfig, HeaderType.Verb verb) {
        EndDeviceConfigEventMessageType endDeviceConfigEventMessageType = new EndDeviceConfigEventMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
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

    @Override
    public String getApplication(){
        return WebServiceApplicationName.MULTISENSE_INSIGHT.getName();
    }
}
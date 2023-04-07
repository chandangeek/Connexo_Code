/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.enddeviceconfig;

import com.elster.jupiter.cim.webservices.outbound.soap.EndDeviceConfigExtendedDataFactory;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.EndPointConfigurationReference;
import com.elster.jupiter.fsm.StateTransitionWebServiceClient;
import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceAttributesProvider;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.HasName;

import ch.iec.tc57._2011.enddeviceconfig.EndDeviceConfig;
import ch.iec.tc57._2011.enddeviceconfigmessage.EndDeviceConfigEventMessageType;
import ch.iec.tc57._2011.enddeviceconfigmessage.EndDeviceConfigPayloadType;
import ch.iec.tc57._2011.replyenddeviceconfig.EndDeviceConfigPort;
import ch.iec.tc57._2011.replyenddeviceconfig.ReplyEndDeviceConfig;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.google.common.collect.HashMultimap;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.cim.webservices.outbound.soap.enddeviceconfig.provider",
        service = {TopicHandler.class, StateTransitionWebServiceClient.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + EndDeviceConfigServiceProvider.NAME})
public class EndDeviceConfigServiceProvider extends AbstractOutboundEndPointProvider<EndDeviceConfigPort> implements TopicHandler, StateTransitionWebServiceClient, OutboundSoapEndPointProvider, ApplicationSpecific {
    private static final String NOUN = "EndDeviceConfig";
    public static final String NAME = "CIM ReplyEndDeviceConfig";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.enddeviceconfigmessage.ObjectFactory endDeviceConfigMessageObjectFactory = new ch.iec.tc57._2011.enddeviceconfigmessage.ObjectFactory();

    private final List<EndDeviceConfigExtendedDataFactory> endDeviceConfigExtendedDataFactories = new CopyOnWriteArrayList<>();
    private final EndDeviceConfigDataFactory endDeviceConfigDataFactory = new EndDeviceConfigDataFactory();

    private volatile MeteringService meteringService;
    private final List<EndDeviceAttributesProvider> endDeviceAttributesProviders = new CopyOnWriteArrayList<>();

    public EndDeviceConfigServiceProvider() {
        // for OSGI purposes
    }

    @Inject
    public EndDeviceConfigServiceProvider(MeteringService meteringService) {
        // for tests
        this();
        setMeteringService(meteringService);
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void addWebServicesService(WebServicesService webServicesService) {
        // Just to inject WebServicesService
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

    public void removeEndDeviceAttributesProvider(EndDeviceAttributesProvider endDeviceAttributesProvider) {
        endDeviceAttributesProviders.stream()
                .filter(e -> e.getClass().equals(endDeviceAttributesProvider.getClass()))
                .forEach(endDeviceAttributesProviders::remove);
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
        return new ReplyEndDeviceConfig(this.getClass().getResource("/wsdl/enddeviceconfig/ReplyEndDeviceConfig.wsdl"));
    }

    @Override
    public Class<EndDeviceConfigPort> getService() {
        return EndDeviceConfigPort.class;
    }

    @Override
    protected String getName() {
        return NAME;
    }

    @Override
    public String getWebServiceName() {
        return getName();
    }

    @Override
    public void call(long id, Set<EndPointConfiguration> endPointConfigurations, String state, Instant effectiveDate) {
        meteringService.findEndDeviceById(id)
                .ifPresent(endDevice -> call(endDevice, endPointConfigurations, state, effectiveDate, false));
    }

    @Override
    public void handle(LocalEvent localEvent) {
        meteringService.findEndDeviceByName(((HasName) localEvent.getSource()).getName()).ifPresent(endDevice ->
                endDevice.getFiniteStateMachine().ifPresent(finiteStateMachine -> {
                    List<EndPointConfiguration> endPointConfigurations = finiteStateMachine.getInitialState().getOnEntryEndPointConfigurations().stream()
                            .map(EndPointConfigurationReference::getStateChangeEndPointConfiguration)
                            .collect(Collectors.toList());
                    endDevice.getLifecycleDates().getReceivedDate().ifPresent(effectiveDate ->
                            call(endDevice, endPointConfigurations, finiteStateMachine.getInitialState().getName(), effectiveDate, true));
                }));
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/data/device/CREATED";
    }

    private void call(EndDevice endDevice, Collection<EndPointConfiguration> endPointConfigurations, String state, Instant effectiveDate, boolean isCreated) {
        EndDeviceConfig endDeviceConfig = endDeviceConfigDataFactory.asEndDevice(endDevice, state, effectiveDate, endDeviceAttributesProviders);
        getEndDeviceConfigExtendedDataFactories()
                .forEach(endDeviceConfigExtendedDataFactory -> endDeviceConfigExtendedDataFactory.extendData(endDevice, endDeviceConfig));
        EndDeviceConfigEventMessageType message;
        String methodName;
        if (isCreated) {
            methodName = "createdEndDeviceConfig";
            message = createResponseMessage(endDeviceConfig, HeaderType.Verb.CREATED);
        } else {
            methodName = "changedEndDeviceConfig";
            message = createResponseMessage(endDeviceConfig, HeaderType.Verb.CHANGED);
        }
        SetMultimap<String, String> values = HashMultimap.create();
        values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), endDevice.getMRID());
        values.put(CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), endDevice.getSerialNumber());
        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), endDevice.getName());

        using(methodName)
                .toEndpoints(endPointConfigurations)
                .withRelatedAttributes(values)
                .send(message);
    }

    private EndDeviceConfigEventMessageType createResponseMessage(EndDeviceConfig endDeviceConfig, HeaderType.Verb verb) {
        EndDeviceConfigEventMessageType endDeviceConfigEventMessageType = new EndDeviceConfigEventMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(verb);
        header.setCorrelationID(UUID.randomUUID().toString());
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

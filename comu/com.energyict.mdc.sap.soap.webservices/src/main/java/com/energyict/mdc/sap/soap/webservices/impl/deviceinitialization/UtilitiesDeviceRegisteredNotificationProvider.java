/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization;

import com.elster.jupiter.fsm.StateTransitionWebServiceClient;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceRegisteredNotification;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilitiesAdvancedMeteringSystemID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilitiesDeviceERPSmartMeterRegisteredNotificationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilitiesDeviceERPSmartMeterRegisteredNotificationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilsDvceERPSmrtMtrRegedNotifMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilsDvceERPSmrtMtrRegedNotifSmrtMtr;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilsDvceERPSmrtMtrRegedNotifUtilsDvce;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.ObjectFactory;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;

import org.apache.cxf.jaxws.JaxWsClientProxy;
import org.apache.cxf.message.Message;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.xml.ws.Service;
import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component(name = UtilitiesDeviceRegisteredNotification.NAME,
        service = {UtilitiesDeviceRegisteredNotification.class, StateTransitionWebServiceClient.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + UtilitiesDeviceRegisteredNotification.NAME})
public class UtilitiesDeviceRegisteredNotificationProvider implements UtilitiesDeviceRegisteredNotification, StateTransitionWebServiceClient,
        OutboundSoapEndPointProvider{

    private final Map<String, UtilitiesDeviceERPSmartMeterRegisteredNotificationCOut> ports = new HashMap<>();
    private final ObjectFactory objectFactory = new ObjectFactory();

    private volatile Thesaurus thesaurus;
    private volatile Clock clock;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile MeteringService meteringService;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile DeviceService deviceService;

    public UtilitiesDeviceRegisteredNotificationProvider() {
        // for OSGI purposes
    }

    @Inject
    public UtilitiesDeviceRegisteredNotificationProvider(Clock clock,
                                                         SAPCustomPropertySets sapCustomPropertySets, MeteringService meteringService,
                                                         EndPointConfigurationService endPointConfigurationService,
                                                         DeviceService deviceService) {
        this();
        this.clock = clock;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.meteringService = meteringService;
        this.endPointConfigurationService = endPointConfigurationService;
        this.deviceService = deviceService;
    }

    @Reference
    public void setThesaurus(WebServiceActivator webServiceActivator) {
        thesaurus = webServiceActivator.getThesaurus();
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }


    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisteredNotificationCOut port,
                                           Map<String, Object> properties) {
        Optional.ofNullable(properties)
                .map(property -> property.get(WebServiceActivator.URL_PROPERTY))
                .map(String.class::cast)
                .ifPresent(url -> ports.put(url, port));
    }

    public void removeRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisteredNotificationCOut port) {
        ports.values().removeIf(entryPort -> port == entryPort);
    }

    @Override
    public Service get() {
        return new UtilitiesDeviceERPSmartMeterRegisteredNotificationCOutService();
    }

    @Override
    public Class getService() {
        return UtilitiesDeviceERPSmartMeterRegisteredNotificationCOut.class;
    }

    @Override
    public String getWebServiceName() {
        return NAME;
    }

    @Override
    public void call(long id, List<Long> endPointConfigurationIds, String state, Instant effectiveDate) {
        meteringService.findEndDeviceById(id).ifPresent(endDevice -> {
            deviceService.findDeviceByMrid(endDevice.getMRID()).ifPresent(
                    device -> {
                        if (device.getStage().getName().equals(EndDeviceStage.OPERATIONAL.getKey())) {
                            sapCustomPropertySets.getSapDeviceId(device).ifPresent(sapDeviceId -> {
                                if (sapCustomPropertySets.isAnyLrn(device.getId())) {
                                    call(sapDeviceId, getEndPointConfigurationByIds(endPointConfigurationIds));
                                }else{
                                    throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_ANY_LRN_ON_DEVICE, sapDeviceId);
                                }
                            });
                        }else{
                            throw new SAPWebServiceException(thesaurus, MessageSeeds.DEVICE_NOT_IN_OPERATIONAL_STAGE, endDevice.getMRID());
                        }
                    }
            );
        });
    }

    @Override
    public void call(String sapDeviceId) {
        UtilsDvceERPSmrtMtrRegedNotifMsg notificationMessage = createNotificationMessage(sapDeviceId);

        if (ports.isEmpty()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_WEB_SERVICE_ENDPOINTS);
        }

        ports.values().stream().findFirst().get().utilitiesDeviceERPSmartMeterRegisteredNotificationCOut(notificationMessage);
    }

    private void call(String sapDeviceId, List<EndPointConfiguration> endPointConfigurations) {
        endPointConfigurations.forEach(endPointConfiguration -> {
            try {
                getStateTransitionWebServiceClients().values().stream()
                        .filter(port -> isValidEndDeviceConfigPortService(port, endPointConfiguration))
                        .findFirst()
                        .ifPresent(portService -> {
                            try {
                                UtilsDvceERPSmrtMtrRegedNotifMsg notificationMessage = createNotificationMessage(sapDeviceId);
                                portService.utilitiesDeviceERPSmartMeterRegisteredNotificationCOut(notificationMessage);
                                endPointConfiguration.log(LogLevel.INFO, String.format("Send registered notification to web service %s with the URL", endPointConfiguration.getWebServiceName(), endPointConfiguration
                                        .getUrl()));
                            } catch (Exception faultMessage) {
                                endPointConfiguration.log(faultMessage.getMessage(), faultMessage);
                            }
                        });
            } catch (Exception e) {
                endPointConfiguration.log(String.format("registered notification to web service %s with the URL", endPointConfiguration.getWebServiceName(), endPointConfiguration.getUrl()), e);
            }
        });
    }

    private UtilsDvceERPSmrtMtrRegedNotifMsg createNotificationMessage(String sapDeviceId) {
        Instant createTime = clock.instant();
        UtilsDvceERPSmrtMtrRegedNotifMsg notificationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegedNotifMsg();
        notificationMessage.setMessageHeader(createMessageHeader(createTime));
        notificationMessage.setUtilitiesDevice(createBody(sapDeviceId));

        return notificationMessage;
    }

    private UtilsDvceERPSmrtMtrRegedNotifUtilsDvce createBody(String sapDeviceId) {
        UtilsDvceERPSmrtMtrRegedNotifUtilsDvce device = objectFactory.createUtilsDvceERPSmrtMtrRegedNotifUtilsDvce();
        UtilitiesDeviceID deviceId = objectFactory.createUtilitiesDeviceID();
        deviceId.setValue(sapDeviceId);

        UtilsDvceERPSmrtMtrRegedNotifSmrtMtr smartMeter = objectFactory.createUtilsDvceERPSmrtMtrRegedNotifSmrtMtr();
        UtilitiesAdvancedMeteringSystemID smartMeterId = objectFactory.createUtilitiesAdvancedMeteringSystemID();
        smartMeterId.setValue(WebServiceActivator.METERING_SYSTEM_ID);
        smartMeter.setUtilitiesAdvancedMeteringSystemID(smartMeterId);


        device.setID(deviceId);
        device.setSmartMeter(smartMeter);

        return device;
    }

    private List<EndPointConfiguration> getEndPointConfigurationByIds(List<Long> endPointConfigurationIds) {
        return endPointConfigurationIds.stream()
                .map(id -> endPointConfigurationService.getEndPointConfiguration(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Map<String, UtilitiesDeviceERPSmartMeterRegisteredNotificationCOut> getStateTransitionWebServiceClients() {
        return Collections.unmodifiableMap(this.ports);
    }

    private boolean isValidEndDeviceConfigPortService(UtilitiesDeviceERPSmartMeterRegisteredNotificationCOut port, EndPointConfiguration endPointConfiguration) {
        return endPointConfiguration.getUrl()
                .toLowerCase()
                .contains(((String) ((JaxWsClientProxy) (Proxy.getInvocationHandler(port))).getRequestContext().get(Message.ENDPOINT_ADDRESS)).toLowerCase());
    }

    private BusinessDocumentMessageHeader createMessageHeader(Instant now) {
        String uuid = UUID.randomUUID().toString();

        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
        header.setUUID(createUUID(uuid));
        header.setCreationDateTime(now);
        return header;
    }

    private com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UUID createUUID(String uuid) {
        com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UUID messageUUID
                = objectFactory.createUUID();
        messageUUID.setValue(uuid);
        return messageUUID;
    }
}
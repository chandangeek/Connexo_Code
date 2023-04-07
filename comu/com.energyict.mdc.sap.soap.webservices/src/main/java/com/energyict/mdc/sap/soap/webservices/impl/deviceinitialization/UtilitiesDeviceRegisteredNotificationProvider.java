/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization;

import com.elster.jupiter.fsm.StateTransitionWebServiceClient;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.UtilitiesDeviceRegisteredNotification;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilitiesAdvancedMeteringSystemID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilitiesDeviceERPSmartMeterRegisteredNotificationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilitiesDeviceERPSmartMeterRegisteredNotificationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilsDvceERPSmrtMtrRegedNotifMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilsDvceERPSmrtMtrRegedNotifSmrtMtr;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterednotification.UtilsDvceERPSmrtMtrRegedNotifUtilsDvce;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import javax.inject.Inject;
import javax.xml.ws.Service;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component(name = "com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.UtilitiesDeviceRegisteredNotificationProvider",
        service = {UtilitiesDeviceRegisteredNotification.class, StateTransitionWebServiceClient.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + UtilitiesDeviceRegisteredNotification.NAME})
public class UtilitiesDeviceRegisteredNotificationProvider extends AbstractOutboundEndPointProvider<UtilitiesDeviceERPSmartMeterRegisteredNotificationCOut> implements UtilitiesDeviceRegisteredNotification, StateTransitionWebServiceClient,
        OutboundSoapEndPointProvider, ApplicationSpecific {

    private final ObjectFactory objectFactory = new ObjectFactory();

    private volatile Thesaurus thesaurus;
    private volatile Clock clock;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile DeviceService deviceService;
    private volatile WebServiceActivator webServiceActivator;

    public UtilitiesDeviceRegisteredNotificationProvider() {
        // for OSGI purposes
    }

    @Inject
    public UtilitiesDeviceRegisteredNotificationProvider(Clock clock,
                                                         SAPCustomPropertySets sapCustomPropertySets,
                                                         WebServicesService webServicesService,
                                                         DeviceService deviceService,
                                                         WebServiceActivator webServiceActivator) {
        // for tests
        this();
        setClock(clock);
        setSAPCustomPropertySets(sapCustomPropertySets);
        setWebServicesService(webServicesService);
        setDeviceService(deviceService);
        setWebServiceActivator(webServiceActivator);
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }

    public void unsetWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = null;
    }

    @Reference
    public void setThesaurus(WebServiceActivator webServiceActivator) {
        thesaurus = webServiceActivator.getThesaurus();
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService){
        // No action, just for binding WebServicesService
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

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisteredNotificationCOut port,
                                           Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisteredNotificationCOut port) {
        super.doRemoveEndpoint(port);
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
    protected String getName() {
        return NAME;
    }

    @Override
    public String getWebServiceName() {
        return getName();
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    @Override
    public void call(long endDeviceId, Set<EndPointConfiguration> endPointConfigurations, String state, Instant effectiveDate) {
        deviceService.findDeviceByMeterId(endDeviceId).ifPresent(
                device -> sapCustomPropertySets.getSapDeviceId(device).ifPresent(sapDeviceId -> {
                    if (!sapCustomPropertySets.isRegistered(device) && sapCustomPropertySets.isAnyLrnPresent(device.getId(), effectiveDate)) {
                        call(sapDeviceId, endPointConfigurations);
                    }
                })
        );
    }

    @Override
    public void call(String sapDeviceId) {
        UtilsDvceERPSmrtMtrRegedNotifMsg notificationMessage = createNotificationMessage(sapDeviceId);
        SetMultimap<String, String> values = HashMultimap.create();
        values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), sapDeviceId);

        Set<WebServiceCallOccurrence> sentNotifications = using("utilitiesDeviceERPSmartMeterRegisteredNotificationCOut")
                .withRelatedAttributes(values)
                .send(notificationMessage)
                .keySet();
        if (!sentNotifications.isEmpty()) {
            sapCustomPropertySets.setRegistered(sapDeviceId, true);
        }
    }

    @Override
    public boolean call(String sapDeviceId, Set<EndPointConfiguration> endPointConfigurations) {
        UtilsDvceERPSmrtMtrRegedNotifMsg notificationMessage = createNotificationMessage(sapDeviceId);
        SetMultimap<String, String> values = HashMultimap.create();
        values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), sapDeviceId);

        Set<WebServiceCallOccurrence> sentNotifications = using("utilitiesDeviceERPSmartMeterRegisteredNotificationCOut")
                .toEndpoints(endPointConfigurations)
                .withRelatedAttributes(values)
                .send(notificationMessage)
                .keySet();
        if (!sentNotifications.isEmpty()) {
            sapCustomPropertySets.setRegistered(sapDeviceId, true);
            return true;
        } else {
            return false;
        }
    }

    private UtilsDvceERPSmrtMtrRegedNotifMsg createNotificationMessage(String sapDeviceId) {
        Instant createTime = clock.instant();
        UtilsDvceERPSmrtMtrRegedNotifMsg notificationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegedNotifMsg();
        notificationMessage.setMessageHeader(createMessageHeader(createTime));
        notificationMessage.setUtilitiesDevice(createBody(sapDeviceId, createTime));

        return notificationMessage;
    }

    private UtilsDvceERPSmrtMtrRegedNotifUtilsDvce createBody(String sapDeviceId, Instant now) {
        UtilsDvceERPSmrtMtrRegedNotifUtilsDvce utilsDevice = objectFactory.createUtilsDvceERPSmrtMtrRegedNotifUtilsDvce();
        UtilitiesDeviceID deviceId = objectFactory.createUtilitiesDeviceID();
        deviceId.setValue(sapDeviceId);

        UtilsDvceERPSmrtMtrRegedNotifSmrtMtr smartMeter = objectFactory.createUtilsDvceERPSmrtMtrRegedNotifSmrtMtr();
        UtilitiesAdvancedMeteringSystemID smartMeterId = objectFactory.createUtilitiesAdvancedMeteringSystemID();
        if (webServiceActivator != null) {
            smartMeterId.setValue(webServiceActivator.getMeteringSystemId());
        }
        smartMeter.setUtilitiesAdvancedMeteringSystemID(smartMeterId);
        Optional<Device> device = sapCustomPropertySets.getDevice(sapDeviceId);
        device.flatMap(dev -> sapCustomPropertySets.getStartDate(dev, now)).ifPresent(smartMeter::setStartDate);

        utilsDevice.setID(deviceId);
        utilsDevice.setSmartMeter(smartMeter);

        return utilsDevice;
    }

    private BusinessDocumentMessageHeader createMessageHeader(Instant now) {
        String uuid = UUID.randomUUID().toString();

        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
        header.setUUID(createUUID(uuid));
        if (webServiceActivator != null) {
            header.setSenderBusinessSystemID(webServiceActivator.getMeteringSystemId());
        }
        header.setReconciliationIndicator(true);
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

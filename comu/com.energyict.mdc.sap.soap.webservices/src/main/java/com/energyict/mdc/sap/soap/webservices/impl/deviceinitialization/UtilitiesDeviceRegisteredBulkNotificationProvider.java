/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceRegisteredBulkNotification;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.UtilitiesAdvancedMeteringSystemID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.UtilsDvceERPSmrtMtrRegedBulkNotifMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.UtilsDvceERPSmrtMtrRegedNotifMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.UtilsDvceERPSmrtMtrRegedNotifSmrtMtr;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.UtilsDvceERPSmrtMtrRegedNotifUtilsDvce;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Component(name = "com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.UtilitiesDeviceRegisteredBulkNotificationProvider",
        service = {UtilitiesDeviceRegisteredBulkNotification.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + UtilitiesDeviceRegisteredBulkNotification.NAME})
public class UtilitiesDeviceRegisteredBulkNotificationProvider extends AbstractOutboundEndPointProvider<UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut> implements UtilitiesDeviceRegisteredBulkNotification,
        OutboundSoapEndPointProvider, ApplicationSpecific {

    private final ObjectFactory objectFactory = new ObjectFactory();

    private volatile Clock clock;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private static final List<WebServiceActivator> webServiceActivatorList = new CopyOnWriteArrayList<>();

    public UtilitiesDeviceRegisteredBulkNotificationProvider() {
        // for OSGI purposes
    }

    @Inject
    public UtilitiesDeviceRegisteredBulkNotificationProvider(Clock clock, SAPCustomPropertySets sapCustomPropertySets, WebServiceActivator webServiceActivator) {
        this();
        setClock(clock);
        setSAPCustomPropertySets(sapCustomPropertySets);
        addWebServiceActivator(webServiceActivator);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void addWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivatorList.add(webServiceActivator);
    }

    public void removeWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivatorList.remove(webServiceActivator);
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut port,
                                           Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    public void removeRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut port) {
        super.doRemoveEndpoint(port);
    }

    @Override
    public Service get() {
        return new UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOutService();
    }

    @Override
    public Class<UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut> getService() {
        return UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut.class;
    }

    @Override
    protected String getName() {
        return UtilitiesDeviceRegisteredBulkNotification.NAME;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    @Override
    public void call(List<String> deviceIds) {
        Instant createTime = clock.instant();
        UtilsDvceERPSmrtMtrRegedBulkNotifMsg notificationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegedBulkNotifMsg();
        notificationMessage.setMessageHeader(createMessageHeader(createTime));

        SetMultimap<String, String> values = HashMultimap.create();

        deviceIds.forEach(deviceId -> {
            values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), deviceId);
            notificationMessage.getUtilitiesDeviceERPSmartMeterRegisteredNotificationMessage().add(createChildMessage(deviceId, createTime));
        });

        Set<EndPointConfiguration> processedEndpoints = using("utilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut")
                .withRelatedAttributes(values)
                .send(notificationMessage)
                .keySet();

        if (!processedEndpoints.isEmpty()) {
            deviceIds.forEach(deviceId -> sapCustomPropertySets.setRegistered(deviceId, true));
        }
    }

    private BusinessDocumentMessageHeader createMessageHeader(Instant now) {
        String uuid = UUID.randomUUID().toString();

        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
        header.setUUID(createUUID(uuid));
        Optional<WebServiceActivator> webServiceActivatorOptional = webServiceActivatorList.stream().findAny();
        webServiceActivatorOptional.ifPresent(webServiceActivator -> header.setSenderBusinessSystemID(webServiceActivator.getMeteringSystemId()));
        header.setReconciliationIndicator(true);
        header.setCreationDateTime(now);
        return header;
    }

    private UtilsDvceERPSmrtMtrRegedNotifMsg createChildMessage(String deviceId, Instant now) {

        UtilsDvceERPSmrtMtrRegedNotifMsg notificationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegedNotifMsg();
        notificationMessage.setMessageHeader(createChildHeader(now));
        notificationMessage.setUtilitiesDevice(createChildBody(deviceId, now));
        return notificationMessage;
    }

    private com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.UUID createUUID(String uuid) {
        com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisteredbulknotification.UUID messageUUID
                = objectFactory.createUUID();
        messageUUID.setValue(uuid);
        return messageUUID;
    }

    private BusinessDocumentMessageHeader createChildHeader(Instant now) {
        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();

        header.setUUID(createUUID(UUID.randomUUID().toString()));
        Optional<WebServiceActivator> webServiceActivatorOptional = webServiceActivatorList.stream().findAny();
        webServiceActivatorOptional.ifPresent(webServiceActivator -> header.setSenderBusinessSystemID(webServiceActivator.getMeteringSystemId()));
        header.setReconciliationIndicator(true);
        header.setCreationDateTime(now);
        return header;
    }

    private UtilsDvceERPSmrtMtrRegedNotifUtilsDvce createChildBody(String sapDeviceId, Instant now) {
        UtilitiesDeviceID deviceId = objectFactory.createUtilitiesDeviceID();
        deviceId.setValue(sapDeviceId);

        UtilsDvceERPSmrtMtrRegedNotifSmrtMtr smartMeter = objectFactory.createUtilsDvceERPSmrtMtrRegedNotifSmrtMtr();
        UtilitiesAdvancedMeteringSystemID smartMeterId = objectFactory.createUtilitiesAdvancedMeteringSystemID();
        Optional<WebServiceActivator> webServiceActivatorOptional = webServiceActivatorList.stream().findAny();
        webServiceActivatorOptional.ifPresent(webServiceActivator -> smartMeterId.setValue(webServiceActivator.getMeteringSystemId()));
        smartMeter.setUtilitiesAdvancedMeteringSystemID(smartMeterId);
        Optional<Device> device = sapCustomPropertySets.getDevice(sapDeviceId);
        device.ifPresent(dev -> sapCustomPropertySets.getStartDate(dev, now).ifPresent(smartMeter::setStartDate));

        UtilsDvceERPSmrtMtrRegedNotifUtilsDvce utilsDevice = objectFactory.createUtilsDvceERPSmrtMtrRegedNotifUtilsDvce();
        utilsDevice.setID(deviceId);
        utilsDevice.setSmartMeter(smartMeter);

        return utilsDevice;
    }
}

/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceRegisteredBulkNotification;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceRegisteredNotification;
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

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.xml.ws.Service;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component(name = UtilitiesDeviceRegisteredBulkNotification.SAP_UTILITIES_DEVICE_ERP_SMART_METER_REGISTERED_BULK_NOTIFICATION_C_OUT,
        service = {UtilitiesDeviceRegisteredNotification.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + UtilitiesDeviceRegisteredBulkNotification.SAP_UTILITIES_DEVICE_ERP_SMART_METER_REGISTERED_BULK_NOTIFICATION_C_OUT})
public class UtilitiesDeviceRegisteredBulkNotificationProvider implements UtilitiesDeviceRegisteredBulkNotification,
        OutboundSoapEndPointProvider {

    private volatile  Clock clock;
    private Thesaurus thesaurus;
    private final Map<String, UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut> ports = new HashMap<>();
    private ObjectFactory objectFactory = new ObjectFactory();

    public UtilitiesDeviceRegisteredBulkNotificationProvider() {
        // for OSGI purposes
    }

    @Inject
    public UtilitiesDeviceRegisteredBulkNotificationProvider(Clock clock) {
        this();
        this.clock = clock;
    }

    @Reference
    public void setThesaurus(WebServiceActivator webServiceActivator) {
        thesaurus = webServiceActivator.getThesaurus();
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }


    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut port,
                                           Map<String, Object> properties) {
        Optional.ofNullable(properties)
                .map(property -> property.get(WebServiceActivator.URL_PROPERTY))
                .map(String.class::cast)
                .ifPresent(url -> ports.put(url, port));
    }

    public void removeRequestConfirmationPort(UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut port) {
        ports.values().removeIf(entryPort -> port == entryPort);
    }

    @Override
    public Service get() {
        return new UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOutService();
    }

    @Override
    public Class getService() {
        return UtilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut.class;
    }

    @Override
    public void call(List<String> deviceIds) {
        Instant createTime = clock.instant();
        UtilsDvceERPSmrtMtrRegedBulkNotifMsg notificationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegedBulkNotifMsg();
        notificationMessage.setMessageHeader(createMessageHeader(createTime));

        deviceIds.forEach(deviceId -> {
            notificationMessage.getUtilitiesDeviceERPSmartMeterRegisteredNotificationMessage().add(createChildMessage(deviceId, createTime));
        });

        if (ports.isEmpty()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_WEB_SERVICE_ENDPOINTS);
        }

        ports.values().stream().findFirst().get().utilitiesDeviceERPSmartMeterRegisteredBulkNotificationCOut(notificationMessage);
    }

    private BusinessDocumentMessageHeader createMessageHeader(Instant now) {
        String uuid = UUID.randomUUID().toString();

        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
        header.setUUID(createUUID(uuid));
        header.setCreationDateTime(now);
        return header;
    }

    private UtilsDvceERPSmrtMtrRegedNotifMsg createChildMessage(String deviceId, Instant now) {

        UtilsDvceERPSmrtMtrRegedNotifMsg notificationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegedNotifMsg();
        notificationMessage.setMessageHeader(createChildHeader(now));
        notificationMessage.setUtilitiesDevice(createChildBody(deviceId));
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

        header.setCreationDateTime(now);
        return header;
    }

    private UtilsDvceERPSmrtMtrRegedNotifUtilsDvce createChildBody(String sapDeviceId) {
        UtilitiesDeviceID deviceId = objectFactory.createUtilitiesDeviceID();
        deviceId.setValue(sapDeviceId);

        UtilsDvceERPSmrtMtrRegedNotifSmrtMtr smartMeter = objectFactory.createUtilsDvceERPSmrtMtrRegedNotifSmrtMtr();
        UtilitiesAdvancedMeteringSystemID smartMeterId = objectFactory.createUtilitiesAdvancedMeteringSystemID();
        smartMeterId.setValue(WebServiceActivator.METERING_SYSTEM_ID);
        smartMeter.setUtilitiesAdvancedMeteringSystemID(smartMeterId);

        UtilsDvceERPSmrtMtrRegedNotifUtilsDvce device = objectFactory.createUtilsDvceERPSmrtMtrRegedNotifUtilsDvce();
        device.setID(deviceId);
        device.setSmartMeter(smartMeter);

        return device;
    }

}

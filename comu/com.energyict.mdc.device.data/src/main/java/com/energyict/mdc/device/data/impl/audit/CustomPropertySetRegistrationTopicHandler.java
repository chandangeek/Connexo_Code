/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.EventType;
import com.elster.jupiter.cps.HardCodedFieldNames;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;

/**
 * When a CustomPropertySet is registered on the dashboard (WebServiceServiceImpl), the AppServer is notified
 */
@Component(name = "com.elster.jupiter.cps.registration.eventhandler", service = TopicHandler.class, immediate = true)
public class CustomPropertySetRegistrationTopicHandler implements TopicHandler {

    private volatile OrmService ormService;
    private volatile CustomPropertySetService customPropertySetService;

    public CustomPropertySetRegistrationTopicHandler() {
    }

    @Override
    public void handle(LocalEvent localEvent) {
        try {
            String componentName = (String) localEvent.getSource();
            Optional<RegisteredCustomPropertySet> rcps = customPropertySetService.findActiveCustomPropertySets().stream()
                    .filter(registeredCustomPropertySet -> registeredCustomPropertySet.getCustomPropertySet().getPersistenceSupport().componentName()
                            .compareToIgnoreCase(componentName) == 0)
                    .findFirst()
                    .map(Optional::of)
                            .orElseGet(() -> customPropertySetService.findAllCustomPropertySets().stream()
                                    .filter(registeredCustomPropertySet -> registeredCustomPropertySet.getCustomPropertySet().getPersistenceSupport().componentName()
                                            .compareToIgnoreCase(componentName) == 0).findFirst());


            rcps.ifPresent(registeredCustomPropertySet -> {
                CustomPropertySet cps = registeredCustomPropertySet.getCustomPropertySet();

                if (cps.getDomainClass().equals(Device.class)) {
                    this.ormService.getDataModel(cps.getPersistenceSupport().componentName())
                            .ifPresent(dataModel -> dataModel.getTable(cps.getPersistenceSupport().tableName())
                                    .audit("")
                                    .domainContext(AuditDomainContextType.DEVICE_CUSTOM_ATTRIBUTES.ordinal())
                                    .domainReferenceColumn(cps.getPersistenceSupport().domainFieldName())
                                    .contextReferenceColumn(HardCodedFieldNames.CUSTOM_PROPERTY_SET.databaseName())
                                    .build());
                } else if (cps.getDomainClass().equals(ChannelSpec.class)) {
                    this.ormService.getDataModel(cps.getPersistenceSupport().componentName())
                            .ifPresent(dataModel -> dataModel.getTable(cps.getPersistenceSupport().tableName())
                                    .audit("")
                                    .domainContext(AuditDomainContextType.DEVICE_CHANNEL_CUSTOM_ATTRIBUTES.ordinal())
                                    .domainReferenceColumn("device")
                                    .contextReferenceColumn(cps.getPersistenceSupport().domainFieldName(), HardCodedFieldNames.CUSTOM_PROPERTY_SET.databaseName())
                                    .build());
                } else if (cps.getDomainClass().equals(RegisterSpec.class)) {
                    this.ormService.getDataModel(cps.getPersistenceSupport().componentName())
                            .ifPresent(dataModel -> dataModel.getTable(cps.getPersistenceSupport().tableName())
                                    .audit("")
                                    .domainContext(AuditDomainContextType.DEVICE_REGISTER_CUSTOM_ATTRIBUTES.ordinal())
                                    .domainReferenceColumn("device")
                                    .contextReferenceColumn(cps.getPersistenceSupport().domainFieldName(), HardCodedFieldNames.CUSTOM_PROPERTY_SET.databaseName())
                                    .build());
                } else if (cps.getDomainClass().equals(DeviceProtocolDialectPropertyProvider.class)) {
                    this.ormService.getDataModel(cps.getPersistenceSupport().componentName())
                            .ifPresent(dataModel -> dataModel.getTable(cps.getPersistenceSupport().tableName())
                                    .audit("")
                                    .domainContext(AuditDomainContextType.DEVICE_PROTOCOL_DIALECTS_PROPS.ordinal())
                                    .domainReferences(cps.getPersistenceSupport().domainForeignKeyName(), "FK_DDC_PROTDIALECTPROPS_DEV", "FK_DDC_DEVICE_ENDDEVICE")
                                    .contextReferenceColumn(CommonDeviceProtocolDialectProperties.Fields.DIALECT_PROPERTY_PROVIDER.databaseName() , HardCodedFieldNames.CUSTOM_PROPERTY_SET.databaseName())
                                    .build());
                }
            });
        }
        catch(Exception exception){
        }
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    public String getTopicMatcher() {
        return EventType.CUSTOM_PROPERTY_SET_REGISTERED.topic();
    }
}

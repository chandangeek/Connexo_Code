/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.EventType;
import com.elster.jupiter.cps.HardCodedFieldNames;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.OrmService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;

/**
 * When a CustomPropertySet is registered, the audit trails is notified
 */
@Component(name = "com.elster.jupiter.metering.impl.audit", service = TopicHandler.class, immediate = true)
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
                    .filter(registeredCustomPropertySet -> registeredCustomPropertySet.getCustomPropertySet().getPersistenceSupport().componentName().equals(componentName))
                    .findFirst()
                    .map(Optional::of)
                            .orElseGet(() -> customPropertySetService.findAllCustomPropertySets().stream()
                                    .filter(registeredCustomPropertySet -> registeredCustomPropertySet.getCustomPropertySet().getPersistenceSupport().componentName().equals(componentName))
                                    .findFirst());


            rcps.ifPresent(registeredCustomPropertySet -> {
                CustomPropertySet cps = registeredCustomPropertySet.getCustomPropertySet();

                if (cps.getDomainClass().equals(UsagePoint.class)) {
                    this.ormService.getDataModel(cps.getPersistenceSupport().componentName())
                            .ifPresent(dataModel -> dataModel.getTable(cps.getPersistenceSupport().tableName())
                                    .audit("")
                                    .domainContext(AuditDomainContextType.USAGEPOINT_CUSTOM_ATTRIBUTES.ordinal())
                                    .domainReferences(cps.getPersistenceSupport().domainForeignKeyName())
                                    .contextReferenceColumn(HardCodedFieldNames.CUSTOM_PROPERTY_SET.databaseName())
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

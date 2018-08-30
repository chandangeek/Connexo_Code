/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl.webservicecall;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.impl.MessageSeeds;
import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.export.webservicecall.ServiceCallStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.NoTransitionException;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import java.util.Optional;

public class DataExportServiceCallTypeImpl implements DataExportServiceCallType {
    // TODO: no way to make names of service call types translatable
    private static final String NAME = TranslationKeys.SERVICE_CALL_TYPE_NAME.getDefaultFormat();
    private static final String VERSION = "1.0";

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;
    private final Object serviceCallLock = new Object();

    @Inject
    public DataExportServiceCallTypeImpl(OrmService ormService, Thesaurus thesaurus, ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService) {
        this.dataModel = ormService.getDataModel(WebServiceDataExportPersistenceSupport.COMPONENT_NAME)
                .orElseThrow(() -> new IllegalStateException("Data model for web service data export CPS isn't found."));
        this.thesaurus = thesaurus;
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
    }

    public ServiceCallType findOrCreate() {
        return serviceCallService.findServiceCallType(NAME, VERSION).orElseGet(() -> {
            RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(WebServiceDataExportCustomPropertySet.CUSTOM_PROPERTY_SET_ID)
                    .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.NO_CPS_FOUND).format(WebServiceDataExportCustomPropertySet.CUSTOM_PROPERTY_SET_ID)));

            return serviceCallService.createServiceCallType(NAME, VERSION)
                    .handler(WebServiceDataExportServiceCallHandler.NAME)
                    .logLevel(LogLevel.FINEST)
                    .customPropertySet(registeredCustomPropertySet)
                    .create();
        });
    }

    @Override
    public ServiceCall startServiceCall(String uuid, long timeout) {
        WebServiceDataExportDomainExtension serviceCallProperties = new WebServiceDataExportDomainExtension();
        serviceCallProperties.setUuid(uuid);
        serviceCallProperties.setTimeout(timeout);
        ServiceCall serviceCall = findOrCreate().newServiceCall()
                .origin(WebServiceDataExportPersistenceSupport.APPLICATION_NAME)
                .extendedWith(serviceCallProperties)
                .create();
        serviceCall.requestTransition(DefaultState.PENDING);
        return serviceCall;
    }

    @Override
    public Optional<ServiceCall> findServiceCall(String uuid) {
        return dataModel.stream(WebServiceDataExportDomainExtension.class)
                .join(ServiceCall.class)
                .filter(Where.where(WebServiceDataExportDomainExtension.FieldNames.UUID.javaName()).isEqualTo(uuid))
                .findAny()
                .map(WebServiceDataExportDomainExtension::getServiceCall);
    }

    @Override
    public void tryFailingServiceCall(ServiceCall serviceCall, String errorMessage) {
        try {
            synchronized (serviceCallLock) {
                serviceCall.requestTransition(DefaultState.FAILED);
                serviceCall.getExtension(WebServiceDataExportDomainExtension.class)
                        .ifPresent(serviceCallProperties -> {
                            serviceCallProperties.setErrorMessage(errorMessage);
                            Save.UPDATE.save(dataModel, serviceCallProperties);
                        });
            }
        } catch (NoTransitionException e) {
            // not intended to do anything if the service call is already closed
        }
    }

    @Override
    public void tryPassingServiceCall(ServiceCall serviceCall) {
        try {
            synchronized (serviceCallLock) {
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            }
        } catch (NoTransitionException e) {
            // not intended to do anything if the service call is already closed
        }
    }

    @Override
    public ServiceCallStatus getStatus(ServiceCall serviceCall) {
        synchronized (serviceCallLock) {
            return new ServiceCallStatusImpl(serviceCallService, serviceCall);
        }
    }

}

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
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.NoTransitionException;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DataExportServiceCallTypeImpl implements DataExportServiceCallType {
    // TODO: no way to make names of service call types translatable
    private static final String NAME = TranslationKeys.SERVICE_CALL_TYPE_NAME.getDefaultFormat();
    private static final String VERSION = "1.0";

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;
    private final TransactionService transactionService;
    private final ThreadPrincipalService threadPrincipalService;

    @Inject
    public DataExportServiceCallTypeImpl(OrmService ormService, Thesaurus thesaurus, ServiceCallService serviceCallService,
                                         CustomPropertySetService customPropertySetService, TransactionService transactionService,
                                         ThreadPrincipalService threadPrincipalService) {
        this.dataModel = ormService.getDataModel(WebServiceDataExportPersistenceSupport.COMPONENT_NAME)
                .orElseThrow(() -> new IllegalStateException("Data model for web service data export CPS isn't found."));
        this.thesaurus = thesaurus;
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
        this.transactionService = transactionService;
        this.threadPrincipalService = threadPrincipalService;
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
        if (transactionService.isInTransaction()) {
            return doStartServiceCall(uuid, timeout);
        } else {
            return startServiceCallInTransaction(uuid, timeout);
        }
    }

    @Override
    public ServiceCall startServiceCallAsync(String uuid, long timeout) {
        Principal principal = threadPrincipalService.getPrincipal();
        try {
            return CompletableFuture.supplyAsync(() -> {
                threadPrincipalService.set(principal);
                return startServiceCallInTransaction(uuid, timeout);
            }).get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private ServiceCall startServiceCallInTransaction(String uuid, long timeout) {
        return transactionService.execute(() -> doStartServiceCall(uuid, timeout));
    }

    private ServiceCall doStartServiceCall(String uuid, long timeout) {
        WebServiceDataExportDomainExtension serviceCallProperties = new WebServiceDataExportDomainExtension();
        serviceCallProperties.setUuid(uuid);
        serviceCallProperties.setTimeout(timeout);
        ServiceCallType serviceCallType = findOrCreate();
        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .origin(WebServiceDataExportPersistenceSupport.APPLICATION_NAME)
                .extendedWith(serviceCallProperties)
                .create();
        serviceCall.requestTransition(DefaultState.PENDING);
        serviceCall.requestTransition(DefaultState.ONGOING);
        return serviceCall;
    }

    @Override
    public Optional<ServiceCall> findServiceCall(String uuid) {
        return dataModel.stream(WebServiceDataExportDomainExtension.class)
                .join(ServiceCall.class)
                .filter(Where.where(WebServiceDataExportDomainExtension.FieldNames.UUID.javaName()).isEqualToIgnoreCase(uuid))
                .findAny()
                .map(WebServiceDataExportDomainExtension::getServiceCall);
    }

    @Override
    public ServiceCallStatus tryFailingServiceCall(ServiceCall serviceCall, String errorMessage) {
        if (transactionService.isInTransaction()) {
            return doTryFailingServiceCall(serviceCall, errorMessage);
        } else {
            return transactionService.execute(() -> doTryFailingServiceCall(serviceCall, errorMessage));
        }
    }

    private ServiceCallStatus doTryFailingServiceCall(ServiceCall serviceCall, String errorMessage) {
        try {
            serviceCall = lock(serviceCall);
            serviceCall.requestTransition(DefaultState.FAILED);
            serviceCall.getExtension(WebServiceDataExportDomainExtension.class)
                    .ifPresent(serviceCallProperties -> {
                        serviceCallProperties.setErrorMessage(errorMessage);
                        Save.UPDATE.save(dataModel, serviceCallProperties);
                    });
            return new ServiceCallStatusImpl(serviceCall, DefaultState.FAILED, errorMessage);
        } catch (NoTransitionException e) {
            // not intended to do anything if the service call is already closed
            return new ServiceCallStatusImpl(serviceCallService, serviceCall);
        }
    }

    @Override
    public ServiceCallStatus tryPassingServiceCall(ServiceCall serviceCall) {
        if (transactionService.isInTransaction()) {
            return doTryPassingServiceCall(serviceCall);
        } else {
            return transactionService.execute(() -> doTryPassingServiceCall(serviceCall));
        }
    }

    private ServiceCallStatus doTryPassingServiceCall(ServiceCall serviceCall) {
        try {
            serviceCall = lock(serviceCall);
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            return new ServiceCallStatusImpl(serviceCall, DefaultState.SUCCESSFUL, null);
        } catch (NoTransitionException e) {
            // not intended to do anything if the service call is already closed
            return new ServiceCallStatusImpl(serviceCallService, serviceCall);
        }
    }

    @Override
    public ServiceCallStatus getStatus(ServiceCall serviceCall) {
        return new ServiceCallStatusImpl(serviceCallService, serviceCall);
    }

    private ServiceCall lock(ServiceCall serviceCall) {
        return serviceCallService.lockServiceCall(serviceCall.getId())
                .orElseThrow(() -> new IllegalStateException("Service call " + serviceCall.getNumber() + " disappeared."));
    }
}

/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl.webservicecall;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
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
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.conditions.Where;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class DataExportServiceCallTypeImpl implements DataExportServiceCallType {
    // TODO: no way to make names of service call types translatable
    private static final String NAME = TranslationKeys.SERVICE_CALL_TYPE_NAME.getDefaultFormat();
    private static final String VERSION = "1.0";
    private static final String CHILD_NAME = TranslationKeys.SERVICE_CALL_TYPE_CHILD_NAME.getDefaultFormat();
    private static final String CHILD_VERSION = "1.0";
    private static final String APPLICATION = null;

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;
    private final TransactionService transactionService;
    private final ThreadPrincipalService threadPrincipalService;
    private final OrmService ormService;

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
        this.ormService = ormService;
    }

    public ServiceCallType findOrCreate() {
        return serviceCallService.findServiceCallType(NAME, VERSION).orElseGet(() -> {
            RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(WebServiceDataExportCustomPropertySet.CUSTOM_PROPERTY_SET_ID)
                    .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.NO_CPS_FOUND).format(WebServiceDataExportCustomPropertySet.CUSTOM_PROPERTY_SET_ID)));

            return serviceCallService.createServiceCallType(NAME, VERSION, APPLICATION)
                    .handler(WebServiceDataExportServiceCallHandler.NAME)
                    .logLevel(LogLevel.FINEST)
                    .customPropertySet(registeredCustomPropertySet)
                    .create();
        });
    }

    private ServiceCallType findOrCreateChildType() {
        return serviceCallService.findServiceCallType(CHILD_NAME, CHILD_VERSION).orElseGet(() -> {
            RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(WebServiceDataExportChildCustomPropertySet.CUSTOM_PROPERTY_SET_CHILD_ID)
                    .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.NO_CPS_FOUND).format(WebServiceDataExportChildCustomPropertySet.CUSTOM_PROPERTY_SET_CHILD_ID)));

            serviceCallService.addServiceCallHandler(ServiceCallHandler.DUMMY, ImmutableMap.of("name", CHILD_NAME));
            return serviceCallService.createServiceCallType(CHILD_NAME, CHILD_VERSION, APPLICATION)
                    .handler(CHILD_NAME)
                    .logLevel(LogLevel.FINEST)
                    .customPropertySet(registeredCustomPropertySet)
                    .create();
        });
    }

    @Override
    public ServiceCall startServiceCall(String uuid, long timeout, List<ReadingTypeDataExportItem> data) {
        if (transactionService.isInTransaction()) {
            return doStartServiceCall(uuid, timeout, data);
        } else {
            return startServiceCallInTransaction(uuid, timeout, data);
        }
    }

    @Override
    public ServiceCall startServiceCallAsync(String uuid, long timeout, List<ReadingTypeDataExportItem> data) {
        Principal principal = threadPrincipalService.getPrincipal();
        try {
            return CompletableFuture.supplyAsync(() -> {
                threadPrincipalService.set(principal);
                return startServiceCallInTransaction(uuid, timeout, data);
            }).get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void createChildServiceCalls(ServiceCall parent, List<ReadingTypeDataExportItem> data){
        data.forEach(item->createChild(parent,
                item.getDomainObject().getName(),
                item.getReadingType().getMRID()));
    }


    private void createChild(ServiceCall parent, String deviceName, String readingTypeMrID){
        WebServiceDataExportChildDomainExtension childSrvCallProperties = new WebServiceDataExportChildDomainExtension();
        childSrvCallProperties.setDeviceName(deviceName);
        childSrvCallProperties.setReadingTypeMRID(readingTypeMrID);

        ServiceCallType srvCallChildType = findOrCreateChildType();

        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(srvCallChildType)
                .extendedWith(childSrvCallProperties);
        ServiceCall child = serviceCallBuilder.create();
        child.requestTransition(DefaultState.PENDING);
        child.requestTransition(DefaultState.ONGOING);
        child.requestTransition(DefaultState.SUCCESSFUL);
    }


    private ServiceCall startServiceCallInTransaction(String uuid, long timeout, List<ReadingTypeDataExportItem>  data) {
        return transactionService.execute(() -> doStartServiceCall(uuid, timeout, data));
    }

    private ServiceCall doStartServiceCall(String uuid, long timeout, List<ReadingTypeDataExportItem> data) {
        WebServiceDataExportDomainExtension serviceCallProperties = new WebServiceDataExportDomainExtension(thesaurus);
        serviceCallProperties.setUuid(uuid);
        serviceCallProperties.setTimeout(timeout);
        ServiceCallType serviceCallType = findOrCreate();
        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .origin(WebServiceDataExportPersistenceSupport.APPLICATION_NAME)
                .extendedWith(serviceCallProperties)
                .create();
        serviceCall.requestTransition(DefaultState.PENDING);
        serviceCall.requestTransition(DefaultState.ONGOING);
        createChildServiceCalls(serviceCall, data);

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

    @Override
    public List<ServiceCallStatus> getStatuses(Collection<ServiceCall> serviceCalls) {
        return ServiceCallStatusImpl.from(serviceCallService, serviceCalls);
    }

    private ServiceCall lock(ServiceCall serviceCall) {
        return serviceCallService.lockServiceCall(serviceCall.getId())
                .orElseThrow(() -> new IllegalStateException("Service call " + serviceCall.getNumber() + " disappeared."));
    }

    @Override
    public Set<ReadingTypeDataExportItem> getDataSources(ServiceCall serviceCall) {
        Subquery dataSourceIds = ormService.getDataModel(WebServiceDataExportChildPersistentSupport.COMPONENT_NAME)
                .orElseThrow(() -> new IllegalStateException("Data model for web service data export child CPS isn't found."))
                .query(WebServiceDataExportChildDomainExtension.class, ServiceCall.class)
                // TODO: update id
                .asSubquery(Where.where("serviceCall.parent").isEqualTo(serviceCall), "dataSourceId");
        return ormService.getDataModel(DataExportService.COMPONENTNAME)
                .orElseThrow(() -> new IllegalStateException("Data model for data export service isn't found."))
                .stream(ReadingTypeDataExportItem.class)
                .filter(ListOperator.IN.contains(dataSourceIds, "id"))
                .collect(Collectors.toSet());
    }
}

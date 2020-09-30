/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl.webservicecall;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.impl.MessageSeeds;
import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.export.webservicecall.ServiceCallStatus;
import com.elster.jupiter.fsm.State;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    public ServiceCallType findOrCreateParentType() {
        serviceCallService.addServiceCallHandler(ServiceCallHandler.DUMMY, ImmutableMap.of("name", HANDLER_NAME));
        return serviceCallService.findServiceCallType(NAME, VERSION).orElseGet(() -> {
            RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(WebServiceDataExportCustomPropertySet.CUSTOM_PROPERTY_SET_ID)
                    .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.NO_CPS_FOUND).format(WebServiceDataExportCustomPropertySet.CUSTOM_PROPERTY_SET_ID)));

            return serviceCallService.createServiceCallType(NAME, VERSION, APPLICATION)
                    .handler(HANDLER_NAME)
                    .logLevel(LogLevel.FINEST)
                    .customPropertySet(registeredCustomPropertySet)
                    .create();
        });
    }

    public ServiceCallType findOrCreateChildType() {
        serviceCallService.addServiceCallHandler(ServiceCallHandler.DUMMY, ImmutableMap.of("name", CHILD_NAME));
        return serviceCallService.findServiceCallType(CHILD_NAME, CHILD_VERSION).orElseGet(() -> {
            RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(WebServiceDataExportChildCustomPropertySet.CUSTOM_PROPERTY_SET_CHILD_ID)
                    .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.NO_CPS_FOUND).format(WebServiceDataExportChildCustomPropertySet.CUSTOM_PROPERTY_SET_CHILD_ID)));

            return serviceCallService.createServiceCallType(CHILD_NAME, CHILD_VERSION, APPLICATION)
                    .handler(CHILD_HANDLER_NAME)
                    .logLevel(LogLevel.FINEST)
                    .customPropertySet(registeredCustomPropertySet)
                    .create();
        });
    }

    @Override
    public ServiceCall startServiceCall(String uuid, long timeout, Map<ReadingTypeDataExportItem, String> data) {
        if (transactionService.isInTransaction()) {
            return doStartServiceCall(uuid, timeout, data);
        } else {
            return startServiceCallInTransaction(uuid, timeout, data);
        }
    }

    @Override
    public ServiceCall startServiceCallAsync(String uuid, long timeout, Map<ReadingTypeDataExportItem, String> data) {
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

    private void createChildServiceCalls(ServiceCall parent, Map<ReadingTypeDataExportItem, String> data) {
        data.forEach((item, customInfo) -> createChild(parent,
                item.getDomainObject(),
                item.getReadingType().getMRID(),
                item.getId(),
                customInfo));
    }

    private void createChild(ServiceCall parent, IdentifiedObject object, String readingTypeMrID, long itemId, String customInfo) {
        WebServiceDataExportChildDomainExtension childSrvCallProperties = new WebServiceDataExportChildDomainExtension();
        childSrvCallProperties.setDeviceName(object.getName());
        childSrvCallProperties.setReadingTypeMRID(readingTypeMrID);
        childSrvCallProperties.setDataSourceId(itemId);
        childSrvCallProperties.setCustomInfo(customInfo);

        ServiceCallType srvCallChildType = findOrCreateChildType();

        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(srvCallChildType)
                .extendedWith(childSrvCallProperties);
        serviceCallBuilder.targetObject(object);
        ServiceCall child = serviceCallBuilder.create();
        child.requestTransition(DefaultState.PENDING);
        child.requestTransition(DefaultState.ONGOING);
    }


    private ServiceCall startServiceCallInTransaction(String uuid, long timeout, Map<ReadingTypeDataExportItem, String> data) {
        return transactionService.execute(() -> doStartServiceCall(uuid, timeout, data));
    }

    private ServiceCall doStartServiceCall(String uuid, long timeout, Map<ReadingTypeDataExportItem, String> data) {
        WebServiceDataExportDomainExtension serviceCallProperties = new WebServiceDataExportDomainExtension(thesaurus);
        serviceCallProperties.setUuid(uuid);
        serviceCallProperties.setTimeout(timeout);
        ServiceCallType serviceCallType = findOrCreateParentType();
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
    public List<ServiceCall> findServiceCalls(EnumSet<DefaultState> states) {
        List<String> stateKeys = states.stream().map(DefaultState::getKey).collect(Collectors.toList());
        return dataModel.stream(WebServiceDataExportDomainExtension.class)
                .join(ServiceCall.class)
                .join(State.class)
                .filter(Where.where(WebServiceDataExportDomainExtension.FieldNames.DOMAIN.javaName() + ".state.name").in(stateKeys))
                .map(WebServiceDataExportDomainExtension::getServiceCall)
                .collect(Collectors.toList());
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
            serviceCall.findChildren().stream().forEach(child -> {
                try {
                    child = lock(child);
                    child.requestTransition(DefaultState.FAILED);
                } catch (NoTransitionException e) {
                    // not intended to do anything if the service call is already closed
                }
            });

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

    @Override
    public ServiceCallStatus tryPartiallyPassingServiceCall(ServiceCall serviceCall, Collection<ServiceCall> successfulChildren, String errorMessage) {
        if (transactionService.isInTransaction()) {
            return doTryPartiallyPassingServiceCall(serviceCall, successfulChildren, errorMessage);
        } else {
            return transactionService.execute(() -> doTryPartiallyPassingServiceCall(serviceCall, successfulChildren, errorMessage));
        }
    }

    private ServiceCallStatus doTryPassingServiceCall(ServiceCall serviceCall) {
        try {
            serviceCall = lock(serviceCall);
            serviceCall.findChildren().stream().forEach(child -> {
                try {
                    child = lock(child);
                    child.requestTransition(DefaultState.SUCCESSFUL);
                } catch (NoTransitionException e) {
                    // not intended to do anything if the service call is already closed
                }
            });

            serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            return new ServiceCallStatusImpl(serviceCall, DefaultState.SUCCESSFUL, null);
        } catch (NoTransitionException e) {
            // not intended to do anything if the service call is already closed
            return new ServiceCallStatusImpl(serviceCallService, serviceCall);
        }
    }

    private ServiceCallStatus doTryPartiallyPassingServiceCall(ServiceCall serviceCall, Collection<ServiceCall> successfulChildren, String errorMessage) {
        try {
            serviceCall = lock(serviceCall);
            serviceCall.findChildren().stream().forEach(child -> {
                try {
                    if (successfulChildren.contains(child)) {
                        child.transitionWithLockIfPossible(DefaultState.SUCCESSFUL);
                    } else {
                        child.transitionWithLockIfPossible(DefaultState.FAILED);
                    }
                } catch (NoTransitionException e) {
                    // not intended to do anything if the service call is already closed
                }
            });

            List<ServiceCall> children = findChildren(serviceCall);
            if (hasAllChildrenInState(children, DefaultState.SUCCESSFUL)) {
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else if (hasAnyChildState(children, DefaultState.SUCCESSFUL)) {
                serviceCall.requestTransition(DefaultState.PARTIAL_SUCCESS);
            } else {
                serviceCall.requestTransition(DefaultState.FAILED);
            }
            serviceCall.getExtension(WebServiceDataExportDomainExtension.class)
                    .ifPresent(serviceCallProperties -> {
                        serviceCallProperties.setErrorMessage(errorMessage);
                        Save.UPDATE.save(dataModel, serviceCallProperties);
                    });
            return new ServiceCallStatusImpl(serviceCall, DefaultState.PARTIAL_SUCCESS, errorMessage);
        } catch (NoTransitionException e) {
            // not intended to do anything if the service call is already closed
            return new ServiceCallStatusImpl(serviceCallService, serviceCall);
        }

    }

    private boolean hasAllChildrenInState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().allMatch(sc -> sc.getState().equals(defaultState));
    }

    private boolean hasAnyChildState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().anyMatch(sc -> sc.getState().equals(defaultState));
    }

    private List<ServiceCall> findChildren(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().collect(Collectors.toList());
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
    public Set<ReadingTypeDataExportItem> getDataSources(Collection<ServiceCall> childServiceCalls) {
        return doGetDataSources(new ArrayList<>(childServiceCalls));
    }

    @Override
    public String getCustomInfoFromChildServiceCall(ServiceCall serviceCall) {
        WebServiceDataExportChildDomainExtension extension = serviceCall.getExtension(WebServiceDataExportChildDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Couldn't find domain extension for child service call"));
        return extension.getCustomInfo();
    }

    private Set<ReadingTypeDataExportItem> doGetDataSources(List<ServiceCall> childServiceCalls) {
        if (childServiceCalls.isEmpty()) {
            return new HashSet<>();
        }
        Subquery dataSourceIds = ormService.getDataModel(WebServiceDataExportChildPersistentSupport.COMPONENT_NAME)
                .orElseThrow(() -> new IllegalStateException("Data model for web service data export child CPS isn't found."))
                .query(WebServiceDataExportChildDomainExtension.class, ServiceCall.class)
                .asSubquery(Where.where("serviceCall").in(childServiceCalls),
                        WebServiceDataExportChildDomainExtension.FieldNames.DATA_SOURCE_ID.javaName());
        return ormService.getDataModel(DataExportService.COMPONENTNAME)
                .orElseThrow(() -> new IllegalStateException("Data model for data export service isn't found."))
                .stream(ReadingTypeDataExportItem.class)
                .filter(ListOperator.IN.contains(dataSourceIds, "id"))
                .collect(Collectors.toSet());
    }
}

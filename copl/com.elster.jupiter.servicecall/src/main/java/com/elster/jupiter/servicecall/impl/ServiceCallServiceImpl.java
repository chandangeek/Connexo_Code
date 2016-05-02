package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.MissingHandlerNameException;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallLifeCycleBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.servicecall.ServiceCallTypeBuilder;
import com.elster.jupiter.servicecall.security.Privileges;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.servicecall",
        service = {ServiceCallService.class, MessageSeedProvider.class, TranslationKeyProvider.class, PrivilegesProvider.class},
        property = "name=" + ServiceCallService.COMPONENT_NAME,
        immediate = true)
@LiteralSql
public class ServiceCallServiceImpl implements IServiceCallService, MessageSeedProvider, TranslationKeyProvider, PrivilegesProvider {

    static final String SERIVCE_CALLS_DESTINATION_NAME = "SerivceCalls";
    static final String SERIVCE_CALLS_SUBSCRIBER_NAME = "SerivceCalls";
    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile MessageService messageService;
    private volatile JsonService jsonService;
    private final Map<String, ServiceCallHandler> handlerMap = new ConcurrentHashMap<>();
    private volatile UserService userService;
    private volatile UpgradeService upgradeService;

    // OSGi
    public ServiceCallServiceImpl() {
    }

    @Inject
    public ServiceCallServiceImpl(FiniteStateMachineService finiteStateMachineService, OrmService ormService, NlsService nlsService, UserService userService, CustomPropertySetService customPropertySetService, MessageService messageService, JsonService jsonService, UpgradeService upgradeService) {
        setFiniteStateMachineService(finiteStateMachineService);
        setOrmService(ormService);
        setNlsService(nlsService);
        setUserService(userService);
        setMessageService(messageService);
        setJsonService(jsonService);
        setCustomPropertySetService(customPropertySetService);
        setUpgradeService(upgradeService);
        activate();
    }

    @Reference
    public void setFiniteStateMachineService(FiniteStateMachineService finiteStateMachineService) {
        this.finiteStateMachineService = finiteStateMachineService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(ServiceCallService.COMPONENT_NAME, "Service calls");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(this.dataModel);
        }
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(ServiceCallService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    @Override
    public void addServiceCallHandler(ServiceCallHandler serviceCallHandler, Map<String, Object> properties) {
        String name = (String) properties.get("name");
        if (Checks.is(name).emptyOrOnlyWhiteSpace()) {
            throw new MissingHandlerNameException(thesaurus, MessageSeeds.NO_NAME_FOR_HANDLER, serviceCallHandler);
        }
        handlerMap.put(name, serviceCallHandler);
    }

    @Override
    public void removeServiceCallHandler(ServiceCallHandler serviceCallHandler, Map<String, Object> properties) {
        String name = (String) properties.get("name");
        handlerMap.remove(name);
    }

    @Override
    public String getComponentName() {
        return ServiceCallService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public String getModuleName() {
        return ServiceCallService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_SERVICE_CALL_TYPES.getKey(), Privileges.RESOURCE_SERVICE_CALL_TYPES_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.ADMINISTRATE_SERVICE_CALL_TYPES, Privileges.Constants.VIEW_SERVICE_CALL_TYPES)));
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_SERVICE_CALL.getKey(), Privileges.RESOURCE_SERVICE_CALL_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.VIEW_SERVICE_CALLS, Privileges.Constants.CHANGE_SERVICE_CALL_STATE)));
        return resources;
    }

    @Override
    public Optional<ServiceCallHandler> findHandler(String handler) {
        if (Checks.is(handler).emptyOrOnlyWhiteSpace()) {
            return Optional.empty();
        }
        return Optional.ofNullable(handlerMap.get(handler));
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(FiniteStateMachineService.class).toInstance(finiteStateMachineService);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                bind(ServiceCallService.class).toInstance(ServiceCallServiceImpl.this);
                bind(IServiceCallService.class).toInstance(ServiceCallServiceImpl.this);
                bind(JsonService.class).toInstance(jsonService);
                bind(MessageService.class).toInstance(messageService);
            }
        };
    }

    @Activate
    public void activate() {
        dataModel.register(getModule());
        upgradeService.register(InstallIdentifier.identifier(COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());
    }

    @Override
    public Finder<ServiceCallLifeCycle> getServiceCallLifeCycles() {
        return DefaultFinder.of(ServiceCallLifeCycle.class, dataModel)
                .defaultSortColumn(ServiceCallLifeCycleImpl.Fields.name.fieldName());
    }

    @Override
    public Optional<ServiceCallLifeCycle> getServiceCallLifeCycle(String name) {
        return dataModel.mapper(ServiceCallLifeCycle.class)
                .getUnique(ServiceCallLifeCycleImpl.Fields.name.fieldName(), name);
    }

    @Override
    public Optional<ServiceCallLifeCycle> getDefaultServiceCallLifeCycle() {
        return dataModel.mapper(ServiceCallLifeCycle.class)
                .getUnique(ServiceCallLifeCycleImpl.Fields.name.fieldName(), TranslationKeys.DEFAULT_SERVICE_CALL_LIFE_CYCLE_NAME
                        .getKey());
    }

    @Override
    public Finder<ServiceCallType> getServiceCallTypes() {
        return DefaultFinder.of(ServiceCallType.class, dataModel)
                .sorted(ServiceCallTypeImpl.Fields.name.fieldName(), true)
                .sorted(ServiceCallTypeImpl.Fields.version.fieldName(), true);
    }

    @Override
    public ServiceCallTypeBuilder createServiceCallType(String name, String versionName, ServiceCallLifeCycle serviceCallLifeCycle) {
        return new ServiceCallTypeBuilderImpl(this, name, versionName, (IServiceCallLifeCycle) serviceCallLifeCycle, dataModel, thesaurus);
    }

    @Override
    public Optional<ServiceCallType> findServiceCallType(String name, String versionName) {
        return dataModel.mapper(IServiceCallType.class)
                .getUnique(ServiceCallTypeImpl.Fields.name.fieldName(), name, ServiceCallTypeImpl.Fields.versionName.fieldName(), versionName)
                .map(ServiceCallType.class::cast);
    }


    @Override
    public Optional<ServiceCallType> findAndLockServiceCallType(long id, long version) {
        return dataModel.mapper(IServiceCallType.class)
                .lockObjectIfVersion(version, id)
                .map(ServiceCallType.class::cast);
    }

    @Override
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Override
    public ServiceCallLifeCycleBuilder createServiceCallLifeCycle(String name) {
        return dataModel.getInstance(ServiceCallLifeCycleBuilderImpl.class).setName(name);
    }

    @Override
    public Collection<String> findAllHandlers() {
        return handlerMap.keySet();
    }

    @Override
    public Optional<ServiceCall> getServiceCall(long id) {
        return dataModel.mapper(ServiceCall.class).getOptional(id);
    }

    @Override
    public Finder<ServiceCall> getServiceCallFinder(ServiceCallFilter filter) {
         return DefaultFinder.of(ServiceCall.class, createConditionFromFilter(filter), dataModel, ServiceCallType.class, State.class)
                .sorted("sign(nvl(" + ServiceCallImpl.Fields.parent.fieldName() + ", 0))", true)
                .sorted(ServiceCallImpl.Fields.modTime.fieldName(), false);
    }

    @Override
    public Finder<ServiceCall> getServiceCallFinder() {
        return DefaultFinder.of(ServiceCall.class, dataModel)
                .sorted("sign(nvl(" + ServiceCallImpl.Fields.parent.fieldName() + ", 0))", true)
                .sorted(ServiceCallImpl.Fields.modTime.fieldName(), false);
    }

    @Override
    public Map<DefaultState, Long> getChildrenStatus(long id) {
        HashMap<DefaultState, Long> childrenStateCount = new HashMap<>();
        SqlBuilder sqlBuilder = new SqlBuilder();

        sqlBuilder.append("SELECT fsm.NAME, scs.TOTAL FROM FSM_STATE fsm, ");
        sqlBuilder.append("(SELECT STATE, COUNT(*) TOTAL FROM SCS_SERVICE_CALL ");
        sqlBuilder.append("WHERE id IN (SELECT id FROM " + TableSpecs.SCS_SERVICE_CALL  +" where parent=");
        sqlBuilder.append(id + ") ");
        sqlBuilder.append("GROUP BY STATE) scs ");
        sqlBuilder.append("WHERE fsm.ID = scs.STATE");

        try (Connection connection = dataModel.getConnection(false)) {
            try (PreparedStatement statement = sqlBuilder.prepare(connection)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        childrenStateCount.put(DefaultState.from(resultSet.getString(1))
                                .get(), resultSet.getLong(2));
                    }
                }
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
        return childrenStateCount;
    }

    @Override
    public String getDisplayName(DefaultState state) {
        return state.getDisplayName(thesaurus);
    }

    @Override
    public DestinationSpec getServiceCallQueue() {
        if (!dataModel.isInstalled()) {
            throw new IllegalStateException();
        }
        return messageService.getDestinationSpec(SERIVCE_CALLS_DESTINATION_NAME).get();
    }

    @Override
    public Set<ServiceCall> findServiceCalls(Object targetObject, Set<DefaultState> inState) {

        List<String> stateKeys = inState.stream()
                .map(DefaultState::getKey)
                .collect(Collectors.toList());

        return dataModel.stream(ServiceCall.class)
                .join(State.class)
                .filter(Where.where(ServiceCallImpl.Fields.state.fieldName() + ".name").in(stateKeys))
                .filter(serviceCall -> serviceCall.getTargetObject().map(targetObject::equals).orElse(false))
                .collect(Collectors.toSet());
    }

    @Override
    public void cancelServiceCallsFor(Object target) {
        EnumSet<DefaultState> states = EnumSet.allOf(DefaultState.class);
        states.remove(DefaultState.CREATED);
        states.remove(DefaultState.CANCELLED);
        states.remove(DefaultState.FAILED);
        states.remove(DefaultState.SUCCESSFUL);
        states.remove(DefaultState.PARTIAL_SUCCESS);
        states.remove(DefaultState.REJECTED);
        findServiceCalls(target, states)
                .stream()
                .forEach(ServiceCall::cancel);
    }

    private Condition createConditionFromFilter(ServiceCallFilter filter) {
        Condition condition = Condition.TRUE;

        if (filter.getReference() != null) {
            condition = condition.and(where(ServiceCallImpl.Fields.externalReference.fieldName()).like(filter.getReference()).or(where("internalReference").like(filter.getReference())));
        }
        if (!filter.getTypes().isEmpty()) {
            condition = condition.and(ofAnyType(filter.getTypes()));
        }
        if (!filter.getStates().isEmpty()) {
            condition = condition.and(ofAnyState(filter.getStates()));
        }
        if (filter.getReceivedDateFrom() != null) {
            Range<Instant> interval =
                    Range.closed(filter.getReceivedDateFrom(),filter.getReceivedDateTo() != null ? filter.getReceivedDateTo() : Instant.now());
            condition = condition.and(where(ServiceCallImpl.Fields.createTime.fieldName()).in(interval));
        } else if (filter.getReceivedDateTo() != null) {
            Range<Instant> interval =
                    Range.closed(Instant.EPOCH,filter.getReceivedDateTo());
            condition = condition.and(where(ServiceCallImpl.Fields.createTime.fieldName()).in(interval));
        }
        if(filter.getModificationDateFrom() != null) {
            Range<Instant> interval =
                    Range.closed(filter.getModificationDateFrom(),filter.getModificationDateTo() != null ? filter.getModificationDateTo() : Instant.now());
            condition = condition.and(where(ServiceCallImpl.Fields.createTime.fieldName()).in(interval));
        } else if (filter.getModificationDateTo() != null) {
            Range<Instant> interval =
                    Range.closed(Instant.EPOCH, filter.getModificationDateTo());
            condition = condition.and(where(ServiceCallImpl.Fields.createTime.fieldName()).in(interval));
        }
        if (filter.getParent() != null) {
            condition = condition.and(where(ServiceCallImpl.Fields.parent.fieldName()).isEqualTo(filter.getParent()));
        }

        return condition;
    }

    private Condition ofAnyType(List<String> types) {
        return types.stream()
                .map(typeName -> where(ServiceCallImpl.Fields.type.fieldName() + "." + ServiceCallTypeImpl.Fields.name.fieldName())
                        .isEqualTo(typeName))
                .reduce(Condition.FALSE, Condition::or);
    }

    private Condition ofAnyState(List<String> states) {
        return states.stream()
                .map(stateName -> where(ServiceCallImpl.Fields.state.fieldName() + ".name").isEqualTo(DefaultState.valueOf(stateName).getKey()))
                .reduce(Condition.FALSE, Condition::or);
    }
}
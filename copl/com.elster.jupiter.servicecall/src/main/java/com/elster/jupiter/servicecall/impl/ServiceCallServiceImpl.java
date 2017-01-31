/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
import com.elster.jupiter.orm.SqlDialect;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
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
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.ImmutableMap;
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
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.servicecall",
        service = {ServiceCallService.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        property = "name=" + ServiceCallService.COMPONENT_NAME,
        immediate = true)
@LiteralSql
public final class ServiceCallServiceImpl implements IServiceCallService, MessageSeedProvider, TranslationKeyProvider {

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
    private volatile SqlDialect sqlDialect = SqlDialect.ORACLE_SE;
    private volatile Clock clock;

    // OSGi
    public ServiceCallServiceImpl() {
    }

    @Inject
    public ServiceCallServiceImpl(FiniteStateMachineService finiteStateMachineService, OrmService ormService, NlsService nlsService, UserService userService, CustomPropertySetService customPropertySetService, MessageService messageService, JsonService jsonService, UpgradeService upgradeService, Clock clock) {
        this();
        sqlDialect = SqlDialect.H2;
        setFiniteStateMachineService(finiteStateMachineService);
        setOrmService(ormService);
        setNlsService(nlsService);
        setUserService(userService);
        setMessageService(messageService);
        setJsonService(jsonService);
        setCustomPropertySetService(customPropertySetService);
        setUpgradeService(upgradeService);
        setClock(clock);
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
            tableSpecs.addTo(this.dataModel, sqlDialect);
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

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
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
        return Stream.of(
                Stream.of(TranslationKeys.values()),
                Stream.of(DefaultState.values()),
                Stream.of(LogLevel.values()),
                Stream.of(Privileges.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
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
                bind(UserService.class).toInstance(userService);
                bind(Clock.class).toInstance(clock);
            }
        };
    }

    @Activate
    public void activate() {
        dataModel.register(getModule());
        upgradeService.register(
                InstallIdentifier.identifier("Pulse", COMPONENT_NAME),
                dataModel,
                Installer.class,
                ImmutableMap.of(
                        version(10, 2), UpgraderV10_2.class
                ));
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

    @Override
    public Set<DefaultState> nonFinalStates() {
        return EnumSet.of(
                DefaultState.CREATED,
                DefaultState.ONGOING,
                DefaultState.PAUSED,
                DefaultState.PENDING,
                DefaultState.SCHEDULED,
                DefaultState.WAITING
        );    }

    private Condition createConditionFromFilter(ServiceCallFilter filter) {
        Condition condition = Condition.TRUE;

        if (filter.reference != null) {
            condition = condition.and(where(ServiceCallImpl.Fields.externalReference.fieldName()).like(filter.reference).or(where("internalReference").like(filter.reference)));
        }
        if (!filter.types.isEmpty()) {
            condition = condition.and(ofAnyType(filter.types));
        }
        if (!filter.states.isEmpty()) {
            condition = condition.and(ofAnyState(filter.states));
        }
        if (filter.receivedDateFrom != null) {
            Range<Instant> interval =
                    Range.closed(filter.receivedDateFrom, filter.receivedDateTo != null ? filter.receivedDateTo : Instant.now(clock));
            condition = condition.and(where(ServiceCallImpl.Fields.createTime.fieldName()).in(interval));
        } else if (filter.receivedDateTo != null) {
            Range<Instant> interval =
                    Range.closed(Instant.EPOCH, filter.receivedDateTo);
            condition = condition.and(where(ServiceCallImpl.Fields.createTime.fieldName()).in(interval));
        }
        if (filter.modificationDateFrom != null) {
            Range<Instant> interval =
                    Range.closed(filter.modificationDateFrom, filter.modificationDateTo != null ? filter.modificationDateTo : Instant.now(clock));
            condition = condition.and(where(ServiceCallImpl.Fields.createTime.fieldName()).in(interval));
        } else if (filter.modificationDateTo != null) {
            Range<Instant> interval =
                    Range.closed(Instant.EPOCH, filter.modificationDateTo);
            condition = condition.and(where(ServiceCallImpl.Fields.createTime.fieldName()).in(interval));
        }
        if (filter.parent != null) {
            condition = condition.and(where(ServiceCallImpl.Fields.parent.fieldName()).isEqualTo(filter.parent));
        }
        if (filter.targetObject != null) {
            condition = condition.and(where(ServiceCallImpl.Fields.targetObject.fieldName()).isEqualTo(dataModel.asRefAny(filter.targetObject)));
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
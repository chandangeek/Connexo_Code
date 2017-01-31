/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroActionException;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheckException;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeException;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeFail;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.users.User;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class UsagePointStateChangeRequestImpl implements UsagePointStateChangeRequest {
    public enum Fields {
        USAGE_POINT("usagePoint"),
        TRANSITION_ID("transitionId"),
        FROM_STATE_NAME("fromStateName"),
        TO_STATE_NAME("toStateName"),
        TRANSITION_TIME("transitionTime"),
        SCHEDULE_TIME("scheduleTime"),
        ORIGINATOR("originator"),
        PROPERTIES("properties"),
        STATUS("status"),
        FAIL_REASON("generalFailReason"),
        FAILS("fails"),;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private final Clock clock;
    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final UsagePointLifeCycleConfigurationService lifeCycleConfigurationService;
    private final ServerUsagePointLifeCycleService lifeCycleService;

    private long id;
    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    @Size(max = Table.NAME_LENGTH)
    private String fromStateName;
    @Size(max = Table.NAME_LENGTH)
    private String toStateName;
    private Instant transitionTime;
    private Instant scheduleTime;
    private Reference<User> originator = ValueReference.absent();
    private List<UsagePointStateChangePropertyImpl> properties = new ArrayList<>();
    private transient Map<String, Object> propertiesMap;
    private UsagePointStateChangeRequest.Status status = UsagePointStateChangeRequest.Status.SCHEDULED;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String generalFailReason;
    private List<UsagePointStateChangeFailImpl> fails = new ArrayList<>();

    private long transitionId;
    private transient UsagePointTransition transition;

    @Inject
    public UsagePointStateChangeRequestImpl(Clock clock,
                                            DataModel dataModel,
                                            Thesaurus thesaurus,
                                            UsagePointLifeCycleConfigurationService lifeCycleConfigurationService,
                                            ServerUsagePointLifeCycleService lifeCycleService) {
        this.clock = clock;
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.lifeCycleConfigurationService = lifeCycleConfigurationService;
        this.lifeCycleService = lifeCycleService;
    }

    UsagePointStateChangeRequestImpl init(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime, String application, Map<String, Object> properties) {
        this.transitionId = transition.getId();
        this.transition = transition;
        this.fromStateName = transition.getFrom().getName();
        this.toStateName = transition.getTo().getName();
        this.usagePoint.set(usagePoint);
        this.transitionTime = transitionTime;
        this.scheduleTime = this.clock.instant();
        initProperties(properties);
        initOriginator(transition, application);
        this.dataModel.persist(this);
        return this;
    }

    UsagePointStateChangeRequestImpl initAsHistoryRecord(UsagePoint usagePoint, String fromStateName, String toStateName, Instant transitionTime) {
        this.status = Status.COMPLETED;
        this.fromStateName = fromStateName;
        this.toStateName = toStateName;
        this.usagePoint.set(usagePoint);
        this.transitionTime = transitionTime;
        this.scheduleTime = this.clock.instant();
        this.originator.set(this.lifeCycleService.getCurrentUser());
        this.dataModel.persist(this);
        return this;
    }

    UsagePointStateChangeRequestImpl initAsFailRecord(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime, String failReason) {
        this.status = Status.FAILED;
        this.transitionId = transition.getId();
        this.transition = transition;
        this.fromStateName = transition.getFrom().getName();
        this.toStateName = transition.getTo().getName();
        this.usagePoint.set(usagePoint);
        this.transitionTime = transitionTime;
        this.generalFailReason = failReason;
        this.originator.set(this.lifeCycleService.getCurrentUser());
        return this;
    }

    private void initProperties(Map<String, Object> properties) {
        Set<PropertySpec> allMicroActionPropertySpecs = this.lifeCycleConfigurationService.getMicroActions().stream()
                .flatMap(microAction -> microAction.getPropertySpecs().stream())
                .collect(Collectors.toSet());
        for (Map.Entry<String, Object> parameter : properties.entrySet()) {
            if (parameter.getValue() == null) {
                continue;
            }
            allMicroActionPropertySpecs.stream()
                    .filter(propertySpec -> propertySpec.getName().equals(parameter.getKey()))
                    .map(propertySpec -> propertySpec.getValueFactory().toStringValue(parameter.getValue()))
                    .findAny()
                    .ifPresent(value -> this.properties.add(this.dataModel.getInstance(UsagePointStateChangePropertyImpl.class)
                            .init(this, parameter.getKey(), value)));
        }
    }

    private void initOriginator(UsagePointTransition transition, String application) {
        User user = this.lifeCycleService.getCurrentUser();
        if (userHasPrivilegeToPerformTransition(user, transition, application)) {
            this.originator.set(user);
            return;
        }
        throw new UsagePointStateChangeException(this.thesaurus.getFormat(MessageSeeds.USER_CAN_NOT_PERFORM_TRANSITION).format());
    }

    private boolean userHasPrivilegeToPerformTransition(User user, UsagePointTransition transition, String application) {
        return transition.getLevels().isEmpty() || transition.getLevels()
                .stream()
                .map(UsagePointTransition.Level::getPrivilege)
                .anyMatch(privilege -> user.hasPrivilege(application, privilege));
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public String getStatusName() {
        return this.thesaurus.getString(TranslationKeys.Keys.CHANGE_REQUEST_STATUS_PREFIX + getStatus(), getStatus().name());
    }

    @Override
    public Type getType() {
        return Type.STATE_CHANGE;
    }

    @Override
    public String getTypeName() {
        return this.thesaurus.getString(TranslationKeys.Keys.CHANGE_REQUEST_TYPE_PREFIX + getType(), getType().getKey());
    }

    @Override
    public UsagePoint getUsagePoint() {
        return this.usagePoint.get();
    }

    @Override
    public String getFromStateName() {
        return this.fromStateName;
    }

    @Override
    public String getToStateName() {
        return this.toStateName;
    }

    @Override
    public User getOriginator() {
        return this.originator.orElse(null);
    }

    @Override
    public Instant getTransitionTime() {
        return this.transitionTime;
    }

    @Override
    public Map<String, Object> getProperties() {
        if (this.propertiesMap == null) {
            this.propertiesMap = new HashMap<>();
            if (!this.properties.isEmpty()) {
                Set<PropertySpec> allMicroActionPropertySpecs = this.lifeCycleConfigurationService.getMicroActions().stream()
                        .flatMap(microAction -> microAction.getPropertySpecs().stream())
                        .collect(Collectors.toSet());
                for (UsagePointStateChangePropertyImpl parameter : this.properties) {
                    allMicroActionPropertySpecs.stream()
                            .filter(propertySpec -> propertySpec.getName().equals(parameter.getKey()))
                            .map(propertySpec -> propertySpec.getValueFactory().fromStringValue(parameter.getDatabaseValue()))
                            .findAny()
                            .ifPresent(value -> this.propertiesMap.put(parameter.getKey(), value));
                }
            }
        }
        return Collections.unmodifiableMap(this.propertiesMap);
    }

    @Override
    public Instant getScheduleTime() {
        return this.scheduleTime;
    }

    public String getGeneralFailReason() {
        return this.generalFailReason;
    }

    @Override
    public List<UsagePointStateChangeFail> getFailReasons() {
        return Collections.unmodifiableList(this.fails);
    }

    @Override
    public boolean userCanManageRequest(String application) {
        return this.status == Status.SCHEDULED
                && transitionExistsAndCanBeFetched()
                && userHasPrivilegeToPerformTransition(this.lifeCycleService.getCurrentUser(), this.transition, application);
    }

    @Override
    public void cancel() {
        if (this.status == Status.SCHEDULED) {
            this.status = Status.CANCELLED;
            this.dataModel.update(this);
            this.lifeCycleService.rescheduleExecutor();
        }
    }

    UsagePointStateChangeRequest execute() {
        if (this.status == Status.SCHEDULED) {
            this.status = Status.FAILED;
            if (transitionExistsAndCanBeFetched() && transitionIsActualForUsagePoint()) {
                try {
                    this.lifeCycleService.triggerMicroChecks(this.usagePoint.get(), this.transition, this.transitionTime);
                    this.lifeCycleService.triggerMicroActions(this.usagePoint.get(), this.transition, this.transitionTime, getProperties());
                    this.lifeCycleService.performTransition(this.usagePoint.get(), this.transition, this.transitionTime);
                    this.status = Status.COMPLETED;
                } catch (ExecutableMicroCheckException cex) {
                    this.generalFailReason = this.thesaurus.getFormat(MessageSeeds.MICRO_CHECKS_FAILED_NO_PARAM).format();
                    cex.getViolations().forEach(violation -> this.fails.add(this.dataModel.getInstance(UsagePointStateChangeFailImpl.class)
                            .checkFail(this, violation.getMicroCheck().getKey(), violation.getMicroCheck().getName(), violation.getLocalizedMessage())));
                } catch (ExecutableMicroActionException aex) {
                    this.generalFailReason = this.thesaurus.getFormat(MessageSeeds.MICRO_ACTION_FAILED_NO_PARAM).format();
                    this.fails.add(this.dataModel.getInstance(UsagePointStateChangeFailImpl.class)
                            .actionFail(this, aex.getMicroAction().getKey(), aex.getMicroAction().getName(), aex.getLocalizedMessage()));
                } catch (Exception ex) {
                    this.generalFailReason = ex.getLocalizedMessage();
                }
            }
            this.dataModel.update(this);
        }
        return this;
    }

    private boolean transitionExistsAndCanBeFetched() {
        if (this.transition == null) {
            Optional<UsagePointTransition> transitionRef = this.lifeCycleConfigurationService.findUsagePointTransition(this.transitionId);
            if (!transitionRef.isPresent()) {
                this.generalFailReason = this.thesaurus.getFormat(MessageSeeds.TRANSITION_NOT_FOUND).format(this.transitionId);
                return false;
            }
            this.transition = transitionRef.get();
        }
        return true;
    }

    private boolean transitionIsActualForUsagePoint() {
        UsagePointState currentUsagePointState = this.usagePoint.get().getState(this.transitionTime);
        if (currentUsagePointState.getId() != this.transition.getFrom().getId()) {
            this.generalFailReason = this.thesaurus.getFormat(MessageSeeds.USAGE_POINT_STATE_DOES_NOT_SUPPORT_TRANSITION).format(currentUsagePointState.getName(), this.toStateName);
            return false;
        }
        return true;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UsagePointStateChangeRequestImpl that = (UsagePointStateChangeRequestImpl) o;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (this.id ^ (this.id >>> 32));
    }
}

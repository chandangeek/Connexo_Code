package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
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
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UsagePointStateChangeRequestImpl implements UsagePointStateChangeRequest {
    public enum Fields {
        USAGE_POINT("usagePoint"),
        TRANSITION_ID("transitionId"),
        TRANSITION_NAME("transitionName"),
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
    private final ThreadPrincipalService threadPrincipalService;
    private final UsagePointLifeCycleConfigurationService lifeCycleConfigurationService;
    private final ServerUsagePointLifeCycleService lifeCycleService;

    private long id;
    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    private long transitionId;
    @Size(max = Table.NAME_LENGTH)
    private String transitionName;
    private transient UsagePointTransition transition;
    private Instant transitionTime;
    private Instant scheduleTime;
    private Reference<User> originator = ValueReference.absent();
    private List<UsagePointStateChangePropertyImpl> properties = new ArrayList<>();
    private transient Map<String, Object> propertiesMap;
    private UsagePointStateChangeRequest.Status status = UsagePointStateChangeRequest.Status.SCHEDULED;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String generalFailReason;
    private List<UsagePointStateChangeFailImpl> fails = new ArrayList<>();

    @Inject
    public UsagePointStateChangeRequestImpl(Clock clock,
                                            DataModel dataModel,
                                            Thesaurus thesaurus,
                                            ThreadPrincipalService threadPrincipalService,
                                            UsagePointLifeCycleConfigurationService lifeCycleConfigurationService,
                                            ServerUsagePointLifeCycleService lifeCycleService) {
        this.clock = clock;
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.threadPrincipalService = threadPrincipalService;
        this.lifeCycleConfigurationService = lifeCycleConfigurationService;
        this.lifeCycleService = lifeCycleService;
    }

    UsagePointStateChangeRequestImpl init(UsagePoint usagePoint, UsagePointTransition transition, Instant transitionTime, String application, Map<String, Object> properties) {
        this.usagePoint.set(usagePoint);
        this.transitionId = transition.getId();
        this.transitionName = transition.getName();
        this.transition = transition;
        this.transitionTime = transitionTime;
        this.scheduleTime = this.clock.instant();
        initProperties(properties);
        initOriginator(transition, application);
        this.dataModel.persist(this);
        return this;
    }

    private void initProperties(Map<String, Object> properties) {
        for (Map.Entry<String, Object> parameter : properties.entrySet()) {
            if (parameter.getValue() == null) {
                continue;
            }
            this.lifeCycleConfigurationService.getMicroActionByKey(parameter.getKey())
                    .flatMap(action -> action.getPropertySpecs().stream()
                            .filter(propertySpec -> propertySpec.getName().equals(parameter.getKey()))
                            .findAny())
                    .map(propertySpec -> propertySpec.getValueFactory().toStringValue(parameter.getValue()))
                    .ifPresent(value -> this.properties.add(this.dataModel.getInstance(UsagePointStateChangePropertyImpl.class)
                            .init(this, parameter.getKey(), value)));
        }
    }

    private void initOriginator(UsagePointTransition transition, String application) {
        Principal currentUser = this.threadPrincipalService.getPrincipal();
        if (currentUser instanceof User) {
            User user = (User) currentUser;
            this.originator.set(user);
            if (!transition.getLevels()
                    .stream()
                    .map(UsagePointTransition.Level::getPrivilege)
                    .anyMatch(privilege -> user.hasPrivilege(application, privilege))) {
                this.status = Status.FAILED;
                this.generalFailReason = this.thesaurus.getFormat(MessageSeeds.USER_CAN_NOT_PERFORM_TRANSITION).format();
            }
        } else {
            throw new UsagePointStateChangeException(this.thesaurus.getFormat(MessageSeeds.USER_CAN_NOT_PERFORM_TRANSITION).format());
        }
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public UsagePoint getUsagePoint() {
        return this.usagePoint.get();
    }

    @Override
    public String getUsagePointTransition() {
        return this.transitionName;
    }

    @Override
    public User getOriginator() {
        return this.originator.get();
    }

    @Override
    public Instant getTransitionTime() {
        return this.transitionTime;
    }

    @Override
    public Map<String, Object> getProperties() {
        if (this.propertiesMap == null) {
            this.propertiesMap = new HashMap<>();
            for (UsagePointStateChangePropertyImpl parameter : this.properties) {
                this.lifeCycleConfigurationService.getMicroActionByKey(parameter.getKey())
                        .flatMap(action -> action.getPropertySpecs().stream()
                                .filter(propertySpec -> propertySpec.getName().equals(parameter.getKey()))
                                .findAny())
                        .map(propertySpec -> propertySpec.getValueFactory().fromStringValue(parameter.getDatabaseValue()))
                        .ifPresent(value -> this.propertiesMap.put(parameter.getKey(), value));
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
    public void cancel() {
        this.status = Status.CANCELLED;
        this.dataModel.update(this);
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
                    cex.getViolations().forEach(violation -> {
                        this.fails.add(this.dataModel.getInstance(UsagePointStateChangeFailImpl.class)
                                .init(this, violation.getMicroCheck().getKey(), violation.getMicroCheck().getName(), violation.getLocalizedMessage()));
                    });
                } catch (ExecutableMicroActionException aex) {
                    this.generalFailReason = this.thesaurus.getFormat(MessageSeeds.MICRO_ACTION_FAILED_NO_PARAM).format();
                    this.fails.add(this.dataModel.getInstance(UsagePointStateChangeFailImpl.class)
                            .init(this, aex.getMicroAction().getKey(), aex.getMicroAction().getName(), aex.getLocalizedMessage()));
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
            this.generalFailReason = this.thesaurus.getFormat(MessageSeeds.USAGE_POINT_STATE_DOES_NOT_SUPPORT_TRANSITION).format(currentUsagePointState.getName(), this.transitionName);
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

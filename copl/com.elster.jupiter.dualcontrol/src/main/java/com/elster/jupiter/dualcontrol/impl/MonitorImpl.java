/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.Monitor;
import com.elster.jupiter.dualcontrol.PendingUpdate;
import com.elster.jupiter.dualcontrol.State;
import com.elster.jupiter.dualcontrol.UnderDualControl;
import com.elster.jupiter.dualcontrol.UserAction;
import com.elster.jupiter.dualcontrol.UserOperation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;

import com.google.common.collect.Lists;

import javax.inject.Inject;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.elster.jupiter.util.streams.Currying.test;
import static com.elster.jupiter.util.streams.DecoratedStream.decorate;
import static com.elster.jupiter.util.streams.Predicates.not;

class MonitorImpl implements Monitor {

    enum Fields {
        STATE("state"),
        OPERATIONS("operations");

        private String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private enum BehaviourState {
        INACTIVE(State.INACTIVE) {
            public State newStateForRequest(PendingUpdate update) {
                if (update.isActivation()) {
                    return State.PENDING_ACTIVATION;
                }
                if (update.isRemoval()) {
                    return State.OBSOLETE;
                }
                throw new IllegalStateException();
            }
        },
        PENDING_ACTIVATION(State.PENDING_ACTIVATION) {
            @Override
            public State newStateForFinalApproval(PendingUpdate update) {
                return State.ACTIVE;
            }

            @Override
            public State newStateForRejection() {
                return State.INACTIVE;
            }
        },
        ACTIVE(State.ACTIVE) {
            public State newStateForRequest(PendingUpdate update) {
                if (update.isActivation()) {
                    throw new IllegalStateException();
                }
                return State.PENDING_UPDATE;
            }
        },
        PENDING_UPDATE(State.PENDING_UPDATE) {
            @Override
            public State newStateForFinalApproval(PendingUpdate update) {
                if (update.isDeactivation()) {
                    return State.INACTIVE;
                }
                return update.isRemoval() ? State.OBSOLETE : State.ACTIVE;
            }

            @Override
            public State newStateForRejection() {
                return State.ACTIVE;
            }

            @Override
            public State newStateForRequest(PendingUpdate update) {
                if (update.isActivation()) {
                    throw new IllegalStateException();
                }
                return State.PENDING_UPDATE;
            }
        },
        OBSOLETE(State.OBSOLETE);

        private final State state;

        BehaviourState(State state) {
            this.state = state;
        }

        public State newStateForRequest(PendingUpdate update) {
            throw new IllegalStateException();
        }

        public State newStateForFinalApproval(PendingUpdate update) {
            throw new IllegalStateException();
        }

        public State newStateForRejection() {
            throw new IllegalStateException();
        }

        boolean match(State state) {
            return this.state.equals(state);
        }
    }

    private static final int REQUIRED_APPROVALS = 2;  // hard coded for 10.3
    private final DataModel dataModel;
    private final ThreadPrincipalService threadPrincipalService;

    private long id;
    private State state = State.INACTIVE;

    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    private List<UserOperationImpl> operations = new ArrayList<>();

    @Inject
    MonitorImpl(DataModel dataModel, ThreadPrincipalService threadPrincipalService) {
        this.dataModel = dataModel;
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public <T extends PendingUpdate> void request(T update, UnderDualControl<T> underDualControl) {
        state = getBehaviourState().newStateForRequest(update);
        dataModel.mapper(Monitor.class).update(this, Fields.STATE.fieldName());

        operations.add(UserOperationImpl.of(this, getUser(), UserAction.REQUEST));

        underDualControl.setPendingUpdate(update);
        if (State.OBSOLETE.equals(state)) {
            underDualControl.applyUpdate();
            underDualControl.clearUpdate();
        }
    }

    @Override
    public <T extends PendingUpdate> void approve(UnderDualControl<T> underDualControl) {
        UserOperationImpl userOperation = UserOperationImpl.of(this, getUser(), UserAction.APPROVE);
        operations.add(userOperation);

        long approvals = decorate(Lists.reverse(operations).stream())
                .takeWhile(not(UserOperation::isRequest))
                .filter(UserOperation::isApproval)
                .distinct(UserOperation::getUser)
                .count();
        if (approvals >= getRequiredApprovals()) {
            state = getBehaviourState().newStateForFinalApproval(underDualControl.getPendingUpdate().get());
            dataModel.mapper(Monitor.class).update(this, Fields.STATE.fieldName());
            underDualControl.applyUpdate();
            underDualControl.clearUpdate();
        }
    }

    @Override
    public <T extends PendingUpdate> void reject(UnderDualControl<T> underDualControl) {
        UserOperationImpl userOperation = UserOperationImpl.of(this, getUser(), UserAction.REJECT);
        operations.add(userOperation);

        state = getBehaviourState().newStateForRejection();
        dataModel.mapper(Monitor.class).update(this, Fields.STATE.fieldName());
        underDualControl.clearUpdate();
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public List<UserOperation> getOperations() {
        return Collections.unmodifiableList(operations);
    }

    @Override
    public boolean hasCurrentUserAccepted() {
        return decorate(Lists.reverse(operations).stream())
                .takeWhile(not(UserOperation::isRequest))
                .filter(UserOperation::isApproval)
                .distinct(UserOperation::getUser)
                .filter(userOperation -> userOperation.getUser().equals(getUser()))
                .findAny()
                .isPresent();
    }

    User getUser() {
        User user = null;
        Principal principal = threadPrincipalService.getPrincipal();
        if (principal instanceof User) {
            user = (User) principal;
        }
        return user;
    }

    private BehaviourState getBehaviourState() {
        return Arrays.stream(BehaviourState.values())
                .filter(test(BehaviourState::match).with(state))
                .findAny()
                .get();
    }

    private int getRequiredApprovals() {
        return REQUIRED_APPROVALS;
    }
}

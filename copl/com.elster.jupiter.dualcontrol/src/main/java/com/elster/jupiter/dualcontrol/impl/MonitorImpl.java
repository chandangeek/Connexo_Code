package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.Monitor;
import com.elster.jupiter.dualcontrol.PendingUpdate;
import com.elster.jupiter.dualcontrol.State;
import com.elster.jupiter.dualcontrol.UnderDualControl;
import com.elster.jupiter.dualcontrol.UserAction;
import com.elster.jupiter.dualcontrol.UserOperation;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;

import com.google.common.collect.Lists;

import javax.inject.Inject;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.elster.jupiter.util.streams.Currying.test;
import static com.elster.jupiter.util.streams.DecoratedStream.decorate;
import static com.elster.jupiter.util.streams.Predicates.not;

class MonitorImpl implements Monitor {

    private static final int REQUIRED_APPROVALS = 2;  // hard coded for 10.3
    private final ThreadPrincipalService threadPrincipalService;
    private State state = State.INACTIVE;

    private List<UserOperationImpl> userOperations = new ArrayList<>();

    @Inject
    MonitorImpl(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public <T extends PendingUpdate> void request(T update, UnderDualControl<T> underDualControl) {
        this.state = getBehaviourState().newStateForRequest(update);

        UserOperationImpl userOperation = UserOperationImpl.of(getUser(), UserAction.REQUEST);
        userOperations.add(userOperation);

        underDualControl.setPendingUpdate(update);
    }

    private BehaviourState getBehaviourState() {
        return Arrays.stream(BehaviourState.values())
                .filter(test(BehaviourState::match).with(state))
                .findAny()
                .get();
    }

    User getUser() {
        User user = null;
        Principal principal = threadPrincipalService.getPrincipal();
        if (principal instanceof User) {
            user = (User) principal;
        }
        return user;
    }

    @Override
    public <T extends PendingUpdate> void approve(UnderDualControl<T> underDualControl) {
        UserOperationImpl userOperation = UserOperationImpl.of(getUser(), UserAction.APPROVE);
        userOperations.add(userOperation);

        long approvals = decorate(Lists.reverse(userOperations).stream())
                .takeWhile(not(UserOperation::isRequest))
                .filter(UserOperation::isApproval)
                .distinct(UserOperation::getUser)
                .count();
        if (approvals >= getRequiredApprovals()) {
            state = getBehaviourState().newStateForFinalApproval(underDualControl.getPendingUpdate().get());
            underDualControl.applyUpdate();
            underDualControl.clearUpdate();
        }
    }

    private int getRequiredApprovals() {
        return REQUIRED_APPROVALS;
    }

    @Override
    public <T extends PendingUpdate> void reject(UnderDualControl<T> underDualControl) {
        UserOperationImpl userOperation = UserOperationImpl.of(getUser(), UserAction.REJECT);
        userOperations.add(userOperation);

        state = getBehaviourState().newStateForRejection();
        underDualControl.clearUpdate();
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public List<UserOperation> getUserOperations() {
        return Collections.unmodifiableList(userOperations);
    }

    private enum BehaviourState {
        INACTIVE(State.INACTIVE) {
            public State newStateForRequest(PendingUpdate update) {
                if (update.isActivation()) {
                    return State.PENDING_ACTIVATION;
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
                if (update.isUpdate() || update.isRemoval()) {
                    return State.PENDING_UPDATE;
                }
                throw new IllegalStateException();
            }
        },
        PENDING_UPDATE(State.PENDING_UPDATE) {
            @Override
            public State newStateForFinalApproval(PendingUpdate update) {
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

        boolean match(State state) {
            return this.state.equals(state);
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
    }
}

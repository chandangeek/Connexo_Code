package com.energyict.mdc.device.lifecycle.config.rest.resource.requests;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.rest.resource.ResourceHelper;
import com.energyict.mdc.device.lifecycle.config.rest.response.AuthorizedActionInfo;

import java.util.Objects;
import java.util.Optional;

public class AuthorizedActionRequestFactory {
    private final ResourceHelper resourceHelper;
    private DeviceLifeCycle deviceLifeCycle;
    private AuthorizedAction authorizedAction;
    private AuthorizedActionInfo info;

    public AuthorizedActionRequestFactory(ResourceHelper resourceHelper) {
        this.resourceHelper = resourceHelper;
    }

    private StateTransitionEventType getEventType(){
        return resourceHelper.findStateTransitionEventTypeOrThrowException(this.info.getEventTypeSymbol());
    }

    private boolean isComplexChanges(AuthorizedTransitionAction transitionAction, AuthorizedActionInfo info){
        Optional<String> specialTransitionName = transitionAction.getStateTransition().getName();
        return (specialTransitionName.isPresent() && !specialTransitionName.get().equals(info.name))
                || fromStateChanged(transitionAction, info)
                || toStateChanged(transitionAction, info)
                || triggerByChanged(transitionAction, info);
    }

    private boolean fromStateChanged(AuthorizedTransitionAction transitionAction, AuthorizedActionInfo info) {
        return this.stateChanged(transitionAction.getStateTransition().getFrom(), info.fromState.id);
    }

    private boolean toStateChanged(AuthorizedTransitionAction transitionAction, AuthorizedActionInfo info) {
        return this.stateChanged(transitionAction.getStateTransition().getTo(), info.toState.id);
    }

    private boolean stateChanged(State state, long stateId) {
        return state.getId() != stateId;
    }

    private boolean triggerByChanged(AuthorizedTransitionAction transitionAction, AuthorizedActionInfo info){
        String newEventType = info.getEventTypeSymbol();
        return newEventType != null && !transitionAction.getStateTransition().getEventType().getSymbol().equals(newEventType);
    }

    public AuthorizedActionChangeRequest from(DeviceLifeCycle deviceLifeCycle, AuthorizedActionInfo info, Operation operation){
        Objects.requireNonNull(deviceLifeCycle);
        Objects.requireNonNull(info);
        Objects.requireNonNull(operation);
        if (info.id > 0){
            this.authorizedAction = this.resourceHelper.findAuthorizedActionByIdOrThrowException(deviceLifeCycle, info.id);
        }
        this.deviceLifeCycle = deviceLifeCycle;
        this.info = info;
        return operation.getRequest(this);
    }

    public AuthorizedActionChangeRequest from(DeviceLifeCycle deviceLifeCycle, long authorizedActionId, Operation operation){
        AuthorizedActionInfo info = new AuthorizedActionInfo();
        info.id = authorizedActionId;
        return from(deviceLifeCycle, info, operation);
    }

    public static enum Operation {
        CREATE {
            @Override
            AuthorizedActionChangeRequest getRequest(AuthorizedActionRequestFactory factory) {
                return new AuthorizedTransitionActionCreateRequest(factory.deviceLifeCycle, factory.getEventType(), factory.info);
            }
        },
        DELETE {
            @Override
            AuthorizedActionChangeRequest getRequest(AuthorizedActionRequestFactory factory) {
                if (factory.authorizedAction instanceof AuthorizedTransitionAction) {
                    return new AuthorizedTransitionActionDeleteRequest(factory.deviceLifeCycle, (AuthorizedTransitionAction) factory.authorizedAction);
                } else {
                    return new AuthorizedActionUnsupportedRequest();
                }
            }
        },
        MODIFY {
            @Override
            AuthorizedActionChangeRequest getRequest(AuthorizedActionRequestFactory factory) {
                if (factory.authorizedAction instanceof AuthorizedTransitionAction) {
                    AuthorizedTransitionAction transitionAction = (AuthorizedTransitionAction) factory.authorizedAction;
                    if (factory.isComplexChanges(transitionAction, factory.info)) {
                        return new AuthorizedTransitionActionComplexEditRequest(factory.deviceLifeCycle, transitionAction, factory.getEventType(), factory.info);
                    } else {
                        return new AuthorizedTransitionActionSimpleEditRequest(factory.deviceLifeCycle, transitionAction, factory.info);
                    }
                } else {
                    return new AuthorizedActionUnsupportedRequest();
                }
            }
        },
        ;

        AuthorizedActionChangeRequest getRequest(AuthorizedActionRequestFactory factory){
            return new AuthorizedActionUnsupportedRequest();
        }
    }
}

package com.elster.jupiter.prepayment.impl.servicecall;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.text.MessageFormat;

/**
 * Implementation of {@link ServiceCallHandler} interface for Redknee prepayment solution
 *
 * @author sva
 * @since 31/03/16 - 13:05
 */
@Component(name = "com.energyict.servicecall.redknee.operation.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + OperationHandler.HANDLER_NAME)
public class OperationHandler implements ServiceCallHandler {

    public static final String HANDLER_NAME = "RedkneeOperationHandler";

    private static final boolean SUCCESS = true;
    private static final boolean FAILURE = true;

    public OperationHandler() {
    }

    @Activate
    public void activate() {
    }

    @Override
    public boolean allowStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        if (newState.equals(DefaultState.CANCELLED)) {
            switch (oldState) {
                case WAITING:
                    return true;
                default:
                    return false;
            }
        }
        return true;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case CANCELLED:
                cancelServiceCallIncludingItsChildren(serviceCall);
                break;
            case SUCCESSFUL:
                callBackRedknee(SUCCESS);
                break;
            default:
                break;
        }
    }

    private void cancelServiceCallIncludingItsChildren(ServiceCall serviceCall) {
        serviceCall.findChildren().stream().forEach(child -> {
            if (child.canTransitionTo(DefaultState.CANCELLED)) {
                serviceCall.log(LogLevel.INFO, MessageFormat.format("Cancelling child service call with id ", child.getId()));
                child.requestTransition(DefaultState.CANCELLED);
            } else {
                serviceCall.log(LogLevel.INFO, MessageFormat.format("Could not cancel child service call with id ", child.getId()));
            }
        });
    }

    @Override
    public void onChildStateChange(ServiceCall parent, ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case SUCCESSFUL:
                parent.log(LogLevel.INFO, MessageFormat.format("Service call {0} (type={1}) was successful", serviceCall.getId(), serviceCall.getType().getName()));
                if (parent.findChildren().stream().allMatch(child -> child.getState().equals(DefaultState.SUCCESSFUL))) {
                    parent.log(LogLevel.INFO, "All child service call operations have been executed successfully");
                    requestTransitionTo(parent, DefaultState.SUCCESSFUL);
                }
                break;
            case FAILED:
                parent.log(LogLevel.SEVERE, MessageFormat.format("Child service call {0} (type={1}) failed", serviceCall.getId(), serviceCall.getType().getName()));
                requestTransitionTo(parent, DefaultState.FAILED);
                callBackRedknee(FAILURE);
                break;
            case CANCELLED:
                parent.log(LogLevel.SEVERE, MessageFormat.format("Child service call {0} (type={1}) has been cancelled", serviceCall.getId(), serviceCall.getType().getName()));
                requestTransitionTo(parent, DefaultState.CANCELLED);
                break;
            default:
                break;
        }
    }

    private void requestTransitionTo(ServiceCall serviceCall, DefaultState state) {
        if (serviceCall.getState().equals(DefaultState.WAITING)) {
            serviceCall.requestTransition(DefaultState.ONGOING);
        }
        if (serviceCall.canTransitionTo(state)) {
            serviceCall.requestTransition(state);
        } // Else the serviceCall is probably already in an end state

    }

    private void callBackRedknee(boolean successIndicator) {
        // TODO
    }
}
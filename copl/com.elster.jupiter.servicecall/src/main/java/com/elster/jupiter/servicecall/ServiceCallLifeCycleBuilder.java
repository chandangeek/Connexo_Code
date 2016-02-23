package com.elster.jupiter.servicecall;

public interface ServiceCallLifeCycleBuilder {

    ServiceCallLifeCycleBuilder remove(DefaultState state);

    ServiceCallLifeCycleBuilder removeTransition(DefaultState from, DefaultState to);

    ServiceCallLifeCycle create();
}

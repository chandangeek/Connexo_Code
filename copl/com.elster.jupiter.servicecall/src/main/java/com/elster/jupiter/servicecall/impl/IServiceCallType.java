package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.servicecall.ServiceCallType;

/**
 * Created by TVN on 19/02/2016.
 */
public interface IServiceCallType extends ServiceCallType {
    @Override
    IServiceCallLifeCycle getServiceCallLifeCycle();
}

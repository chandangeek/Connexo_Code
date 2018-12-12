/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.rest;

import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;

import aQute.bnd.annotation.ProviderType;

import java.util.Map;


@ProviderType
public interface ServiceCallInfoFactory {

    ServiceCallInfo detailed(ServiceCall serviceCall, Map<DefaultState, Long> childrenInformation);

    ServiceCallInfo summarized(ServiceCall serviceCall);

    ServiceCallFilter convertToServiceCallFilter(JsonQueryFilter filter);
}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallType;

/**
 * Created by bvn on 2/15/16.
 */
public interface ServiceCallTypeCustomPropertySetUsage {
    ServiceCallType getServiceCallType();
    RegisteredCustomPropertySet getCustomPropertySet();
}

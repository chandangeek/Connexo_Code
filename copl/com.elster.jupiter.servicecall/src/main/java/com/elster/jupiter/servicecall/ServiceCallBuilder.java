/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import com.elster.jupiter.cps.PersistentDomainExtension;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ServiceCallBuilder {

    ServiceCallBuilder origin(String origin);

    ServiceCallBuilder externalReference(String externalReference);

    ServiceCallBuilder targetObject(Object targetObject);

    ServiceCallBuilder extendedWith(PersistentDomainExtension<ServiceCall> extension, Object... additionalPrimaryKeyValues);

    ServiceCall create();
}

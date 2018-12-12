/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.whiteboard.ReferenceInfo;
import com.elster.jupiter.rest.whiteboard.SpecificReferenceResolver;

import org.osgi.service.component.annotations.Component;

import java.util.Optional;

@Component(name = "com.elster.jupiter.metering.rest.UsagePointReferenceResolver", immediate = true, service = SpecificReferenceResolver.class)
public class UsagePointReferenceResolver implements SpecificReferenceResolver {
    @Override
    public Optional<ReferenceInfo> resolve(Object object) {
        if (object instanceof UsagePoint) {
            return Optional.of(new ReferenceInfo("com.elster.jupiter.metering.UsagePoint", ((UsagePoint) object).getName()));
        }
        return Optional.empty();
    }
}

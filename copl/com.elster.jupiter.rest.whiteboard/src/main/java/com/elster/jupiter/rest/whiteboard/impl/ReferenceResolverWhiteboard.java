/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.rest.whiteboard.ReferenceInfo;
import com.elster.jupiter.rest.whiteboard.ReferenceResolver;
import com.elster.jupiter.rest.whiteboard.SpecificReferenceResolver;
import com.elster.jupiter.util.concurrent.CopyOnWriteServiceContainer;
import com.elster.jupiter.util.streams.Functions;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.Optional;

import static com.elster.jupiter.util.streams.Currying.use;

@Component(name = "com.elster.jupiter.rest.whiteboard.referenceresolver" , immediate = true , service = {ReferenceResolver.class}  )
public class ReferenceResolverWhiteboard implements ReferenceResolver {

    private final CopyOnWriteServiceContainer<SpecificReferenceResolver> resolvers = new CopyOnWriteServiceContainer<>();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addReferenceResolver(SpecificReferenceResolver resolver) {
        resolvers.register(resolver);
    }

    public void removeReferenceResolver(SpecificReferenceResolver resolver) {
        resolvers.unregister(resolver);
    }

    @Override
    public Optional<ReferenceInfo> resolve(Object object) {
        return resolvers.getServices()
                .stream()
                .map(use(SpecificReferenceResolver::resolve).with(object))
                .flatMap(Functions.asStream())
                .findAny();
    }
}

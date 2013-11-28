package com.elster.jupiter.security.thread.impl;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;

public class ServiceMatcher extends AbstractMatcher<TypeLiteral<?>> {

    private final Class<?> serviceClass;

    public ServiceMatcher(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    @Override
    public boolean matches(TypeLiteral<?> typeLiteral) {
        return serviceClass.isAssignableFrom(typeLiteral.getRawType());
    }
}

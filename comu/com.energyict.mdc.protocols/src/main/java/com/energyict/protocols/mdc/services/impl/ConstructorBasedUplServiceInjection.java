package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.protocol.api.services.UnableToCreateProtocolInstance;
import com.energyict.mdc.upl.Services;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

/**
 *
 *
 * @author khe
 * @since 23/01/2017 - 10:47
 */
class ConstructorBasedUplServiceInjection implements InstanceFactory, Comparable<ConstructorBasedUplServiceInjection> {
    private final Constructor constructor;

    private ConstructorBasedUplServiceInjection(Constructor constructor) {
        this.constructor = constructor;
    }

    static InstanceFactory from(String className) {
        try {
            return from(Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new UnableToCreateProtocolInstance(e, className);
        }
    }

    static InstanceFactory from(Class zClass) {
        return Stream
                .of(zClass.getConstructors())
                .map(ConstructorBasedUplServiceInjection::new)
                .sorted()
                .findFirst()
                .get(); // There is always at least one constructor
    }

    @Override
    public int compareTo(ConstructorBasedUplServiceInjection other) {
        return Integer.valueOf(this.constructor.getParameterCount()).compareTo(other.constructor.getParameterCount());
    }

    @Override
    public Object newInstance() throws UnableToCreateProtocolInstance {
        try {
            return this.constructor.newInstance(this.constructorArguments());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new UnableToCreateProtocolInstance(e, this.constructor.getDeclaringClass().getName());
        }
    }

    private Object[] constructorArguments() {
        return Stream
                .of(this.constructor.getParameterTypes())
                .map(this::toConstructorArgument)
                .toArray(Object[]::new);
    }

    private Object toConstructorArgument(Class parameterType) {
        try {
            return Services.serviceOfType(parameterType);
        } catch (Services.UnknownServiceType e) {
            throw new UnableToCreateProtocolInstance(e, constructor.getDeclaringClass().getName());
        }
    }
}
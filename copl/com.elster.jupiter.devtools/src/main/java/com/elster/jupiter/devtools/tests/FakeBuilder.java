package com.elster.jupiter.devtools.tests;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class FakeBuilder {

    public static <T> T initBuilderStub(final Object build, Class<T> builderInterface, Class<?>... subInterfaces) {
        final Class<?>[] allInterfaces = new Class<?>[subInterfaces.length + 1];
        allInterfaces[0] = builderInterface;
        System.arraycopy(subInterfaces, 0, allInterfaces, 1, subInterfaces.length);
        final AtomicReference<T> proxyInstance = new AtomicReference<>();
        proxyInstance.set((T) Proxy.newProxyInstance(builderInterface.getClassLoader(), allInterfaces, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (Arrays.stream(allInterfaces).anyMatch(aClass -> aClass.isAssignableFrom(method.getReturnType()))) {
                    return proxyInstance.get();
                }

                return buildResultGetter.get();
            }
            private Supplier<Object> buildResultGetter = () -> build;
        }));
        return proxyInstance.get();
    }

}

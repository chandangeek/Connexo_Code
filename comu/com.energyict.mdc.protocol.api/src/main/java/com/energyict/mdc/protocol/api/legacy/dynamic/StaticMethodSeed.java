package com.energyict.mdc.protocol.api.legacy.dynamic;

import com.energyict.mdc.common.ApplicationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class StaticMethodSeed implements Seed {

    private String className;
    private String methodName;
    private Object[] arguments;
    private Class[] argumentTypes;

    public StaticMethodSeed(String className, String method) {
        this.className = className;
        this.methodName = method;
        this.arguments = new Object[0];
        this.argumentTypes = new Class[0];
    }

    public StaticMethodSeed(String className, String method, Object argument, Class argumentType) {
        this.className = className;
        this.methodName = method;
        this.arguments = new Object[]{argument};
        this.argumentTypes = new Class[]{argumentType};
    }

    public StaticMethodSeed(String className, String method, Object argument1, Class argumentType1, Object argument2, Class argumentType2) {
        this.className = className;
        this.methodName = method;
        this.arguments = new Object[]{argument1, argument2};
        this.argumentTypes = new Class[]{argumentType1, argumentType2};
    }

    public StaticMethodSeed(String className, String method, Object argument1, Class argumentType1, Object argument2, Class argumentType2, Object argument3, Class argumentType3) {
        this.className = className;
        this.methodName = method;
        this.arguments = new Object[]{argument1, argument2, argument3};
        this.argumentTypes = new Class[]{argumentType1, argumentType2, argumentType3};
    }

    public StaticMethodSeed(String className, String method, Object[] arguments, Class[] argumentTypes) {
        this.className = className;
        this.methodName = method;
        this.arguments = arguments;
        this.argumentTypes = argumentTypes;
    }

    public Object get() {
        try {
            return doGet();
        } catch (NoSuchMethodException |
                 ClassNotFoundException |
                 IllegalAccessException |
                 InstantiationException |
                InvocationTargetException ex) {
            throw new ApplicationException(ex);
        }
    }

    private Object doGet() throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
        Class targetClass = Class.forName(className);
        Method method = targetClass.getMethod(methodName, argumentTypes);
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new ApplicationException("Method " + method + " is not static");
        }
        return method.invoke(null, arguments);
    }

}

package com.elster.jupiter.demo.impl;

import com.elster.jupiter.demo.impl.factories.Factory;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class Log {
    private static final boolean IS_PRODUCTION = false;

    public static <T extends Factory<?>> void write(T factory){
        if (factory != null && !IS_PRODUCTION){
            Field[] factoryFields = factory.getClass().getDeclaredFields();
            StringBuilder log = new StringBuilder();
            boolean hasParameters = false;
            try {
                for (Field field : factoryFields) {
                    if (!Modifier.isFinal(field.getModifiers())){
                        hasParameters = true;
                        field.setAccessible(true);
                        log.append("\t\n ").append(field.getName()).append(" = ").append(objToReadableString(field.get(factory)));
                    }
                }
                if (hasParameters){
                    log.insert(0, ". Parameters: ");
                }
                log.insert(0, whatCreate(factory));
                log.insert(0, " ==> Creating a ");
            } catch (ReflectiveOperationException e) {
                // ignore it, this output is only for dev team
            }
            System.out.println(log.toString());
        }
    }

    private static <T extends Factory<?>> String whatCreate(T factory){
        try {
            Method getMethod = factory.getClass().getDeclaredMethod("get");
            return getMethod.getReturnType().getSimpleName();
        } catch (NoSuchMethodException e) {
            // ignore it, this output is only for dev team
        }
        return " [unable to determine] ";
    }

    private static <T> String objToReadableString(T obj){
        if (obj != null){
            String out = obj.toString();
            if (out.contains("@")){ // standard serialization
                StringBuilder readableOutput = new StringBuilder(obj.getClass().getSimpleName());
                readableOutput.append(" [");
                if (obj instanceof HasId){
                    readableOutput.append("id = ").append(((HasId) obj).getId());
                }
                if (obj instanceof HasName){
                    readableOutput.append(" name = ").append(((HasName) obj).getName());
                }
                readableOutput.append("]");
                out = readableOutput.toString();
            }
            return out;
        }
        return "null";
    }
}

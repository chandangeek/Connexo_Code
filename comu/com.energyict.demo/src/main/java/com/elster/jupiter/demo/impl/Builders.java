/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl;

import com.elster.jupiter.demo.impl.builders.Builder;
import com.elster.jupiter.demo.impl.templates.Template;

import com.google.inject.Injector;

public class Builders {
    private static Injector injector;

    private static void checkState() {
        if (Builders.injector == null){
            throw new IllegalStateException("Builders class is not initialized");
        }
    }

    static void initWith(Injector injector){
        Builders.injector = injector;
    }

    public static <T, B extends Builder<T>> B from(Template<T, B> template){
        checkState();
        return template.get(Builders.injector.getInstance(template.getBuilderClass()));
    }


    public static <T, B extends Builder<T>> B from(Class<B> clazz){
        checkState();
        return Builders.injector.getInstance(clazz);
    }
}

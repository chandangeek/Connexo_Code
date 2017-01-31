/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.test.api.rest.impl;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component(name = "com.energyict.mdc.test.api.rest",
        service = {Application.class},
        immediate = true,
        property = {"alias=/testmdc", "app=MDC", "name=" + MdcTestApiApplication.COMPONENT_NAME})
public class MdcTestApiApplication extends Application {

    public static final String COMPONENT_NAME = "MDC_TEST";

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    class HK2Binder extends AbstractBinder {
        @Override
        public void configure() {
            // nothing to configure
        }
    }
}

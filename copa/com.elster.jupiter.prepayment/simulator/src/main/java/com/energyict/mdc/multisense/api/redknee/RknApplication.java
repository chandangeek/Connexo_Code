/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.redknee;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Named;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by bvn on 9/17/15.
 */
public class RknApplication extends Application {
    private final ConsumptionExportGenerator consumptionExportGenerator;
    private final Configuration configuration;

    public RknApplication(ConsumptionExportGenerator consumptionExportGenerator, Configuration configuration) {
        this.consumptionExportGenerator = consumptionExportGenerator;
        this.configuration = configuration;
    }

    public Set<Class<?>> getClasses() {
        Set<Class<?>> set = new HashSet<>();
        set.add(RknProxyResource.class);
        set.add(RequestSecurityFilter.class);
        set.add(SimExceptionMapper.class);
        return Collections.unmodifiableSet(set);
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Named
    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(consumptionExportGenerator).to(ConsumptionExportGenerator.class);
            bind(configuration).to(Configuration.class);
        }
    }
}

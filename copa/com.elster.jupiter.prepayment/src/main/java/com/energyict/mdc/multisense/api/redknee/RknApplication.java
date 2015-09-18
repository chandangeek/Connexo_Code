package com.energyict.mdc.multisense.api.redknee;

import jersey.repackaged.com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by bvn on 9/17/15.
 */
public class RknApplication extends Application {
    private final ConsumptionExportGenerator consumptionExportGenerator;

    public RknApplication(ConsumptionExportGenerator consumptionExportGenerator) {
        this.consumptionExportGenerator = consumptionExportGenerator;
    }

    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(RknProxyResource.class);
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(consumptionExportGenerator).to(ConsumptionExportGenerator.class);
        }
    }



}

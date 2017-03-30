/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.datavault.DataVault;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bvn on 5/7/15.
 */
public class DataVaultServiceImplTest {

    @Test
    public void testGuiceSingletonInjection() throws Exception {

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            public void configure() {
                bind(DataVault.class).toProvider(MyDataVaultProvider.class).in(Singleton.class);
            }
        });
        injector.getInstance(DataVault.class);
        injector.getInstance(DataVault.class);
        injector.getInstance(DataVault.class);
        injector.getInstance(DataVault.class);
        assertThat(MyDataVaultProvider.count).isEqualTo(1);
    }

    private static class MyDataVaultProvider implements Provider<DataVault> {
        public static int count;

        @Override
        public DataVault get() {
            count++;
            return null;
        }
    }

}
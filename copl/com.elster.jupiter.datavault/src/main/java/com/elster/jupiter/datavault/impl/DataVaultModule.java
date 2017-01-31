/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Module intended for use by integration tests
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:49)
 */
public class DataVaultModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(NlsService.class);

        bind(DataVaultService.class).to(DataVaultServiceImpl.class).in(Scopes.SINGLETON);
    }

    public static class FakeDataVaultService implements DataVaultService {

        private FakeDataVaultService() {

        }

        public static DataVaultService getInstance() {
            return new FakeDataVaultService();
        }

        @Override
        public String encrypt(byte[] decrypted) {
            return new String(decrypted);
        }

        @Override
        public byte[] decrypt(String encrypted) {
            return encrypted.getBytes();
        }
    }
}
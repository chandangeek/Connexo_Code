/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.security.NoSuchAlgorithmException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bbl on 9/03/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class KeyStoreImplTest {

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private Injector injector;
    private TransactionService transactionService;

    private BasicAuthentication authentication;

    @Mock
    private DataModel dataModel;
    @Mock
    private DataVaultService dataVaultService;

    public void setupH2() {
        try {
            injector = Guice.createInjector(
                    inMemoryBootstrapModule,
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new TransactionModule(),
                    new NlsModule(),
                    new DataVaultModule(),
                    new UserModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            authentication = injector.getInstance(BasicAuthentication.class);
            return null;
        });
    }


    @Test
    public void testKeyStore() throws NoSuchAlgorithmException {
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        when(dataVaultService.encrypt(captor.capture())).thenReturn("key1", "key2");
        DataMapper mapper = mock(DataMapper.class);
        when(dataModel.mapper(eq(KeyStoreImpl.class))).thenReturn(mapper);
        KeyStoreImpl instance = new KeyStoreImpl(dataModel);
        instance.init(dataVaultService);

        verify(mapper).persist(instance);
        assertThat(instance.getPublicKey()).isEqualTo("key1");
        assertThat(instance.getPrivateKey()).isEqualTo("key2");

        assertThat(captor.getAllValues()).hasSize(2);
    }

}
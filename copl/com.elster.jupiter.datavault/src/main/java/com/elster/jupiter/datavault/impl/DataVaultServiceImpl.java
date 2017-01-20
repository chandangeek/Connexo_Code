package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.datavault.DataVault;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * Created by bvn on 11/6/14.
 */
@Component(
        name = "com.elster.kore.datavault",
        service = {DataVaultService.class, MessageSeedProvider.class},
        immediate = true)
public final class DataVaultServiceImpl implements DataVaultService, MessageSeedProvider {

    private volatile ServerKeyStoreService keyStoreService;

    public DataVaultServiceImpl() {
    }

    @Inject
    public DataVaultServiceImpl(ServerKeyStoreService keyStoreService) {
        this();
        setKeyStoreService(keyStoreService);
    }

    @Reference
    public void setKeyStoreService(ServerKeyStoreService keyStoreService) {
        this.keyStoreService = keyStoreService;
    }

    @Override
    public String encrypt(byte[] plainText) {
        DataVault dataVault = this.keyStoreService.getDataVaultInstance();
        return dataVault.encrypt(plainText);
    }

    @Override
    public byte[] decrypt(String encrypted) {
        DataVault dataVault = this.keyStoreService.getDataVaultInstance();
        return dataVault.decrypt(encrypted);
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

}
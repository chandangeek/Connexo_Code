package com.elster.jupiter.pki.impl;

import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.properties.Expiration;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Copyrights EnergyICT
 * Date: 3/10/2017
 * Time: 13:02
 */
public class DataVaultPrivateKeyFactoryTest {

    private static PkiInMemoryPersistence inMemoryPersistence = new PkiInMemoryPersistence();

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence.activate();
    }

    @Test
    public void findExpiredKeysTest(){
        List<PrivateKeyWrapper> privateKeys = inMemoryPersistence.getDataVaultPrivateKeyFactory().findExpired(new Expiration(Expiration.Type.EXPIRED), inMemoryPersistence.getClock().instant());
    }

    @Test
    public void findExpiredWithin1WeekKeysTest(){
        List<PrivateKeyWrapper> privateKeys = inMemoryPersistence.getDataVaultPrivateKeyFactory().findExpired(new Expiration(Expiration.Type.EXPIRES_1WEEK), inMemoryPersistence.getClock().instant());
    }

    @Test
    public void findExpiredWithin1MonthKeysTest(){
        List<PrivateKeyWrapper> privateKeys = inMemoryPersistence.getDataVaultPrivateKeyFactory().findExpired(new Expiration(Expiration.Type.EXPIRES_1MONTH), inMemoryPersistence.getClock().instant());
    }

    @Test
    public void findExpiredWithin3MonthKeysTest(){
        List<PrivateKeyWrapper> privateKeys = inMemoryPersistence.getDataVaultPrivateKeyFactory().findExpired(new Expiration(Expiration.Type.EXPIRES_3MONTHS), inMemoryPersistence.getClock().instant());
    }
}

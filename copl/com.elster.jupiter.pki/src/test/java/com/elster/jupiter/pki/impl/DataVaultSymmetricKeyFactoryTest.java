package com.elster.jupiter.pki.impl;

import com.elster.jupiter.pki.ExpirationSupport;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.properties.Expiration;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Copyrights EnergyICT
 * Date: 3/10/2017
 * Time: 13:02
 */
public class DataVaultSymmetricKeyFactoryTest {

    private static PkiInMemoryPersistence inMemoryPersistence = new PkiInMemoryPersistence();

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence.activate();
    }
    @AfterClass
    public static void uninstall(){
        inMemoryPersistence.deactivate();
    }
    @Test
    public void findExpiredTest(){
        List<SecurityValueWrapper> symmetricKeys = ((ExpirationSupport) inMemoryPersistence.getDataVaultSymmetricKeyFactory()).findExpired(new Expiration(Expiration.Type.EXPIRED), inMemoryPersistence.getClock().instant());
    }

    @Test
    public void findExpiredWithin1WeekKeysTest(){
        List<SecurityValueWrapper> symmetricKeys = ((ExpirationSupport) inMemoryPersistence.getDataVaultSymmetricKeyFactory()).findExpired(new Expiration(Expiration.Type.EXPIRES_1WEEK), inMemoryPersistence.getClock().instant());
    }

    @Test
    public void findExpiredWithin1MonthKeysTest(){
        List<SecurityValueWrapper> symmetricKeys = ((ExpirationSupport) inMemoryPersistence.getDataVaultSymmetricKeyFactory()).findExpired(new Expiration(Expiration.Type.EXPIRES_1MONTH), inMemoryPersistence.getClock().instant());
    }

    @Test
    public void findExpiredWithin3MonthKeysTest(){
        List<SecurityValueWrapper> symmetricKeys = ((ExpirationSupport) inMemoryPersistence.getDataVaultSymmetricKeyFactory()).findExpired(new Expiration(Expiration.Type.EXPIRES_3MONTHS), inMemoryPersistence.getClock().instant());
    }
}

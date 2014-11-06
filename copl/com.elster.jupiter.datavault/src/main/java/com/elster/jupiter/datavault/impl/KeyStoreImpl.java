package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyStore;
import java.util.Random;
import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 *
 * Enables data vault functionality by providing a Java key store from a UserFile
 *
 * @since 10/4/12 10:48 AM
 */
public class KeyStoreImpl extends KeyStoreDataVault {

    private final ExceptionFactory exceptionFactory;
    private final DataModel dataModel;

    enum Fields {
        STORE_DATA("keyStore"),
        ID("id");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private long id;
    private byte[] keyStore;

    @Inject
    public KeyStoreImpl(ExceptionFactory exceptionFactory, DataModel dataModel, Random random) {
        super(random, exceptionFactory);
        this.exceptionFactory = exceptionFactory;
        this.dataModel = dataModel;
    }

    protected KeyStore readKeyStore(char[] password)  {
        try {
            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            final ByteArrayInputStream stream = new ByteArrayInputStream(this.keyStore);
            keyStore.load(stream, password);
            return keyStore;
        } catch (Exception e) {
            throw exceptionFactory.newException(MessageSeeds.KEYSTORE_LOAD_FILE);
        }
    }

    public KeyStoreImpl createNewDataVault() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        super.createVault(outputStream);
        this.keyStore = outputStream.toByteArray();
        this.save();
        return this;
    }

    final public void save() {
        Save.action(this.id).save(dataModel, this);
    }

    public boolean hasKeyStore() {
        return false;
    }

}

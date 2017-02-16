package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.datavault.PersistentKeyStore;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Objects;

/**
 * Serves as the root for the implementation class hierarchy for {@link PersistentKeyStore}.
 *
 * @since 10/4/12 10:48 AM
 */
abstract class PersistentKeyStoreImpl implements PersistentKeyStore {

    static final Map<String, Class<? extends PersistentKeyStore>> IMPLEMENTERS =
            ImmutableMap.of(
                    "D", DataVaultKeyStore.class,
                    "U", UserDefinedKeyStore.class,
                    "S", SystemDefinedKeyStore.class);

    private final DataModel dataModel;
    private final ExceptionFactory exceptionFactory;

    enum Fields {
        STORE_DATA("keyStoreBytes"),
        NAME("name"),
        TYPE("type"),
        ID("id");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String type;
    private byte[] keyStoreBytes;

    @Inject
    PersistentKeyStoreImpl(DataModel dataModel, ExceptionFactory exceptionFactory) {
        this.dataModel = dataModel;
        this.exceptionFactory = exceptionFactory;
    }

    byte[] getKeyStoreBytes() {
        return keyStoreBytes;
    }

    void setKeyStoreBytesFrom(ByteArrayOutputStream outputStream) {
        this.keyStoreBytes = outputStream.toByteArray();
    }

    final void save() {
        Save.action(this.id).save(this.dataModel, this);
    }

    @Override
    public long getId() {
        return id;
    }

    String getType() {
        return type;
    }

    void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PersistentKeyStoreImpl that = (PersistentKeyStoreImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public KeyStore forReading(char[] password) {
        try {
            KeyStore keyStore = KeyStore.getInstance(this.getType());
            try (InputStream inputStream = new ByteArrayInputStream(this.getKeyStoreBytes())) {
                keyStore.load(inputStream, password);
            } catch (CertificateException | IOException | NoSuchAlgorithmException e) {
                throw exceptionFactory.newException(MessageSeeds.KEYSTORE_LOAD_FILE);
            }
            return keyStore;
        } catch (KeyStoreException e) {
            throw exceptionFactory.newException(MessageSeeds.KEYSTORE_LOAD_FILE);
        }
    }

    @Override
    public Updater forUpdating(char[] password) {
        return new UpdaterImpl(this.forReading(password));
    }

    private class UpdaterImpl implements Updater {
        private final KeyStore keyStore;

        private UpdaterImpl(KeyStore keyStore) {
            this.keyStore = keyStore;
        }

        @Override
        public Updater setEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protParam) throws KeyStoreException {
            this.keyStore.setEntry(alias, entry, protParam);
            return this;
        }

        @Override
        public Updater setCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
            this.keyStore.setCertificateEntry(alias, cert);
            return this;
        }

        @Override
        public Updater setKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
            this.keyStore.setKeyEntry(alias, key, chain);
            return this;
        }

        @Override
        public Updater setKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
            this.keyStore.setKeyEntry(alias, key, password, chain);
            return this;
        }

        @Override
        public void save() {
            PersistentKeyStoreImpl.this.save();
        }
    }

}
package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.pki.impl.MessageSeeds;

import com.google.common.collect.ImmutableMap;

import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public abstract class KeyImpl implements SymmetricKeyWrapper  {

    private DataModel dataModel;

    private long id;
    @Size(max = Table.MAX_STRING_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String encryptedKey;

    private Reference<KeyType> keyTypeReference = Reference.empty();
    private Instant expirationTime;

    KeyImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    String getEncryptedKey() {
        return encryptedKey;
    }

    void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    Reference<KeyType> getKeyTypeReference() {
        return keyTypeReference;
    }

    @Override
    public Optional<Instant> getExpirationTime() {
        return Optional.ofNullable(expirationTime);
    }

    void setExpirationTime(Instant expirationTime) {
        this.expirationTime = expirationTime;
    }

    DataModel getDataModel() {
        return dataModel;
    }

    void save() {
        Save.action(id).save(dataModel, this);
    }

    @Override
    public void delete() {
        dataModel.remove(this);
    }

    public enum Fields {
        ENCRYPTED_KEY("encryptedKey"),
        LABEL("label"),
        SMARTMETER_KEY("smartMeterKey"),
        KEY_TYPE("keyTypeReference"),
        EXPIRATION("expirationTime"),;

        private final String fieldName;

        Fields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String fieldName() {
            return fieldName;
        }
    }

    public static final Map<String, Class<? extends SymmetricKeyWrapper>> IMPLEMENTERS =
            ImmutableMap.of(
                    "H", HsmKeyImpl.class,
                    "P", PlaintextSymmetricKeyImpl.class);

}

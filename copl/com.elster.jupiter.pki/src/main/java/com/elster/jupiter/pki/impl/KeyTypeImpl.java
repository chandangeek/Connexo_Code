package com.elster.jupiter.pki.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyType;

import javax.inject.Inject;
import javax.validation.constraints.Size;

public class KeyTypeImpl implements KeyType {
    private final DataModel dataModel;

    private long id;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private String name;
    private CryptographicType cryptographicType;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String algorithm;
    private Integer keySize;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String curve;

    enum Fields {
        NAME("name"),
        CRYPTOGRAPHIC_TYPE("cryptographicType"),
        ALGORITHM("algorithm"),
        KEY_SIZE("keySize"),
        CURVE("curve"),
        ;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @Inject
    public KeyTypeImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public CryptographicType getCryptographicType() {
        return cryptographicType;
    }

    public void setCryptographicType(CryptographicType cryptographicType) {
        this.cryptographicType = cryptographicType;
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public Integer getKeySize() {
        return keySize;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    @Override
    public String getCurve() {
        return curve;
    }

    public void setCurve(String curve) {
        this.curve = curve;
    }

    public void save() {
        Save.action(id).save(dataModel, this);
    }
}

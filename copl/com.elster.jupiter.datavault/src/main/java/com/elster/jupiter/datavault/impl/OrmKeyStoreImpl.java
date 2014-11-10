package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import java.io.ByteArrayOutputStream;
import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 *
 * Enables data vault functionality by providing a Java key store from a UserFile
 *
 * @since 10/4/12 10:48 AM
 */
public class OrmKeyStoreImpl {

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
    public OrmKeyStoreImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void setKeyStore(ByteArrayOutputStream outputStream) {
        this.keyStore = outputStream.toByteArray();
    }

    final public void save() {
        Save.action(this.id).save(dataModel, this);
    }

    public long getId() {
        return id;
    }


    public byte[] getKeyStoreBytes() {
        return keyStore;
    }
}

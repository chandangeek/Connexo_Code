/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;

class OrmKeyStoreImpl {

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

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    private byte[] keyStore;

    @Inject
    OrmKeyStoreImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    void setKeyStore(ByteArrayOutputStream outputStream) {
        this.keyStore = outputStream.toByteArray();
    }

    public final void save() {
        Save.action(this.id).save(dataModel, this);
    }

    public long getId() {
        return id;
    }

    byte[] getKeyStoreBytes() {
        return keyStore;
    }

}
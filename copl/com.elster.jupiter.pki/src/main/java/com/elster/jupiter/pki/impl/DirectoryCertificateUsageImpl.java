/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.DirectoryCertificateUsage;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.users.UserDirectory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;

public class DirectoryCertificateUsageImpl implements DirectoryCertificateUsage {
    public enum Fields {
        // Common fields
        DIRECTORY("directory"),
        CERTIFICATE("certificate"),
        TRUSTSTORE("trustStore"),
        ;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<UserDirectory> directory  = Reference.empty();
    private Reference<TrustStore> trustStore = Reference.empty();
    private Reference<CertificateWrapper> certificate = Reference.empty();

    private long id;

    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    private final DataModel dataModel;

    @Inject
    public DirectoryCertificateUsageImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    DirectoryCertificateUsage init(UserDirectory directory) {
        this.directory.set(directory);
        return this;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public UserDirectory getDirectory() {
        return directory.get();
    }

    @Override
    public String getDirectoryName() {
        return directory.get().getDomain();
    }

    @Override
    public Optional<TrustStore> getTrustStore() {
        return trustStore.getOptional();
    }

    @Override
    public void setTrustStore(TrustStore trustStore) {
        this.trustStore.set(trustStore);
    }

    @Override
    public Optional<CertificateWrapper> getCertificate() {
        return certificate.getOptional();
    }

    @Override
    public void setCertificate(CertificateWrapper certificate) {
        this.certificate.set(certificate);
    }

    @Override
    public void save() {
        if (id != 0) {
            doUpdate();
        } else {
            doPersist();
        }
    }

    private void doPersist() {
        Save.CREATE.save(dataModel, this);
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
    }

    @Override
    public void delete() {
        dataModel.remove(this);
    }
}

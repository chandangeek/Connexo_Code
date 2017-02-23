/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.pki.impl.wrappers.certificate.TrustedCertificateImpl;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_UNIQUE + "}")
public class TrustStoreImpl implements TrustStore {

    private final DataModel dataModel;
    private final PkiService pkiService;

    public enum Fields {
        NAME("name"),
        DESCRIPTION("description"),
        CERTIFICATES("trustedCertificates"),
        ;

        private final String fieldName;

        Fields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String fieldName() {
            return fieldName;
        }
    }

    private long id;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private String name;
    @Size(max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String description;

    private List<TrustedCertificate> trustedCertificates = new ArrayList<>();

    @Inject
    public TrustStoreImpl(DataModel dataModel, PkiService pkiService) {
        this.dataModel = dataModel;
        this.pkiService = pkiService;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public List<TrustedCertificate> getCertificates() {
        return ImmutableList.copyOf(this.trustedCertificates);
    }

    @Override
    public void setCertificate(X509Certificate x509Certificate) {
        TrustedCertificateImpl trustedCertificate = (TrustedCertificateImpl) pkiService.newTrustedCertificateWrapper(this);
        trustedCertificate.setCertificate(x509Certificate);
        trustedCertificate.save();
        this.trustedCertificates.add(trustedCertificate);
        this.save();
    }

    @Override
    public void removeCertificate(X509Certificate x509Certificate) {

    }

    public void save() {
        Save.action(id).save(dataModel, this);
    }
}

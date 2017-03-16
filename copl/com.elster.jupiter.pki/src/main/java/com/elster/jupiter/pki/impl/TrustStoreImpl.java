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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

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

    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;


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
    public TrustedCertificate addCertificate(String alias, X509Certificate x509Certificate) {
        TrustedCertificateImpl trustedCertificate = dataModel.getInstance(TrustedCertificateImpl.class);
        trustedCertificate.init(this, alias, x509Certificate);
        trustedCertificate.save();
        this.trustedCertificates.add(trustedCertificate);
        this.save();
        return trustedCertificate;
    }

    @Override
    public void removeCertificate(String alias) {
        List<TrustedCertificate> toBeRemoved = this.trustedCertificates.stream()
                .filter(trustedCertificate -> trustedCertificate.getAlias().equals(alias))
                .collect(toList());
        this.trustedCertificates.removeAll(toBeRemoved);
        toBeRemoved.stream().forEach(dataModel::remove);
    }

    @Override
    public long getVersion() {
        return version;
    }

    public void save() {
        Save.action(id).save(dataModel, this);
    }
}

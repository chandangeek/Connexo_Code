/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.accessors;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.pki.impl.EventType;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.properties.PropertySpec;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public abstract class AbstractSecurityAccessorImpl<T extends SecurityValueWrapper> implements SecurityAccessor<T> {
    private final SecurityManagementService securityManagementService;
    private final DataModel dataModel;
    private final FileImportService fileImportService;
    private final Thesaurus thesaurus;
    private final EventService eventService;

    private Reference<SecurityAccessorType> keyAccessorTypeReference = Reference.empty();
    private boolean swapped;

    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    public static final Map<String, Class<? extends SecurityAccessor>> IMPLEMENTERS =
            ImmutableMap.of(
                    "C", CertificateAccessorImpl.class
            );

    protected AbstractSecurityAccessorImpl(SecurityManagementService securityManagementService,
                                           DataModel dataModel,
                                           FileImportService fileImportService,
                                           Thesaurus thesaurus,
                                           EventService eventService) {
        this.securityManagementService = securityManagementService;
        this.dataModel = dataModel;
        this.fileImportService = fileImportService;
        this.thesaurus = thesaurus;
        this.eventService = eventService;
    }

    public enum Fields {
        KEY_ACCESSOR_TYPE("keyAccessorTypeReference"),
        SWAPPED("swapped"),
        CERTIFICATE_WRAPPER_ACTUAL("actualCertificate"),
        CERTIFICATE_WRAPPER_TEMP("tempCertificate"),;

        private final String fieldName;

        Fields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String fieldName() {
            return fieldName;
        }
    }

    public void init(SecurityAccessorType securityAccessorType) {
        this.keyAccessorTypeReference.set(securityAccessorType);
    }

    @Override
    public SecurityAccessorType getSecurityAccessorType() {
        return keyAccessorTypeReference.get();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return securityManagementService.getPropertySpecs(getSecurityAccessorType());
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void swapValues() {
        this.swapped = !swapped;
    }

    @Override
    public void clearTempValue() {
        this.swapped = false;
    }

    public abstract void clearActualValue();

    @Override
    public boolean isSwapped() {
        return swapped;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public void delete() {
        if (fileImportService.doImportersUse(this)) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.SECURITY_ACCESSOR_USED_BY_IMPORT);
        }
        eventService.postEvent(EventType.SECURITY_ACCESSOR_VALIDATE_DELETE.topic(), this);
        dataModel.remove(this);
    }

    @Override
    public void save() {
        Save.UPDATE.save(dataModel, this);
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected SecurityManagementService getSecurityManagementService() {
        return securityManagementService;
    }
}

/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.parent;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.TranslationKeys;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class AbstractMasterCustomPropertySet<E extends AbstractMasterDomainExtension>
        implements CustomPropertySet<ServiceCall, E> {

    public abstract PropertySpecService getPropertySpecService();

    public abstract Thesaurus getThesaurus();

    @Override
    public abstract String getName();

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.DOMAIN_NAME).format();
    }

    @Override
    public abstract PersistenceSupport<ServiceCall, E> getPersistenceSupport();

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public boolean isVersioned() {
        return false;
    }

    @Override
    public Set<ViewPrivilege> defaultViewPrivileges() {
        return Collections.emptySet();
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return Collections.emptySet();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                getPropertySpecService().bigDecimalSpec()
                        .named(AbstractMasterDomainExtension.FieldNames.CALLS_SUCCESS.javaName(),
                                TranslationKeys.CALLS_SUCCESS)
                        .describedAs(TranslationKeys.CALLS_SUCCESS).fromThesaurus(getThesaurus()).finish(),
                getPropertySpecService().bigDecimalSpec()
                        .named(AbstractMasterDomainExtension.FieldNames.CALLS_FAILED.javaName(),
                                TranslationKeys.CALLS_ERROR)
                        .describedAs(TranslationKeys.CALLS_ERROR).fromThesaurus(getThesaurus()).finish(),
                getPropertySpecService().bigDecimalSpec()
                        .named(AbstractMasterDomainExtension.FieldNames.CALLS_EXPECTED.javaName(),
                                TranslationKeys.CALLS_EXPECTED)
                        .describedAs(TranslationKeys.CALLS_EXPECTED).fromThesaurus(getThesaurus()).finish(),
                getPropertySpecService().stringSpec()
                        .named(AbstractMasterDomainExtension.FieldNames.CALLBACK_URL.javaName(),
                                TranslationKeys.CALLBACK_URL)
                        .describedAs(TranslationKeys.CALLBACK_URL).fromThesaurus(getThesaurus()).finish(),
                getPropertySpecService().stringSpec()
                        .named(AbstractMasterDomainExtension.FieldNames.CORRELATION_ID.javaName(),
                            TranslationKeys.CORRELATION_ID)
                        .describedAs(TranslationKeys.CORRELATION_ID).fromThesaurus(getThesaurus()).finish());
    }

}

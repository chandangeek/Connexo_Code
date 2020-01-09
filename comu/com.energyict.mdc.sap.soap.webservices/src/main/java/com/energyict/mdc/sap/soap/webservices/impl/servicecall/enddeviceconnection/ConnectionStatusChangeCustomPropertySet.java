/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.TranslationKeys;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ConnectionStatusChangeCustomPropertySet implements CustomPropertySet<ServiceCall, ConnectionStatusChangeDomainExtension> {
    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;

    public ConnectionStatusChangeCustomPropertySet() {
    }

    @Inject
    public ConnectionStatusChangeCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.CONNECTION_STATUS_CHANGE_CPS).format();
    }

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return thesaurus.getFormat(TranslationKeys.DOMAIN_NAME).format();
    }

    @Override
    public PersistenceSupport<ServiceCall, ConnectionStatusChangeDomainExtension> getPersistenceSupport() {
        return new ConnectionStatusChangePersistenceSupport();
    }

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
                propertySpecService
                        .stringSpec()
                        .named(ConnectionStatusChangeDomainExtension.FieldNames.ID.javaName(), TranslationKeys.ID)
                        .describedAs(TranslationKeys.ID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                propertySpecService
                        .stringSpec()
                        .named(ConnectionStatusChangeDomainExtension.FieldNames.REQUEST_ID.javaName(), TranslationKeys.REQUEST_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .stringSpec()
                        .named(ConnectionStatusChangeDomainExtension.FieldNames.UUID.javaName(), TranslationKeys.UUID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .stringSpec()
                        .named(ConnectionStatusChangeDomainExtension.FieldNames.CATEGORY_CODE.javaName(), TranslationKeys.CATEGORY_CODE)
                        .describedAs(TranslationKeys.CATEGORY_CODE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .stringSpec()
                        .named(ConnectionStatusChangeDomainExtension.FieldNames.REASON_CODE.javaName(), TranslationKeys.REASON_CODE)
                        .describedAs(TranslationKeys.REASON_CODE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(ConnectionStatusChangeDomainExtension.FieldNames.PROCESS_DATE.javaName(), TranslationKeys.PROCESS_DATE)
                        .describedAs(TranslationKeys.PROCESS_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(ConnectionStatusChangeDomainExtension.FieldNames.BULK.javaName(), TranslationKeys.BULK)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(ConnectionStatusChangeDomainExtension.FieldNames.CANCELLED_BY_SAP.javaName(), TranslationKeys.CANCELLED_BY_SAP)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish()
        );
    }
}

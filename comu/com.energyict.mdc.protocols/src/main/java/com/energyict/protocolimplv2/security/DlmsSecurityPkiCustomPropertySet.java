/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.security.SecurityCustomPropertySet;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link DlmsSecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (14:23)
 */
public class DlmsSecurityPkiCustomPropertySet extends SecurityCustomPropertySet<DlmsSecurityPkiProperties> {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    public DlmsSecurityPkiCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.SECURITY_PROPERTY_SET_CPS_DOMAIN_NAME).format();
    }

    @Override
    public DlmsSecurityPkiPersistenceSupport getPersistenceSupport() {
        return new DlmsSecurityPkiPersistenceSupport();
    }

    @Override
    public String getName() {
        return "DLMS PKI Security";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        //The property specs for this security set are defined in the 9.1 protocol code base
        DeviceProtocolSecurityCapabilities dlmsSecuritySupport = new com.energyict.protocolimplv2.security.DlmsSecuritySupport(propertySpecService);

        return dlmsSecuritySupport.getSecurityProperties()
                .stream()
                .map(UPLToConnexoPropertySpecAdapter::new)
                .collect(Collectors.toList());

//        return Arrays.asList(
//                DeviceSecurityProperty.PASSWORD.getPropertySpec(this.propertySpecService, this.thesaurus),
//                DeviceSecurityProperty.ENCRYPTION_KEY_WITH_KEY_ACCESSOR.getPropertySpec(this.propertySpecService, this.thesaurus),
//                DeviceSecurityProperty.AUTHENTICATION_KEY_WITH_KEY_ACCESSOR.getPropertySpec(this.propertySpecService, this.thesaurus));
    }

}
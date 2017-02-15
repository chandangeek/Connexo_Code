/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.serial.optical.dlms;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.protocols.impl.channels.CustomPropertySetTranslationKeys;
import com.energyict.protocols.impl.channels.TranslationKeys;
import com.energyict.protocols.impl.channels.serial.optical.serialio.SioOpticalConnectionType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.energyict.protocols.impl.channels.serial.optical.dlms.LegacyOpticalDlmsConnectionProperties.DEFAULT_ADDRESSING_MODE;
import static com.energyict.protocols.impl.channels.serial.optical.dlms.LegacyOpticalDlmsConnectionProperties.DEFAULT_SERVER_LOWER_MAC_ADDRESS;
import static com.energyict.protocols.impl.channels.serial.optical.dlms.LegacyOpticalDlmsConnectionProperties.DEFAULT_SERVER_MAC_ADDRESS;
import static com.energyict.protocols.impl.channels.serial.optical.dlms.LegacyOpticalDlmsConnectionProperties.DEFAULT_SERVER_UPPER_MAC_ADDRESS;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface
 * for the {@link LegacyOpticalDlmsConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-10 (13:44)
 */
public class LegacyOpticalDlmsCustomPropertySet implements CustomPropertySet<ConnectionProvider, LegacyOpticalDlmsConnectionProperties> {

    private final Thesaurus thesaurus;
    private final SioOpticalConnectionType actualConnectionType;
    private final PropertySpecService propertySpecService;

    public LegacyOpticalDlmsCustomPropertySet(Thesaurus thesaurus, SioOpticalConnectionType actualConnectionType, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.actualConnectionType = actualConnectionType;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getId() {
        return LegacyOpticalDlmsConnectionType.class.getSimpleName();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.LEGACY_OPTICAL_DLMS_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public Class<ConnectionProvider> getDomainClass() {
        return ConnectionProvider.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.CONNECTION_PROVIDER_DOMAIN_NAME).format();
    }

    @Override
    public PersistenceSupport<ConnectionProvider, LegacyOpticalDlmsConnectionProperties> getPersistenceSupport() {
        return new LegacyOpticalDlmsConnectionPropertiesPersistenceSupport();
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public boolean isVersioned() {
        return true;
    }

    @Override
    public Set<ViewPrivilege> defaultViewPrivileges() {
        return EnumSet.allOf(ViewPrivilege.class);
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return EnumSet.allOf(EditPrivilege.class);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(this.actualConnectionType.getPropertySpecs());
        propertySpecs.add(this.getAddressingModePropertySpec());
        propertySpecs.add(this.getDataLinkLayerTypePropertySpec());
        propertySpecs.add(this.getServerMacAddress());
        propertySpecs.add(this.getServerLowerMacAddress());
        propertySpecs.add(this.getServerUpperMacAddress());
        return propertySpecs;
    }


    private PropertySpec getAddressingModePropertySpec() {
        return this.propertySpecService
                .bigDecimalSpec()
                .named(TranslationKeys.LEGACY_OPTICAL_DLMS_ADDRESSING_MODE)
                .fromThesaurus(this.thesaurus)
                .markExhaustive()
                .addValues(
                    BigDecimal.ONE,
                    DEFAULT_ADDRESSING_MODE,
                    BigDecimal.valueOf(4))
                .setDefaultValue(DEFAULT_ADDRESSING_MODE).finish();
    }

    PropertySpec getDataLinkLayerTypePropertySpec() {
        return this.propertySpecService
                .bigDecimalSpec()
                .named(LegacyOpticalDlmsConnectionProperties.Field.DATA_LINK_LAYER_TYPE.javaName(), TranslationKeys.DATA_LINK_LAYER_TYPE)
                .fromThesaurus(this.thesaurus)
                .setDefaultValue(BigDecimal.ZERO)
                .finish();
    }

    private PropertySpec getServerMacAddress() {
        return this.propertySpecService
                .bigDecimalSpec()
                .named(LegacyOpticalDlmsConnectionProperties.Field.SERVER_MAC_ADDRESS.javaName(), TranslationKeys.SERVER_MAC_ADDRESS)
                .fromThesaurus(this.thesaurus)
                .setDefaultValue(DEFAULT_SERVER_MAC_ADDRESS)
                .finish();
    }

    private PropertySpec getServerLowerMacAddress() {
        return this.propertySpecService
                .bigDecimalSpec()
                .named(LegacyOpticalDlmsConnectionProperties.Field.SERVER_LOWER_MAC_ADDRESS.javaName(), TranslationKeys.SERVER_LOWER_MAC_ADDRESS)
                .fromThesaurus(this.thesaurus)
                .setDefaultValue(DEFAULT_SERVER_LOWER_MAC_ADDRESS)
                .finish();
    }

    private PropertySpec getServerUpperMacAddress() {
        return this.propertySpecService
                .bigDecimalSpec()
                .named(LegacyOpticalDlmsConnectionProperties.Field.SERVER_UPPER_MAC_ADDRESS.javaName(), TranslationKeys.SERVER_UPPER_MAC_ADDRESS)
                .fromThesaurus(this.thesaurus)
                .setDefaultValue(DEFAULT_SERVER_UPPER_MAC_ADDRESS)
                .finish();
    }

}
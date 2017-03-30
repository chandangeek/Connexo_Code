/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.idis.am130.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimplv2.dlms.idis.am500.properties.IDISProperties;

import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.CIPHERING_TYPE;

public class AM130Properties extends IDISProperties {

    public AM130Properties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new IDISSecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel(), DLMSConnectionException.REASON_ABORT_INVALID_FRAMECOUNTER);
        }
        return securityProvider;
    }

    @Override
    public boolean isBulkRequest() {
        return true;
    }

    @Override
    public ConformanceBlock getConformanceBlock() {
        ConformanceBlock conformanceBlock = super.getConformanceBlock();
        conformanceBlock.setGeneralBlockTransfer(useGeneralBlockTransfer());
        conformanceBlock.setGeneralProtection(getCipheringType().equals(CipheringType.GENERAL_DEDICATED) || getCipheringType().equals(CipheringType.GENERAL_GLOBAL));
        return conformanceBlock;
    }

    @Override
    public boolean useGeneralBlockTransfer() {
        return getProperties().<Boolean>getTypedProperty(USE_GBT, AM130ConfigurationSupport.USE_GBT_DEFAULT_VALUE);
    }

    @Override
    public int getGeneralBlockTransferWindowSize() {
        return getProperties().getTypedProperty(GBT_WINDOW_SIZE, AM130ConfigurationSupport.DEFAULT_GBT_WINDOW_SIZE).intValue();
    }

    @ProtocolProperty
    public CipheringType getCipheringType() {
        String cipheringDescription = getProperties().getTypedProperty(CIPHERING_TYPE, AM130ConfigurationSupport.DEFAULT_CIPHERING_TYPE.getDescription());
        for (CipheringType cipheringType : CipheringType.values()) {
            if (cipheringType.getDescription().equals(cipheringDescription)) {
                return cipheringType;
            }
        }
        return AM130ConfigurationSupport.DEFAULT_CIPHERING_TYPE;
    }
}
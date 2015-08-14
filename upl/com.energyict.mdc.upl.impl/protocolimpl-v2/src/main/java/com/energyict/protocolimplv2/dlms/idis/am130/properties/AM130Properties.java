package com.energyict.protocolimplv2.dlms.idis.am130.properties;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimplv2.dlms.idis.am500.properties.IDISProperties;

import static com.energyict.dlms.common.DlmsProtocolProperties.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 12/02/2015 - 9:18
 */
public class AM130Properties extends IDISProperties {

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new IDISSecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel());
        }
        return securityProvider;
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
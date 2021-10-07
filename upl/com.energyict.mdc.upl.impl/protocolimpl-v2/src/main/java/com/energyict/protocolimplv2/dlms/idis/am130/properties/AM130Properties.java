package com.energyict.protocolimplv2.dlms.idis.am130.properties;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.protocol.exception.DeviceConfigurationException;
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

    private static int SERIAL_NUMBER_TO_MAC_ADDRESS_LENGTH = 2;
    private static int SERIAL_NUMBER_TO_MAC_ADDRESS_OFFSET = 16;

    public static final String OVERWRITE_SERVER_LOWER_MAC_ADDRESS = "OverwriteServerLowerMacAddress";

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new IDISSecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel(), DLMSConnectionException.REASON_ABORT_INVALID_FRAMECOUNTER);
        }
        return securityProvider;
    }

    @Override
    public int getServerLowerMacAddress() {
        if (!isOverwriteServerLowerMacAddress())
            return super.getServerLowerMacAddress();
        return createServerLowerMacAddress();
    }

    private int createServerLowerMacAddress() {
        String serialNumber = getSerialNumber();
        if (serialNumber != null && serialNumber.length() >= SERIAL_NUMBER_TO_MAC_ADDRESS_LENGTH)
            try {
                String macAddress = serialNumber.substring(serialNumber.length() - SERIAL_NUMBER_TO_MAC_ADDRESS_LENGTH);
                return Integer.parseInt(macAddress) + SERIAL_NUMBER_TO_MAC_ADDRESS_OFFSET;
            } catch (NumberFormatException e) {
            }
        throw DeviceConfigurationException.invalidPropertyFormat(DlmsProtocolProperties.SYSTEM_IDENTIFIER, serialNumber, "Last " + SERIAL_NUMBER_TO_MAC_ADDRESS_LENGTH + " characters should be a number");
    }

    public boolean isOverwriteServerLowerMacAddress() {
        return getProperties().<Boolean>getTypedProperty(OVERWRITE_SERVER_LOWER_MAC_ADDRESS, false);
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
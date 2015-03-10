package com.energyict.protocolimplv2.security;

/**
 * Summarizes all the usable SecurityRelationType names.
 * Each different set of security properties requires a relationTable to be created.
 * This overview should eliminate overlap or duplications.
 * <p/>
 * <b>It is very import that you don't change the names as these will be used
 * as relationTable names for the security properties!</b>
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/01/13
 * Time: 16:54
 */
public enum SecurityRelationTypeName {

    NONE("NoneSecurity"),
    SIMPLE_PASSWORD("SimplePassword"),
    PASSWORD_AND_LEVEL("LevelAndPassword"),
    PASSWORD_AND_USER("UserAndPassword"),
    DLMS_SECURITY("DlmsSecurity"),
    CRYPTO_DLMS_SECURITY("CryptoDlmsSecurity"),
    DLMS_SECURITY_PER_CLIENT("DlmsSecurityPerClient"),
    WAVENIS_SECURITY("WavenisSecurity"),
    IEC1107_SECURITY("IEC1107Security"),
    ANSI_C12_SECURITY("AnsiC12Security"),
    EXTENDED_ANSI_C12_SECURITY("ExtendedAnsiC12Security"),
    NO_OR_PASSWORD_SECURITY("NoOrPasswordSecurity"),
    MTU155_SECURITY("MTU155Security"),
    GARNET_SECURITY("GarnetSecurity");

    private final String securityRelationTypeName;

    private SecurityRelationTypeName(String securityRelationTypeName) {
        this.securityRelationTypeName = securityRelationTypeName;
    }

    @Override
    public String toString() {
        return securityRelationTypeName;
    }
}

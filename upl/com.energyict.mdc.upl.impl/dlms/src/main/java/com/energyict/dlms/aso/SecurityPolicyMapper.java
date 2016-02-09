package com.energyict.dlms.aso;

import com.energyict.mdc.protocol.security.AdvancedDeviceProtocolSecurityPropertySet;
import com.energyict.protocolimplv2.security.DlmsSecuritySuite1And2Support;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 8/02/2016 - 13:30
 */
public class SecurityPolicyMapper {

    //The suite 0 policies
    public static final int SECURITYPOLICY_NONE = 0;
    public static final int SECURITYPOLICY_AUTHENTICATION = 1;
    public static final int SECURITYPOLICY_ENCRYPTION = 2;
    public static final int SECURITYPOLICY_BOTH = 3;

    public static final int REQUESTS_AUTHENTICATED_FLAG = 2;
    public static final int REQUESTS_ENCRYPTED_FLAG = 3;
    public static final int REQUESTS_SIGNED_FLAG = 4;
    public static final int RESPONSES_AUTHENTICATED_FLAG = 5;
    public static final int RESPONSES_ENCRYPTED_FLAG = 6;
    public static final int RESPONSES_SIGNED_FLAG = 7;
    private final AdvancedDeviceProtocolSecurityPropertySet advancedSecurityPropertySet;

    public SecurityPolicyMapper(AdvancedDeviceProtocolSecurityPropertySet advancedSecurityPropertySet) {
        this.advancedSecurityPropertySet = advancedSecurityPropertySet;
    }

    public boolean isRequestAuthenticated() {
        return advancedSecurityPropertySet.getMessageAuthenticationLevel() == DlmsSecuritySuite1And2Support.MessageAuthenticationLevels.OnlyRequestsAuthenticated.getId()
                || advancedSecurityPropertySet.getMessageAuthenticationLevel() == DlmsSecuritySuite1And2Support.MessageAuthenticationLevels.RequestsAndResponsesAuthenticated.getId();
    }

    public boolean isResponseAuthenticated() {
        return advancedSecurityPropertySet.getMessageAuthenticationLevel() == DlmsSecuritySuite1And2Support.MessageAuthenticationLevels.OnlyResponsesAuthenticated.getId()
                || advancedSecurityPropertySet.getMessageAuthenticationLevel() == DlmsSecuritySuite1And2Support.MessageAuthenticationLevels.RequestsAndResponsesAuthenticated.getId();
    }

    public boolean isRequestEncrypted() {
        return advancedSecurityPropertySet.getMessageEncryptionLevel() == DlmsSecuritySuite1And2Support.MessageEncryptionLevels.OnlyRequestsEncrypted.getId()
                || advancedSecurityPropertySet.getMessageEncryptionLevel() == DlmsSecuritySuite1And2Support.MessageEncryptionLevels.RequestsAndResponsesEncrypted.getId();
    }

    public boolean isResponseEncrypted() {
        return advancedSecurityPropertySet.getMessageEncryptionLevel() == DlmsSecuritySuite1And2Support.MessageEncryptionLevels.OnlyResponsesEncrypted.getId()
                || advancedSecurityPropertySet.getMessageEncryptionLevel() == DlmsSecuritySuite1And2Support.MessageEncryptionLevels.RequestsAndResponsesEncrypted.getId();
    }

    public boolean isRequestSigned() {
        return advancedSecurityPropertySet.getMessageSigningLevel() == DlmsSecuritySuite1And2Support.MessageSigningLevels.OnlyRequestsSigned.getId()
                || advancedSecurityPropertySet.getMessageSigningLevel() == DlmsSecuritySuite1And2Support.MessageSigningLevels.RequestsAndResponsesSigned.getId();
    }

    public boolean isResponseSigned() {
        return advancedSecurityPropertySet.getMessageSigningLevel() == DlmsSecuritySuite1And2Support.MessageSigningLevels.OnlyResponsesSigned.getId()
                || advancedSecurityPropertySet.getMessageSigningLevel() == DlmsSecuritySuite1And2Support.MessageSigningLevels.RequestsAndResponsesSigned.getId();
    }
}
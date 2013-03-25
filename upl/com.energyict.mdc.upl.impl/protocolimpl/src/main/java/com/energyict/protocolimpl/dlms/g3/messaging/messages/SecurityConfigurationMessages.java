package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.*;

/**
 * Copyrights EnergyICT
 * Date: 7/13/12
 * Time: 11:02 AM
 */
public interface SecurityConfigurationMessages {

    String SECURITY_CONFIGURATION = "Security configuration";

    @RtuMessageDescription(category = SECURITY_CONFIGURATION, description = "Change HLS secret", tag = "ChangeHLSSecret")
    interface ChangeHLSSecretMessage extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "HLS_Secret", required = true)
        String getHLSSecret();

    }


    @RtuMessageDescription(category = SECURITY_CONFIGURATION, description = "Change LLS secret", tag = "ChangeLLSSecret")
    interface ChangeLLSSecretMessage extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "LLS_Secret", required = true)
        String getLLSSecret();

    }

    @RtuMessageDescription(category = SECURITY_CONFIGURATION, description = "Change authentication level", tag = "ChangeAuthenticationLevel")
    interface ChangeAuthenticationLevelMessage extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "Authentication_level", required = true)
        int getAuthenticationLevel();

    }

    @RtuMessageDescription(category = SECURITY_CONFIGURATION, description = "Activate security level", tag = "ActivateSecurityLevel")
    interface ActivateSecurityLevelMessage extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "Security_level", required = true)
        int getSecurityLevel();

    }

    @RtuMessageDescription(category = SECURITY_CONFIGURATION, description = "Change encryption key", tag = "ChangeEncryptionKey")
    interface ChangeEncryptionKeyMessage extends AnnotatedMessage {

        //No attributes, key is in properties

    }

    @RtuMessageDescription(category = SECURITY_CONFIGURATION, description = "Change authentication key", tag = "ChangeAuthenticationKey")
    interface ChangeAuthenticationKeyMessage extends AnnotatedMessage {

        //No attributes, key is in properties

    }
}
package com.elster.jupiter.hsm.model.keys;

import com.atos.worldline.jss.api.custom.energy.ProtectedSessionKeyCapability;

public enum SessionKeyCapability {

    /**
     *
     * Check {@link ProtectedSessionKeyCapability}
     *
     * These are mandatory parameters for different JSS operation and linked with label configuration provisioned in HSM. Therefore the sole scope of this ENUM (more or less copy with one from JSS) is
     * to enable HSM bundle configuration to load these from configuration and also do not export any JSS deps to any modules that are using this bundle.
     *
     *
     */
    SM_KEK_AUTHENTIC(ProtectedSessionKeyCapability.SM_KEK_AUTHENTIC),
    SM_KEK_NONAUTHENTIC(ProtectedSessionKeyCapability.SM_KEK_NONAUTHENTIC),
    SM_KEK_RENEWAL(ProtectedSessionKeyCapability.SM_KEK_RENEWAL),
    SM_KEK_AGREEMENT(ProtectedSessionKeyCapability.SM_KEK_AGREEMENT),
    DC_KEK_NONAUTHENTIC(ProtectedSessionKeyCapability.DC_KEK_NONAUTHENTIC),
    DC_KEK_RENEWAL(ProtectedSessionKeyCapability.DC_KEK_RENEWAL),
    SM_WK_CRYPTENC_AUTHENTIC(ProtectedSessionKeyCapability.SM_WK_CRYPTENC_AUTHENTIC),
    SM_WK_CRYPTENC_NONAUTHENTIC(ProtectedSessionKeyCapability.SM_WK_CRYPTENC_NONAUTHENTIC),
    SM_WK_CRYPTENC_RENEWAL(ProtectedSessionKeyCapability.SM_WK_CRYPTENC_RENEWAL),
    SM_WK_CRYPTENC_AGREEMENT(ProtectedSessionKeyCapability.SM_WK_CRYPTENC_AGREEMENT),
    DC_WK_CRYPTENC_NONAUTHENTIC(ProtectedSessionKeyCapability.DC_WK_CRYPTENC_NONAUTHENTIC),
    DC_WK_CRYPTENC_RENEWAL(ProtectedSessionKeyCapability.DC_WK_CRYPTENC_RENEWAL),
    SM_WK_CRYPTAUTH_AUTHENTIC(ProtectedSessionKeyCapability.SM_WK_CRYPTAUTH_AUTHENTIC),
    SM_WK_CRYPTAUTH_NONAUTHENTIC(ProtectedSessionKeyCapability.SM_WK_CRYPTAUTH_NONAUTHENTIC),
    SM_WK_CRYPTAUTH_RENEWAL(ProtectedSessionKeyCapability.SM_WK_CRYPTAUTH_RENEWAL),
    SM_WK_CRYPTAUTH_AGREEMENT(ProtectedSessionKeyCapability.SM_WK_CRYPTAUTH_AGREEMENT),
    DC_WK_CRYPTAUTH_NONAUTHENTIC(ProtectedSessionKeyCapability.DC_WK_CRYPTAUTH_NONAUTHENTIC),
    DC_WK_CRYPTAUTH_RENEWAL(ProtectedSessionKeyCapability.DC_WK_CRYPTAUTH_RENEWAL),
    SM_WK_HLSAUTH_AUTHENTIC(ProtectedSessionKeyCapability.SM_WK_HLSAUTH_AUTHENTIC),
    SM_WK_HLSAUTH_NONAUTHENTIC(ProtectedSessionKeyCapability.SM_WK_HLSAUTH_NONAUTHENTIC),
    SM_WK_HLSAUTH_RENEWAL(ProtectedSessionKeyCapability.SM_WK_HLSAUTH_RENEWAL),
    SM_WK_EXPORT_CRYPTENC_NONAUTHENTIC(ProtectedSessionKeyCapability.SM_WK_EXPORT_CRYPTENC_NONAUTHENTIC),
    SM_WK_EXPORT_CRYPTENC_RENEWAL(ProtectedSessionKeyCapability.SM_WK_EXPORT_CRYPTENC_RENEWAL),
    SM_KEK_MBUSDEV_AUTHENTIC(ProtectedSessionKeyCapability.SM_KEK_MBUSDEV_AUTHENTIC),
    SM_KEK_MBUSDEV_NONAUTHENTIC(ProtectedSessionKeyCapability.SM_KEK_MBUSDEV_NONAUTHENTIC),
    SM_WK_MBUSFWAUTH_AUTHENTIC(ProtectedSessionKeyCapability.SM_WK_MBUSFWAUTH_AUTHENTIC),
    SM_WK_MBUSFWAUTH_NONAUTHENTIC(ProtectedSessionKeyCapability.SM_WK_MBUSFWAUTH_NONAUTHENTIC),
    SM_WK_MBUSFWAUTH_RENEWAL(ProtectedSessionKeyCapability.SM_WK_MBUSFWAUTH_RENEWAL),
    SM_WK_DC_CRYPTENC_NONAUTHENTIC(ProtectedSessionKeyCapability.SM_WK_DC_CRYPTENC_NONAUTHENTIC),
    SM_WK_DC_CRYPTENC_RENEWAL(ProtectedSessionKeyCapability.SM_WK_DC_CRYPTENC_RENEWAL),
    SM_WK_EXPORT_CRYPTAUTH_NONAUTHENTIC(ProtectedSessionKeyCapability.SM_WK_EXPORT_CRYPTAUTH_NONAUTHENTIC),
    SM_WK_EXPORT_CRYPTAUTH_RENEWAL(ProtectedSessionKeyCapability.SM_WK_EXPORT_CRYPTAUTH_RENEWAL),
    SM_WK_CUSTOMER_NONAUTHENTIC(ProtectedSessionKeyCapability.SM_WK_CUSTOMER_NONAUTHENTIC),
    SM_WK_DC_CRYPTAUTH_NONAUTHENTIC(ProtectedSessionKeyCapability.SM_WK_DC_CRYPTAUTH_NONAUTHENTIC),
    SM_WK_DC_CRYPTAUTH_RENEWAL(ProtectedSessionKeyCapability.SM_WK_DC_CRYPTAUTH_RENEWAL),
    SM_EEK_CRYPT_ENC(ProtectedSessionKeyCapability.SM_EEK_CRYPT_ENC),
    SM_EEK_CRYPT_DEC(ProtectedSessionKeyCapability.SM_EEK_CRYPT_DEC);

    private final ProtectedSessionKeyCapability pskc;

    SessionKeyCapability(ProtectedSessionKeyCapability pskc) {
        this.pskc = pskc;
    }


    public ProtectedSessionKeyCapability  toProtectedSessionKeyCapability(){
        return this.pskc;
    }

}

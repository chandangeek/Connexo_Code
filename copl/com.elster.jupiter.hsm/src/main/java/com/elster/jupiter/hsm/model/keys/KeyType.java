package com.elster.jupiter.hsm.model.keys;

import com.atos.worldline.jss.api.custom.energy.ProtectedSessionKeyCapability;

public enum KeyType {

    /**
     * Here there are multiple types in HSM/JSS... to be added when needed
     * Check {@link ProtectedSessionKeyCapability}
     *
     * This is something that is strictly HSM/JSS there is no need for this enum yet since we try to not export
     * any HSM/JSS outside this maven module we discover the need of it.
     *
     * Ideally we can get rid of this if we can find the solution (mapping) between normal world algorithm + chaining mode + padding  and kek encryption from HSM/JSS
     */
    SM_KEK_AUTHENTIC(ProtectedSessionKeyCapability.SM_KEK_AUTHENTIC),
    SM_KEK_NONAUTHENTIC(ProtectedSessionKeyCapability.SM_KEK_NONAUTHENTIC),
    DC_WK_CRYPTENC_NONAUTHENTIC(ProtectedSessionKeyCapability.DC_WK_CRYPTENC_NONAUTHENTIC);

    private final ProtectedSessionKeyCapability pskc;

    KeyType(ProtectedSessionKeyCapability pskc) {
        this.pskc = pskc;
    }


    public ProtectedSessionKeyCapability  toProtectedSessionKeyCapability(){
        return this.pskc;
    }

}

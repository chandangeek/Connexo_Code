package com.energyict.dlms.cosem;

import com.energyict.mdc.protocol.api.NotInObjectListException;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.ProtocolLink;

/**
 *
 * @author  Koen
 */
public class LoadProfile {
    private ProtocolLink protocolLink;
    private ProfileGeneric profileGeneric;
    private CosemObjectFactory cof;

    /** Creates a new instance of StoredValues */
    public LoadProfile(CosemObjectFactory cof) {
        this.protocolLink=cof.getProtocolLink();
        this.cof=cof;
    }

    protected void retrieve() throws NotInObjectListException {
        profileGeneric = new ProfileGeneric(protocolLink,cof.getObjectReference(DLMSCOSEMGlobals.LOAD_PROFILE_LN,protocolLink.getMeterConfig().getProfileSN()));
    }

    /**
     * Getter for property profileGeneric.
     * @return Value of property profileGeneric.
     */
    public com.energyict.dlms.cosem.ProfileGeneric getProfileGeneric() {
        return profileGeneric;
    }

}

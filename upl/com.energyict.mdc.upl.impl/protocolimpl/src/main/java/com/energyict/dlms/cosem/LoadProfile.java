/*
 * LoadProfile.java
 *
 * Created on 20 augustus 2004, 16:05
 */

package com.energyict.dlms.cosem;
import java.io.*;
import java.util.*;

import com.energyict.protocolimpl.dlms.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.ProtocolLink;
/**
 *
 * @author  Koen
 */
public class LoadProfile {
    ProtocolLink protocolLink;
    ProfileGeneric profileGeneric;
    CosemObjectFactory cof;

    /** Creates a new instance of StoredValues */
    public LoadProfile(CosemObjectFactory cof) {
        this.protocolLink=cof.getProtocolLink();
        this.cof=cof;
    }
    
    protected void retrieve() throws IOException {
        profileGeneric = new ProfileGeneric(protocolLink,cof.getObjectReference(cof.LOAD_PROFILE_LN,protocolLink.getMeterConfig().getProfileSN()));
    }
    
    /**
     * Getter for property profileGeneric.
     * @return Value of property profileGeneric.
     */
    public com.energyict.dlms.cosem.ProfileGeneric getProfileGeneric() {
        return profileGeneric;
    }    
        
}

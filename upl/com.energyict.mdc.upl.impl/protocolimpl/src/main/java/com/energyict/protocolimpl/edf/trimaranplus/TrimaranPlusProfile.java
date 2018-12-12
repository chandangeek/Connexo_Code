/*
 * TrimaranPlusProfile.java
 *
 * Created on 5 maart 2007, 16:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaranplus;

import com.energyict.protocol.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author Koen
 */
public class TrimaranPlusProfile {
    
    TrimaranPlus trimaranPlus;
    
    /** Creates a new instance of TrimaranPlusProfile */
    public TrimaranPlusProfile(TrimaranPlus trimaranPlus) {
        this.trimaranPlus=trimaranPlus;
    }
    
    public ProfileData getProfileData(Date lastReading) throws IOException {
        return trimaranPlus.getTrimaranObjectFactory().getCourbeCharge(lastReading).getProfileData();
    }
}

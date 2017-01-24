/*
 * IsFeatureEnabled.java
 *
 * Created on 8 december 2006, 18:03
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms.remoteprocedures;

import com.energyict.protocolimpl.itron.quantum1000.minidlms.RemoteProcedureCallFactory;

/**
 *
 * @author Koen
 */
public class IsFeatureEnabled extends FeatureId {

    private int featureId;

    /** Creates a new instance of IsFeatureEnabled */
    public IsFeatureEnabled(RemoteProcedureCallFactory remoteProcedureCallFactory) {
        super(remoteProcedureCallFactory);
    }

    public byte[] getParameters() {
        byte[] data = new byte[4];
        data[3] = (byte)(getFeatureId()>>24);
        data[2] = (byte)(getFeatureId()>>16);
        data[1] = (byte)(getFeatureId()>>8);
        data[0] = (byte)(getFeatureId());
        return data;
    }

    public int getFunctionName() {
        return 0x0055; // 85 RPC_IS_FEATURE_ENABLED
    }

    public int getFeatureId() {
        return featureId;
    }

    public void setFeatureId(int featureId) {
        this.featureId = featureId;
    }


} // public class IsFeatureEnabled extends AbstractRemoteProcedure

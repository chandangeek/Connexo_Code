/*
 * FeatureId.java
 *
 * Created on 13 december 2006, 15:22
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
abstract public class AbstractViewRpc extends AbstractRemoteProcedure {

    abstract protected int getSubfunction();
    abstract protected byte[] getSubfunctionParameters();

    private final int VRPC_SELECT_DEFAULT_VIEW_ID=0;
    private final int VRPC_SELECT_VIEW_ID=1;
    private final int VRPC_START_EXCLUSIVE_SESSION=2;
    private final int VRPC_START_SHARED_SESSION=3;
    private final int VRPC_END_SESSION=4;
    private final int VRPC_SELECT_OBJECT_ID=5;
    private final int VRPC_SELECT_OBJECT_INDEX=6;
    private final int VRPC_SELECT_RECORD_PAST_TIME=7;
    private final int VRPC_SELECT_RECORD_NUMBER=8;
    private final int VRPC_MAXIMIZE_RECSPERREAD=9;
    private final int VRPC_SET_RESTRICTIONS=10;
    private final int VRPC_SET_SOURCE_ID=11;
    private final int VRPC_SELECT_RECORD_GREATERTHANOREQUALTO_TIME=12;
    private final int VRPC_SET_RECSPERREAD=13;

    /** Creates a new instance of FeatureId */
    public AbstractViewRpc(RemoteProcedureCallFactory remoteProcedureCallFactory) {
        super(remoteProcedureCallFactory);
    }

    public byte[] getParameters() {
        byte[] subfunctionParameters = getSubfunctionParameters();
        byte[] parameters = new byte[2+(subfunctionParameters==null?0:subfunctionParameters.length)];
        parameters[0] = (byte)(getSubfunction()>>8);
        parameters[1] = (byte)getSubfunction();
        if (subfunctionParameters!=null)
            System.arraycopy(subfunctionParameters, 0, parameters,2, subfunctionParameters.length);
        return parameters;
    }

    public int getFunctionName() {
        return 20; // 85 RPC_VIEW_RPC
    }

    public int getVRPC_SELECT_DEFAULT_VIEW_ID() {
        return VRPC_SELECT_DEFAULT_VIEW_ID;
    }

    public int getVRPC_SELECT_VIEW_ID() {
        return VRPC_SELECT_VIEW_ID;
    }

    public int getVRPC_START_EXCLUSIVE_SESSION() {
        return VRPC_START_EXCLUSIVE_SESSION;
    }

    public int getVRPC_START_SHARED_SESSION() {
        return VRPC_START_SHARED_SESSION;
    }

    public int getVRPC_END_SESSION() {
        return VRPC_END_SESSION;
    }

    public int getVRPC_SELECT_OBJECT_ID() {
        return VRPC_SELECT_OBJECT_ID;
    }

    public int getVRPC_SELECT_OBJECT_INDEX() {
        return VRPC_SELECT_OBJECT_INDEX;
    }

    public int getVRPC_SELECT_RECORD_PAST_TIME() {
        return VRPC_SELECT_RECORD_PAST_TIME;
    }

    public int getVRPC_SELECT_RECORD_NUMBER() {
        return VRPC_SELECT_RECORD_NUMBER;
    }

    public int getVRPC_MAXIMIZE_RECSPERREAD() {
        return VRPC_MAXIMIZE_RECSPERREAD;
    }

    public int getVRPC_SET_RESTRICTIONS() {
        return VRPC_SET_RESTRICTIONS;
    }

    public int getVRPC_SET_SOURCE_ID() {
        return VRPC_SET_SOURCE_ID;
    }

    public int getVRPC_SELECT_RECORD_GREATERTHANOREQUALTO_TIME() {
        return VRPC_SELECT_RECORD_GREATERTHANOREQUALTO_TIME;
    }

    public int getVRPC_SET_RECSPERREAD() {
        return VRPC_SET_RECSPERREAD;
    }

} // abstract public class AbstractViewRpc extends AbstractRemoteProcedure

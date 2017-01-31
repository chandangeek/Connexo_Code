/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ObisCodeRelation.java
 *
 * Created on 13 oktober 2004, 11:06
 */

package com.energyict.protocolimpl.dlms.flex;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author  Koen
 */
public class ObisCodeRelation {


    List obisCodes=null; // of type ObisCode
    List profileObisCodes=null; // of type ObisCode

    /** Creates a new instance of ObisCodeRelation */
    public ObisCodeRelation() {
       obisCodes = new ArrayList();
       profileObisCodes = new ArrayList();
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ObisCodeRelation:\n");
        for (int i=0;i<obisCodes.size();i++) {
            ObisCode obisCode = (ObisCode)obisCodes.get(i);
            ObisCode profileObisCode = (ObisCode)profileObisCodes.get(i);
            strBuff.append("obisCode="+obisCode+", profileObisCode="+profileObisCode+"\n");
        }
        return strBuff.toString();
    }

    public void addObisCodePair(ObisCode obisCode, ObisCode profileObisCode) {
        obisCodes.add(obisCode);
        profileObisCodes.add(profileObisCode);
    }

    public ObisCode getProfileObisCode(ObisCode obisCode) throws IOException {


        for (int i=0;i<obisCodes.size();i++) {
            ObisCode oc = (ObisCode)obisCodes.get(i);
            if (oc.equals(obisCode))
                return (ObisCode)profileObisCodes.get(i);
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }

    public ObisCode getObisCode(ObisCode profileObisCode) throws IOException {
        for (int i=0;i<profileObisCodes.size();i++) {
            ObisCode poc = (ObisCode)profileObisCodes.get(i);
            if (poc.equals(profileObisCode))
                return (ObisCode)obisCodes.get(i);
        }
        throw new NoSuchRegisterException("ObisCode "+profileObisCode.toString()+" is not supported!");
    }

} // public class ObisCodeRelation

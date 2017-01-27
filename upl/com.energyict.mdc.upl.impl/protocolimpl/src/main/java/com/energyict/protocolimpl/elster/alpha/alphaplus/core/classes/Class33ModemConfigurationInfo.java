/*
 * Class33ModemConfigurationInfo.java
 *
 * Created on 11 juli 2005, 15:36
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class Class33ModemConfigurationInfo extends AbstractClass {
    
    ClassIdentification classIdentification = new ClassIdentification(33,64,false);
    String COMID;
    String INITSTR;
    int TWIND3;
    int AUTOANS;
    int INITDEL;
    int TRYDEL;
    int RSPEED;
    int DEVNUM;
    //RESERVED [12]

    public String toString() {
        return "Class33ModemConfigurationInfo: COMID="+COMID+", INITSTR="+INITSTR+", TWIND3="+TWIND3+", AUTOANS="+AUTOANS+", INITDEL="+INITDEL+", TRYDEL="+TRYDEL+", RSPEED=0x"+Integer.toHexString(RSPEED)+", DEVNUM="+DEVNUM;                
    }
    
    /** Creates a new instance of Class33ModemConfigurationInfo */
    public Class33ModemConfigurationInfo(ClassFactory classFactory) {
        super(classFactory);
    }
    
    protected void parse(byte[] data) throws IOException {
        COMID = new String(ProtocolUtils.getSubArray(data,0, 7));
        INITSTR = new String(ProtocolUtils.getSubArray(data,8, 43));
        TWIND3 = ProtocolUtils.getBCD2Int(data, 44, 2);
        AUTOANS = ProtocolUtils.getInt(data,46, 1);
        INITDEL = ProtocolUtils.getInt(data,47, 1);
        TRYDEL = ProtocolUtils.getInt(data,48, 1);
        RSPEED = ProtocolUtils.getInt(data,49, 1);
        DEVNUM = ProtocolUtils.getInt(data,50, 1);
    }
    
    protected ClassIdentification getClassIdentification() {
        return classIdentification; 
    }

    public String getCOMID() {
        return COMID;
    }

    public String getINITSTR() {
        return INITSTR;
    }

    public int getTWIND3() {
        return TWIND3;
    }

    public int getAUTOANS() {
        return AUTOANS;
    }

    public int getINITDEL() {
        return INITDEL;
    }

    public int getTRYDEL() {
        return TRYDEL;
    }

    public int getRSPEED() {
        return RSPEED;
    }

    public int getDEVNUM() {
        return DEVNUM;
    }
    
    
}

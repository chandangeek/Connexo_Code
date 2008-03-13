/*
 * Class6MeteringFunctionBlock.java
 *
 * Created on 12 juli 2005, 16:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import java.io.*;
import java.util.*;
import java.math.*;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.elster.alpha.core.connection.*;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.cbo.*;


/**
 *
 * @author Koen
 */
public class Class6MeteringFunctionBlock extends AbstractClass {
    
    ClassIdentification classIdentification = new ClassIdentification(6,272,true);
    
    
    private int XREV; // 1 byte class06-07 eeprom memory map revision code, 1 byte; 2 bcd digits 0 to 99.
    private int XPGMREV; // 1 byte SSI-377 program revision number; 1 byte, 2 bcd digits 0 to 99.
    private int XUOM; // 2 bytes meter capabilities 
    // kvadel (xuom:15); 1 = meter provides VAhr delivered pulses, 0 = meter does not provide VAhr delivered pulses.
    // kvarec (xuom:14); 1 = meter provides VAhr received pulses, 0 = meter does not provide VAhr received pulses.
    // kqhdel (xuom:13); 1 = meter provides Qhr delivered pulses, 0 = meter does not provide Qhr delivered pulses.
    // kqhrec (xuom:12); 1 = meter provides Qhr received pulses, 0 = meter does not provide Qhr received pulses.
    // kvardel (xuom:11); 1 = meter provides reactive delivered pulses, 0 = meter does not provide reactive delivered pulses.
    // kvarrec (xuom:10); 1 = meter provides reactive received pulses, 0 = meter does not provide reactive received pulses. 
    // kwhdel (xuom:9); 1 = meter provides real delivered pulses, 0 = meter does not provide real delivered pulses.
    // kwhrec (xuom:8); 1 = meter provides real received pulses, 0 = meter does not provide real received pulses.
    // me (xuom:7); 1 = meter has three voltage elements, 0 = meter has two voltage elements.
    // spares (xuom:6-2); undefined. 
    // memsiz memory available for load profile (xuom:0-1); 00 = no load profile
        // 01 = 1 x 32k less overhead = 28032 approx
        // 02 = 2 x 32k less overhead = 59712 approx
        // 03 = 3 x 32k less overhead = 91392 approx    
    
    // RESERVED 3 bytes
    private int DSPFUNC; // 1 byte
    // RESERVED 5 bytes
    private int XPGMREV2; // 2 bytes
    // RESERVED 256
    
    public String toString() {
        return "Class6MeteringFunctionBlock: XREV="+getXREV()+", XPGMREV="+getXPGMREV()+", XUOM="+Integer.toHexString(getXUOM())+", DSPFUNC="+getDSPFUNC()+", +XPGMREV2="+getXPGMREV2();
    }
    
    /** Creates a new instance of Class6MeteringFunctionBlock */
    public Class6MeteringFunctionBlock(ClassFactory classFactory) {
        super(classFactory);
    }
    
    protected void parse(byte[] data) throws IOException {
        setXREV(ProtocolUtils.getBCD2Int(data, 0, 1));
        setXPGMREV(ProtocolUtils.getBCD2Int(data, 1, 1));
        setXUOM(ProtocolUtils.getInt(data,2,2));
        // reserved 3 bytes
        setDSPFUNC(ProtocolUtils.getInt(data,7,1));
        // reserved 5 bytes
        setXPGMREV2(ProtocolUtils.getBCD2Int(data, 13, 1));
    }
    
    protected ClassIdentification getClassIdentification() {
        return classIdentification; 
    }

    public int getXREV() {
        return XREV;
    }

    public void setXREV(int XREV) {
        this.XREV = XREV;
    }

    public int getXPGMREV() {
        return XPGMREV;
    }

    public void setXPGMREV(int XPGMREV) {
        this.XPGMREV = XPGMREV;
    }

    public int getXUOM() {
        return XUOM;
    }

    public void setXUOM(int XUOM) {
        this.XUOM = XUOM;
    }

    public int getDSPFUNC() {
        return DSPFUNC;
    }

    public void setDSPFUNC(int DSPFUNC) {
        this.DSPFUNC = DSPFUNC;
    }

    public int getXPGMREV2() {
        return XPGMREV2;
    }

    public void setXPGMREV2(int XPGMREV2) {
        this.XPGMREV2 = XPGMREV2;
    }
    
    
    
}

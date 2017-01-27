/*
 * Class6MeteringFunctionBlock.java
 *
 * Created on 12 juli 2005, 16:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;


/**
 *
 * @author Koen
 */
public class Class7MeteringFunctionBlock extends AbstractClass {
    
    ClassIdentification classIdentification = new ClassIdentification(7,304,true);
    

    long XMTRSN;
    BigDecimal XKH;
    int XPR1;
    BigDecimal XKE1;
    int XKHDIV;
    // XKE2 [5]
    // RESERVED [283]
    
    public String toString() {
        return "Class7MeteringFunctionBlock: XMTRSN="+XMTRSN+", XKH="+XKH+", XPR1="+XPR1+", XKE1="+XKE1+", XKHDIV="+XKHDIV;
    }
    
    /** Creates a new instance of Class6MeteringFunctionBlock */
    public Class7MeteringFunctionBlock(ClassFactory classFactory) {
        super(classFactory);
    }
    
    protected void parse(byte[] data) throws IOException {
        XMTRSN = ParseUtils.getBCD2Long(data,0, 5); //new String(ProtocolUtils.getSubArray2(data, 0, 5));
        if (getClassIdentification().getLength() > 5) {
            XKH = BigDecimal.valueOf(ParseUtils.getBCD2Long(data, 5, 3),3);
            XPR1 = ProtocolUtils.getBCD2Int(data,8, 1);
            XKE1 = BigDecimal.valueOf(ParseUtils.getBCD2Long(data, 9, 5),6);
            XKHDIV = ProtocolUtils.getInt(data,14, 1);
        }
    }
    
    protected ClassIdentification getClassIdentification() {
        return classIdentification; 
    }

    public void discoverSerialNumber() {
        getClassIdentification().setLength(5);
        getClassIdentification().setVerify(false);
    }
    
    public long getXMTRSN() {
        return XMTRSN;
    }

    public BigDecimal getXKH() {
        return XKH;
    }

    public int getXPR1() {
        return XPR1;
    }

    public BigDecimal getXKE1() {
        return XKE1;
    }

    public int getXKHDIV() {
        return XKHDIV;
    }
    
}

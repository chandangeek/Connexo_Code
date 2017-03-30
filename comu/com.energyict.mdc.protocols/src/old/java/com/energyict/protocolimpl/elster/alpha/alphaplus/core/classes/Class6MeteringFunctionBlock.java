/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;


/**
 *
 * @author Koen
 */
public class Class6MeteringFunctionBlock extends AbstractClass {

    ClassIdentification classIdentification = new ClassIdentification(6,288,true);

    int XREV;
    //RESERVED [1]

/*  Mail from Al Angel of Elster US 12/07/2005
Not sure why the document does not discuss XUOM2; but the results is that
XUOM2 duplicates XUOM1 and is the next byte.

XUOM1 is for the so called primary metering quantities and XUOM2 for for
the alternate metering quantities.

The reality of the situation is that XUOM has 15 possible values, and only
four are used.  4-14 is undefined and of course =15 points to XUOMx.

Thus to date there is no requirement for the flexibility that =15 provides.
*/
    int XUOM;
    // RESERVED [3]
    int XUOM1;
    int XUOM2;
    // RESERVED [278]

    public String toString() {
        return "Class6MeteringFunctionBlock: XREV="+XREV+", XUOM=0x"+Integer.toHexString(XUOM)+", XUOM1=0x"+Integer.toHexString(XUOM1)+", XUOM2=0x"+Integer.toHexString(XUOM2);
    }

    /** Creates a new instance of Class6MeteringFunctionBlock */
    public Class6MeteringFunctionBlock(ClassFactory classFactory) {
        super(classFactory);
    }

    protected void parse(byte[] data) throws IOException {
       XREV = ProtocolUtils.getBCD2Int(data, 0,1);
       XUOM = ProtocolUtils.getInt(data, 2,2);
       // reserved [3]
       XUOM1 = ProtocolUtils.getInt(data, 7,1);
       XUOM2 = ProtocolUtils.getInt(data, 8,1);
    }

    protected ClassIdentification getClassIdentification() {
        return classIdentification;
    }



}

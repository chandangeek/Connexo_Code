/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Class10Status2.java
 *
 * Created on 20 juli 2005, 13:39
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;


/**
 *
 * @author koen
 */
public class Class10Status2 extends AbstractClass {

    ClassIdentification classIdentification = new ClassIdentification(10,24,false);

    int KH;
    int PR;
    int PULDEF;
    long MTRSN;
    long KEADJ;
    long KDADJ;
    int ENEWCON;
    int ENEWACT;

    public String toString() {
        return "Class10Status2: KH="+KH+", "+
               "PR="+PR+", "+
               "PULDEF=0x"+Integer.toHexString(PULDEF)+", "+
               "MTRSN="+MTRSN+", "+
               "KEADJ="+KEADJ+", "+
               "KDADJ="+KDADJ+", "+
               "ENEWCON="+ENEWCON+", "+
               "ENEWACT=0x"+Integer.toHexString(ENEWACT);
    }

    /** Creates a new instance of Class10Status2 */
    public Class10Status2(ClassFactory classFactory) {
        super(classFactory);
    }

    protected void parse(byte[] data) throws IOException {
         KH = ProtocolUtils.getBCD2Int(data,0, 3);
         PR = ProtocolUtils.getBCD2Int(data,3, 1);
         PULDEF = ProtocolUtils.getInt(data,4, 1);
         MTRSN = ParseUtils.getBCD2Long(data,5, 5);
         KEADJ = ParseUtils.getBCD2Long(data,10, 5);
         KDADJ = ParseUtils.getBCD2Long(data,15, 5);
         ENEWCON = ProtocolUtils.getBCD2Int(data,20, 3);
         ENEWACT = ProtocolUtils.getInt(data,23,1);
    }

    protected ClassIdentification getClassIdentification() {
        return classIdentification;
    }

}

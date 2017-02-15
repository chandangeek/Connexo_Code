/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * XID.java
 *
 * Created on 19 juni 2006, 16:53
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaran.core;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class EOS extends AbstractSPDU {

    /** Creates a new instance of XID */
    public EOS(SPDUFactory sPDUFactory) {
        super(sPDUFactory);
    }

    protected byte[] prepareBuild() throws IOException {
        byte[] data = new byte[1];
        data[0] = SPDU_EOS;
        return data;
    }

    protected void parse(byte[] data) throws IOException {

    }

} // public class XID extends AabstractSPDU

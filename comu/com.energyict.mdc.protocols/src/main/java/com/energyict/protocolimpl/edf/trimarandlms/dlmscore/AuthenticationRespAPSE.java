/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AuthenticationRespAPSE.java
 *
 * Created on 15 februari 2007, 15:43
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.dlmscore;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class AuthenticationRespAPSE extends AbstractAPSEPDU {


    /** Creates a new instance of AuthenticationRespAPSE */
    public AuthenticationRespAPSE(APSEPDUFactory aPSEFactory) {
        super(aPSEFactory);
    }

    final int AUTHENTICATION_RESP_APSE = 5;

    byte[] preparebuild() throws IOException {
        return null;
    }

    void parse(byte[] data) throws IOException {
        int offset=0;
        int tag = ProtocolUtils.getInt(data,offset++,1);
        if (tag != AUTHENTICATION_RESP_APSE){
            throw new IOException("AuthenticationRespAPSE, parse, invalid tag 0x"+Integer.toHexString(tag)+" received");
        }

        offset+=2; // skip the fuzzy ASN.1 - like notation of the length of the bit string
        if (!getAPSEFactory().getAPSEParameters().checkCipheredClientRandom(ProtocolUtils.getSubArray2(data,offset, 8))) {
			throw new IOException("AuthenticationRespAPSE, parse, ERROR client authentication failed. Probably wrong password!");
		}
        offset+=8; // skip the fuzzy ASN.1 - like notation of the length of the bit string
        offset+=2; // skip the fuzzy ASN.1 - like notation of the length of the bit string
        getAPSEFactory().getAPSEParameters().setServerRandom(ProtocolUtils.getSubArray2(data,offset, 8));

    }


}

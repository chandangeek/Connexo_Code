/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AuthenticationReqAPSE.java
 *
 * Created on 15 februari 2007, 11:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.dlmscore;

import com.energyict.protocolimpl.base.RandomGenerator;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class AuthenticationReqAPSE extends AbstractAPSEPDU {

    private AuthenticationRespAPSE authenticationRespAPSE=null;

    /** Creates a new instance of AuthenticationReqAPSE */
    public AuthenticationReqAPSE(APSEPDUFactory aPSEFactory) {
        super(aPSEFactory);
    }

    final int AUTHENTICATION_REQ_APSE = 4;

    byte[] preparebuild() throws IOException {
        byte[] data = new byte[13];
        data[0] = (byte)AUTHENTICATION_REQ_APSE;
        int clientType = getAPSEFactory().getAPSEParameters().getClientType();
        data[1] = (byte)(clientType >> 8);
        data[2] = (byte)(clientType);
        byte[] randomNr = RandomGenerator.getRandomSequence();

        getAPSEFactory().getAPSEParameters().setClientRandom(randomNr);
        // A-XDR or BER encoding? I don't think so cause this is wrong!!
        // this suppose to be the representation of a BIT STRING SIZE 64. So, this is a size of 0x40
        // Following A-XDR & BER, this SIZE specific BIT STRING should not contain a size field
        // Anyhow, if the following bytes should represent the size, then again only one byte for the size
        // should exist, 0x40
        // Here, we have 0x8040. What kind of notation is that?
        // So, we believe that 0x40 is the size and 0x80 is also added for some reason...
        data[3] = (byte)0x80;
        data[4] = (byte)0x40;
        System.arraycopy(randomNr, 0, data, 5, randomNr.length);
        return data;
    }

    void parse(byte[] data) throws IOException {

        authenticationRespAPSE = new AuthenticationRespAPSE(getAPSEFactory());
        authenticationRespAPSE.parse(data);
    }

    public AuthenticationRespAPSE getAuthenticationRespAPSE() {
        return authenticationRespAPSE;
    }

    private void setAuthenticationRespAPSE(AuthenticationRespAPSE authenticationRespAPSE) {
        this.authenticationRespAPSE = authenticationRespAPSE;
    }
}

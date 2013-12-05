/*
 * APSEPDUFactory.java
 *
 * Created on 13 februari 2007, 17:10
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.dlmscore;

import com.energyict.protocolimpl.edf.trimarandlms.protocol.APSEParameters;
import com.energyict.protocolimpl.edf.trimarandlms.protocol.ProtocolLink;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class APSEPDUFactory {

    private ProtocolLink protocolLink;
    private APSEParameters aPSEParameters;
    /**
     * Creates a new instance of APSEPDUFactory
     */
    public APSEPDUFactory(ProtocolLink protocolLink,APSEParameters aPSEParameters) {
        this.setProtocolLink(protocolLink);
        this.setAPSEParameters(aPSEParameters);
    }

    public AuthenticationReqAPSE getAuthenticationReqAPSE() throws IOException {
        AuthenticationReqAPSE authenticationReqAPSE = new AuthenticationReqAPSE(this);
        authenticationReqAPSE.invoke();
        return authenticationReqAPSE;
    }

    public ProtocolLink getProtocolLink() {
        return protocolLink;
    }

    public void setProtocolLink(ProtocolLink protocolLink) {
        this.protocolLink = protocolLink;
    }

    public APSEParameters getAPSEParameters() {
        return aPSEParameters;
    }

    public void setAPSEParameters(APSEParameters aPSEParameters) {
        this.aPSEParameters = aPSEParameters;
    }



}

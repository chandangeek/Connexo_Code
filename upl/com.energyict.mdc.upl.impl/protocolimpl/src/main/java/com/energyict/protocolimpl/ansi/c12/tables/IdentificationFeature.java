/*
 * IdentificationFeature.java
 *
 * Created on 19 oktober 2005, 15:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;
import com.energyict.protocolimpl.utils.ProtocolUtils;
/**
 *
 * @author Koen
 */
public class IdentificationFeature {
    
    private int authenticationType;
    private int authentificationAlgorithm;
    private byte[] ticket;
    
    /** Creates a new instance of IdentificationFeature */
    public IdentificationFeature() {
    }

    public String toString() {
        return "IdentificationFeature: authenticationType="+getAuthenticationType()+", authentificationAlgorithm="+getAuthentificationAlgorithm()+", ticket="+ProtocolUtils.outputHexString(getTicket());
    }
    
    public int getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(int authenticationType) {
        this.authenticationType = authenticationType;
    }

    public int getAuthentificationAlgorithm() {
        return authentificationAlgorithm;
    }

    public void setAuthentificationAlgorithm(int authentificationAlgorithm) {
        this.authentificationAlgorithm = authentificationAlgorithm;
    }

    public byte[] getTicket() {
        return ticket;
    }

    public void setTicket(byte[] ticket) {
        this.ticket = ticket;
    }
    
}

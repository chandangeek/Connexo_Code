/*
 * SecurityResponse.java
 *
 * Created on 19 oktober 2005, 14:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;
import java.io.*;
import com.energyict.protocol.*;
/**
 *
 * @author Koen
 */
public class AuthenticateResponse extends AbstractResponse {

    private boolean authenticated=false;
    
    private int securityLevel;
    private byte[] doubleEncryptedTicket;
    
    /** Creates a new instance of SecurityResponse */
    public AuthenticateResponse(PSEMServiceFactory psemServiceFactory) {
        super(psemServiceFactory);
    }
    
    protected void parse(ResponseData responseData) throws IOException {
        // in case of <ok>
        setAuthenticated(true);
        byte[] data = responseData.getData();
        int length = C12ParseUtils.getInt(data,1);
        setSecurityLevel(C12ParseUtils.getInt(data,2));
        setDoubleEncryptedTicket(new byte[length-1]);
        System.arraycopy(data,3,getDoubleEncryptedTicket(), 0,(length-1));
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public int getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(int securityLevel) {
        this.securityLevel = securityLevel;
    }

    public byte[] getDoubleEncryptedTicket() {
        return doubleEncryptedTicket;
    }

    public void setDoubleEncryptedTicket(byte[] doubleEncryptedTicket) {
        this.doubleEncryptedTicket = doubleEncryptedTicket;
    }
}

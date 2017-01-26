/*
 * PasswordValidator.java
 *
 * Created on 6 april 2004, 15:27
 */

package com.energyict.protocolimpl.pact.core.common;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class PasswordValidator {
    
    long seed;
    
    /** Creates a new instance of PasswordValidator */
    public PasswordValidator(byte[] data) throws IOException {
        parse(data);
    }
    
    private void parse(byte[] data) throws IOException {
        setSeed(ProtocolUtils.getIntLE(data,1,4));   
    }
    
    public byte[] getPasswordClearanceRequest(int level, long password) {
        byte[] clearanceRequest = new byte[4];
        clearanceRequest[0] = (byte)0xC4;
        clearanceRequest[1] = (byte)level;
        int temp = getPasswordValidator(password);
        clearanceRequest[2] = (byte)temp;
        clearanceRequest[3] = (byte)(temp>>8);
        return clearanceRequest;
    }
    
    private int getPasswordValidator(long password) {
          long z,acc;
          
          acc=password;
          for (int i=0;i<7;i++) {
              acc+=seed;
              z = (acc >> 5) ^ (acc >> 23);
              acc = (acc << 8) | (z & 0xFF);
          }
          return (int)(acc&0xFFFF); 
    }    
    
    static public void main(String[] args) {
        try {
             byte[] data={(byte)0xC5,(byte)0xA6,(byte)0x39,(byte)0x4B,(byte)0xBE};
             PasswordValidator pv = new PasswordValidator(data);
             pv.getPasswordClearanceRequest(2, 0x2000EFAB);
             ProtocolUtils.printResponseData(pv.getPasswordClearanceRequest(2, 0x2000EFAB));
        }
        catch(IOException e) {
             e.printStackTrace();   
        }
    }
    
    /** Getter for property seed.
     * @return Value of property seed.
     *
     */
    public long getSeed() {
        return seed;
    }
    
    /** Setter for property seed.
     * @param seed New value of property seed.
     *
     */
    public void setSeed(long seed) {
        this.seed = seed;
    }
    
    
    
}

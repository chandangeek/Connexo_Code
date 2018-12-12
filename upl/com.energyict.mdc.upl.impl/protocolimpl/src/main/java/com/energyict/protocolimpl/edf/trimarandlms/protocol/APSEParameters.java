/*
 * APSEParameters.java
 *
 * Created on 16 februari 2007, 12:10
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.protocol;

    
/**
 *
 * @author Koen
 */
public class APSEParameters {
    
    
    
    private int clientType;
    private String callingPhysicalAddress;
    private int proposedAppCtxName;
    
    
    private byte[] key;
    private byte[] serverRandom;
    private byte[] clientRandom;
    
    private int encryptionMask;
    
    /** Creates a new instance of APSEParameters */
    public APSEParameters() {
    }

    
    
    public int getClientType() {
        return clientType;
    }

    public void setClientType(int clientType) {
        this.clientType = clientType;
    }

    public String getCallingPhysicalAddress() {
        return callingPhysicalAddress;
    }

    public void setCallingPhysicalAddress(String callingPhysicalAddress) {
        this.callingPhysicalAddress = callingPhysicalAddress;
    }

    public int getProposedAppCtxName() {
        return proposedAppCtxName;
    }

    public void setProposedAppCtxName(int proposedAppCtxName) {
        this.proposedAppCtxName = proposedAppCtxName;
    }

    public boolean checkCipheredClientRandom(byte[] cipheredClientRandom2Check) {        
        byte[] cipheredClientRandom = SHA1Encryptor.getCipheredRandomNumber(getClientRandom(), getKey());        
        for (int i=0;i<cipheredClientRandom.length;i++) {
            if (cipheredClientRandom2Check[i] != cipheredClientRandom[i]){
                return false;
            }
        }
        return true;
    }


    public byte[] getServerRandom() {
        return serverRandom;
    }

    public void setServerRandom(byte[] serverRandom) {
        this.serverRandom = serverRandom;
    }

    public byte[] getCipheredServerRandom() {
         setEncryptionMask(SHA1Encryptor.getMasking16Bit(serverRandom, getKey()));
         
         //System.out.println("KV_DEBUG> serverrandom "+ProtocolUtils.outputHexString(serverRandom));

         byte[] cipheredNumber =  SHA1Encryptor.getCipheredRandomNumber(serverRandom, getKey());     
         //System.out.println("KV_DEBUG> ciphered serverrandom "+ProtocolUtils.outputHexString(cipheredNumber));
         //System.out.println("KV_DEBUG> key "+ProtocolUtils.outputHexString(getKey()));
         
         return cipheredNumber;
    }


    public byte[] getClientRandom() {
        return clientRandom;
    }

    public void setClientRandom(byte[] clientRandom) {
        this.clientRandom = clientRandom;
    }



    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public int getEncryptionMask() {
        return encryptionMask;
    }

    public void setEncryptionMask(int encryptionMask) {
        this.encryptionMask = encryptionMask;
    }
    
}

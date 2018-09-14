package com.elster.jupiter.hsm.integration.helpers.keys;


public class AsymetricKey {

    private final byte[] hexPublicKey;
    private final byte[] hexPrivateKey;

    public AsymetricKey(String hexPublicKey, String hexPrivateKey,Encoder decoder){
        this.hexPublicKey = decoder.decode(hexPublicKey);
        this.hexPrivateKey = decoder.decode(hexPrivateKey);
    }

    public byte[] getPublicKey(){
        return hexPublicKey;
    }

    public byte[] getPrivateKey(){
        return hexPrivateKey;
    }

}

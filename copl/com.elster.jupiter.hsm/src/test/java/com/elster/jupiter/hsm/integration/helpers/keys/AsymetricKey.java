package com.elster.jupiter.hsm.integration.helpers.keys;


public class AsymetricKey {

    private final byte[] publicKey;
    private final byte[] privateKey;

    public AsymetricKey(String publicKey, String hexPrivateKey, Encoder decoder){
        this.publicKey = decoder.decode(publicKey);
        this.privateKey = decoder.decode(hexPrivateKey);
    }

    public byte[] getPublicKey(){
        return publicKey;
    }

    public byte[] getPrivateKey(){
        return privateKey;
    }

}

package com.elster.jupiter.hsm.integration.helpers.keys;


import java.util.Arrays;

public class AsymmetricKey {

    private final byte[] publicKey;
    private final byte[] privateKey;

    public AsymmetricKey(String publicKey, String privateKey, Encoder decoder){
        this.publicKey = decoder.decode(publicKey);
        this.privateKey = decoder.decode(privateKey);
    }

    public byte[] getPublicKey(){
        return publicKey;
    }

    public byte[] getPrivateKey(){
        return privateKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AsymmetricKey)) {
            return false;
        }

        AsymmetricKey that = (AsymmetricKey) o;

        if (!Arrays.equals(publicKey, that.publicKey)) {
            return false;
        }
        return Arrays.equals(privateKey, that.privateKey);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(publicKey);
        result = 31 * result + Arrays.hashCode(privateKey);
        return result;
    }
}

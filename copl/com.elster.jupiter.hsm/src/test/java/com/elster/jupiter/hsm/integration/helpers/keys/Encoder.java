package com.elster.jupiter.hsm.integration.helpers.keys;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

public enum Encoder {

    HEX(){
        @Override
        public byte[] decode(String msg){
            return Hex.decode(msg);
        }

        @Override
        public String encode(byte[] bytes) {
            return Hex.toHexString(bytes);
        }
    },
    BASE64(){
        @Override
        public byte[] decode(String msg){
            return Base64.decode(msg);
        }

        @Override
        public String encode(byte[] bytes) {
            return Base64.toBase64String(bytes);
        }
    };




    public abstract byte[] decode(String msg);
    public abstract String encode(byte[] bytes);

}

package com.elster.jupiter.hsm.integration.helpers.keys;

import com.elster.jupiter.hsm.model.Message;

import java.util.Base64;

public class TransportedDeviceKey {

    private final AsymmetricKey hsmKey;
    private final Message wrappingKey;
    private final Message deviceKey;
    private final Message iv;

    private TransportedDeviceKey(AsymmetricKey hsmKey, Message wrappingKey, Message deviceKey, Message iv) {
        this.hsmKey = hsmKey;
        this.wrappingKey = wrappingKey;
        this.deviceKey = deviceKey;
        this.iv = iv;
    }

    public AsymmetricKey getHsmKey() {
        return hsmKey;
    }

    public Message getWrappingKey() {
        return wrappingKey;
    }

    public Message getDeviceKey() {
        return deviceKey;
    }

    public Message getIv() {
        return iv;
    }

    public static TransportedDeviceKey fromEncrypted(AsymmetricKey hsmKey, String encryptedWrappingKeyB64, String encryptedDeviceKeyB64) {
        Message deviceKey = new Message(Base64.getDecoder().decode(encryptedDeviceKeyB64));
        Message wrappingKey = new Message(Base64.getDecoder().decode(encryptedWrappingKeyB64));
        return new TransportedDeviceKey(hsmKey, wrappingKey, getKey(deviceKey.getBytes(),16), getInitVector(deviceKey.getBytes(),16));
    }

    public static TransportedDeviceKey fromPlain(AsymmetricKey hsmKey, Message wrappingKey, Message deviceKey, Message initVector) {
        return new TransportedDeviceKey(hsmKey, wrappingKey, deviceKey, initVector);
    }

    private static Message getInitVector(byte[] bits, int iv) {
        byte[] initializationVector = new byte[iv];
        System.arraycopy(bits, 0, initializationVector, 0, iv);
        return new Message(initializationVector);
    }

    public static Message getKey(byte[] bits, int iv) {
        byte[] cipher = new byte[bits.length - iv];
        System.arraycopy(bits, iv, cipher, 0, bits.length - iv);
        return new Message(cipher);

    }

}

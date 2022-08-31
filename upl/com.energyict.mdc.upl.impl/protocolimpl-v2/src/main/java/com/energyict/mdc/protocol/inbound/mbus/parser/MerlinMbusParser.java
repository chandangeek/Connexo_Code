package com.energyict.mdc.protocol.inbound.mbus.parser;

import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.MerlinLogger;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.protocolimpl.mbus.core.CIField72h;
import com.energyict.protocolimpl.mbus.core.CIField7Ah;
import com.energyict.protocolimpl.mbus.core.DataRecord;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.logging.Logger;

public class MerlinMbusParser {
    private static final int BUFFER_SIZE = 1024;
    public static final String AES_CBC_NO_PADDING = "AES/CBC/NoPadding";
    public static final String AES_CBC_PKCS_5_PADDING = "AES/CBC/PKCS5Padding";
    public static final String AES_CBC_ISO_10126_PADDING = "AES/CBC/ISO10126Padding";
    public static final int AES_BLOCK_SIZE = 16;

    //private DataLinkLayer dataLinkLayer;
    //private TransportLayer transportLayer;
    //private AesVerification aesVerification;
    private Telegram telegram;

    private final InboundContext inboundContext;

    public MerlinMbusParser(InboundContext inboundContext) {
        this.inboundContext = inboundContext;
    }

    public InboundContext getInboundContext() {
        return inboundContext;
    }

    public MerlinLogger getLogger(){
        return getInboundContext().getLogger();
    }

    public Telegram getTelegram(){
        return this.telegram;
    }

    public void parse(byte[] buffer){
        telegram = new Telegram();
        String telegramString = ProtocolTools.getHexStringFromBytes(buffer, " ").trim();
        telegram.createTelegram(telegramString, false);
        telegram.decryptTelegram("4F A7 0B 24 46 5F 81 4A 66 76 31 77 3A 39 76 44");
        telegram.parse();
        telegram.debugOutput();
    }
/*
    public void parse(byte[] buffer) {
        int index = 0;
        int ciFieldIndex = 0;

        dataLinkLayer = new DataLinkLayer();
        transportLayer = new TransportLayer();
        aesVerification = new AesVerification();

        ciFieldIndex = dataLinkLayer.parse(buffer, 0);

        index = transportLayer.parse(buffer, ciFieldIndex);

        getLogger().log(dataLinkLayer.toString());
        getLogger().log(transportLayer.toString());

        byte[] aplLayerPayload = decryptEncryptionBlocks(buffer, index, getKey(), getInitializationVector());

        getLogger().log("Decrypted payload", aplLayerPayload);

        index = aesVerification.parse(aplLayerPayload,0);

        final byte SHORT_HEADER_LENGTH = 4;
        byte[] telegramPayload = new byte[aplLayerPayload.length + SHORT_HEADER_LENGTH];

        System.arraycopy(buffer, ciFieldIndex, telegramPayload, 0, SHORT_HEADER_LENGTH);
        System.arraycopy(aplLayerPayload, 0, telegramPayload, SHORT_HEADER_LENGTH, aplLayerPayload.length);

        getLogger().log("Final telegram", telegramPayload);



    }

    public byte[] decryptEncryptionBlocks(byte[] buffer, int index, byte[] aesKey, byte[] initializationVector) {
        int dataLength = buffer.length - index;

        byte[] data = new byte[dataLength];
        byte[] payload = new byte[dataLength];
        int payloadIndex = 0;

        System.arraycopy(buffer, index, data, 0, dataLength);
        getLogger().log("Encrypted data ", data);

        while (index < buffer.length) {
            byte[] encryptionBlock = new byte[AES_BLOCK_SIZE];
            Arrays.fill(encryptionBlock, (byte) 0); // zero-padding
            System.arraycopy(buffer, index, encryptionBlock, 0, Math.max(16, index+AES_BLOCK_SIZE-buffer.length));

            try {
                byte[] decryptedData = decrypt(encryptionBlock, aesKey, initializationVector);
                System.arraycopy(decryptedData, 0, payload, payloadIndex, AES_BLOCK_SIZE);
                payloadIndex += AES_BLOCK_SIZE;
            } catch (Exception ex) {
                getLogger().log("Error decrypting: " + ex.getLocalizedMessage());
            }

            index += AES_BLOCK_SIZE;
        }

        return payload;
    }

    private byte[] getInitializationVector() {
        return ProtocolTools.getBytesFromHexString("00000000000000000000000000000000", "");
    }

    private byte[] getKey() {
        return ProtocolTools.getBytesFromHexString("4FA70B24465F814A667631773A397644", "");
    }

    public byte[] decrypt(byte[] cipheredData, byte[] aesKey, byte[] initializationVector) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher decrypt = Cipher.getInstance(AES_CBC_NO_PADDING);

        IvParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector);

        SecretKeySpec skeySpec = new SecretKeySpec(aesKey, "AES");

        //getLogger().log(" Ciphered data : "+ DatatypeConverter.printHexBinary(cipheredData));
       // getLogger().log(" AES IV (128)  : "+DatatypeConverter.printHexBinary(ivParameterSpec.getIV()));
       // getLogger().log(" AES-256 key   : "+DatatypeConverter.printHexBinary(skeySpec.getEncoded()));

        decrypt.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);


        byte[] clear = decrypt.doFinal(cipheredData);
        //getLogger().log("Decrypted data: ", clear);
        return clear;

    }

 */
}

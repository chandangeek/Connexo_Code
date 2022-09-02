package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams;


import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.TelegramBody;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.header.TelegramHeader;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.AESEncrypt;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.Converter;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Arrays;

public class Telegram {

    private static final int BLOCK_SIZE = 16; /* Encryption Block size */
    private TelegramHeader header;
    private TelegramBody body;

    public Telegram() {
        this.header = new TelegramHeader();
        this.body = new TelegramBody();
    }

    public Telegram(TelegramHeader header, TelegramBody body) {
        super();
        this.header = header;
        this.body = body;
    }

    public void createTelegram(String telegram, boolean crc) {
        this.createTelegram(telegram.split(" "), crc);
    }

    public void createTelegram(String[] telegram, boolean crc) {
        int headerLength = TelegramHeader.headerLengthCRC;
        if(crc == false) {
            headerLength = TelegramHeader.headerLengthNoCRC;
        }

        String[] headerPart = Arrays.copyOfRange(telegram, 0, headerLength + 1);
        String[] bodyPart = Arrays.copyOfRange(telegram, headerLength + 1, telegram.length);

        this.header.createTelegramHeader(headerPart);
        this.body.createTelegramBody(bodyPart);
    }

    public TelegramHeader getHeader() {
        return header;
    }

    public void setHeader(TelegramHeader header) {
        this.header = header;
    }

    public TelegramBody getBody() {
        return body;
    }

    public void setBody(TelegramBody body) {
        this.body = body;
    }

    public boolean decryptTelegram(String aesKey) {
        if(aesKey == null){
            this.getBody().getBodyPayload().setDecryptedTelegramBodyPayload(this.getBody().getBodyPayload().getEncryptedPayload());
            return false;
        }
        byte[] keyArr = Converter.convertStringArrToByteArray(aesKey.split(" "));
        byte[] initCTRVArr = this.getAESCBCInitVector();

        try {
            System.out.println("Encrypted payload: " +this.getBody().getBodyPayload().getPayloadAsString().replace(" ",""));

            final int length = this.getBody().getBodyPayload().getEncryptedPayload().length;
            int start = 0;
            byte[] decryptedResult = new byte[length];

            while (start <= length - BLOCK_SIZE) {
                byte[] payloadArr = Converter.convertStringListToByteArray(this.getBody().getBodyPayload().getPayloadAsList().subList(start, start+BLOCK_SIZE));

                //System.out.println("\t EB: " + Converter.convertByteArrayToString(payloadArr).replace(" ",""));
                byte[] result = AESEncrypt.decrypt(payloadArr, keyArr, initCTRVArr);

                System.arraycopy(result, 0, decryptedResult, start, BLOCK_SIZE);
                start += BLOCK_SIZE;
            }
            this.getBody().getBodyPayload().setDecryptedTelegramBodyPayload(Converter.convertByteArrayToString(decryptedResult).split(" "));
            System.out.println("Decrypted payload: " + Converter.convertByteArrayToString(decryptedResult).replace(" ",""));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void parse() {
        this.body.parse();
    }

    public byte[] getAESCBCInitVector() {
        byte[] properInitVector = this.generateAESCBCInitVector();
        byte[] dummyInitVector = ProtocolTools.getBytesFromHexString("00000000000000000000000000000000", "");
        //System.out.println("Proper init-vector: " + Converter.convertByteArrayToString(properInitVector).replace(" ",""));
        return dummyInitVector;
    }

    private byte[] generateAESCBCInitVector() {
        byte[] aesCbcInitVector = new byte[16];
        byte[] aesCbcInitVectorHeaderPart = this.header.getAESCBCInitVectorPart();
        byte[] aesCbcInitVectorBodyPart = this.body.getBodyHeader().getAESCBCInitVectorPart();
        System.arraycopy(aesCbcInitVectorHeaderPart, 0, aesCbcInitVector, 0, aesCbcInitVectorHeaderPart.length);
        System.arraycopy(aesCbcInitVectorBodyPart, 0, aesCbcInitVector, aesCbcInitVectorHeaderPart.length, aesCbcInitVectorBodyPart.length);

        return aesCbcInitVector;
    }

    public void debugOutput() {
        if(this.header != null) {
            this.header.debugOutput();
        }
        if(this.body != null) {
            this.body.debugOutput();
        }
    }

    public String getSerialNr() {
        return this.getHeader().getSerialNr();
    }

    public String getPowerValue() {
        return this.getBody().getPowerValue();
    }

    public String getEnergyValue() {
        return this.getBody().getEnergyValue();
    }

}
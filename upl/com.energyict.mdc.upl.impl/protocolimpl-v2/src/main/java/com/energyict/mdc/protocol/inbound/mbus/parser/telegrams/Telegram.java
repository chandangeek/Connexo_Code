package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams;


import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.TelegramBody;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.header.TelegramHeader;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.AESEncrypt;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.Converter;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Arrays;

public class Telegram {

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
            return false;
        }
        byte[] keyArr = Converter.convertStringArrToByteArray(aesKey.split(" "));
        byte[] initCTRVArr = this.getAESCBCInitVector();
        byte[] payloadArr = Converter.convertStringListToByteArray(this.getBody().getBodyPayload().getPayloadAsList());

        try {
            byte[] result = AESEncrypt.decrypt(payloadArr, keyArr, initCTRVArr);
            this.getBody().getBodyPayload().setDecryptedTelegramBodyPayload(Converter.convertByteArrayToString(result).split(" "));
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
        return ProtocolTools.getBytesFromHexString("00000000000000000000000000000000", "");
        //return this.generateAESCBCInitVector();
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
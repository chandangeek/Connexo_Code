package com.energyict.mdc.protocol.inbound.mbus.parser;

import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.MerlinLogger;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.protocolimpl.utils.ProtocolTools;

public class MerlinMbusParser {
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

    public Telegram parseHeader(byte[] buffer){
        telegram = new Telegram(getLogger());
        String telegramString = ProtocolTools.getHexStringFromBytes(buffer, " ").trim();
        telegram.createTelegram(telegramString, false);

        return telegram;
    }

    public Telegram parse(){

        boolean result = telegram.decryptTelegram(getInboundContext().getEncryptionKey());

        if (result) {
            if (isDecryptionOk()) {
                telegram.parse();
                // telegram.debugOutput();

                return telegram;
            } else {
                getLogger().error("Decryption is not correct!");
            }
        } else {
            getLogger().error("Could not decrypt telegram!");
        }
        // TODO -> check invalid decryption, check 2f 2f, throw errors, etc

        return null;
    }

    private boolean isDecryptionOk() {
        try {
            return "2f".equalsIgnoreCase(telegram.getBody().getBodyPayload().getDecryptedPayloadAsList().get(0))
                    && "2f".equalsIgnoreCase(telegram.getBody().getBodyPayload().getDecryptedPayloadAsList().get(1));
        } catch (Exception ex) {
            getLogger().error("Could not check if decryption is ok");
            return false;
        }
    }

}

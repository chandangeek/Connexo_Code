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
            telegram.parse();
            return telegram;
        } else {
            getLogger().error("Could not decrypt telegram!");
        }

        return null;
    }


}

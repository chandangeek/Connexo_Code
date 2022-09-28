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
        telegram = new Telegram();
        String telegramString = ProtocolTools.getHexStringFromBytes(buffer, " ").trim();
        telegram.createTelegram(telegramString, false);

        return telegram;
    }

    public Telegram parse(){
        telegram.decryptTelegram(getInboundContext().getEncryptionKey());

        // TODO -> check invalid decryption, check 2f 2f, throw errors, etc

        //telegram.decryptTelegram(null);
        telegram.parse();
       // telegram.debugOutput();

        return telegram;
    }

}

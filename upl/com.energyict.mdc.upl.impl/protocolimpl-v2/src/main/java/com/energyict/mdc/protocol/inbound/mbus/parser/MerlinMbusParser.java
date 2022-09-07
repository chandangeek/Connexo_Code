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

    public Telegram parse(byte[] buffer){
        telegram = new Telegram();
        String telegramString = ProtocolTools.getHexStringFromBytes(buffer, " ").trim();
        telegram.createTelegram(telegramString, false);
                                      //4FA70B24465F814A667631773A397644
        telegram.decryptTelegram("4F A7 0B 24 46 5F 81 4A 66 76 31 77 3A 39 76 44");
        //telegram.decryptTelegram(null);
        telegram.parse();
        telegram.debugOutput();

        return telegram;
    }

}

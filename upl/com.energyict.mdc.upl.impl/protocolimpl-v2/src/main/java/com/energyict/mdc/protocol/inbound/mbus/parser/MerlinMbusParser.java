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
                                      //4FA70B24465F814A667631773A397644
        telegram.decryptTelegram("4F A7 0B 24 46 5F 81 4A 66 76 31 77 3A 39 76 44");
        //telegram.decryptTelegram(null);
        telegram.parse();
        telegram.debugOutput();
    }

}

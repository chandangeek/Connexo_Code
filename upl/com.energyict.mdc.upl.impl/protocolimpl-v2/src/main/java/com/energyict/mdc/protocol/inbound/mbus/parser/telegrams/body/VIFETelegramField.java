package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body;

import com.energyict.mdc.protocol.inbound.mbus.MerlinLogger;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.TelegramField;

import java.util.StringJoiner;

public class VIFETelegramField extends TelegramField {

    private boolean extensionBit = false;

    public VIFETelegramField(MerlinLogger logger) {
        super(logger);
    }

    public boolean isExtensionBit() {
        return extensionBit;
    }

    public void setExtensionBit(boolean extensionBit) {
        this.extensionBit = extensionBit;
    }

    public void debugOutput(StringJoiner joiner) {
        joiner.add("VIFE-Field: ");
        joiner.add("\tExtension-Bit: \t" + this.extensionBit);
        // TODO: finish
    }
}
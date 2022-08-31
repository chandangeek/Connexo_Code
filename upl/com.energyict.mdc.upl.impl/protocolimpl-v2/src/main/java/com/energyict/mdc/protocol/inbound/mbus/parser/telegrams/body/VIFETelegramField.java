package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body;

import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.TelegramField;

public class VIFETelegramField extends TelegramField {

    private boolean extensionBit = false;

    public boolean isExtensionBit() {
        return extensionBit;
    }

    public void setExtensionBit(boolean extensionBit) {
        this.extensionBit = extensionBit;
    }

    public void debugOutput() {
        System.out.println("VIFE-Field: ");
        System.out.println("\tExtension-Bit: \t" + this.extensionBit);
        // TODO: finish
    }
}
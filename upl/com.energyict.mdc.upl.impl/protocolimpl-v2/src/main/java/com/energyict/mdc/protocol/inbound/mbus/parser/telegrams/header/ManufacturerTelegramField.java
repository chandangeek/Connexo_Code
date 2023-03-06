/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.header;

import com.energyict.mdc.protocol.inbound.mbus.MerlinLogger;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.TelegramField;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.Converter;

public class ManufacturerTelegramField extends TelegramField {

    private static final int MASK_5_BIT = 0x1F; 		// FIRST 5-BIT-MASK
    private static final int MASK_10_BIT = 0x3FF; 	// FIRST 10-BIT-MASK

    public ManufacturerTelegramField(MerlinLogger logger) {
        super(logger);
    }

    public void parse() {
        int telAsInt = Converter.hexToInt(this.fieldParts.get(1) + this.fieldParts.get(0));

        int lastAsciiLetter = telAsInt & ManufacturerTelegramField.MASK_5_BIT;
        char lastLetter = (char) (lastAsciiLetter + 64);

        int secondAsciiLetter = telAsInt - lastAsciiLetter;
        secondAsciiLetter = secondAsciiLetter & ManufacturerTelegramField.MASK_10_BIT;
        char secondLetter = (char) ((secondAsciiLetter / 32) + 64);

        int firstAsciiLetter = telAsInt - (int) lastLetter - (int) secondLetter;
        char firstLetter = (char) ((firstAsciiLetter / 32 / 32) + 64);

        this.parsedValue =  String.valueOf(firstLetter) + secondLetter + lastLetter;
    }
}

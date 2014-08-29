package com.elster.jupiter.metering;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Copyrights EnergyICT
 * Date: 29/08/2014
 * Time: 14:25
 */
public class CannotDeleteMeter extends LocalizedException {

    public CannotDeleteMeter(Thesaurus thesaurus, MessageSeed reason, String mRID) {
        super(thesaurus, reason, mRID);
    }
}

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class CannotDeleteFormulaException extends LocalizedException {

    public CannotDeleteFormulaException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.CAN_NOT_DELETE_FORMULA_IN_USE);
    }
}

package com.elster.jupiter.estimation;

import com.elster.jupiter.estimation.impl.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class EstimatorNotFoundException extends LocalizedException {

    public EstimatorNotFoundException(Thesaurus thesaurus, String implementation) {
        super(thesaurus, MessageSeeds.NO_SUCH_ESTIMATOR, implementation);
        set("implementation", implementation);
    }
}

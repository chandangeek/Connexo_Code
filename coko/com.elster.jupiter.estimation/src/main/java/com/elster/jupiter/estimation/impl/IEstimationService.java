package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.nls.Thesaurus;

public interface IEstimationService extends EstimationService {

    Thesaurus getThesaurus();
}

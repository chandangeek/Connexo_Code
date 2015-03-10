package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.RecurrentTask;

import java.util.Optional;

public interface IEstimationService extends EstimationService {

    Thesaurus getThesaurus();

    DestinationSpec getDestination();

    Optional<? extends EstimationTask> findEstimationTask(RecurrentTask recurrentTask);
}

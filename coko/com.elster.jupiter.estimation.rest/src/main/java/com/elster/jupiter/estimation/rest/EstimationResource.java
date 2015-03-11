package com.elster.jupiter.estimation.rest;


import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import javax.inject.Inject;
import javax.ws.rs.Path;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/validation")
public class EstimationResource {

    private final RestQueryService queryService;
    private final EstimationService estimationService;
    private final TransactionService transactionService;

    @Inject
    public EstimationResource(RestQueryService queryService, EstimationService estimationService, TransactionService transactionService) {
        this.queryService = queryService;
        this.estimationService = estimationService;
        this.transactionService = transactionService;
    }


}

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.exception.MessageSeed;
import org.mockito.Mock;

import javax.ws.rs.core.Application;
import java.time.Clock;

public class MeteringApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    MeteringService meteringService;
    @Mock
    RestQueryService restQueryService;
    @Mock
    Clock clock;

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
    }

    @Override
    protected Application getApplication() {
        MeteringApplication app = new MeteringApplication();
        app.setClock(clock);
        app.setTransactionService(transactionService);
        app.setRestQueryService(restQueryService);
        app.setMeteringService(meteringService);
        app.setNlsService(nlsService);
        return app;
    }
}

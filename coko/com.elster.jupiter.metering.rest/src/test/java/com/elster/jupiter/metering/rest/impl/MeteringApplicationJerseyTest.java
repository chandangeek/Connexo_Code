package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.RestQueryService;

import javax.ws.rs.core.Application;
import java.time.Clock;

import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class MeteringApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    MeteringService meteringService;
    @Mock
    RestQueryService restQueryService;
    @Mock
    Clock clock;

    @Override
    protected Application getApplication() {
        when(thesaurus.join(any(Thesaurus.class))).thenReturn(thesaurus);
        MeteringApplication app = new MeteringApplication();
        app.setClock(clock);
        app.setTransactionService(transactionService);
        app.setRestQueryService(restQueryService);
        app.setMeteringService(meteringService);
        app.setNlsService(nlsService);
        return app;
    }

}
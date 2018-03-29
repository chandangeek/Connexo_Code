/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.rest.impl.CertificateRevocationUtils;
import com.elster.jupiter.pki.rest.impl.PkiApplication;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.Transaction;

import javax.ws.rs.core.Application;

import org.mockito.Matchers;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

public class PkiApplicationTest extends FelixRestApplicationJerseyTest {

    @Mock
    SecurityManagementService securityManagementService;
    @Mock
    CaService caService;
    @Mock
    RestQueryService restQueryService;
    @Mock
    CertificateRevocationUtils revocationUtils;

    @Override
    protected Application getApplication() {
        PkiApplication application = new PkiApplication();
        application.setSecurityManagementService(securityManagementService);
        application.setCaService(caService);
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setCertificateRevocationUtils(revocationUtils);
        return application;
    }

    @Override
    public void setupMocks() {
        super.setupMocks();
        when(transactionService.execute(Matchers.any(Transaction.class)))
                .thenAnswer(invocation -> ((Transaction) invocation.getArguments()[0]).perform());
    }

}

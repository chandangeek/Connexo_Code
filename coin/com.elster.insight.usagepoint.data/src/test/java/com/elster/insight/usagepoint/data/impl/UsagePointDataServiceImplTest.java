package com.elster.insight.usagepoint.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class UsagePointDataServiceImplTest {
    private static UsagePointDataInMemoryBootstrapModule inMemoryBootstrapModule = new UsagePointDataInMemoryBootstrapModule();

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void beforeClass(){
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void afterClass(){
        inMemoryBootstrapModule.deactivate();
    }

    // NO TESTS FOR NOW
}

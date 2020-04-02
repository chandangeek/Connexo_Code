package com.elster.jupiter.http.whiteboard.impl.token;

import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;

@RunWith(MockitoJUnitRunner.class)
public class TokenServicePersistence {

    protected static TokenServiceBootstrapModule inMemoryPersistence = new TokenServiceBootstrapModule();

    @Rule
    public TestRule transactionalRule = new TransactionalRule(inMemoryPersistence.getTransactionService());

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence.initializeDatabase();
    }

    @AfterClass
    public static void cleanUpDatabase() throws SQLException {
        inMemoryPersistence.cleanUpDatabase();
    }

}

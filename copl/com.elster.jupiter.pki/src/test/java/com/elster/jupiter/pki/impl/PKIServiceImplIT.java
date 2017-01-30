package com.elster.jupiter.pki.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.pki.KeyType;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PKIServiceImplIT {

    private static PkiInMemoryPersistence pkiInMemoryPersistence = new PkiInMemoryPersistence();

    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();
    @Rule
    public TestRule transactionalRule = new TransactionalRule(pkiInMemoryPersistence.getTransactionService());

    @BeforeClass
    public static void initialize() {
        pkiInMemoryPersistence.activate();
    }

    @Test
    @Transactional
    public void findCreateSymmetricKey() {
        KeyType keyType = pkiInMemoryPersistence.getPkiService().addSymmetricKeyType("AES128", "AES", 1024);
        List<KeyType> keyTypes = pkiInMemoryPersistence.getPkiService().findAllKeyTypes().find();
        assertThat(keyTypes).hasSize(1);
        assertThat(keyTypes.get(0).getName()).isEqualTo("AES128");
        assertThat(keyTypes.get(0).getAlgorithm()).isEqualTo("AES");
        assertThat(keyTypes.get(0).getKeySize()).isEqualTo(1024);
        assertThat(keyTypes.get(0).getCurve()).isNull();
    }

}

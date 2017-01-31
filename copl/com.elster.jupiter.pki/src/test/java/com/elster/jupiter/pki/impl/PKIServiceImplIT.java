package com.elster.jupiter.pki.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyType;

import java.util.Optional;

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
    public void testCreateSymmetricKey() {
        KeyType created = pkiInMemoryPersistence.getPkiService().addSymmetricKeyType("AES128", "AES", 128);
        Optional<KeyType> keyType = pkiInMemoryPersistence.getPkiService().getKeyType("AES128");
        assertThat(keyType).isPresent();
        assertThat(keyType.get().getName()).isEqualTo("AES128");
        assertThat(keyType.get().getAlgorithm()).isEqualTo("AES");
        assertThat(keyType.get().getCryptographicType()).isEqualTo(CryptographicType.SymmetricKey);
        assertThat(keyType.get().getKeySize()).isEqualTo(128);
        assertThat(keyType.get().getCurve()).isNull();
    }

    @Test
    @Transactional
    public void testCreateRSAKey() {
        KeyType created = pkiInMemoryPersistence.getPkiService().addAsymmetricKeyType("RSA2048").RSA().keySize(2048).add();
        Optional<KeyType> keyType = pkiInMemoryPersistence.getPkiService().getKeyType("RSA2048");
        assertThat(keyType).isPresent();
        assertThat(keyType.get().getName()).isEqualTo("RSA2048");
        assertThat(keyType.get().getAlgorithm()).isEqualTo("RSA");
        assertThat(keyType.get().getCryptographicType()).isEqualTo(CryptographicType.AsymmetricKey);
        assertThat(keyType.get().getKeySize()).isEqualTo(2048);
        assertThat(keyType.get().getCurve()).isNull();
    }

    @Test
    @Transactional
    public void testCreateDSAKey() {
        KeyType created = pkiInMemoryPersistence.getPkiService().addAsymmetricKeyType("DSA1024").DSA().keySize(1024).add();
        Optional<KeyType> keyType = pkiInMemoryPersistence.getPkiService().getKeyType("DSA1024");
        assertThat(keyType).isPresent();
        assertThat(keyType.get().getName()).isEqualTo("DSA1024");
        assertThat(keyType.get().getAlgorithm()).isEqualTo("DSA");
        assertThat(keyType.get().getCryptographicType()).isEqualTo(CryptographicType.AsymmetricKey);
        assertThat(keyType.get().getKeySize()).isEqualTo(1024);
        assertThat(keyType.get().getCurve()).isNull();
    }

    @Test
    @Transactional
    public void testCreateECKey() {
        KeyType created = pkiInMemoryPersistence.getPkiService().addAsymmetricKeyType("NIST P-256").EC().curve("secp256r1").add();
        Optional<KeyType> keyType = pkiInMemoryPersistence.getPkiService().getKeyType("NIST P-256");
        assertThat(keyType).isPresent();
        assertThat(keyType.get().getName()).isEqualTo("NIST P-256");
        assertThat(keyType.get().getAlgorithm()).isEqualTo("EC");
        assertThat(keyType.get().getCryptographicType()).isEqualTo(CryptographicType.AsymmetricKey);
        assertThat(keyType.get().getKeySize()).isEqualTo(0); // CXO-5375, I expect null here
        assertThat(keyType.get().getCurve()).isEqualTo("secp256r1");
    }

}

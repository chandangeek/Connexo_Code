package com.elster.jupiter.util;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;

import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Random;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class KeyStoreCacheIT extends AbstractBenchmark {

    private static final String KEY_STORE_TYPE = "JCEKS";
    private static final char[] PASSOWRD = {'P', 'a', 's', 's'};
    private static int NO_OF_KEYS = 16;


    private static final Random random = new Random();
    private static final KeyStoreAliasGenerator aliasGenerator = new  KeyStoreAliasGenerator("KEY-", NO_OF_KEYS);

    private static KeyStoreCache keyStoreCache;


    @BeforeClass
    public static void setUp() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {

        KeyStore keyStore = KeyStoreLoader.generate(new TestOutStream(), KEY_STORE_TYPE, aliasGenerator,  PASSOWRD);
        keyStoreCache = new KeyStoreCache(keyStore);
    }

    @BenchmarkOptions(benchmarkRounds = 1000, warmupRounds = 30)
    @Test
    public void test() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        Assert.assertNotNull(keyStoreCache.getKey(aliasGenerator.getAlias(random.nextInt(NO_OF_KEYS) + 1), PASSOWRD));
    }

    public static class TestOutStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {

        }
    }

}

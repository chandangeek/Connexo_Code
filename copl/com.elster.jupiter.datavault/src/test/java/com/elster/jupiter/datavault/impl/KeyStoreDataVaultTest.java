/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import java.util.Random;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeyStoreDataVaultTest {

    @Mock
    Random random;
    @Mock
    Thesaurus thesaurus;
    @Mock
    NlsService nlsService;

    private KeyStoreDataVault keyStoreDataVault;

    private static final Integer RANDOM_INT = 7;

    @Before
    public void setUp() throws Exception {
        when(random.nextInt(anyInt())).thenReturn(RANDOM_INT);
        when(nlsService.getThesaurus(anyString(), anyObject())).thenReturn(thesaurus);
        when(thesaurus.getString(anyString(), anyString())).thenAnswer(invocationOnMock -> {
            for (MessageSeed messageSeeds : MessageSeeds.values()) {
                if (messageSeeds.getKey().equals(invocationOnMock.getArguments()[0])) {
                    return messageSeeds.getDefaultFormat();
                }
            }
            return (String) invocationOnMock.getArguments()[1];
        });
        when(thesaurus.getFormat(Matchers.<MessageSeed>anyObject())).thenAnswer(invocation -> new SimpleNlsMessageFormat((MessageSeed) invocation.getArguments()[0]));

        keyStoreDataVault = new JarKeyStoreDataVault(random, new ExceptionFactory(thesaurus));
   }

    @Test
    public void testEncryptDecryptResultsInOriginalMessage() throws Exception {
        String plainText = "my secret message";
        String cipherText = keyStoreDataVault.encrypt(plainText.getBytes());
        byte[] decrypted = keyStoreDataVault.decrypt(cipherText);

        assertThat(plainText).isEqualTo(new String(decrypted));
    }

    @Test
    public void testEncryptDecryptShortMessageResultsInOriginalMessage() throws Exception {
        String plainText = "my";
        String cipherText = keyStoreDataVault.encrypt(plainText.getBytes());
        byte[] decrypted = keyStoreDataVault.decrypt(cipherText);

        assertThat(plainText).isEqualTo(new String(decrypted));
    }

    @Test
    public void testEncryptDecryptLongMessageResultsInOriginalMessage() throws Exception {
        String plainText = "abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefghijklmnopqrstuvwxyz0123456789";
        String cipherText = keyStoreDataVault.encrypt(plainText.getBytes());
        byte[] decrypted = keyStoreDataVault.decrypt(cipherText);

        assertThat(plainText).isEqualTo(new String(decrypted));
    }

    @Test
    public void testEncryptingTwiceWithSameKeyYieldsDifferentCipherText() throws Exception {
        String plainText = "my secret message";
        String cipherText1 = keyStoreDataVault.encrypt(plainText.getBytes());
        String cipherText2 = keyStoreDataVault.encrypt(plainText.getBytes());

        assertThat(cipherText1).isNotEqualTo(cipherText2);
    }

    @Test
    public void testEncryptNull() throws Exception {
        final String encrypt = keyStoreDataVault.encrypt(null);
        assertThat(encrypt).isEqualTo("");
    }

    @Test
    public void testEncryptEmptyArray() throws Exception {
        keyStoreDataVault.encrypt(new byte[0]);
    }

    @Test
    public void testDecryptNull() throws Exception {
        try {
            keyStoreDataVault.decrypt(null);
        } catch (Exception e) {
            fail("Should not throw an exception, was "+e);
        }
    }

    @Test
    public void testDecryptEmptyString() throws Exception {
        try {
            keyStoreDataVault.decrypt("");
        } catch (Exception e) {
            fail("Should not throw an exception, was "+e);
        }
    }

    class JarKeyStoreDataVault extends KeyStoreDataVault {

        private String eictKeyStoreResourceName = "eictKeyStore";

        @Inject
        public JarKeyStoreDataVault(Random random, ExceptionFactory exceptionFactory) {
            super(random, exceptionFactory);
            readKeyStore(this.getClass().getClassLoader().getResourceAsStream(eictKeyStoreResourceName));
        }
    }
}

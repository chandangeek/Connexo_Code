package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.DeviceSecretImporter;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.KeypairWrapper;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.exception.MessageSeed;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 8/24/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataVaultSymmetricKeyImporterTest {

    private static final String SYMMETRIC_ALGORITHM = "AES/CBC/PKCS5PADDING";
    private static final String ASYMMETRIC_ALGORITHM = "RSA/ECB/PKCS1Padding";
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DataModel dataModel;
    @Mock
    private SecurityManagementService securityManagementService;
    @Mock
    private DataVaultService dataVaultService;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;

    private Clock clock = Clock.system(ZoneId.systemDefault());

    private SecretKey wrapKey;
    private KeyPair hsmKeyPair;
    private byte[] encryptedDeviceSecret;
    private byte[] iv;
    private byte[] encryptedWrapKey;

    @Before
    public void setUp() throws Exception {
        when(thesaurus.getFormat(any(TranslationKey.class)))
                .thenAnswer(invocation -> new SimpleNlsMessageFormat((TranslationKey) invocation.getArguments()[0]));
        when(thesaurus.getFormat(any(MessageSeed.class)))
                .thenAnswer(invocation -> new SimpleNlsMessageFormat((MessageSeed) invocation.getArguments()[0]));
        when(thesaurus.getString(anyString(), anyString())).then(invocation -> invocation.getArgumentAt(1, String.class));
        generateKeys();

        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(validator.validate(anyObject(), any(Class.class))).thenReturn(new HashSet<>());
    }

    private void generateKeys() throws
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        hsmKeyPair = keyPairGenerator.generateKeyPair();

        wrapKey = keyGenerator.generateKey();

        Cipher symCipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
        symCipher.init(Cipher.ENCRYPT_MODE, wrapKey);
        String deviceKey = "Hello world";
        encryptedDeviceSecret = symCipher.doFinal(deviceKey.getBytes());
        iv = symCipher.getIV();

        Cipher asymCipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);
        asymCipher.init(Cipher.ENCRYPT_MODE, hsmKeyPair.getPublic());
        encryptedWrapKey = asymCipher.doFinal(wrapKey.getEncoded());
    }

    @Test
    public void testSuccessfulImport() throws Exception {
        PrivateKeyWrapper privateKeyWrapper = mock(PrivateKeyWrapper.class);
        when(privateKeyWrapper.getPrivateKey()).thenReturn(Optional.ofNullable(hsmKeyPair.getPrivate()));
        KeypairWrapper keypairWrapper = mock(KeypairWrapper.class);
        when(keypairWrapper.getPrivateKeyWrapper()).thenReturn(Optional.of(privateKeyWrapper));
        when(keypairWrapper.hasPrivateKey()).thenReturn(true);
        when(securityManagementService.findKeypairWrapper("ImportKey")).thenReturn(Optional.of(keypairWrapper));

        SecurityAccessorType securityAccessorType = mock(SecurityAccessorType.class);
        KeyType keyType = mock(KeyType.class);
        when(securityAccessorType.getKeyType()).thenReturn(keyType);
        when(securityAccessorType.getDuration()).thenReturn(Optional.of(TimeDuration.years(2)));
        when(keyType.getKeyAlgorithm()).thenReturn(ASYMMETRIC_ALGORITHM);
        DeviceSecretImporter deviceKeyImporter = new DataVaultSymmetricKeyImporter(securityAccessorType, thesaurus, securityManagementService, Optional.of("ImportKey"));
        PlaintextSymmetricKey plaintextSymmetricKeyWrapper = mock(PlaintextSymmetricKey.class);
        when(securityManagementService.newSymmetricKeyWrapper(securityAccessorType)).thenReturn(plaintextSymmetricKeyWrapper);
        SecurityValueWrapper securityValueWrapper = deviceKeyImporter.importSecret(encryptedDeviceSecret, iv, encryptedWrapKey, SYMMETRIC_ALGORITHM, ASYMMETRIC_ALGORITHM);

        ArgumentCaptor<SecretKey> secretKeyArgumentCaptor = ArgumentCaptor.forClass(SecretKey.class);
        verify(plaintextSymmetricKeyWrapper, times(1)).setKey(secretKeyArgumentCaptor.capture());
        assertThat(new String(secretKeyArgumentCaptor.getValue().getEncoded())).isEqualTo("Hello world");
    }

    class SimpleNlsMessageFormat implements NlsMessageFormat {

        private final String defaultFormat;

        SimpleNlsMessageFormat(MessageSeed messageSeed) {
            this.defaultFormat = messageSeed.getDefaultFormat();
        }

        SimpleNlsMessageFormat(TranslationKey translationKey) {
            this.defaultFormat = translationKey.getDefaultFormat();
        }

        @Override
        public String format(Object... args) {
            return MessageFormat.format(defaultFormat, args);
        }

        @Override
        public String format(Locale locale, Object... args) {
            return MessageFormat.format(defaultFormat, args);
        }
    }
}

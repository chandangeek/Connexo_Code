package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.crypto.spec.SecretKeySpec;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.HsmEncryptedKey;
import com.elster.jupiter.hsm.model.keys.HsmKeyType;
import com.elster.jupiter.hsm.model.krypto.AsymmetricAlgorithm;
import com.elster.jupiter.hsm.model.krypto.SymmetricAlgorithm;
import com.elster.jupiter.hsm.model.request.ImportKeyRequest;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.HsmKey;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigFaultMessageFactory;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.SecurityAccessor;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;

@RunWith(MockitoJUnitRunner.class)
public class SecurityHelperTest {

	private static final String DEVICE_NAME = "DEVICE_NAME";
	private static final String PUBLIC_KEY_LABEL = "PUBLIC_KEY_LABEL";
	private static final byte[] SECURITY_ACCESSOR_KEY = "SECURITY_ACCESSOR_KEY".getBytes();
	private static final String SECURITY_ACCESSOR_NAME = "SECURITY_ACCESSOR_NAME";
	private static final byte[] SYMMETRIC_KEY = "SYMMETRIC_KEY".getBytes();
	private static final byte[] HSM_ENCRYPTED_KEY = "HSM_ENCRYPTED_KEY".getBytes();
	private static final String HSM_KEY_LABEL = "HSM_KEY_LABEL";
	private static final String KEY_ALGORITHM = "KEY_ALGORITHM";

	@Mock
	private HsmEnergyService hsmEnergyService;

	@Mock
	private SecurityManagementService securityManagementService;

	@Mock
	private MeterConfigFaultMessageFactory faultMessageFactory;

	@Mock
	private Thesaurus thesaurus;

	@Mock
	private Device device;

	@Mock
	private DeviceType deviceType;

	@Mock
	private SecurityAccessorType securityAccessorType;

	@Mock
	private SecurityAccessor<SecurityValueWrapper> securityAccessor;

	@Mock
	private HsmKeyType hsmKeyType;

	@Mock
	private HsmKey hsmKey;

	@Mock
	private PlaintextSymmetricKey plaintextSymmetricKey;

	@Mock
	private PlaintextPassphrase plaintextPassphrase;

	@Mock
	private KeyType keyType;

	@Mock
	private HsmEncryptedKey hsmEncryptedKey;

	@Mock
	private ServiceCall serviceCall;

	private SecurityHelper testable;

	@Before
	public void setUp() {
		final ArgumentCaptor<MessageSeeds> captor = ArgumentCaptor.forClass(MessageSeeds.class);
		when(thesaurus.getSimpleFormat(captor.capture())).thenReturn(new NlsMessageFormat() {

			@Override
			public String format(Locale locale, Object... args) {
				return MessageFormat.format(captor.getValue().getDefaultFormat(), args);
			}

			@Override
			public String format(Object... args) {
				return MessageFormat.format(captor.getValue().getDefaultFormat(), args);
			}
		});
		when(device.getName()).thenReturn(DEVICE_NAME);
		when(device.getDeviceType()).thenReturn(deviceType);
		when(securityAccessorType.getName()).thenReturn(SECURITY_ACCESSOR_NAME);
		when(securityAccessorType.getHsmKeyType()).thenReturn(hsmKeyType);
		when(hsmEncryptedKey.getEncryptedKey()).thenReturn(HSM_ENCRYPTED_KEY);
		when(hsmEncryptedKey.getKeyLabel()).thenReturn(HSM_KEY_LABEL);
		when(securityAccessorType.getKeyType()).thenReturn(keyType);
		when(keyType.getKeyAlgorithm()).thenReturn(KEY_ALGORITHM);
		testable = new SecurityHelper(hsmEnergyService, securityManagementService, faultMessageFactory, thesaurus);
	}

	@Test
	public void testAddKeysHsmSuccess() throws HsmBaseException {
		final SecurityKeyInfo securityInfo = new SecurityKeyInfo();
		securityInfo.setPublicKeyLabel(PUBLIC_KEY_LABEL);
		securityInfo.setSecurityAccessorKey(SECURITY_ACCESSOR_KEY);
		securityInfo.setSecurityAccessorName(SECURITY_ACCESSOR_NAME);
		securityInfo.setSymmetricKey(SYMMETRIC_KEY);
		final List<SecurityKeyInfo> securityInfoList = Arrays.asList(securityInfo);
		List<SecurityAccessorType> securityAccessorTypes = Arrays.asList(securityAccessorType);
		when(deviceType.getSecurityAccessorTypes()).thenReturn(securityAccessorTypes);
		when(device.getSecurityAccessor(securityAccessorType)).thenReturn(Optional.of(securityAccessor));
		when(securityAccessor.getActualValue()).thenReturn(Optional.empty());
		ArgumentCaptor<ImportKeyRequest> captor = ArgumentCaptor.forClass(ImportKeyRequest.class);
		when(hsmEnergyService.importKey(Mockito.notNull(ImportKeyRequest.class))).thenReturn(hsmEncryptedKey);
		when(securityManagementService.newSymmetricKeyWrapper(securityAccessorType)).thenReturn(hsmKey);
		List<FaultMessage> faults = testable.addSecurityKeys(device, securityInfoList, serviceCall);
		assertTrue(faults.isEmpty());
		verify(hsmEnergyService).importKey(captor.capture());
		ImportKeyRequest value = captor.getValue();
		assertEquals(PUBLIC_KEY_LABEL, Whitebox.getInternalState(value, "wrapperKeyLabel"));
		assertEquals(AsymmetricAlgorithm.RSA_15, Whitebox.getInternalState(value, "wrapperKeyAlgorithm"));
		assertEquals(SYMMETRIC_KEY, Whitebox.getInternalState(value, "encryptedTransportKey"));
		assertEquals(SymmetricAlgorithm.AES_256_CBC, Whitebox.getInternalState(value, "deviceKeyAlgorhitm"));
		assertArrayEquals(getCipher(SECURITY_ACCESSOR_KEY),
				(byte[]) Whitebox.getInternalState(value, "deviceKeyValue"));
		assertArrayEquals(getInitializationVector(SECURITY_ACCESSOR_KEY),
				(byte[]) Whitebox.getInternalState(value, "deviceKeyInitialVector"));
		assertEquals(hsmKeyType, Whitebox.getInternalState(value, "hsmKeyType"));
		verify(hsmKey).setKey(HSM_ENCRYPTED_KEY, HSM_KEY_LABEL);
		verify(securityAccessor).setActualValue(hsmKey);
		verify(securityAccessor).save();
	}

	@Test
	public void testAddKeysNoHsmSuccessSymmetricKey() throws HsmBaseException {
		final SecurityKeyInfo securityInfo = new SecurityKeyInfo();
		securityInfo.setPublicKeyLabel(null);
		securityInfo.setSecurityAccessorKey(SECURITY_ACCESSOR_KEY);
		securityInfo.setSecurityAccessorName(SECURITY_ACCESSOR_NAME);
		securityInfo.setSymmetricKey(null);
		final List<SecurityKeyInfo> securityInfoList = Arrays.asList(securityInfo);
		List<SecurityAccessorType> securityAccessorTypes = Arrays.asList(securityAccessorType);
		when(deviceType.getSecurityAccessorTypes()).thenReturn(securityAccessorTypes);
		when(device.getSecurityAccessor(securityAccessorType)).thenReturn(Optional.of(securityAccessor));
		when(securityAccessor.getActualValue()).thenReturn(Optional.empty());
		when(securityManagementService.newSymmetricKeyWrapper(securityAccessorType)).thenReturn(plaintextSymmetricKey);
		List<FaultMessage> faults = testable.addSecurityKeys(device, securityInfoList, serviceCall);
		assertTrue(faults.isEmpty());
		SecretKeySpec secretKeySpec = new SecretKeySpec(SECURITY_ACCESSOR_KEY, KEY_ALGORITHM);
		verify(plaintextSymmetricKey).setKey(secretKeySpec);
		verify(securityAccessor).setActualValue(plaintextSymmetricKey);
		verify(securityAccessor).save();
	}

	@Test
	public void testAddKeysNoHsmSuccessPassphrase() throws HsmBaseException {
		final SecurityKeyInfo securityInfo = new SecurityKeyInfo();
		securityInfo.setPublicKeyLabel(null);
		securityInfo.setSecurityAccessorKey(SECURITY_ACCESSOR_KEY);
		securityInfo.setSecurityAccessorName(SECURITY_ACCESSOR_NAME);
		securityInfo.setSymmetricKey(null);
		final List<SecurityKeyInfo> securityInfoList = Arrays.asList(securityInfo);
		List<SecurityAccessorType> securityAccessorTypes = Arrays.asList(securityAccessorType);
		when(deviceType.getSecurityAccessorTypes()).thenReturn(securityAccessorTypes);
		when(device.getSecurityAccessor(securityAccessorType)).thenReturn(Optional.of(securityAccessor));
		when(securityAccessor.getActualValue()).thenReturn(Optional.empty());
		when(securityManagementService.newPassphraseWrapper(securityAccessorType)).thenReturn(plaintextPassphrase);
		when(keyType.getKeyAlgorithm()).thenReturn(null);
		List<FaultMessage> faults = testable.addSecurityKeys(device, securityInfoList, serviceCall);
		assertTrue(faults.isEmpty());
		SecretKeySpec secretKeySpec = new SecretKeySpec(SECURITY_ACCESSOR_KEY, KEY_ALGORITHM);
		verify(plaintextPassphrase).setPassphrase(new String(SECURITY_ACCESSOR_KEY));
		verify(securityAccessor).setActualValue(plaintextPassphrase);
		verify(securityAccessor).save();
	}

	private byte[] getInitializationVector(byte[] encryptedKey) {
		byte[] initializationVector = new byte[16];
		System.arraycopy(encryptedKey, 0, initializationVector, 0, 16);
		return initializationVector;
	}

	private byte[] getCipher(byte[] encryptedKey) {
		byte[] cipher = new byte[encryptedKey.length - 16];
		System.arraycopy(encryptedKey, 16, cipher, 0, encryptedKey.length - 16);
		return cipher;
	}
}

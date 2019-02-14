/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.HsmEncryptedKey;
import com.elster.jupiter.hsm.model.krypto.AsymmetricAlgorithm;
import com.elster.jupiter.hsm.model.krypto.SymmetricAlgorithm;
import com.elster.jupiter.hsm.model.request.ImportKeyRequest;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.HsmKey;
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigFaultMessageFactory;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.SecurityAccessor;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;

import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class SecurityHelper {

	private static final Logger LOGGER = Logger.getLogger(SecurityHelper.class.getName());

	private final HsmEnergyService hsmEnergyService;

	private final SecurityManagementService securityManagementService;

	private final LoggerUtils loggerUtils;

	@Inject
	public SecurityHelper(HsmEnergyService hsmEnergyService, SecurityManagementService securityManagementService,
			MeterConfigFaultMessageFactory faultMessageFactory, Thesaurus thesaurus) {
		this.hsmEnergyService = hsmEnergyService;
		this.securityManagementService = securityManagementService;
		this.loggerUtils = new LoggerUtils(LOGGER, thesaurus, faultMessageFactory);
	}

	public List<FaultMessage> addSecurityKeys(Device device, List<SecurityKeyInfo> securityInfoList) {
		return addSecurityKeys(device, securityInfoList, null);
	}

	public List<FaultMessage> addSecurityKeys(Device device, List<SecurityKeyInfo> securityInfoList,
			ServiceCall serviceCall) {
		List<FaultMessage> allFaults = new ArrayList<>();
		Optional.ofNullable(securityInfoList)
				.orElseGet(Collections::emptyList)
				.stream().forEach(securityInfo -> handleSecurityInfo(device, serviceCall, allFaults, securityInfo));
		return allFaults;
	}

	private void handleSecurityInfo(Device device, ServiceCall serviceCall, List<FaultMessage> allFaults, SecurityKeyInfo securityInfo) {
		try {
			loggerUtils.logInfo(serviceCall, MessageSeeds.IMPORTING_SECURITY_KEY_FOR_DEVICE, device.getName(),
					securityInfo.getSecurityAccessorName());
			List<FaultMessage> faults = addKey(device, securityInfo, serviceCall);
			if (faults != null && !faults.isEmpty()) {
				allFaults.addAll(faults);
			}
		} catch (Exception e) {
			loggerUtils.logException(device, allFaults, serviceCall, e, MessageSeeds.EXCEPTION_OCCURRED_DURING_KEY_IMPORT,
					device.getName(), securityInfo.getSecurityAccessorName());
		}
	}

	@SuppressWarnings("unchecked")
	private List<FaultMessage> addKey(Device device, SecurityKeyInfo securityInfo, ServiceCall serviceCall) {
		final List<FaultMessage> faults = new ArrayList<>();
		Optional<SecurityAccessorType> optionalSecurityAccessorType = getSecurityAccessorType(device,
				securityInfo.getSecurityAccessorName());
		SecurityAccessorType securityAccessorType;
		if (optionalSecurityAccessorType.isPresent()) {
			securityAccessorType = optionalSecurityAccessorType.get();
		} else {
			loggerUtils.logSevere(device, faults, serviceCall, MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE_ON_DEVICE_TYPE,
					device.getName(), securityInfo.getSecurityAccessorName());
			return faults;
		}

		final SecurityAccessor securityAccessor = device.getSecurityAccessor(securityAccessorType)
				.orElseGet(() -> device.newSecurityAccessor(securityAccessorType));

		if (securityInfo.getPublicKeyLabel() != null && securityInfo.getSymmetricKey() != null) {
			handleEncryptedKey(device, securityInfo, serviceCall, faults, securityAccessorType, securityAccessor);
		} else if (securityInfo.getPublicKeyLabel() == null && securityInfo.getSymmetricKey() == null) {
			handlePlaintextKey(securityAccessorType, securityAccessor, securityInfo.getSecurityAccessorKey());
		} else {
			loggerUtils.logSevere(device, faults, serviceCall, MessageSeeds.BOTH_PUBLIC_AND_SYMMETRIC_KEYS_SHOULD_BE_SPECIFIED);
		}
		return faults;
	}

	private void handlePlaintextKey(SecurityAccessorType securityAccessorType, SecurityAccessor securityAccessor, byte[] securityAccessorKey) {
		SecurityValueWrapper wrapperValue;
		if (securityAccessorType.getKeyType().getKeyAlgorithm() != null) {
			wrapperValue = createPlaintextSymmetricKeyWrapper(securityAccessorKey,
					securityAccessorType);
		} else {
			wrapperValue = createPlaintextPassphraseWrapper(securityAccessorKey,
					securityAccessorType);
		}
		securityAccessor.setActualValue(wrapperValue);
		securityAccessor.save();
	}

	private void handleEncryptedKey(Device device, SecurityKeyInfo securityInfo, ServiceCall serviceCall, List<FaultMessage> faults, SecurityAccessorType securityAccessorType, SecurityAccessor securityAccessor) {
		HsmEncryptedKey hsmEncryptedKey = null;
		try {
			hsmEncryptedKey = hsmEnergyService.importKey(createImportKeyRequest(securityInfo, securityAccessorType));
		} catch (HsmBaseException hsmEx) {
			loggerUtils.logException(device, faults, serviceCall, hsmEx, MessageSeeds.CANNOT_IMPORT_KEY_TO_HSM,
					device.getName(), securityInfo.getSecurityAccessorName());
			return;
		}
		securityAccessor.setActualValue(prepareHsmKey(securityAccessorType, hsmEncryptedKey));
		securityAccessor.save();
	}

	private HsmKey prepareHsmKey(SecurityAccessorType securityAccessorType, HsmEncryptedKey hsmEncryptedKey) {
		HsmKey hsmKey = (HsmKey) securityManagementService.newSymmetricKeyWrapper(securityAccessorType);
		hsmKey.setKey(hsmEncryptedKey.getEncryptedKey(), hsmEncryptedKey.getKeyLabel());
		return hsmKey;
	}

	private ImportKeyRequest createImportKeyRequest(SecurityKeyInfo securityInfo, SecurityAccessorType securityAccessorType) {
		return new ImportKeyRequest(securityInfo.getPublicKeyLabel(),
				getAsymmetricAlgorithm(), securityInfo.getSymmetricKey(), getSymmetricAlgorithm(), getCipher(securityInfo.getSecurityAccessorKey()),
				getInitializationVector(securityInfo.getSecurityAccessorKey()), securityAccessorType.getHsmKeyType());
	}

	private Optional<SecurityAccessorType> getSecurityAccessorType(Device device, String securityAccessorName) {
		return device.getDeviceType().getSecurityAccessorTypes().stream()
				.filter(kat -> kat.getName().equals(securityAccessorName)).findAny();
	}

	private PlaintextSymmetricKey createPlaintextSymmetricKeyWrapper(byte[] bytes,
			SecurityAccessorType securityAccessorType) {
		PlaintextSymmetricKey instance = (PlaintextSymmetricKey) securityManagementService
				.newSymmetricKeyWrapper(securityAccessorType);
		SecretKeySpec secretKeySpec = new SecretKeySpec(bytes, securityAccessorType.getKeyType().getKeyAlgorithm());
		instance.setKey(secretKeySpec);
		return instance;
	}

	private PlaintextPassphrase createPlaintextPassphraseWrapper(byte[] bytes,
			SecurityAccessorType securityAccessorType) {
		PlaintextPassphrase instance = (PlaintextPassphrase) securityManagementService
				.newPassphraseWrapper(securityAccessorType);
		instance.setPassphrase(new String(bytes));
		return instance;
	}

	private AsymmetricAlgorithm getAsymmetricAlgorithm() {
		return AsymmetricAlgorithm.RSA_15;
	}

	private SymmetricAlgorithm getSymmetricAlgorithm() {
		return SymmetricAlgorithm.AES_256_CBC;
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

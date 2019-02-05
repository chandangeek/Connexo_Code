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
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigFaultMessageFactory;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.SecurityAccessor;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;

import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SecurityHelper {

	private static final Logger LOGGER = Logger.getLogger(SecurityHelper.class.getName());

	private final HsmEnergyService hsmEnergyService;

	private final SecurityManagementService securityManagementService;

	private final MeterConfigFaultMessageFactory faultMessageFactory;

	private final Thesaurus thesaurus;

	@Inject
	public SecurityHelper(HsmEnergyService hsmEnergyService, SecurityManagementService securityManagementService,
			MeterConfigFaultMessageFactory faultMessageFactory, Thesaurus thesaurus) {
		this.hsmEnergyService = hsmEnergyService;
		this.securityManagementService = securityManagementService;
		this.faultMessageFactory = faultMessageFactory;
		this.thesaurus = thesaurus;
	}

	public List<FaultMessage> addSecurityKeys(Device device, List<SecurityKeyInfo> securityInfoList) {
		return addSecurityKeys(device, securityInfoList, null);
	}

	public List<FaultMessage> addSecurityKeys(Device device, List<SecurityKeyInfo> securityInfoList,
			ServiceCall serviceCall) {
		List<FaultMessage> allFaults = new ArrayList<>();
		if (securityInfoList != null && !securityInfoList.isEmpty()) {
			for (SecurityKeyInfo securityInfo : securityInfoList) {
				try {
					logInfo(serviceCall, MessageSeeds.IMPORTING_SECURITY_KEY_FOR_DEVICE, device.getName(),
							securityInfo.getSecurityAccessorName());
					List<FaultMessage> faults = addKey(device, securityInfo, serviceCall);
					if (faults != null && !faults.isEmpty()) {
						allFaults.addAll(faults);
					}
				} catch (Exception e) {
					logException(device, allFaults, serviceCall, e, MessageSeeds.EXCEPTION_OCCURRED_DURING_KEY_IMPORT,
							device.getName(), securityInfo.getSecurityAccessorName());
				}
			}
		}
		return allFaults;
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
			logSevere(device, faults, serviceCall, MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE_ON_DEVICE_TYPE,
					device.getName(), securityInfo.getSecurityAccessorName());
			return faults;
		}
		@SuppressWarnings("rawtypes")
		Optional<SecurityAccessor> securityAccessorOptional = device.getSecurityAccessor(securityAccessorType);
		final SecurityAccessor securityAccessor = securityAccessorOptional
				.orElseGet(() -> device.newSecurityAccessor(securityAccessorType));
		if (securityInfo.getPublicKeyLabel() != null && securityInfo.getSymmetricKey() != null) {
			// HSM
			AsymmetricAlgorithm wrapperKeyAlgorithm = getAsymmetricAlgorithm();
			SymmetricAlgorithm symmetricAlgorithm = getSymmetricAlgorithm();
			final byte[] initializationVector = getInitializationVector(securityInfo.getSecurityAccessorKey());
			final byte[] cipher = getCipher(securityInfo.getSecurityAccessorKey());
			ImportKeyRequest importKeyRequest = new ImportKeyRequest(securityInfo.getPublicKeyLabel(),
					wrapperKeyAlgorithm, securityInfo.getSymmetricKey(), symmetricAlgorithm, cipher,
					initializationVector, securityAccessorType.getHsmKeyType());
			HsmEncryptedKey hsmEncryptedKey = null;
			try {
				hsmEncryptedKey = hsmEnergyService.importKey(importKeyRequest);
			} catch (HsmBaseException hsmEx) {
				logException(device, faults, serviceCall, hsmEx, MessageSeeds.CANNOT_IMPORT_KEY_TO_HSM,
						device.getName(), securityInfo.getSecurityAccessorName());
				return faults;
			}
			HsmKey hsmKey = (HsmKey) securityManagementService.newSymmetricKeyWrapper(securityAccessorType);
			hsmKey.setKey(hsmEncryptedKey.getEncryptedKey(), hsmEncryptedKey.getKeyLabel());
			securityAccessor.setActualValue(hsmKey);
			securityAccessor.save();
		} else if (securityInfo.getPublicKeyLabel() == null && securityInfo.getSymmetricKey() == null) {
			SecurityValueWrapper wrapperValue;
			if (securityAccessorType.getKeyType().getKeyAlgorithm() != null) {
				wrapperValue = createPlaintextSymmetricKeyWrapper(securityInfo.getSecurityAccessorKey(),
						securityAccessorType);
			} else {
				wrapperValue = createPlaintextPassphraseWrapper(securityInfo.getSecurityAccessorKey(),
						securityAccessorType);
			}
			securityAccessor.setActualValue(wrapperValue);
			securityAccessor.save();
		} else {
			logSevere(device, faults, serviceCall, MessageSeeds.BOTH_PUBLIC_AND_SYMMETRIC_KEYS_SHOULD_BE_SPECIFIED);
			return faults;
		}
		return faults;
	}

	private Optional<SecurityAccessorType> getSecurityAccessorType(Device device, String securityAccessorName) {
		return device.getDeviceType().getSecurityAccessorTypes().stream()
				.filter(kat -> kat.getName().equals(securityAccessorName)).findAny();
	}

	public PlaintextSymmetricKey createPlaintextSymmetricKeyWrapper(byte[] bytes,
			SecurityAccessorType securityAccessorType) {
		PlaintextSymmetricKey instance = (PlaintextSymmetricKey) securityManagementService
				.newSymmetricKeyWrapper(securityAccessorType);
		SecretKeySpec secretKeySpec = new SecretKeySpec(bytes, securityAccessorType.getKeyType().getKeyAlgorithm());
		instance.setKey(secretKeySpec);
		return instance;
	}

	public PlaintextPassphrase createPlaintextPassphraseWrapper(byte[] bytes,
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

	private void logInfo(ServiceCall serviceCall, MessageSeeds messageSeeds, Object... args) {
		if (serviceCall != null) {
			serviceCall.log(LogLevel.INFO, messageSeeds.translate(thesaurus, args));
		} else {
			LOGGER.log(Level.INFO, messageSeeds.translate(thesaurus, args));
		}
	}

	private void logSevere(Device device, List<FaultMessage> faults, ServiceCall serviceCall, MessageSeeds messageSeeds,
			Object... args) {
		if (serviceCall != null) {
			serviceCall.log(LogLevel.SEVERE, messageSeeds.translate(thesaurus, args));
		} else {
			LOGGER.log(Level.SEVERE, messageSeeds.translate(thesaurus, args));
		}
		faults.add(faultMessageFactory.meterConfigFaultMessageSupplier(device.getName(), messageSeeds, args).get());
	}

	private void logException(Device device, List<FaultMessage> faults, ServiceCall serviceCall, Exception ex,
			MessageSeeds messageSeeds, Object... args) {
		if (serviceCall != null) {
			serviceCall.log(messageSeeds.translate(thesaurus, args), ex);
		} else {
			LOGGER.log(Level.SEVERE, messageSeeds.translate(thesaurus, args), ex);
		}
		faults.add(faultMessageFactory.meterConfigFaultMessageSupplier(device.getName(), messageSeeds, args).get());
	}
}

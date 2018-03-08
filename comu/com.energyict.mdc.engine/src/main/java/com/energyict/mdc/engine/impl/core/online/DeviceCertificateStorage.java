package com.energyict.mdc.engine.impl.core.online;

import com.elster.jupiter.pki.CertificateType;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.energyict.mdc.device.data.Device;

import com.google.common.io.BaseEncoding;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class DeviceCertificateStorage {

    private final SecurityManagementService securityManagementService;

    public DeviceCertificateStorage(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    void updateDeviceCSR(Device device, String certificateTypeName, String csr) {

        CertificateType certificateType = getCertificateType(certificateTypeName);
        String alias = certificateType.getPrefix() + device.getSerialNumber();

        getKeyType(certificateType).ifPresent(keyType -> {
                    ClientCertificateWrapper certificateWrapper = securityManagementService.findClientCertificateWrapper(alias)
                            .orElseGet(() -> securityManagementService.newClientCertificateWrapper(keyType, "DataVault").alias(alias).add());
                    try {
                        PKCS10CertificationRequest certificationRequest = new PKCS10CertificationRequest(BaseEncoding.base16().decode(csr));
                        certificateWrapper.setCSR(certificationRequest, keyType.getKeyUsages(), keyType.getExtendedKeyUsages());
                        certificateWrapper.save();
                    } catch (IOException e) {
                        //do nothing
                    }
                }
        );

    }

    private CertificateType getCertificateType(String certificateType) {
        return Arrays.stream(CertificateType.values()).filter(ct -> ct.getName().equals(certificateType)).findFirst().orElse(CertificateType.OTHER);
    }

    private Optional<KeyType> getKeyType(CertificateType certificateType) {
        return securityManagementService.getKeyTypes().stream().filter(keyType -> certificateType.isApplicableTo(keyType)).findFirst();
    }
}

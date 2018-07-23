/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.online;

import com.elster.jupiter.pki.CertificateType;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.data.Device;

import com.google.common.io.BaseEncoding;

import java.util.Arrays;
import java.util.Comparator;
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
                    ClientCertificateWrapper certificateWrapper = securityManagementService.newClientCertificateWrapper(keyType, "DataVault")
                            .alias(findAliasAndIncrement(alias))
                            .add();
                    certificateWrapper.setCSR(BaseEncoding.base16().decode(csr), keyType.getKeyUsages(), keyType.getExtendedKeyUsages());
                    certificateWrapper.save();
                }
        );
    }

    private Integer getSequentialNumber(CertificateWrapper certificateWrapper, String alias) {
        try {
            return Integer.valueOf(certificateWrapper.getAlias().replace("-" + alias, ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String findAliasAndIncrement(String alias) {
        return securityManagementService
                .findCertificateWrappers(Where.where("alias").like("*-" + alias))
                .stream()
                .filter(cw -> cw instanceof ClientCertificateWrapper)
                .map(ClientCertificateWrapper.class::cast)
                .max(Comparator.comparing(cw -> getSequentialNumber(cw, alias)))
                .map(cw -> String.valueOf(getSequentialNumber(cw, alias) + 1) + "-" + alias)
                .orElse("1-" + alias);

    }

    private CertificateType getCertificateType(String certificateType) {
        return Arrays.stream(CertificateType.values()).filter(ct -> ct.getName().equals(certificateType)).findFirst().orElse(CertificateType.OTHER);
    }

    private Optional<KeyType> getKeyType(CertificateType certificateType) {
        return securityManagementService.getKeyTypes().stream().filter(keyType -> certificateType.isApplicableTo(keyType)).findFirst();
    }
}

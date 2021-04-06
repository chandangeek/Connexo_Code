package com.energyict.mdc.device.data.impl.pki;

import com.elster.jupiter.pki.CertificateUsagesFinder;
import com.elster.jupiter.pki.CertificateWrapper;
import com.energyict.mdc.device.data.DeviceService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MdcCertificateUsagesFinder implements CertificateUsagesFinder {
    private final DeviceService deviceService;

    public MdcCertificateUsagesFinder(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public List<String> findAssociatedDevicesNames(CertificateWrapper certificateWrapper) {
        if (certificateWrapper == null) {
            return Collections.emptyList();
        }
        return deviceService.getAssociatedKeyAccessors(certificateWrapper).stream()
                .map(securityAccessor -> securityAccessor.getDevice().getName())
                .collect(Collectors.toList());
    }
}

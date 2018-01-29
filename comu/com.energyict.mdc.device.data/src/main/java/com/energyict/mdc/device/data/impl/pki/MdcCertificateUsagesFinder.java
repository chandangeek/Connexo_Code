package com.energyict.mdc.device.data.impl.pki;

import com.elster.jupiter.pki.CertificateUsagesFinder;
import com.elster.jupiter.pki.CertificateWrapper;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component(name = "MdcCertificateUsagesFinder", service = CertificateUsagesFinder.class, immediate = true)
public class MdcCertificateUsagesFinder implements CertificateUsagesFinder {
    private DeviceService deviceService;

    @Reference
    public void setDeviceDataModelService(DeviceDataModelService deviceDataModelService) {
        this.deviceService = deviceDataModelService.deviceService();
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

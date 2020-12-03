/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.importers.impl.pki;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.pki.AssociatedDeviceType;
import com.elster.jupiter.pki.CertificateUsagesFinder;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityManagementService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImporterFactory;
import com.energyict.mdc.device.data.importers.impl.certificatesimport.DeviceCertificatesImporterFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.pki.AssociatedDeviceType.BEACON;
import static com.elster.jupiter.pki.AssociatedDeviceType.BOTH;
import static com.elster.jupiter.pki.AssociatedDeviceType.METER;
import static com.energyict.mdc.device.data.importers.impl.TranslationKeys.DEVICE_CERTIFICATES_IMPORTER_SECURITY_ACCESSOR_MAPPING;

@Component(name = "CertificateUsagesFinder", service = CertificateUsagesFinder.class, immediate = true)
public class CertificateUsagesFinderImpl implements CertificateUsagesFinder {

    private volatile DeviceService deviceService;
    private volatile FileImportService fileImportService;
    private volatile SecurityManagementService securityManagementService;

    public CertificateUsagesFinderImpl() {
    }

    @Inject
    public CertificateUsagesFinderImpl(DeviceService deviceService, FileImportService fileImportService, SecurityManagementService securityManagementService) {
        setDeviceService(deviceService);
        setFileImportService(fileImportService);
        setSecurityManagementService(securityManagementService);
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public final void setFileImportService(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @Reference
    public final void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
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

    @Override
    public AssociatedDeviceType getAssociatedDeviceType(CertificateWrapper certificateWrapper) {
        AssociatedDeviceType associatedDeviceType = AssociatedDeviceType.NOT_ASSOCIATED;
        List<Device> devices = deviceService.getAssociatedKeyAccessors(certificateWrapper).stream()
                .map(securityAccessor -> securityAccessor.getDevice()).collect(Collectors.toList());
        for (Device device : devices) {
            if (device.getDeviceType().canActAsGateway() && device.getDeviceConfiguration().canActAsGateway()) {
                if (associatedDeviceType == AssociatedDeviceType.NOT_ASSOCIATED) {
                    associatedDeviceType = BEACON;
                } else if (associatedDeviceType != BEACON) {
                    return BOTH;
                }
            } else {
                if (associatedDeviceType == AssociatedDeviceType.NOT_ASSOCIATED) {
                    associatedDeviceType = METER;
                } else if (associatedDeviceType != METER) {
                    return BOTH;
                }
            }
        }
        return associatedDeviceType;
    }

    @Override
    public boolean isCertificateRelatedToType(CertificateWrapper certificateWrapper, String prefix) {
        List<SecurityAccessor> securityAccessors = deviceService.getAssociatedKeyAccessors(certificateWrapper);
        List<JSONObject> mappings = getSecurityAccessorsJSONMappings();
        if (!mappings.isEmpty()) {
            for (JSONObject mapping : mappings) {
                List<String> possibleNames = getPossibleNames(mapping, prefix);
                for (SecurityAccessor securityAccessor : securityAccessors) {
                    if (possibleNames.contains(securityAccessor.getName())) {
                        return true;
                    }
                }
            }
        } else {
            for (SecurityAccessor securityAccessor : securityAccessors) {
                if (securityAccessor.getName().startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> getPossibleNames(JSONObject mapping, String prefix) {
        Iterator keys = mapping.keys();
        while (keys.hasNext()) {
            String certFileNamePrefix = (String) keys.next();
            if (prefix.equalsIgnoreCase(certFileNamePrefix)) {
                try {
                    Object mappingSetting = mapping.get(certFileNamePrefix);
                    if (mappingSetting instanceof Boolean) {
                        return Collections.emptyList();
                    }
                    if (mappingSetting instanceof JSONArray) {
                        List<String> possibleNames = new ArrayList<>();
                        JSONArray possibleAccessorNames = (JSONArray) mappingSetting;
                        for (int i = 0; i < possibleAccessorNames.length(); i++) {
                            possibleNames.add(possibleAccessorNames.getString(i));
                        }
                        return possibleNames;
                    }
                    if (mappingSetting instanceof String) {
                        return Collections.singletonList((String) mappingSetting);
                    }
                } catch (JSONException e) {
                    return Collections.emptyList();
                }
            }
        }
        return Collections.emptyList();
    }

    private List<JSONObject> getSecurityAccessorsJSONMappings() {
        List<JSONObject> mappings = new ArrayList<>();
        List<ImportSchedule> importers = fileImportService.getImportSchedules().stream().filter(schedule -> schedule.getImporterName().equals(DeviceCertificatesImporterFactory.NAME)).collect(Collectors.toList());
        for (ImportSchedule importer : importers) {
            String property = (String) importer.getProperties().get(AbstractDeviceDataFileImporterFactory.IMPORTER_FACTORY_PROPERTY_PREFIX + "." + DEVICE_CERTIFICATES_IMPORTER_SECURITY_ACCESSOR_MAPPING.getKey());
            if (property != null && !property.isEmpty()) {
                try {
                    mappings.add(new JSONObject(property));
                } catch (JSONException ex) {
                    // do nothing
                }
            }
        }
        return mappings;
    }
}

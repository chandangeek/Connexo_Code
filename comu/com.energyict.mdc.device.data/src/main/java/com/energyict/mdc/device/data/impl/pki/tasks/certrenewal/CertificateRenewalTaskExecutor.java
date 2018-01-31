/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.certrenewal;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceLifeCycleStatus;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.device.data.CertificateAccessor;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;

import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CertificateRenewalTaskExecutor implements TaskExecutor {

    private volatile DeviceDataModelService deviceDataModelService;
    private volatile BpmService bpmService;
    private volatile Clock clock;

    private Long certRenewalBpmProcessDefinitionId;
    private Integer certRenewalExpitationDays;
    private Integer certRenewalBpmProcessCount;
    private final Logger logger;

    CertificateRenewalTaskExecutor(DeviceDataModelService deviceDataModelService,
                                   BpmService bpmService,
                                   Clock clock,
                                   Long certRenewalBpmProcessDefinitionId,
                                   Integer certRenewalExpitationDays
    ) {
        this.deviceDataModelService = deviceDataModelService;
        this.bpmService = bpmService;
        this.clock = clock;
        this.certRenewalBpmProcessDefinitionId = certRenewalBpmProcessDefinitionId;
        this.certRenewalExpitationDays = certRenewalExpitationDays;
        certRenewalBpmProcessCount = 0;
        logger = Logger.getAnonymousLogger();
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        logger.addHandler(occurrence.createTaskLogHandler().asHandler());

        Condition operationalDeviceContition = Where.where("deviceReference.meter.status.interval").isEffective()
                .and(Where.where("deviceReference.meter.status.state.stage.name").isEqualTo(EndDeviceStage.OPERATIONAL.getKey()));
        Instant expiration = clock.instant().plus(Duration.ofDays(certRenewalExpitationDays));
        Condition expiredCertificateCondition = Where.where("actualCertificate.expirationTime").isLessThanOrEqual(expiration);
        Condition condition = operationalDeviceContition.and(expiredCertificateCondition);

        List<SecurityAccessor> securityAccessors = deviceDataModelService.dataModel()
                .query(SecurityAccessor.class, Device.class, EndDevice.class, EndDeviceLifeCycleStatus.class, State.class, Stage.class, CertificateWrapper.class)
                .select(condition);
        logger.log(Level.INFO, "Number of security accessors to process:  " + securityAccessors.size());

        List<SecurityAccessor> resultList = securityAccessors
                .stream()
                .filter(this::checkSecuritySets)
                .filter(securityAccessor -> securityAccessor.isEditable())
                .collect(Collectors.toList());

        resultList.forEach(securityAccessor -> triggerBpmProcess(securityAccessor, logger));
    }

    @Override
    public void postExecute(TaskOccurrence occurrence) {
        logger.log(Level.INFO, "Number of device certificate renewal processes triggered  " + certRenewalBpmProcessCount);
    }


    private boolean checkSecuritySets(SecurityAccessor securityAccessor) {
        long id = securityAccessor.getKeyAccessorType().getId();
        boolean result;
        logger.log(Level.INFO, "Checking " + securityAccessor.getKeyAccessorType().getName() + " for " +
                securityAccessor.getDevice().getName());
        List<ConfigurationSecurityProperty> configurationSecurityProperties = new ArrayList<>();
        securityAccessor.getDevice().getDeviceConfiguration().getSecurityPropertySets().forEach(
                securityPropertySet -> {
                    List<ConfigurationSecurityProperty> properties = securityPropertySet.getConfigurationSecurityProperties();
                    if (!properties.isEmpty()) {
                        configurationSecurityProperties.addAll(properties);
                    }
                }
        );
        result = configurationSecurityProperties.stream().anyMatch(property -> property.getSecurityAccessorType().getId() == id);
        logger.log(Level.INFO, result ? "Used by security set" : "Not used by security set");
        return result;
    }

    private Optional<X509Certificate> getDeviceCertificate(SecurityAccessor securityAccessor) {
        X509Certificate x509Certificate = null;
        if (securityAccessor instanceof CertificateAccessor) {
            if (securityAccessor.getActualValue().isPresent() && securityAccessor.getActualValue().get() instanceof CertificateWrapper) {
                CertificateWrapper certificateWrapper = (CertificateWrapper) securityAccessor.getActualValue().get();
                if (certificateWrapper.getCertificate().isPresent()) {
                    x509Certificate = certificateWrapper.getCertificate().get();
                }
            }
        }
        return Optional.ofNullable(x509Certificate);
    }

    private List<BpmProcessDefinition> getActiveCertRenewalProcesses() {
        return bpmService.getAllBpmProcessDefinitions()
                .stream()
                .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus()) && p.getId() == certRenewalBpmProcessDefinitionId)
                .collect(Collectors.toList());
    }

    private boolean checkCertRenewalProcess(List<BpmProcessDefinition> processList, SecurityAccessor securityAccessor) {
        return processList.stream().anyMatch(bpmProcessDefinition -> {
            Map<String, Object> m = bpmProcessDefinition.getProperties();
            if (m.containsKey("SecurityAccessor")) {
                SecurityAccessor s = (SecurityAccessor) m.get("SecurityAccessor");
                return s.getDevice().getId() == securityAccessor.getDevice().getId() &&
                        getDeviceCertificate(s).isPresent() && getDeviceCertificate(securityAccessor).isPresent() &&
                        getDeviceCertificate(s).get().getSerialNumber().compareTo(
                                getDeviceCertificate(securityAccessor).get().getSerialNumber()) == 0;
            }
            return false;
        });
    }


    private void triggerBpmProcess(SecurityAccessor securityAccessor, Logger logger) {
        Map<String, Object> expectedParams = new HashMap<>();
        expectedParams.put("SecurityAccessor", securityAccessor);

        Optional<BpmProcessDefinition> definition = bpmService.findBpmProcessDefinition(certRenewalBpmProcessDefinitionId);
        if (definition.isPresent()) {
            List<BpmProcessDefinition> activeProcesses = getActiveCertRenewalProcesses();
            if (activeProcesses.isEmpty() || !checkCertRenewalProcess(activeProcesses, securityAccessor)) {
                bpmService.startProcess(definition.get(), expectedParams);
                logger.log(Level.INFO, "Device certificate renewal process has been triggered on device " +
                        securityAccessor.getDevice().getName() + " for " + securityAccessor.getKeyAccessorType().getName());
                certRenewalBpmProcessCount++;
                logger.log(Level.INFO, "Number of device certificate renewal processes triggered  " + certRenewalBpmProcessCount);
            }
        }
    }

}

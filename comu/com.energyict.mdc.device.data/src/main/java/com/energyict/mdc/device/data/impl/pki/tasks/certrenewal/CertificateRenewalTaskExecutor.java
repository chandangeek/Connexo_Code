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
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;

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

    private String certRenewalBpmProcessDefinitionId;
    private Integer certRenewalExpitationDays;
    private Integer certRenewalBpmProcessCount;
    private final Logger logger;

    private static final String CERTIFICATE_RENEWAL_PROCESS_NAME = "Certificate renewal";

    CertificateRenewalTaskExecutor(DeviceDataModelService deviceDataModelService,
                                   BpmService bpmService,
                                   Clock clock,
                                   String certRenewalBpmProcessDefinitionId,
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

    private boolean getActiveCertRenewalProcesses(SecurityAccessor securityAccessor) {
        boolean found = bpmService.getRunningProcesses(null, "?variableid=deviceId&variablevalue=" + securityAccessor.getDevice().getmRID())
                .processes
                .stream()
                .anyMatch(processInstanceInfo -> processInstanceInfo.name.equalsIgnoreCase(CERTIFICATE_RENEWAL_PROCESS_NAME));
        if (found) {
            logger.log(Level.INFO, " Active certificate renewal process found for " + securityAccessor.getDevice().getName() +
                    " mrid = " + securityAccessor.getDevice().getmRID());
        }
       return found;
    }

    private void triggerBpmProcess(SecurityAccessor securityAccessor, Logger logger) {
        Map<String, Object> expectedParams = new HashMap<>();
        expectedParams.put("deviceId", securityAccessor.getDevice().getmRID());
        expectedParams.put("accessorType", securityAccessor.getKeyAccessorType().getName());

        Optional<BpmProcessDefinition> definition = bpmService.getAllBpmProcessDefinitions()
                .stream()
                .filter(p -> p.getProcessName().equalsIgnoreCase(CERTIFICATE_RENEWAL_PROCESS_NAME))
                .findAny();

        if (definition.isPresent()) {
            logger.log(Level.INFO, "Process definition found ");
            if (!getActiveCertRenewalProcesses(securityAccessor)) {
                bpmService.startProcess(definition.get(), expectedParams);
                logger.log(Level.INFO, "Device certificate renewal process has been triggered on device " +
                        securityAccessor.getDevice().getName() + " mrid = " + securityAccessor.getDevice().getmRID() +
                        " for " + securityAccessor.getKeyAccessorType().getName());
                certRenewalBpmProcessCount++;
                logger.log(Level.INFO, "Number of device certificate renewal processes triggered  " + certRenewalBpmProcessCount);
            }
        }
    }

}

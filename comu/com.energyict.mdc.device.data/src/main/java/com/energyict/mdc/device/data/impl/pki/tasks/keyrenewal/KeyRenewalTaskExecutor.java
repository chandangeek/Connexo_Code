/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.keyrenewal;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceLifeCycleStatus;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.SymmetricKeyAccessor;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class KeyRenewalTaskExecutor implements TaskExecutor {

    private volatile DeviceDataModelService deviceDataModelService;
    private volatile BpmService bpmService;
    private volatile Clock clock;

    private String keyRenewalBpmProcessDefinitionId;
    private Integer keyRenewalExpitationDays;
    private Integer keyRenewalBpmProcessCount;
    private final Logger logger;

    private static final String KEY_RENEWAL_PROCESS_NAME = "Key renewal";

    KeyRenewalTaskExecutor(DeviceDataModelService deviceDataModelService,
                           BpmService bpmService,
                           Clock clock,
                           String keyRenewalBpmProcessDefinitionId,
                           Integer keyRenewalExpitationDays
    ) {
        this.deviceDataModelService = deviceDataModelService;
        this.bpmService = bpmService;
        this.clock = clock;
        this.keyRenewalBpmProcessDefinitionId = keyRenewalBpmProcessDefinitionId;
        this.keyRenewalExpitationDays = keyRenewalExpitationDays;
        keyRenewalBpmProcessCount = 0;
        logger = Logger.getAnonymousLogger();
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        logger.addHandler(occurrence.createTaskLogHandler().asHandler());

        Condition operationalDeviceContition = Where.where("deviceReference.meter.status.interval").isEffective()
                .and(Where.where("deviceReference.meter.status.state.stage.name").isEqualTo(EndDeviceStage.OPERATIONAL.getKey()));

        List<SecurityAccessor> securityAccessors = deviceDataModelService.dataModel()
                .query(SecurityAccessor.class, Device.class, EndDevice.class, EndDeviceLifeCycleStatus.class, State.class, Stage.class)
                .select(operationalDeviceContition);
        logger.log(Level.INFO, "Number of security accessors to process:  " + securityAccessors.size());

        List<SecurityAccessor> resultList = securityAccessors
                .stream()
                .filter(securityAccessor -> securityAccessor instanceof SymmetricKeyAccessor)
                .filter(securityAccessor -> {
                    Optional<SecretKey> secretKey =
                            getDeviceKey((SymmetricKeyAccessor) securityAccessor);
                    return secretKey.isPresent() && deviceKeyExpired((SymmetricKeyAccessor) securityAccessor);
                })
                .filter(this::checkSecuritySets)
                .filter(securityAccessor -> securityAccessor.isEditable())
                .collect(Collectors.toList());

        resultList.forEach(securityAccessor -> triggerBpmProcess(securityAccessor, logger));
    }

    @Override
    public void postExecute(TaskOccurrence occurrence) {
        logger.log(Level.INFO, "Number of device key renewal processes triggered  " + keyRenewalBpmProcessCount);
    }

    private Optional<SecretKey> getDeviceKey(SymmetricKeyAccessor symmetricKeyAccessor) {
        SecretKey secretKey = null;
        Optional<SymmetricKeyWrapper> symmetricKeyWrapper = symmetricKeyAccessor.getActualValue();
        if (symmetricKeyWrapper.isPresent() && symmetricKeyWrapper.get() instanceof PlaintextSymmetricKey) {
            PlaintextSymmetricKey plaintextSymmetricKey = (PlaintextSymmetricKey) symmetricKeyWrapper.get();
            if (plaintextSymmetricKey.getKey().isPresent()) {
                secretKey = plaintextSymmetricKey.getKey().get();
            }
        }
        return Optional.ofNullable(secretKey);
    }

    private boolean deviceKeyExpired(SymmetricKeyAccessor symmetricKeyAccessor) {
        Optional<SymmetricKeyWrapper> symmetricKeyWrapper = symmetricKeyAccessor.getActualValue();
        if (symmetricKeyWrapper.isPresent() && symmetricKeyWrapper.get() instanceof PlaintextSymmetricKey) {
            Optional<Instant> expirationTime = symmetricKeyWrapper.get().getExpirationTime();
            if (expirationTime.isPresent()) {
                long daysBetween = Math.abs(ChronoUnit.DAYS.between(Instant.now(), expirationTime.get()));
                return daysBetween <= keyRenewalExpitationDays;
            }
        }
        return false;
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

    private boolean getActiveKeyRenewalProcesses(SecurityAccessor securityAccessor) {
        boolean found = bpmService.getRunningProcesses(null, "?variableid=deviceId&variablevalue=" + securityAccessor.getDevice().getmRID())
                .processes
                .stream()
                .anyMatch(processInstanceInfo -> processInstanceInfo.name.equalsIgnoreCase(KEY_RENEWAL_PROCESS_NAME));
        if (found) {
            logger.log(Level.INFO, " Active key renewal process found for " + securityAccessor.getDevice().getName() +
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
                .filter(p -> p.getProcessName().equalsIgnoreCase(KEY_RENEWAL_PROCESS_NAME))
                .findAny();

        if (definition.isPresent()) {
            logger.log(Level.INFO, "Process definition found ");
            if (!getActiveKeyRenewalProcesses(securityAccessor)) {
                bpmService.startProcess(definition.get(), expectedParams);
                logger.log(Level.INFO, "Device key renewal process has been triggered on device " + securityAccessor.getDevice().getName()
                        + " mrid = " + securityAccessor.getDevice().getmRID() + " for " + securityAccessor.getKeyAccessorType().getName());
                keyRenewalBpmProcessCount++;
                logger.log(Level.INFO, "Number of device key renewal processes triggered  " + keyRenewalBpmProcessCount);
            }
        }
    }
}

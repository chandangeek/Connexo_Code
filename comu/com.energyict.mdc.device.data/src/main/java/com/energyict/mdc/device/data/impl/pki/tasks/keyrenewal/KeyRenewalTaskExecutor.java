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

    private Long keyRenewalBpmProcessDefinitionId;
    private Integer keyRenewalExpitationDays;
    private Integer keyRenewalBpmProcessCount;
    private final Logger logger;

    KeyRenewalTaskExecutor(DeviceDataModelService deviceDataModelService,
                           BpmService bpmService,
                           Clock clock,
                           Long keyRenewalBpmProcessDefinitionId,
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

        List<SecurityAccessor> resultList = securityAccessors
                .stream()
                .filter(securityAccessor -> securityAccessor instanceof SymmetricKeyAccessor)
                .filter(securityAccessor -> {
                    Optional<SecretKey> secretKey =
                            getDeviceKey((SymmetricKeyAccessor) securityAccessor);
                    return secretKey.isPresent() && deviceKeyExpired((SymmetricKeyAccessor) securityAccessor);
                })
                .filter(this::checkSecuritySets)
                /*.filter(securityAccessor -> !securityAccessor.isEditable())*/
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
        List<ConfigurationSecurityProperty> configurationSecurityProperties = new ArrayList<>();
        securityAccessor.getDevice().getDeviceConfiguration().getSecurityPropertySets().forEach(
                securityPropertySet -> {
                    List<ConfigurationSecurityProperty> properties = securityPropertySet.getConfigurationSecurityProperties();
                    if (!properties.isEmpty()) {
                        configurationSecurityProperties.addAll(properties);
                    }
                }
        );
        return configurationSecurityProperties.stream().anyMatch(property -> property.getSecurityAccessorType().getId() == id);
    }

    private void triggerBpmProcess(SecurityAccessor securityAccessor, Logger logger) {
        Map<String, Object> expectedParams = new HashMap<>();
        expectedParams.put("SecurityAccessor", securityAccessor);
        Optional<BpmProcessDefinition> bpmProcessDefinition = bpmService.findBpmProcessDefinition(keyRenewalBpmProcessDefinitionId);
        if (bpmProcessDefinition.isPresent()) {
            bpmService.startProcess(bpmProcessDefinition.get(), expectedParams);
            logger.log(Level.INFO, "Device key renewal process has been triggered on device " + securityAccessor.getDevice().getName()
                    + " for " + securityAccessor.getKeyAccessorType().getName());
            keyRenewalBpmProcessCount++;
            logger.log(Level.INFO, "Number of device key renewal processes triggered  " + keyRenewalBpmProcessCount);
        }
    }
}

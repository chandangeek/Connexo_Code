package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.*;
import com.elster.jupiter.util.exception.BaseException;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.device.lifecycle.*;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.time.Instant;
import java.util.*;
import java.util.logging.Level;

import static com.energyict.mdc.device.lifecycle.impl.DeviceLifeCyclePropertySupport.effectiveTimestamp;
import static com.energyict.mdc.device.lifecycle.impl.DeviceLifeCyclePropertySupport.getEffectiveTimestamp;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will force validation and estimation on channels and registers.
 * A runtime error is thrown when the device is not set for validation and/or estimation.
 * When after validation and estimation still invalid values are encountered, the action is undone.
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#FORCE_VALIDATION_AND_ESTIMATION}
 *
 * action bits: 4096
 *
 */
public class ForceValidationAndEstimation implements ServerMicroAction {

    //Required services
    private ThreadPrincipalService threadPrincipalService;
    private TransactionService transactionService;
    private ValidationService validationService;
    private EstimationService estimationService;

    private Device device;

    public ForceValidationAndEstimation(ThreadPrincipalService threadPrincipalService, TransactionService transactionService, ValidationService validationService, EstimationService estimationService){
        this.threadPrincipalService = threadPrincipalService;
        this.transactionService = transactionService;
        this.validationService = validationService;
        this.estimationService = estimationService;
    }

    @Override
    public List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        return Collections.singletonList(effectiveTimestamp(propertySpecService));
    }

    @Override
    public void execute(Device device, List<ExecutableActionProperty> properties) {
        this.device = device;
        if (!this.device.forValidation().isValidationActive())
            throw new ForceValidationAndEstimationException(MessageSeeds.VALIDATION_NOT_SET_ON_DEVICE);
        if (!this.device.forEstimation().isEstimationActive())
            throw new ForceValidationAndEstimationException(MessageSeeds.ESTIMATION_NOT_SET_ON_DEVICE);

        try {
            transactionService.execute(VoidTransaction.of(() -> {
                this.device.getLoadProfiles().forEach(loadProfile -> this.setLastReading(loadProfile,  getEffectiveTimestamp(properties)));
                this.device.getCurrentMeterActivation().ifPresent(this::validateAndEstimate);
            }));
        } finally {
            threadPrincipalService.clear();
        }
    }

    private void setLastReading(LoadProfile loadProfile, Instant newlastReading) {
        device.getLoadProfileUpdaterFor(loadProfile).setLastReadingIfLater(newlastReading).update();
    }

    private void validateAndEstimate(MeterActivation meterActivation){
        validationService.validate(meterActivation);
        estimationService.estimate(meterActivation, meterActivation.getRange());
        if (!validationService.getEvaluator().isAllDataValidated(meterActivation))
            throw new ForceValidationAndEstimationException(MessageSeeds.NOT_ALL_DATA_VALID_FOR_DEVICE);
    }

    public class ForceValidationAndEstimationException extends BaseException{
        protected ForceValidationAndEstimationException(MessageSeed messageSeed) {
            super(messageSeed, device.getName());
        }
    }

    private enum MessageSeeds implements MessageSeed, TranslationKey {

            VALIDATION_NOT_SET_ON_DEVICE(1," microAction.exception.validationNotSetOnDeviceX", "Validation not set on device '{0}'"),
            ESTIMATION_NOT_SET_ON_DEVICE(2," microAction.exception.estimationNotSetOnDeviceX", "Estimation not set on device '{0}'"),
            NOT_ALL_DATA_VALID_FOR_DEVICE(3, "microAction.exception.notAllDataValidForDeviceX", "Device {0} has still suspect values: Action is undone.");

            private final int number;
            private final String key;
            private final String defaultFormat;

            MessageSeeds(int number, String key, String defaultFormat) {
                this.number = number;
                this.key = key;
                this.defaultFormat = defaultFormat;
            }

            @Override
            public int getNumber() {
                return number;
            }

            @Override
            public String getKey() {
                return key;
            }

            @Override
            public String getDefaultFormat() {
                return defaultFormat;
            }

            @Override
            public Level getLevel() {
                return  Level.SEVERE;
            }

            @Override
            public String getModule() {
                return DeviceLifeCycleService.COMPONENT_NAME;
            }
        }
    }

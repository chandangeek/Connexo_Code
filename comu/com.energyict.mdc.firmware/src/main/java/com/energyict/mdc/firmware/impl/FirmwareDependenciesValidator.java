/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FirmwareDependenciesValidator implements ConstraintValidator<CorrectFirmwareDependencies, FirmwareVersion> {
    private final Thesaurus thesaurus;
    private final DataModel dataModel;

    @Inject
    public FirmwareDependenciesValidator(Thesaurus thesaurus, DataModel dataModel) {
        this.thesaurus = thesaurus;
        this.dataModel = dataModel;
    }

    @Override
    public void initialize(CorrectFirmwareDependencies annotation) {
        // not necessary
    }

    @Override
    public boolean isValid(FirmwareVersion firmwareVersion, ConstraintValidatorContext context) {
        boolean valid = true;
        Optional<FirmwareVersion> dependency = firmwareVersion.getMeterFirmwareDependency();
        if (dependency.map(FirmwareVersion::getFirmwareType).filter(type -> type != FirmwareType.METER).isPresent()) {
            logError(context, FirmwareVersionImpl.Fields.METER_FW_DEP.fieldName(), MessageSeeds.WRONG_FIRMWARE_TYPE_FOR_METER_FW_DEPENDENCY,
                    dependency.get().getFirmwareType().getTranslation(thesaurus),
                    dependency.get().getFirmwareVersion(),
                    firmwareVersion.getFirmwareVersion());
            valid = false;
        }
        if (dependency.map(FirmwareVersion::getRank).filter(rank -> rank >= firmwareVersion.getRank()).isPresent()) {
            logError(context, FirmwareVersionImpl.Fields.METER_FW_DEP.fieldName(), MessageSeeds.WRONG_RANK_FOR_METER_FW_DEPENDENCY,
                    firmwareVersion.getFirmwareVersion(),
                    dependency.get().getFirmwareVersion());
            valid = false;
        }

        dependency = firmwareVersion.getCommunicationFirmwareDependency();
        if (dependency.map(FirmwareVersion::getFirmwareType).filter(type -> type != FirmwareType.COMMUNICATION).isPresent()) {
            logError(context, FirmwareVersionImpl.Fields.COM_FW_DEP.fieldName(), MessageSeeds.WRONG_FIRMWARE_TYPE_FOR_COM_FW_DEPENDENCY,
                    dependency.get().getFirmwareType().getTranslation(thesaurus),
                    dependency.get().getFirmwareVersion(),
                    firmwareVersion.getFirmwareVersion());
            valid = false;
        }
        if (dependency.map(FirmwareVersion::getRank).filter(rank -> rank >= firmwareVersion.getRank()).isPresent()) {
            logError(context, FirmwareVersionImpl.Fields.COM_FW_DEP.fieldName(), MessageSeeds.WRONG_RANK_FOR_COM_FW_DEPENDENCY,
                    firmwareVersion.getFirmwareVersion(),
                    dependency.get().getFirmwareVersion());
            valid = false;
        }
        dependency = firmwareVersion.getAuxiliaryFirmwareDependency();
        if (dependency.map(FirmwareVersion::getFirmwareType).filter(type -> type != FirmwareType.AUXILIARY).isPresent()) {
            logError(context, FirmwareVersionImpl.Fields.AUX_FW_DEP.fieldName(), MessageSeeds.WRONG_FIRMWARE_TYPE_FOR_AUX_FW_DEPENDENCY,
                    dependency.get().getFirmwareType().getTranslation(thesaurus),
                    dependency.get().getFirmwareVersion(),
                    firmwareVersion.getFirmwareVersion());
            valid = false;
        }
        if (dependency.map(FirmwareVersion::getRank).filter(rank -> rank >= firmwareVersion.getRank()).isPresent()) {
            logError(context, FirmwareVersionImpl.Fields.AUX_FW_DEP.fieldName(), MessageSeeds.WRONG_RANK_FOR_AUX_FW_DEPENDENCY,
                    firmwareVersion.getFirmwareVersion(),
                    dependency.get().getFirmwareVersion());
            valid = false;
        }
        return valid;
    }

    void validateDependencyRanks(List<? extends FirmwareVersion> firmwareVersionsWithUpdatedRanks) {
        try (QueryStream<FirmwareVersion> dependentFirmwareVersions = dataModel.stream(FirmwareVersion.class)) {
            dependentFirmwareVersions
                    .join(FirmwareVersion.class)
                    .filter(Where.where(FirmwareVersionImpl.Fields.METER_FW_DEP.fieldName()).in(firmwareVersionsWithUpdatedRanks)
                            .or(Where.where(FirmwareVersionImpl.Fields.COM_FW_DEP.fieldName()).in(firmwareVersionsWithUpdatedRanks))
                            .or(Where.where(FirmwareVersionImpl.Fields.AUX_FW_DEP.fieldName()).in(firmwareVersionsWithUpdatedRanks)));
            Stream.concat(dependentFirmwareVersions, firmwareVersionsWithUpdatedRanks.stream())
                    .distinct()
                    .forEach(firmwareVersion -> {
                        firmwareVersion.getMeterFirmwareDependency()
                                .filter(dep -> dep.getRank() >= firmwareVersion.getRank())
                                .ifPresent(dependency -> throwError(MessageSeeds.WRONG_RANK_FOR_METER_FW_DEPENDENCY,
                                        firmwareVersion.getFirmwareVersion(),
                                        dependency.getFirmwareVersion()));
                        firmwareVersion.getCommunicationFirmwareDependency()
                                .filter(dep -> dep.getRank() >= firmwareVersion.getRank())
                                .ifPresent(dependency -> throwError(MessageSeeds.WRONG_RANK_FOR_COM_FW_DEPENDENCY,
                                        firmwareVersion.getFirmwareVersion(),
                                        dependency.getFirmwareVersion()));
                        firmwareVersion.getAuxiliaryFirmwareDependency()
                                .filter(dep -> dep.getRank() >= firmwareVersion.getRank())
                                .ifPresent(dependency -> throwError(MessageSeeds.WRONG_RANK_FOR_AUX_FW_DEPENDENCY,
                                        firmwareVersion.getFirmwareVersion(),
                                        dependency.getFirmwareVersion()));
                    });
        }
    }

    private void logError(ConstraintValidatorContext context, String property, MessageSeeds messageSeed, Object... args) {
        HibernateConstraintValidatorContext hibernateContext = context.unwrap(HibernateConstraintValidatorContext.class);
        for (int i = 0; i < args.length; ++i) {
            hibernateContext.addExpressionVariable(Integer.toString(i), args[i]);
        }
        hibernateContext.buildConstraintViolationWithTemplate(messageSeed.getKey())
                .addPropertyNode(property)
                .addConstraintViolation()
                .disableDefaultConstraintViolation();
    }

    private void throwError(MessageSeeds messageSeed, Object... args) {
        throw new LocalizedException(thesaurus, messageSeed, args) {
        };
    }
}

package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import java.util.Optional;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MustHaveUniqueEndDeviceGroupValidator implements ConstraintValidator<MustHaveUniqueEndDeviceGroup, DataCollectionKpi> {

    private final DataCollectionKpiService dataCollectionKpiService;
    private String message;

    @Inject
    public MustHaveUniqueEndDeviceGroupValidator(DataCollectionKpiService dataCollectionKpiService) {
        this.dataCollectionKpiService = dataCollectionKpiService;
    }

    @Override
    public void initialize(MustHaveUniqueEndDeviceGroup mustHaveUniqueEndDeviceGroup) {
        message = mustHaveUniqueEndDeviceGroup.message();
    }

    @Override
    public boolean isValid(DataCollectionKpi dataCollectionKpi, ConstraintValidatorContext constraintValidatorContext) {
        Optional<DataCollectionKpi> kpiOptional = dataCollectionKpiService.findDataCollectionKpi(dataCollectionKpi.getDeviceGroup());
        if (kpiOptional.isPresent() && kpiOptional.get().getId()!=dataCollectionKpi.getId()) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(message).addPropertyNode("endDeviceGroup").addConstraintViolation().disableDefaultConstraintViolation();
            return false;
        }
        return true;
    }
}
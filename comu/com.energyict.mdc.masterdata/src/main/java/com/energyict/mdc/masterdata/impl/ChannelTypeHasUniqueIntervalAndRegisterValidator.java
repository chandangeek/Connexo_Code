package com.energyict.mdc.masterdata.impl;

import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;
import java.util.Optional;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Copyrights EnergyICT
 * Date: 7/22/14
 * Time: 1:21 PM
 */
public class ChannelTypeHasUniqueIntervalAndRegisterValidator implements ConstraintValidator<ChannelTypeHasUniqueIntervalAndRegister, ChannelType> {

    private final MasterDataService masterDataService;

    @Inject
    public ChannelTypeHasUniqueIntervalAndRegisterValidator(MasterDataService masterDataService) {
        super();
        this.masterDataService = masterDataService;
    }

    @Override
    public void initialize(ChannelTypeHasUniqueIntervalAndRegister channelTypeHasUniqueIntervalAndRegister) {

    }

    @Override
    public boolean isValid(ChannelType channelType, ConstraintValidatorContext context) {
        Optional<ChannelType> xChannelType = this.masterDataService.findChannelTypeByTemplateRegisterAndInterval(channelType.getTemplateRegister(), channelType.getInterval());
        if (xChannelType.isPresent() && xChannelType.get().getId() != channelType.getId()) {
            context.
                    buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CHANNEL_TYPE_WITH_REGISTER_TYPE_AND_INTERVAL_DUPLICATE + "}").
                    addPropertyNode(MeasurementTypeImpl.Fields.INTERVAL.fieldName()).
                    addPropertyNode(MeasurementTypeImpl.Fields.TEMPLATE_REGISTER_ID.fieldName()).
                    addConstraintViolation().disableDefaultConstraintViolation();
            return false;
        }
        return true;
    }
}

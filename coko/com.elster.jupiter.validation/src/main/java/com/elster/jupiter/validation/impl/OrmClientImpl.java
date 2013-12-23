package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ReadingTypeInValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;

class OrmClientImpl implements OrmClient {

    private final DataModel dataModel;

    public OrmClientImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public DataMapper<IValidationRuleSet> getValidationRuleSetFactory() {
        return dataModel.mapper(IValidationRuleSet.class);
    }

    @Override
    public DataMapper<IValidationRule> getValidationRuleFactory() {
        return dataModel.mapper(IValidationRule.class);
    }

    @Override
    public DataMapper<ValidationRuleProperties> getValidationRulePropertiesFactory() {
        return dataModel.mapper(ValidationRuleProperties.class);
    }

    @Override
    public DataMapper<ReadingTypeInValidationRule> getReadingTypesInValidationRuleFactory() {
        return dataModel.mapper(ReadingTypeInValidationRule.class);
    }

    @Override
    public DataMapper<ChannelValidation> getChannelValidationFactory() {
        return dataModel.mapper(ChannelValidation.class);
    }

    @Override
    public DataMapper<MeterActivationValidation> getMeterActivationValidationFactory() {
        return dataModel.mapper(MeterActivationValidation.class);
    }

    @Override
    public void install(boolean executeDdl, boolean saveMappings) {
        dataModel.install(executeDdl, saveMappings);
    }

    @Override
    public DataModel getDataModel() {
        return dataModel;
    }
}

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.validation.ReadingTypeInValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;

class OrmClientImpl implements OrmClient {

    private final DataModel dataModel;

    public OrmClientImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public TypeCache<IValidationRuleSet> getValidationRuleSetFactory() {
        return Bus.getComponentCache().getTypeCache(IValidationRuleSet.class, ValidationRuleSetImpl.class, TableSpecs.VAL_VALIDATIONRULESET.name());
    }

    @Override
    public TypeCache<IValidationRule> getValidationRuleFactory() {
        return Bus.getComponentCache().getTypeCache(IValidationRule.class, ValidationRuleImpl.class, TableSpecs.VAL_VALIDATIONRULE.name());
    }

    @Override
    public TypeCache<ValidationRuleProperties> getValidationRulePropertiesFactory() {
        return Bus.getComponentCache().getTypeCache(ValidationRuleProperties.class, ValidationRulePropertiesImpl.class, TableSpecs.VAL_VALIDATIONRULEPROPS.name());
    }

    @Override
    public DataMapper<ReadingTypeInValidationRule> getReadingTypesInValidationRuleFactory() {
        return dataModel.getDataMapper(ReadingTypeInValidationRule.class, ReadingTypeInValidationRuleImpl.class, TableSpecs.VAL_READINGTYPEINVALRULE.name());
    }

    @Override
    public DataMapper<ChannelValidation> getChannelValidationFactory() {
        return dataModel.getDataMapper(ChannelValidation.class, ChannelValidationImpl.class, TableSpecs.VAL_CH_VALIDATION.name());
    }

    @Override
    public DataMapper<MeterActivationValidation> getMeterActivationValidationFactory() {
        return dataModel.getDataMapper(MeterActivationValidation.class, MeterActivationValidationImpl.class, TableSpecs.VAL_MA_VALIDATION.name());
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

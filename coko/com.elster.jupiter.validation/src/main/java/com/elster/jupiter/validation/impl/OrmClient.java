package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.validation.ReadingTypeInValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;

interface OrmClient {

    void install(boolean executeDdl , boolean storeMappings);

    DataModel getDataModel();

    TypeCache<IValidationRuleSet> getValidationRuleSetFactory();

    TypeCache<IValidationRule> getValidationRuleFactory();

    TypeCache<ValidationRuleProperties> getValidationRulePropertiesFactory();

    DataMapper<ReadingTypeInValidationRule> getReadingTypesInValidationRuleFactory();

    DataMapper<MeterActivationValidation> getMeterActivationValidationFactory();

    DataMapper<ChannelValidation> getChannelValidationFactory();
}

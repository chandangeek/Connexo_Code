package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;
import com.elster.jupiter.validation.ValidationRuleSet;

public interface OrmClient {

    void install(boolean executeDdl , boolean storeMappings);

    DataModel getDataModel();

    TypeCache<ValidationRuleSet> getValidationRuleSetFactory();

    TypeCache<ValidationRule> getValidationRuleFactory();

    TypeCache<ValidationRuleProperties> getValidationRulePropertiesFactory();
}

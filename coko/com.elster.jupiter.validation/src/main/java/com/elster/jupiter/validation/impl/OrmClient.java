package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;

public interface OrmClient {
    TypeCache<ValidationRuleSet> getValidationRuleSetFactory();

    void install(boolean executeDdl , boolean storeMappings);

    DataModel getDataModel();

    TypeCache<ValidationRule> getValidationRuleFactory();
}

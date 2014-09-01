package com.elster.jupiter.validators.impl;

import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.validation.Validator;

import java.util.List;

interface IValidator extends Validator {

    NlsKey getNlsKey();

    NlsKey getPropertyNlsKey(String property);

    String getPropertyDefaultFormat(String property);
    
    List<Pair<? extends NlsKey, String>> getExtraTranslations();
    
    List<String> getRequiredProperties();
}

package com.elster.jupiter.validators.impl;

import java.util.List;

import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.validation.Validator;

/**
 * Copyrights EnergyICT
 * Date: 8/07/2014
 * Time: 9:02
 */
public interface IValidator extends Validator {

    NlsKey getNlsKey();

    NlsKey getPropertyNlsKey(String property);

    String getPropertyDefaultFormat(String property);
    
    List<Pair<? extends NlsKey, String>> getExtraTranslations();
    
    List<String> getRequiredProperties();
}

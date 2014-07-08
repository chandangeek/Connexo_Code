package com.elster.jupiter.validators.impl;

import com.elster.jupiter.nls.NlsKey;
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
}

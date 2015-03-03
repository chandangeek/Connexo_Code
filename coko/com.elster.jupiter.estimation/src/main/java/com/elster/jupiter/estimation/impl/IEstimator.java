package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.util.Pair;

import java.util.List;

/**
 * Created by igh on 3/03/2015.
 */
public interface IEstimator extends Estimator {

    NlsKey getNlsKey();

    NlsKey getPropertyNlsKey(String property);

    String getPropertyDefaultFormat(String property);

    List<Pair<? extends NlsKey, String>> getExtraTranslations();

    List<String> getRequiredProperties();
}

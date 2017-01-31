/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;

import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides all the {@link TranslationKey}s of this bundles to the NlsService.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-07 (14:48)
 */
@Component(name = "com.elster.jupiter.estimators", service = {com.elster.jupiter.nls.TranslationKeyProvider.class})
public class TranslationKeyProvider implements com.elster.jupiter.nls.TranslationKeyProvider {

    @Override
    public String getComponentName() {
        return EstimationService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        Collections.addAll(keys, AverageWithSamplesEstimator.TranslationKeys.values());
        Collections.addAll(keys, EqualDistribution.TranslationKeys.values());
        Collections.addAll(keys, LinearInterpolation.TranslationKeys.values());
        Collections.addAll(keys, PowerGapFill.TranslationKeys.values());
        Collections.addAll(keys, ValueFillEstimator.TranslationKeys.values());
        return keys;
    }

}
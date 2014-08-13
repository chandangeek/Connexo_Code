package com.elster.jupiter.validation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.util.time.Interval;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 16/06/2014
 * Time: 16:27
 */
public interface ValidationRuleSetResolver {

    List<ValidationRuleSet> resolve(MeterActivation meterActivation);
}

package com.elster.jupiter.validation;

import com.elster.jupiter.metering.ChannelsContainer;

import java.util.List;

/**
 * A ValidationRuleSetResolver resolves which rule sets are applicable to a given meterActivation.
 * <p>
 * Copyrights EnergyICT
 * Date: 16/06/2014
 * Time: 16:27
 */
public interface ValidationRuleSetResolver {

    List<ValidationRuleSet> resolve(ChannelsContainer channelsContainer);

    boolean isValidationRuleSetInUse(ValidationRuleSet ruleset);
}

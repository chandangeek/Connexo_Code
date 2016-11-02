package com.elster.jupiter.usagepoint.lifecycle.config;

import aQute.bnd.annotation.ConsumerType;

/**
 * Models pre-transition checks for {@link UsagePointTransition}.
 */
@ConsumerType
public interface MicroCheck {

    String getKey();

    String getName();

    String getDescription();

    String getCategory();

    String getCategoryName();
}

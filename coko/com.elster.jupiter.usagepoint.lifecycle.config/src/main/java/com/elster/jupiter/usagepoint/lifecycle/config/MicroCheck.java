package com.elster.jupiter.usagepoint.lifecycle.config;

import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ConsumerType;

/**
 * Models pre-transition checks for {@link UsagePointTransition}.
 */
@ConsumerType
public interface MicroCheck extends HasName {

    String getKey();

    String getDescription();

    String getCategory();

    String getCategoryName();
}

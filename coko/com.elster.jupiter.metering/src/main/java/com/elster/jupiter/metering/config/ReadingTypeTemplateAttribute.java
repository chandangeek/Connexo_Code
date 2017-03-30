/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.ReadingType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface ReadingTypeTemplateAttribute {

    /**
     * @return the unique attribute name
     */
    ReadingTypeTemplateAttributeName getName();

    /**
     * @return reading type attribute's code
     */
    Optional<Integer> getCode();

    /**
     * @return a set of allowed values for that reading type attribute.
     */
    List<Integer> getPossibleValues();

    /**
     * Checks that the specific reading type's attribute (see {@link #getName()}) is within limits
     * (equal to {@link #getCode()} or has one of the {@link #getPossibleValues()} values).
     *
     * @param candidate reading type for check
     * @return <code>true</code> if candidate has correct value in that attribute
     */
    boolean matches(ReadingType candidate);
}

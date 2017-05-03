/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface PartiallySpecifiedReadingTypeRequirement extends ReadingTypeRequirement {

    /**
     * Overrides a specific reading type attribute's value.
     *
     * @param name attribute unique id
     * @param code value for the attribute
     * @return that object
     */
    PartiallySpecifiedReadingTypeRequirement overrideAttribute(ReadingTypeTemplateAttributeName name, int code);

    /**
     * Resets a specific reading type attribute's value back to the value from template.
     *
     * @param name attribute unique id
     * @return that object
     */
    PartiallySpecifiedReadingTypeRequirement removeOverriddenAttribute(ReadingTypeTemplateAttributeName name);

    /**
     * @return A source template
     */
    ReadingTypeTemplate getReadingTypeTemplate();

    MacroPeriod getMacroPeriod();

    TimeAttribute getMeasuringPeriod();

    /**
     * @return A string value of attribute
     * Returns empty if less than one value present in the template.
     */
    Optional<String> getAttributeValue(ReadingTypeTemplateAttributeName attributeName);

    /**
     * @return A list of attribute values
     * Returns a list with single value if values overridden by specific value.
     */
    List<Optional<String>>  getAttributeValues(ReadingTypeTemplateAttributeName attributeName);
}

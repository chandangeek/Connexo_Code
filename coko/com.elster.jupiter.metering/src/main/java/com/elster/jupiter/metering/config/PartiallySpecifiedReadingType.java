package com.elster.jupiter.metering.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.ReadingTypeTemplate;
import com.elster.jupiter.metering.ReadingTypeTemplateAttributeName;

@ProviderType
public interface PartiallySpecifiedReadingType extends ReadingTypeRequirement {
    String TYPE_IDENTIFIER = "P";

    /**
     * Overrides a specific reading type attribute's value.
     *
     * @param name attribute unique id
     * @param code value for the attribute
     * @return that object
     */
    PartiallySpecifiedReadingType overrideAttribute(ReadingTypeTemplateAttributeName name, int code);

    /**
     * Resets a specific reading type attribute's value back to the value from template.
     *
     * @param name attribute unique id
     * @return that object
     */
    PartiallySpecifiedReadingType removeOverriddenAttribute(ReadingTypeTemplateAttributeName name);

    /**
     * @return A source template
     */
    ReadingTypeTemplate getReadingTypeTemplate();
}

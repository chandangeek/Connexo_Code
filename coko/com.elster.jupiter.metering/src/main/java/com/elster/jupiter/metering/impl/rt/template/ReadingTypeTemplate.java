package com.elster.jupiter.metering.impl.rt.template;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import java.util.Set;

public interface ReadingTypeTemplate extends HasId, HasName {
    /**
     * @return an ordered set of attributes for that template (18 elements).
     */
    Set<ReadingTypeTemplateAttribute> getAttributes();

    /**
     * Updates attribute in template.
     *
     * @param name           reading type attribute name
     * @param code           attribute value
     * @param canBeAny       indicates that user can specify any value for that attribute (from the list of system
     *                       possible values, see {@link ReadingTypeTemplateAttributeName#getPossibleValues()})
     * @param possibleValues possible attribute values, will be ignored if the <code>canBeAny</code> is true
     * @return the template
     */
    ReadingTypeTemplate setAttribute(ReadingTypeTemplateAttributeName name, int code, boolean canBeAny, Integer... possibleValues);
}

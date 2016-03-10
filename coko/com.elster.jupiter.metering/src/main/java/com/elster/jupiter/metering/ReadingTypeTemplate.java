package com.elster.jupiter.metering;

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
     * @param possibleValues possible attribute values, will be ignored if the <code>canBeAny</code> is true
     * @return the template
     */
    ReadingTypeTemplate setAttribute(ReadingTypeTemplateAttributeName name, Integer code, Integer... possibleValues);

    long getVersion();
}

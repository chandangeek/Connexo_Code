package com.elster.jupiter.metering;

import java.util.List;
import java.util.Optional;

public interface ReadingTypeTemplateAttribute {

    /**
     * @return the unique attribute name
     */
    ReadingTypeTemplateAttributeName getName();

    /**
     * @return reading type code for the specific attribute
     */
    Optional<Integer> getCode();

    /**
     * @return a sub-set of system allowed values for that reading type attribute.
     * @see ReadingTypeTemplateAttributeName.ReadingTypeAttribute#getPossibleValues()
     */
    List<Integer> getPossibleValues();

    /**
     * Tests a candidate reading type's attribute.
     *
     * @param candidate
     * @return <code>true</code> if candidate has correct value in that attribute
     */
    boolean matches(ReadingType candidate);
}

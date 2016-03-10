package com.elster.jupiter.metering.impl.rt.template;

import java.util.List;

public interface ReadingTypeTemplateAttribute {

    /**
     * @return the unique attribute name
     */
    ReadingTypeTemplateAttributeName getName();

    /**
     * indicates that user can specify any value for that attribute.
     *
     * @return <code>false</code> if reading type attribute value should be selected from the list of possible values
     * (default is <code>true</code>).
     * @see #getPossibleValues()
     */
    boolean canBeAny();

    /**
     * @return reading type code (integer) for the specific attribute, <code>0</code> by default.
     */
    int getCode();

    /**
     * @return an empty list if {@link #canBeAny()} returns true, or sub-set of system allowed values for that
     * reading type attribute.
     * @see ReadingTypeTemplateAttributeName#getPossibleValues()
     */
    List<Integer> getPossibleValues();

}

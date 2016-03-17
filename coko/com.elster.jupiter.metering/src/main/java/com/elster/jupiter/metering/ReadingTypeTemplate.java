package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import java.util.Set;

@ProviderType
public interface ReadingTypeTemplate extends HasId, HasName {
    /**
     * @return an ordered set of attributes for that template (18 elements).
     */
    Set<ReadingTypeTemplateAttribute> getAttributes();

    /**
     * Returns a specific template attribute.
     *
     * @param attributeName attribute unique id
     * @return a specific template attribute
     */
    ReadingTypeTemplateAttribute getAttribute(ReadingTypeTemplateAttributeName attributeName);

    ReadingTypeTemplateUpdater updater();

    /**
     * Indicates that one or more attributes does not define nor specific code, nor possible values.
     */
    boolean hasWildcards();

    /**
     * Checks that all attributes in the given reading type (candidate) are within template limits.
     *
     * @param candidate reading type for check
     * @return <code>true</code> if all attributes are within limits
     * @see ReadingTypeTemplateAttribute#matches(ReadingType)
     */
    boolean matches(ReadingType candidate);

    long getVersion();

    interface ReadingTypeTemplateUpdater {
        /**
         * Updates attribute in template.
         *
         * @param name           reading type attribute name
         * @param code           attribute value
         * @param possibleValues possible attribute values
         * @return the updater
         */
        ReadingTypeTemplateUpdater setAttribute(ReadingTypeTemplateAttributeName name, Integer code, Integer... possibleValues);

        void done();
    }
}

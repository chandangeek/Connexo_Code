package com.energyict.mdc.upl.messages.legacy;

import com.energyict.mdc.upl.properties.NumberLookup;

import java.util.List;

/**
 * Extracts information that pertains to {@link NumberLookup}s
 * from message related objects for the purpose
 * of formatting it as an old-style device message.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-30 (15:08)
 */
public interface NumberLookupExtractor {
    /**
     * Extracts the unique identifier of a {@link NumberLookup}
     * and returns it as a String for easy formatting in XML based content.
     *
     * @param numberLookup The NumberLookup
     * @return The String representation of the NumberLookup's identifier
     */
    String id(NumberLookup numberLookup);

    List<String> keys(NumberLookup numberLookup);
}
package com.energyict.mdc.dynamic.relation;

import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link DefaultAttributeTypeDetective}
 * interface that acts as a composite for smaller DefaultAttributeTypeDetectives.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-04 (13:37)
 */
public class CompositeAttributeTypeDetective implements DefaultAttributeTypeDetective {

    private List<DefaultAttributeTypeDetective> detectives;

    public CompositeAttributeTypeDetective(DefaultAttributeTypeDetective... detectives) {
        this(Arrays.asList(detectives));
    }

    public CompositeAttributeTypeDetective(List<DefaultAttributeTypeDetective> detectives) {
        super();
        this.detectives = detectives;
    }

    @Override
    public boolean isDefaultAttribute (RelationAttributeType attributeType) {
        for (DefaultAttributeTypeDetective each : this.detectives) {
            if (each.isDefaultAttribute(attributeType)) {
                return true;
            }
        }
        return false;
    }

}
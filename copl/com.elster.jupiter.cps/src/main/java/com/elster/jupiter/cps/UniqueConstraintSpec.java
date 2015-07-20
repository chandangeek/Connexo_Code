package com.elster.jupiter.cps;

import java.util.List;

/**
 * Models the specification of a uniqueness constraint
 * on the properties provided by a {@link CustomPropertySet}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-20 (15:30)
 */
public interface UniqueConstraintSpec {

    /**
     * The List of names of the PropertySpecs that are
     * part of this uniqueness constraint.
     *
     * @return The list of PropertySpec names
     */
    List<String> getPropertySpecNames();

}
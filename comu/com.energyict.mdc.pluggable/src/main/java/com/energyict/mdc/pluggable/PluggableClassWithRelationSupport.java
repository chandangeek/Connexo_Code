package com.energyict.mdc.pluggable;

import com.energyict.mdc.dynamic.relation.RelationType;

/**
 * Models a {@link PluggableClass} that uses
 * {@link RelationType}s to store the values of its properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-10 (10:38)
 */
public interface PluggableClassWithRelationSupport extends PluggableClass, RelationTypeSupport, RelationSupport {
}
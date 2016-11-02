package com.elster.jupiter.mdm.usagepoint.lifecycle;

import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ConsumerType;

import java.util.Collections;
import java.util.List;

/**
 * Models a number of tiny actions that will be executed by the
 * usage point life cycle engine as part of an {@link UsagePointTransition}.
 */
@ConsumerType
public interface MicroAction {

    String getKey();

    String getName();

    String getDescription();

    String getCategory();

    String getCategoryName();

    default List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }
}

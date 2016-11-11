package com.elster.jupiter.usagepoint.lifecycle.config;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ConsumerType;

import java.util.Collections;
import java.util.List;

/**
 * Models a number of tiny actions that will be executed by the
 * usage point life cycle engine as part of an {@link UsagePointTransition}.
 */
@ConsumerType
public interface MicroAction extends HasName {

    String getKey();

    String getDescription();

    String getCategory();

    String getCategoryName();

    default List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }
}

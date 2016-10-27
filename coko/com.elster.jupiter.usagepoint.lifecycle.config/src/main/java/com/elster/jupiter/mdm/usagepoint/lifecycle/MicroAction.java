package com.elster.jupiter.mdm.usagepoint.lifecycle;

import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ProviderType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Models a number of tiny actions that will be executed by the
 * usage point life cycle engine as part of an {@link UsagePointTransition}.
 */
@ProviderType
public interface MicroAction {

    Key getKey();

    String getName();

    String getDescription();

    String getCategoryName();

    default List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    enum Key {
        CANCEL_ALL_SERVICE_CALLS(MicroCategory.SERVICE_CALLS),;

        private MicroCategory category;
        private String conflictGroupKey;

        Key(MicroCategory category) {
            this.category = category;
        }

        Key(MicroCategory category, String conflictGroupKey) {
            this(category);
            this.conflictGroupKey = conflictGroupKey;
        }

        public MicroCategory getCategory() {
            return this.category;
        }

        public Optional<String> getConflictGroupKey() {
            return Optional.ofNullable(this.conflictGroupKey);
        }
    }
}

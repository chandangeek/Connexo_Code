package com.elster.jupiter.mdm.usagepoint.lifecycle;

import aQute.bnd.annotation.ProviderType;

/**
 * Models pre-transition checks for {@link UsagePointTransition}.
 */
@ProviderType
public interface MicroCheck {

    Key getKey();

    String getName();

    String getDescription();

    String getCategoryName();

    enum Key {
        ALL_DATA_VALID(MicroCategory.VALIDATION),;

        private MicroCategory category;

        Key(MicroCategory category) {
            this.category = category;
        }

        public MicroCategory getCategory() {
            return this.category;
        }
    }
}

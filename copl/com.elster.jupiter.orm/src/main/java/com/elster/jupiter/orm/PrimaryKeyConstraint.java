/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

/*
 * Models a primary key definition.
 */
@ProviderType
public interface PrimaryKeyConstraint extends TableConstraint {

    /*
     * indicates whether 0 is allowed if the primary key consist of a single long field
     */
    boolean allowZero();

    @ProviderType
    interface Builder {
        Builder on(Column... columns);

        Builder allowZero();

        PrimaryKeyConstraint add();

        Builder since(Version version);

        Builder upTo(Version version);

        Builder during(Range... ranges);

        Builder previously(PrimaryKeyConstraint primaryKeyConstraint);

        Builder noDdl();
    }
}

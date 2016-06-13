package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.util.List;

/*
 * Models an index.
 */
@ProviderType
public interface Index {
    List<? extends Column> getColumns();

    String getName();

    int getCompress();

    Table<?> getTable();

    boolean isInVersion(Version version);

    Index since(Version version);

    Index upTo(Version version);

    Index during(Range... ranges);

    @ProviderType
    public interface Builder {
        Builder on(Column... columns);

        Builder compress(int number);

        Index add();
    }
}

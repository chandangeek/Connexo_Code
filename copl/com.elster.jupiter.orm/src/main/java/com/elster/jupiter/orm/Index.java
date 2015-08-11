package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;

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

    @ProviderType
    public interface Builder {
        Builder on(Column... columns);

        Builder compress(int number);

        Index add();
    }
}

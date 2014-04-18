package com.elster.jupiter.orm;

import java.util.List;

public interface Index {
    List<? extends Column> getColumns();

    String getName();

    int getCompress();

    Table<?> getTable();

    public interface Builder {
        Builder on(Column... columns);

        Builder compress(int number);

        Index add();
    }
}

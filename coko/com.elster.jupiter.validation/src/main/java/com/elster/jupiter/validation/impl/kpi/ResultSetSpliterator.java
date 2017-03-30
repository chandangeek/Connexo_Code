/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.orm.UnderlyingSQLFailedException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Spliterator;
import java.util.function.Consumer;

public class ResultSetSpliterator implements Spliterator<ResultSet> {

    private final ResultSet resultSet;

    public ResultSetSpliterator(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    @Override
    public boolean tryAdvance(Consumer<? super ResultSet> action) {
        try {
            if (!resultSet.next()) {
                return false;
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
        action.accept(resultSet);
        return true;
    }

    @Override
    public Spliterator<ResultSet> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return 0;
    }
}

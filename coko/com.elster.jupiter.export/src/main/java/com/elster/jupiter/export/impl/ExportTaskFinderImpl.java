/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.ExportTaskFinder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

class ExportTaskFinderImpl implements ExportTaskFinder {

    private DataModel dataModel;
    private Condition condition = Condition.TRUE;
    private Order defaultOrder;
    private Integer start;
    private Integer limit;

    public ExportTaskFinderImpl(DataModel dataModel, Order defaultOrder) {
        this.dataModel = dataModel;
        this.defaultOrder = defaultOrder;
    }

    @Override
    public ExportTaskFinder setStart(int start) {
        this.start = start;
        return this;
    }

    @Override
    public ExportTaskFinder setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public ExportTaskFinder ofApplication(String application) {
        this.condition = condition.and(where("recurrentTask.application").isEqualTo(application));
        return this;
    }

    @Override
    public List<? extends ExportTask> find() {
        return stream().select();
    }

    @Override
    public QueryStream<? extends ExportTask> stream() {
        QueryStream<? extends ExportTask> stream = dataModel.stream(IExportTask.class)
                .join(RecurrentTask.class)
                .filter(condition);
        if (start != null) {
            stream = stream.skip(start);
        }
        if (limit != null) {
            stream = stream.limit(limit);
        }
        return stream;
    }
}

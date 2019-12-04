/*
 * QueryDateGetTasks.java
 *
 * Created on 30 september 2003, 10:04
 */

package com.energyict.mdc.engine.offline.core;

import java.util.Date;

/**
 * @author Koen
 */
public class QueryDateGetTasks {

    Date queryDate;

    /**
     * Creates a new instance of QueryDateGetTasks
     */
    public QueryDateGetTasks() {
    }

    public QueryDateGetTasks(Date queryDate) {
        this.queryDate = queryDate;
    }

    public Date getQueryDate() {
        return queryDate;
    }

    public void setQueryDate(Date queryDate) {
        this.queryDate = queryDate;
    }
}
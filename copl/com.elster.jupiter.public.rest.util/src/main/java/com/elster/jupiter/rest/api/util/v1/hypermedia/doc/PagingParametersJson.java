/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1.hypermedia.doc;

import javax.ws.rs.QueryParam;

/**
 * Class created solely to instruct Miredot how to create javadoc for the alternate class com.elster.jupiter.rest.util.JsonQueryParameters
 */
public class PagingParametersJson {
    /**
     * @summary Paging parameter denoting the number of elements to be returned in the answer.
     * To get a paged answer, make sure you always specify both paging parameters <i>start</i> <b>and</b> <i>limit</i>
     */
    private
    @QueryParam("limit")
    Integer limit;
    /**
     * @summary Paging parameter denoting the index of the first element in the total list to be returned in the answer.
     * To get a paged answer, make sure you always specify both paging parameters <i>start</i> <b>and</b> <i>limit</i>
     */
    private
    @QueryParam("start")
    Integer start;

    public Integer getLimit() {
        return limit;
    }

    public Integer getStart() {
        return start;
    }
}

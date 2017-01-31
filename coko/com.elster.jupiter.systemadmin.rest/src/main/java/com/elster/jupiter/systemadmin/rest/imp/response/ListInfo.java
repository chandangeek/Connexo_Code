/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.response;

import com.elster.jupiter.rest.util.QueryParameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListInfo<T> {
    public int total;
    public List<T> data = new ArrayList<>();

    public static <I, O> ListInfo from(Collection<? extends I> entities, Function<I, O> mapper) {
        ListInfo<O> info = new ListInfo<>();
        if (entities != null){
            info.data = entities.stream().map(mapper).collect(Collectors.toList());
            info.total = info.data.size();
        }
        return info;
    }

    public ListInfo<T> paged(QueryParameters parameters){
        if (parameters != null){
            this.total = parameters.determineTotal(this.data.size());
            this.data = parameters.clipToLimit(this.data);
        }
        return this;
    }
}

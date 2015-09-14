package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.domain.util.QueryParameters;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class PagedInfoListCustomized {
    private final String jsonListName;
    private List<?> data = new ArrayList<>();
    private final int total;

    public int getTotal() {
        return total;
    }

    public List<?> getData() {
        return ImmutableList.copyOf(data);
    }
    public PagedInfoListCustomized(String jsonListName, List<?> data, int total) {
        this.jsonListName = jsonListName;
        this.data = data;
        this.total = total;
    }

    public static PagedInfoListCustomized fromPagedList(String jsonListName, List<?> infos, QueryParameters queryParameters, int totalIncrease) {
        boolean couldHaveNextPage=queryParameters.getLimit().isPresent() && infos.size() > queryParameters.getLimit().get() + totalIncrease;
        if (couldHaveNextPage) {
            infos=infos.subList(0,queryParameters.getLimit().get() + totalIncrease);
        }
        int total = infos.size();
        if (queryParameters.getStart().isPresent()) {
            total+=queryParameters.getStart().get();
        }
        if (couldHaveNextPage) {
            total++;
        }

        return new PagedInfoListCustomized(jsonListName, infos, total);
    }
}

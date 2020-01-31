/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.rest.util.impl;

import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;

import com.google.common.cache.CacheStats;
import com.google.common.collect.Lists;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Path("/cache")
public class TableCacheResource {
    private static final String STATUS_TEMPLATE = "totalHits(Nr): {0}, usefulHits(Nr): {1}, usefulHits(%): {2}%, evictionCount(Nr): {3}";
    private volatile OrmService ormService;

    @Inject
    public TableCacheResource(OrmService ormService) {
        this.ormService = ormService;
    }

    @GET
    @Path("/status")
    public Map<String, String> getStatus(){
        return ormService.getDataModels().stream().flatMap(dataModel -> dataModel.getTables().stream())
                .filter(Table::isCached).filter(table -> Objects.nonNull(((Table) table).getCacheStats()))
                .collect(Collectors.toMap(Table::getName, this::getCacheDetails));
    }


    @GET
    @Path("/wholecachedtablestatus")
    public Map<String, String> getWholeCachedTableStatus(){
        return ormService.getDataModels().stream().flatMap(dataModel -> dataModel.getTables().stream())
                .filter(Table::isWholeTableCached).filter(table -> Objects.nonNull(((Table) table).getCacheStats()))
                .collect(Collectors.toMap(Table::getName, this::getCacheDetails));
    }

    private String getCacheDetails(Table table){
        CacheStats stats = table.getCacheStats();
        long total = stats.requestCount();
        long useful = stats.hitCount();
        double percentage = total > 0 ? useful*100/total : 0;
        return MessageFormat.format(STATUS_TEMPLATE, total, useful, percentage, stats.evictionCount());
    }

}

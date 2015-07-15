package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.KorePagedInfoList;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.util.streams.DecoratedStream;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Path("/fields")
public class ReadingTypeFieldResource {

    private final MeteringService meteringService;
    private final Thesaurus thesaurus;

    @Inject
    public ReadingTypeFieldResource(MeteringService meteringService, Thesaurus thesaurus) {
        this.meteringService = meteringService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Path("/unitsofmeasure")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getUnitsOfMeasure(@BeanParam QueryParameters queryParameters) {
        return Response.ok(KorePagedInfoList.asJson(
                "unitsOfMeasure",
                DecoratedStream.decorate(
                        meteringService.getAvailableReadingTypes().
                                stream()).
                        map(rt -> new UnitOfMeasureFieldInfo(rt.getMultiplier(), rt.getUnit())).
                        distinct(i -> i.name).
                        sorted((c1, c2) -> c1.name.compareToIgnoreCase(c2.name)).collect(toList()),
                queryParameters)).build();
    }

    @GET
    @Path("/intervals")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public IntervalFieldInfos getIntervals() {
        List<ReadingType> readingTypes = meteringService.getAvailableReadingTypes();

        Set<TimeAttribute> timeAttributes = readingTypes.stream()
                .map(ReadingType::getMeasuringPeriod)
                .collect(Collectors.<TimeAttribute>toSet());
        Set<MacroPeriod> macroPeriods = readingTypes.stream()
                .map(ReadingType::getMacroPeriod)
                .collect(Collectors.<MacroPeriod>toSet());

        IntervalFieldInfos intervalFieldInfos = new IntervalFieldInfos();
        intervalFieldInfos.from(timeAttributes, macroPeriods, thesaurus);
        return intervalFieldInfos;
    }

    @GET
    @Path("/timeofuse")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public TimeOfUseFieldInfos getTou() {
        return new TimeOfUseFieldInfos();
    }

    @GET
    @Path("/readingtypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getReadingTypes(@BeanParam JsonQueryFilter queryFilter, @BeanParam JsonQueryParameters queryParameters) {
        List<ReadingType> readingTypes;
        if (queryFilter.hasProperty("equidistant")) {
            readingTypes = queryFilter.getBoolean("equidistant")
                    ? meteringService.getAvailableEquidistantReadingTypes()
                    : meteringService.getAvailableNonEquidistantReadingTypes();
        } else {
            readingTypes = meteringService.getAvailableReadingTypes();
        }
        Predicate<ReadingType> filter = getReadingTypeFilterPredicate(queryFilter);
        readingTypes = readingTypes.stream()
                .filter(filter)
                .sorted((c1, c2) -> (c1.getFullAliasName().compareToIgnoreCase(c2.getFullAliasName())))
                .collect(Collectors.toList());
        List<ReadingTypeInfo> pagedReadingTypes = ListPager.of(readingTypes)
                .paged(queryParameters.getStart().orElse(null), queryParameters.getLimit().orElse(null))
                .find()
                .stream()
                .map(ReadingTypeInfo::new)
                .collect(toList());
        return PagedInfoList.fromPagedList("readingTypes", pagedReadingTypes, queryParameters);
    }

    private Predicate<ReadingType> getReadingTypeFilterPredicate(JsonQueryFilter queryFilter) {
        Predicate<ReadingType> filter = rt -> true; // unfiltered
        if (queryFilter.hasFilters()) {
            if (queryFilter.hasProperty("mRID")) {
                filter = filter.and(rt -> rt.getMRID().toLowerCase().contains(queryFilter.getString("mRID").toLowerCase()));
            }
            if (queryFilter.hasProperty("name")) {
                filter = filter.and(rt -> rt.getFullAliasName().toLowerCase().contains(queryFilter.getString("name").toLowerCase()));
            }
            if (queryFilter.hasProperty("tou")) {
                filter = filter.and(rt -> rt.getTou() == queryFilter.getInteger("tou"));
            }
            if (queryFilter.hasProperty("unitOfMeasure")) {
                filter = filter.and(rt -> rt.getUnit().getId() == queryFilter.getLong("unitOfMeasure"));
            }
            if (queryFilter.hasProperty("time")) {
                filter = filter.and(rt -> rt.getMeasuringPeriod().getId() == queryFilter.getInteger("time"));
            }
            if (queryFilter.hasProperty("macro")) {
                filter = filter.and(rt -> rt.getMacroPeriod().getId() == queryFilter.getInteger("macro"));
            }
            if (queryFilter.hasProperty("multiplier")) {
                filter = filter.and(rt -> rt.getMultiplier().getMultiplier() == queryFilter.getInteger("multiplier"));
            }
            if (queryFilter.hasProperty("selectedReadings")) {
                filter = filter.and(rt -> !queryFilter.getStringList("selectedReadings").contains(rt.getMRID().toLowerCase()));
            }
        }
        return filter;
    }
}
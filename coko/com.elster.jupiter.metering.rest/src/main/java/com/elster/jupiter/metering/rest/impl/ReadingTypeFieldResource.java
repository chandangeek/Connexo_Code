package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public UnitOfMeasureFieldInfos getUnitsOfMeasure() {
        List<ReadingType> readingTypes = meteringService.getAvailableReadingTypes();
        UnitOfMeasureFieldInfos unitOfMeasureFieldInfos = new UnitOfMeasureFieldInfos();
        HashMap<String, ReadingType> unitNameReadingTypeHashMap = new HashMap<>();
        for (ReadingType rt : readingTypes) {
            String unitName = rt.getMultiplier().getSymbol() + rt.getUnit().getSymbol();
            unitNameReadingTypeHashMap.put(unitName, rt);
        }
        unitOfMeasureFieldInfos.add(unitNameReadingTypeHashMap);
        return unitOfMeasureFieldInfos;
    }

    @GET
    @Path("/intervals")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public IntervalFieldInfos getIntervals() {
        IntervalFieldInfos intervalFieldInfos = new IntervalFieldInfos();
        Set<TimeAttribute> timeAttributes = meteringService.getAvailableReadingTypes().stream()
                .map(ReadingType::getMeasuringPeriod)
                .collect(Collectors.<TimeAttribute>toSet());
        timeAttributes.stream()
                .sorted((ta1, ta2) -> Integer.compare(ta1.getMinutes(), ta2.getMinutes()))
                .forEach(ta -> intervalFieldInfos.add(ta.getId(), thesaurus.getString(MessageSeeds.Keys.TIME_ATTRIBUTE_KEY_PREFIX + ta.getId(), ta.getDescription())));
        return intervalFieldInfos;
    }

    @GET
    @Path("/timeofuse")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public TimeOfUseFieldInfos getTou() {
        return new TimeOfUseFieldInfos();
    }

    @GET
    @Path("/readingtypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getReadingTypes(@BeanParam JsonQueryFilter queryFilter, @BeanParam QueryParameters queryParameters) {
        List<ReadingType> readingTypes = meteringService.getAvailableReadingTypes();
        Predicate<ReadingType> filter = getReadingTypeFilterPredicate(queryFilter);
        readingTypes = readingTypes.stream().filter(filter::test).collect(Collectors.<ReadingType>toList());
        List<ReadingTypeInfo> pagedReadingTypes = ListPager.of(readingTypes).from(queryParameters).find().stream().map(ReadingTypeInfo::new).collect(toList());
        return Response.ok(PagedInfoList.asJson("readingTypes", pagedReadingTypes, queryParameters)).build();
    }

    private Predicate<ReadingType> getReadingTypeFilterPredicate(JsonQueryFilter queryFilter) {
        if (queryFilter.hasFilters()) {
            Optional<String> mRID = queryFilter.hasProperty("mRID") ? Optional.of(queryFilter.getString("mRID")) : Optional.empty();
            String name = queryFilter.hasProperty("name") ? queryFilter.getString("name") : "";
            Integer tou = queryFilter.getInteger("tou");
            Long unitOfMeasure = queryFilter.getLong("unitOfMeasure");
            Integer time = queryFilter.getInteger("time");
            Integer multiplier = queryFilter.getInteger("multiplier");
            return rt -> (name.isEmpty() || readingTypeAssembledNameFilter(name, rt)) &&
                    (tou == null || rt.getTou() == tou) &&
                    (!mRID.isPresent() || rt.getMRID().contains(mRID.get())) &&
                    (unitOfMeasure == null || rt.getUnit().getId() == unitOfMeasure) &&
                    (time == null || rt.getMeasuringPeriod().getId() == time) &&
                    (multiplier == null || rt.getMultiplier().getMultiplier() == multiplier);
        }
        return e -> true;
    }

    private boolean readingTypeAssembledNameFilter(String value, ReadingType rt) {
        StringBuilder rtAssembledName = new StringBuilder();
        rtAssembledName.append(rt.getName()).append(" ");
        rtAssembledName.append(rt.getAliasName()).append(" ");
        rtAssembledName.append(rt.getDescription()).append(" ");
        rtAssembledName.append(rt.getMultiplier().getSymbol()).append(rt.getUnit().getSymbol());
        return rtAssembledName.toString().toLowerCase().contains(value.toLowerCase());
    }
}
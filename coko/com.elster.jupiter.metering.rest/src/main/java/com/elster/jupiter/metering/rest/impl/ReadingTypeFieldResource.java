package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.rest.util.JsonQueryFilter;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Path("/fields")
public class ReadingTypeFieldResource {

    private final MeteringService meteringService;

    @Inject
    public ReadingTypeFieldResource(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @GET
    @Path("/unitsofmeasure")
    @Produces(MediaType.APPLICATION_JSON)
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
    @Produces(MediaType.APPLICATION_JSON)
    public IntervalFieldInfos getIntervals() {
        IntervalFieldInfos intervalFieldInfos = new IntervalFieldInfos();
        Set<TimeAttribute> timeAttributes = meteringService.getAvailableReadingTypes().stream().
                filter(rt -> rt.getMeasuringPeriod().isApplicable()).map(ReadingType::getMeasuringPeriod).
                collect(Collectors.<TimeAttribute>toSet());
        for (TimeAttribute ta : timeAttributes) {
            intervalFieldInfos.add(ta);
        }
        return intervalFieldInfos;
    }

    @GET
    @Path("/timeofuse")
    @Produces(MediaType.APPLICATION_JSON)
    public TimeOfUseFieldInfos getTou() {
        return new TimeOfUseFieldInfos();
    }

    @GET
    @Path("/readingtypes")
    @Produces(MediaType.APPLICATION_JSON)
    public ReadingTypeInfos getReadingTypes(@BeanParam JsonQueryFilter queryFilter) {
        List<ReadingType> readingTypes = meteringService.getAvailableReadingTypes();
        Predicate<ReadingType> filter = getReadingTypeFilterPredicate(queryFilter);
        readingTypes = readingTypes.stream().filter(filter::test).collect(Collectors.<ReadingType>toList());
        return new ReadingTypeInfos(readingTypes);
    }

    private Predicate<ReadingType> getReadingTypeFilterPredicate(JsonQueryFilter queryFilter) {
        if (queryFilter.hasFilters()) {
            String name = queryFilter.hasProperty("name") ? queryFilter.getString("name") : "";
            Integer tou = queryFilter.getInteger("tou");
            Long unitOfMeasure = queryFilter.getLong("unitOfMeasure");
            Integer time = queryFilter.getInteger("time");
            Integer multiplier = queryFilter.getInteger("multiplier");
            return rt -> (name.isEmpty() || readingTypeAssembledNameFilter(name, rt)) &&
                    (tou == null || rt.getTou() == tou) &&
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
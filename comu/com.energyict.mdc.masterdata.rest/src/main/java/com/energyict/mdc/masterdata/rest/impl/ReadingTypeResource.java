package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/readingtypes")
public class ReadingTypeResource {

    private final MeteringService meteringService;
    private final MasterDataService masterDataService;
    private final Thesaurus thesaurus;

    @Inject
    public ReadingTypeResource(MeteringService meteringService, MasterDataService masterDataService, Thesaurus thesaurus) {
        this.meteringService = meteringService;
        this.masterDataService = masterDataService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ReadingTypeInfos getReadingTypes(@BeanParam QueryParameters queryParameters) {
        //TODO where is this rest call made, filter should be adjusted
        String searchText = queryParameters.getLike();
        if (searchText != null && !searchText.isEmpty()) {
            String dbSearchText = "*" + searchText + "*";
            List<ReadingType> readingTypes = meteringService.getAvailableReadingTypes();
//        Predicate<ReadingType> filter = getReadingTypeFilterPredicate(queryFilter);
            List<RegisterType> registerTypes = masterDataService.findAllRegisterTypes().find();
            List<String> readingTypesInUseIds = new ArrayList<>();
            for (RegisterType registerType : registerTypes) {
                readingTypesInUseIds.add(registerType.getReadingType().getMRID());
            }
            readingTypes = readingTypes.stream().filter(rt -> !readingTypesInUseIds.contains(rt.getMRID()))
                    .collect(Collectors.<ReadingType>toList());
            return new ReadingTypeInfos(readingTypes);
        }
        return new ReadingTypeInfos();
    }





//    private Predicate<ReadingType> getReadingTypeFilterPredicate(JsonQueryFilter queryFilter) {
//        if (queryFilter.hasFilters()) {
//            if (queryFilter.hasProperty("unitOfMeasureId") && queryFilter.hasProperty("tou")) {
//                long unitOfMeasureId = queryFilter.getLong("unitOfMeasureId");
//                int timeOfUse = queryFilter.getInteger("tou");
//                Optional<Phenomenon> phenomenon = masterDataService.findPhenomenon(unitOfMeasureId);
//                if (phenomenon.isPresent()) {
//                    String measurementCode = phenomenon.get().getUnit().getBaseUnit().toString();
//                    return rt -> rt.getTou() == timeOfUse && rt.getUnit().getSymbol().equals(measurementCode) &&
//                            phenomenon.get().getUnit().getScale() == rt.getMultiplier().getMultiplier();
//                }
//            }
//
//        }
//        return e -> true;
//    }
}
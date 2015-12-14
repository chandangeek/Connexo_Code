package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Path("/")
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

    @GET @Transactional
    @Path("/unusedreadingtypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public ReadingTypeInfos getUnusedReadingTypes(@BeanParam JsonQueryParameters queryParameters) {
        String searchText = queryParameters.getLike();
        if (searchText != null && !searchText.isEmpty()) {
            String dbSearchText = searchText;
            List<ReadingType> readingTypes = meteringService.getAllReadingTypesWithoutInterval();
            Predicate<ReadingType> filter = getReadingTypeFilterPredicate(dbSearchText);
            List<RegisterType> registerTypes = masterDataService.findAllRegisterTypes().find();
            List<String> readingTypesInUseIds = new ArrayList<>();
            for (RegisterType registerType : registerTypes) {
                readingTypesInUseIds.add(registerType.getReadingType().getMRID());
            }
            readingTypes = readingTypes.stream().filter(rt -> filter.test(rt) && !readingTypesInUseIds.contains(rt.getMRID()))
                    .collect(Collectors.<ReadingType>toList());
            if (readingTypes.size() > 50) {
                return new ReadingTypeInfos(readingTypes.subList(0, 50));
            } else {
                return new ReadingTypeInfos((readingTypes));
            }
        }
        return new ReadingTypeInfos();
    }

    @GET @Transactional
    @Path("/readingtypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public ReadingTypeInfos getReadingTypes(@BeanParam JsonQueryParameters queryParameters) {
        String searchText = queryParameters.getLike();
        if (searchText != null && !searchText.isEmpty()) {
            String dbSearchText = searchText;
            List<ReadingType> readingTypes = meteringService.getAllReadingTypesWithoutInterval();
            Predicate<ReadingType> filter = getReadingTypeFilterPredicate(dbSearchText);
            readingTypes = readingTypes.stream().filter(filter::test)
                    .collect(Collectors.<ReadingType>toList());
            if (readingTypes.size() > 50) {
                return new ReadingTypeInfos(readingTypes.subList(0, 50));
            } else {
                return new ReadingTypeInfos((readingTypes));
            }
        }
        return new ReadingTypeInfos();
    }


    private Predicate<ReadingType> getReadingTypeFilterPredicate(String dbSearchText) {
        String regex = ".*".concat(escapeSpecialCharacters(dbSearchText).replace(" ", ".*").toLowerCase().concat(".*"));
        return rt -> rt.getFullAliasName().toLowerCase().matches(regex);
    }

    private String escapeSpecialCharacters(String text){
        for (String keyword: Arrays.asList("\\", "_", "%", "(", ")", "+", "-", ".", "[", "*")) {
            text=text.replace(keyword,"\\"+keyword);
        }
        return text;
    }
}
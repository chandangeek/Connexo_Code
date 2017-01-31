/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeFilter;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.masterdata.MasterDataService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
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

    @GET
    @Transactional
    @Path("/unusedreadingtypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public ReadingTypeInfos getUnusedReadingTypes(@BeanParam JsonQueryParameters queryParameters) {
        String searchText = queryParameters.getLike();
        if (searchText != null && !searchText.isEmpty()) {
            ReadingTypeFilter filter = new ReadingTypeFilter();
            filter.addCondition(getReadingTypeFilterCondition(searchText));
            List<String> readingTypesInUseIds = masterDataService.findAllRegisterTypes().stream()
                    .map(rT -> rT.getReadingType().getMRID()).collect(Collectors.toList());
            return new ReadingTypeInfos(meteringService.findReadingTypes(filter).stream()
                    .filter(rt -> !readingTypesInUseIds.contains(rt.getMRID()))
                    .limit(50)
                    .collect(Collectors.<ReadingType>toList()));
        }
        return new ReadingTypeInfos();
    }

    @GET
    @Transactional
    @Path("/readingtypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public ReadingTypeInfos getReadingTypes(@BeanParam JsonQueryParameters queryParameters) {
        String searchText = queryParameters.getLike();
        if (searchText != null && !searchText.isEmpty()) {
            ReadingTypeFilter filter = new ReadingTypeFilter();
            filter.addCondition(getReadingTypeFilterCondition(searchText));
            return new ReadingTypeInfos(meteringService.findReadingTypes(filter).stream()
                    .limit(50)
                    .collect(Collectors.<ReadingType>toList()));
        }
        return new ReadingTypeInfos();
    }

    private Condition getReadingTypeFilterCondition(String dbSearchText) {
        String regex = "*" + dbSearchText.replace(" ", "*") + "*";
        return Where.where("fullAliasName").likeIgnoreCase(regex)
                .and(mrIdMatchOfNormalRegisters()
                        .or(mrIdMatchOfBillingRegisters())
                        .or(mrIdMatchOfPeriodRelatedRegisters()));
    }

    private Condition mrIdMatchOfPeriodRelatedRegisters() {
        return Where.where("mRID").matches("^[11-13]\\.\\[1-24]\\.0", "");
    }

    private Condition mrIdMatchOfBillingRegisters() {
        return Where.where("mRID").matches("^8\\.\\d+\\.0", "");
    }

    private Condition mrIdMatchOfNormalRegisters() {
        return Where.where("mRID").matches("^0\\.\\d+\\.0", "");
    }
}

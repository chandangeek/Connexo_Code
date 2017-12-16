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
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.ReadingTypeInformation;

import com.energyict.obis.ObisCode;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/")
public class ReadingTypeResource {

    private final MeteringService meteringService;
    private final MasterDataService masterDataService;
    private final MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private final Thesaurus thesaurus;

    @Inject
    public ReadingTypeResource(MeteringService meteringService, MasterDataService masterDataService, Thesaurus thesaurus, MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        this.meteringService = meteringService;
        this.masterDataService = masterDataService;
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Transactional
    @Path("/unusedreadingtypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public ReadingTypeInfos getUnusedReadingTypes(@BeanParam JsonQueryParameters queryParameters,
                                                  @QueryParam("obisCode") String obisCodeString) {

        List<String> readingTypesInUseIds = masterDataService.findAllRegisterTypes()
                .stream()
                .map(rT -> rT.getReadingType().getMRID())
                .collect(Collectors.toList());

        ReadingTypeFilter filter;
        List<ReadingType> readingTypes;
        String searchText = queryParameters.getLike();

        if (obisCodeString != null && !obisCodeString.isEmpty()){
            ObisCode code = ObisCode.fromString(obisCodeString);
            if (!code.isInvalid()){
                filter = this.createFilter(code);
                if (this.doesObisMapToReadingType(filter)){
                    filter.addCondition(this.getSearchTextCondition(searchText).orElse(Condition.TRUE));
                    readingTypes = this.findReadingTypes(filter, readingTypesInUseIds);
                    return new ReadingTypeFromObisInfos(readingTypes, false);
                }
            }
        }

        filter = this.createFilter(searchText);
        readingTypes = this.findReadingTypes(filter, readingTypesInUseIds);
        return new ReadingTypeFromObisInfos(readingTypes, true);
    }

    @GET
    @Transactional
    @Path("/obiscode")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public ObisCodeInfo getObisCodeByReadingType(@QueryParam("mRID") String mRID) {
        String obisCode = mdcReadingTypeUtilService.getReadingTypeInformationFrom(mRID)
                .map(ReadingTypeInformation::getObisCode)
                .map(ObisCode::getValue)
                .orElse("");

        return new ObisCodeInfo(obisCode);
    }

    @GET
    @Transactional
    @Path("/readingtypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public ReadingTypeInfos getReadingTypes(@BeanParam JsonQueryParameters queryParameters) {
        Optional<Condition> condition = this.getSearchTextCondition(queryParameters.getLike());
        if (condition.isPresent()){
            ReadingTypeFilter filter = new ReadingTypeFilter();
            filter.addCondition(condition.get());
            return new ReadingTypeInfos(meteringService.findReadingTypes(filter).stream()
                    .limit(50)
                    .collect(Collectors.toList()));
        }
        return new ReadingTypeInfos();
    }

    private List<ReadingType> findReadingTypes(ReadingTypeFilter filter, List<String> readingTypesInUseIds) {
        return meteringService.findReadingTypes(filter)
                .stream()
                .filter(rt -> !readingTypesInUseIds.contains(rt.getMRID()))
                .limit(50)
                .collect(Collectors.toList());
    }

    private boolean doesObisMapToReadingType(ReadingTypeFilter filter) {
        return !meteringService.findReadingTypes(filter).find().isEmpty();
    }

    private ReadingTypeFilter createFilter(ObisCode code) {
        ReadingTypeFilter filter = new ReadingTypeFilter();
        String mRID = mdcReadingTypeUtilService.getReadingTypeFilterFrom(code);
        filter.addCondition(ReadingTypeConditionUtil.mridFromObisMatch(mRID));
        return filter;
    }

    private ReadingTypeFilter createFilter(String searchText) {
        ReadingTypeFilter filter = new ReadingTypeFilter();
        filter.addCondition(this.getSearchTextCondition(searchText).orElse(Condition.TRUE));
        return filter;
    }

    private Optional<Condition> getSearchTextCondition(String text) {
        return (text == null || text.isEmpty()) ? Optional.empty() : Optional.of(ReadingTypeConditionUtil.searchTextMatch(text));
    }

}

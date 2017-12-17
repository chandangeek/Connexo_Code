/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeFilter;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.Transactional;
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


import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/")
public class ReadingTypeResource {

    private final MeteringService meteringService;
    private final MasterDataService masterDataService;
    private final MdcReadingTypeUtilService mdcReadingTypeUtilService;

    @Inject
    public ReadingTypeResource(MeteringService meteringService, MasterDataService masterDataService, MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        this.meteringService = meteringService;
        this.masterDataService = masterDataService;
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
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

        String searchText = queryParameters.getLike();
        ObisMapper obisMapper = new ObisMapper(obisCodeString);
        List<ReadingType> readingTypes = obisMapper.getReadingTypes(readingTypesInUseIds);
        if (!readingTypes.isEmpty()) {
            return this.createInfoFromObisAndSearchText(searchText, readingTypes);
        }
        return this.createInfoFromSearchText(searchText, readingTypesInUseIds);
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
        String searchText = queryParameters.getLike();
        if (searchText != null && !searchText.isEmpty()){
            ReadingTypeFilter filter = new ReadingTypeFilter();
            filter.addCondition(ReadingTypeFilterUtil.getFilterCondition(searchText));
            return new ReadingTypeInfos(meteringService.findReadingTypes(filter).stream()
                    .limit(50)
                    .collect(Collectors.toList()));
        }
        return new ReadingTypeInfos();
    }

    /**
     * @param filter used to query the database
     * @param readingTypesInUseIds reading types that we're already used to create a register type
     * @return zero or more reading types that are not in use
     */
    private List<ReadingType> findReadingTypes(ReadingTypeFilter filter, List<String> readingTypesInUseIds) {
        return meteringService.findReadingTypes(filter)
                .stream()
                .filter(rt -> !readingTypesInUseIds.contains(rt.getMRID()))
                .limit(50)
                .collect(Collectors.toList());
    }

    /**
     * This method is called after we've already queried the database using the obis mapper filter.
     * The list that was obtained after the query, is further filtered using the like query parameter.
     * @param searchText like query parameter
     * @param readingTypes non empty list of reading types
     * @return ReadingTypeInfo with mappingError = false
     */
    private ReadingTypeInfos createInfoFromObisAndSearchText(String searchText, List<ReadingType> readingTypes) {
        readingTypes = ReadingTypeFilterUtil.getFilteredList(searchText, readingTypes);
        return new ReadingTypeFromObisInfos(readingTypes, false);
    }

    /**
     * This method is called when an obis code is not present, invalid or doesn't map to a reading type
     * @param searchText like query parameter
     * @param readingTypesInUseIds reading types that we're already used to create a register type
     * @return ReadingTypeInfo with mappingError = true
     */
    private ReadingTypeInfos createInfoFromSearchText(String searchText, List<String> readingTypesInUseIds) {
        ReadingTypeFilter filter = new ReadingTypeFilter();
        filter.addCondition(ReadingTypeFilterUtil.getFilterCondition(searchText));
        List<ReadingType> readingTypes = this.findReadingTypes(filter, readingTypesInUseIds);
        return new ReadingTypeFromObisInfos(readingTypes, true);
    }

    /**
     * Class that processes the obis query parameter.
     * Returns the list of ReadingTypes that the ObisCode maps to.
     */
    private class ObisMapper {

        private final String obisCode;

        ObisMapper(String obisParam) {
            this.obisCode = obisParam;
        }

        List<ReadingType> getReadingTypes(List<String> readingTypesInUseIds) {
             return this.getCode()
                     .map(code -> getFilteredList(code, readingTypesInUseIds))
                     .orElse(Collections.emptyList());
        }

        private List<ReadingType> getFilteredList(ObisCode code, List<String> readingTypesInUseIds) {
            ReadingTypeFilter filter = new ReadingTypeFilter();
            String mRID = mdcReadingTypeUtilService.getReadingTypeFilterFrom(code);
            filter.addCondition(ReadingTypeFilterUtil.getMRIDFilterContion(mRID));
            return findReadingTypes(filter, readingTypesInUseIds);
        }

        private Optional<ObisCode> getCode() {
            if (this.obisCode == null || this.obisCode.isEmpty()) {
                return Optional.empty();
            }
            ObisCode code = ObisCode.fromString(this.obisCode);
            return code.isInvalid() ? Optional.empty() : Optional.of(code);
        }
    }
}



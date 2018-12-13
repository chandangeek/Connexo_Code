/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeFilter;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.nls.Thesaurus;
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
import javax.ws.rs.PathParam;
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
    private final Thesaurus thesaurus;


    @Inject
    public ReadingTypeResource(MeteringService meteringService, MasterDataService masterDataService, MdcReadingTypeUtilService mdcReadingTypeUtilService, Thesaurus thesaurus) {
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
                                                  @QueryParam("obisCode") String obisCodeString,
                                                  @QueryParam("mRID") String mRID) {

        if (mRID != null && !mRID.isEmpty()) {
            return this.getReadingTypeInfoByMrid(mRID);
        }

        List<ReadingType> unusedReadingTypes;
        String mappingError = null;
        String searchText = queryParameters.getLike();
        List<String> readingTypesInUseIds = this.getReadingTypesInUse();
        if (obisCodeString == null || obisCodeString.isEmpty()) {
            unusedReadingTypes = this.findUnusedReadingTypesBySearchText(searchText, readingTypesInUseIds);
        } else {
            try {
                List<ReadingType> allReadingTypes = findReadingTypesByObisAndSearchText(obisCodeString, searchText);
                unusedReadingTypes = this.getUnusedReadingTypes(allReadingTypes, readingTypesInUseIds);
            } catch (MappingException e) {
                // If any error is present, we will display the error message and all unused reading types filtered by the text query.
                mappingError = e.getLocalizedMessage();
                unusedReadingTypes = this.findUnusedReadingTypesBySearchText(searchText, readingTypesInUseIds);
            }
        }

        return mappingError != null ? new ReadingTypeWithMappingErrorInfos(unusedReadingTypes, mappingError) : new ReadingTypeInfos(unusedReadingTypes);
    }


    @GET
    @Transactional
    @Path("/mappedReadingType/{obis}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public StringResponse getMappedReadingType(@PathParam("obis") String obisCode) {
        String response = this.getObisCode(obisCode)
                .map(mdcReadingTypeUtilService::getReadingTypeFilterFrom)
                .map(ReadingTypeUtil::extractUniqueFromRegex)
                .orElse("");
        return new StringResponse(response);
    }

    @GET
    @Transactional
    @Path("/mappedObisCode/{mRID}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public StringResponse getMappedObisCode(@PathParam("mRID") String mRID) {
        String obisCode = mdcReadingTypeUtilService.getReadingTypeInformationFrom(mRID)
                .map(ReadingTypeInformation::getObisCode)
                .map(ObisCode::getValue)
                .orElse("");
        return new StringResponse(obisCode);
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
            filter.addCondition(MridDbMatcher.getFilterCondition(searchText));
            return new ReadingTypeInfos(meteringService.findReadingTypes(filter).stream()
                    .limit(50)
                    .collect(Collectors.toList()));
        }
        return new ReadingTypeInfos();
    }

    /**
     * The mRID corresponds to a newly added reading type. It should not be in use
     *
     * @param mRID CIM query param
     * @return One Reading type info
     */
    private ReadingTypeInfos getReadingTypeInfoByMrid(String mRID) {
        if (!MridStringMatcher.isValid(mRID))
            return new ReadingTypeInfos();

        return meteringService.getReadingType(mRID)
                .map(ReadingTypeInfos::new)
                .orElse(new ReadingTypeInfos());
    }

    /**
     * @param obisCodeString obis code query param
     * @param searchText text query param
     * @throws MappingException when filtering by obis/text and no reading type is found
     * @return list of reading types mapped from the obis code and filtered using the text param
     */
    private List<ReadingType> findReadingTypesByObisAndSearchText(String obisCodeString, String searchText) {
        List<ReadingType> mappedReadingTypes = this.getMappedReadingTypes(obisCodeString);
        List<ReadingType> readingTypes = ReadingTypeUtil.getFilteredList(searchText, mappedReadingTypes);
        if (readingTypes.isEmpty()) {
            throw new MappingException(thesaurus, MessageSeeds.NO_OBIS_TO_READING_TYPE_MAPPING_POSSIBLE);
        }
        return readingTypes;
    }

    /**
     * Searches the database for the text query param value
     * @param searchText text query param
     * @param readingTypesInUseIds list of reading type mrids that are currently in use
     * @return list of unused reading types.
     */
    private List<ReadingType> findUnusedReadingTypesBySearchText(String searchText, List<String> readingTypesInUseIds) {
        ReadingTypeFilter filter = new ReadingTypeFilter();
        filter.addCondition(MridDbMatcher.getFilterCondition(searchText));
        return meteringService.findReadingTypes(filter)
                .stream()
                .filter(rt -> !readingTypesInUseIds.contains(rt.getMRID()))
                .limit(50)
                .collect(Collectors.toList());
    }

    /**
     * Filters the readingTypes list, using the readingTypesInUseIds
     * @param readingTypes list of reading types mapped from the obis code and filtered using the text param. Not empty
     * @param readingTypesInUseIds list of reading type mrids that are currently in use
     * @throws MappingException if no unused reading types are present after filtering the input reading types list
     * @return list of unused reading types.
     */
    private List<ReadingType> getUnusedReadingTypes(List<ReadingType> readingTypes, List<String> readingTypesInUseIds) {
        List<ReadingType> unusedReadingTypes = readingTypes
                .stream()
                .filter(readingType -> !readingTypesInUseIds.contains(readingType.getMRID()))
                .limit(50)
                .collect(Collectors.toList());
        if (unusedReadingTypes.isEmpty()){
            throw new MappingException(thesaurus, MessageSeeds.MAPPED_READING_TYPE_IS_IN_USE);
        }
        return unusedReadingTypes;
    }

    /**
     * @return list of reading types that are currently used by registers
     */
    private List<String> getReadingTypesInUse() {
        return masterDataService.findAllRegisterTypes()
                .stream()
                .map(rT -> rT.getReadingType().getMRID())
                .collect(Collectors.toList());
    }

    private ReadingTypeFilter getMRIDFilter(ObisCode obisCode) {
        ReadingTypeFilter filter = new ReadingTypeFilter();
        String mRID = this.mdcReadingTypeUtilService.getReadingTypeFilterFrom(obisCode);
        filter.addCondition(MridDbMatcher.getMRIDFilterCondition(mRID));
        return filter;
    }

    private List<ReadingType> getMappedReadingTypes(String obisCodeString) {
        return this.getObisCode(obisCodeString)
                .map(this::getMRIDFilter)
                .map(meteringService::findReadingTypes)
                .map(Finder::find)
                .orElse(Collections.emptyList());
    }

    private Optional<ObisCode> getObisCode(String codeString) {
        if (codeString == null || codeString.isEmpty()) {
            return Optional.empty();
        }
        ObisCode code = ObisCode.fromString(codeString);
        return code.isInvalid() ? Optional.empty() : Optional.of(code);
    }
}



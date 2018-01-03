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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


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
                                                  @QueryParam("obisCode") String obisCodeString,
                                                  @QueryParam("mRID") String mRID) {

        if (mRID != null && !mRID.isEmpty()) {
            return meteringService.getReadingType(mRID)
                    .map(ReadingTypeInfos::new)
                    .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        } else {
            List<String> readingTypesInUseIds = masterDataService.findAllRegisterTypes()
                    .stream()
                    .map(rT -> rT.getReadingType().getMRID())
                    .collect(Collectors.toList());

            List<ReadingType> readingTypes = ReadingTypeResourceUtil.getObisCode(obisCodeString)
                    .map(this::getMRIDFilter)
                    .map(filter -> this.findReadingTypes(filter, readingTypesInUseIds))
                    .orElse(Collections.emptyList());

            String searchText = queryParameters.getLike();
            if (!readingTypes.isEmpty()) {
                return this.createInfoFromObisAndSearchText(searchText, readingTypes);
            }
            return this.createInfoFromSearchText(searchText, readingTypesInUseIds);
        }
    }


    @GET
    @Transactional
    @Path("/mappedReadingType/{obis}")
    @Produces(MediaType.TEXT_PLAIN + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public StringResponse getMappedReadingType(@PathParam("obis") String obisCode) {
        String response = ReadingTypeResourceUtil.getObisCode(obisCode)
                .map(mdcReadingTypeUtilService::getReadingTypeFilterFrom)
                .map(ReadingTypeResourceUtil::extractUniqueFromRegex)
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
     * Builds a filter that matches all reading types that map to the obis code
     * @param obisCode obis query param
     * @return Non null ReadingTypeFilter
     */
    private ReadingTypeFilter getMRIDFilter(ObisCode obisCode) {
        ReadingTypeFilter filter = new ReadingTypeFilter();
        String mRID = mdcReadingTypeUtilService.getReadingTypeFilterFrom(obisCode);
        filter.addCondition(ReadingTypeFilterUtil.getMRIDFilterCondition(mRID));
        return filter;
    }


    /**
     * Util class for the Reading Type resource
     */
    private static class ReadingTypeResourceUtil {

        private final static String DELIMITER = "\\\\.";
        private final static String NUMBER_REGEX = "[0-9]+";
        private final static String DOT= ".";
        private final static String DEFAULT_VALUE = "0";


        /**
         * @param codeString obis query param
         * @return ObisCode object if present and valid
         */
        static Optional<ObisCode> getObisCode(String codeString) {
            if (codeString == null || codeString.isEmpty()) {
                return Optional.empty();
            }
            ObisCode code = ObisCode.fromString(codeString);
            return code.isInvalid() ? Optional.empty() : Optional.of(code);
        }

        /**
         * Check every MRID field for multiple matches. If there is a single match,
         * we return the unchanged value, otherwise we return a default value.
         * Example:
         * IN : 0\.0\.0.\.0\.1\.1\.(12|37)\.0\.0\.0\.0\.0\.[0-9]+\.[0-9]+\.0\.-?[0-9]+\.[0-9]+\.[0-9]+
         * OUT: 0.0.0.0.1.1.0.0.0.0.0.0.0.0.0.0.0.0
         * @param regex MRID regex that matches one or multiple reading types
         * @return String
         */
        static String extractUniqueFromRegex(String regex) {
            String values[] = regex.split(DELIMITER);
            StringBuilder mrid = new StringBuilder();
            int i = 1;
            String code;
            for(String value : values){
                code = value.matches(NUMBER_REGEX) ? value : DEFAULT_VALUE;
                mrid.append(code);

                if (i++ != values.length)
                    mrid.append(DOT);
            }
            return mrid.toString();
        }
    }
}



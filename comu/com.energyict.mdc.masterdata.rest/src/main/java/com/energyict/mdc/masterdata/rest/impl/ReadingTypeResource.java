/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeFilter;
import com.elster.jupiter.metering.rest.ReadingTypeInfos;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.ReadingTypeInformation;

import com.energyict.obis.ObisCode;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.swing.text.html.Option;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
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
                                                  @QueryParam("obisCode") String obisCodeString,
                                                  @QueryParam("mRID") String mRIDString) {

        // mRID is present when we're returning from the Add ReadingType page
        if (mRIDString != null && !mRIDString.isEmpty()) {
            return meteringService.getReadingType(mRIDString)
                    .map(ReadingTypeInfos::new)
                    .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

        } else {
            List<String> readingTypesInUseIds = masterDataService.findAllRegisterTypes()
                    .stream()
                    .map(rT -> rT.getReadingType().getMRID())
                    .collect(Collectors.toList());

            // Filter reading types based on obis code and search text.
            // If obis is not present or it doesn't map to a reading type,we filter based on the search text only.
            // If obis is not present or it doesn't map to a reading type and the search text is not present,
            // we return all reading type values that are not in use.
            String searchText = queryParameters.getLike();
            ObisAndSearchFilterProvider provider = new ObisAndSearchFilterProvider(obisCodeString);
            ReadingTypeFilter filter = provider.getFilter(searchText);
            List<ReadingType> readingTypes = this.findReadingTypes(filter, readingTypesInUseIds);
            if (readingTypes.isEmpty()){
                filter = SearchFilterProvider.getFilter(searchText);
                readingTypes = this.findReadingTypes(filter, readingTypesInUseIds);
            }

            return readingTypes.isEmpty() ? new ReadingTypeInfos() : new ReadingTypeInfos(readingTypes);
        }
    }

    @GET
    @Transactional
    @Path("/obiscode")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public ObisCodeInfo getObisCodeByReadingType(@QueryParam("mRID") String mRID) {

        ReadingTypeInformation readingTypeInformation = mdcReadingTypeUtilService.getReadingTypeInformationFrom(mRID)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.INVALID_CIM_OBIS_MAPPING, "readingType"));

        return new ObisCodeInfo(readingTypeInformation.getObisCode());
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
            filter.addCondition(SearchFilterProvider.getCondition(searchText));
            return new ReadingTypeInfos(meteringService.findReadingTypes(filter).stream()
                    .limit(50)
                    .collect(Collectors.toList()));
        }
        return new ReadingTypeInfos();
    }


    /**
     *
     * @param filter ReadingTypeFilter
     * @param readingTypesInUseIds ReadingType MRID list extracted from the Register Type List
     * @return Filtered list of ReadingTypes that are not currently in use
     */
    private List<ReadingType> findReadingTypes(ReadingTypeFilter filter, List<String> readingTypesInUseIds) {
        return meteringService.findReadingTypes(filter)
                .stream()
                .filter(rt -> !readingTypesInUseIds.contains(rt.getMRID()))
                .limit(50)
                .collect(Collectors.toList());
    }


    /**
     * Creates a ReadingTypeFilter using the obis code string and the like string from the URL
     */
    private class ObisAndSearchFilterProvider {

        private final String obisCode;

        ObisAndSearchFilterProvider(String obisCode) {
            this.obisCode = obisCode;
        }

        /**
         * @param searchText like="bulk.." string from the URL
         * @return ReadingTypeFilter. Doesn't return null
         */
        ReadingTypeFilter getFilter(String searchText) {
            if (obisCode == null || obisCode.isEmpty()) {
                return SearchFilterProvider.getFilter(searchText);
            }

            ReadingTypeFilter filter = new ReadingTypeFilter();
            filter.addCondition(this.getCondition());
            filter.addCondition(SearchFilterProvider.getCondition(searchText));
            return filter;
        }

        /**
         *
         * @return Condition TRUE if obisCode is invalid, a regex used to match reading types otherwise
         */
        private Condition getCondition() {
            ObisCode obis = ObisCode.fromString(this.obisCode);
            if (obis.isInvalid()) {
                return Condition.TRUE;
            }

            String mRID = mdcReadingTypeUtilService.getReadingTypeFilterFrom(obis);
            return Where.where("mRID").matches(mRID, "");
        }
    }

    /**
     * Creates a ReadingTypeFilter using the like string from the URL
     */
    private static class SearchFilterProvider {

        static ReadingTypeFilter getFilter(String searchText) {
            ReadingTypeFilter filter = new ReadingTypeFilter();
            filter.addCondition(getCondition(searchText));
            return filter;
        }

        /**
         *
         * @param searchText like="bulk.." string from the URL
         * @return Condition.TRUE if text is missing, a regex used to match reading types otherwise
         */
        static Condition getCondition(String searchText) {
            return (searchText == null || searchText.isEmpty()) ? Condition.TRUE : buildCondition(searchText);
        }

        private static Condition buildCondition(String dbSearchText) {
            String regex = "*" + dbSearchText.replace(" ", "*") + "*";
            return Where.where("fullAliasName").likeIgnoreCase(regex)
                    .and(mrIdMatchOfNormalRegisters()
                            .or(mrIdMatchOfBillingRegisters())
                            .or(mrIdMatchOfPeriodRelatedRegisters()));
        }

        private static Condition mrIdMatchOfPeriodRelatedRegisters() {
            return Where.where("mRID").matches("^[11-13]\\.\\[1-24]\\.0", "");
        }

        private static Condition mrIdMatchOfBillingRegisters() {
            return Where.where("mRID").matches("^8\\.\\d+\\.0", "");
        }

        private static Condition mrIdMatchOfNormalRegisters() {
            return Where.where("mRID").matches("^0\\.\\d+\\.0", "");
        }


    }
}

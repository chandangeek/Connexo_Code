/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeFilter;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
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
                                                  @QueryParam("obisCode") String obisCodeString) {

        List<String> readingTypesInUseIds = masterDataService.findAllRegisterTypes()
                .stream()
                .map(rT -> rT.getReadingType().getMRID())
                .collect(Collectors.toList());

        List<ReadingType> readingTypes;
        boolean mappingError = true;
        String searchText = queryParameters.getLike();
        ObisFilterProvider provider = new ObisFilterProvider(obisCodeString);
        // If obis is missing or invalid, we use the like condition
        if (!provider.hasValidObis()) {
            readingTypes = this.findReadingTypes(SearchFilterProvider.getFilter(searchText), readingTypesInUseIds);
        } else {
            ReadingTypeFilter filter = provider.getFilter();
            // If obis is mapping to a reading type, we add the like condition to the filter
            if (this.isObisMapping(filter)) {
                Condition condition = SearchFilterProvider.getCondition(queryParameters.getLike()).orElse(Condition.TRUE);
                filter.addCondition(condition);
                readingTypes = this.findReadingTypes(filter, readingTypesInUseIds);
                mappingError = false;
            } else {
                // If obis is not mapping to a reading type, we use the like condition
               readingTypes = this.findReadingTypes(SearchFilterProvider.getFilter(searchText), readingTypesInUseIds);
            }
        }

        ReadingTypeInfos infos = new ReadingTypeInfos(readingTypes);
        infos.mappingError(mappingError);
        return infos;

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
        Optional<Condition> condition = SearchFilterProvider.getCondition(searchText);
        if (condition.isPresent()){
            ReadingTypeFilter filter = new ReadingTypeFilter();
            filter.addCondition(condition.get());
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

    private boolean isObisMapping(ReadingTypeFilter filter) {
        return !meteringService.findReadingTypes(filter).find().isEmpty();
    }


    private class ObisFilterProvider {

        private final ObisCode obisCode;

        ObisFilterProvider(String obisCodeString){
            this.obisCode = (obisCodeString == null || obisCodeString.isEmpty()) ? null : ObisCode.fromString(obisCodeString);
        }

        ReadingTypeFilter getFilter() {
            String mRID = mdcReadingTypeUtilService.getReadingTypeFilterFrom(this.obisCode);
            ReadingTypeFilter filter = new ReadingTypeFilter();
            filter.addCondition(Where.where("mRID").matches(mRID, ""));
            return filter;
        }

        boolean hasValidObis() {
            return obisCode != null && !obisCode.isInvalid();
        }

    }

    /**
     * Creates a ReadingTypeFilter using the like string from the URL
     */
    private static class SearchFilterProvider {

        static ReadingTypeFilter getFilter(String searchText) {
            ReadingTypeFilter filter = new ReadingTypeFilter();
            filter.addCondition(getCondition(searchText).orElse(Condition.TRUE));
            return filter;
        }

        /**
         *
         * @param searchText like="bulk.." string from the URL
         * @return Condition.TRUE if text is missing, a regex used to match reading types otherwise
         */
        static Optional<Condition> getCondition(String searchText) {
            return (searchText == null || searchText.isEmpty()) ? Optional.empty() : Optional.of(buildCondition(searchText));
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

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
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
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
    public ReadingTypeInfos getUnusedReadingTypes(@BeanParam JsonQueryParameters queryParameters, @QueryParam("obisCode") String obisCodeString) {

        ReadingTypeFilter filter = null;

        if (!Checks.is(obisCodeString).empty()) {
            ObisCode obisCode = ObisCode.fromString(obisCodeString);
            if (!obisCode.isInvalid()) {
                String mRID = mdcReadingTypeUtilService.getReadingTypeFilterFrom(obisCode);
                filter = new ReadingTypeFilter();
                filter.addCondition(mrIdMatchOfRegisters(mRID));
            }
        }

        String searchText = queryParameters.getLike();
        if (!Checks.is(searchText).empty()) {
            if (filter == null) {
                filter = new ReadingTypeFilter();
            }
            filter.addCondition(getReadingTypeFilterCondition(searchText));
        }

        if (filter != null){
            List<String> readingTypesInUseIds = masterDataService.findAllRegisterTypes()
                    .stream()
                    .map(rT -> rT.getReadingType().getMRID())
                    .collect(Collectors.toList());

            List<ReadingType> readingTypeList = meteringService.findReadingTypes(filter)
                    .stream()
                    .filter(rt -> !readingTypesInUseIds.contains(rt.getMRID()))
                    .limit(50)
                    .collect(Collectors.toList());
/*

            if (readingTypeList.isEmpty())
                throw new LocalizedFieldValidationException(MessageSeeds.INVALID_OBIS_CIM_MAPPING, "obisCode");
*/


            return new ReadingTypeInfos(readingTypeList);
        }
        return new ReadingTypeInfos();
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
            filter.addCondition(getReadingTypeFilterCondition(searchText));
            return new ReadingTypeInfos(meteringService.findReadingTypes(filter).stream()
                    .limit(50)
                    .collect(Collectors.toList()));
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

    private Condition mrIdMatchOfRegisters(String mrId) {
        return Where.where("mRID").matches(mrId, "");
    }
}

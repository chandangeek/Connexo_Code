/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.ReadingTypeComparator;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Path("/readingtypes")
public class ReadingTypeResource {

    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final MasterDataService masterDataService;
    private final MeteringService meteringService;
    private final Thesaurus thesaurus;
    private final ReadingTypeInfoFactory readingTypeInfoFactory;

    @Inject
    public ReadingTypeResource(MdcReadingTypeUtilService readingTypeUtilService, MeteringService meteringService, Thesaurus thesaurus, MasterDataService masterDataService, ReadingTypeInfoFactory readingTypeInfoFactory) {
        this.readingTypeUtilService = readingTypeUtilService;
        this.meteringService = meteringService;
        this.thesaurus = thesaurus;
        this.masterDataService = masterDataService;
        this.readingTypeInfoFactory = readingTypeInfoFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getReadingType(@BeanParam JsonQueryFilter queryFilter, @BeanParam JsonQueryParameters queryParameters) throws Exception {
        List<ReadingTypeInfo> readingTypeInfos = new ArrayList<>();
        if (queryFilter.hasFilters()) {
            ObisCode obisCode = queryFilter.getProperty("obisCode", new ObisCodeAdapter());

            String mrid = readingTypeUtilService.getReadingTypeMridFrom(obisCode, Unit.getUndefined());
            Optional<ReadingType> readingType = meteringService.getReadingType(mrid);
            if (readingType.isPresent()) {
                readingTypeInfos.add(readingTypeInfoFactory.from(readingType.get()));
            }
        } else {
            List<ReadingType> readingTypes = ListPager.of(meteringService.getAvailableReadingTypes(), new ReadingTypeComparator()).from(queryParameters).find();
            for (ReadingType readingType : readingTypes) {
                readingTypeInfos.add(readingTypeInfoFactory.from(readingType));
            }
        }
        return PagedInfoList.fromPagedList("readingTypes", readingTypeInfos, queryParameters);
    }

}

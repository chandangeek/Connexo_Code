package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/readingtypes")
public class ReadingTypeResource {

    private final MdcReadingTypeUtilService readingTypeUtilService;

    @Inject
    public ReadingTypeResource(MdcReadingTypeUtilService readingTypeUtilService) {
        this.readingTypeUtilService = readingTypeUtilService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ReadingTypeInfo getReadingTypes(@BeanParam JsonQueryFilter queryFilter) throws Exception {
        if (!queryFilter.getFilterProperties().isEmpty()) {
            String obisCode = queryFilter.getFilterProperties().get("obisCode");
            String unit = queryFilter.getFilterProperties().get("unit");
//            if (!Checks.is(unit).emptyOrOnlyWhiteSpace() && !Checks.is(obisCode).emptyOrOnlyWhiteSpace()) {
//                ReadingTypeUnit readingTypeUnit = new ReadingTypeUnitAdapter().unmarshal(unit);
//                readingTypeUtilService.getReadingTypeFrom(ObisCode.fromString(obisCode), readingTypeUnit.getUnit());
//            }
        }

        ReadingTypeInfo test = new ReadingTypeInfo();
        test.mrid = "0.0.10.1.20.1.12.0.0.0.0.0.0.0.0.3.73.0";

        return test;
    }

}

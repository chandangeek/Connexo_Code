/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.security.Privileges;
import com.energyict.mdc.device.data.validation.DataQualityOverviews;
import com.energyict.mdc.device.data.validation.DeviceDataQualityService;

import com.google.common.collect.Range;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Currying.perform;
import static com.elster.jupiter.util.streams.Currying.test;

@Path("/validationresults")
public class ValidationResultsResource {

    private final DeviceDataQualityService deviceDataQualityService;
    private final MeteringGroupsService meteringGroupsService;
    private final ExceptionFactory exceptionFactory;

    private enum Validator {
        THRESHOLDVALIDATOR("thresholdViolation") {
            @Override
            protected void applyTo(DeviceDataQualityService.DataQualityOverviewBuilder builder) {
                builder.includeThresholdValidator();
            }
        },
        MISSINGVALUESVALIDATOR("checkMissing") {
            @Override
            protected void applyTo(DeviceDataQualityService.DataQualityOverviewBuilder builder) {
                builder.includeMissingValuesValidator();
            }
        },
        READINGQUALITIESVALIDATOR("intervalState") {
            @Override
            protected void applyTo(DeviceDataQualityService.DataQualityOverviewBuilder builder) {
                builder.includeReadingQualitiesValidator();
            }
        },
        REGISTERINCREASEVALIDATOR("registerIncrease") {
            @Override
            protected void applyTo(DeviceDataQualityService.DataQualityOverviewBuilder builder) {
                builder.includeRegisterIncreaseValidator();
            }
        };

        private final String abbreviation;

        Validator(String abbreviation) {
            this.abbreviation = abbreviation;
        }

        protected abstract void applyTo(DeviceDataQualityService.DataQualityOverviewBuilder builder);

        static Validator fromAbbreviation(String abbreviation) {
            return Stream
                        .of(values())
                        .filter(test(Validator::matchesAbbreviation).with(abbreviation))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unknown kpi type " + abbreviation));
        }

        private boolean matchesAbbreviation(String abbreviation) {
            return this.abbreviation.equals(abbreviation);
        }
    }

    @Inject
    public ValidationResultsResource(DeviceDataQualityService deviceDataQualityService, MeteringGroupsService meteringGroupsService, ExceptionFactory exceptionFactory) {
        this.deviceDataQualityService = deviceDataQualityService;
        this.meteringGroupsService = meteringGroupsService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/devicegroups")
    @RolesAllowed({Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, Privileges.Constants.VALIDATE_MANUAL, Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public PagedInfoList getValidationResultsPerDeviceGroup(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) throws JSONException {
        Instant from = null;
        Instant to = null;
        List<Long> deviceGroupIds = new ArrayList<>();
        SuspectsRange suspectsRange = new IgnoreSuspectRange();
        Set<Validator> validators = EnumSet.noneOf(Validator.class);

        if (uriInfo.getQueryParameters().getFirst("filter") != null) {
            JSONArray filters = new JSONArray(uriInfo.getQueryParameters().getFirst("filter"));
            for (int i = 0; i < filters.length(); i++) {
                if ("from".equals(filters.getJSONObject(i).get("property"))) {
                    from = Instant.ofEpochMilli((Long) filters.getJSONObject(i).get("value"));
                }
                if ("to".equals(filters.getJSONObject(i).get("property"))) {
                    to = Instant.ofEpochMilli((Long) filters.getJSONObject(i).get("value"));
                }
                if ("deviceGroups".equals(filters.getJSONObject(i).get("property"))) {
                    JSONArray groups = new JSONArray(filters.getJSONObject(i).get("value").toString());
                    for (int j = 0; j < groups.length(); j++) {
                        deviceGroupIds.add(groups.getLong(j));
                    }
                }
                if ("amountOfSuspects".equals(filters.getJSONObject(i).get("property"))) {
                    try {
                        JSONObject value = new JSONObject(filters.getJSONObject(i).get("value").toString());
                        if ("==".equals(value.get("operator"))) {
                            suspectsRange = new ExactMatch(Long.parseLong(value.get("criteria").toString()));
                        } else if ("BETWEEN".equals(value.get("operator"))) {
                            JSONArray interval = new JSONArray(value.get("criteria").toString());
                            suspectsRange =
                                    new LongRange(
                                            Long.parseLong(interval.get(0).toString()),
                                            Long.parseLong(interval.get(1).toString()));
                        }
                    } catch (NumberFormatException e) {
                        throw exceptionFactory.newException(MessageSeeds.NUMBER_FORMAT_INVALID);
                    } catch (JSONException e){
                        throw exceptionFactory.newException(MessageSeeds.BETWEEN_VALUES_INVALID);
                    }
                }
                if ("validator".equals(filters.getJSONObject(i).get("property"))) {
                    JSONArray validatorNames = new JSONArray(filters.getJSONObject(i).get("value").toString());
                    for (int j = 0; j < validatorNames.length(); j++) {
                        validators.add(Validator.fromAbbreviation(validatorNames.get(j).toString()));
                    }
                }
            }
        }

        Range<Instant> range = from != null && to != null ? Range.closed(from, to) : Range.all();
        DeviceDataQualityService.DataQualityOverviewBuilder builder =
                this.deviceDataQualityService
                    .forAllGroups(this.endDevicesFromIds(deviceGroupIds))
                    .in(range);
        suspectsRange.applyTo(builder);
        validators.forEach(perform(Validator::applyTo).with(builder));
        DataQualityOverviews dataQualityOverviews =
            builder
                .paged(
                    this.getPageStart(queryParameters),
                    this.getPageEnd(queryParameters));
        return PagedInfoList.fromPagedList("summary", toInfos(dataQualityOverviews), queryParameters);
    }

    private Integer getPageStart(@BeanParam JsonQueryParameters queryParameters) {
        return queryParameters.getStart().orElse(0) + 1;
    }

    private Integer getPageEnd(@BeanParam JsonQueryParameters queryParameters) {
        return queryParameters
                .getLimit()
                .map(limit -> this.getPageStart(queryParameters) + limit)
                .orElse(Integer.MAX_VALUE);
    }

    private List<ValidationOverviewInfo> toInfos(DataQualityOverviews dataQualityOverviews) {
        return dataQualityOverviews
                .allOverviews()
                .stream()
                .map(ValidationOverviewInfo::from)
                .collect(Collectors.toList());
    }

    private List<EndDeviceGroup> endDevicesFromIds(List<Long> ids) {
        return ids.stream()
                .map(this.meteringGroupsService::findEndDeviceGroup)
                .flatMap(Functions.asStream())
                .collect(Collectors.toList());
    }

    interface SuspectsRange {
        DeviceDataQualityService.DataQualityOverviewBuilder applyTo(DeviceDataQualityService.DataQualityOverviewBuilder builder);
    }

    private class IgnoreSuspectRange implements SuspectsRange {
        @Override
        public DeviceDataQualityService.DataQualityOverviewBuilder applyTo(DeviceDataQualityService.DataQualityOverviewBuilder builder) {
            // Since we are ignoring, there is nothing to copy
            return builder;
        }
    }

    private class ExactMatch implements SuspectsRange {
        private final long match;

        private ExactMatch(long match) {
            this.match = match;
        }

        @Override
        public DeviceDataQualityService.DataQualityOverviewBuilder applyTo(DeviceDataQualityService.DataQualityOverviewBuilder builder) {
            return builder.suspects().equalTo(this.match);
        }
    }

    private class LongRange implements SuspectsRange {
        private final Range<Long> range;

        private LongRange(long from, long to) {
            this(Range.closed(from, to));
        }

        private LongRange(Range<Long> range) {
            this.range = range;
        }

        @Override
        public DeviceDataQualityService.DataQualityOverviewBuilder applyTo(DeviceDataQualityService.DataQualityOverviewBuilder builder) {
            return builder.suspects().inRange(this.range);
        }
    }

}

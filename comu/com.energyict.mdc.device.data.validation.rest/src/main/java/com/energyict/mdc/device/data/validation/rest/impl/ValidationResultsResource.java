package com.energyict.mdc.device.data.validation.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.validation.security.Privileges;
import com.energyict.mdc.device.data.validation.DeviceDataValidationService;

import com.google.common.collect.Range;
import org.json.JSONArray;
import org.json.JSONException;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/validationresults")
public class ValidationResultsResource {

    private final DeviceDataValidationService deviceDataValidationService;

    private enum Validator {
        THRESHOLDVALIDATOR("thresholdViolation"),
        MISSINGVALUESVALIDATOR("checkMissing"),
        READINGQUALITIESVALIDATOR("intervalState"),
        REGISTERINCREASEVALIDATOR("registerIncrease");

        private final String elementAbbreviation;

        public String getElementAbbreviation() {
            return elementAbbreviation;
        }

        Validator(String elementName) {
            this.elementAbbreviation = elementName;
        }


        private static final Map<String, Boolean> stringToEnum
                = new HashMap<>();

        static void refreshValidatorValues() {
            Stream.of(values()).forEach(e -> stringToEnum.put(e.elementAbbreviation, false));
        }

        static {
            refreshValidatorValues();
        }

        public static Boolean fromAbbreviation(String abbreviation) {
            return stringToEnum.get(abbreviation);
        }
    }

    @Inject
    public ValidationResultsResource(DeviceDataValidationService deviceDataValidationService) {
        this.deviceDataValidationService = deviceDataValidationService;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/devicegroups")
    @RolesAllowed({Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, Privileges.Constants.VALIDATE_MANUAL, Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE, Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public PagedInfoList getValidationResultsPerDeviceGroup(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) throws JSONException {

        Instant from = null;
        Instant to = null;
        List<Long> deviceGroups = new ArrayList<>();
        List<Long> amountOfSuspectsList = new ArrayList<>();

        JSONArray filters = new JSONArray(uriInfo.getQueryParameters().getFirst("filter"));
        for (int i = 0; i < filters.length(); i++) {
            if (filters.getJSONObject(i).get("property").equals("from")) {
                from = Instant.ofEpochMilli((Long) filters.getJSONObject(i).get("value"));
            }
            if (filters.getJSONObject(i).get("property").equals("to")) {
                to = Instant.ofEpochMilli((Long) filters.getJSONObject(i).get("value"));
            }
            if (filters.getJSONObject(i).get("property").equals("deviceGroups")) {
                List<String> groups = Stream.of(filters.getJSONObject(i).get("value")).map(Object::toString).collect(Collectors.toList());
                Arrays.asList(groups.get(0).substring(1, groups.get(0).length() - 1).split(",")).stream().forEach(value -> deviceGroups.add(Long.parseLong(value)));
            }
            if (filters.getJSONObject(i).get("property").equals("amountOfSuspects")) {
                String value = filters.getJSONObject(i).get("value").toString();
                if (value.contains("==")) {
                    amountOfSuspectsList.add(0L);
                    amountOfSuspectsList.add(Long.parseLong(value.substring(value.lastIndexOf(":") + 1, value.lastIndexOf("}"))));
                } else if (value.contains("BETWEEN")) {
                    Arrays.asList(value.substring(value.lastIndexOf(":") + 2, value.lastIndexOf("]")).split(",")).stream().forEach(amount -> amountOfSuspectsList.add(Long.parseLong(amount)));
                }
            }
            if (filters.getJSONObject(i).get("property").equals("validator")) {
                List<String> validatorList = Stream.of(filters.getJSONObject(i).get("value")).map(Object::toString).collect(Collectors.toList());
                Arrays.asList(validatorList.get(0).substring(1, validatorList.get(0).length() - 1).replace("\"", "").split(",")).stream().forEach(value -> Validator.stringToEnum.put(value, true));
            }
        }

        Range<Instant> range = from != null && to != null ? Range.closed(from, to) : Range.all();
        List<ValidationSummaryInfo> data = deviceGroups.stream().flatMap(groupId ->
                deviceDataValidationService
                        .getValidationResultsOfDeviceGroup(groupId, range)
                        .stream()
                        .filter(resultAmt -> amountOfSuspectsList.isEmpty() || (amountOfSuspectsList.get(0) <= resultAmt.getDeviceValidationKpiResults().getAmountOfSuspects()
                                && resultAmt.getDeviceValidationKpiResults().getAmountOfSuspects() <= amountOfSuspectsList.get(1)))
                        .filter(validator -> Validator.stringToEnum.entrySet().stream().allMatch(a -> a.getValue() == false) ||
                                (validator.getDeviceValidationKpiResults().isThresholdValidator() && Validator.fromAbbreviation(Validator.THRESHOLDVALIDATOR.getElementAbbreviation()) ||
                                        validator.getDeviceValidationKpiResults().isMissingValuesValidator() && Validator.fromAbbreviation(Validator.MISSINGVALUESVALIDATOR.getElementAbbreviation()) ||
                                        validator.getDeviceValidationKpiResults()
                                                .isReadingQualitiesValidator() && Validator.fromAbbreviation(Validator.READINGQUALITIESVALIDATOR.getElementAbbreviation()) ||
                                        validator.getDeviceValidationKpiResults()
                                                .isRegisterIncreaseValidator() && Validator.fromAbbreviation(Validator.REGISTERINCREASEVALIDATOR.getElementAbbreviation())))
                        .map(ValidationSummaryInfo::new)).distinct().collect(Collectors.toList());
        Validator.refreshValidatorValues();
        return PagedInfoList.fromPagedList("summary", data, queryParameters);
    }

}

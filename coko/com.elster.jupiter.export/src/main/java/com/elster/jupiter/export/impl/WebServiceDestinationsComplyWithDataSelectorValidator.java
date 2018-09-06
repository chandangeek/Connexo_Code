/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.export.WebServiceDestination;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WebServiceDestinationsComplyWithDataSelectorValidator implements ConstraintValidator<WebServiceDestinationsComplyWithDataSelector, ExportTaskImpl> {
    private final DataExportService exportService;
    private String messageTemplate;

    @Inject
    public WebServiceDestinationsComplyWithDataSelectorValidator(DataExportService exportService) {
        this.exportService = exportService;
    }

    @Override
    public void initialize(WebServiceDestinationsComplyWithDataSelector annotation) {
        messageTemplate = annotation.message();
    }

    @Override
    public boolean isValid(ExportTaskImpl exportTask, ConstraintValidatorContext context) {
        Set<String> allowedServices = exportService.getExportWebServicesMatching(exportTask.getDataSelectorFactory()).stream()
                .map(DataExportWebService::getName)
                .collect(Collectors.toSet());
        if (exportTask.getDestinations().stream()
                .filter(WebServiceDestination.class::isInstance)
                .map(WebServiceDestination.class::cast)
                .flatMap(destination -> Stream.of(destination.getCreateWebServiceEndpoint(), destination.getChangeWebServiceEndpoint().orElse(null)))
                .filter(Objects::nonNull)
                .map(EndPointConfiguration::getWebServiceName)
                .allMatch(allowedServices::contains)) {
            return true;
        }
        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addPropertyNode("destinations")
                .addConstraintViolation()
                .disableDefaultConstraintViolation();
        return false;
    }
}

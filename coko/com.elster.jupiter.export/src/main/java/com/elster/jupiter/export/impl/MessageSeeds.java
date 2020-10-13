/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    NO_SUCH_READINGTYPE(1001, Keys.NO_SUCH_READINGTYPE, "Reading type {0} does not exist."),
    FIELD_CAN_NOT_BE_EMPTY(1002, Keys.FIELD_CAN_NOT_BE_EMPTY, "This field is required"),
    FIELD_SIZE_BETWEEN_MIN_AND_MAX(1003, Keys.FIELD_SIZE_BETWEEN_MIN_AND_MAX, "Field should be between {min} and {max} characters"),
    NO_SUCH_FORMATTER(1004, Keys.NO_SUCH_FORMATTER, "Formatter {0} does not exist."),
    NAME_MUST_BE_UNIQUE(1005, Keys.NAME_MUST_BE_UNIQUE, "Data export task with such name already exists"),
    ITEM_FAILED(1006, "dataexport.item.failed", "Item {0} failed to export", Level.WARNING),
    ITEM_FATALLY_FAILED(1007, "dataexport.item.fatally.failed", "Item {0} fatally failed to export"),
    ITEM_EXPORTED_SUCCESFULLY(1008, "dataexport.item.success", "Item {0} exported successfully for period {1} - {2}", Level.INFO),
    CANNOT_DELETE_WHILE_RUNNING(1009, "dataexport.cannot.delete", "Cannot delete a data export task (id = {0}) while it is running."),
    RELATIVE_PERIOD_USED(1010, "dataexport.using.relativeperiod", "Relative period is still in use by the following data export tasks: {0}"),
    FIELD_TOO_LONG(1011, Keys.FIELD_TOO_LONG, "Field mustn''t exceed {max} characters."),
    MUST_SELECT_READING_TYPE(1012, Keys.MUST_SELECT_AT_LEAST_ONE_READING_TYPE, "At least one reading type has to be selected"),
    VETO_DEVICEGROUP_DELETION(1013, "deviceGroupXstillInUseByTask", "Device group {0} is still in use by an export task."),
    NO_SUCH_SELECTOR(1014, Keys.NO_SUCH_SELECTOR, "Selector {0} does not exist"),
    FILE_IO(1015, "file.io.writing.failure", "Failure while doing IO on file {0} : {1}"),
    PARENT_BREAKING_PATH_NOT_ALLOWED(1016, Keys.PARENT_BREAKING_PATH, "Paths that navigate above parent are not allowed here"),
    INVALIDCHARS_EXCEPTION(1017, Keys.INVALIDCHARS_EXCEPTION, "Characters {0} are not allowed."),
    DUPLICATE_IMPORT_SCHEDULE(1018, Keys.DUPLICATE_EXPORT_TASK, "Name must be unique"),
    FTP_IO(1019, Keys.FTP_FAILURE, "Failure while doing IO on ftp server {0}, port {1}."),
    MISSING_INTERVAL(1020, "dataexport.item.missinginterval", "The interval(s) starting from {0} till {1} with missing data are not exported for item {2}", Level.INFO),
    MISSING_WINDOW(1021, "dataexport.item.missingwindow", "The export window starting from {0} till {1} with missing data is not exported for item {2}", Level.INFO),

    SUSPECT_INTERVAL(1022, "dataexport.item.suspectinterval", "The interval(s) starting from {0} till {1} with suspect/not validated data are not exported for item {2}", Level.INFO),
    SUSPECT_WINDOW(1023, "dataexport.item.suspectwindow", "The export window starting from {0} till {1} with suspect/not validated data is not exported for item {2}", Level.INFO),
    MUST_SELECT_EVENT_TYPE(1024, Keys.MUST_SELECT_AT_LEAST_ONE_EVENT_TYPE, "At least one event type has to be selected"),
    DATA_EXPORTED_TO(1025, "dataexport.dataexportedto", "Data exported to {0}.", Level.INFO),
    DATA_MAILED_TO(1026, "dataexport.datamailedto", "Data exported to {0} with attachment(s) {1}.", Level.INFO),
    SOME_DEVICES_HAVE_NONE_OF_THE_SELECTED_READINGTYPES(1027, "dataexport.device.mismatch", "Some devices of device group {0} don''t contain the selected reading type(s) that have to be exported.", Level.INFO),
    EXPORT_PERIOD_COVERS_FUTURE(1029, "dataexport.exportwindow.overlapsfuture", "The export window {0} overlaps with the future. As a result the exported data is incomplete.", Level.WARNING),
    NO_DATA_TOEXPORT(1030, "dataexport.nodata", "There is no data to export.", Level.INFO),

    MAIL_DESTINATION_FAILED(1031, "dataexport.mailDestinationFailed", "Failed to export to mail destination {0}, due to {1}", Level.SEVERE),
    FILE_DESTINATION_FAILED(1032, "dataexport.fileDestinationFailed", "Failed to export to file destination {0}, due to {1}", Level.SEVERE),
    FTP_DESTINATION_FAILED(1033, "dataexport.ftpDestinationFailed", "Failed to export to ftp(s) destination {0}, due to {1}", Level.SEVERE),
    FTP_DESTINATION_CREATE_FOLDER_FAILED(1034, "dataexport.ftpFolderCreationFailed", "failure creating the parent folder of the export file, due to {1}", Level.SEVERE),

    DEFAULT_MESSAGE_EXPORT_FAILED(1035, "dataexport.exportFailed", "Failed to export, due to {0}", Level.SEVERE),
    SOME_USAGEPOINTS_HAVE_NONE_OF_THE_SELECTED_READINGTYPES(1036, "dataexport.usagepoint.mismatch", "Some usage points of usage point group {0} don''t contain the selected reading type(s) that have to be exported.", Level.WARNING),
    VETO_USAGEPOINTGROUP_DELETION(1037, "usagePointGroupXstillInUseByTask", "Usage point group {0} is still in use by an export task"),
    USAGE_POINT_MISSING_WINDOW(1038, "dataexport.usagepoint.missingwindow", "The export window starting from {0} till {1} with missing data is not exported for usage point {2}", Level.INFO),

    NO_WEBSERVICE_FOUND(1039, "webservice.export.not.found", "No data export web service is found for endpoint ''{0}''."),
    NO_CPS_FOUND(1040, "service.call.cps.not.found", "No active custom attribute set is found with id ''{0}''."),
    NO_CPS_VALUES_FOUND(1041, "service.call.cps.values.not.found", "No properties are kept for web service call {0}."),
    WEB_SERVICE_EXPORT_FAILURE(1042, "webservice.export.failure", "Failure while exporting data via web service: {0}"),
    WEB_SERVICE_EXPORT_NOT_CONFIRMED(1043, "webservice.export.not.confirmed", "Data export via web service isn''t confirmed for service call {0}. {1}"),
    WEB_SERVICE_EXPORT_NO_CONFIRMATION(1044, "webservice.export.no.confirmation", "No data export confirmation has been received in the configured timeout."),
    WEB_SERVICE_EXPORT_UNEXPECTED_STATE(1045, "webservice.export.unexpected.state", "Unexpected state of the service call: {0}."),
    WEB_SERVICE_EXPORT_WAITING_FAILURE(1046, "webservice.export.waiting.failure", "Failure while waiting for data export confirmation: {0}"),
    WEB_SERVICE_EXPORT_NO_ERROR_MESSAGE(1047, "webservice.export.no.error.message", "Received error code, but no error has been provided."),

    NULL_FORMATTER_WITH_FILE_DESTINATIONS(1048, Keys.NULL_FORMATTER_WITH_FILE_DESTINATIONS, "Not applicable(for 'Web service' destination) formatter is supported only by web service destination."),
    NO_CHANGE_ENDPOINT_FOR_UPDATED_DATA(1049, Keys.NO_CHANGE_ENDPOINT_FOR_UPDATED_DATA,
            "Web service endpoint for updated data should be specified on web service destinations if updated data is exported."),
    ENDPOINT_IS_USED_BY_EXPORT_TASK(1050, "EndpointIsUsedByExportTask", "Web service endpoint {0} is still in use by an export task."),
    BAD_ENDPOINTS_FOR_DATA_SELECTOR(1051, Keys.BAD_ENDPOINTS_FOR_DATA_SELECTOR, "Endpoints specified on web service destinations mismatch the data selector."),
    ITEM_DOES_NOT_HAVE_CREATED_DATA_FOR_EXPORT_WINDOW(1052, "dataexport.item.created.nodata", "Item {0} doesn''t contain created data for the selected export window.", Level.INFO),
    ITEM_DOES_NOT_HAVE_CHANGED_DATA_FOR_UPDATE_WINDOW(1053, "dataexport.item.changed.nodata", "Item {0} doesn''t contain changed data for the selected update window.", Level.INFO),
    OCCURRENCE_HAS_BEEN_SET_TO_FAILED(1054, "occurrence.has.been.set.to.failed", "The status of the occurrence has been manually set to failed.", Level.INFO),

    DATA_SENDING_FAILED_ALL_DATA_SOURCES(2000, "data.sending.failed.all.data.sources", "Failed to export all data to one or more destinations."),
    DATA_SENDING_FAILED_SPECIFIC_DATA_SOURCES(2001, "data.sending.failed.specific.data.sources", "Failed to export {0} data source(s) to one or more destinations: {1}."),
    WEB_SERVICE_EXPORT_NO_SERVICE_CALL(2002, "webservice.export.no.service.call", "Couldn''t find tracking service call for data source {0}."),

    PAIRED_EXPORT_TASK_ALREADY_PAIRED(2100, "paired.export.task.already.paired", "Data export task ''{0}'' is already paired with ''{1}''."),
    PAIRED_EXPORT_TASK_CANNOT_BE_PAIRED_WITH_ITSELF(2101, "paired.export.task.cannot.be.paired.with.itself", "Can''t pair data export task with itself."),
    PAIRED_EXPORT_TASK_HAS_NO_DATA_SOURCES(2102, "paired.export.task.has.no.sources", "Can''t pair data export task ''{0}'' that has no data sources."),
    PAIRED_EXPORT_TASK_HAS_DIFFERENT_SELECTOR(2103, "paired.export.task.has.different.selector", "Can''t pair data export tasks with different data selector types."),
    PAIRED_EXPORT_TASK_HAS_DIFFERENT_APPLICATION(2104, "paired.export.task.has.different.application", "Can''t pair data export tasks from different applications."),
    PAIRED_EXPORT_TASK_DOES_NOT_HAVE_MATCHING_READING_TYPES(2105, "paired.task.does.not.have.matching.reading.types", "Can''t pair data export tasks without matching reading types.");

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, Level.SEVERE);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return DataExportService.COMPONENTNAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    public void log(Logger logger, Thesaurus thesaurus, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args));
    }

    public void log(Logger logger, Thesaurus thesaurus, Throwable t, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args), t);
    }

    public interface Keys {
        String NO_SUCH_READINGTYPE = "NoSuchReadingType";
        String FIELD_CAN_NOT_BE_EMPTY = "FieldCanNotBeEmpty";
        String MUST_SELECT_AT_LEAST_ONE_READING_TYPE = "MustHaveReadingTypes";
        String MUST_SELECT_AT_LEAST_ONE_EVENT_TYPE = "MustHaveEventTypes";
        String FIELD_SIZE_BETWEEN_MIN_AND_MAX = "FieldSizeBetweenMinAndMax";
        String NO_SUCH_FORMATTER = "NoSuchFormatter";
        String NAME_MUST_BE_UNIQUE = "NameMustBeUnique";
        String NO_SUCH_SELECTOR = "NoSuchSelector";
        String PARENT_BREAKING_PATH = "path.parent.breaking.disallowed";
        String INVALIDCHARS_EXCEPTION = "InvalidChars";
        String DUPLICATE_EXPORT_TASK = "exporttask.duplicate.name";
        String FTP_FAILURE = "ftp.io.writing.failure";
        String NULL_FORMATTER_WITH_FILE_DESTINATIONS = "NullFormatterWithFileDestinations";
        String NO_CHANGE_ENDPOINT_FOR_UPDATED_DATA = "NoChangeEndpointForUpdatedData";
        String BAD_ENDPOINTS_FOR_DATA_SELECTOR = "BadEndpointsForDataSelector";
        String FIELD_TOO_LONG = "FieldTooLong";
    }
}


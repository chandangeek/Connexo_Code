package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.metering.imports.impl.MessageSeeds;
import com.elster.jupiter.metering.imports.impl.UsagePointFileImporterFactory;
import com.elster.jupiter.metering.imports.impl.UsagePointFileImporterMessageHandler;
import com.elster.jupiter.metering.imports.impl.usagepoint.usagepointimport.UsagePointsImporterFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.exception.MessageSeed;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

@Component(name = "com.elster.jupiter.metering.imports.impl.usagepoint.translations", service = {TranslationKeyProvider.class, MessageSeedProvider.class}, immediate = true)
public class Translations implements TranslationKeyProvider, MessageSeedProvider {

    @Override
    public String getComponentName() {
        return FileImportService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(Labels.values());
    }

    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    public enum Labels implements TranslationKey {
        USAGEPOINT_FILE_IMPORTER(UsagePointsImporterFactory.NAME, "Usage Point Importer"),
        USAGEPOINT_MESSAGE_SUBSCRIBER(UsagePointFileImporterMessageHandler.SUBSCRIBER_NAME, "Handle usage point import"),
        // Properties translations
        DATA_IMPORTER_DELIMITER("delimiter", "Delimiter"),
        DATA_IMPORTER_DELIMITER_DESCRIPTION("delimiter", "The character that delimits the values for the different properties to import"),
        DATA_IMPORTER_DATE_FORMAT("dateFormat", "Date format"),
        DATA_IMPORTER_DATE_FORMAT_DESCRIPTION("dateFormat.description", "The format that is used for date properties"),
        DATA_IMPORTER_TIMEZONE("timeZone", "Time zone"),
        DATA_IMPORTER_TIMEZONE_DESCRIPTION("timeZone.description", "The unique identifier of the Timezone in which date properties are specified"),
        DATA_IMPORTER_NUMBER_FORMAT("numberFormat", "Number format"),
        DATA_IMPORTER_NUMBER_FORMAT_DESCRIPTION("numberFormat.description", "The format that is used for numerical properties"),

        DATA_IMPORTER_SUBSCRIBER(UsagePointFileImporterMessageHandler.SUBSCRIBER_NAME, "Handle data import"),

        IMPORT_RESULT_NO_USAGEPOINTS_WERE_PROCESSED("ImportResultNoDevicesWereProcessed", "Failed to complete, no usagepoints have been processed."),
        IMPORT_RESULT_FAIL("ImportResultFail", "Failed to complete. {0} devices processed successfully."),
        IMPORT_RESULT_FAIL_WITH_ERRORS("ImportResultFailWithErrors", "Failed to complete. {0} devices processed successfully, {1} devices skipped due to errors."),
        IMPORT_RESULT_FAIL_WITH_WARN_AND_ERRORS("ImportResultFailWithWarnAndErrors", "Failed to complete. {0} devices processed successfully of which {1} devices contain a note, {2} devices skipped due to errors."),
        IMPORT_RESULT_FAIL_WITH_WARN("ImportResultFailWithWarn", "Failed to complete. {0} devices processed successfully of which {1} devices contain a note."),
        IMPORT_RESULT_SUCCESS("ImportResultSuccess", "Finished successfully. {0} device(s) processed successfully."),
        IMPORT_RESULT_SUCCESS_WITH_ERRORS("ImportResultSuccessWithErrors", "Finished successfully with (some) failures. {0} devices processed successfully, {1} devices skipped due to errors. "),
        IMPORT_RESULT_SUCCESS_WITH_WARN_AND_ERRORS("ImportResultSuccessWithWarnAndErrors", "Finished successfully with (some) failures and notes. {0} devices processed successfully of which {1} devices contain a note, {2} devices skipped due to errors."),
        IMPORT_RESULT_SUCCESS_WITH_WARN("ImportResultSuccessWithWarn", "Finished successfully with (some) note. {0} devices processed successfully of which {1} devices contain a note."),
        IMPORT_DEFAULT_PROCESSOR_ERROR_TEMPLATE("ImportDefaultProcessorErrorPrefix", "Can''t process line {0}: {1}"),
                ;
        private final String key;
        private final String defaultFormat;

        Labels(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }

    }

}
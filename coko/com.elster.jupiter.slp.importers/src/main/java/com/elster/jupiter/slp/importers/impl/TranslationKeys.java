/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

@Component(name = "com.elster.jupiter.slp.importers.impl.translations", service = {TranslationKeyProvider.class, MessageSeedProvider.class}, immediate = true)
public class TranslationKeys implements TranslationKeyProvider, MessageSeedProvider {

    @Override
    public String getComponentName() {
        return SyntheticLoadProfileFileImporterMessageHandler.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(Labels.values());
    }

    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    public enum Labels implements TranslationKey {
        CORRECTION_FACTOR_FILE_IMPORTER("correctionFactorImporter", "Synthetic load profiles Importer"),
        SLP_MESSAGE_SUBSCRIBER(SyntheticLoadProfileFileImporterMessageHandler.SUBSCRIBER_NAME, "Handle synthetic load profile import"),
        // Properties translations
        DATA_IMPORTER_DELIMITER("delimiter", "Delimiter"),
        DATA_IMPORTER_DELIMITER_DESCRIPTION("delimiter", "The character that delimits the values for the different properties to import"),
        DATA_IMPORTER_DATE_FORMAT("dateFormat", "Date format"),
        DATA_IMPORTER_DATE_FORMAT_DESCRIPTION("dateFormat.description", "The format that is used for date properties"),
        DATA_IMPORTER_TIMEZONE("timeZone", "Time zone"),
        DATA_IMPORTER_TIMEZONE_DESCRIPTION("timeZone.description", "The unique identifier of the Timezone in which date properties are specified"),
        DATA_IMPORTER_NUMBER_FORMAT("numberFormat", "Number format"),
        DATA_IMPORTER_NUMBER_FORMAT_DESCRIPTION("numberFormat.description", "The format that is used for numerical properties"),

        IMPORT_DEFAULT_PROCESSOR_ERROR_TEMPLATE("ImportDefaultProcessorErrorPrefix", "Can''t process line {0}: {1}"),

        CF_IMPORT_RESULT_FAILED("CFImportResultFailed", "Import failed."),
        CF_IMPORT_RESULT_SUCCESS("CFImportResultSuccess", "Import finished successfully. {0} interval(s) are imported for {1}.");


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

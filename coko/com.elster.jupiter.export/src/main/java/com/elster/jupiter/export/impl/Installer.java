package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.security.Privileges;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.*;

class Installer {

    public static final String DESTINATION_NAME = DataExportServiceImpl.DESTINATION_NAME;
    public static final String SUBSCRIBER_NAME = DataExportServiceImpl.SUBSCRIBER_NAME;
    public static final String RELATIVE_PERIOD_CATEGORY = "relativeperiod.category.dataExport";

    private final DataModel dataModel;
    private final MessageService messageService;
    private final TimeService timeService;
    private final Thesaurus thesaurus;
    private final UserService userService;

    private DestinationSpec destinationSpec;

    Installer(DataModel dataModel, MessageService messageService, TimeService timeService, Thesaurus thesaurus, UserService userService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.timeService = timeService;
        this.thesaurus = thesaurus;
        this.userService = userService;
    }

    public DestinationSpec getDestinationSpec() {
        return destinationSpec;
    }

    void install() {
        ExceptionCatcher.executing(
                this::installDataModel,
                this::createDestinationAndSubscriber,
                this::createRelativePeriodCategory,
                this::createTranslations,
                this::createRelativePeriods
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
    }

    private void createTranslations() {
        List<Translation> translations = new ArrayList<>(/*MessageSeeds.values().length*/);
        NlsKey categoryKey = SimpleNlsKey.key(DataExportService.COMPONENTNAME, Layer.DOMAIN, RELATIVE_PERIOD_CATEGORY);
        Translation translation = SimpleTranslation.translation(categoryKey, Locale.ENGLISH, "Data Export");
        translations.add(translation);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            NlsKey nlsKey = SimpleNlsKey.key(DataExportService.COMPONENTNAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
            translations.add(SimpleTranslation.translation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
        }
        for (DataExportStatus status : DataExportStatus.values()) {
            NlsKey statusKey = SimpleNlsKey.key(DataExportService.COMPONENTNAME, Layer.DOMAIN, status.toString());
            Translation statusTranslation = SimpleTranslation.translation(statusKey, Locale.ENGLISH, status.toString());
            translations.add(statusTranslation);
        }
        translations.add(SimpleTranslation.translation(SimpleNlsKey.key(DataExportService.COMPONENTNAME, Layer.DOMAIN, StandardDataSelectorFactory.class.getName()), Locale.ENGLISH, DataExportService.STANDARD_DATA_SELECTOR));

        NlsKey statusKey = SimpleNlsKey.key(DataExportService.COMPONENTNAME, Layer.DOMAIN, SUBSCRIBER_NAME);
        Translation statusTranslation = SimpleTranslation.translation(statusKey, Locale.ENGLISH, DataExportServiceImpl.SUBSCRIBER_DISPLAYNAME);
        translations.add(statusTranslation);

        thesaurus.addTranslations(translations);
    }

    private void createRelativePeriodCategory() {
        timeService.createRelativePeriodCategory(RELATIVE_PERIOD_CATEGORY);
    }

    private void createDestinationAndSubscriber() {
        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        destinationSpec = queueTableSpec.createDestinationSpec(DESTINATION_NAME, 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe(SUBSCRIBER_NAME);
    }

    private void installDataModel() {
        dataModel.install(true, true);
    }

    private RelativePeriodCategory getCategory() {
        return timeService.findRelativePeriodCategoryByName(RELATIVE_PERIOD_CATEGORY).orElseThrow(IllegalArgumentException::new);
    }

    private void createRelativePeriods() {
        RelativePeriodCategory category = getCategory();

        EnumSet.of(LAST_7_DAYS, PREVIOUS_MONTH, PREVIOUS_WEEK, THIS_MONTH, THIS_WEEK, TODAY, YESTERDAY).stream()
                .forEach(definition -> {
                    RelativePeriod relativePeriod = timeService.findRelativePeriodByName(definition.getPeriodName()).orElseThrow(IllegalArgumentException::new);
                    relativePeriod.addRelativePeriodCategory(category);
                });

    }

}

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.MessageSeeds;
import com.elster.jupiter.estimation.security.Privileges;
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
import com.elster.jupiter.tasks.TaskStatus;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.LAST_7_DAYS;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.PREVIOUS_MONTH;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.PREVIOUS_WEEK;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_MONTH;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_WEEK;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.THIS_YEAR;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.TODAY;
import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.YESTERDAY;

class InstallerImpl {

    public static final String DESTINATION_NAME = EstimationServiceImpl.DESTINATION_NAME;
    public static final String SUBSCRIBER_NAME = EstimationServiceImpl.SUBSCRIBER_NAME;
    public static final String RELATIVE_PERIOD_CATEGORY = "relativeperiod.category.estimation";

    private final DataModel dataModel;
    private final MessageService messageService;
    private final TimeService timeService;
    private final Thesaurus thesaurus;
    private final UserService userService;

    private DestinationSpec destinationSpec;

    InstallerImpl(DataModel dataModel, MessageService messageService, Thesaurus thesaurus, UserService userService, TimeService timeService/*, Thesaurus thesaurus*/) {
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
                this::createPrivileges,
                this::createRelativePeriodCategory,
                this::createTranslations,
                this::createRelativePeriods
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
    }

    private void createTranslations() {
        List<Translation> translations = new ArrayList<>();
        NlsKey categoryKey = SimpleNlsKey.key(EstimationService.COMPONENTNAME, Layer.DOMAIN, RELATIVE_PERIOD_CATEGORY);
        Translation translation = SimpleTranslation.translation(categoryKey, Locale.ENGLISH, "Estimation");
        translations.add(translation);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            NlsKey nlsKey = SimpleNlsKey.key(EstimationService.COMPONENTNAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
            translations.add(SimpleTranslation.translation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
        }
        for (TaskStatus status : TaskStatus.values()) {
            NlsKey statusKey = SimpleNlsKey.key(EstimationService.COMPONENTNAME, Layer.DOMAIN, status.toString());
            Translation statusTranslation = SimpleTranslation.translation(statusKey, Locale.ENGLISH, status.toString());
            translations.add(statusTranslation);
        }

        NlsKey statusKey = SimpleNlsKey.key(EstimationService.COMPONENTNAME, Layer.DOMAIN, SUBSCRIBER_NAME);
        Translation statusTranslation = SimpleTranslation.translation(statusKey, Locale.ENGLISH, EstimationServiceImpl.SUBSCRIBER_DISPLAYNAME);
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

    private void createPrivileges() {
        this.userService.createResourceWithPrivileges("SYS", "estimation.estimations", "estimation.estimations.description", new String[]
                {
                        Privileges.ADMINISTRATE_ESTIMATION_CONFIGURATION,
                        Privileges.VIEW_ESTIMATION_CONFIGURATION,
                        Privileges.UPDATE_ESTIMATION_CONFIGURATION,
                        Privileges.UPDATE_SCHEDULE_ESTIMATION_TASK,
                        Privileges.RUN_ESTIMATION_TASK,
                        Privileges.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE,
                        Privileges.FINE_TUNE_ESTIMATION_CONFIGURATION_ON_DEVICE_CONFIGURATION,
                });
    }


    private RelativePeriodCategory getCategory() {
        return timeService.findRelativePeriodCategoryByName(RELATIVE_PERIOD_CATEGORY).orElseThrow(IllegalArgumentException::new);
    }

    private void createRelativePeriods() {
        RelativePeriodCategory category = getCategory();

        EnumSet.of(LAST_7_DAYS, PREVIOUS_MONTH, PREVIOUS_WEEK, THIS_MONTH, THIS_WEEK, THIS_YEAR, TODAY, YESTERDAY).stream()
                .forEach(definition -> {
                    RelativePeriod relativePeriod = timeService.findRelativePeriodByName(definition.getPeriodName()).orElseThrow(IllegalArgumentException::new);
                    relativePeriod.addRelativePeriodCategory(category);
                });

    }

}

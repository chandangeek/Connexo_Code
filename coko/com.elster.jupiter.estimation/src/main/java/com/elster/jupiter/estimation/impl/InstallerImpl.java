package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.MessageSeeds;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.TaskStatus;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.time.DefaultRelativePeriodDefinition.*;

class InstallerImpl {

    public static final String DESTINATION_NAME = EstimationServiceImpl.DESTINATION_NAME;
    public static final String SUBSCRIBER_NAME = EstimationServiceImpl.SUBSCRIBER_NAME;
    public static final String RELATIVE_PERIOD_CATEGORY = "relativeperiod.category.estimation";
    private static final String ESTIMATIONS_PRIVILEGE_CATEGORY_NAME = "estimation.estimations";
    private static final String ESTIMATIONS_PRIVILEGE_CATEGORY_DESCRIPTION = "estimation.estimations.description";

    private final DataModel dataModel;
    private final MessageService messageService;
    private final TimeService timeService;
    private final Thesaurus thesaurus;
    private final UserService userService;
    private final EventService eventService;

    private DestinationSpec destinationSpec;

    InstallerImpl(DataModel dataModel, MessageService messageService, Thesaurus thesaurus, UserService userService, TimeService timeService, EventService eventService/*, Thesaurus thesaurus*/) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.timeService = timeService;
        this.thesaurus = thesaurus;
        this.userService = userService;
        this.eventService = eventService;
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
                this::createRelativePeriods,
                this::createEventTypes
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
    }

    private void createTranslations() {
        List<Translation> translations =
                Stream.of(
                        fromMessageSeeds(),
                        fromTaskStatus(),
                        Stream.of(relativePeriodCategoryTranslation()),
                        Stream.of(statusTranslation())
                )
                        .flatMap(Function.identity())
                        .collect(Collectors.toList());

        thesaurus.addTranslations(translations);
    }

    private Stream<Translation> fromMessageSeeds() {
        return Arrays.stream(MessageSeeds.values())
                .map(this::toTranslation);
    }

    private Stream<Translation> fromTaskStatus() {
        return Arrays.stream(TaskStatus.values())
                .map(this::toTranslation);
    }

    private Translation statusTranslation() {
        NlsKey statusKey = SimpleNlsKey.key(EstimationService.COMPONENTNAME, Layer.DOMAIN, SUBSCRIBER_NAME);
        return SimpleTranslation.translation(statusKey, Locale.ENGLISH, EstimationServiceImpl.SUBSCRIBER_DISPLAYNAME);
    }

    private Translation toTranslation(TranslationKey translationKey) {
        NlsKey nlsKey = SimpleNlsKey.key(EstimationService.COMPONENTNAME, Layer.DOMAIN, translationKey.getKey()).defaultMessage(translationKey.getDefaultFormat());
        return SimpleTranslation.translation(nlsKey, Locale.ENGLISH, translationKey.getDefaultFormat());
    }

    private Translation relativePeriodCategoryTranslation() {
        NlsKey categoryKey = SimpleNlsKey.key(EstimationService.COMPONENTNAME, Layer.DOMAIN, RELATIVE_PERIOD_CATEGORY);
        return SimpleTranslation.translation(categoryKey, Locale.ENGLISH, "Estimation");
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

        EnumSet.of(LAST_7_DAYS, PREVIOUS_MONTH, PREVIOUS_WEEK, THIS_MONTH, THIS_WEEK, THIS_YEAR, TODAY, YESTERDAY).stream()
                .forEach(definition -> {
                    RelativePeriod relativePeriod = timeService.findRelativePeriodByName(definition.getPeriodName()).orElseThrow(IllegalArgumentException::new);
                    relativePeriod.addRelativePeriodCategory(category);
                });

    }
    
    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(eventService);
        }
    }
}

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.exception.ExceptionCatcher;

class InstallerImpl {

    public static final String DESTINATION_NAME = EstimationServiceImpl.DESTINATION_NAME;
    public static final String SUBSCRIBER_NAME = EstimationServiceImpl.SUBSCRIBER_NAME;
    public static final String RELATIVE_PERIOD_CATEGORY = "relativeperiod.category.dataExport";

    private final DataModel dataModel;
    private final MessageService messageService;
//    private final TimeService timeService;
//    private final Thesaurus thesaurus;
//    private final UserService userService;

    private DestinationSpec destinationSpec;

    InstallerImpl(DataModel dataModel, MessageService messageService/* ,TimeService timeService, Thesaurus thesaurus, UserService userService*/) {
        this.dataModel = dataModel;
        this.messageService = messageService;
//        this.timeService = timeService;
//        this.thesaurus = thesaurus;
//        this.userService = userService;
    }

    public DestinationSpec getDestinationSpec() {
        return destinationSpec;
    }

    void install() {
        ExceptionCatcher.executing(
                this::installDataModel,
                this::createDestinationAndSubscriber
//                this::createRelativePeriodCategory,
//                this::createTranslations,
//                this::createPrivileges,
//                this::createRelativePeriods
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
    }

//    private void createTranslations() {
//        List<Translation> translations = new ArrayList<>(/*MessageSeeds.values().length*/);
//        NlsKey categoryKey = SimpleNlsKey.key(DataExportService.COMPONENTNAME, Layer.DOMAIN, RELATIVE_PERIOD_CATEGORY);
//        Translation translation = SimpleTranslation.translation(categoryKey, Locale.ENGLISH, "Data Export");
//        translations.add(translation);
//        for (MessageSeeds messageSeed : MessageSeeds.values()) {
//            NlsKey nlsKey = SimpleNlsKey.key(DataExportService.COMPONENTNAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
//            translations.add(SimpleTranslation.translation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
//        }
//        for (DataExportStatus status : DataExportStatus.values()) {
//            NlsKey statusKey = SimpleNlsKey.key(DataExportService.COMPONENTNAME, Layer.DOMAIN, status.toString());
//            Translation statusTranslation = SimpleTranslation.translation(statusKey, Locale.ENGLISH, status.toString());
//            translations.add(statusTranslation);
//        }
//
//        NlsKey statusKey = SimpleNlsKey.key(DataExportService.COMPONENTNAME, Layer.DOMAIN, SUBSCRIBER_NAME);
//        Translation statusTranslation = SimpleTranslation.translation(statusKey, Locale.ENGLISH, DataExportServiceImpl.SUBSCRIBER_DISPLAYNAME);
//        translations.add(statusTranslation);
//
//        thesaurus.addTranslations(translations);
//    }

//    private void createRelativePeriodCategory() {
//        timeService.createRelativePeriodCategory(RELATIVE_PERIOD_CATEGORY);
//    }

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

//    private void createPrivileges() {
//        userService.createResourceWithPrivileges("SYS", "dataExportTask.dataExportTasks", "dataExportTask.dataExportTasks.description", new String[]
//                {Privileges.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.VIEW_DATA_EXPORT_TASK, Privileges.UPDATE_DATA_EXPORT_TASK, Privileges.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.RUN_DATA_EXPORT_TASK});
//    }

//    private List<RelativePeriodCategory> getCategories() {
//        List<RelativePeriodCategory> categories = new ArrayList<>();
//        categories.add(timeService.findRelativePeriodCategoryByName(RELATIVE_PERIOD_CATEGORY).orElseThrow(IllegalArgumentException::new));
//        return categories;
//    }

//    private void createRelativePeriods() {
//        List<RelativePeriodCategory> categories = getCategories();
//
//        Arrays.stream(DefaultRelativePeriodDefinition.values())
//                .forEach(definition -> definition.create(timeService, categories));
//
//    }

}

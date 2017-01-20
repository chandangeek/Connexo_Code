Ext.define('Dxp.controller.Tasks', {
    extend: 'Ext.app.Controller',

    requires: [
        'Dxp.privileges.DataExport',
        'Uni.form.field.Password',
        'Uni.util.Application'
    ],

    views: [
        'Dxp.view.tasks.Add',
        'Dxp.view.tasks.Setup',
        'Dxp.view.tasks.Details',
        'Dxp.view.tasks.History',
        'Dxp.view.tasks.HistoryPreview',
        'Dxp.view.tasks.HistoryPreviewForm',
        'Dxp.view.datasources.Setup',
        'Dxp.view.tasks.AddReadingTypesToTaskSetup',
        'Dxp.view.tasks.AddReadingTypesToTaskBulk',
        'Uni.form.field.DateTime',
        'Dxp.view.tasks.AddDestination',
        'Dxp.view.tasks.EventTypeWindow'
    ],

    stores: [
        'Dxp.store.DeviceGroups',
        'Dxp.store.UsagePointGroups',
        'Dxp.store.DaysWeeksMonths',
        'Dxp.store.ExportPeriods',
        'Dxp.store.FileFormatters',
        'Dxp.store.ReadingTypes',
        'Dxp.store.DeviceTypes',
        'Dxp.store.DeviceDomains',
        'Dxp.store.DeviceSubDomains',
        'Dxp.store.DeviceEventOrActions',
        'Dxp.store.DataExportTasks',
        'Dxp.store.DataExportTasksHistory',
        'Dxp.store.ReadingTypesForTask',
        'Dxp.store.EventTypesForTask',
        'Dxp.store.DataSources',
        'Dxp.store.LoadedReadingTypes',
        'Dxp.store.AdaptedReadingsForBulk',
        'Dxp.store.UnitsOfMeasure',
        'Dxp.store.TimeOfUse',
        'Dxp.store.MetrologyConfigurations',
        'Dxp.store.MetrologyPurposes',
        'Dxp.store.Intervals',
        'Dxp.store.Clipboard',
        'Dxp.store.DataSelectors',
        'Dxp.store.UpdateWindows',
        'Dxp.store.UpdateTimeframes',
        'Dxp.store.SelectedReadingTypes',
        'Dxp.store.Status',
        'Dxp.store.DataExportTaskFilter'
    ],

    models: [
        'Dxp.model.DeviceGroup',
        'Dxp.model.DayWeekMonth',
        'Dxp.model.ExportPeriod',
        'Dxp.model.FileFormatter',
        'Dxp.model.SchedulePeriod',
        'Dxp.model.ReadingType',
        'Dxp.model.EndDeviceEventTypePart',
        'Dxp.model.DataExportTask',
        'Dxp.model.DataExportTaskHistory',
        'Dxp.model.AddDataExportTaskForm',
        'Dxp.model.ReadingTypeForAddTaskGrid',
        'Dxp.model.DataSource',
        'Dxp.model.HistoryFilter',
        'Dxp.model.UnitOfMeasure',
        'Dxp.model.TimeOfUse',
        'Dxp.model.Interval',
        'Dxp.model.DataSelector',
        'Dxp.model.DataProcessor',
        'Dxp.model.StandardDataSelector',
        'Dxp.model.Destination'

    ],


    refs: [
        {
            ref: 'page',
            selector: 'data-export-tasks-setup'
        },
        {
            ref: 'addPage',
            selector: 'data-export-tasks-add'
        },
        {
            ref: 'historyPreviewForm',
            selector: 'dxp-tasks-history-preview-form'
        },
        {
            ref: 'addDestinationPage',
            selector: 'data-export-add-destination'
        },
        {
            ref: 'detailsPage',
            selector: 'data-export-tasks-details'
        },
        {
            ref: 'history',
            selector: 'data-export-tasks-history'
        },
        {
            ref: 'dataSourcesPage',
            selector: 'data-sources-setup'
        },
        {
            ref: 'addReadingTypesSetup',
            selector: '#AddReadingTypesToTaskSetup'
        },
        {
            ref: 'eventTypesGrid',
            selector: '#eventTypesGridPanel'
        },
        {
            ref: 'noEventTypesLabel',
            selector: '#noEventTypesLabel'
        },
        {
            ref: 'eventTypeWindow',
            selector: '#eventTypeWindow'
        },
        {
            ref: 'addReadingTypesToTaskBulk',
            selector: 'AddReadingTypesToTaskBulk'
        }
    ],

    fromDetails: false,
    fromEdit: false,
    taskModel: null,
    taskId: null,
    counter: 0,

    readingTypesArray: null,
    eventTypesArray: null,
    destinationsArray: [],

    destinationToEdit: null,
    destinationIndexToEdit: -1,

    requiredFieldText: Uni.I18n.translate('dataExport.requiredField', 'DES', 'This field is required'),

    deviceTypesStore: null,
    deviceDomainsStore: null,
    deviceSubDomainsStore: null,
    deviceEventOrActionsStore: null,
    comboBoxValueForAll: -1,

    init: function () {
        this.control({
            'data-export-tasks-add': {
                render: this.populateStores
            },
            'data-export-tasks-add #recurrence-trigger': {
                change: this.recurrenceChange
            },
            'data-export-tasks-add #start-on': {
                change: this.fillScheduleGridOrNot
            },
            'data-export-tasks-add #recurrence-number': {
                change: this.fillScheduleGridOrNot
            },
            'data-export-tasks-add #recurrence-type': {
                change: this.fillScheduleGridOrNot
            },
            'data-export-tasks-add #export-period-combo': {
                change: this.fillScheduleGridOrNot
            },
            'data-export-tasks-add #update-window': {
                change: this.fillScheduleGridOrNot
            },
            'data-export-tasks-add #updated-data-trigger': {
                change: this.updatedDataChange
            },
            'data-export-tasks-add #add-export-task-button': {
                click: this.addTask
            },
            'data-export-tasks-add #export-updated': {
                change: this.exportUpdatedEnableDisabled
            },
            'data-export-tasks-add #add-destination-button': {
                click: this.showAddDestination
            },
            'data-export-add-destination #save-destination-button': {
                click: this.addDestinationToGrid
            },
            'data-export-add-destination #cancel-add-destination-link': {
                click: this.cancelAddDestination
            },
            'data-export-tasks-add #file-formatter-combo': {
                change: this.updateProperties
            },
            'data-export-tasks-add #data-selector-combo': {
                change: this.updateDataSelectorProperties
            },
            'data-export-add-destination #destination-methods-combo': {
                change: this.updateDestinationAttributes
            },
            'data-export-tasks-add #addReadingTypeButton': {
                click: this.showAddReadingGrid
            },
            'data-export-tasks-add #addEventTypeButton': {
                click: this.showAddEventTypePopUp
            },
            '#AddReadingTypesToTaskSetup button[name=cancel]': {
                click: this.forwardToPreviousPage
            },
            '#AddReadingTypesToTaskSetup button[name=add]': {
                click: this.addSelectedReadingTypes
            },

            'data-export-tasks-setup dxp-tasks-grid': {
                select: this.showPreview
            },
            'data-export-tasks-history dxp-tasks-history-grid': {
                select: this.showHistoryPreview
            },
            'dxp-tasks-action-menu': {
                click: this.chooseAction
            },
            'dxp-tasks-destination-action-menu': {
                click: this.chooseDestinationAction
            },
            'AddReadingTypesToTaskBulk': {
                selectionchange: this.onSelectionChange
            },
            '#addEventTypeToTask': {
                click: this.addEventTypeToTask
            },
            'AddReadingTypesToTaskSetup addReadingTypesNoItemsFoundPanel': {
                openInfoWindow: this.showSelectedReadingTypes,
                showNoFoundPanel: this.showNoFoundPanel,
                uncheckAll: this.uncheckAll
            }
        });
    },

    cancelAddDestination: function () {
        if (!this.destinationToEdit) {
            //cancel "add destination"
            this.forwardToPreviousPage();
        } else {
            //cancel "edit" destination
            //add the old one again, it was removed before the edit (edit = remove + add new)
            this.doAddDestinationToGrid();
        }
    },

    showDataExportTasks: function () {
        var me = this,
            view = Ext.widget('data-export-tasks-setup', {
                router: me.getController('Uni.controller.history.Router')
            });

        me.fromDetails = false;
        me.getApplication().fireEvent('changecontentevent', view);

        if (Dxp.privileges.DataExport.canRun()) {
            Ext.Array.each(Ext.ComponentQuery.query('#run'), function (item) {
                item.show();
            });
        }
    },

    showTaskDetailsView: function (currentTaskId) {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            router = me.getController('Uni.controller.history.Router'),
            taskModel = me.getModel('Dxp.model.DataExportTask'),
            view = Ext.widget('data-export-tasks-details', {
                router: router,
                taskId: currentTaskId
            }),
            actionsMenu = view.down('dxp-tasks-action-menu');

        me.fromDetails = true;
        me.getApplication().fireEvent('changecontentevent', view);

        mainView.setLoading();
        taskModel.load(currentTaskId, {
            success: function (record) {
                var detailsForm = view.down('dxp-tasks-preview-form'),
                    propertyForm = detailsForm.down('#task-properties-preview'),
                    selectorPropertyForm = detailsForm.down('#data-selector-properties-preview'),
                    deviceGroup = detailsForm.down('#data-selector-deviceGroup-preview'),
                    usagePointGroup = detailsForm.down('#data-selector-usage-point-group-preview'),
                    exportPeriod = detailsForm.down('#data-selector-exportPeriod-preview'),
                    readingTypes = detailsForm.down('#data-selector-readingTypes-preview'),
                    eventTypes = detailsForm.down('#data-selector-eventTypes-preview'),
                    continuousDataPreview = detailsForm.down('#continuousData-preview'),
                    dataValidation = detailsForm.down('#data-selector-validated-data'),
                    missingData = detailsForm.down('#data-selector-export-complete'),
                    updatedData = detailsForm.down('#updated-data'),
                    updatedValuesData = detailsForm.down('#updated-values');

                actionsMenu.record = record;
                Ext.suspendLayouts();
                view.down('#tasks-view-menu #tasks-view-link').setText(record.get('name'));
                me.getApplication().fireEvent('dataexporttaskload', record);
                detailsForm.loadRecord(record);
                if (record.get('status') !== 'Busy') {
                    if (record.get('status') === 'Failed') {
                        view.down('#reason-field').show();
                    }
                    if (Dxp.privileges.DataExport.canRun()) {
                        view.down('#run').show();
                    }
                }
                if (record.getDataProcessor() && record.getDataProcessor().properties().count()) {
                    propertyForm.loadRecord(record.getDataProcessor());
                }
                if ((record.getDataSelector()) && (record.getDataSelector().properties()) && (record.getDataSelector().properties().count())) {
                    selectorPropertyForm.setVisible(true);
                    deviceGroup.setVisible(false);
                    exportPeriod.setVisible(false);
                    readingTypes.setVisible(false);
                    setVisible.setVisible(false);
                    dataValidation.setVisible(false);
                    missingData.setVisible(false);
                    updatedData.setVisible(false);
                    continuousDataPreview.setVisible(false);
                    updatedValuesData.setVisible(false);
                    selectorPropertyForm.loadRecord(record.getDataSelector());

                } else {
                    switch (record.getDataSelector().get('selectorType')) {
                        case 'DEFAULT_READINGS':
                            selectorPropertyForm.setVisible(false);
                            deviceGroup.setVisible(true);
                            usagePointGroup.setVisible(false);
                            exportPeriod.setVisible(true);
                            readingTypes.setVisible(true);
                            eventTypes.setVisible(false);
                            dataValidation.setVisible(true);
                            missingData.setVisible(true);
                            updatedData.setVisible(true);
                            continuousDataPreview.setVisible(true);
                            if (record.getData().exportUpdate === 'false') {
                                updatedValuesData.setVisible(false);
                            } else {
                                updatedValuesData.setVisible(true);
                            }
                            break;
                        case 'DEFAULT_USAGE_POINT_READINGS':
                            selectorPropertyForm.setVisible(false);
                            deviceGroup.setVisible(false);
                            usagePointGroup.setVisible(true);
                            exportPeriod.setVisible(true);
                            readingTypes.setVisible(true);
                            eventTypes.setVisible(false);
                            dataValidation.setVisible(true);
                            missingData.setVisible(true);
                            updatedData.setVisible(false);
                            updatedValuesData.setVisible(false);
                            continuousDataPreview.setVisible(true);
                            break;
                        case 'DEFAULT_EVENTS':
                            selectorPropertyForm.setVisible(false);
                            deviceGroup.setVisible(false);
                            usagePointGroup.setVisible(false);
                            exportPeriod.setVisible(true);
                            continuousDataPreview.setVisible(false);
                            readingTypes.setVisible(false);
                            eventTypes.setVisible(true);
                            dataValidation.setVisible(false);
                            missingData.setVisible(false);
                            updatedData.setVisible(false);
                            updatedValuesData.setVisible(false);
                            view.down('#tasks-view-menu').removeDataSourcesMenuItem();
                            break;
                    }
                }
                Ext.resumeLayouts(true);
            },
            callback: function () {
                mainView.setLoading(false);
            }
        });
    },

    showDataExportTaskHistory: function (currentTaskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            store = me.getStore('Dxp.store.DataExportTasksHistory'),
            taskModel = me.getModel('Dxp.model.DataExportTask'),
            noSpecificExportTask = (currentTaskId === undefined),
            view;

        noSpecificExportTask ? store.getProxy().setCommonUrl() : store.getProxy().setUrl(router.arguments);

        view = Ext.widget('data-export-tasks-history', {
            router: router,
            taskId: currentTaskId,
            showExportTask: !noSpecificExportTask
        });

        me.getApplication().fireEvent('changecontentevent', view);
        store.load();

        if(!noSpecificExportTask){
            taskModel.load(currentTaskId, {
                success: function (record) {
                    me.getApplication().fireEvent('dataexporttaskload', record);
                    view.down('#tasks-view-menu  #tasks-view-link').setText(record.get('name'));
                    if (record.get('dataSelector').selectorType === 'CUSTOM') {
                        view.down('#export-period-column').hide();
                    } else {
                        view.down('#export-period-column').show();
                        if (record.get('dataSelector').selectorType === 'DEFAULT_EVENTS') {
                            view.down('#tasks-view-menu').removeDataSourcesMenuItem();
                        }
                    }
                }
            });
        }
    },

    showHistoryPreview: function (selectionModel, record) {
        var me = this,
            page = me.getHistory(),
            preview = page.down('dxp-tasks-history-preview'),
            previewForm = page.down('dxp-tasks-history-preview-form');

        if (record) {
            Ext.suspendLayouts();

            preview.setTitle(record.get('startedOn_formatted'));
            previewForm.down('displayfield[name=startedOn_formatted]').setVisible(true);
            previewForm.down('displayfield[name=finishedOn_formatted]').setVisible(true);
            previewForm.loadRecord(record);
            // preview.down('tasks-history-action-menu').record = record;

            if (record.get('status') === 'Failed') {
                previewForm.down('#reason-field').show();
            } else {
                previewForm.down('#reason-field').hide();
            }

            // TODO: Fix properties stuff.

            if (record.getTask().getDataProcessor().properties() && record.getTask().getDataProcessor().properties().count()) {
                previewForm.down('#task-properties-preview').loadRecord(record.getTask().getDataProcessor());
            }

            if ((record.getTask().getDataSelector()) && (record.getTask().getDataSelector().properties()) && (record.getTask().getDataSelector().properties().count())) {
                previewForm.down('#data-selector-properties-preview').show();
                previewForm.down('#data-selector-deviceGroup-preview').hide();
                previewForm.down('#data-selector-readingTypes-preview').hide();
                previewForm.down('#data-selector-exportPeriod-preview').hide();
                previewForm.down('#continuousData-preview').hide();
                previewForm.down('#updated-data').hide();
                previewForm.down('#updated-values').hide();
                previewForm.down('#data-selector-export-complete').hide();
                previewForm.down('#data-selector-validated-data').hide();
                //dataValidation.hide();
                //missingData.hide();
                //updatedData.hide();
                //updatedValuesData.hide();
                previewForm.down('#data-selector-properties-preview').loadRecord(record.getTask().getDataSelector());
            } else {
                switch (record.getTask().getDataSelector().get('selectorType')) {
                    case 'DEFAULT_READINGS':
                        previewForm.down('#data-selector-properties-preview').hide();
                        previewForm.down('#data-selector-deviceGroup-preview').show();
                        previewForm.down('#data-selector-usage-point-group-preview').hide();
                        previewForm.down('#data-selector-readingTypes-preview').show();
                        previewForm.down('#data-selector-eventTypes-preview').hide();
                        previewForm.down('#data-selector-exportPeriod-preview').show();
                        previewForm.down('#continuousData-preview').show();
                        previewForm.down('#updated-data').show();
                        if (record.getData().task.standardDataSelector.exportUpdate === false) {
                            previewForm.down('#updated-values').hide();
                        } else {
                            previewForm.down('#updated-values').show();
                        }
                        previewForm.down('#data-selector-export-complete').show();
                        previewForm.down('#data-selector-validated-data').show();
                        //dataValidation.show();
                        //missingData.show();
                        //updatedData.show();
                        //updatedValuesData.show();
                        break;
                    case 'DEFAULT_USAGE_POINT_READINGS':
                        previewForm.down('#data-selector-properties-preview').hide();
                        previewForm.down('#data-selector-deviceGroup-preview').hide();
                        previewForm.down('#data-selector-usage-point-group-preview').show();
                        previewForm.down('#data-selector-readingTypes-preview').show();
                        previewForm.down('#data-selector-eventTypes-preview').hide();
                        previewForm.down('#data-selector-exportPeriod-preview').show();
                        previewForm.down('#continuousData-preview').show();
                        previewForm.down('#updated-data').hide();
                        previewForm.down('#updated-values').hide();
                        previewForm.down('#data-selector-export-complete').show();
                        previewForm.down('#data-selector-validated-data').show();
                        break;
                    case 'DEFAULT_EVENTS':
                        previewForm.down('#data-selector-properties-preview').hide();
                        previewForm.down('#data-selector-deviceGroup-preview').show();
                        previewForm.down('#data-selector-usage-point-group-preview').hide();
                        previewForm.down('#data-selector-readingTypes-preview').hide();
                        previewForm.down('#data-selector-eventTypes-preview').show();
                        previewForm.down('#data-selector-exportPeriod-preview').show();
                        previewForm.down('#continuousData-preview').hide();
                        previewForm.down('#updated-data').hide();
                        previewForm.down('#updated-values').hide();
                        previewForm.down('#data-selector-export-complete').hide();
                        previewForm.down('#data-selector-validated-data').hide();
                        break;
                }
            }

            previewForm.loadRecord(record);

            //if (record.data.properties && record.data.properties.length) {
            //    previewForm.down('grouped-property-form').loadRecord(record.getTask());
            //}

            Ext.resumeLayouts(true);
        }
    },

    showAddReadingGrid: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            page = this.getAddPage(),
            readingTypesStore = page.down('#readingTypesGridPanel').getStore(),
            addReadingTypesRoute = router.currentRoute + '/readingtypes';

        // Prepare the already assigned reading types (for method addReadingTypes())
        me.readingTypesArray = [];
        readingTypesStore.each(function (record) {
            me.readingTypesArray.push(record.getData());
        });

        me.saveFormValues();
        router.getRoute(addReadingTypesRoute).forward();
    },

    populateStores: function () {
        var me = this;
        if (me.deviceTypesStore) { // store(s) was/were already loaded previously
            return;
        }
        var modelEntry = Ext.create('Dxp.model.EndDeviceEventTypePart', {
            value: me.comboBoxValueForAll,
            displayName: Uni.I18n.translate('general.all', 'DES', 'All')
        });
        me.deviceTypesStore = Ext.getStore('Dxp.store.DeviceTypes');
        me.deviceDomainsStore = Ext.getStore('Dxp.store.DeviceDomains');
        me.deviceSubDomainsStore = Ext.getStore('Dxp.store.DeviceSubDomains');
        me.deviceEventOrActionsStore = Ext.getStore('Dxp.store.DeviceEventOrActions');
        me.deviceTypesStore.load(function () {
            me.deviceTypesStore.insert(0, modelEntry);
        });
        me.deviceDomainsStore.load(function () {
            me.deviceDomainsStore.insert(0, modelEntry);
        });
        me.deviceSubDomainsStore.load(function () {
            me.deviceSubDomainsStore.insert(0, modelEntry);
        });
        me.deviceEventOrActionsStore.load(function () {
            me.deviceEventOrActionsStore.insert(0, modelEntry);
        });
    },

    showAddEventTypePopUp: function () {
        Ext.create('Dxp.view.tasks.EventTypeWindow', {
            title: Uni.I18n.translate('general.addEventType', 'DES', 'Add event type')
        });
    },

    addEventTypeToTask: function (button) {
        if (!this.getEventTypeWindow().isFormValid(this.getEventTypesGrid().getStore())) {
            return;
        }

        var eventType = this.getEventTypeWindow().getEventType(),
            eventTypeModel;

        eventTypeModel = Ext.create('Dxp.model.EventTypeForAddTaskGrid',
            {
                eventFilterCode: eventType,
                deviceTypeName: this.getEventTypeWindow().getDeviceTypeName(),
                deviceDomainName: this.getEventTypeWindow().getDeviceDomainName(),
                deviceSubDomainName: this.getEventTypeWindow().getDeviceSubDomainName(),
                deviceEventOrActionName: this.getEventTypeWindow().getDeviceEventOrActionName()
            });
        this.getEventTypesGrid().getStore().add(eventTypeModel);

        if (this.getEventTypesGrid().getStore().count() > 0) {
            this.getNoEventTypesLabel().hide();
            this.getEventTypesGrid().show();
        }
        this.validateEventsGrid(false);
        button.up('window').close();
    },

    showAddDestination: function (button) {
        this.doShowAddOrEditDestination(false);
    },

    showEditDestination: function () {
        this.doShowAddOrEditDestination(true);
    },

    doShowAddOrEditDestination: function (edit) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            addDestinationRoute = router.currentRoute + '/destination',
            route;

        me.destinationsArray = [];
        me.saveFormValues();
        route = router.getRoute(addDestinationRoute);
        if (edit) {
            route.setTitle(Uni.I18n.translate('dataExport.editDestination', 'DES', 'Edit destination'));
        } else {
            route.setTitle(Uni.I18n.translate('dataExport.addDestination', 'DES', 'Add destination'));
        }
        route.forward();
    },

    addDestination: function () {
        var me = this,
            view = Ext.widget('data-export-add-destination');
        me.getApplication().fireEvent('changecontentevent', view);

        if (me.destinationToEdit) {
            Ext.suspendLayouts();
            view.down('#save-destination-button').setText(Uni.I18n.translate('general.save', 'DES', 'Save'));
            view.down('#save-destination-button').setDisabled(false);
            view.down('#add-destination-form').setTitle(Uni.I18n.translate('dataExport.editDestination', 'DES', 'Edit destination'));
            me.showAllDestinationAttributes(false);
            var type = me.destinationToEdit.get('type');
            switch (me.destinationToEdit.get('type')) {
                case 'FILE':
                    me.showFileDestinationAttributes(true);
                    view.down('#destination-methods-combo').setValue('FILE');
                    view.down('#destination-methods-combo').setDisabled(true);
                    view.down('#destination-file-name').setValue(me.destinationToEdit.get('fileName'));
                    view.down('#destination-file-extension').setValue(me.destinationToEdit.get('fileExtension'));
                    view.down('#destination-file-location').setValue(me.destinationToEdit.get('fileLocation'));
                    break;
                case 'EMAIL':
                    me.showMailDestinationAttributes(true);
                    view.down('#destination-methods-combo').setValue('EMAIL');
                    view.down('#destination-methods-combo').setDisabled(true);
                    view.down('#destination-recipients').setValue(me.destinationToEdit.get('recipients'));
                    view.down('#destination-subject').setValue(me.destinationToEdit.get('subject'));
                    view.down('#destination-attachment-name').setValue(me.destinationToEdit.get('fileName'));
                    view.down('#destination-attachment-extension').setValue(me.destinationToEdit.get('fileExtension'));
                    break;
                case 'FTP':
                    me.showFtpDestinationAttributes(true);
                    view.down('#destination-methods-combo').setValue('FTP');
                    view.down('#destination-methods-combo').setDisabled(true);
                    view.down('#destination-file-name').setValue(me.destinationToEdit.get('fileName'));
                    view.down('#destination-file-extension').setValue(me.destinationToEdit.get('fileExtension'));
                    view.down('#destination-file-location').setValue(me.destinationToEdit.get('fileLocation'));
                    view.down('#hostname').setValue(me.destinationToEdit.get('server'));
                    view.down('#user-field').setValue(me.destinationToEdit.get('user'));
                    view.down('#dxp-port-field').setValue(me.destinationToEdit.get('port'));
                    view.down('#password-field').setValue(me.destinationToEdit.get('password'));
                    break;
                case 'FTPS':
                    me.showFtpDestinationAttributes(true);
                    view.down('#destination-methods-combo').setValue('FTPS');
                    view.down('#destination-methods-combo').setDisabled(true);
                    view.down('#destination-file-name').setValue(me.destinationToEdit.get('fileName'));
                    view.down('#destination-file-extension').setValue(me.destinationToEdit.get('fileExtension'));
                    view.down('#destination-file-location').setValue(me.destinationToEdit.get('fileLocation'));
                    view.down('#hostname').setValue(me.destinationToEdit.get('server'));
                    view.down('#user-field').setValue(me.destinationToEdit.get('user'));
                    view.down('#dxp-port-field').setValue(me.destinationToEdit.get('port'));
                    view.down('#password-field').setValue(me.destinationToEdit.get('password'));
                    break;
            }
            Ext.resumeLayouts(true);
        } else {
            me.destinationIndexToEdit = -1;
            me.showAllDestinationAttributes(false);
        }
    },


    showAllDestinationAttributes: function (visible) {
        Ext.suspendLayouts();
        this.showFileDestinationAttributes(visible);
        this.showMailDestinationAttributes(visible);
        this.showFtpDestinationAttributes(visible);
        Ext.resumeLayouts(true);
    },

    showFileDestinationAttributes: function (visible) {
        var me = this,
            page = me.getAddDestinationPage();
        Ext.suspendLayouts();
        page.down('#dxp-file-name-container').setVisible(visible);
        page.down('#destination-file-extension').setVisible(visible);
        page.down('#dxp-file-location-container').setVisible(visible);

        page.down('#dxp-file-name-container').disabled = !visible;
        page.down('#destination-file-extension').disabled = !visible;
        page.down('#dxp-file-location-container').disabled = !visible;
        Ext.resumeLayouts(true);
    },

    showMailDestinationAttributes: function (visible) {
        var me = this,
            page = me.getAddDestinationPage();
        Ext.suspendLayouts();
        page.down('#dxp-destination-recipients-container').setVisible(visible);
        page.down('#destination-subject').setVisible(visible);
        page.down('#dxp-attachment-name-container').setVisible(visible);
        page.down('#destination-attachment-extension').setVisible(visible);

        page.down('#dxp-destination-recipients-container').disabled = !visible;
        page.down('#destination-subject').disabled = !visible;
        page.down('#dxp-attachment-name-container').disabled = !visible;
        page.down('#destination-attachment-extension').disabled = !visible;
        Ext.resumeLayouts(true);
    },

    showFtpDestinationAttributes: function (visible) {
        var me = this,
            page = me.getAddDestinationPage();
        Ext.suspendLayouts();
        page.down('#dxp-file-name-container').setVisible(visible);
        page.down('#destination-file-extension').setVisible(visible);
        page.down('#dxp-file-location-container').setVisible(visible);
        page.down('#hostname').setVisible(visible);
        page.down('#user-field').setVisible(visible);
        page.down('#dxp-port-field').setVisible(visible);
        page.down('#password-field').setVisible(visible);

        page.down('#dxp-file-name-container').disabled = !visible;
        page.down('#destination-file-extension').disabled = !visible;
        page.down('#dxp-file-location-container').disabled = !visible;
        page.down('#hostname').disabled = !visible;
        page.down('#user-field').disabled = !visible;
        page.down('#dxp-port-field').disabled = !visible;
        page.down('#password-field').disabled = !visible;
        Ext.resumeLayouts(true);
    },

    updateDestinationAttributes: function () {
        var me = this,
            page = me.getAddDestinationPage(),
            method = page.down('#destination-methods-combo').getValue();
        Ext.suspendLayouts();
        me.showAllDestinationAttributes(false);
        switch (method) {
            case 'FILE':
                me.showFileDestinationAttributes(true);
                page.down('#destination-file-name').focus(false, 200);
                break;
            case 'EMAIL':
                me.showMailDestinationAttributes(true);
                page.down('#destination-recipients').focus(false, 200);
                break;
            case 'FTP':
            case 'FTPS':
                me.showFtpDestinationAttributes(true);
                page.down('#hostname').focus(false, 200);
                break;
        }
        Ext.resumeLayouts(true);
    },

    showAddExportTask: function () {
        var me = this,
            view = Ext.create('Dxp.view.tasks.Add'),
            dataSelectorCombo = view.down('#data-selector-combo'),
            deviceGroupCombo = view.down('#device-group-combo'),
            usagePointGroupCombo = view.down('#usage-point-group-combo'),
            exportPeriodCombo = view.down('#export-period-combo'),
            updateWindowCombo = view.down('#update-window'),
            timeframeCombo = view.down('#timeFrame'),
            recurrenceTypeCombo = view.down('#recurrence-type'),
            destinationsStore = view.down('#task-destinations-grid').getStore(),
            readingTypesStore = view.down('#readingTypesGridPanel').getStore(),
            eventTypesStore = view.down('#eventTypesGridPanel').getStore();

        me.getApplication().fireEvent('changecontentevent', view);

        Ext.util.History.on('change', this.checkRoute, this);
        me.taskModel = null;
        me.taskId = null;
        me.fromEdit = false;

        readingTypesStore.removeAll();
        destinationsStore.removeAll();
        eventTypesStore.removeAll();
        exportPeriodCombo.store.load({
            params: {
                category: 'relativeperiod.category.dataExport'
            }
        });
        updateWindowCombo.store.load({
            params: {
                category: 'relativeperiod.category.updateWindow'
            }
        });
        timeframeCombo.store.load({
            params: {
                category: 'relativeperiod.category.updateTimeframe'
            }
        });

        deviceGroupCombo.store.load(function () {
            if (this.getCount() === 0) {
                deviceGroupCombo.allowBlank = true;
                Ext.suspendLayouts();
                deviceGroupCombo.hide();
                view.down('#no-device').show();
                Ext.resumeLayouts(true);
            }
        });

        usagePointGroupCombo.store.load(function () {
            if (this.getCount() === 0) {
                usagePointGroupCombo.allowBlank = true;
                Ext.suspendLayouts();
                usagePointGroupCombo.hide();
                view.down('#no-usage-point').show();
                Ext.resumeLayouts(true);
            }
        });


        dataSelectorCombo.store.load(function () {
            recurrenceTypeCombo.setValue(recurrenceTypeCombo.store.findRecord('name', 'months'));
            if (me.getStore('Dxp.store.Clipboard').get('addDataExportTaskValues')) {
                me.setFormValues(view);
            } else if (this.getCount() === 1) {
                dataSelectorCombo.setValue(this.getAt(0).get('name'));
            }
        });
        me.recurrenceEnableDisable();
    },

    showEditExportTask: function (taskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view;

        if (me.fromDetails) {
            view = Ext.create('Dxp.view.tasks.Add', {
                edit: true,
                returnLink: router.getRoute('administration/dataexporttasks/dataexporttask').buildUrl({taskId: taskId})
            })
        } else {
            view = Ext.create('Dxp.view.tasks.Add', {
                edit: true,
                returnLink: router.getRoute('administration/dataexporttasks').buildUrl()
            })
        }

        if (me.destinationToEdit) { // coming from an edit destination (that hence was cancelled), add the old one again
            me.destinationsArray.push(me.destinationToEdit);
            me.destinationToEdit = null;
            me.destinationIndexToEdit = -1;
        }

        var taskModel = me.getModel('Dxp.model.DataExportTask'),
            taskForm = view.down('#add-data-export-task-form'),
            fileFormatterCombo = view.down('#file-formatter-combo'),
            deviceGroupCombo = view.down('#device-group-combo'),
            usagePointGroupCombo = view.down('#usage-point-group-combo'),
            exportPeriodCombo = view.down('#export-period-combo'),

            dataSelectorCombo = view.down('#data-selector-combo'),
            emptyDestinationsLabel = view.down('#noDestinationsLabel'),
            destinationsGrid = view.down('#task-destinations-grid'),
            destinationsStore = view.down('#task-destinations-grid').getStore(),
            recurrenceTypeCombo = view.down('#recurrence-type'),
            missingData = view.down('#data-selector-export-complete'),
            updatedDataRadioGroup = view.down('#updated-data-trigger'),
            updateWindowCombo = view.down('#update-window'),
            timeframeCombo = view.down('#timeFrame'),
            timeFrameRadioGroup = view.down('#export-updated'),
            continuousDataRadioGroup = view.down('#continuous-data-radiogroup');

        // --- Begin --- Take privileges update vs. update.schedule into account:
        if (Dxp.privileges.DataExport.canUpdateSchedule() && !Dxp.privileges.DataExport.canUpdateFull()) {
            Ext.suspendLayouts();
            view.down('#task-name').setDisabled(true);
            view.down('#dxp-data-selector-container').setDisabled(true);
            view.down('#device-group-container').setDisabled(true);
            view.down('#usage-point-group-container').setDisabled(true);
            view.down('#readingTypesFieldContainer').setDisabled(true);
            view.down('#eventTypesFieldContainer').setDisabled(true);
            view.down('#readingTypesGridPanel').setDisabled(true);
            view.down('#addReadingTypeButton').setDisabled(true);
            view.down('#export-periods-container').setDisabled(true);
            view.down('#continuous-data-container').setDisabled(true);
            view.down('#updated-data-container').setDisabled(true);
            view.down('#schedule-preview').setDisabled(true);
            view.down('#data-selector-export-complete').setDisabled(true);
            view.down('#data-selector-validated-data').setDisabled(true);
            view.down('#formatter-container').setDisabled(true);
            view.down('grouped-property-form').setDisabled(true);
            view.down('#destinationsFieldcontainer').setDisabled(true);
            view.down('#task-destinations-grid').setDisabled(true);
            Ext.resumeLayouts(true);
        }
        // ---  End  ---

        destinationsStore.removeAll();

        dataSelectorCombo.disabled = true;
        me.fromEdit = true;
        me.taskId = taskId;
        Ext.util.History.on('change', this.checkRoute, this);


        fileFormatterCombo.store.load({
            callback: function () {
                taskModel.load(taskId, {
                    success: function (record) {
                        var destinations = record.get('destinations'),
                            taskReadingTypes,
                            readingTypesGrid;
                        Ext.suspendLayouts();
                        if (record.destinationsStore.count() > 0) {
                            emptyDestinationsLabel.hide();
                            destinationsGrid.store.add(record.destinations().data.items);
                            destinationsGrid.show();
                        }

                        var dataSelector = record.get('dataSelector');

                        dataSelectorCombo.store.load({
                            callback: function () {
                                dataSelectorCombo.setValue(dataSelectorCombo.store.getById(record.getDataSelector().data.name));
                                if (record.getDataSelector().get('selectorType') === 'CUSTOM') {
                                    taskForm.down('#data-selector-properties').loadRecord(record.getDataSelector());
                                }
                            }
                        });
                        var schedule = record.get('schedule');
                        me.taskModel = record;
                        me.getApplication().fireEvent('dataexporttaskload', record);
                        taskForm.setTitle(Uni.I18n.translate('general.editx', 'DES', "Edit '{0}'", [record.get('name')]));
                        if (me.getStore('Dxp.store.Clipboard').get('addDataExportTaskValues')) {
                            me.setFormValues(view);
                        } else {
                            taskForm.loadRecord(record);

                            switch (record.getDataSelector().get('selectorType')) {
                                case 'DEFAULT_READINGS':
                                    taskReadingTypes = record.getStandardDataSelector().get('readingTypes');
                                    readingTypesGrid = view.down('#readingTypesGridPanel');

                                    readingTypesGrid.getStore().removeAll();
                                    Ext.each(taskReadingTypes, function (readingType) {
                                        readingTypesGrid.getStore().add({readingType: readingType})
                                    });
                                    view.updateReadingTypesGrid();

                                    exportPeriodCombo.store.load({
                                        params: {
                                            category: 'relativeperiod.category.dataExport'
                                        },
                                        callback: function () {
                                            exportPeriodCombo.setValue(exportPeriodCombo.store.getById(record.getStandardDataSelector().data.exportPeriod.id));
                                        }
                                    });
                                    updateWindowCombo.store.load({
                                        params: {
                                            category: 'relativeperiod.category.updateWindow'
                                        },
                                        callback: function () {
                                            if (record.getStandardDataSelector().data.updatePeriod && record.getStandardDataSelector().data.updatePeriod.id !== 0) {
                                                Ext.suspendLayouts();
                                                updateWindowCombo.setValue(updateWindowCombo.store.getById(record.getStandardDataSelector().data.updatePeriod.id));
                                                updatedDataRadioGroup.setValue({exportUpdate: record.getStandardDataSelector().get('exportUpdate')});
                                                Ext.resumeLayouts(true);
                                            }
                                        }
                                    });
                                    timeframeCombo.store.load({
                                        params: {
                                            category: 'relativeperiod.category.updateTimeframe'
                                        },
                                        callback: function () {
                                            if (record.getStandardDataSelector().data.updateWindow) {
                                                Ext.suspendLayouts();
                                                timeframeCombo.setValue(timeframeCombo.store.getById(record.getStandardDataSelector().data.updateWindow.id));
                                                timeFrameRadioGroup.setValue({updatedDataAndOrAdjacentData: true});
                                                Ext.resumeLayouts(true);
                                            }


                                        }
                                    });

                                    deviceGroupCombo.store.load({
                                        callback: function () {
                                            Ext.suspendLayouts();
                                            if (this.getCount() === 0) {
                                                deviceGroupCombo.allowBlank = true;
                                                deviceGroupCombo.hide();
                                                view.down('#no-device').show();
                                            }
                                            deviceGroupCombo.setValue(deviceGroupCombo.store.getById(record.getStandardDataSelector().data.deviceGroup.id));
                                            Ext.resumeLayouts(true);
                                        }
                                    });
                                    missingData.setValue({exportComplete: record.getStandardDataSelector().get('exportComplete')});

                                    continuousDataRadioGroup.setValue({exportContinuousData: record.getStandardDataSelector().get('exportContinuousData')});
                                    break;
                                case 'DEFAULT_USAGE_POINT_READINGS':
                                    taskReadingTypes = record.getStandardDataSelector().get('readingTypes');
                                    readingTypesGrid = view.down('#readingTypesGridPanel');

                                    readingTypesGrid.getStore().removeAll();
                                    Ext.each(taskReadingTypes, function (readingType) {
                                        readingTypesGrid.getStore().add({readingType: readingType})
                                    });
                                    view.updateReadingTypesGrid();

                                    exportPeriodCombo.store.load({
                                        params: {
                                            category: 'relativeperiod.category.dataExport'
                                        },
                                        callback: function () {
                                            exportPeriodCombo.setValue(exportPeriodCombo.store.getById(record.getStandardDataSelector().data.exportPeriod.id));
                                        }
                                    });
                                    updateWindowCombo.store.load({
                                        params: {
                                            category: 'relativeperiod.category.updateWindow'
                                        },
                                        callback: function () {
                                            if (record.getStandardDataSelector().data.updatePeriod && record.getStandardDataSelector().data.updatePeriod.id !== 0) {
                                                Ext.suspendLayouts();
                                                updateWindowCombo.setValue(updateWindowCombo.store.getById(record.getStandardDataSelector().data.updatePeriod.id));
                                                updatedDataRadioGroup.setValue({exportUpdate: record.getStandardDataSelector().get('exportUpdate')});
                                                Ext.resumeLayouts(true);
                                            }
                                        }
                                    });
                                    timeframeCombo.store.load({
                                        params: {
                                            category: 'relativeperiod.category.updateTimeframe'
                                        },
                                        callback: function () {
                                            if (record.getStandardDataSelector().data.updateWindow) {
                                                Ext.suspendLayouts();
                                                timeframeCombo.setValue(timeframeCombo.store.getById(record.getStandardDataSelector().data.updateWindow.id));
                                                timeFrameRadioGroup.setValue({updatedDataAndOrAdjacentData: true});
                                                Ext.resumeLayouts(true);
                                            }


                                        }
                                    });

                                    usagePointGroupCombo.store.load({
                                        callback: function () {
                                            Ext.suspendLayouts();
                                            if (this.getCount() === 0) {
                                                usagePointGroupCombo.allowBlank = true;
                                                usagePointGroupCombo.hide();
                                                view.down('#no-usage-point').show();
                                            }
                                            usagePointGroupCombo.setValue(usagePointGroupCombo.store.getById(record.getStandardDataSelector().data.usagePointGroup.id));
                                            Ext.resumeLayouts(true);
                                        }
                                    });
                                    missingData.setValue({exportComplete: record.getStandardDataSelector().get('exportComplete')});

                                    continuousDataRadioGroup.setValue({exportContinuousData: record.getStandardDataSelector().get('exportContinuousData')});
                                    break;
                                case 'DEFAULT_EVENTS':
                                    var eventTypes = record.getStandardDataSelector().get('eventTypeCodes'),
                                        eventTypesGrid = view.down('#eventTypesGridPanel');

                                    eventTypesGrid.getStore().removeAll();
                                    Ext.each(eventTypes, function (eventType) {
                                        me.fillInDisplayNames(eventType);
                                        eventTypesGrid.getStore().add(eventType);
                                    });
                                    view.updateEventTypesGrid();

                                    exportPeriodCombo.store.load({
                                        params: {
                                            category: 'relativeperiod.category.dataExport'
                                        },
                                        callback: function () {
                                            exportPeriodCombo.setValue(exportPeriodCombo.store.getById(record.getStandardDataSelector().data.exportPeriod.id));
                                        }
                                    });
                                    deviceGroupCombo.store.load({
                                        callback: function () {
                                            if (this.getCount() === 0) {
                                                deviceGroupCombo.allowBlank = true;
                                                Ext.suspendLayouts();
                                                deviceGroupCombo.hide();
                                                view.down('#no-device').show();
                                                Ext.resumeLayouts(true);
                                            }
                                            deviceGroupCombo.setValue(deviceGroupCombo.store.getById(record.getStandardDataSelector().data.deviceGroup.id));
                                        }
                                    });
                                    continuousDataRadioGroup.setValue({exportContinuousData: record.getStandardDataSelector().get('exportContinuousData')});
                                    break;
                            }
                            fileFormatterCombo.setValue(fileFormatterCombo.store.getById(record.data.dataProcessor.name));
                            if (record.data.nextRun && (record.data.nextRun !== 0)) {
                                view.down('#start-on').setValue(record.data.nextRun);
                            }
                            if (schedule) {
                                view.down('#recurrence-trigger').setValue({recurrence: true});
                                view.down('#recurrence-number').setValue(schedule.count);
                                recurrenceTypeCombo.setValue(schedule.timeUnit);
                            } else {
                                recurrenceTypeCombo.setValue(recurrenceTypeCombo.store.getAt(2));
                            }
                            if (record.getDataProcessor().properties() && record.getDataProcessor().properties().count()) {
                                taskForm.down('grouped-property-form').loadRecord(record.getDataProcessor());
                            }
                        }
                        me.recurrenceEnableDisable();
                        me.updatedDataEnableDisable();
                        me.exportUpdatedEnableDisabled();
                        Ext.resumeLayouts(true);
                        view.setLoading(false);
                    }

                });
            }
        });

        me.getApplication().fireEvent('changecontentevent', view);
        view.setLoading();
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('dxp-tasks-preview'),
            previewForm = page.down('dxp-tasks-preview-form'),
            selectorPropertyForm = previewForm.down('#data-selector-properties-preview'),
            deviceGroup = previewForm.down('#data-selector-deviceGroup-preview'),
            usagePointGroup = previewForm.down('#data-selector-usage-point-group-preview'),
            exportPeriod = previewForm.down('#data-selector-exportPeriod-preview'),
            continuousData = previewForm.down('#continuousData-preview'),
            readingTypes = previewForm.down('#data-selector-readingTypes-preview'),
            eventTypes = previewForm.down('#data-selector-eventTypes-preview'),
            propertyForm = previewForm.down('#task-properties-preview'),
            dataValidation = previewForm.down('#data-selector-validated-data'),
            missingData = previewForm.down('#data-selector-export-complete'),
            updatedData = previewForm.down('#updated-data'),
            updatedValuesData = previewForm.down('#updated-values');

        Ext.suspendLayouts();

        if (record.get('status') === 'Busy') {
            Ext.Array.each(Ext.ComponentQuery.query('#run'), function (item) {
                item.hide();
            });
        } else {
            if (record.get('status') === 'Failed') {
                previewForm.down('#reason-field').show();
            } else {
                previewForm.down('#reason-field').hide();
            }
            if (Dxp.privileges.DataExport.canRun()) {
                Ext.Array.each(Ext.ComponentQuery.query('#run'), function (item) {
                    item.show();
                });
            }
        }

        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        previewForm.loadRecord(record);
        preview.down('dxp-tasks-action-menu').record = record;

        if (record.getDataProcessor() && record.getDataProcessor().properties().count()) {
            propertyForm.loadRecord(record.getDataProcessor());
            propertyForm.show();
        } else {
            propertyForm.hide();
        }
        if ((record.getDataSelector()) && (record.getDataSelector().properties()) && (record.getDataSelector().properties().count())) {
            selectorPropertyForm.show();
            deviceGroup.hide();
            exportPeriod.hide();
            continuousData.hide();
            readingTypes.hide();
            eventTypes.hide();
            dataValidation.hide();
            missingData.hide();
            updatedData.hide();
            updatedValuesData.hide();
            selectorPropertyForm.loadRecord(record.getDataSelector());
        } else {
            switch (record.getDataSelector().get('selectorType')) {
                case 'DEFAULT_READINGS':
                    selectorPropertyForm.hide();
                    deviceGroup.show();
                    usagePointGroup.hide();
                    exportPeriod.show();
                    continuousData.show();
                    readingTypes.show();
                    eventTypes.hide();
                    dataValidation.show();
                    missingData.show();
                    updatedData.show();
                    if (record.getData().standardDataSelector.exportUpdate === false) {
                        updatedValuesData.hide();
                    } else {
                        updatedValuesData.show();
                    }
                    break;
                case 'DEFAULT_USAGE_POINT_READINGS':
                    selectorPropertyForm.hide();
                    deviceGroup.hide();
                    usagePointGroup.show();
                    exportPeriod.show();
                    continuousData.show();
                    readingTypes.show();
                    eventTypes.hide();
                    dataValidation.show();
                    missingData.show();
                    updatedData.hide();
                    updatedValuesData.hide();
                    break;
                case 'DEFAULT_EVENTS':
                    selectorPropertyForm.hide();
                    deviceGroup.show();
                    usagePointGroup.hide();
                    exportPeriod.show();
                    continuousData.hide();
                    readingTypes.hide();
                    eventTypes.show();
                    dataValidation.hide();
                    missingData.hide();
                    updatedData.hide();
                    updatedValuesData.hide();
            }
        }

        Ext.resumeLayouts(true);
    },

    chooseDestinationAction: function (menu, item) {
        var me = this;
        switch (item.action) {
            case 'removeDestination':
                var page = me.getAddPage(),
                    destinationsGrid = page.down('#task-destinations-grid'),
                    destinationsStore = destinationsGrid.getStore(),
                    emptyDestinationsLabel = page.down('#noDestinationsLabel');
                Ext.suspendLayouts();
                destinationsStore.remove(menu.record);
                if (destinationsStore.count() === 0) {
                    emptyDestinationsLabel.show();
                    destinationsGrid.hide();
                }
                Ext.resumeLayouts(true);
                break;
            case 'editDestination':
                me.destinationToEdit = menu.record;
                var page = me.getAddPage(),
                    destinationsGrid = page.down('#task-destinations-grid');
                // edit = remove + add new
                me.destinationIndexToEdit = destinationsGrid.getStore().indexOf(menu.record);
                destinationsGrid.getStore().remove(menu.record);
                me.showEditDestination();
                break;
        }

    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        if (me.getHistory()) {
            router.arguments.occurrenceId = menu.record.getId();
        } else {
            router.arguments.taskId = menu.record.getId();
        }

        switch (item.action) {
            case 'viewDetails':
                route = 'administration/dataexporttasks/dataexporttask';
                break;
            case 'editExportTask':
                route = 'administration/dataexporttasks/dataexporttask/edit';
                break;
            case 'removeTask':
                me.removeTask(menu.record);
                break;
            case 'viewHistory':
                route = 'administration/dataexporttasks/dataexporttask/history';
                break;
            case 'run':
                me.runTask(menu.record);
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward(router.arguments);
    },

    runTask: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('general.run', 'DES', 'Run'),
                confirmation: function () {
                    me.submitRunTask(record, this);
                }
            });

        confirmationWindow.insert(1,
            {
                xtype: 'panel',
                itemId: 'date-errors',
                hidden: true,
                bodyStyle: {
                    color: '#eb5642',
                    padding: '0 0 15px 65px'
                },
                html: ''
            }
        );

        confirmationWindow.show({
            msg: Uni.I18n.translate('exportTasks.runMsg', 'DES', 'Data export task will be queued to run at the earliest possible time.'),
            title: Uni.I18n.translate('general.runExportTaskx', 'DES', "Run export task {0}?", [record.data.name])
        });
    },

    submitRunTask: function (record, confWindow) {
        var me = this,
            id = record.get('id'),
            taskModel = me.getModel('Dxp.model.DataExportTask'),
            grid,
            store,
            index,
            view;

        Ext.Ajax.request({
            url: '/api/export/dataexporttask/' + id + '/trigger',
            method: 'PUT',
            jsonData: record.getRecordData(),
            isNotEdit: true,
            success: function () {
                confWindow.destroy();
                if (me.getPage()) {
                    view = me.getPage();
                    grid = view.down('grid');
                    store = grid.getStore();
                    index = store.indexOf(record);
                    view.down('preview-container').selectByDefault = false;
                    store.load(function () {
                        grid.getSelectionModel().select(index);
                    });
                } else {
                    taskModel.load(id, {
                        success: function (rec) {
                            view = me.getDetailsPage();
                            view.down('dxp-tasks-action-menu').record = rec;
                            view.down('dxp-tasks-preview-form').loadRecord(rec);
                            if (record.get('status') === 'Busy') {
                                view.down('#run').hide();
                            }
                            if (Ext.isFunction(rec.properties) && rec.properties().count()) {
                                view.down('grouped-property-form').loadRecord(rec);
                            }
                        }
                    });
                }
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('exportTasks.run', 'DES', 'Export task run'));
            },
            failure: function (response) {
                var res = Ext.decode(response.responseText, true);

                if (response.status !== 409 && res && res.errors && res.errors.length) {
                    confWindow.update(res.errors[0].msg);
                    confWindow.setVisible(true);
                } else {
                    confWindow.destroy();
                }
            }
        });
    },

    removeTask: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation');
        confirmationWindow.show({
            msg: Uni.I18n.translate('general.remove.msgx', 'DES', 'This export task will no longer be available.'),
            title: Uni.I18n.translate('general.removex', 'DES', 'Remove {0}?', [record.data.name]),
            config: {},
            fn: function (state) {
                if (state === 'confirm') {
                    me.removeOperation(record);
                } else if (state === 'cancel') {
                    this.close();
                }
            }
        });
    },

    removeOperation: function (record) {
        var me = this;
        record.destroy({
            success: function () {
                if (me.getPage()) {
                    var grid = me.getPage().down('dxp-tasks-grid');
                    grid.down('pagingtoolbartop').totalCount = 0;
                    grid.down('pagingtoolbarbottom').resetPaging();
                    grid.getStore().load();
                } else {
                    me.getController('Uni.controller.history.Router').getRoute('administration/dataexporttasks').forward();
                }
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.remove.confirm.msgx', 'DES', 'Export task removed'));
            },
            failure: function (object, operation) {
                if (operation.response.status === 409) {
                    return
                }
                var json = Ext.decode(operation.response.responseText, true);
                var errorText = Uni.I18n.translate('communicationtasks.error.unknown', 'DES', 'Unknown error occurred');
                if (json && json.errors) {
                    errorText = json.errors[0].msg;
                }
                //me.getApplication().getController('Uni.controller.Error').showError(Uni.I18n.translate('general.remove.error.msg', 'DES', 'Remove operation failed'), errorText);
                if (!Ext.ComponentQuery.query('#remove-error-messagebox')[0]) {
                    Ext.widget('messagebox', {
                        itemId: 'remove-error-messagebox',
                        buttons: [
                            {
                                text: 'Retry',
                                ui: 'remove',
                                handler: function (button, event) {
                                    this.up('messagebox').destroy();
                                    me.removeOperation(record);
                                }
                            },
                            {
                                text: 'Cancel',
                                action: 'cancel',
                                ui: 'link',
                                href: '#/administration/dataexporttasks/',
                                handler: function (button, event) {
                                    this.up('messagebox').destroy();
                                }
                            }
                        ]
                    }).show({
                        title: Uni.I18n.translate('general.remove.error.msg', 'DES', 'Remove operation failed'),
                        ui: 'message-error',
                        icon: 'icon-warning2',
                        style: 'font-size: 34px;',
                        msg: errorText,
                        modal: false
                    })
                }
            }
        });
    },

    updateDataSelectorProperties: function (field, newValue, oldValue) {
        if (newValue == "") {
            return;
        }
        var me = this,
            page = me.getAddPage(),
            record = Ext.getStore('Dxp.store.DataSelectors').getById(newValue),
            formatterCombo = page.down('#file-formatter-combo'),
            formatterContainer = page.down('#formatter-container'),
            formatterTitle = page.down('#file-formatter-title'),
            propertyForm = page.down('#data-selector-properties');

        if (!me.getStore('Dxp.store.Clipboard').get('addDataExportTaskValues') ||
            me.getStore('Dxp.store.Clipboard').get('addDataExportTaskValues')['readingTypeDataSelector.value.dataSelector'] != newValue) {
            me.getStore('Dxp.store.Clipboard').removeAll(true);
            formatterCombo.store.load({
                scope: me,
                params: {
                    selector: newValue
                },
                callback: function (record) {
                    if (!page.edit) {
                        formatterCombo.setValue(formatterCombo.store.getCount() == 1 ? formatterCombo.store.getAt(0) : null);
                    }
                }
            });
        }

        Ext.suspendLayouts();
        formatterContainer.show();
        formatterTitle.show();
        formatterCombo.show();
        switch (record.get('selectorType')) {
            case 'DEFAULT_READINGS':
                me.showDeviceReadingsDataSelectorProperties();
                break;
            case 'DEFAULT_EVENTS':
                me.showEventTypeDataSelectorProperties();
                break;
            case 'DEFAULT_USAGE_POINT_READINGS':
                me.showUsagePointReadingsDataSelectorProperties();
                break;
            default:
                me.hideDefaultDataSelectorProperties(true);
                if (record && record.properties() && record.properties().count()) {
                    propertyForm.addEditPage = true;
                    propertyForm.loadRecord(record);
                    propertyForm.show();
                } else {
                    propertyForm.hide();
                }
        }
        Ext.resumeLayouts(true);
    },

    xOrFunction: function (a, b) {
        return ( a || b ) && !( a && b );
    },

    changeFormatterTooltip: function (tooltip, formatterType) {
        Ext.suspendLayouts();
        switch (formatterType) {
            case 'standardCsvDataProcessorFactory':
                tooltip.setVisible(true);
                tooltip.setTooltip(
                    Ext.String.format(
                        Uni.I18n.translate('addDataExportTask.formatter.tooltip', 'DES', 'The export file contains 6 columns')
                        + ':<br>'
                        + '<div style="text-indent: 40px">{0}</div>'
                        + '<div style="text-indent: 40px">{1}</div>'
                        + '<div style="text-indent: 40px">{2}</div>'
                        + '<div style="text-indent: 40px">{3}</div>'
                        + '<div style="text-indent: 40px">{4}</div>'
                        + '<div style="text-indent: 40px">{5}</div>',
                        Uni.I18n.translate('addDataExportTask.formatter.tooltip.col1', 'DES', 'Interval timestamp (YYYY-MM-DDThh:mm:ss.sTZD)'),
                        Uni.I18n.translate('addDataExportTask.formatter.tooltip.col2', 'DES', 'Device MRID (text)'),
                        Uni.I18n.translate('addDataExportTask.formatter.tooltip.col3', 'DES', 'Device name (text)'),
                        Uni.I18n.translate('addDataExportTask.formatter.tooltip.col4', 'DES', 'Reading type (text)'),
                        Uni.I18n.translate('addDataExportTask.formatter.tooltip.col5', 'DES', 'Value (number)'),
                        Uni.I18n.translate('addDataExportTask.formatter.tooltip.col6', 'DES', 'Validation result (text)')));
                break;
            case 'standardCsvEventDataProcessorFactory':
                tooltip.setVisible(true);
                tooltip.setTooltip(
                    Ext.String.format(
                        Uni.I18n.translate('addDataExportTask.formatter.tooltip2', 'DES', 'The export file contains 4 columns')
                        + ':<br>'
                        + '<div style="text-indent: 40px">{0}</div>'
                        + '<div style="text-indent: 40px">{1}</div>'
                        + '<div style="text-indent: 40px">{2}</div>'
                        + '<div style="text-indent: 40px">{3}</div>'
                        + '<div style="text-indent: 40px">{4}</div>',
                        Uni.I18n.translate('addDataExportTask.eventFormatter.tooltip.col1', 'DES', 'Event date (YYYY-MM-DDThh:mm:ss.sTZD)'),
                        Uni.I18n.translate('addDataExportTask.eventFormatter.tooltip.col2', 'DES', 'Event type (text)'),
                        Uni.I18n.translate('addDataExportTask.eventFormatter.tooltip.col3', 'DES', 'Device MRID (text)'),
                        Uni.I18n.translate('addDataExportTask.eventFormatter.tooltip.col4', 'DES', 'Device name (text)')));
                break;
            default:
                tooltip.setTooltip('');
                tooltip.setVisible(false);
        }
        Ext.resumeLayouts(true);
    },

    showDeviceReadingsDataSelectorProperties: function () {
        var me = this;
        var page = me.getAddPage();
        Ext.suspendLayouts();
        page.down('#device-group-container').setVisible(true);
        page.down('#usage-point-group-container').setVisible(false);
        page.down('#readingTypesFieldContainer').setVisible(true);
        page.down('#eventTypesFieldContainer').setVisible(false);
        page.down('#export-periods-container').setVisible(true);
        page.down('#data-selector-properties').setVisible(false);
        page.down('#data-selector-validated-data').setVisible(true);
        page.down('#data-selector-export-complete').setVisible(true);
        page.down('#updated-data-container').setVisible(true);
        page.down('#continuous-data-container').setVisible(true);

        me.updatedDataEnableDisable();
        me.exportUpdatedEnableDisabled();
        Ext.resumeLayouts(true);
    },

    showUsagePointReadingsDataSelectorProperties: function () {
        var me = this;
        var page = me.getAddPage();
        Ext.suspendLayouts();
        page.down('#device-group-container').setVisible(false);
        page.down('#usage-point-group-container').setVisible(true);
        page.down('#readingTypesFieldContainer').setVisible(true);
        page.down('#eventTypesFieldContainer').setVisible(false);
        page.down('#export-periods-container').setVisible(true);
        page.down('#data-selector-properties').setVisible(false);
        page.down('#data-selector-validated-data').setVisible(true);
        page.down('#data-selector-export-complete').setVisible(true);
        page.down('#updated-data-container').setVisible(false);
        page.down('#continuous-data-container').setVisible(true);

        me.updatedDataEnableDisable();
        me.exportUpdatedEnableDisabled();
        Ext.resumeLayouts(true);
    },

    showEventTypeDataSelectorProperties: function (hidden) {
        var me = this;
        var page = me.getAddPage();
        Ext.suspendLayouts();
        page.down('#device-group-container').setVisible(true);
        page.down('#usage-point-group-container').setVisible(false);
        page.down('#readingTypesFieldContainer').setVisible(false);
        page.down('#eventTypesFieldContainer').setVisible(true);
        page.down('#export-periods-container').setVisible(true);
        page.down('#data-selector-properties').setVisible(false);
        page.down('#data-selector-validated-data').setVisible(false);
        page.down('#data-selector-export-complete').setVisible(false);
        page.down('#updated-data-container').setVisible(false);
        page.down('#continuous-data-container').setVisible(false);
        Ext.resumeLayouts(true);
    },

    hideDefaultDataSelectorProperties: function () {
        var me = this;
        var page = me.getAddPage();
        Ext.suspendLayouts();
        page.down('#device-group-container').setVisible(false);
        page.down('#readingTypesFieldContainer').setVisible(false);
        page.down('#eventTypesFieldContainer').setVisible(false);
        page.down('#export-periods-container').setVisible(false);
        page.down('#data-selector-properties').setVisible(true);
        page.down('#data-selector-validated-data').setVisible(false);
        page.down('#data-selector-export-complete').setVisible(false);
        page.down('#updated-data-container').setVisible(false);
        page.down('#continuous-data-container').setVisible(false);
        Ext.resumeLayouts(true);
    },


    updateProperties: function (field, newValue) {
        var me = this,
            page = me.getAddPage(),
            record = Ext.getStore('Dxp.store.FileFormatters').getById(newValue),
            propertyForm = page.down('grouped-property-form'),
            tooltip = page.down('#file-formatter-info');

        if (record !== null) {
            me.changeFormatterTooltip(tooltip, record.get('name'));
        }

        if (record && record.properties() && record.properties().count()) {
            propertyForm.addEditPage = true;
            propertyForm.loadRecord(record);
            propertyForm.show();
        } else {
            propertyForm.hide();
        }
    },

    addSelectedReadingTypes: function () {
        var me = this,
            widget = this.getAddReadingTypesSetup(),
            grid = widget.down('#addReadingTypesGrid'),
            selection = grid.getSelectedRecords();

        if (selection.length > 0) {
            Ext.each(selection, function (record) {
                me.readingTypesArray.push({readingType: record.get('readingType')});
            });
        }

        me.forwardToPreviousPage();
    },

    addDestinationToGrid: function (button) {
        var me = this,
            id;

        if (!me.getAddDestinationPage().isFormValid()) {
            return;
        }

        if (me.destinationToEdit) {
            id = me.destinationToEdit.get('id');
        }
        me.destinationToEdit = null;
        me.doAddDestinationToGrid(button, id);
    },

    doAddDestinationToGrid: function (button, id) {
        var me = this;

        if (me.destinationToEdit) { //edit destination was cancelled, add the old one again
            me.destinationsArray.push(me.destinationToEdit);
            me.destinationToEdit = null;
            me.destinationIndexToEdit = -1;
            me.forwardToPreviousPage();
        } else {
            var page = me.getAddDestinationPage(),
                form = page.down('#add-destination-form'),
                methodComboBoxValue = page.down('#destination-methods-combo').getValue(),
                destinationModel,
                formValues = form.getForm().getValues();

            switch (methodComboBoxValue) {
                case 'FILE':
                    //tooltip & method duplicated from destination model, have not found another way!
                    destinationModel = Ext.create('Dxp.model.Destination', {
                        id: id ? id : undefined,
                        type: 'FILE',
                        fileName: formValues['fileName'],
                        fileExtension: formValues['fileExtension'],
                        fileLocation: formValues['fileLocation'],
                        method: Uni.I18n.translate('destination.file', 'DES', 'Save file'),
                        destination: formValues['fileLocation'] + '/' + formValues['fileName'] + '.' + formValues['fileExtension'],
                        tooltiptext: Uni.I18n.translate('general.fileLocation', 'DES', 'File location')
                        + ': ' + Ext.String.htmlEncode(Ext.String.htmlEncode(formValues['fileLocation'])) + '<br>'
                        + Uni.I18n.translate('general.fileName', 'DES', 'File name')
                        + ': ' + Ext.String.htmlEncode(Ext.String.htmlEncode(formValues['fileName'])) + '<br>'
                        + Uni.I18n.translate('general.fileExtension', 'DES', 'File extension')
                        + ': ' + formValues['fileExtension']
                    });
                    break;
                case 'EMAIL':
                    destinationModel = Ext.create('Dxp.model.Destination', {
                        id: id ? id : undefined,
                        type: 'EMAIL',
                        fileName: formValues['attachmentName'],
                        fileExtension: formValues['attachmentExtension'],
                        recipients: formValues['recipients'],
                        subject: formValues['subject'],
                        method: Uni.I18n.translate('destination.email', 'DES', 'Mail'),
                        destination: formValues['recipients'],
                        tooltiptext: Uni.I18n.translate('dataExportdestinations.recipients', 'DES', 'Recipients')
                        + ': ' + formValues['recipients'] + '<br>'
                        + Uni.I18n.translate('general.subject', 'DES', 'Subject') + ': ' + formValues['subject'] + '<br>'
                        + Uni.I18n.translate('general.fileName', 'DES', 'File name')
                        + ': ' + Ext.String.htmlEncode(Ext.String.htmlEncode(formValues['attachmentName'])) + '<br>'
                        + Uni.I18n.translate('general.fileExtension', 'DES', 'File extension')
                        + ': ' + formValues['attachmentExtension']
                    });
                    break;
                case 'FTP':
                    destinationModel = Ext.create('Dxp.model.Destination', {
                        id: id ? id : undefined,
                        type: 'FTP',
                        server: formValues['server'],
                        user: formValues['user'],
                        port: formValues['port'],
                        password: formValues['password'],
                        fileName: formValues['fileName'],
                        fileExtension: formValues['fileExtension'],
                        fileLocation: formValues['fileLocation'],
                        method: Uni.I18n.translate('destination.ftp', 'DES', 'FTP'),
                        destination: formValues['server'],
                        tooltiptext: Uni.I18n.translate('dataExportdestinations.ftpServer', 'DES', 'FTP server')
                        + ': ' + formValues['server'] + '<br>'
                        + Uni.I18n.translate('general.port', 'DES', 'Port') + ': ' + formValues['port'] + '<br>'
                        + Uni.I18n.translate('general.user', 'DES', 'User') + ': ' + formValues['user'] + '<br>'
                        + Uni.I18n.translate('general.fileName', 'DES', 'File name')
                        + ': ' + Ext.String.htmlEncode(Ext.String.htmlEncode(formValues['fileName'])) + '<br>'
                        + Uni.I18n.translate('general.fileExtension', 'DES', 'File extension')
                        + ': ' + formValues['fileExtension'] + '<br>'
                        + Uni.I18n.translate('general.fileLocation', 'DES', 'File location')
                        + ': ' + Ext.String.htmlEncode(Ext.String.htmlEncode(formValues['fileLocation']))
                    });
                    break;
                case 'FTPS':
                    destinationModel = Ext.create('Dxp.model.Destination', {
                        id: id ? id : undefined,
                        type: 'FTPS',
                        server: formValues['server'],
                        user: formValues['user'],
                        port: formValues['port'],
                        password: formValues['password'],
                        fileName: formValues['fileName'],
                        fileExtension: formValues['fileExtension'],
                        fileLocation: formValues['fileLocation'],
                        method: Uni.I18n.translate('destination.ftps', 'DES', 'FTPS'),
                        destination: formValues['server'],
                        tooltiptext: Uni.I18n.translate('dataExportdestinations.ftpsServer', 'DES', 'FTPS server')
                        + ': ' + formValues['server'] + '<br>'
                        + Uni.I18n.translate('general.port', 'DES', 'Port') + ': ' + formValues['port'] + '<br>'
                        + Uni.I18n.translate('general.user', 'DES', 'User') + ': ' + formValues['user'] + '<br>'
                        + Uni.I18n.translate('general.fileName', 'DES', 'File name')
                        + ': ' + Ext.String.htmlEncode(Ext.String.htmlEncode(formValues['fileName'])) + '<br>'
                        + Uni.I18n.translate('general.fileExtension', 'DES', 'File extension')
                        + ': ' + formValues['fileExtension'] + '<br>'
                        + Uni.I18n.translate('general.fileLocation', 'DES', 'File location')
                        + ': ' + Ext.String.htmlEncode(Ext.String.htmlEncode(formValues['fileLocation']))
                    });
                    break;
            }

            me.destinationsArray.push(destinationModel);
            me.forwardToPreviousPage();

        }
    },


    addTask: function (button) {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            page = me.getAddPage(),
            form = page.down('#add-data-export-task-form'),
            formErrorsPanel = form.down('#form-errors'),
            propertyForm = form.down('grouped-property-form'),
            selectorPropertyForm = form.down('#data-selector-properties'),
            dataSelectorCombo = form.down('#data-selector-combo'),
            exportWindowCombo = form.down('#export-period-combo'),
            lastDayOfMonth = false,
            startOnDate,
            timeUnitValue,
            dayOfMonth,
            hours,
            minutes;

        propertyForm.updateRecord();

        var selectedDataSelector = dataSelectorCombo.findRecord(dataSelectorCombo.valueField, dataSelectorCombo.getValue());
        var emptyReadingTypes = selectedDataSelector
            && (selectedDataSelector.get('selectorType') === 'DEFAULT_READINGS' || selectedDataSelector.get('selectorType') === 'DEFAULT_USAGE_POINT_READINGS')
            && page.down('#readingTypesGridPanel').getStore().data.items.length == 0;

        Ext.suspendLayouts();
        if (emptyReadingTypes) {
            form.down('#readingTypesFieldContainer').setActiveError(me.requiredFieldText);
        } else {
            form.down('#readingTypesFieldContainer').unsetActiveError();
        }
        form.down('#readingTypesFieldContainer').doComponentLayout();


        var emptyEventTypes = (selectedDataSelector)
            && (selectedDataSelector.get('selectorType') === 'DEFAULT_EVENTS')
            && (page.down('#eventTypesGridPanel').getStore().data.items.length == 0);
        me.validateEventsGrid(emptyEventTypes);
        form.down('#eventTypesFieldContainer').doComponentLayout();


        var emptyDestinations = page.down('#task-destinations-grid').getStore().data.items.length == 0;
        if (emptyDestinations) {
            form.down('#destinationsFieldcontainer').setActiveError(me.requiredFieldText);
        } else {
            form.down('#destinationsFieldcontainer').unsetActiveError();
        }
        form.down('#destinationsFieldcontainer').doComponentLayout();

        var noDataSelectorChosen = !dataSelectorCombo.getValue() || dataSelectorCombo.getValue().length === 0;
        if (noDataSelectorChosen) {
            form.down('#dxp-data-selector-container').setActiveError(me.requiredFieldText);
        } else {
            form.down('#dxp-data-selector-container').unsetActiveError();
            var formatterCombo = page.down('#file-formatter-combo'),
                noFormatterChosen = !formatterCombo.getValue() || formatterCombo.getValue().length === 0;
            if (noFormatterChosen) {
                form.down('#formatter-container').setActiveError(me.requiredFieldText);
            } else {
                form.down('#formatter-container').unsetActiveError();
            }
            form.down('#formatter-container').doComponentLayout();

            if (selectedDataSelector.get('selectorType') !== 'DEFAULT_USAGE_POINT_READINGS') {
                var deviceGroupCombo = page.down('#device-group-combo'),
                    noDeviceGroupChosen = !deviceGroupCombo.getValue() || deviceGroupCombo.getValue().length === 0;
                if (noDeviceGroupChosen) {
                    form.down('#device-group-container').setActiveError(me.requiredFieldText);
                } else {
                    form.down('#device-group-container').unsetActiveError();
                }
                form.down('#device-group-container').doComponentLayout();
            } else {
                var usagePointGroupCombo = form.down('#usage-point-group-combo'),
                    noUsagePointGroupChosen = !usagePointGroupCombo.getValue() || usagePointGroupCombo.getValue().length === 0;
                if (noUsagePointGroupChosen) {
                    form.down('#usage-point-group-container').setActiveError(me.requiredFieldText);
                } else {
                    form.down('#usage-point-group-container').unsetActiveError();
                }
                form.down('#device-group-container').doComponentLayout();
            }

            var selectedExportWindow = !exportWindowCombo.getValue() || exportWindowCombo.getValue().length === 0;
            if (selectedExportWindow) {
                form.down('#export-periods-container').setActiveError(me.requiredFieldText);
            } else {
                form.down('#export-periods-container').unsetActiveError();
            }
            form.down('#export-periods-container').doComponentLayout();
        }
        form.down('#dxp-data-selector-container').doComponentLayout();
        Ext.resumeLayouts(true);

        if (form.isValid()
            && !emptyReadingTypes
            && !emptyEventTypes
            && !emptyDestinations
            && !noFormatterChosen
            && !noDeviceGroupChosen
            && !noUsagePointGroupChosen) {
            var record = me.taskModel || Ext.create('Dxp.model.DataExportTask'),
                readingTypesStore = page.down('#readingTypesGridPanel').getStore(),
                eventTypesStore = page.down('#eventTypesGridPanel').getStore(),
                arrReadingTypes = [],
                arrEventTypes = [];

            record.beginEdit();
            if (!formErrorsPanel.isHidden()) {
                formErrorsPanel.hide();
                form.down('#readingTypesGridPanel').removeCls('error-border');
            }

            record.set('name', form.down('#task-name').getValue());

            startOnDate = moment(form.down('#start-on').getValue()).valueOf();

            if (form.down('#recurrence-trigger').getValue().recurrence) {
                timeUnitValue = form.down('#recurrence-type').getValue();
                dayOfMonth = moment(startOnDate).date();
                if (dayOfMonth >= 29) {
                    lastDayOfMonth = true;
                }
                hours = form.down('#date-time-field-hours').getValue();
                minutes = form.down('#date-time-field-minutes').getValue();
                switch (timeUnitValue) {
                    case 'years':
                        record.set('schedule', {
                            count: form.down('#recurrence-number').getValue(),
                            timeUnit: timeUnitValue,
                            offsetMonths: moment(startOnDate).month() + 1,
                            offsetDays: dayOfMonth,
                            lastDayOfMonth: lastDayOfMonth,
                            dayOfWeek: null,
                            offsetHours: hours,
                            offsetMinutes: minutes,
                            offsetSeconds: 0
                        });
                        break;
                    case 'months':
                        record.set('schedule', {
                            count: form.down('#recurrence-number').getValue(),
                            timeUnit: timeUnitValue,
                            offsetMonths: 0,
                            offsetDays: dayOfMonth,
                            lastDayOfMonth: lastDayOfMonth,
                            dayOfWeek: null,
                            offsetHours: hours,
                            offsetMinutes: minutes,
                            offsetSeconds: 0
                        });
                        break;
                    case 'weeks':
                        record.set('schedule', {
                            count: form.down('#recurrence-number').getValue(),
                            timeUnit: timeUnitValue,
                            offsetMonths: 0,
                            offsetDays: 0,
                            lastDayOfMonth: lastDayOfMonth,
                            dayOfWeek: moment(startOnDate).format('dddd').toUpperCase(),
                            offsetHours: hours,
                            offsetMinutes: minutes,
                            offsetSeconds: 0
                        });
                        break;
                    case 'days':
                        record.set('schedule', {
                            count: form.down('#recurrence-number').getValue(),
                            timeUnit: timeUnitValue,
                            offsetMonths: 0,
                            offsetDays: 0,
                            lastDayOfMonth: lastDayOfMonth,
                            dayOfWeek: null,
                            offsetHours: hours,
                            offsetMinutes: minutes,
                            offsetSeconds: 0
                        });
                        break;
                    case 'hours':
                        record.set('schedule', {
                            count: form.down('#recurrence-number').getValue(),
                            timeUnit: timeUnitValue,
                            offsetMonths: 0,
                            offsetDays: 0,
                            lastDayOfMonth: lastDayOfMonth,
                            dayOfWeek: null,
                            offsetHours: hours,
                            offsetMinutes: minutes,
                            offsetSeconds: 0
                        });
                        break;
                    case 'minutes':
                        record.set('schedule', {
                            count: form.down('#recurrence-number').getValue(),
                            timeUnit: timeUnitValue,
                            offsetMonths: 0,
                            offsetDays: 0,
                            lastDayOfMonth: lastDayOfMonth,
                            dayOfWeek: null,
                            offsetHours: hours,
                            offsetMinutes: minutes,
                            offsetSeconds: 0
                        });
                        break;
                }
            } else {
                record.set('schedule', null);
            }
            record.set('nextRun', startOnDate);

            var processorModel = Ext.create('Dxp.model.DataProcessor', {
                name: form.down('#file-formatter-combo').getValue()
            });
            record.setDataProcessor(processorModel);
            if (propertyForm.getRecord() && propertyForm.isVisible()) {
                record.getDataProcessor().propertiesStore = propertyForm.getRecord().properties();
            }

            //record.set('dataProcessor', {
            //    name: form.down('#file-formatter-combo').getValue()
            //});

            var selectorModel = Ext.create('Dxp.model.DataSelector', {
                name: dataSelectorCombo.getValue(),
                selectorType: selectedDataSelector.get('selectorType')
            });
            record.setDataSelector(selectorModel);

            switch (selectedDataSelector.get('selectorType')) {
                case 'DEFAULT_READINGS':
                    record.setStandardDataSelector(null);
                    readingTypesStore.each(function (record) {
                        arrReadingTypes.push(record.getData().readingType);
                    });
                    var timeFrameValue = form.down('#export-updated').getValue().updatedDataAndOrAdjacentData;

                    record.set('standardDataSelector', {
                        deviceGroup: {
                            id: form.down('#device-group-combo').getValue(),
                            name: form.down('#device-group-combo').getRawValue()
                        },
                        exportPeriod: {
                            id: form.down('#export-period-combo').getValue(),
                            name: form.down('#export-period-combo').getRawValue()
                        },
                        exportComplete: form.down('#data-selector-export-complete').getValue().exportComplete,
                        validatedDataOption: form.down('#data-selector-validated-data').getValue().validatedDataOption,
                        exportUpdate: form.down('#updated-data-trigger').getValue().exportUpdate,
                        updatePeriod: {
                            id: form.down('#update-window').getValue(),
                            name: form.down('#update-window').getRawValue()
                        },
                        exportAdjacentData: timeFrameValue,
                        updateWindow: timeFrameValue ? {
                            id: form.down('#timeFrame').getValue(),
                            name: form.down('#timeFrame').getRawValue()
                        } : {},
                        exportContinuousData: form.down('#continuous-data-radiogroup').getValue().exportContinuousData,
                        readingTypes: arrReadingTypes
                    });
                    break;
                case 'DEFAULT_USAGE_POINT_READINGS':
                    record.setStandardDataSelector(null);
                    readingTypesStore.each(function (record) {
                        arrReadingTypes.push(record.getData().readingType);
                    });

                    record.set('standardDataSelector', {
                        usagePointGroup: {
                            id: form.down('#usage-point-group-combo').getValue(),
                            name: form.down('#usage-point-group-combo').getRawValue()
                        },
                        exportPeriod: {
                            id: form.down('#export-period-combo').getValue(),
                            name: form.down('#export-period-combo').getRawValue()
                        },
                        exportComplete: form.down('#data-selector-export-complete').getValue().exportComplete,
                        validatedDataOption: form.down('#data-selector-validated-data').getValue().validatedDataOption,
                        exportUpdate: form.down('#updated-data-trigger').getValue().exportUpdate,
                        updatePeriod: {
                            id: form.down('#update-window').getValue(),
                            name: form.down('#update-window').getRawValue()
                        },
                        exportContinuousData: form.down('#continuous-data-radiogroup').getValue().exportContinuousData,
                        readingTypes: arrReadingTypes
                    });
                    break;
                case 'DEFAULT_EVENTS':
                    record.setStandardDataSelector(null);
                    eventTypesStore.each(function (record) {
                        arrEventTypes.push({eventFilterCode: record.getData().eventFilterCode});
                    });
                    record.set('standardDataSelector', {
                        deviceGroup: {
                            id: form.down('#device-group-combo').getValue(),
                            name: form.down('#device-group-combo').getRawValue()
                        },
                        exportPeriod: {
                            id: form.down('#export-period-combo').getValue(),
                            name: form.down('#export-period-combo').getRawValue()
                        },
                        exportContinuousData: form.down('#continuous-data-radiogroup').getValue().exportContinuousData,
                        eventTypeCodes: arrEventTypes
                    });
                    break;
                default:
                    record.set('standardDataSelector', null);
                    selectorPropertyForm.updateRecord();
                    record.getDataSelector().propertiesStore = selectorPropertyForm.getRecord().properties();
            }

            record.destinations();
            record.destinationsStore.removeAll();
            record.destinationsStore.add(page.down('#task-destinations-grid').getStore().data.items);

            record.endEdit();
            mainView.setLoading();
            record.save({
                backUrl: button.action === 'editTask' && me.fromDetails
                    ? me.getController('Uni.controller.history.Router').getRoute('administration/dataexporttasks/dataexporttask').buildUrl({taskId: record.getId()})
                    : me.getController('Uni.controller.history.Router').getRoute('administration/dataexporttasks').buildUrl(),
                success: function () {
                    if (button.action === 'editTask' && me.fromDetails) {
                        me.getController('Uni.controller.history.Router').getRoute('administration/dataexporttasks/dataexporttask').forward({taskId: record.getId()});
                    } else {
                        me.getController('Uni.controller.history.Router').getRoute('administration/dataexporttasks').forward();
                    }
                    if (button.action === 'editTask') {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('editExportTask.successMsg.saved', 'DES', 'Export task saved'));
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('addExportTask.successMsg', 'DES', 'Export task added'));
                    }
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        Ext.suspendLayouts();
                        form.getForm().markInvalid(json.errors);
                        formErrorsPanel.show();
                        Ext.resumeLayouts(true);
                    }
                },
                callback: function () {
                    mainView.setLoading(false);
                }
            })
        } else {
            formErrorsPanel.show();
        }
    },

    validateEventsGrid: function (emptyEventTypes) {
        var me = this,
            page = me.getAddPage(),
            form = page.down('#add-data-export-task-form');
        Ext.suspendLayouts();
        if (emptyEventTypes) {
            form.down('#eventTypesFieldContainer').setActiveError(me.requiredFieldText);
        } else {
            form.down('#eventTypesFieldContainer').unsetActiveError();
        }
        form.down('#eventTypesFieldContainer').doComponentLayout();
        Ext.resumeLayouts(true);
    },

    forwardToPreviousPage: function () {
        var router = this.getController('Uni.controller.history.Router'),
            splittedPath = router.currentRoute.split('/');

        splittedPath.pop();

        router.getRoute(splittedPath.join('/')).forward();
    },

    addReadingTypes: function () {
        var me = this,
            readingTypeStore = me.getStore('Dxp.store.LoadedReadingTypes');

        if (!me.readingTypesArray) {
            me.forwardToPreviousPage();
            return;
        }

        me.getApplication().fireEvent('changecontentevent', Ext.widget('AddReadingTypesToTaskSetup', {
            defaultFilters: {
                active: true,
                selectedreadingtypes: _.map(me.readingTypesArray, function (readingType) {
                    return readingType.readingType.mRID.toLowerCase();
                })
            }
        }));

        readingTypeStore.on('beforeload', function () {
            me.setAddBtnDisabled(true);
        }, {single: true});
        readingTypeStore.load();
    },

    onSelectionChange: function (component, selection) {
        this.setAddBtnDisabled(Ext.isEmpty(selection));
    },

    setAddBtnDisabled: function (disabled) {
        this.getAddReadingTypesSetup().down('#buttonsContainer button[name=add]').setDisabled(disabled);
    },

    saveFormValues: function () {
        var me = this,
            page = me.getAddPage(),
            form = page.down('#add-data-export-task-form'),
            formValues = form.getValues(),
            readingTypesStore = page.down('#readingTypesGridPanel').getStore(),
            destinationsStore = page.down('#task-destinations-grid').getStore(),
            eventTypesStore = page.down('#eventTypesGridPanel').getStore(),
            storeDestinations = [],
            arrReadingTypes = [],
            eventTypes = [];
        readingTypesStore.each(function (record) {
            arrReadingTypes.push(record.getData());
        });

        destinationsStore.each(function (record) {
            storeDestinations.push(record);
        });

        eventTypesStore.each(function (record) {
            eventTypes.push(record);
        });

        formValues.readingTypes = arrReadingTypes;
        formValues.destinations = storeDestinations;
        formValues.eventTypes = eventTypes;
        page.down('#data-selector-properties').updateRecord();
        formValues.dataSelectorProperties = page.down('#data-selector-properties').getRecord();
        page.down('grouped-property-form').updateRecord();
        formValues.groupedProperty = page.down('grouped-property-form').getRecord();
        formValues.dataProcessor = page.down('#file-formatter-combo').getValue();
        me.getStore('Dxp.store.Clipboard').set('addDataExportTaskValues', formValues);
    },

    setFormValues: function (view) {
        var me = this,
            obj = me.getStore('Dxp.store.Clipboard').get('addDataExportTaskValues'),
            page = me.getAddPage(),
            readingTypesArray = obj.readingTypes,
            destinationsArray = obj.destinations,
            eventTypesArray = obj.eventTypes,
            readingTypesGrid = page.down('#readingTypesGridPanel'),
            destinationsGrid = page.down('#task-destinations-grid'),
            eventTypesGrid = page.down('#eventTypesGridPanel'),
            emptyDestinationsLabel = page.down('#noDestinationsLabel'),
            emptyReadingTypesLabel = page.down('#noReadingTypesLabel'),
            destinationsStore = destinationsGrid.getStore(),
            gridStore = readingTypesGrid.getStore(),
            evenTypesStore = eventTypesGrid.getStore(),
            formatterStore = page.down('#file-formatter-combo').getStore();

        Ext.suspendLayouts();
        gridStore.removeAll();
        destinationsStore.removeAll();
        evenTypesStore.removeAll();

        if (me.readingTypesArray) {
            Ext.each(me.readingTypesArray, function (record) {
                gridStore.add(record);
            });
        } else {
            if (!Ext.isEmpty(readingTypesArray)) {
                Ext.each(readingTypesArray, function (readingType) {
                    gridStore.add(readingType);
                });
            }
        }

        if (gridStore.count() > 0) {
            emptyReadingTypesLabel.hide();
            readingTypesGrid.show();
        }


        if (me.eventTypesArray) {
            Ext.each(me.eventTypesArray, function (record) {
                evenTypesStore.add(record);
            });
        } else {
            if (!Ext.isEmpty(eventTypesArray)) {
                Ext.each(eventTypesArray, function (eventType) {
                    evenTypesStore.add(eventType);
                });
            }
        }

        if (evenTypesStore.count() > 0) {
            this.getNoEventTypesLabel().hide();
            eventTypesGrid.show();
        }


        Ext.each(me.destinationsArray, function (record) {
            if (me.destinationIndexToEdit != -1) {
                destinationsArray.splice(me.destinationIndexToEdit, 0, record);
            } else {
                destinationsArray.push(record);
            }
        });
        if (!Ext.isEmpty(destinationsArray)) {
            Ext.each(destinationsArray, function (destination) {
                destinationsStore.add(destination);
            });
        }
        me.destinationsArray = [];

        if (destinationsStore.count() > 0) {
            emptyDestinationsLabel.hide();
            destinationsGrid.show();
        } else {
            emptyDestinationsLabel.show();
            destinationsGrid.hide();
        }

        var formModel = Ext.create('Dxp.model.AddDataExportTaskForm', obj);
        view.down('#add-data-export-task-form').loadRecord(formModel);

        view.down('#data-selector-combo').setValue(formModel.get('readingTypeDataSelector.value.dataSelector'));
        view.down('#device-group-combo').setValue(formModel.get('readingTypeDataSelector.value.endDeviceGroup'));
        view.down('#usage-point-group-combo').setValue(formModel.get('readingTypeDataSelector.value.usagePointGroup'));
        view.down('#export-period-combo').setValue(formModel.get('readingTypeDataSelector.value.exportPeriod'));


        view.down('#recurrence-trigger').setValue({recurrence: formModel.get('recurrence')});

        view.down('#data-selector-export-complete').setValue({exportComplete: formModel.get('exportComplete')});
        view.down('#data-selector-validated-data').setValue(formModel.get('validatedDataOption'));


        view.down('#updated-data-trigger').setValue({exportUpdate: formModel.get('exportUpdate')});
        view.down('#update-window').setValue(formModel.get('updateWindow'));
        view.down('#export-updated').setValue({updatedDataAndOrAdjacentData: formModel.get('updatedDataAndOrAdjacentData')});
        if (formModel.get('updateTimeFrame')) {
            view.down('#timeFrame').setValue(formModel.get('updateTimeFrame'));
        }
        view.down('#continuous-data-radiogroup').setValue({exportContinuousData: formModel.get('exportContinuousData')});

        if (obj.dataSelectorProperties) {
            page.down('#data-selector-properties').loadRecord(obj.dataSelectorProperties);
        }
        if (obj.groupedProperty) {
            page.down('grouped-property-form').loadRecord(obj.groupedProperty);
        }
        Ext.resumeLayouts(true);
    },

    checkRoute: function (token) {
        var me = this,
            relativeRegexp = /administration\/relativeperiods\/add/,
            destinationRegexp = /administration\/dataexporttasks\/(.*)\/destination/,
            readingRegexp = /administration\/dataexporttasks\/(.*)\/readingtypes/,
            editTaskRegexp = /administration\/dataexporttasks\/(.*)\/edit/,
            addTaskRegexp = /administration\/dataexporttasks\/add/;

        if (token.search(relativeRegexp) === -1 &&
            token.search(readingRegexp) === -1 &&
            token.search(destinationRegexp) === -1 &&
            token.search(editTaskRegexp) === -1 &&
            token.search(addTaskRegexp) === -1
        ) { // If the new destination is non of the above five...
            me.getStore('Dxp.store.Clipboard').clear('addDataExportTaskValues'); // ...clear the clipboard and
            Ext.util.History.un('change', this.checkRoute, this); // ...stop listening
        }
    },

    onRecurrenceTriggerChange: function (field, newValue, oldValue) {
        var me = this,
            page = me.getAddPage(),
            recurrenceNumberField = page.down('#recurrence-number'),
            recurrenceTypeCombo = page.down('#recurrence-type'),
            exportPeriodValue = page.down('#export-period-combo').getValue(),
            gridPreview = page.down('#schedule-preview'),
            scheduleRecords = [];

        if (newValue.recurrence && !recurrenceNumberField.getValue() && !recurrenceTypeCombo.getValue()) {
            recurrenceNumberField.setValue(recurrenceNumberField.minValue);
            recurrenceTypeCombo.setValue(recurrenceTypeCombo.store.getAt(0));
        }
        if (newValue.recurrence && exportPeriodValue) {
            me.fillGrid(0, scheduleRecords);
        }
        if (!newValue.recurrence && !gridPreview.isHidden()) {
            gridPreview.hide();
        }
    },

    fillScheduleGridOrNot: function () {
        var me = this,
            page = me.getAddPage(),
            exportPeriodValue = page.down('#export-period-combo').getValue(),
            recurrenceTriggerValue = page.down('#recurrence-trigger').getValue(),
            scheduleRecords = [];

        if (recurrenceTriggerValue.recurrence && exportPeriodValue) {
            me.fillGrid(0, scheduleRecords, arguments);
        }


    },

    fillGrid: function (i, scheduleRecords, fieldEventParams) {
        var me = this,
            page = me.getAddPage(),
            startOnDate = page.down('#start-on').getValue(),
            exportPeriodId = page.down('#export-period-combo').getValue(),
            updatePeriodId = page.down('#update-window').getValue(),
            everyAmount = page.down('#recurrence-number').getValue(),
            everyTimeKey = page.down('#recurrence-type').getValue(),
            date = moment(startOnDate).add(everyAmount * i, everyTimeKey),
            sendingData = {};

        if (!date.isValid()) {
            fieldEventParams[0].setValue(fieldEventParams[2]);
            return;
        }
        sendingData.zoneOffset = startOnDate.getTimezoneOffset();
        sendingData.date = date.valueOf();

        Ext.Ajax.request({
            url: '/api/tmr/relativeperiods/' + exportPeriodId + '/preview',
            method: 'PUT',
            jsonData: sendingData,
            success: function (response1) {
                if (page.down('#updated-data-trigger').getValue().exportUpdate && updatePeriodId !== null && updatePeriodId != '') {
                    Ext.Ajax.request({
                        url: '/api/tmr/relativeperiods/' + updatePeriodId + '/preview',
                        method: 'PUT',
                        jsonData: sendingData,
                        success: function (response2) {
                            me.populatePreview(i, scheduleRecords, response1, response2, fieldEventParams);
                        }
                    });
                } else {
                    me.populatePreview(i, scheduleRecords, response1, undefined, fieldEventParams);
                }
            }
        });
    },

    showDataSources: function (currentTaskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            taskModel = me.getModel('Dxp.model.DataExportTask'),
            store = me.getStore('Dxp.store.DataSources'),
            view;

        store.getProxy().setUrl(router.arguments);
        view = Ext.widget('data-sources-setup', {
            router: router,
            taskId: currentTaskId
        });
        me.getApplication().fireEvent('changecontentevent', view);
        taskModel.load(currentTaskId, {
            success: function (record) {
                me.getApplication().fireEvent('dataexporttaskload', record);
                view.down('#tasks-view-menu  #tasks-view-link').setText(record.get('name'));
            }
        });
    },

    populatePreview: function (i, scheduleRecords, response1, response2, fieldEventParams) {
        var me = this,
            obj = Ext.decode(response1.responseText, true),
            scheduleRecord = Ext.create('Dxp.model.SchedulePeriod'),
            startDateLong = obj.start.date,
            zoneOffset = obj.start.zoneOffset || obj.end.zoneOffset,
            endDateLong = obj.end.date,
            startZonedDate,
            endZonedDate,
            startZonedUpdateDate,
            endZonedUpdateDate,
            page = me.getAddPage(),
            startOnDate = page.down('#start-on').getValue(),
            everyAmount = page.down('#recurrence-number').getValue(),
            everyTimeKey = page.down('#recurrence-type').getValue(),
            gridPreview = page.down('#schedule-preview'),
            grid = page.down('add-schedule-grid'),
            obj2,
            startUpdateLong,
            zoneUpdateOffset,
            endUpdateLong;
        if (response2) {
            obj2 = Ext.decode(response2.responseText, true);
            startUpdateLong = obj2.start.date;
            zoneUpdateOffset = obj2.start.zoneOffset || obj2.end.zoneOffset;
            endUpdateLong = obj2.end.date;
            page.down('#startUpdatePeriod').setVisible(true);
            page.down('#endUpdatePeriod').setVisible(true);
        } else {
            page.down('#startUpdatePeriod').setVisible(false);
            page.down('#endUpdatePeriod').setVisible(false);
        }
        if (typeof startDateLong !== 'undefined') {
            var startDate = new Date(startDateLong),
                startDateUtc = startDate.getTime() + (startDate.getTimezoneOffset() * 60000);
            startZonedDate = startDateUtc - (60000 * zoneOffset);
        }
        if (typeof endDateLong !== 'undefined') {
            var endDate = new Date(endDateLong),
                endDateUtc = endDate.getTime() + (endDate.getTimezoneOffset() * 60000);
            endZonedDate = endDateUtc - (60000 * zoneOffset);
        }
        if (typeof startUpdateLong !== 'undefined') {
            var startUpdateDate = new Date(startUpdateLong),
                startUpdateDateUtc = startUpdateDate.getTime() + (startUpdateDate.getTimezoneOffset() * 60000);
            startZonedUpdateDate = startUpdateDateUtc - (60000 * zoneUpdateOffset);
        }
        if (typeof endUpdateLong !== 'undefined') {
            var endUpdateDate = new Date(endUpdateLong),
                endUpdateDateUtc = endUpdateDate.getTime() + (endUpdateDate.getTimezoneOffset() * 60000);
            endZonedUpdateDate = endUpdateDateUtc - (60000 * zoneUpdateOffset);
        }
        scheduleRecord.beginEdit();
        scheduleRecord.set('schedule', moment(startOnDate).add(everyAmount * i, everyTimeKey).valueOf());
        scheduleRecord.set('start', startZonedDate);
        scheduleRecord.set('end', endZonedDate);
        scheduleRecord.set('updateStart', startZonedUpdateDate);
        scheduleRecord.set('updateEnd', endZonedUpdateDate);
        scheduleRecord.endEdit();
        scheduleRecords.push(scheduleRecord);
        if (i < 4) {
            i++;
            me.fillGrid(i, scheduleRecords, fieldEventParams);
        } else {
            grid.getStore().loadData(scheduleRecords, false);
            gridPreview.show();
        }
    },

    fillInDisplayNames: function (eventType /*type: EventTypeForAddTaskGrid*/) {
        eventType.deviceTypeName = this.getEventTypePartDisplayName(1, eventType.eventFilterCode);
        eventType.deviceDomainName = this.getEventTypePartDisplayName(2, eventType.eventFilterCode);
        eventType.deviceSubDomainName = this.getEventTypePartDisplayName(3, eventType.eventFilterCode);
        eventType.deviceEventOrActionName = this.getEventTypePartDisplayName(4, eventType.eventFilterCode);
        return eventType;
    },

    getEventTypePartDisplayName: function (partNr, eventTypeAsString) {
        var me = this,
            store,
            parts = eventTypeAsString.split('.'),
            selectedValue;

        if (parts[partNr - 1] === '*') {
            return '*';
        }
        selectedValue = parseInt(parts[partNr - 1]);
        switch (partNr) {
            case 1:
                store = me.deviceTypesStore;
                break;
            case 2:
                store = me.deviceDomainsStore;
                break;
            case 3:
                store = me.deviceSubDomainsStore;
                break;
            case 4:
                store = me.deviceEventOrActionsStore;
                break;
        }
        var index = store.findExact('value', selectedValue);
        if (index === -1) { // shouldn't be the case, though
            return '?';
        }
        var record = store.getAt(index);
        if (!record) { // shouldn't be the case, though
            return '?';
        }
        return record.get('displayName');
    },

    exportUpdatedEnableDisabled: function () {
        var me = this,
            page = me.getAddPage();
        if (!page.down('#export-updated').getValue().updatedDataAndOrAdjacentData) {
            page.down('#timeFrame').disable();
        } else {
            page.down('#timeFrame').enable();
        }
    },

    updatedDataEnableDisable: function () {
        var me = this,
            page = me.getAddPage();
        if (!page.down('#updated-data-trigger').getValue().exportUpdate) {
            page.down('#update-window').disable();
        } else {
            page.down('#update-window').enable();
        }
    },

    recurrenceEnableDisable: function () {
        var me = this,
            page = me.getAddPage();
        if (!page.down('#recurrence-trigger').getValue().recurrence) {
            page.down('#recurrence-values').disable();
        } else {
            page.down('#recurrence-values').enable();
        }
    },

    recurrenceChange: function (field, newValue, oldValue) {
        var me = this;
        me.onRecurrenceTriggerChange(field, newValue, oldValue);
        me.recurrenceEnableDisable();
    },

    updatedDataChange: function () {
        var me = this;
        me.updatedDataEnableDisable();
        me.fillScheduleGridOrNot();
    },

    showSelectedReadingTypes: function () {
        var me = this;
        var widget = Ext.widget('dataExportSelectedReadingTypes');
        widget.setTitle(me.setCountOfSelectedReadingTypes());
        widget.show();
    },

    showNoFoundPanel: function (cmp) {
        var me = this,
            grid = me.getAddReadingTypesToTaskBulk();
        cmp.getSelectionCounter().setText(me.setCountOfSelectedReadingTypes());
        if (grid.hiddenSelection.length) {
            cmp.getuncheckAllBtn().setDisabled(false);
            cmp.getInfoBtn().show();
        } else {
            cmp.getuncheckAllBtn().setDisabled(true);
            cmp.getInfoBtn().hide();
        }
    },

    setCountOfSelectedReadingTypes: function () {
        var me = this,
            grid = me.getAddReadingTypesToTaskBulk();
        return grid.counterTextFn(grid.hiddenSelection.length)
    },

    uncheckAll: function () {
        var me = this,
            grid = me.getAddReadingTypesToTaskBulk();
        grid.getUncheckAllButton().fireEvent('click', grid.getUncheckAllButton());
    }
});

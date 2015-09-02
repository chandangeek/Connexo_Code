Ext.define('Dxp.controller.Tasks', {
    extend: 'Ext.app.Controller',

    requires: [
        'Dxp.privileges.DataExport'
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
        'Uni.form.field.DateTime'
    ],

    stores: [
        'Dxp.store.DeviceGroups',
        'Dxp.store.DaysWeeksMonths',
        'Dxp.store.ExportPeriods',
        'Dxp.store.FileFormatters',
        'Dxp.store.ReadingTypes',
        'Dxp.store.DataExportTasks',
        'Dxp.store.DataExportTasksHistory',
        'Dxp.store.ReadingTypesForTask',
        'Dxp.store.DataSources',
        'Dxp.store.LoadedReadingTypes',
        'Dxp.store.AdaptedReadingsForBulk',
        'Dxp.store.UnitsOfMeasure',
        'Dxp.store.TimeOfUse',
        'Dxp.store.Intervals',
        'Dxp.store.Clipboard',
        'Dxp.store.DataSelectors'
    ],

    models: [
        'Dxp.model.DeviceGroup',
        'Dxp.model.DayWeekMonth',
        'Dxp.model.ExportPeriod',
        'Dxp.model.FileFormatter',
        'Dxp.model.SchedulePeriod',
        'Dxp.model.ReadingType',
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
        }
    ],

    fromDetails: false,
    fromEdit: false,
    taskModel: null,
    taskId: null,

    readingTypesArray: null,
    destinationsArray: [],

    destinationToEdit: null,
    destinationIndexToEdit: -1,

    init: function () {
        this.control({
            'data-export-tasks-add #recurrence-trigger': {
                change: this.onRecurrenceTriggerChange
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
            'data-export-tasks-add #add-export-task-button': {
                click: this.addTask
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
            'tasks-history-action-menu': {
                click: this.chooseAction
            },
            'AddReadingTypesToTaskBulk': {
                selectionchange: this.onSelectionChange
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
            router = me.getController('Uni.controller.history.Router'),
            taskModel = me.getModel('Dxp.model.DataExportTask'),
            view = Ext.widget('data-export-tasks-details', {
                router: router,
                taskId: currentTaskId
            }),
            actionsMenu = view.down('dxp-tasks-action-menu');

        me.fromDetails = true;
        me.getApplication().fireEvent('changecontentevent', view);

        taskModel.load(currentTaskId, {
            success: function (record) {
                var detailsForm = view.down('dxp-tasks-preview-form'),
                    propertyForm = detailsForm.down('#task-properties-preview'),
                    selectorPropertyForm = detailsForm.down('#data-selector-properties-preview'),
                    deviceGroup = detailsForm.down('#data-selector-deviceGroup-preview'),
                    exportPeriod = detailsForm.down('#data-selector-exportPeriod-preview'),
                    readingTypes = detailsForm.down('#data-selector-readingTypes-preview');

                actionsMenu.record = record;
                actionsMenu.down('#view-details').hide();
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
                if (record.properties() && record.properties().count()) {
                    propertyForm.loadRecord(record);
                }
                if ((record.getDataSelector()) && (record.getDataSelector().properties()) && (record.getDataSelector().properties().count())) {
                    selectorPropertyForm.setVisible(true);
                    deviceGroup.setVisible(false);
                    exportPeriod.setVisible(false);
                    readingTypes.setVisible(false);
                    selectorPropertyForm.loadRecord(record.getDataSelector());

                } else {
                    selectorPropertyForm.setVisible(false);
                    deviceGroup.setVisible(true);
                    exportPeriod.setVisible(true);
                    readingTypes.setVisible(true);
                }
            }
        });
    },

    showDataExportTaskHistory: function (currentTaskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            store = me.getStore('Dxp.store.DataExportTasksHistory'),
            taskModel = me.getModel('Dxp.model.DataExportTask'),
            view;

        store.getProxy().setUrl(router.arguments);
        view = Ext.widget('data-export-tasks-history', {
            router: router,
            taskId: currentTaskId
        });

        me.getApplication().fireEvent('changecontentevent', view);
        Ext.getStore('Dxp.store.DataExportTasksHistory').load();

        taskModel.load(currentTaskId, {
            success: function (record) {
                view.down('#tasks-view-menu  #tasks-view-link').setText(record.get('name'));
            }
        });
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
            preview.down('tasks-history-action-menu').record = record;

            if (record.get('status') === 'Failed') {
                previewForm.down('#reason-field').show();
            } else {
                previewForm.down('#reason-field').hide();
            }
            previewForm.loadRecord(record);

            if (record.data.properties && record.data.properties.length) {
                previewForm.down('property-form').loadRecord(record.getTask());
            }

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

    showAddDestination: function (button) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            addDestinationRoute = router.currentRoute + '/destination';
        me.destinationsArray = [];
        me.saveFormValues();
        router.getRoute(addDestinationRoute).forward();

    },

    addDestination: function () {
        var me = this,
            view = Ext.create('Dxp.view.tasks.AddDestination');
        me.getApplication().fireEvent('changecontentevent', view);

        if (me.destinationToEdit) {
            view.down('#save-destination-button').setText(Uni.I18n.translate('general.save', 'DES', 'Save'));
            view.down('#add-destination-form').setTitle(Uni.I18n.translate('dataExport.editDestination', 'DES', 'Edit destination'));
            me.showAllDestinationAttributes(false);
            var type = me.destinationToEdit.get('type');
            if (type == 'FILE') {
                me.showFileDestinationAttributes(true);
                view.down('#destination-methods-combo').setValue('FILE');
                view.down('#destination-file-name').setValue(me.destinationToEdit.get('fileName'));
                view.down('#destination-file-extension').setValue(me.destinationToEdit.get('fileExtension'));
                view.down('#destination-file-location').setValue(me.destinationToEdit.get('fileLocation'));
            } else if (type == 'EMAIL') {
                me.showMailDestinationAttributes(true);
                view.down('#destination-methods-combo').setValue('EMAIL');
                view.down('#destination-recipients').setValue(me.destinationToEdit.get('recipients'));
                view.down('#destination-subject').setValue(me.destinationToEdit.get('subject'));
                view.down('#destination-attachment-name').setValue(me.destinationToEdit.get('fileName'));
                view.down('#destination-attachment-extension').setValue(me.destinationToEdit.get('fileExtension'));
            }
        } else {
            me.destinationIndexToEdit = -1;
            view.down('#destination-methods-combo').setValue('FILE');
        }
    },


    showAllDestinationAttributes: function (visible) {
        this.showFileDestinationAttributes(visible);
        this.showMailDestinationAttributes(visible);
    },

    showFileDestinationAttributes: function (visible) {
        var me = this,
            page = me.getAddDestinationPage();
        page.down('#destination-file-name').setVisible(visible);
        page.down('#destination-file-extension').setVisible(visible);
        page.down('#destination-file-location').setVisible(visible);

        page.down('#destination-file-name').disabled = !visible;
        page.down('#destination-file-extension').disabled = !visible;
        page.down('#destination-file-location').disabled = !visible;
    },

    showMailDestinationAttributes: function (visible) {
        var me = this,
            page = me.getAddDestinationPage();
        page.down('#destination-recipients').setVisible(visible);
        page.down('#destination-subject').setVisible(visible);
        page.down('#destination-attachment-name').setVisible(visible);
        page.down('#destination-attachment-extension').setVisible(visible);

        page.down('#destination-recipients').disabled = !visible;
        page.down('#destination-subject').disabled = !visible;
        page.down('#destination-attachment-name').disabled = !visible;
        page.down('#destination-attachment-extension').disabled = !visible;
    },

    updateDestinationAttributes: function () {
        var me = this,
            page = me.getAddDestinationPage();
        var method = page.down('#destination-methods-combo').getValue();
        me.showAllDestinationAttributes(false);
        if (method === 'FILE') {
            me.showFileDestinationAttributes(true);
        }
        if (method === 'EMAIL') {
            me.showMailDestinationAttributes(true);
        }

    },

    showAddExportTask: function () {
        var me = this,
            view = Ext.create('Dxp.view.tasks.Add'),
        //fileFormatterCombo = view.down('#file-formatter-combo'),
            dataSelectorCombo = view.down('#data-selector-combo'),
            deviceGroupCombo = view.down('#device-group-combo'),
            exportPeriodCombo = view.down('#export-period-combo'),
            destinationsStore = view.down('#task-destinations-grid').getStore(),
            recurrenceTypeCombo = view.down('#recurrence-type'),
            readingTypesStore = view.down('#readingTypesGridPanel').getStore();

        //me.destinationsArray = [];

        me.getApplication().fireEvent('changecontentevent', view);

        Ext.util.History.on('change', this.checkRoute, this);
        me.taskModel = null;
        me.taskId = null;
        me.fromEdit = false;

        readingTypesStore.removeAll();
        destinationsStore.removeAll();
        exportPeriodCombo.store.load({
            params: {
                category: 'relativeperiod.category.dataExport'
            }
        });

        deviceGroupCombo.store.load(function () {
            if (this.getCount() === 0) {
                deviceGroupCombo.allowBlank = true;
                deviceGroupCombo.hide();
                view.down('#no-device').show();
            }
        });


        dataSelectorCombo.store.load(function () {
            recurrenceTypeCombo.setValue(recurrenceTypeCombo.store.findRecord('name','months'));
            if (me.getStore('Dxp.store.Clipboard').get('addDataExportTaskValues')) {
                me.setFormValues(view);
            }
        });

    },

    showEditExportTask: function (taskId) {

        var me = this,

            router = me.getController('Uni.controller.history.Router'),
            view;
        //me.destinationsArray = [];
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

        var taskModel = me.getModel('Dxp.model.DataExportTask'),
            taskForm = view.down('#add-data-export-task-form'),
            fileFormatterCombo = view.down('#file-formatter-combo'),
            deviceGroupCombo = view.down('#device-group-combo'),
            exportPeriodCombo = view.down('#export-period-combo'),
            dataSelectorCombo = view.down('#data-selector-combo'),
            emptyDestinationsLabel = view.down('#noDestinationsLabel'),
            destinationsGrid = view.down('#task-destinations-grid'),
            destinationsStore = view.down('#task-destinations-grid').getStore(),
            readingTypesStore = view.down('#readingTypesGridPanel').getStore(),
            recurrenceTypeCombo = view.down('#recurrence-type');

        //readingTypesStore.removeAll();
        destinationsStore.removeAll();

        dataSelectorCombo.disabled = true;
        me.fromEdit = true;
        me.taskId = taskId;
        Ext.util.History.on('change', this.checkRoute, this);


        fileFormatterCombo.store.load({
            callback: function () {
                taskModel.load(taskId, {
                    success: function (record) {

                        var destinations = record.get('destinations');
                        if (record.destinationsStore.count() > 0) {
                            emptyDestinationsLabel.hide();
                            destinationsGrid.store.add(record.destinations().data.items);

                            /*Ext.each(record.destinations().data.items, function (record) {
                             me.destinationsArray.push(record);
                             });*/

                            destinationsGrid.show();
                        }


                        var dataSelector = record.get('dataSelector');

                        dataSelectorCombo.store.load({
                            callback: function () {
                                dataSelectorCombo.setValue(dataSelectorCombo.store.getById(record.getDataSelector().data.name));
                                if (!record.getDataSelector().get('isDefault')) {
                                    taskForm.down('#data-selector-properties').loadRecord(record.getDataSelector());
                                }
                            }
                        });
                        var schedule = record.get('schedule');
                        me.taskModel = record;
                        me.getApplication().fireEvent('dataexporttaskload', record);
                        taskForm.setTitle(Uni.I18n.translate('general.edit', 'DES', 'Edit') + " '" + record.get('name') + "'");
                        if (me.getStore('Dxp.store.Clipboard').get('addDataExportTaskValues')) {
                            me.setFormValues(view);
                        } else {
                            taskForm.loadRecord(record);

                            if (record.getDataSelector().get('isDefault')) {

                                var taskReadingTypes = record.getStandardDataSelector().get('readingTypes'),
                                    readingTypesGrid = view.down('#readingTypesGridPanel');

                                readingTypesGrid.getStore().removeAll();

                                Ext.each(taskReadingTypes, function (readingType) {
                                    readingTypesGrid.getStore().add({readingType: readingType})
                                });

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
                                            deviceGroupCombo.hide();
                                            view.down('#no-device').show();
                                        }
                                        deviceGroupCombo.setValue(deviceGroupCombo.store.getById(record.getStandardDataSelector().data.deviceGroup.id));
                                    },
                                });


                            }
                            /*else {
                             taskForm.down('#data-selector-properties').loadRecord(record.getDataSelector());
                             }*/
                            fileFormatterCombo.setValue(fileFormatterCombo.store.getById(record.data.dataProcessor.name));
                            if (record.data.nextRun && (record.data.nextRun !== 0)) {
                                view.down('#recurrence-trigger').setValue({recurrence: true});
                                view.down('#recurrence-number').setValue(schedule.count);
                                recurrenceTypeCombo.setValue(schedule.timeUnit);
                                view.down('#start-on').setValue(record.data.nextRun);
                            } else {
                                recurrenceTypeCombo.setValue(recurrenceTypeCombo.store.getAt(2));
                            }
                            if (record.properties() && record.properties().count()) {
                                taskForm.down('grouped-property-form').loadRecord(record);
                            }


                        }
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
            exportPeriod = previewForm.down('#data-selector-exportPeriod-preview'),
            readingTypes = previewForm.down('#data-selector-readingTypes-preview'),
            propertyForm = previewForm.down('#task-properties-preview');

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

        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        preview.down('dxp-tasks-action-menu').record = record;

        if (record.properties() && record.properties().count()) {
            propertyForm.loadRecord(record);
        }

        if ((record.getDataSelector()) && (record.getDataSelector().properties()) && (record.getDataSelector().properties().count())) {
            selectorPropertyForm.show();
            deviceGroup.hide();
            exportPeriod.hide();
            readingTypes.hide();
            selectorPropertyForm.loadRecord(record.getDataSelector());
        } else {
            selectorPropertyForm.hide();
            deviceGroup.show();
            exportPeriod.show();
            readingTypes.show();
        }


        Ext.resumeLayouts();
    },

    chooseDestinationAction: function (menu, item) {
        var me = this;
        switch (item.action) {
            case 'removeDestination':
                var page = me.getAddPage(),
                    destinationsGrid = page.down('#task-destinations-grid');
                destinationsGrid.getStore().remove(menu.record);
                break;
            case 'editDestination':
                me.destinationToEdit = menu.record;
                var page = me.getAddPage(),
                    destinationsGrid = page.down('#task-destinations-grid');
                // edit = remove + add new
                me.destinationIndexToEdit = destinationsGrid.getStore().indexOf(menu.record);
                destinationsGrid.getStore().remove(menu.record);
                me.showAddDestination();
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
            case 'viewLog':
                route = 'administration/dataexporttasks/dataexporttask/history/occurrence';
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
            msg: Uni.I18n.translate('dataExportTasks.runMsg', 'DES', 'The data export task will be queued to run at the earliest possible time.'),
            title: Uni.I18n.translate('general.runDataExportTask', 'DES', 'Run data export task') + ' ' + record.data.name + '?'
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
            method: 'POST',
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
                            if (rec.properties() && rec.properties().count()) {
                                view.down('property-form').loadRecord(rec);
                            }
                        }
                    });
                }
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('dataExportTasks.run', 'DES', 'Data export task run'));
            },
            failure: function (response) {
                var res = Ext.JSON.decode(response.responseText);
                confWindow.update(res.errors[0].msg);
                confWindow.setVisible(true);
            }
        });
    },

    removeTask: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation');
        confirmationWindow.show({
            msg: Uni.I18n.translate('general.remove.msg', 'DES', 'This data export task will no longer be available.'),
            title: Uni.I18n.translate('general.remove', 'DES', 'Remove') + '&nbsp' + record.data.name + '?',
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
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.remove.confirm.msg', 'DES', 'Data export task removed'));
            },
            failure: function (object, operation) {
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
                        ui: 'notification-error',
                        title: Uni.I18n.translate('general.remove.error.msg', 'DES', 'Remove operation failed'),
                        msg: errorText,
                        modal: false,
                        icon: Ext.MessageBox.ERROR
                    })
                }
            }
        });
    },

    updateDataSelectorProperties: function (field, newValue) {
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

        formatterCombo.store.load({
            params: {
                selector: newValue
            }
        }, function () {
            if (me.getStore('Dxp.store.Clipboard').get('addDataExportTaskValues')) {
                me.setFormValues(page);
            } else {
                fileFormatterCombo.setValue(this.getAt(0));
            }
        });
        formatterContainer.show();
        formatterTitle.show();
        formatterCombo.show();
        if (record.get('isDefault')) {
            me.hideDefaultDataSelectorProperties(false);
        } else {
            me.hideDefaultDataSelectorProperties(true);
            if (record && record.properties() && record.properties().count()) {
                propertyForm.addEditPage = true;
                propertyForm.loadRecord(record);
                propertyForm.show();
            } else {
                propertyForm.hide();
            }
        }
    },

    hideDefaultDataSelectorProperties: function (hidden) {
        var me = this;
        var page = me.getAddPage();
        page.down('#device-group-container').setVisible(!hidden);
        page.down('#readingTypesFieldContainer').setVisible(!hidden);
        page.down('#export-periods-container').setVisible(!hidden);
        page.down('#data-selector-properties').setVisible(hidden);
    },


    updateProperties: function (field, newValue) {
        var me = this,
            page = me.getAddPage(),
            record = Ext.getStore('Dxp.store.FileFormatters').getById(newValue),
            propertyForm = page.down('grouped-property-form');

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
            selection = grid.getView().getSelectionModel().getSelection();

        if (selection.length > 0) {
            Ext.each(selection, function (record) {
                me.readingTypesArray.push({readingType: record.get('readingType')});
            });
        }

        me.forwardToPreviousPage();
    },

    addDestinationToGrid: function (button) {
        var me = this;
        me.destinationToEdit = null;
        me.doAddDestinationToGrid(button);
    },


    doAddDestinationToGrid: function (button) {
        var me = this;
        //edit destination was cancelled, add the old one again
        if (me.destinationToEdit) {
            me.destinationsArray.push(me.destinationToEdit);
            me.destinationToEdit = null;
            me.destinationIndexToEdit = -1;
            me.forwardToPreviousPage();
        } else {
            var page = me.getAddDestinationPage();
            var form = page.down('#add-destination-form');
            var formErrorsPanel = form.down('#form-errors');
            var destinationModel;
            if (form.isValid()) {
                var formValues = form.getForm().getValues();
                if (formValues['method'] === 'FILE') {
                    //tooltip & method duplicated from destination model, have not found another way!
                    destinationModel = Ext.create('Dxp.model.Destination', {
                        type: 'FILE',
                        fileName: formValues['fileName'],
                        fileExtension: formValues['fileExtension'],
                        fileLocation: formValues['fileLocation'],
                        method: Uni.I18n.translate('general.saveFile', 'DES', 'Save file'),
                        destination: formValues['fileLocation'] + '/' + formValues['fileName'] + '.' + formValues['fileExtension'],
                        tooltiptext: Uni.I18n.translate('general.fileLocation', 'DES', 'File location') + ': ' + formValues['fileLocation'] + '&lt;br/&gt;' +
                        Uni.I18n.translate('general.fileName', 'DES', 'File name') + ': ' + formValues['fileName'] + '&lt;br/&gt;' +
                        Uni.I18n.translate('general.fileExtension', 'DES', 'File extension') + ': ' + formValues['fileExtension']
                    })
                } else if (formValues['method'] === 'EMAIL') {
                    destinationModel = Ext.create('Dxp.model.Destination', {
                        type: 'EMAIL',
                        fileName: formValues['attachmentName'],
                        fileExtension: formValues['attachmentExtension'],
                        recipients: formValues['recipients'],
                        subject: formValues['subject'],
                        method: Uni.I18n.translate('dataExportdestinations.email', 'DES', 'Email'),
                        destination: formValues['recipients'],
                        tooltiptext: Uni.I18n.translate('dataExportdestinations.recipients', 'DES', 'Recipients') + ': ' + formValues['recipients'] + '&lt;br/&gt;' +
                        Uni.I18n.translate('general.subject', 'DES', 'Subject') + ': ' + formValues['subject'] + '&lt;br/&gt;' +
                        Uni.I18n.translate('general.fileName', 'DES', 'File name') + ': ' + formValues['attachmentName'] + '&lt;br/&gt;' +
                        Uni.I18n.translate('general.fileExtension', 'DES', 'File extension') + ': ' + formValues['attachmentExtension']
                    })
                }
                me.destinationsArray.push(destinationModel);
                me.forwardToPreviousPage();
            }
            else {
                formErrorsPanel.show();
            }
        }
    },


    addTask: function (button) {
        var me = this,
            page = me.getAddPage(),
            form = page.down('#add-data-export-task-form'),
            formErrorsPanel = form.down('#form-errors'),
            propertyForm = form.down('grouped-property-form'),
            selectorPropertyForm = form.down('#data-selector-properties'),
            lastDayOfMonth = false,
            startOnDate,
            timeUnitValue,
            dayOfMonth,
            hours,
            minutes;

        propertyForm.updateRecord();


        var dataSelectorCombo = form.down('#data-selector-combo');
        var selectedDataSelector = dataSelectorCombo.findRecord(dataSelectorCombo.valueField, dataSelectorCombo.getValue());
        if ((selectedDataSelector) && (!selectedDataSelector.get('isDefault'))) {
            form.down('#export-period-combo').allowBlank = true;
            form.down('#device-group-combo').allowBlank = true;
        }

        var emptyReadingTypes = (selectedDataSelector) && (selectedDataSelector.get('isDefault')) && (page.down('#readingTypesGridPanel').getStore().data.items.length == 0);
        if (emptyReadingTypes) {
            form.down('#readingTypesGridPanel').addCls('error-border');
            form.down('#readingTypesFieldContainer').setActiveError('This field is required');

            form.getForm().markInvalid(
                Ext.create('Object', {
                    id: 'readingTypeDataSelector.value.readingTypes',
                    msg: 'This field is required'
                }));

            formErrorsPanel.show();
        }

        var emptyDestinations = page.down('#task-destinations-grid').getStore().data.items.length == 0;
        if (emptyDestinations) {
            //form.down('#destinationsFieldcontainer').addCls('error-border');
            //form.down('#destinationsFieldcontainer').setActiveError('This field is required');
            form.down('#noDestinationsErrorLabel').show();

            form.getForm().markInvalid(
                Ext.create('Object', {
                    id: 'destinationsFieldcontainer',
                    msg: 'This field is required'
                }));

            formErrorsPanel.show();
        }

        if ((form.isValid()) && (!emptyReadingTypes) && (!emptyDestinations)) {
            var record = me.taskModel || Ext.create('Dxp.model.DataExportTask'),
                readingTypesStore = page.down('#readingTypesGridPanel').getStore(),
                arrReadingTypes = [];

            record.beginEdit();
            if (!formErrorsPanel.isHidden()) {
                formErrorsPanel.hide();
                form.down('#readingTypesGridPanel').removeCls('error-border');
            }

            if (propertyForm.getRecord()) {
                record.propertiesStore = propertyForm.getRecord().properties();
            }
            record.set('name', form.down('#task-name').getValue());

            if (form.down('#recurrence-trigger').getValue().recurrence) {
                startOnDate = moment(form.down('#start-on').getValue()).valueOf();
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
                }
                record.set('nextRun', startOnDate);
            } else {
                record.set('schedule', null);
            }
            record.set('dataProcessor', {
                name: form.down('#file-formatter-combo').getValue()
            });

            //var dataSelectorCombo = form.down('#data-selector-combo');

            var selectorModel = Ext.create('Dxp.model.DataSelector', {
                name: dataSelectorCombo.getValue(),
            })
            record.setDataSelector(selectorModel);

            var selectedDataSelector = dataSelectorCombo.findRecord(dataSelectorCombo.valueField, dataSelectorCombo.getValue());


            if (selectedDataSelector.get('isDefault')) {

                record.setStandardDataSelector(null);
                readingTypesStore.each(function (record) {
                    arrReadingTypes.push(record.getData().readingType);
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
                    readingTypes: arrReadingTypes
                });
            } else {
                record.set('standardDataSelector', null);
                //record.setDataSelector(selectorModel);
                selectorPropertyForm.updateRecord();

                record.propertiesStore.add(selectorPropertyForm.getRecord().properties().data.items)
            }
            record.destinations();
            record.destinationsStore.removeAll();
            record.destinationsStore.add(page.down('#task-destinations-grid').getStore().data.items);
            /*record.destinationsStore.add([Ext.create('Dxp.model.Destination', {
             fileName: 'test',
             fileExtension: 'csv',
             fileLocation: '.',
             type: 'FILE',
             })]);*/


            record.endEdit();
            record.save({
                success: function () {
                    if (button.action === 'editTask' && me.fromDetails) {
                        me.getController('Uni.controller.history.Router').getRoute('administration/dataexporttasks/dataexporttask').forward({taskId: record.getId()});
                    } else {
                        me.getController('Uni.controller.history.Router').getRoute('administration/dataexporttasks').forward();
                    }
                    if (button.action === 'editTask') {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('editDataExportTask.successMsg.saved', 'DES', 'Data export task saved'));
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('addDataExportTask.successMsg', 'DES', 'Data export task added'));
                    }
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        form.getForm().markInvalid(json.errors);
                        formErrorsPanel.show();
                    }
                }
            })
        } else {
            formErrorsPanel.show();
        }
    },


    forwardToPreviousPage: function () {
        var router = this.getController('Uni.controller.history.Router'),
            splittedPath = router.currentRoute.split('/');

        splittedPath.pop();

        router.getRoute(splittedPath.join('/')).forward();
    },

    addReadingTypes: function () {
        if (!this.readingTypesArray) {
            this.forwardToPreviousPage();
            return;
        }

        this.getApplication().fireEvent('changecontentevent', Ext.widget('AddReadingTypesToTaskSetup'));
        this.loadReadingTypes();
    },


    loadReadingTypes: function () {
        var me = this,
            readingTypeStore = me.getStore('Dxp.store.LoadedReadingTypes');

        // Tell the REST side what readingTypes to exclude (because they're already assigned)
        if (Ext.isArray(me.readingTypesArray) && !Ext.isEmpty(me.readingTypesArray)) {
            var mRIDs = [];
            me.readingTypesArray.forEach(function (readingType) {
                mRIDs.push(readingType.readingType.mRID.toLowerCase());
            });
            Ext.ComponentQuery.query('viewport')[0].down('dxp-view-tasks-addreadingtypestotaskfilter').setSelectedReadings(mRIDs);
        }

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
            storeDestinations = [],
            arrReadingTypes = [];

        readingTypesStore.each(function (record) {
            arrReadingTypes.push(record.getData());
        });

        destinationsStore.each(function (record) {
            storeDestinations.push(record);
        });

        formValues.readingTypes = arrReadingTypes;
        formValues.destinations = storeDestinations;
        me.getStore('Dxp.store.Clipboard').set('addDataExportTaskValues', formValues);

    },

    setFormValues: function (view) {
        var me = this,
            obj = me.getStore('Dxp.store.Clipboard').get('addDataExportTaskValues'),
            page = me.getAddPage(),
            readingTypesArray = obj.readingTypes,
            destinationsArray = obj.destinations,
            readingTypesGrid = page.down('#readingTypesGridPanel'),
            destinationsGrid = page.down('#task-destinations-grid'),
            emptyDestinationsLabel = page.down('#noDestinationsLabel'),
            destinationsStore = destinationsGrid.getStore(),
            gridStore = readingTypesGrid.getStore();

        gridStore.removeAll();
        destinationsStore.removeAll();

        if (me.readingTypesArray) {
            Ext.each(me.readingTypesArray, function (record) {
                gridStore.add(record);
            });
            //me.readingTypesArray = null;
        } else {
            if (!Ext.isEmpty(readingTypesArray)) {
                Ext.each(readingTypesArray, function (readingType) {
                    gridStore.add(readingType);
                });
            }
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
        }

        var formModel = Ext.create('Dxp.model.AddDataExportTaskForm', obj);
        view.down('#add-data-export-task-form').loadRecord(formModel);

        view.down('#data-selector-combo').setValue(formModel.get('readingTypeDataSelector.value.dataSelector'));
        view.down('#device-group-combo').setValue(formModel.get('readingTypeDataSelector.value.endDeviceGroup'));
        view.down('#export-period-combo').setValue(formModel.get('readingTypeDataSelector.value.exportPeriod'));

        view.down('#recurrence-trigger').setValue({recurrence: formModel.get('recurrence')});



        Ext.suspendLayouts();
        Ext.Array.each(view.down('grouped-property-form').query('[isFormField=true]'), function (formItem) {
            if (formItem.name in obj) {
                formItem.setValue(obj[formItem.name]);
            }
        });
        //view.down('#recurrence-values numberfield').setValue({recurrence: formModel.get('recurrence-number')});
        //view.down('#recurrence-values combobox').setValue({recurrence: formModel.get('recurrence-type')});
        Ext.resumeLayouts(true);
    },

    checkRoute: function (token) {
        var me = this,
            relativeRegexp = /administration\/relativeperiods\/add/,
            destinationRegexp = /administration\/dataexporttasks\/(.*)\/destination/,
            readingRegexp = /administration\/dataexporttasks\/(.*)\/readingtypes/;

        Ext.util.History.un('change', this.checkRoute, this);

        if (token.search(relativeRegexp) == -1 && token.search(readingRegexp) == -1 && token.search(destinationRegexp) == -1) {
            me.getStore('Dxp.store.Clipboard').clear('addDataExportTaskValues');
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
            me.fillGrid(0, scheduleRecords);
        }
    },

    fillGrid: function (i, scheduleRecords) {
        var me = this,
            page = me.getAddPage(),
            startOnDate = page.down('#start-on').getValue(),
            everyAmount = page.down('#recurrence-number').getValue(),
            everyTimeKey = page.down('#recurrence-type').getValue(),
            grid = page.down('add-schedule-grid'),
            gridPreview = page.down('#schedule-preview'),
            exportPeriodId = page.down('#export-period-combo').getValue(),
            sendingData = {};

        sendingData.zoneOffset = startOnDate.getTimezoneOffset();
        sendingData.date = moment(startOnDate).add(everyAmount * i, everyTimeKey).valueOf();

        Ext.Ajax.request({
            url: '/api/tmr/relativeperiods/' + exportPeriodId + '/preview',
            method: 'PUT',
            jsonData: sendingData,
            success: function (response) {
                var obj = Ext.decode(response.responseText, true),
                    scheduleRecord = Ext.create('Dxp.model.SchedulePeriod'),
                    startDateLong = obj.start.date,
                    zoneOffset = obj.start.zoneOffset || obj.end.zoneOffset,
                    endDateLong = obj.end.date,
                    startZonedDate,
                    endZonedDate;
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
                scheduleRecord.set('schedule', moment(startOnDate).add(everyAmount * i, everyTimeKey).valueOf());
                scheduleRecord.set('start', startZonedDate);
                scheduleRecord.set('end', endZonedDate);
                scheduleRecords.push(scheduleRecord);
                if (i < 4) {
                    i++;
                    me.fillGrid(i, scheduleRecords);
                } else {
                    grid.getStore().loadData(scheduleRecords, false);
                    gridPreview.show();
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
    }
});
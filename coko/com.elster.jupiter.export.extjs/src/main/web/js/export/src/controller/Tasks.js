Ext.define('Dxp.controller.Tasks', {
    extend: 'Ext.app.Controller',
    views: [
        'Dxp.view.tasks.Add',
        'Dxp.view.tasks.Setup',
        'Dxp.view.tasks.Details',
        'Dxp.view.tasks.History',
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
        'Dxp.store.Intervals'

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
        'Dxp.model.Interval'

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
            ref: 'sideFilterForm',
            selector: '#side-filter #filter-form'
        },
        {
            ref: 'filterTopPanel',
            selector: '#tasks-history-filter-top-panel'
        },
        {
            ref: 'addReadingTypesSetup',
            selector: '#AddReadingTypesToTaskSetup'
        },
        {
            ref: 'actionMenu',
            selector: 'tasks-action-menu'
        }
    ],
    fromDetails: false,
    fromEdit: false,
    taskModel: null,
    taskId: null,
    readingTypeIndex: 2,

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
            'data-export-tasks-add #add-button': {
                click: this.addTask
            },
            'data-export-tasks-add #file-formatter-combo': {
                change: this.updateProperties
            },
            'data-export-tasks-add #addReadingTypeButton': {
                click: this.showAddReadingGrid
            },
            'data-export-tasks-add #add-task-add-export-period': {
                click: this.redirectToRelativePeriodsPage
            },
            '#AddReadingTypesToTaskSetup button[name=cancel]': {
                click: this.forwardToPreviousPage
            },
            '#AddReadingTypesToTaskSetup button[name=add]': {
                click: this.addSelectedReadingTypes
            },
            'data-export-tasks-setup tasks-grid': {
                select: this.showPreview
            },
            'data-export-tasks-history tasks-history-grid': {
                select: this.showHistoryPreview
            },
            'tasks-action-menu': {
                click: this.chooseAction
            },
            '#AddReadingTypesToTaskSetup rt-side-filter button[action=applyfilter]': {
                click: this.loadReadingTypes
            },
            '#AddReadingTypesToTaskSetup #filterReadingTypes': {
                removeFilter: this.removeFilter,
                clearAllFilters: this.clearAllFilters
            },
            '#AddReadingTypesToTaskSetup rt-side-filter button[action=clearfilter]': {
                click: this.clearAllCombos
            }
        });
    },

    showDataExportTasks: function () {
        var me = this,
            view = Ext.widget('data-export-tasks-setup', {
                router: me.getController('Uni.controller.history.Router')
            });

        me.fromDetails = false;
        me.getApplication().fireEvent('changecontentevent', view);
        if (Uni.Auth.hasPrivilege('privilege.run.dataExportTask')) {
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
            actionsMenu = view.down('tasks-action-menu');

        me.fromDetails = true;
        me.getApplication().fireEvent('changecontentevent', view);
        taskModel.load(currentTaskId, {
            success: function (record) {
                var detailsForm = view.down('tasks-preview-form'),
                    propertyForm = detailsForm.down('property-form');

                actionsMenu.record = record;
                actionsMenu.down('#view-details').hide();
                view.down('#tasks-view-menu').setTitle(record.get('name'));
                me.getApplication().fireEvent('dataexporttaskload', record);
                detailsForm.loadRecord(record);
                if (record.get('status') !== 'Busy') {
                    if (record.get('status') === 'Failed') {
                        view.down('#reason-field').show();
                    }
                    if (Uni.Auth.hasPrivilege('privilege.run.dataExportTask')) {
                        view.down('#run').show();
                    }
                }
                if (record.properties() && record.properties().count()) {
                    propertyForm.loadRecord(record);
                }
            }
        });
    },

    initFilter: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            filter = router.filter,
            date;

        me.getSideFilterForm().loadRecord(filter);
        for (var f in filter.getData()) {
            var name = '', exportPeriod;
            switch (f) {
                case 'startedOnFrom':
                    name = Uni.I18n.translate('general.startedFrom', 'DES', 'Started from');
                    break;
                case 'startedOnTo':
                    name = Uni.I18n.translate('general.startedTo', 'DES', 'Started to');
                    break;
                case 'finishedOnFrom':
                    name = Uni.I18n.translate('general.finishedFrom', 'DES', 'Finished from');
                    name = 'Finished from';
                    break;
                case 'finishedOnTo':
                    name = Uni.I18n.translate('general.finishedTo', 'DES', 'Finished to');
                    break;
                case 'exportPeriodContains':
                    name = Uni.I18n.translate('general.exportPeriodContains', 'DES', 'Export period contains');
                    exportPeriod = true;
                    break;
            }
            if (!Ext.isEmpty(filter.get(f))) {
                date = new Date(filter.get(f));
                me.getFilterTopPanel().setFilter(f, name, exportPeriod
                    ? Uni.DateTime.formatDateShort(date)
                    : Uni.DateTime.formatDateShort(date)
                    + ' ' + Uni.I18n.translate('general.at', 'DES', 'At').toLowerCase() + ' '
                    + Uni.DateTime.formatTimeShort(date));
            }
        }
        me.getFilterTopPanel().setVisible(true);
    },

    clearAllFilters: function () {
        this.clearAllCombos();
        this.loadReadingTypes();
    },

    removeFilter: function (key) {
        var widget = this.getAddReadingTypesSetup(),
            field;

        if (key === 'name') {
            field = widget.down('textfield[name=' + key + ']');
        } else {
            field = widget.down('combobox[name=' + key + ']');
        }

        field.setValue(null);
        this.loadReadingTypes();
    },

    showDataExportTaskHistory: function (currentTaskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            store = me.getStore('Dxp.store.DataExportTasksHistory'),
            taskModel = me.getModel('Dxp.model.DataExportTask'),
            view = Ext.widget('data-export-tasks-history', {
                router: router,
                taskId: currentTaskId
            });

        me.getApplication().fireEvent('changecontentevent', view);
        store.getProxy().setUrl(router.arguments);
        me.initFilter();
        var grid = me.getHistory().down('tasks-history-grid');

        Ext.Array.each(Ext.ComponentQuery.query('tasks-action-menu'), function (item) {
            Ext.each(item.query(), function (menuitem) {
                if (menuitem.action !== 'viewLog') {
                    menuitem.setVisible(false);
                } else {
                    menuitem.setVisible(true);
                }
            });
        });

        taskModel.load(currentTaskId, {
            success: function (record) {
                view.down('#tasks-view-menu').setTitle(record.get('name'));
                store.load(function (records, operation, success) {
                    records.map(function (r) {
                        r.set(Ext.apply({}, r.raw, record.raw));
                        r.propertiesStore = record.propertiesStore;
                    });
                    me.showHistoryPreview(grid.getSelectionModel(), records[0]);
                });
            }
        });
    },

    showHistoryPreview: function (selectionModel, record) {
        var me = this,
            page = me.getHistory(),
            preview = page.down('tasks-preview'),
            previewForm = page.down('tasks-preview-form'),
            propertyForm = previewForm.down('property-form');

        if (record) {
            preview.setTitle(record.get('startedOn_formatted'));
            previewForm.down('displayfield[name=lastRun]').setVisible(false);
            previewForm.down('displayfield[name=nextRun_formatted]').setVisible(false);
            previewForm.down('displayfield[name=startedOn]').setVisible(false);
            previewForm.down('displayfield[name=finishedOn]').setVisible(false);
            previewForm.down('displayfield[name=startedOn_formatted]').setVisible(true);
            previewForm.down('displayfield[name=finishedOn_formatted]').setVisible(true);
            previewForm.loadRecord(record);
            preview.down('tasks-action-menu').record = record;
            if (record.get('status') === 'Failed') {
                previewForm.down('#reason-field').show();
            } else {
                previewForm.down('#reason-field').hide();
            }
            if (record.properties() && record.properties().count()) {
                propertyForm.loadRecord(record);
            }
        }
    },

    showAddExportTask: function () {
        var me = this,
            view = Ext.create('Dxp.view.tasks.Add'),
            fileFormatterCombo = view.down('#file-formatter-combo'),
            deviceGroupCombo = view.down('#device-group-combo'),
            exportPeriodCombo = view.down('#export-period-combo'),
            recurrenceTypeCombo = view.down('#recurrence-type'),
            readingTypesStore = view.down('#readingTypesGridPanel').getStore();

        Ext.util.History.on('change', this.checkRoute, this);
        me.taskModel = null;
        me.taskId = null;
        me.fromEdit = false;

        readingTypesStore.removeAll();
        exportPeriodCombo.store.load(function () {
            deviceGroupCombo.store.load(function () {
                if (this.getCount() === 0) {
                    deviceGroupCombo.allowBlank = true;
                    deviceGroupCombo.hide();
                    view.down('#no-device').show();
                }
                fileFormatterCombo.store.load(function () {
                    if (localStorage.getItem('addDataExportTaskValues')) {
                        me.setFormValues(view);
                    } else {
                        fileFormatterCombo.setValue(this.getAt(0));
                        recurrenceTypeCombo.setValue(recurrenceTypeCombo.store.getAt(2));
                    }
                    me.getApplication().fireEvent('changecontentevent', view);
                });
            });
        });
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
        var taskModel = me.getModel('Dxp.model.DataExportTask'),
            taskForm = view.down('#add-data-export-task-form'),
            fileFormatterCombo = view.down('#file-formatter-combo'),
            deviceGroupCombo = view.down('#device-group-combo'),
            exportPeriodCombo = view.down('#export-period-combo'),
            recurrenceTypeCombo = view.down('#recurrence-type');
        if (Uni.Auth.hasNoPrivilege('privilege.update.dataExportTask')) {
            deviceGroupCombo.disabled = true;
            exportPeriodCombo.disabled = true;
        }
        me.fromEdit = true;
        me.taskId = taskId;
        Ext.util.History.on('change', this.checkRoute, this);
        exportPeriodCombo.store.load(function () {
            deviceGroupCombo.store.load(function () {
                if (this.getCount() === 0) {
                    deviceGroupCombo.allowBlank = true;
                    deviceGroupCombo.hide();
                    view.down('#no-device').show();
                }
                fileFormatterCombo.store.load(function () {
                    taskModel.load(taskId, {
                        success: function (record) {
                            var schedule = record.get('schedule');
                            me.taskModel = record;
                            me.getApplication().fireEvent('dataexporttaskload', record);
                            taskForm.setTitle(Uni.I18n.translate('general.edit', 'DES', 'Edit') + " '" + record.get('name') + "'");
                            if (localStorage.getItem('addDataExportTaskValues')) {
                                me.setFormValues(view);
                            } else {
                                taskForm.loadRecord(record);
                                var taskReadingTypes = record.get('readingTypes'),
                                    readingTypesGrid = view.down('#readingTypesGridPanel');

                                readingTypesGrid.getStore().removeAll();

                                Ext.each(taskReadingTypes, function (readingType) {
                                    readingTypesGrid.getStore().add({readingType: readingType})
                                });

                                exportPeriodCombo.setValue(exportPeriodCombo.store.getById(record.data.exportperiod.id));
                                deviceGroupCombo.setValue(deviceGroupCombo.store.getById(record.data.deviceGroup.id));
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
                                    taskForm.down('tasks-property-form').loadRecord(record);
                                }
                            }
                            view.setLoading(false);
                        }
                    });
                    me.getApplication().fireEvent('changecontentevent', view);
                    view.setLoading();
                });
            });
        });
    },

    showDataSources: function (currentTaskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            taskModel = me.getModel('Dxp.model.DataExportTask'),
            store = me.getStore('Dxp.store.DataSources'),
            sideMenu,
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
                sideMenu = view.down('#tasks-view-menu');
                sideMenu.setTitle(record.get('name'));
            }
        });
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('tasks-preview'),
            previewForm = page.down('tasks-preview-form'),
            propertyForm = previewForm.down('property-form');

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
            if (Uni.Auth.hasPrivilege('privilege.run.dataExportTask')) {
                Ext.Array.each(Ext.ComponentQuery.query('#run'), function (item) {
                    item.show();
                });
            }
        }
        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        preview.down('tasks-action-menu').record = record;
        if (record.properties() && record.properties().count()) {
            propertyForm.loadRecord(record);
        }
        Ext.resumeLayouts();
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
                            view.down('tasks-action-menu').record = rec;
                            view.down('tasks-preview-form').loadRecord(rec);
                            if (record.get('status') === 'Busy') {
                                view.down('#run').hide();
                            }
                            if (rec.properties() && rec.properties().count()) {
                                view.down('property-form').loadRecord(rec);
                            }
                        }
                    });
                }
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('dataExportTasks.run', 'DXP', 'Data export task run'));
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
            msg: Uni.I18n.translate('general.remove.msg', 'DXP', 'This data export task will no longer be available.'),
            title: Uni.I18n.translate('general.remove', 'DXP', 'Remove') + '&nbsp' + record.data.name + '?',
            config: {
            },
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
                    var grid = me.getPage().down('tasks-grid');
                    grid.down('pagingtoolbartop').totalCount = 0;
                    grid.down('pagingtoolbarbottom').resetPaging();
                    grid.getStore().load();
                } else {
                    me.getController('Uni.controller.history.Router').getRoute('administration/dataexporttasks').forward();
                }
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.remove.confirm.msg', 'DXP', 'Data export task removed'));
            },
            failure: function (object, operation) {
                var json = Ext.decode(operation.response.responseText, true);
                var errorText = Uni.I18n.translate('communicationtasks.error.unknown', 'MDC', 'Unknown error occurred');
                if (json && json.errors) {
                    errorText = json.errors[0].msg;
                }
                //me.getApplication().getController('Uni.controller.Error').showError(Uni.I18n.translate('general.remove.error.msg', 'DXP', 'Remove operation failed'), errorText);
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
                            title: Uni.I18n.translate('general.remove.error.msg', 'DXP', 'Remove operation failed'),
                            msg: errorText,
                            icon: Ext.MessageBox.ERROR
                        })
                }
            }
        });
    },

    updateProperties: function (field, newValue) {
        var me = this,
            page = me.getAddPage(),
            record = Ext.getStore('Dxp.store.FileFormatters').getById(newValue),
            propertyForm = page.down('tasks-property-form');

        if (record && record.properties() && record.properties().count()) {
            propertyForm.addEditPage = true;
            propertyForm.loadRecord(record);
            propertyForm.show();
        } else {
            propertyForm.hide();
        }
    },

    addTask: function (button) {
        var me = this,
            page = me.getAddPage(),
            form = page.down('#add-data-export-task-form'),
            formErrorsPanel = form.down('#form-errors'),
            propertyForm = form.down('tasks-property-form'),
            lastDayOfMonth = false,
            startOnDate,
            timeUnitValue,
            dayOfMonth,
            hours,
            minutes;

        propertyForm.updateRecord();
        if (form.isValid()) {
            var record = me.taskModel || Ext.create('Dxp.model.DataExportTask'),
                readingTypesStore = page.down('#readingTypesGridPanel').getStore(),
                arrReadingTypes = [];

            record.beginEdit();
            if (!formErrorsPanel.isHidden()) {
                formErrorsPanel.hide();
            }
            if (button.action === 'editTask') {
                record.readingTypes().removeAll();
            }

            readingTypesStore.each(function (record) {
                arrReadingTypes.push(record.getData().readingType);
            });

            record.readingTypes().add(arrReadingTypes);

            if (propertyForm.getRecord()) {
                record.propertiesStore = propertyForm.getRecord().properties();
            }
            record.set('name', form.down('#task-name').getValue());
            record.set('deviceGroup', {
                id: form.down('#device-group-combo').getValue(),
                name: form.down('#device-group-combo').getRawValue()
            });
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
            record.set('exportperiod', {
                id: form.down('#export-period-combo').getValue(),
                name: form.down('#export-period-combo').getRawValue()
            });
            record.set('dataProcessor', {
                name: form.down('#file-formatter-combo').getValue()
            });
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

    showAddReadingGrid: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            page = this.getAddPage(),
            readingTypesStore = page.down('#readingTypesGridPanel').getStore(),
            addReadingTypesRoute = router.currentRoute + '/readingtypes';

        me.readingTypesArray = [];

        readingTypesStore.each(function (record) {
            me.readingTypesArray.push(record.getData());
        });

        me.saveFormValues();
        router.getRoute(addReadingTypesRoute).forward();
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
        } else {
            var me = this;
            var widget = Ext.widget('AddReadingTypesToTaskSetup');

            var unitsOfMeasureStore = Ext.create('Dxp.store.UnitsOfMeasure');
            var timeOfUseStore = Ext.create('Dxp.store.TimeOfUse');
            var intervalsStore = Ext.create('Dxp.store.Intervals');

            me.getApplication().fireEvent('changecontentevent', widget);
            widget.down('#unitsOfMeasureCombo').bindStore(unitsOfMeasureStore);
            widget.down('#intervalsCombo').bindStore(intervalsStore);
            widget.down('#timeOfUseCombo').bindStore(timeOfUseStore);

            me.loadReadingTypes();
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


    loadReadingTypes: function () {
        var me = this,
            widget = this.getAddReadingTypesSetup(),
            readingTypeStore = Ext.create('Dxp.store.LoadedReadingTypes'),

            unitOfMeasureCombo = widget.down('#unitsOfMeasureCombo'),
            intervalsCombo = widget.down('#intervalsCombo'),
            timeOfUseCombo = widget.down('#timeOfUseCombo'),
            readingTypeNameText = widget.down('#readingTypeNameTextField'),
            adaptedReadingTypeStore = Ext.create('Dxp.store.AdaptedReadingsForBulk'),
            unitOfMeasureComboValue = unitOfMeasureCombo.getValue(),
            filter = widget.down('#filterReadingTypes'),
            filterBtns = Ext.ComponentQuery.query('#filterReadingTypes tag-button'),
            bulkGridContainer = widget.down('#AddReadingTypesToTaskBulk'),
            properties = [],
            unitOfMeasureRecord,
            intervalsRecord,
            previewContainer;

        widget.setLoading(true);

        previewContainer = {
            xtype: 'preview-container',
            grid: {
                itemId: 'addReadingTypesGrid',
                xtype: 'AddReadingTypesToTaskBulk',
                store: adaptedReadingTypeStore
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                margin: '0 0 20 0',
                title: Uni.I18n.translate('validation.readingType.empty.title', 'CFG', 'No reading types found.'),
                reasons: [
                    Uni.I18n.translate('validation.readingType.empty.list.item1', 'CFG', 'No reading types have been added yet.'),
                    Uni.I18n.translate('validation.readingType.empty.list.item2', 'CFG', 'No reading types comply to the filter.'),
                    Uni.I18n.translate('validation.readingType.empty.list.item3', 'CFG', 'All reading types have been already added to rule.')
                ]
            }
        };

        bulkGridContainer.removeAll();


        Ext.each(filterBtns, function (btn) {
            btn.destroy();
        });

        if (unitOfMeasureComboValue) {
            unitOfMeasureRecord = unitOfMeasureCombo.findRecord(unitOfMeasureCombo.valueField, unitOfMeasureComboValue);
            properties.push({
                property: 'unitOfMeasure',
                value: unitOfMeasureRecord.get('unit')
            });
            properties.push({
                property: 'multiplier',
                value: unitOfMeasureRecord.get('multiplier')
            });
            filter.setFilter('unitOfMeasure', 'Unit of measure', unitOfMeasureRecord.get('name'), false);
        }
        if (!Ext.isEmpty(intervalsCombo.getValue())) {
            intervalsRecord = intervalsCombo.findRecord(intervalsCombo.valueField, intervalsCombo.getValue());
            properties.push({
                property: 'time',
                value: intervalsCombo.getValue()
            });
            filter.setFilter('time', 'Interval', intervalsRecord.get('name'), false);
        }
        if (readingTypeNameText.getValue()) {
            properties.push({
                property: 'name',
                value: readingTypeNameText.getValue()
            });
            filter.setFilter('name', 'Name', readingTypeNameText.getValue(), false);
        }
        if (!Ext.isEmpty(timeOfUseCombo.getValue())) {
            properties.push({
                property: 'tou',
                value: timeOfUseCombo.getValue()
            });
            filter.setFilter('tou', 'Time of use', timeOfUseCombo.getValue(), false);
        }

        readingTypeStore.getProxy().setExtraParam('filter', Ext.encode(properties));



        bulkGridContainer.add(previewContainer);
        adaptedReadingTypeStore = bulkGridContainer.down('#addReadingTypesGrid').getStore();
        readingTypeStore.load({
            callback: function () {
                this.each(function (record) {
                    if (!me.checkMridAlreadyAdded(me.readingTypesArray, record)) {
                        adaptedReadingTypeStore.add({readingType: record.getData()});
                    }
                });
                adaptedReadingTypeStore.fireEvent('load');
                bulkGridContainer.down('#addReadingTypesGrid').fireEvent('selectionchange');

                widget.setLoading(false);
                if (adaptedReadingTypeStore.getCount() < 1) {
                    widget.down('#buttonsContainer button[name=add]').setDisabled(true);
                }

            }
        });
    },

    checkMridAlreadyAdded: function (array, record) {
        var isExist = false;
        Ext.each(array, function (addedRecord) {
            if (record.get('mRID') === addedRecord.readingType.mRID) {
                isExist = true;
            }
        });
        return isExist;
    },

    redirectToRelativePeriodsPage: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            additionalParams = {};

        me.saveFormValues();
        additionalParams.fromEdit = me.fromEdit;
        additionalParams.taskId = me.taskId;
        router.getRoute('administration/relativeperiods/add').forward(null, additionalParams);
    },

    saveFormValues: function () {
        var me = this,
            page = me.getAddPage(),
            form = page.down('#add-data-export-task-form'),
            formValues = form.getValues(),
            readingTypesStore = page.down('#readingTypesGridPanel').getStore(),
            arrReadingTypes = [];

        readingTypesStore.each(function (record) {
            arrReadingTypes.push(record.getData());
        });

        formValues.readingTypes = arrReadingTypes;
        localStorage.setItem('addDataExportTaskValues', JSON.stringify(formValues));

    },

    setFormValues: function (view) {
        var me = this,
            obj = JSON.parse(localStorage.getItem('addDataExportTaskValues')),
            page = me.getAddPage(),
            readingTypesArray = obj.readingTypes,
            readingTypesGrid = page.down('#readingTypesGridPanel'),
            gridStore = readingTypesGrid.getStore();

        gridStore.removeAll();

        if (me.readingTypesArray) {
            Ext.each(me.readingTypesArray, function (record) {
                gridStore.add(record);
            });
            me.readingTypesArray = null;
        } else {
            if (!Ext.isEmpty(readingTypesArray)) {
                Ext.each(readingTypesArray, function (readingType) {
                    gridStore.add(readingType);
                });
            }
        }

        var formModel = Ext.create('Dxp.model.AddDataExportTaskForm', obj);
        view.down('#add-data-export-task-form').loadRecord(formModel);
        view.down('#recurrence-trigger').setValue({recurrence: formModel.get('recurrence')});
    },

    checkRoute: function (token) {
        var relativeRegexp = /administration\/relativeperiods\/add/,
            readingRegexp = /administration\/dataexporttasks\/(.*)\/readingtypes/;

        Ext.util.History.un('change', this.checkRoute, this);

        if (token.search(relativeRegexp) == -1 && token.search(readingRegexp) == -1) {
            localStorage.removeItem('addDataExportTaskValues');
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

    clearAllCombos: function () {
        var widget = this.getAddReadingTypesSetup(),
            unitOfMeasureCombo = widget.down('#unitsOfMeasureCombo'),
            intervalsCombo = widget.down('#intervalsCombo'),
            timeOfUseCombo = widget.down('#timeOfUseCombo'),
            readingTypeNameText = widget.down('#readingTypeNameTextField');

        unitOfMeasureCombo.setValue(null);
        intervalsCombo.setValue(null);
        timeOfUseCombo.setValue(null);
        readingTypeNameText.setValue(null);
    },

    clearAllFilters: function () {
        this.clearAllCombos();
        this.loadReadingTypes();
    }
});
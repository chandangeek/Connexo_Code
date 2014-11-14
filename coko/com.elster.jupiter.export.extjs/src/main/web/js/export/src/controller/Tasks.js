Ext.define('Dxp.controller.Tasks', {
    extend: 'Ext.app.Controller',
    views: [
        'Dxp.view.tasks.Add',
        'Dxp.view.tasks.Setup',
        'Dxp.view.tasks.Details',
        'Dxp.view.tasks.History'
    ],
    stores: [
        'Dxp.store.DeviceGroups',
        'Dxp.store.DaysWeeksMonths',
        'Dxp.store.ExportPeriods',
        'Dxp.store.FileFormatters',
        'Dxp.store.ReadingTypes',
        'Dxp.store.DataExportTasks',
        'Dxp.store.DataExportTasksHistory'
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
        'Dxp.model.AddDataExportTaskForm'
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
            'data-export-tasks-add button[action=addReadingTypeAction]': {
                click: this.addReadingType
            },
            'data-export-tasks-add #file-formatter-combo': {
                change: this.updateProperties
            },
            'data-export-tasks-add #add-task-add-export-period': {
                click: this.saveFormValues
            },
            'data-export-tasks-setup tasks-grid': {
                select: this.showPreview
            },
            'data-export-tasks-history tasks-history-grid': {
                select: this.showHistoryPreview
            },
            'tasks-action-menu': {
                click: this.chooseAction
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
    },

    showTaskDetailsView: function (taskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            taskModel = me.getModel('Dxp.model.DataExportTask'),
            view = Ext.widget('data-export-tasks-details', {
                router: router
            }),
            actionsMenu = view.down('tasks-action-menu');
        me.fromDetails = true;
        me.getApplication().fireEvent('changecontentevent', view);
        taskModel.load(taskId, {
            success: function (record) {
                var detailsForm = view.down('tasks-preview-form'),
                    propertyForm = detailsForm.down('property-form');

                actionsMenu.record = record;
                actionsMenu.down('#view-details').hide();
                me.getApplication().fireEvent('dataexporttaskload', record);
                detailsForm.loadRecord(record);
                if (record.properties() && record.properties().count()) {
                    propertyForm.loadRecord(record);
                }
            }
        });
    },

    showDataExportTaskHistory: function(taskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            store = me.getStore('Dxp.store.DataExportTasksHistory'),
            taskModel = me.getModel('Dxp.model.DataExportTask'),
            view = Ext.widget('data-export-tasks-history', {
                router: router
            });

        me.getApplication().fireEvent('changecontentevent', view);
        store.getProxy().setUrl(router.arguments);
        var grid = me.getHistory().down('tasks-history-grid');

        taskModel.load(taskId, {
            success: function (record) {
                store.load(function(records, operation, success) {
                    records.map(function(r){
                        r.set(Ext.apply({}, r.raw, record.raw));
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

        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        preview.down('tasks-action-menu').record = record;
        if (record.properties() && record.properties().count()) {
            propertyForm.loadRecord(record);
        }
    },

    showAddExportTask: function () {
        var me = this,
            view = Ext.create('Dxp.view.tasks.Add'),
            fileFormatterCombo = view.down('#file-formatter-combo'),
            deviceGroupCombo = view.down('#device-group-combo'),
            exportPeriodCombo = view.down('#export-period-combo'),
            recurrenceTypeCombo = view.down('#recurrence-type');

        Ext.util.History.on('change', this.checkRoute, this);
        me.taskModel = null;
        me.taskId = null;
        me.fromEdit = false;
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
                            me.taskModel = record;
                            me.getApplication().fireEvent('dataexporttaskload', record);
                            taskForm.setTitle(Uni.I18n.translate('general.edit', 'DES', 'Edit') + ' ' + record.get('name'));
                            if (localStorage.getItem('addDataExportTaskValues')) {
                                me.setFormValues(view);
                            } else {
                                taskForm.loadRecord(record);
                                view.down('#readingType1').setValue(record.get('readingTypes')[0].mRID);
                                var taskReadingTypes = record.get('readingTypes');
                                for (var i = 1; i < taskReadingTypes.length; i++) {
                                    var readingType = taskReadingTypes[i],
                                        field = me.addReadingType();
                                    field.down('textfield').setValue(readingType.mRID);
                                }
                                exportPeriodCombo.setValue(exportPeriodCombo.store.getById(record.data.exportperiod.id));
                                deviceGroupCombo.setValue(deviceGroupCombo.store.getById(record.data.deviceGroup.id));
                                if (record.data.nextRun && (record.data.nextRun !== 0)) {
                                    view.down('#recurrence-trigger').setValue({recurrence: true});
                                    view.down('#recurrence-number').setValue(record.data.schedule.every.count);
                                    recurrenceTypeCombo.setValue(record.data.schedule.timeUnit);
                                    view.down('#start-on').setValue(record.data.nextRun);
                                } else {
                                    recurrenceTypeCombo.setValue(recurrenceTypeCombo.store.getAt(2));
                                }
                                if (record.properties() && record.properties().count()) {
                                    taskForm.down('tasks-property-form').loadRecord(record);
                                }
                            }
                        }
                    });
                    me.getApplication().fireEvent('changecontentevent', view);
                });
            });
        });
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('tasks-preview'),
            previewForm = page.down('tasks-preview-form'),
            propertyForm = previewForm.down('property-form');

        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        preview.down('tasks-action-menu').record = record;
        if (record.properties() && record.properties().count()) {
            propertyForm.loadRecord(record);
        }
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

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
                route = 'administration/dataexporttasks/dataexporttask/log';
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward({
            taskId: menu.record.getId()
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
                if(!Ext.ComponentQuery.query('#remove-error-messagebox')[0])  {
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
                }}
        });
    },

    updateProperties: function (field, newValue) {
        var me = this,
            page = me.getAddPage(),
            record = Ext.getStore('Dxp.store.FileFormatters').getById(newValue),
            propertyForm = page.down('tasks-property-form');

        if (record && record.properties() && record.properties().count()) {
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
            arrReadingTypes = [],
            formErrorsPanel = form.down('#form-errors'),
            propertyForm = form.down('tasks-property-form');

        propertyForm.updateRecord();
        if (form.isValid()) {
            var record = me.taskModel || Ext.create('Dxp.model.DataExportTask'),
                readingTypes = page.down('#readingValuesTextFieldsContainer').items;
            if (!formErrorsPanel.isHidden()) {
                formErrorsPanel.hide();
            }
            if (button.action === 'editTask') {
                record.readingTypes().removeAll();
            }
            for (var i = 0; i < readingTypes.items.length; i++) {
                var readingTypeMRID = readingTypes.items[i].items.items[0],
                    readingType = readingTypeMRID.value,
                    readingTypeRecord = Ext.create('Dxp.model.ReadingType');
                readingTypeMRID.name = 'readingTypes[' + i + '].readingTypeMRID';
                readingTypeRecord.set('mRID', readingType);
                arrReadingTypes.push(readingTypeRecord);
            }
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
                record.set('schedule', {
                    every: {
                        count: form.down('#recurrence-number').getValue(),
                        timeUnit: form.down('#recurrence-type').getValue()
                    }
                });
                record.set('nextRun', moment(form.down('#start-on').getValue()).valueOf());
            } else {
                record.set('schedule', null);
            }
            record.set('exportperiod', {
                id: form.down('#export-period-combo').getValue(),
                name: form.down('#export-period-combo').getRawValue()
            });
            record.set('dataProcessor', form.down('#file-formatter-combo').getValue());
            record.save({
                success: function () {
                    if (button.action === 'editTask' && me.fromDetails) {
                        me.getController('Uni.controller.history.Router').getRoute('administration/dataexporttasks/dataexporttask').forward({taskId: record.getId()});
                    } else {
                        me.getController('Uni.controller.history.Router').getRoute('administration/dataexporttasks').forward();
                    }
                    if (button.action === 'editTask') {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('editDataExportTask.successMsg', 'DES', 'Data export task edited'));
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

    saveFormValues: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            page = me.getAddPage(),
            form = page.down('#add-data-export-task-form'),
            formValues = form.getValues(),
            readingTypes = page.down('#readingValuesTextFieldsContainer').items,
            arrReadingTypes = [],
            additionalParams = {};

        for (var i = 0; i < readingTypes.items.length; i++) {
            var readingTypeMRID = readingTypes.items[i].items.items[0],
                readingType = readingTypeMRID.value;
            arrReadingTypes.push(readingType);
        }
        formValues.readingTypes = arrReadingTypes;
        localStorage.setItem('addDataExportTaskValues', JSON.stringify(formValues));
        additionalParams.fromEdit = me.fromEdit;
        additionalParams.taskId = me.taskId;
        router.getRoute('administration/relativeperiods/add').forward(null, additionalParams);
    },

    setFormValues: function (view) {
        var me = this,
            obj = JSON.parse(localStorage.getItem('addDataExportTaskValues')),
            readingTypesArray = obj.readingTypes;
        if (!Ext.isEmpty(readingTypesArray)) {
            view.down('#readingType1').setValue(readingTypesArray[0]);
            for (var i = 1; i < readingTypesArray.length; i++) {
                var readingType = readingTypesArray[i],
                    field = me.addReadingType();
                field.down('textfield').setValue(readingType);
            }
        }
        var formModel = Ext.create('Dxp.model.AddDataExportTaskForm', obj);
        view.down('#add-data-export-task-form').loadRecord(formModel);
        view.down('#recurrence-trigger').setValue({recurrence: formModel.get('recurrence')});
    },

    checkRoute: function (token) {
        var relativeRegexp = /administration\/relativeperiods\/add/;

        Ext.util.History.un('change', this.checkRoute, this);

        if (token.search(relativeRegexp) == -1) {
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

    addReadingType: function () {
        var me = this,
            indexToRemove = me.readingTypeIndex,
            page = me.getAddPage(),
            readingTypesContainer = page.down('#readingValuesTextFieldsContainer'),
            widget = readingTypesContainer.add(
                {
                    xtype: 'container',
                    itemId: 'readingType' + me.readingTypeIndex,
                    name: 'readingType' + me.readingTypeIndex,
                    layout: {
                        type: 'hbox'
                    },
                    items: [
                        {
                            xtype: 'textfield',
                            fieldLabel: '&nbsp',
                            labelAlign: 'right',
                            name: 'readingType',
                            msgTarget: 'under',
                            labelWidth: 250,
                            maskRe: /^($|\S.*$)/,
                            maxLength: 80,
                            validateOnChange: false,
                            validateOnBlur: false,
                            allowBlank: false,
                            enforceMaxLength: true,
                            width: 500
                        },
                        {
                            text: '-',
                            xtype: 'button',
                            action: 'removeReadingTypeAction',
                            pack: 'center',
                            margin: '0 0 5 5',
                            itemId: 'readingTypeRemoveButton' + me.readingTypeIndex,
                            handler: function () {
                                readingTypesContainer.remove(Ext.ComponentQuery.query('#readingType' + indexToRemove)[0]);
                            }
                        }
                    ]
                }
            );

        me.readingTypeIndex = me.readingTypeIndex + 1;
        return widget;
    }
});
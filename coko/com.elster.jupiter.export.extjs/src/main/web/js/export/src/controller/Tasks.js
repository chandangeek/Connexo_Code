Ext.define('Dxp.controller.Tasks', {
    extend: 'Ext.app.Controller',
    views: [
        'Dxp.view.tasks.Add',
        'Dxp.view.tasks.Setup',
        'Dxp.view.tasks.Details'
    ],
    stores: [
        'Dxp.store.DeviceGroups',
        'Dxp.store.DaysWeeksMonths',
        'Dxp.store.ExportPeriods',
        'Dxp.store.FileFormatters',
        'Dxp.store.ReadingTypes',
        'Dxp.store.DataExportTasks'
    ],
    models: [
        'Dxp.model.DeviceGroup',
        'Dxp.model.DayWeekMonth',
        'Dxp.model.ExportPeriod',
        'Dxp.model.FileFormatter',
        'Dxp.model.SchedulePeriod',
        'Dxp.model.ReadingType',
        'Dxp.model.DataExportTask'
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
        }
    ],

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
            'data-export-tasks-setup tasks-grid': {
                select: this.showPreview
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

        me.getApplication().fireEvent('changecontentevent', view);
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
        }

        route && (route = router.getRoute(route));
        route && route.forward({
            taskId: menu.record.getId()
        });
    },

    showTaskDetailsView: function (taskId) {
        var me = this,
            view = Ext.create('Dxp.view.tasks.Details'),
            taskModel = me.getModel('Dxp.model.DataExportTask');

        me.getApplication().fireEvent('changecontentevent', view);
        taskModel.load(taskId, {
            success: function (record) {
                var detailsForm = view.down('tasks-preview-form'),
                    propertyForm = detailsForm.down('property-form');

                me.getApplication().fireEvent('dataexporttaskload', record);
                detailsForm.loadRecord(record);
                if (record.properties() && record.properties().count()) {
                    propertyForm.loadRecord(record);
                }
            }
        });
    },

    showAddExportTask: function () {
        var me = this,
            view = Ext.create('Dxp.view.tasks.Add'),
            fileFormatterCombo = view.down('#file-formatter-combo'),
            deviceGroupCombo = view.down('#device-group-combo');

        deviceGroupCombo.store.load(function () {
            if (this.getCount() === 0) {
                deviceGroupCombo.allowBlank = true;
                deviceGroupCombo.hide();
                view.down('#no-device').show();
            }
            fileFormatterCombo.store.load(function () {
                fileFormatterCombo.setValue(this.getAt(0));
                me.getApplication().fireEvent('changecontentevent', view);
            });
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

    addTask: function () {
        var me = this,
            page = me.getAddPage(),
            form = page.down('#add-data-export-task-form'),
            arrReadingTypes = [],
            formErrorsPanel = form.down('#form-errors'),
            propertyForm = form.down('tasks-property-form');

        propertyForm.updateRecord();

        if (form.isValid()) {
            var record = Ext.create('Dxp.model.DataExportTask'),
                readingTypes = page.down('#readingValuesTextFieldsContainer').items;

            if (!formErrorsPanel.isHidden()) {
                formErrorsPanel.hide();
            }
            for (var i = 0; i < readingTypes.items.length; i++) {
                var readingTypeMRID = readingTypes.items[i].items.items[0],
                    readingType = readingTypeMRID.value,
                    readingTypeRecord = Ext.create('Dxp.model.ReadingType');
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

            if (form.down('#recurrence-trigger').getValue().recurrence && moment(form.down('#date-time-field-date').getValue()).year()) {
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
                    me.getController('Uni.controller.history.Router').getRoute('administration/dataexporttasks').forward();
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('addDataExportTask.successMsg', 'DES', 'Data export task added'));
                }
            })
        } else {
            formErrorsPanel.show();
        }
    },

    onRecurrenceTriggerChange: function (field, newValue, oldValue) {
        var me = this,
            page = me.getAddPage(),
            recurrenceNumberField = page.down('#recurrence-number'),
            recurrenceTypeCombo = page.down('#recurrence-type'),
            dateValue = page.down('#date-time-field-date').getValue(),
            exportPeriodValue = page.down('#export-period-combo').getValue(),
            gridPreview = page.down('#schedule-preview'),
            scheduleRecords = [];

        if (newValue.recurrence && !recurrenceNumberField.getValue() && !recurrenceTypeCombo.getValue()) {
            recurrenceNumberField.setValue(recurrenceNumberField.minValue);
            recurrenceTypeCombo.setValue(recurrenceTypeCombo.store.getAt(0));
        }
        if (newValue.recurrence && moment(dateValue).year() && exportPeriodValue) {
            me.fillGrid(0, scheduleRecords);
        }
        if (!newValue.recurrence && !gridPreview.isHidden()) {
            gridPreview.hide();
        }
    },

    fillScheduleGridOrNot: function () {
        var me = this,
            page = me.getAddPage(),
            dateValue = page.down('#date-time-field-date').getValue(),
            exportPeriodValue = page.down('#export-period-combo').getValue(),
            recurrenceTriggerValue = page.down('#recurrence-trigger').getValue(),
            scheduleRecords = [];

        if (recurrenceTriggerValue.recurrence && moment(dateValue).year() && exportPeriodValue) {
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
Ext.define('Dxp.controller.Tasks', {
    extend: 'Ext.app.Controller',
    views: [
        'Dxp.view.tasks.Add'
    ],
    stores: [
        'Dxp.store.DeviceGroups',
        'Dxp.store.DaysWeeksMonths',
        'Dxp.store.ExportPeriods',
        'Dxp.store.FileFormatters',
        'Dxp.store.ReadingTypes'
    ],
    models: [
        'Dxp.model.DeviceGroup',
        'Dxp.model.DayWeekMonth',
        'Dxp.model.ExportPeriod',
        'Dxp.model.FileFormatter',
        'Dxp.model.SchedulePeriod',
        'Dxp.model.DataExportTask',
        'Dxp.model.ReadingType'
    ],
    refs: [
        {
            ref: 'addPage',
            selector: 'data-export-tasks-add'
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
            'data-export-tasks-add #task-name': {
                blur: this.setDefaultPrefixName
            },
            'data-export-tasks-add #file-name-prefix': {
                blur: this.showExampleOrNot
            },
            'data-export-tasks-add #extension': {
                blur: this.showExampleOrNot
            },
            'data-export-tasks-add #add-button': {
                click: this.addTask
            },
            'data-export-tasks-add button[action=addReadingTypeAction]': {
                click: this.addReadingType
            }
       /*     'data-export-tasks-add #file-formatter-combo': {
                change: this.updateProperties
            }*/
        });
    },

    showOverview: function () {
        var me = this,
            view = Ext.create('Dxp.view.tasks.Overview');

        me.getApplication().fireEvent('changecontentevent', view);
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
//            fileFormatterCombo.setValue(fileFormatterCombo.store.getAt(0));
            me.getApplication().fireEvent('changecontentevent', view);
        });
    },

    updateProperties: function (field, newValue) {
        var me = this,
            page = me.getAddPage(),
            record = Ext.getStore('Dxp.store.FileFormatters').getById(newValue),
            propertyForm = page.down('property-form');

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
            taskModel;
        if (form.isValid()) {
            taskModel = me.formToModel(form);
           /* taskModel.save({
                success: function (record) {

                }
            })*/
        }
    },

    formToModel: function (form) {
        var me = this,
            page = me.getAddPage(),
            taskModel = Ext.create('Dxp.model.DataExportTask');
        taskModel.set('name', form);
        console.info('fff');
    },

    setDefaultPrefixName: function (nameField) {
        var me = this,
            page = me.getAddPage(),
            prefixField = page.down('#file-name-prefix'),
            nameFieldValue = nameField.getValue(),
            trimValue;

        if (!prefixField.getValue()) {
            trimValue = nameFieldValue.replace(/\s+/g, '');
            prefixField.setValue(trimValue);
        }
    },

    showExampleOrNot: function () {
        var me = this,
            page = me.getAddPage(),
            prefixValue = page.down('#file-name-prefix').getValue(),
            extensionValue = page.down('#extension').getValue(),
            exampleField = page.down('#example');

        if (prefixValue && extensionValue) {
            exampleField.setValue(prefixValue + '.' + extensionValue);
            if (exampleField.isHidden()) {
                exampleField.show();
            }
        } else {
            exampleField.hide();
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
                var obj = Ext.decode(response.responseText),
                    scheduleRecord = Ext.create('Dxp.model.SchedulePeriod');

                scheduleRecord.set('schedule', moment(startOnDate).add(everyAmount * i, everyTimeKey).valueOf());
                scheduleRecord.set('start', obj.start);
                scheduleRecord.set('end', obj.end);
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
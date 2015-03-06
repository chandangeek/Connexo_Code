Ext.define('Cfg.controller.Tasks', {
    extend: 'Ext.app.Controller',
    views: [
        'Cfg.view.validationtask.Add',
        'Cfg.view.validationtask.Setup',
        'Cfg.view.validationtask.Details',
        'Uni.form.field.DateTime'
    ],
    stores: [
        'Cfg.store.DeviceGroups',
        'Cfg.store.DaysWeeksMonths',
        'Cfg.store.DataValidationTasks'
    ],
    models: [
        'Cfg.model.DeviceGroup',
        'Cfg.model.DayWeekMonth',        
        'Cfg.model.DataValidationTask'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'data-validation-tasks-setup'
        },
        {
            ref: 'addPage',
            selector: 'data-validation-tasks-add'
        },
        {
            ref: 'detailsPage',
            selector: 'data-validation-tasks-details'
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
            'data-validation-tasks-add #recurrence-trigger': {
                change: this.onRecurrenceTriggerChange
            },/*
            'data-validation-tasks-add #start-on': {
                change: this.fillScheduleGridOrNot
            },
            'data-validation-tasks-add #recurrence-number': {
                change: this.fillScheduleGridOrNot
            },
            'data-validation-tasks-add #recurrence-type': {
                change: this.fillScheduleGridOrNot
            },
            'data-validation-tasks-add #export-period-combo': {
                change: this.fillScheduleGridOrNot
            },*/
            'data-validation-tasks-add #add-button': {
                click: this.addTask
            },
            /*'data-validation-tasks-add #file-formatter-combo': {
                change: this.updateProperties
            },
            'data-validation-tasks-add #addReadingTypeButton': {
                click: this.showAddReadingGrid
            },
            'data-validation-tasks-add #add-task-add-validation-period': {
                click: this.redirectToRelativePeriodsPage
            },
            '#AddReadingTypesToTaskSetup button[name=cancel]': {
                click: this.forwardToPreviousPage
            },
            '#AddReadingTypesToTaskSetup button[name=add]': {
                click: this.addSelectedReadingTypes
            },*/
            'data-validation-tasks-setup tasks-grid': {
                select: this.showPreview
            }/*,
            'data-validation-tasks-history tasks-history-grid': {
                select: this.showHistoryPreview
            }*/,
            'tasks-action-menu': {
                click: this.chooseAction
            },
            'tasks-history-action-menu': {
                click: this.chooseAction
            }/*,
            '#AddReadingTypesToTaskSetup rt-side-filter button[action=applyfilter]': {
                click: this.loadReadingTypes
            },
            '#AddReadingTypesToTaskSetup #filterReadingTypes': {
                removeFilter: this.removeFilter,
                clearAllFilters: this.clearAllFilters
            },
            '#AddReadingTypesToTaskSetup rt-side-filter button[action=clearfilter]': {
                click: this.clearAllCombos
            },
            'history-filter-form  button[action=applyfilter]': {
                click: this.applyHistoryFilter
            },
            'history-filter-form  button[action=clearfilter]': {
                click: this.clearHistoryFilter
            },
            '#tasks-history-filter-top-panel': {
                removeFilter: this.removeHistoryFilter,
                clearAllFilters: this.clearHistoryFilter
            }*/
        });
    },

    showDataValidationTasks: function () {
        var me = this,
            view = Ext.widget('data-validation-tasks-setup', {
                router: me.getController('Uni.controller.history.Router')
            });

        me.fromDetails = false;
        me.getApplication().fireEvent('changecontentevent', view);        
    },

    showTaskDetailsView: function (currentTaskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            taskModel = me.getModel('Cfg.model.DataValidationTask'),
            view = Ext.widget('data-validation-tasks-details', {
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
                view.down('#tasks-view-menu #tasks-view-link').setText(record.get('name'));
                me.getApplication().fireEvent('datavalidationtaskload', record);
                detailsForm.loadRecord(record);
                if (record.get('status') !== 'Busy') {
                    if (record.get('status') === 'Failed') {
                        view.down('#reason-field').show();
                    }               
                }              
            }
        });
    },


    showAddValidationTask: function () {
        var me = this,
            view = Ext.create('Cfg.view.validationtask.Add'),            
            deviceGroupCombo = view.down('#device-group-combo'),            
            recurrenceTypeCombo = view.down('#recurrence-type');

        me.getApplication().fireEvent('changecontentevent', view);

        //Ext.util.History.on('change', this.checkRoute, this);
        me.taskModel = null;
        me.taskId = null;
        me.fromEdit = false;		
		 
		deviceGroupCombo.store.load(function () {
			if (this.getCount() === 0) {
				deviceGroupCombo.allowBlank = true;
				deviceGroupCombo.hide();
				view.down('#no-device').show();
			}
		});
		recurrenceTypeCombo.setValue(recurrenceTypeCombo.store.getAt(2));
        
    },

    showEditValidationTask: function (taskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view;
        if (me.fromDetails) {
            view = Ext.create('Cfg.view.validationtask.Add', {
                edit: true,
                returnLink: router.getRoute('administration/datavalidationtasks/datavalidationtask').buildUrl({taskId: taskId})
            })
        } else {
            view = Ext.create('Cfg.view.validationtask.Add', {
                edit: true,
                returnLink: router.getRoute('administration/datavalidationtasks').buildUrl()
            })
        }
        var taskModel = me.getModel('Cfg.model.DataValidationTask'),
            taskForm = view.down('#add-data-validation-task-form'),            
            deviceGroupCombo = view.down('#device-group-combo'),            
            recurrenceTypeCombo = view.down('#recurrence-type');
			
        if (Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration')) {
            deviceGroupCombo.disabled = true;         
        }
        me.fromEdit = true;
        me.taskId = taskId;
        //Ext.util.History.on('change', this.checkRoute, this);
    		
		 taskModel.load(taskId, {
                    success: function (record) {
                        var schedule = record.get('schedule');
                        me.taskModel = record;
                        me.getApplication().fireEvent('datavalidationtaskload', record);
                        taskForm.setTitle(Uni.I18n.translate('dataValidationTasks.general.edit', 'CFG', 'Edit') + " '" + record.get('name') + "'");
                        	taskForm.loadRecord(record);
                        
							deviceGroupCombo.store.load({
                                callback: function () {
                                    if (this.getCount() === 0) {
                                        deviceGroupCombo.allowBlank = true;
                                        deviceGroupCombo.hide();
                                        view.down('#no-device').show();
                                    }
                                    deviceGroupCombo.setValue(deviceGroupCombo.store.getById(record.data.deviceGroup.id));
                                }
                            });      
							if (record.data.nextRun && (record.data.nextRun !== 0)) {
                                view.down('#recurrence-trigger').setValue({recurrence: true});
                                view.down('#recurrence-number').setValue(schedule.count);
                                recurrenceTypeCombo.setValue(schedule.timeUnit);
                                view.down('#start-on').setValue(record.data.nextRun);
                            } else {
                                recurrenceTypeCombo.setValue(recurrenceTypeCombo.store.getAt(2));
                            }
							
                        view.setLoading(false);
                    }
                });
				
				
        me.getApplication().fireEvent('changecontentevent', view);
        view.setLoading();
    },

    showDataSources: function (currentTaskId) {
	debugger;
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            taskModel = me.getModel('Cfg.model.DataValidationTask'),
            store = me.getStore('Cfg.store.DataSources'),
            view;

        store.getProxy().setUrl(router.arguments);
        view = Ext.widget('data-sources-setup', {
            router: router,
            taskId: currentTaskId
        });
        me.getApplication().fireEvent('changecontentevent', view);
        taskModel.load(currentTaskId, {
            success: function (record) {
                me.getApplication().fireEvent('datavalidationtaskload', record);
                view.down('#tasks-view-menu  #tasks-view-link').setText(record.get('name'));
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
        }
        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        preview.down('tasks-action-menu').record = record;
        Ext.resumeLayouts();
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        router.arguments.taskId = menu.record.getId();
        

        switch (item.action) {
            case 'viewDetails':
                route = 'administration/datavalidationtasks/datavalidationtask';
                break;
            case 'editValidationTask':
                route = 'administration/datavalidationtasks/datavalidationtask/edit';
                break;
            case 'removeTask':
                me.removeTask(menu.record);
                break;
         /*   case 'viewLog':
                route = 'administration/datavalidationtasks/datavalidationtask/history/occurrence';
                break;
            case 'viewHistory':
                route = 'administration/datavalidationtasks/datavalidationtask/history';
                break;
            case 'run':
                me.runTask(menu.record);
                break;*/
        }

        route && (route = router.getRoute(route));
        route && route.forward(router.arguments);
    },


    removeTask: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation');
        confirmationWindow.show({
            msg: Uni.I18n.translate('dataValidationTasks.general.remove.msg', 'CFG', 'This data validation task will no longer be available.'),
            title: Uni.I18n.translate('dataValidationTasks.general.remove', 'CFG', 'Remove') + '&nbsp' + record.data.name + '?',
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
                    var grid = me.getPage().down('tasks-grid');
                    grid.down('pagingtoolbartop').totalCount = 0;
                    grid.down('pagingtoolbarbottom').resetPaging();
                    grid.getStore().load();
                } else {
                    me.getController('Uni.controller.history.Router').getRoute('administration/datavalidationtasks').forward();
                }
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('dataValidationTasks.general.remove.confirm.msg', 'CFG', 'Data validation task removed'));
            },
            failure: function (object, operation) {
                var json = Ext.decode(operation.response.responseText, true);
                var errorText = Uni.I18n.translate('communicationtasks.error.unknown', 'MDC', 'Unknown error occurred');
                if (json && json.errors) {
                    errorText = json.errors[0].msg;
                }
                //me.getApplication().getController('Uni.controller.Error').showError(Uni.I18n.translate('dataValidationTasks.general.remove.error.msg', 'CFG', 'Remove operation failed'), errorText);
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
                                href: '#/administration/datavalidationtasks/',
                                handler: function (button, event) {
                                    this.up('messagebox').destroy();
                                }
                            }
                        ]
                    }).show({
                        ui: 'notification-error',
                        title: Uni.I18n.translate('dataValidationTasks.general.remove.error.msg', 'CFG', 'Remove operation failed'),
                        msg: errorText,
                        modal: false,
                        icon: Ext.MessageBox.ERROR
                    })
                }
            }
        });
    },


    addTask: function (button) {
        var me = this,
            page = me.getAddPage(),
            form = page.down('#add-data-validation-task-form'),
            formErrorsPanel = form.down('#form-errors'),           
            lastDayOfMonth = false,
            startOnDate,
            timeUnitValue,
            dayOfMonth,
            hours,
            minutes;
        
        if (form.isValid()) {
            var record = me.taskModel || Ext.create('Cfg.model.DataValidationTask');

            record.beginEdit();
            if (!formErrorsPanel.isHidden()) {
                formErrorsPanel.hide();
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
     
			record.endEdit();
            record.save({
                success: function () {
                    if (button.action === 'editTask' && me.fromDetails) {
                        me.getController('Uni.controller.history.Router').getRoute('administration/datavalidationtasks/datavalidationtask').forward({taskId: record.getId()});
                    } else {
                        me.getController('Uni.controller.history.Router').getRoute('administration/datavalidationtasks').forward();
                    }
                    if (button.action === 'editTask') {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('dataValidationTasks.editDataValidationTask.successMsg.saved', 'CFG', 'Data validation task saved'));
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('dataValidationTasks.addDataValidationTask.successMsg', 'CFG', 'Data validation task added'));
                    }
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        Ext.Array.each(json.errors, function (item) {
                            if (item.id.indexOf("readingTypes") !== -1) {
                                form.down('#readingTypesFieldContainer').setActiveError(item.msg);
                            }
                        });
                        form.getForm().markInvalid(json.errors);
                        formErrorsPanel.show();
                    }
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
            recurrenceTypeCombo = page.down('#recurrence-type');

        if (newValue.recurrence && !recurrenceNumberField.getValue()) {
            recurrenceNumberField.setValue(recurrenceNumberField.minValue);            
        }  
		if (newValue.recurrence && !recurrenceTypeCombo.getValue()) {            
            recurrenceTypeCombo.setValue(recurrenceTypeCombo.store.getAt(0));
        }  		
    },
/*
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
*/
   /* fillScheduleGridOrNot: function () {
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
                    scheduleRecord = Ext.create('Cfg.model.SchedulePeriod'),
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
    }*/
});
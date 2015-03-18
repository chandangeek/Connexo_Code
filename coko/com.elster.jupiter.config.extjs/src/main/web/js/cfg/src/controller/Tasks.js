Ext.define('Cfg.controller.Tasks', {
    extend: 'Ext.app.Controller',
    views: [
        'Cfg.view.validationtask.Add',
        'Cfg.view.validationtask.Setup',
        'Cfg.view.validationtask.Details',
		'Cfg.view.validationtask.History',
        'Cfg.view.validationtask.HistoryPreview',
        'Cfg.view.validationtask.HistoryPreviewForm',
        'Uni.form.field.DateTime'
    ],
    stores: [
        'Cfg.store.DeviceGroups',
        'Cfg.store.DaysWeeksMonths',		
        'Cfg.store.DataValidationTasks',
		'Cfg.store.DataValidationTasksHistory'
    ],
    models: [
        'Cfg.model.DeviceGroup',
        'Cfg.model.DayWeekMonth',        
        'Cfg.model.DataValidationTask',
		'Cfg.model.DataValidationTaskHistory',
		'Cfg.model.HistoryFilter'
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
        },
		{
            ref: 'history',
            selector: 'data-validation-tasks-history'
        },
		   {
            ref: 'filterTopPanel',
            selector: '#tasks-history-filter-top-panel'
        },
		{
            ref: 'sideFilterForm',
            selector: '#side-filter #filter-form'
        }
    ],
    fromDetails: false,
    fromEdit: false,
    taskModel: null,
    taskId: null,    

    init: function () {
        this.control({
            'data-validation-tasks-add #recurrence-trigger': {
                change: this.onRecurrenceTriggerChange
            },
            'data-validation-tasks-add #add-button': {
                click: this.addTask
            },
			'data-validation-tasks-setup tasks-grid': {
                select: this.showPreview
            },
            'tasks-action-menu': {
                click: this.chooseAction
            },
            'tasks-history-action-menu': {
                click: this.chooseAction
            },
            'data-validation-tasks-history tasks-history-grid': {
                select: this.showHistoryPreview
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
            }
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

	showDataValidationTaskHistory: function (currentTaskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            store = me.getStore('Cfg.store.DataValidationTasksHistory'),
            taskModel = me.getModel('Cfg.model.DataValidationTask'),
            view;

        store.getProxy().setUrl(router.arguments);
        view = Ext.widget('data-validation-tasks-history', {
            router: router,
            taskId: currentTaskId
        });
        me.getApplication().fireEvent('changecontentevent', view);
        me.initFilter();

        taskModel.load(currentTaskId, {
            success: function (record) {
                view.down('#tasks-view-menu  #tasks-view-link').setText(record.get('name'));
            }
        });
    },

    showHistoryPreview: function (selectionModel, record) {
        var me = this,
            page = me.getHistory(),
            preview = page.down('tasks-history-preview'),
            previewForm = page.down('tasks-history-preview-form');

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
            Ext.resumeLayouts(true);
        }
    },

    showAddValidationTask: function () {
        var me = this,
            view = Ext.create('Cfg.view.validationtask.Add'),            
            deviceGroupCombo = view.down('#device-group-combo'),            
            recurrenceTypeCombo = view.down('#recurrence-type');

        me.getApplication().fireEvent('changecontentevent', view);

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
							//if (schedule) {
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

		if (me.getHistory()) {
            router.arguments.occurrenceId = menu.record.getId();
        } else {
            router.arguments.taskId = menu.record.getId();
        }
        
        

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
			case 'viewLog':
                route = 'administration/datavalidationtasks/datavalidationtask/history/occurrence';
                break;
            case 'viewHistory':
                route = 'administration/datavalidationtasks/datavalidationtask/history';
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward(router.arguments);
    },

	initFilter: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            filter = router.filter,
            date;

        me.getSideFilterForm().loadRecord(filter);
        for (var f in filter.getData()) {
            var name = '', validationPeriod;
            switch (f) {
                case 'startedOnFrom':
                    name = Uni.I18n.translate('dataValidationTasks.general.startedFrom', 'CFG', 'Started from');
                    break;
                case 'startedOnTo':
                    name = Uni.I18n.translate('dataValidationTasks.general.startedTo', 'CFG', 'Started to');
                    break;
                case 'finishedOnFrom':
                    name = Uni.I18n.translate('dataValidationTasks.general.finishedFrom', 'CFG', 'Finished from');
                    name = 'Finished from';
                    break;
                case 'finishedOnTo':
                    name = Uni.I18n.translate('dataValidationTasks.general.finishedTo', 'CFG', 'Finished to');
                    break;
                case 'validationPeriodContains':
                    name = Uni.I18n.translate('dataValidationTasks.general.validationPeriod', 'CFG', 'Validation period contains');
                    validationPeriod = true;
                    break;
            }
            if (!Ext.isEmpty(filter.get(f))) {
                date = new Date(filter.get(f));
                me.getFilterTopPanel().setFilter(f, name, validationPeriod
                    ? Uni.DateTime.formatDateLong(date)
                    : Uni.DateTime.formatDateLong(date)
                + ' ' + Uni.I18n.translate('dataValidationTasks.general.at', 'CFG', 'At').toLowerCase() + ' '
                + Uni.DateTime.formatTimeShort(date));
            }
        }
        me.getFilterTopPanel().setVisible(true);
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
	
	 applyHistoryFilter: function () {
        this.getSideFilterForm().updateRecord();
        this.getSideFilterForm().getRecord().save();
    },

    clearHistoryFilter: function () {
        this.getSideFilterForm().getForm().reset();
        this.getFilterTopPanel().setVisible(false);
        this.getSideFilterForm().getRecord().getProxy().destroy();
    },

    removeHistoryFilter: function (key) {
        var router = this.getController('Uni.controller.history.Router'),
            record = router.filter;
        if (record) {
            delete record.data[key];
            record.save();
        }
    }
});
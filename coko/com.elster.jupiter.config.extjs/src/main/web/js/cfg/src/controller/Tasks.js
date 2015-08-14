Ext.define('Cfg.controller.Tasks', {
    extend: 'Ext.app.Controller',

    requires: [,
        'Cfg.privileges.Validation'
    ],

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
        'Cfg.store.ValidationTasks',
        'Cfg.store.ValidationTasksHistory'
    ],

    models: [
        'Cfg.model.DeviceGroup',
        'Cfg.model.DayWeekMonth',
        'Cfg.model.ValidationTask',
        'Cfg.model.ValidationTaskHistory',
        'Cfg.model.HistoryFilter'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'validation-tasks-setup'
        },
        {
            ref: 'addPage',
            selector: 'cfg-validation-tasks-add'
        },
        {
            ref: 'detailsPage',
            selector: 'cfg-validation-tasks-details'
        },
        {
            ref: 'actionMenu',
            selector: 'cfg-validation-tasks-action-menu'
        },
        {
            ref: 'history',
            selector: 'cfg-validation-tasks-history'
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
            'cfg-validation-tasks-add #rgr-validation-tasks-recurrence-trigger': {
                change: this.onRecurrenceTriggerChange
            },
            'cfg-validation-tasks-add #add-button': {
                click: this.addTask
            },
            'validation-tasks-setup cfg-validation-tasks-grid': {
                select: this.showPreview
            },
            'cfg-validation-tasks-action-menu': {
                click: this.chooseAction
            },
            'cfg-tasks-history-action-menu': {
                click: this.chooseAction
            },
            'cfg-validation-tasks-history cfg-tasks-history-grid': {
                select: this.showHistoryPreview
            }
        });
    },

    showValidationTasks: function () {
        var me = this,
            view = Ext.widget('validation-tasks-setup', {
                router: me.getController('Uni.controller.history.Router')
            });

        me.fromDetails = false;
        me.getApplication().fireEvent('changecontentevent', view); 
        
		if ( Cfg.privileges.Validation.canRun()) {
            Ext.Array.each(Ext.ComponentQuery.query('#run-task'), function (item) {
                item.show();
            });
        }
    },

    showValidationTaskDetailsView: function (currentTaskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            taskModel = me.getModel('Cfg.model.ValidationTask'),
            view = Ext.widget('cfg-validation-tasks-details', {
                router: router,
                taskId: currentTaskId
            }),
            actionsMenu = view.down('cfg-validation-tasks-action-menu');

        me.fromDetails = true;
        me.getApplication().fireEvent('changecontentevent', view);
        taskModel.load(currentTaskId, {
            success: function (record) {
                var detailsForm = view.down('cfg-tasks-preview-form'),
                    propertyForm = detailsForm.down('property-form');

                actionsMenu.record = record;
                actionsMenu.down('#view-details').hide();
                view.down('#tasks-view-menu #tasks-view-link').setText(record.get('name'));
                me.getApplication().fireEvent('validationtaskload', record);
                detailsForm.loadRecord(record);
                if (record.get('status') !== 'Busy') {
                    if (record.get('status') === 'Failed') {
                        view.down('#lbl-reason-field').show();
                    }  
					if (Cfg.privileges.Validation.canRun()) {
                        view.down('#run-task').show();
                    }
                }              
            }
        });
    },

    showValidationTaskHistory: function (currentTaskId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            store = me.getStore('Cfg.store.ValidationTasksHistory'),
            taskModel = me.getModel('Cfg.model.ValidationTask'),
            view;

        store.getProxy().setUrl(router.arguments);

        view = Ext.widget('cfg-validation-tasks-history', {
            router: router,
            taskId: currentTaskId
        });

        me.getApplication().fireEvent('changecontentevent', view);
        store.load();

        taskModel.load(currentTaskId, {
            success: function (record) {
                view.down('#tasks-view-menu  #tasks-view-link').setText(record.get('name'));
            }
        });
    },

    showHistoryPreview: function (selectionModel, record) {
        var me = this,
            page = me.getHistory(),
            preview = page.down('cfg-tasks-history-preview'),
            previewForm = page.down('cfg-tasks-history-preview-form');

        if (record) {
            Ext.suspendLayouts();
            preview.setTitle(record.get('startedOn_formatted'));
            previewForm.down('displayfield[name=startedOn_formatted]').setVisible(true);
            previewForm.down('displayfield[name=finishedOn_formatted]').setVisible(true);
            previewForm.loadRecord(record);
            preview.down('cfg-tasks-history-action-menu').record = record;
            if (record.get('status') === 'Failed') {
                previewForm.down('#lbl-reason-field').show();
            } else {
                previewForm.down('#lbl-reason-field').hide();
            }

            previewForm.loadRecord(record);
            Ext.resumeLayouts(true);
        }
    },

    showAddValidationTask: function () {
        var me = this,
            view = Ext.create('Cfg.view.validationtask.Add'),
            deviceGroupCombo = view.down('#cbo-validation-task-device-group'),
            recurrenceTypeCombo = view.down('#cbo-recurrence-type');

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
                returnLink: router.getRoute('administration/validationtasks/validationtask').buildUrl({taskId: taskId})
            })
        } else {
            view = Ext.create('Cfg.view.validationtask.Add', {
                edit: true,
                returnLink: router.getRoute('administration/validationtasks').buildUrl()
            })
        }
        var taskModel = me.getModel('Cfg.model.ValidationTask'),
            taskForm = view.down('#frm-add-validation-task'),
            deviceGroupCombo = view.down('#cbo-validation-task-device-group'),
            recurrenceTypeCombo = view.down('#cbo-recurrence-type');

        if (!Cfg.privileges.Validation.canAdministrate()) {
            deviceGroupCombo.disabled = true;
        }
        me.fromEdit = true;
        me.taskId = taskId;

        taskModel.load(taskId, {
            success: function (record) {
                var schedule = record.get('schedule');
                me.taskModel = record;
                me.getApplication().fireEvent('validationtaskload', record);
                taskForm.setTitle(Uni.I18n.translate('general.edit', 'CFG', 'Edit') + " '" + Ext.String.htmlEncode(record.get('name')) + "'");
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
                    view.down('#rgr-validation-tasks-recurrence-trigger').setValue({recurrence: true});
                    view.down('#num-recurrence-number').setValue(schedule.count);
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
            preview = page.down('cfg-tasks-preview'),
            previewForm = page.down('cfg-tasks-preview-form'),
            propertyForm = previewForm.down('property-form');

        Ext.suspendLayouts();
        if (record.get('status') === 'Busy') {
            Ext.Array.each(Ext.ComponentQuery.query('#run-task'), function (item) {
                item.hide();
            });
        } else {
            if (record.get('status') === 'Failed') {
                previewForm.down('#lbl-reason-field').show();
            } else {
                previewForm.down('#lbl-reason-field').hide();
            }  
			if ( Cfg.privileges.Validation.canRun()) {
                Ext.Array.each(Ext.ComponentQuery.query('#run-task'), function (item) {
                    item.show();
                });
            }
        }
        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        previewForm.loadRecord(record);
        preview.down('cfg-validation-tasks-action-menu').record = record;
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
                route = 'administration/validationtasks/validationtask';
                break;
            case 'editValidationTask':
                route = 'administration/validationtasks/validationtask/edit';
                break;
            case 'removeTask':
                me.removeTask(menu.record);
                break;
            case 'viewLog':
                route = 'administration/validationtasks/validationtask/history/occurrence';
                break;
            case 'viewHistory':
                route = 'administration/validationtasks/validationtask/history';
                break;
			case 'runTask':
                me.runTask(menu.record);
                break;

        }

        route && (route = router.getRoute(route));
        route && route.forward(router.arguments);
    },
	
	runTask: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('validationTasks.general.run', 'CFG', 'Run'),
                confirmation: function () {
                    me.submitRunTask(record, this);
                }
            });

        confirmationWindow.insert(1,
            {
                xtype: 'panel',
                itemId: 'date-errors',
                hidden: true,
				cls: 'confirmation-window',
                html: 'sssss'
            }
        );

        confirmationWindow.show({
            msg: Uni.I18n.translate('validationTasks.runMsg', 'CFG', 'This validation task will be queued to run at the earliest possible time.'),
			title: Ext.String.format(Uni.I18n.translate('validationTasks.runTask', 'CFG', "Run validation task '{0}'?"), record.data.name)			
        });
    },

    submitRunTask: function (record, confWindow) {
        var me = this,
            id = record.get('id'),
            taskModel = me.getModel('Cfg.model.ValidationTask'),
            grid,
            store,
            index,
            view;

        Ext.Ajax.request({			
            url: '/api/val/validationtasks/' + id + '/trigger',
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
                                view.down('#run-task').hide();
                            }
                            if (rec.properties() && rec.properties().count()) {
                                view.down('property-form').loadRecord(rec);
                            }
                        }
                    });
                }
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validationTasks.run', 'CFG', 'Data validation task run'));
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
            msg: Uni.I18n.translate('validationTasks.general.remove.msg', 'CFG', 'This validation task will no longer be available.'),
            title: Uni.I18n.translate('general.remove', 'CFG', 'Remove') + '&nbsp' + record.data.name + '?',
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
                    var grid = me.getPage().down('cfg-validation-tasks-grid');
                    grid.down('pagingtoolbartop').totalCount = 0;
                    grid.down('pagingtoolbarbottom').resetPaging();
                    grid.getStore().load();
                } else {
                    me.getController('Uni.controller.history.Router').getRoute('administration/validationtasks').forward();
                }
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validationTasks.general.remove.confirm.msg', 'CFG', 'Validation task removed'));
            },
            failure: function (object, operation) {
                var json = Ext.decode(operation.response.responseText, true);
                var errorText = Uni.I18n.translate('communicationtasks.error.unknown', 'CFG', 'Unknown error occurred');
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
                                href: '#/administration/validationtasks/',
                                handler: function (button, event) {
                                    this.up('messagebox').destroy();
                                }
                            }
                        ]
                    }).show({
                        ui: 'notification-error',
                        title: Uni.I18n.translate('validationTasks.general.remove.error.msg', 'CFG', 'Remove operation failed'),
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
            form = page.down('#frm-add-validation-task'),
            formErrorsPanel = form.down('#form-errors'),
            lastDayOfMonth = false,
            startOnDate,
            timeUnitValue,
            dayOfMonth,
            hours,
            minutes;

        if (form.isValid()) {
            var record = me.taskModel || Ext.create('Cfg.model.ValidationTask');

            record.beginEdit();
            if (!formErrorsPanel.isHidden()) {
                formErrorsPanel.hide();
            }

            record.set('name', form.down('#txt-task-name').getValue());
            record.set('deviceGroup', {
                id: form.down('#cbo-validation-task-device-group').getValue(),
                name: form.down('#cbo-validation-task-device-group').getRawValue()
            });
            if (form.down('#rgr-validation-tasks-recurrence-trigger').getValue().recurrence) {
                startOnDate = moment(form.down('#start-on').getValue()).valueOf();
                timeUnitValue = form.down('#cbo-recurrence-type').getValue();
                dayOfMonth = moment(startOnDate).date();
                if (dayOfMonth >= 29) {
                    lastDayOfMonth = true;
                }
                hours = form.down('#date-time-field-hours').getValue();
                minutes = form.down('#date-time-field-minutes').getValue();
                switch (timeUnitValue) {
                    case 'years':
                        record.set('schedule', {
                            count: form.down('#num-recurrence-number').getValue(),
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
                            count: form.down('#num-recurrence-number').getValue(),
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
                            count: form.down('#num-recurrence-number').getValue(),
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
                            count: form.down('#num-recurrence-number').getValue(),
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
                record.set('nextRun', null);
                record.set('schedule', null);
            }

            record.endEdit();
            record.save({
                success: function () {
                    if (button.action === 'editTask' && me.fromDetails) {
                        me.getController('Uni.controller.history.Router').getRoute('administration/validationtasks/validationtask').forward({taskId: record.getId()});
                    } else {
                        me.getController('Uni.controller.history.Router').getRoute('administration/validationtasks').forward();
                    }
                    if (button.action === 'editTask') {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validationTasks.editValidationTask.successMsg.saved', 'CFG', 'Validation task saved'));
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validationTasks.addValidationTask.successMsg', 'CFG', 'Validation task added'));
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
            recurrenceNumberField = page.down('#num-recurrence-number'),
            recurrenceTypeCombo = page.down('#cbo-recurrence-type');

        if (newValue.recurrence && !recurrenceNumberField.getValue()) {
            recurrenceNumberField.setValue(recurrenceNumberField.minValue);
        }
        if (newValue.recurrence && !recurrenceTypeCombo.getValue()) {
            recurrenceTypeCombo.setValue(recurrenceTypeCombo.store.getAt(0));
        }
    }
});
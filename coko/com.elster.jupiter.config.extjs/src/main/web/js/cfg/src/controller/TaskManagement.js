/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.controller.TaskManagement', {
    extend: 'Cfg.controller.Tasks',

    view: [
        'Cfg.view.taskmanagement.AddTaskManagement',
        'Uni.form.field.DateTime'
    ],
    stores: [
        'Cfg.store.AllTasks'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'validation-tasks-setup'
        },
        {
            ref: 'addPage',
            selector: 'cfg-validation-tasks-add-task-mgm'
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

    init: function () {
        this.control({
            'cfg-validation-tasks-add-task-mgm #rgr-validation-tasks-recurrence-trigger': {
                change: this.onRecurrenceTriggerChange
            },
            '#cfg-tasks-history-action-menu-tsk': {
                click: this.chooseAction
            }
        });
        Apr.TaskManagementApp.addTaskManagementApp(this.getType(), {
            name: Uni.I18n.translate('general.dataValidation', 'CFG', 'Data validation'),
            controller: this
        });
    },

    historyActionItemId: 'cfg-tasks-history-action-menu-tsk',
    canAdministrate: function () {
        return Cfg.privileges.Validation.canAdministrate();
    },

    canView: function () {
        return Cfg.privileges.Validation.canViewOrAdministrate();
    },

    canRun: function () {
        return Cfg.privileges.Validation.canRun();
    },

    canEdit: function () {  // Lau - folosesc canEdit ptr suspend
        return Cfg.privileges.Validation.canAdministrate();
    },

    canSetTriggers: function () {
        return Cfg.privileges.Validation.canAdministrate();
    },

    canHistory: function () {
        return Cfg.privileges.Validation.canViewOrAdministrate();
    },

    canRemove: function () {
        return Cfg.privileges.Validation.canAdministrate();
    },

    getType: function () {
        return 'DataValidation';
    },

    getTaskForm: function (caller, completedFunc) {
        var me = this,
            appName = Uni.util.Application.getAppName(),
            view = Ext.create('Cfg.view.taskmanagement.AddTaskManagement',
                {
                    edit: false,
                    appName: appName
                }),
            followByStore = view.down('#followedBy-combo').getStore(),
            recurrenceTypeCombo = view.down('#cbo-recurrence-type');

        followByStore.load({
            callback: function () {
                view.down('#cfg-validation-task-add-loglevel').setValue(900);
                me.loadGroup(appName, view);
                me.recurrenceEnableDisable(view);
                completedFunc.call(caller, view);
            }
        });

        return view;
    },

    saveTaskForm: function (panel, formErrorsPanel, saveOperationComplete, controller) {
        var me = this,
            form = panel.down('cfg-validation-tasks-add-task-mgm'),
            page = me.getAddPage(),
            lastDayOfMonth = false,
            dataSourcesContainer = me.getAddPage().down('#field-validation-task-group'),
            startOnDate,
            timeUnitValue,
            dayOfMonth,
            hours,
            minutes;

        var record = page.getRecord() || Ext.create('Cfg.model.ValidationTask');

        form.getForm().clearInvalid();
        record.beginEdit();
        if (!formErrorsPanel.isHidden()) {
            formErrorsPanel.hide();
        }

        record.set('name', form.down('#txt-task-name').getValue());
        record.set('logLevel', form.down('#cfg-validation-task-add-loglevel').getValue());

        if (dataSourcesContainer) {
            dataSourcesContainer.setDataSourcesToRecord(record);
        }

        startOnDate = moment(form.down('#start-on').getValue()).valueOf();
        if (form.down('#rgr-validation-tasks-recurrence-trigger').getValue().recurrence) {
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
                case 'hours':
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
                case 'minutes':
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
        } else {
            record.set('schedule', null);
        }
        record.set('nextRun', startOnDate);

        // set selected tasks
        var selectedTask = [];
        Ext.Array.each(form.down('#followedBy-combo').getValue(), function (value) {
            selectedTask.push({id: value});
        })
        record.nextRecurrentTasksStore = Ext.create('Ext.data.Store', {
            fields: ['id'],
            data: selectedTask
        });

        page.setLoading(true);
        record.endEdit();
        record.save({
            // backUrl: button.action === 'editTask' && me.fromDetails
            //     ? me.getController('Uni.controller.history.Router').getRoute('administration/validationtasks/validationtask').buildUrl({taskId: record.getId()})
            //     : me.getController('Uni.controller.history.Router').getRoute('administration/validationtasks').buildUrl(),
            success: function (record, operation) {
                saveOperationComplete.call(controller);
                switch (operation.action) {
                    case 'update':
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validationTasks.editValidationTask.successMsg.saved', 'CFG', 'Validation task saved'));
                        break;
                    case 'create':
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validationTasks.addValidationTask.successMsg', 'CFG', 'Validation task added'));
                        break;
                }
                //    if (button.action === 'editTask' && me.fromDetails) {
                //        me.getController('Uni.controller.history.Router').getRoute('administration/validationtasks/validationtask').forward({taskId: record.getId()});
                //     } else {
                //        me.getController('Uni.controller.history.Router').getRoute('administration/validationtasks').forward();
                //     }
                //    if (button.action === 'editTask') {
                //        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validationTasks.editValidationTask.successMsg.saved', 'CFG', 'Validation task saved'));
                //     } else {
                //         me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validationTasks.addValidationTask.successMsg', 'CFG', 'Validation task added'));
                //     }
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        form.getForm().markInvalid(json.errors);
                        formErrorsPanel.show();
                    }
                }
            },
            callback: function () {
                page.setLoading(false);
            }
        })
    },

    runTaskManagement: function (taskManagement, operationStartFunc, operationCompletedFunc, controller) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('validationTasks.general.run', 'CFG', 'Run'),
                confirmation: function () {
                    me.submitRunTask(taskManagement, operationStartFunc, operationCompletedFunc, controller, this);
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
            title: Uni.I18n.translate('validationTasks.runTask', 'CFG', "Run validation task '{0}'?", [taskManagement.get('name')])
        });
    },

    submitRunTask: function (taskManagement, operationStartFunc, operationCompletedFunc, controller, confWindow) {
        var me = this;

        operationStartFunc.call(controller);
        Ext.Ajax.request({
            url: '/api/val/validationtasks/recurrenttask/' + taskManagement.get('id'),
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Cfg.store.ValidationTasks');

                store.loadRawData([response]);
                store.each(function (record) {
                    Ext.Ajax.request({
                        url: '/api/val/validationtasks/' + record.get('id') + '/trigger',
                        method: 'PUT',
                        jsonData: record.getProxy().getWriter().getRecordData(record),
                        isNotEdit: true,
                        success: function () {
                            confWindow.destroy();
                            operationCompletedFunc.call(controller, true);
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validationTasks.run', 'CFG', 'Data validation task run queued'));
                        },
                        failure: function (response) {
                            operationCompletedFunc.call(controller, false);
                            if (response.status === 400) {
                                var res = Ext.JSON.decode(response.responseText);
                                confWindow.update(res.errors[0].msg);
                                confWindow.setVisible(true);
                            }
                            else {
                                confWindow.destroy();
                            }
                        }
                    });
                });
            }
        })
    },

    editTaskManagement: function (taskManagementId, formErrorsPanel,
                                  operationStartFunc, editOperationCompleteLoading,
                                  operationCompletedFunc, setTitleFunc, controller) {
        var me = this,
            appName = Uni.util.Application.getAppName(),
            taskForm = me.getAddPage();

        operationStartFunc.call(controller);
        Ext.Ajax.request({
            url: '/api/val/validationtasks/recurrenttask/' + taskManagementId,
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Cfg.store.ValidationTasks');

                store.loadRawData([response]);
                store.each(function (record) {
                        setTitleFunc.call(controller, record.get('name'));

                        var schedule = record.get('schedule'),
                            recurrenceTypeCombo,
                            callback = function () {
                                recurrenceTypeCombo = taskForm.down('#cbo-recurrence-type');

                                Ext.suspendLayouts();
                                taskForm.loadRecord(record);

                                var nextRecurrentTasks = record.get('nextRecurrentTasks');
                                if (nextRecurrentTasks) {
                                    var selectedTasks = [];
                                    Ext.Array.each(nextRecurrentTasks, function (nextRecurrentTask) {
                                        selectedTasks.push(nextRecurrentTask.id);
                                    });
                                    taskForm.down('[name=nextRecurrentTasks]').setValue(selectedTasks);
                                }

                                if (record.data.nextRun && (record.data.nextRun !== 0)) {
                                    taskForm.down('#start-on').setValue(record.data.nextRun);
                                }

                                if (schedule) {
                                    taskForm.down('#rgr-validation-tasks-recurrence-trigger').setValue({recurrence: true});
                                    taskForm.down('#num-recurrence-number').setValue(schedule.count);
                                    recurrenceTypeCombo.setValue(schedule.timeUnit);
                                } else {
                                    recurrenceTypeCombo.setValue(recurrenceTypeCombo.store.getAt(2));
                                }

                                Ext.resumeLayouts(true);
                            };
                        if (taskForm.rendered) {
                            switch (appName) {
                                case me.MULTISENSE_KEY:
                                {
                                    callback();
                                }
                                    break;
                                case me.INSIGHT_KEY:
                                {
                                    callback();
                                }
                                    break;
                            }
                        }
                        editOperationCompleteLoading.call(controller)
                    }
                );
            }
        })
    },

    historyTaskManagement: function (taskId) {
        var me = this,
            controller = me.getController('Cfg.controller.Log');

        controller.detailLogRoute = me.detailLogRoute;
        this.showValidationTaskHistory(taskId);
    },

    historyLogTaskManagement: function (taskId, occurrenceId) {
        var me = this,
            controller = me.getController('Cfg.controller.Log');

        controller.viewLogRoute = me.viewLogRoute;
        controller.detailLogRoute = me.detailLogRoute;
        controller.logRoute = me.logRoute;
        controller.showLog(taskId, occurrenceId);
    },

    getTask: function (controller, taskManagementId, operationCompleted) {
        var me = this;

        Ext.Ajax.request({
            url: '/api/val/validationtasks/recurrenttask/' + taskManagementId,
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Cfg.store.ValidationTasks');
                store.loadRawData([response]);
                store.each(function (record) {
                    operationCompleted.call(controller, me, taskManagementId, record);
                });
            }
        })
    },

    removeTaskManagement: function (taskManagement, startRemovingFunc, removeCompleted, controller) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation');

        confirmationWindow.show({
            msg: Uni.I18n.translate('validationTasks.general.remove.msg', 'CFG', 'This validation task will no longer be available.'),
            title: Uni.I18n.translate('general.removex', 'CFG', "Remove '{0}'?", [taskManagement.get('name')]),
            config: {},
            fn: function (state) {
                if (state === 'confirm') {
                    me.removeOperation(taskManagement, startRemovingFunc, removeCompleted, controller);
                } else if (state === 'cancel') {
                    this.close();
                }
            }
        });
    },

    removeOperation: function (taskManagement, startRemovingFunc, removeCompleted, controller) {
        var me = this;

        startRemovingFunc.call(controller);
        Ext.Ajax.request({
            url: '/api/val/validationtasks/recurrenttask/' + taskManagement.get('id'),
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Cfg.store.ValidationTasks');
                store.loadRawData([response]);
                store.each(function (record) {
                    record.destroy({
                        success: function () {
                            removeCompleted.call(controller, true);
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validationTasks.general.remove.confirm.msg', 'CFG', 'Validation task removed'));
                        },
                        failure: function (record, operation) {
                            removeCompleted.call(controller, false);
                            if (operation.response.status === 409) {
                                return;
                            }
                        }
                    });
                });
            }
        })
    },

    viewTaskManagement: function (taskId, actionMenu, taskManagementRecord) {
        var me = this;

        me.detailRoute = me.detailRoute;
        me.historyRoute = me.historyRoute;
        me.actionMenu = actionMenu;
        me.showValidationTaskDetailsView(taskId);
        me.getDetailsPage().down('#validation-task-suspend').setValue(taskManagementRecord.get('suspendUntilTime'));
        me.getDetailsPage().down('#' + actionMenu.itemId).record = taskManagementRecord;
    }
});
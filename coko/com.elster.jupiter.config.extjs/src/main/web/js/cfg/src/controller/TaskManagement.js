/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.controller.TaskManagement', {
    extend: 'Cfg.controller.Tasks',

    view: [
        'Cfg.view.taskmanagement.AddTaskManagement',
        'Uni.form.field.DateTime'
    ],
    refs: [
        {
            ref: 'addPage',
            selector: 'cfg-validation-tasks-add-task-mgm'
        }
    ],

    init: function () {
        this.control({
            'cfg-validation-tasks-add-task-mgm #rgr-validation-tasks-recurrence-trigger': {
                change: this.onRecurrenceTriggerChange
            }
        });
        Apr.TaskManagementApp.addTaskManagementApp('DataValidation', {
            name: Uni.I18n.translate('general.dataValidation', 'CFG', 'Data validation'),
            controller: this
        });
    },

    getTaskForm: function () {
        var me = this,
            appName = Uni.util.Application.getAppName(),
            view = Ext.create('Cfg.view.taskmanagement.AddTaskManagement',
                {
                    edit: false,
                    appName: appName
                }),
            recurrenceTypeCombo = view.down('#cbo-recurrence-type');

        view.down('#cfg-validation-task-add-loglevel').setValue(900);
        me.loadGroup(appName, view);
        me.recurrenceEnableDisable(view);
        return view;
    },

    saveTaskForm: function (panel, formErrorsPanel) {
        var me = this,
            form = panel.down('cfg-validation-tasks-add-task-mgm'),
            page = me.getAddPage(),
        //form = page.down('#frm-add-validation-task'),
        //formErrorsPanel = form.down('#form-errors'),
            lastDayOfMonth = false,
            dataSourcesContainer = me.getAddPage().down('#field-validation-task-group'),
            startOnDate,
            timeUnitValue,
            dayOfMonth,
            hours,
            minutes;

        // var record = me.taskModel || Ext.create('Cfg.model.ValidationTask');
        var record = Ext.create('Cfg.model.ValidationTask');

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
            record.set('nextRun', startOnDate);
        } else {
            record.set('nextRun', null);
            record.set('schedule', null);
        }

        page.setLoading(true);
        record.endEdit();
        record.save({
            // backUrl: button.action === 'editTask' && me.fromDetails
            //     ? me.getController('Uni.controller.history.Router').getRoute('administration/validationtasks/validationtask').buildUrl({taskId: record.getId()})
            //     : me.getController('Uni.controller.history.Router').getRoute('administration/validationtasks').buildUrl(),
            success: function () {
                me.getController('Uni.controller.history.Router').getRoute(me.getController('Uni.controller.history.Router').currentRoute.replace('/add', '')).forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('validationTasks.addValidationTask.successMsg', 'CFG', 'Validation task added'));
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

    canAdministrate: function () {
        return true;
    },

    canRun: function () {
        return true;
    },

    canEdit: function () {
        return true;
    },

    canHistory: function () {
        return true;
    },

    canRemove: function () {
        return true;
    },

    runTaskManagement: function (taskManagement) {

    },

    editTaskManagement: function (taskManagement) {

    },

    historyTaskManagement: function (taskManagement) {

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
                store.each(function (rec) {
                    rec.destroy({
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
    }
});
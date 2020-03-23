/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.crlrequest.controller.TaskManagementCrlRequest', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.crlrequest.model.CrlRequest'
    ],

    views: [
        'Mdc.crlrequest.view.AddEditCrlRequest',
        'Mdc.crlrequest.view.DetailsCrlRequest'
    ],
    stores: [
        'Mdc.model.MeterGroup'
    ],
    refs: [
        {ref: 'crlRequestAddEditForm', selector: 'crl-request-addedit-tgm'}
    ],

    init: function () {
        Apr.TaskManagementApp.addTaskManagementApp(this.getType(), {
            name: Uni.I18n.translate('general.crlRequest', 'MDC', 'CRL Request'),
            controller: this
        });
    },

    canView: function () {
        return Mdc.privileges.CrlRequest.canView();
    },

    canAdministrate: function () {
        return Mdc.privileges.CrlRequest.canEdit();
    },

    canEdit: function () {
        return Mdc.privileges.CrlRequest.canEdit();
    },

    canSetTriggers: function () {
        return Mdc.privileges.CrlRequest.canEdit();
    },

    canRemove: function () {
        return Mdc.privileges.CrlRequest.canEdit();
    },

    canRun: function () {
        return false;
    },

    canHistory: function () {
        return false;
    },

    getType: function () {
        return 'CrlRequest';
    },

    getTaskForm: function (caller, completedFunc) {
        var form = Ext.create('Mdc.crlrequest.view.AddEditCrlRequest'),
            model = Ext.create('Mdc.crlrequest.model.CrlRequest');

        form.loadRecord(model);
        form.form.clearInvalid();
        completedFunc.call(caller, form);

        return form;
    },

    saveTaskForm: function (panel, formErrorsPanel, saveOperationComplete, controller) {
        var me = this,
            form = me.getCrlRequestAddEditForm(),
            recurrenceNumber = form.down('#crl-recurrence-number'),
            recurrenceType = form.down('#crl-recurrence-type'),
            editTask = panel.up('#frm-add-task').down('#task-management-task-type').isDisabled(),
            record = {};

        if (form.isValid()) {
            formErrorsPanel.setVisible(false);
            form.setLoading();
            form.updateRecord();
            record = form.getRecord();
            record.beginEdit();

            var nextRun = record.get('nextRun').getTime(),
                nextRunDate = moment(nextRun),
                dayOfMonth = nextRunDate.date(),
                lastDayOfMonth = dayOfMonth >= 29,
                hours = nextRunDate.hours(),
                minutes = nextRunDate.minutes();

            record.set('nextRun', nextRun);
            record.set('securityAccessor', { id: record.get('securityAccessor')});
            record.set('logLevel', { id: record.get('logLevel')});

            var timeUnit = recurrenceType.getValue(),
                periodicalExpressionInfo = {};
            if (timeUnit){
                switch (timeUnit) {
                    case 'years':
                        periodicalExpressionInfo = {
                            count: recurrenceNumber.getValue(),
                            timeUnit: timeUnit,
                            offsetMonths: nextRunDate.month() + 1,
                            offsetDays: dayOfMonth,
                            lastDayOfMonth: lastDayOfMonth,
                            dayOfWeek: null,
                            offsetHours: hours,
                            offsetMinutes: minutes,
                            offsetSeconds: 0
                        };
                        break;
                    case 'months':
                        periodicalExpressionInfo = {
                            count: recurrenceNumber.getValue(),
                            timeUnit: timeUnit,
                            offsetMonths: 0,
                            offsetDays: dayOfMonth,
                            lastDayOfMonth: lastDayOfMonth,
                            dayOfWeek: null,
                            offsetHours: hours,
                            offsetMinutes: minutes,
                            offsetSeconds: 0
                        };
                        break;
                    case 'weeks':
                        periodicalExpressionInfo = {
                            count: recurrenceNumber.getValue(),
                            timeUnit: timeUnit,
                            offsetMonths: 0,
                            offsetDays: 0,
                            lastDayOfMonth: lastDayOfMonth,
                            dayOfWeek: nextRunDate.format('dddd').toUpperCase(),
                            offsetHours: hours,
                            offsetMinutes: minutes,
                            offsetSeconds: 0
                        };
                        break;
                    case 'days':
                        periodicalExpressionInfo = {
                            count: recurrenceNumber.getValue(),
                            timeUnit: timeUnit,
                            offsetMonths: 0,
                            offsetDays: 0,
                            lastDayOfMonth: lastDayOfMonth,
                            dayOfWeek: null,
                            offsetHours: hours,
                            offsetMinutes: minutes,
                            offsetSeconds: 0
                        };
                        break;
                    case 'hours':
                        periodicalExpressionInfo = {
                            count: recurrenceNumber.getValue(),
                            timeUnit: timeUnit,
                            offsetMonths: 0,
                            offsetDays: 0,
                            lastDayOfMonth: lastDayOfMonth,
                            dayOfWeek: null,
                            offsetHours: hours,
                            offsetMinutes: minutes,
                            offsetSeconds: 0
                        };
                        break;
                    case 'minutes':
                        periodicalExpressionInfo = {
                            count: recurrenceNumber.getValue(),
                            timeUnit: timeUnit,
                            offsetMonths: 0,
                            offsetDays: 0,
                            lastDayOfMonth: lastDayOfMonth,
                            dayOfWeek: null,
                            offsetHours: hours,
                            offsetMinutes: minutes,
                            offsetSeconds: 0
                        };
                        break;
                }
            }
            else{
                periodicalExpressionInfo.schedule = null;
            }

            record.set('periodicalExpressionInfo', periodicalExpressionInfo);

            record.endEdit();
            record.save({
                success: function (record, operation) {
                    var successMessage = editTask
                        ? Uni.I18n.translate('crlRequest.saved', 'MDC', 'CRL request task saved')
                        : Uni.I18n.translate('crlRequest.added', 'MDC', 'CRL request task added');

                    saveOperationComplete.call(controller);
                    me.getApplication().fireEvent('acknowledge', successMessage);
                },
                failure: function (record, operation) {
                    if (operation.response.status == 400) {
                        formErrorsPanel.show();
                        if (!Ext.isEmpty(operation.response.responseText)) {
                            var json = Ext.decode(operation.response.responseText, true);
                            if (json && json.errors) {
                                form.getForm().markInvalid(json.errors);
                            }
                        }
                    }
                },
                callback: function () {
                    form.setLoading(false);
                }
            });
        } else {
            formErrorsPanel.setVisible(true);
        }
    },

    runTaskManagement: function (taskManagement) {
    },

    editTaskManagement: function (taskManagementId, formErrorsPanel,
                                  operationStartFunc, editOperationCompleteLoading,
                                  operationCompletedFunc, setTitleFunc, controller) {
        var me = this,
            form = me.getCrlRequestAddEditForm(),
            recurrenceNumber = form.down('#crl-recurrence-number'),
            recurrenceType = form.down('#crl-recurrence-type'),
            model = Ext.ModelManager.getModel('Mdc.crlrequest.model.CrlRequest'),
            router = me.getController('Uni.controller.history.Router'),
            record = {};

        operationStartFunc.call(controller);

        model.load(router.arguments.taskManagementId, {
            success: function (record) {
                var periodicalExpressionInfo = record.get('periodicalExpressionInfo') ? record.get('periodicalExpressionInfo') : {};
                record.set('securityAccessor', record.get('securityAccessor').id);
                record.set('logLevel', record.get('logLevel').id);
                setTitleFunc.call(controller, record.get('caName'));
                form.loadRecord(record);
                recurrenceNumber.setValue(periodicalExpressionInfo.count);
                recurrenceType.setValue(periodicalExpressionInfo.timeUnit);
                form.form.clearInvalid();
                editOperationCompleteLoading.call(controller);
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    formErrorsPanel.show();
                    if (!Ext.isEmpty(operation.response.responseText)) {
                        var json = Ext.decode(operation.response.responseText, true);
                        if (json && json.errors) {
                            form.getForm().markInvalid(json.errors);
                        }
                    }
                }
            }
        });
    },

    historyTaskManagement: function (taskManagement) {
        return false;
    },

    getTask: function (controller, taskManagementId, operationCompleted) {
        var me = this,
            model = Ext.ModelManager.getModel('Mdc.crlrequest.model.CrlRequest'),
            router = me.getController('Uni.controller.history.Router');

        model.load(router.arguments.taskManagementId, {
            success: function (record) {
                record.set('id', me.getType());
                operationCompleted.call(controller, me, taskManagementId, record);
            }
        });
    },


    removeTaskManagement: function (taskManagement, startRemovingFunc, removeCompleted, controller) {
        var me = this;

        startRemovingFunc.call(controller);

        Ext.create('Uni.view.window.Confirmation').show({
            title: Uni.I18n.translate('general.removeCrlRequestTask', 'MDC', "Remove this CRL request task?"),
            msg: Uni.I18n.translate('crlRequest.deleteConfirmation.msg', 'MDC', 'This CRL request task will no longer be available.'),
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        me.removeOperation(taskManagement, startRemovingFunc, removeCompleted, controller);
                        break;
                }
            }
        });
    },

    removeOperation: function (taskManagement, startRemovingFunc, removeCompleted, controller) {
        var me = this,
            model = Ext.create('Mdc.crlrequest.model.CrlRequest'),
            router = me.getController('Uni.controller.history.Router');

        Ext.Ajax.request({
            url: '/api/ddr/crlprops/'+ taskManagement.get("id"),
            method: 'DELETE',
            success: function () {
                removeCompleted.call(controller, true);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('crlRequest.removed', 'MDC', 'Crl request task removed'));
            },
            failure: function (record, operation) {
                removeCompleted.call(controller, false);
            }
        });
    },

    viewTaskManagement: function (taskId, actionMenu, taskManagementRecord) {
        var me = this,
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            model = Ext.ModelManager.getModel('Mdc.crlrequest.model.CrlRequest'),
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('crl-request-details', {
                actionMenu: actionMenu,
                canAdministrate: me.canAdministrate()
            });

        pageMainContent.setLoading(true);

        model.load(router.arguments.taskManagementId, {
            success: function (record) {
                var periodicalExpressionInfo = record.get('periodicalExpressionInfo');

                record.set('id', me.getType());
                record.set('securityAccessor', record.get('securityAccessor').name);
                record.set('logLevel', record.get('logLevel').name);
                record.set('task', record.get('task').name);
                widget.loadRecord(record);
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getApplication().fireEvent('loadTask', me.getType());
                widget.down('#' + actionMenu.itemId) && (widget.down('#' + actionMenu.itemId).record = taskManagementRecord);
            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        });
    }


});

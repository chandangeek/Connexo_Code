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
        return true;
        // return Mdc.privileges.CrlRequest.canView();
    },

    canAdministrate: function () {
        return true;
        // return Mdc.privileges.CrlRequest.canEdit();
    },

    canEdit: function () {
        return true;
        // return Mdc.privileges.CrlRequest.canEdit();
    },

    canSetTriggers: function () {
        return true;
        // return Mdc.privileges.CrlRequest.canEdit();
    },

    canRemove: function () {
        return true;
        // return Mdc.privileges.CrlRequest.canEdit();
    },

    canRun: function () {
        return true;
    },

    canHistory: function () {
        return true;
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
            record = {};

        if (form.isValid()) {
            formErrorsPanel.setVisible(false);
            form.setLoading();
            form.updateRecord();
            record = form.getRecord();
            record.beginEdit();

            record.set('nextRun', record.get('nextRun').getTime());
            record.set('securityAccessor', { id: record.get('securityAccessor')});
            record.set('timeDurationInfo', {
                count: recurrenceNumber.getValue(),
                timeUnit: recurrenceType.getValue()
            });

            record.endEdit();
            record.save({
                success: function (record, operation) {
                    var successMessage = operation.action === 'create'
                        ? Uni.I18n.translate('crlRequest.added', 'MDC', 'CRL request task added')
                        : Uni.I18n.translate('crlRequest.saved', 'MDC', 'CRL request task saved');

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
            record = {};

        operationStartFunc.call(controller);

        model.load('', {
            success: function (record) {
                var timeDurationInfo = record.get('timeDurationInfo') ? record.get('timeDurationInfo') : {};
                record.set('securityAccessor', record.get('securityAccessor').id);
                form.loadRecord(record);
                recurrenceNumber.setValue(timeDurationInfo.count);
                recurrenceType.setValue(timeDurationInfo.timeUnit);
                form.form.clearInvalid();
                editOperationCompleteLoading.call(controller);
            }
        });
    },

    historyTaskManagement: function (taskManagement) {
        return false;
    },

    getTask: function (controller, taskManagementId, operationCompleted) {
        var me = this,
            model = Ext.ModelManager.getModel('Mdc.crlrequest.model.CrlRequest');

        model.load('', {
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
            title: Uni.I18n.translate('general.removeCrlRequestTask', 'MDC', "Remove the CRL request task?"),
            msg: Uni.I18n.translate('crlRequest.deleteConfirmation.msg', 'MDC', 'This CRL request will no longer be available.'),
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        me.removeOperation(null, startRemovingFunc, removeCompleted, controller);
                        break;
                }
            }
        });
    },

    removeOperation: function (record, startRemovingFunc, removeCompleted, controller) {
        var me = this,
            model = Ext.create('Mdc.crlrequest.model.CrlRequest');

        Ext.Ajax.request({
            url: '/api/ddr/crlprops',
            method: 'DELETE',
            success: function () {
                removeCompleted.call(controller, true);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('crlRequest.removed', 'MDC', 'Registered devices KPI removed'));
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
            widget = Ext.widget('crl-request-details', {
                actionMenu: actionMenu,
                canAdministrate: me.canAdministrate()
            });

        pageMainContent.setLoading(true);

        model.load('', {
            success: function (record) {
                var timeDurationInfo = record.get('timeDurationInfo');

                record.set('id', me.getType());
                record.set('securityAccessor', record.get('securityAccessor').name);
                widget.loadRecord(record);
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getApplication().fireEvent('loadTask', me.getType());
            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        });
    }


});

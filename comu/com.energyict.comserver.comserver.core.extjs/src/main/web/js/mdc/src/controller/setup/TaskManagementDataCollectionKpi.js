/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.TaskManagementDataCollectionKpi', {
    extend: 'Mdc.controller.setup.DataCollectionKpi',
    views: [
        'Mdc.view.setup.taskmanagement.AddEditDataCollectionKpis',
        'Mdc.view.setup.taskmanagement.DetailsDataCollectionKpi'
    ],
    refs: [
        {ref: 'dataCollectionKpiEditForm', selector: 'data-collection-kpi-addedit-tgm'}
    ],

    init: function () {
        this.control({
            'data-collection-kpi-addedit-tgm #cmb-frequency': {
                change: this.onFrequencyChange
            }
        });
        Apr.TaskManagementApp.addTaskManagementApp(this.getType(), {
            name: Uni.I18n.translate('general.datadataCollectionKPI', 'MDC', 'Data collection KPI'),
            controller: this
        });
    },

    canAdministrate: function () {
        return Mdc.privileges.DataCollectionKpi.canEdit();
    },

    canView: function () {
        return Mdc.privileges.DataCollectionKpi.canView();
    },

    canRun: function () {
        return false;
    },

    canEdit: function () {
        return Mdc.privileges.DataCollectionKpi.canEdit();
    },

    canSetTriggers: function () {
        return Mdc.privileges.DataCollectionKpi.canEdit();
    },

    canHistory: function () {
        return false;
    },

    getType: function () {
        return 'MDCKpiCalculatorTopic';
    },

    canRemove: function () {
        return Mdc.privileges.DataCollectionKpi.canEdit();
    },

    getTaskForm: function (caller, completedFunc) {
        var me = this,
            form = Ext.create('Mdc.view.setup.taskmanagement.AddEditDataCollectionKpis'),
            deviceGroupStore = form.down('combobox[name=deviceGroup]').getStore(),
            kpiModel = Ext.ModelManager.getModel('Mdc.model.DataCollectionKpi'),
            deviceGroupCombo = form.down('#cmb-device-group'),
            deviceGroupDisplayField = form.down('#devicegroupDisplayField'),
            createBtn = form.down('#createEditButton');

        deviceGroupStore.load({
            callback: function () {
                if (deviceGroupStore.getCount() > 0) {
                    completedFunc.call(caller, form);
                    //form.loadRecord(Ext.create(kpiModel));
                } else {
                    Ext.suspendLayouts();
                    deviceGroupDisplayField.setValue('<span style="color: #eb5642">' + Uni.I18n.translate('datacollectionkpis.noDeviceGroup', 'MDC', 'No device group defined yet.') + '</span>');
                    deviceGroupDisplayField.show();
                    deviceGroupCombo.hide();
                    createBtn.disable();
                    Ext.resumeLayouts(true);
                }
                //  widget.setLoading(false);
            }
        });
        return form;
    },

    saveTaskForm: function (panel, formErrorsPanel, saveOperationComplete, controller) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            editForm = panel.down('data-collection-kpi-addedit-tgm'),
            record = editForm.getRecord() || Ext.create('Mdc.model.DataCollectionKpi'),
            deviceGroup = {
                id: editForm.down('[name=deviceGroup]').getValue()
            },
            frequency = editForm.down('[name=frequency]').getValue(),
            displayRange = editForm.down('[name=displayRange]').getValue(),
            connectionTarget = editForm.down('#connectionKpiField').getValue(),
            communicationTarget = editForm.down('#communicationKpiField').getValue();

        formErrorsPanel.hide();
        editForm.getForm().clearInvalid();
        editForm.setLoading();
        record.beginEdit();
        if (!record.getId()) {
            record.set('deviceGroup', deviceGroup);
        } else {
            record.set('deviceGroup', {
                id: record.get('deviceGroup').id,
                name: record.get('deviceGroup').name
            });
        }
        if (frequency) {
            record.set('frequency', me.getStore('Mdc.store.DataCollectionKpiFrequency').getById(frequency).get('value'));
        }
        if (displayRange) {
            record.set('displayRange', me.getStore('Mdc.store.DataCollectionKpiRange').getById(displayRange).get('value'));
        }
        record.set('communicationTarget', communicationTarget);
        record.set('connectionTarget', connectionTarget);
        record.endEdit();
        record.save({
            success: function (record, operation) {
                var successMessage = '';

                switch (operation.action) {
                    case 'update':
                        successMessage = Uni.I18n.translate('datacollectionkpis.saved', 'MDC', 'Data collection KPI saved');
                        break;
                    case 'create':
                        successMessage = Uni.I18n.translate('datacollectionkpis.added', 'MDC', 'Data collection KPI added');
                        break;
                }

                //me.getController('Uni.controller.history.Router').getRoute(me.getController('Uni.controller.history.Router').currentRoute.replace('/add', '')).forward();
                saveOperationComplete.call(controller);
                me.getApplication().fireEvent('acknowledge', successMessage);
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    formErrorsPanel.show();
                    if (!Ext.isEmpty(operation.response.responseText)) {
                        var json = Ext.decode(operation.response.responseText, true);
                        if (json && json.errors) {
                            Ext.each(json.errors, function (error) {
                                if (error.id === 'communicationKpi') {
                                    editForm.down('#communicationKpiField #noTarget').markInvalid(error.msg);
                                }
                                if (error.id === 'endDeviceGroup') {
                                    deviceGroupCombo.markInvalid(error.msg);
                                }
                            });
                            editForm.getForm().markInvalid(json.errors);
                        }
                    }
                }
            },
            callback: function () {
                editForm.setLoading(false);
            }
        });
    },

    runTaskManagement: function (taskManagement) {
    },

    editTaskManagement: function (taskManagementId, formErrorsPanel,
                                  operationStartFunc, editOperationCompleteLoading,
                                  operationCompletedFunc, setTitleFunc, controller) {
        var me = this,
            form = me.getDataCollectionKpiEditForm();

        operationStartFunc.call(controller);
        Ext.Ajax.request({
            url: '/api/ddr/kpis/recurrenttask/' + taskManagementId,
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Mdc.store.DataCollectionKpis');
                store.loadRawData([response]);
                store.each(function (record) {
                    setTitleFunc.call(controller, record.get('deviceGroup').name);
                    Ext.suspendLayouts();
                    form.loadRecord(record);
                    form.down('[name=deviceGroup]').disable();
                    form.down('[name=frequency]').disable();
                    Ext.resumeLayouts(true);
                    editOperationCompleteLoading.call(controller)
                });
            }
        })
    },

    historyTaskManagement: function () {
        return false;
    },

    getTask: function (controller, taskManagementId, operationCompleted) {
        var me = this;

        Ext.Ajax.request({
            url: '/api/ddr/kpis/recurrenttask/' + taskManagementId,
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Mdc.store.DataCollectionKpis');
                store.loadRawData([response]);
                store.each(function (record) {
                    operationCompleted.call(controller, me, taskManagementId, record);
                });
            }
        })
    },

    removeTaskManagement: function (taskManagement, startRemovingFunc, removeCompleted, controller) {
        var me = this;

        startRemovingFunc.call(controller);
        Ext.Ajax.request({
            url: '/api/ddr/kpis/recurrenttask/' + taskManagement.get('id'),
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Mdc.store.DataCollectionKpis');
                store.loadRawData([response]);
                store.each(function (record) {
                    Ext.create('Uni.view.window.Confirmation').show({
                        title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?", [record.get('deviceGroup').name]),
                        msg: Uni.I18n.translate('datacollectionkpis.deleteConfirmation.msg', 'MDC', 'This data collection KPI will no longer be available on connections and communications overview.'),
                        fn: function (state) {
                            switch (state) {
                                case 'confirm':
                                    me.removeOperation(record, startRemovingFunc, removeCompleted, controller);
                                    break;
                            }
                        }
                    });
                });
            }
        })
    },

    removeOperation: function (record, startRemovingFunc, removeCompleted, controller) {
        var me = this;

        record.destroy({
            success: function () {
                removeCompleted.call(controller, true);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('datacollectionkpis.kpiRemoved', 'MDC', 'Data collection KPI removed'));
            },
            failure: function (record, operation) {
                removeCompleted.call(controller, false);
                if (operation.response.status === 409) {
                    return;
                }
            }
        });
    },

    viewTaskManagement: function (taskId, actionMenu, taskManagementRecord) {
        var me = this,
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            widget = Ext.widget('data-collection-kpi-details', {
                actionMenu: actionMenu,
                canAdministrate: me.canAdministrate()
            });

        pageMainContent.setLoading(true);

        Ext.Ajax.request({
            url: '/api/ddr/kpis/recurrenttask/' + taskManagementRecord.get('id'),
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Mdc.store.DataCollectionKpis');
                store.loadRawData([response]);
                store.each(function (record) {
                    me.getApplication().fireEvent('changecontentevent', widget);
                    me.getApplication().fireEvent('loadTask', record.get('deviceGroup').name);
                    widget.down('#data-collection-kpi-details-side-menu').setHeader(record.get('deviceGroup').name);
                    var connectionTarget = record.get('connectionTarget'),
                        communicationTarget = record.get('communicationTarget'),
                        displayRange = record.get('displayRange');

                    connectionTarget = connectionTarget != null ? connectionTarget + ' %' : Uni.I18n.translate('datacollectionkpis.noKpi', 'MDC', 'No KPI');
                    communicationTarget = communicationTarget != null ? communicationTarget + ' %' : Uni.I18n.translate('datacollectionkpis.noKpi', 'MDC', 'No KPI');
                    widget.down('#data-collection-kpi-device-group').setValue(record.get('deviceGroup').name);
                    widget.down('#data-collection-kpi-frequency').setValue(displayRange ? Ext.getStore('Mdc.store.DataCollectionKpiRange').getById(displayRange.count + displayRange.timeUnit).get('name') : '');
                    widget.down('#data-collection-kpi-connection').setValue(connectionTarget);
                    widget.down('#data-collection-kpi-communication').setValue(communicationTarget);
                    widget.down('#' + actionMenu.itemId) && (widget.down('#' + actionMenu.itemId).record = taskManagementRecord);
                });
            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        })

    }
});

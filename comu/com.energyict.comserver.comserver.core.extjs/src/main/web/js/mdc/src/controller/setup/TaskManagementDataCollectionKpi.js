/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.TaskManagementDataCollectionKpi', {
    extend: 'Mdc.controller.setup.DataCollectionKpi',
    views: [
        'Mdc.view.setup.taskmanagement.AddEditDataCollectionKpis'
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
        Apr.TaskManagementApp.addTaskManagementApp('MDCKpiCalculatorTopic', {
            name: Uni.I18n.translate('general.datadataCollectionKPI', 'MDC', 'Data collection KPI'),
            controller: this
        });
    },

    canAdministrate: function () {
        return true;
    },

    canRun: function () {
        return false;
    },

    canEdit: function () {
        return Mdc.privileges.DataCollectionKpi.canEdit();
    },

    canHistory: function () {
        return false;
    },

    canRemove: function () {
        return Mdc.privileges.DataCollectionKpi.canEdit();
    },

    getTaskForm: function () {
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
                    form.loadRecord(Ext.create(kpiModel));
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
            record = editForm.getRecord(),
            deviceGroup = {
                id: editForm.down('[name=deviceGroup]').getValue()
            },
            frequency = editForm.down('[name=frequency]').getValue(),
            displayRange = editForm.down('[name=displayRange]').getValue(),
            connectionTarget = editForm.down('#connectionKpiField').getValue(),
            kpiMessageContainer = editForm.down('#kpiErrorContainer'),
            communicationTarget = editForm.down('#communicationKpiField').getValue();

        kpiMessageContainer.hide();
        formErrorsPanel.hide();

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
                                    kpiMessageContainer.update(error.msg);
                                    formErrorsPanel.show();
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
    }
});

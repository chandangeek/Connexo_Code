/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.TaskManagementRegisteredDevices', {
    extend: 'Mdc.registereddevices.controller.RegisteredDevices',

    views: [
        'Mdc.view.setup.taskmanagement.AddEditRegisteredDevicesKPI'
    ],

    refs: [
        {ref: 'kpiEditForm', selector: '#mdc-registered-devices-kpi-addedit-form'},
        {ref: 'kpiPreviewForm', selector: '#mdc-registered-devices-kpi-details-form'},
        {ref: 'kpiPreviewContainer', selector: '#mdc-registered-devices-kpi-preview'},
        {ref: 'kpisGrid', selector: 'registered-devices-kpis-grid'},
        {ref: 'kpisOverview', selector: 'registered-devices-kpis-view'},
        {ref: 'registeredDevicesView', selector: 'registered-devices-view'},
        {ref: 'registeredDevicesOnGatewayView', selector: 'registered-devices-on-gateway-view'},
        {ref: 'periodFilter', selector: 'registered-devices-view #mdc-registered-devices-filters #mdc-registered-devices-period-filter'},
        {ref: 'applyButtonOfPeriodFilter', selector: 'registered-devices-view #mdc-registered-devices-filters #mdc-registered-devices-period-filter button[action=apply]'},
        {ref: 'clearButtonOfPeriodFilter', selector: 'registered-devices-view #mdc-registered-devices-filters #mdc-registered-devices-period-filter button[action=clear]'},
        {ref: 'registeredDevicesOnGatewayView', selector: 'registered-devices-on-gateway-view'},
        {ref: 'gatewayPeriodFilter', selector: 'registered-devices-on-gateway-view #mdc-registered-devices-on-gateway-filters #mdc-registered-devices-on-gateway-period-filter'}
    ],

    init: function () {
        this.control({
            '#mdc-registered-devices-on-gateway-frequency-combo': {
                select: this.onSelectFrequency
            }
        });
        Apr.TaskManagementApp.addTaskManagementApp('MDCKpiRegisteredDevTopic', {
            name: Uni.I18n.translate('general.registeredDevicesKPI', 'MDC', 'Registered devices KPI'),
            controller: this
        });
    },

    getTaskForm: function (id) {
        var me = this,
            form = Ext.create('Mdc.view.setup.taskmanagement.AddEditRegisteredDevicesKPI'),
            deviceGroupStore = form.down('combobox[name=deviceGroup]').getStore(),
            kpiModel = Ext.ModelManager.getModel('Mdc.registereddevices.model.RegisteredDevicesKPI'),
            deviceGroupCombo = form.down('#mdc-registered-devices-kpi-add-device-group-combo'),
            deviceGroupDisplayField = form.down('#mdc-registered-devices-kpi-add-device-group-displayField'),
            createBtn = form.down('#mdc-registered-devices-kpi-add-addEditButton');

        //me.getApplication().fireEvent('changecontentevent', widget);
        //widget.setLoading(true);
        deviceGroupStore.load({
            callback: function () {
                if (!Ext.isEmpty(id)) {
                    // form.setTitle(Uni.I18n.translate('registeredDevicesKPIs.edit', 'MDC', 'Edit registered devices KPI'));
                    kpiModel.load(id, {
                        success: function (kpiRecord) {
                            Ext.suspendLayouts();
                            form.loadRecord(kpiRecord);
                            form.down('[name=deviceGroup]').disable();
                            form.down('[name=frequency]').disable();
                            form.setTitle(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", kpiRecord.get('deviceGroup').name));
                            me.getApplication().fireEvent('loadRegisteredDevicesKpi', kpiRecord.get('deviceGroup').name);
                            createBtn.setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
                            createBtn.action = 'save';
                            Ext.resumeLayouts(true);
                            //  widget.setLoading(false);
                        }
                    });
                } else {
                    if (deviceGroupStore.getCount() > 0) {
                        form.loadRecord(Ext.create(kpiModel));
                        if (deviceGroupStore.getCount() === 1) {
                            deviceGroupCombo.setValue(deviceGroupStore.getAt(0).get('id'));
                        }
                    } else {
                        Ext.suspendLayouts();
                        deviceGroupDisplayField.setValue('<span style="color: #eb5642">' + Uni.I18n.translate('general.noDeviceGroups', 'MDC', 'No device groups available') + '</span>');
                        deviceGroupDisplayField.show();
                        deviceGroupCombo.hide();
                        createBtn.disable();
                        Ext.resumeLayouts(true);
                    }
                    //  form.setTitle(Uni.I18n.translate('registeredDevicesKPIs.add', 'MDC', 'Add registered devices KPI'));
                    //    widget.setLoading(false);
                }
            }
        });

        return form;
    },

    saveTaskForm: function (panel, formErrorsPanel) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            editForm = panel.down('registered-devices-kpi-addedit-tgm'),
            record = editForm.getRecord(),
            deviceGroup = {
                id: editForm.down('[name=deviceGroup]').getValue()
            },
            frequency = editForm.down('[name=frequency]').getValue(),
            target = editForm.down('[name=target]').getValue()

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
            record.set('frequency', me.getStore('Mdc.registereddevices.store.RegisteredDevicesKPIFrequencies').getById(frequency).get('value'));
        }
        record.set('target', target);
        record.endEdit();
        record.save({
            success: function (record, operation) {
                var successMessage = '';
                switch (operation.action) {
                    case 'update':
                        successMessage = Uni.I18n.translate('registeredDevicesKPIs.saved', 'MDC', 'Registered devices KPI saved');
                        break;
                    case 'create':
                        successMessage = Uni.I18n.translate('registeredDevicesKPIs.added', 'MDC', 'Registered devices KPI added');
                        break;
                }
                me.getController('Uni.controller.history.Router').getRoute(me.getController('Uni.controller.history.Router').currentRoute.replace('/add', '')).forward();
                me.getApplication().fireEvent('acknowledge', successMessage);
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    formErrorsPanel.show();
                    if (!Ext.isEmpty(operation.response.responseText)) {
                        var json = Ext.decode(operation.response.responseText, true);
                        if (json && json.errors) {
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

    canAdministrate: function () {
        return true;
    },

    canRun: function () {
        return false;
    },

    canEdit: function () {
        return true;
    },

    canHistory: function () {
        return false;
    },

    canRemove: function () {
        return true;
    },

    runTaskManagement: function (taskManagement) {

    },

    editTaskManagement: function (taskManagement) {

    },

    historyTaskManagement: function (taskManagement) {
        return false;
    },

    removeTaskManagement: function (taskManagement, startRemovingFunc, removeCompleted, controller) {
        var me = this;

        startRemovingFunc.call(controller);
        Ext.Ajax.request({
            url: '/api/ddr/registereddevkpis/recurrenttask/' + taskManagement.get('id'),
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Mdc.registereddevices.store.RegisteredDevicesKPIs');
                store.loadRawData([response]);
                store.each(function (record) {
                    Ext.create('Uni.view.window.Confirmation').show({
                        title: Uni.I18n.translate('general.removeRegisteredDevicesKPIOnX', 'MDC', "Remove registered devices KPI on '{0}'?", record.get('deviceGroup').name),
                        msg: Uni.I18n.translate('registeredDevicesKPIs.deleteConfirmation.msg', 'MDC', 'This registered devices KPI will no longer be available.'),
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
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('registeredDevicesKPIs.removed', 'MDC', 'Registered devices KPI removed'));
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


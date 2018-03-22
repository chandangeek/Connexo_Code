/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.TaskManagementRegisteredDevices', {
    extend: 'Mdc.registereddevices.controller.RegisteredDevices',

    views: [
        'Mdc.view.setup.taskmanagement.AddEditRegisteredDevicesKpi',
        'Mdc.view.setup.taskmanagement.DetailsRegisteredDevicesKpi'
    ],
    stores: [
        'Mdc.registereddevices.store.AllTasks'
    ],
    refs: [
        {ref: 'kpiEditForm', selector: 'registered-devices-kpi-addedit-tgm'}
    ],

    init: function () {
        this.control({
            '#mdc-registered-devices-add-gateway-frequency-combo': {
                select: this.onSelectFrequency
            }
        });
        Apr.TaskManagementApp.addTaskManagementApp(this.getType(), {
            name: Uni.I18n.translate('general.registeredDevicesKPI', 'MDC', 'Registered devices KPI'),
            controller: this
        });
    },

    canAdministrate: function () {
        return Mdc.privileges.RegisteredDevicesKpi.canAdmin();
    },

    canView: function () {
        return Mdc.privileges.RegisteredDevicesKpi.canView();
    },

    canRun: function () {
        return false;
    },

    canEdit: function () {
        return Mdc.privileges.RegisteredDevicesKpi.canAdmin();
    },

    canSetTriggers: function () {
        return Mdc.privileges.RegisteredDevicesKpi.canAdmin();
    },

    canHistory: function () {
        return false;
    },

    canRemove: function () {
        return Mdc.privileges.RegisteredDevicesKpi.canAdmin();
    },

    getType: function () {
        return 'MDCKpiRegisteredDevTopic';
    },

    getTaskForm: function (caller, completedFunc) {
        var me = this,
            form = Ext.create('Mdc.view.setup.taskmanagement.AddEditRegisteredDevicesKpi'),
            deviceGroupStore = form.down('combobox[name=deviceGroup]').getStore(),
            followByStore = form.down('#followedBy-combo').getStore(),
            kpiModel = Ext.ModelManager.getModel('Mdc.registereddevices.model.RegisteredDevicesKPI'),
            deviceGroupCombo = form.down('#mdc-registered-devices-kpi-add-device-group-combo'),
            deviceGroupDisplayField = form.down('#mdc-registered-devices-kpi-add-device-group-displayField');

        followByStore.load({
            callback: function () {
                deviceGroupStore.load({
                    callback: function () {
                        if (deviceGroupStore.getCount() > 0) {
                            //form.loadRecord(Ext.create(kpiModel));
                            if (deviceGroupStore.getCount() === 1) {
                                deviceGroupCombo.setValue(deviceGroupStore.getAt(0).get('id'));
                            }
                        } else {
                            Ext.suspendLayouts();
                            deviceGroupDisplayField.setValue('<span style="color: #eb5642">' + Uni.I18n.translate('general.noDeviceGroups', 'MDC', 'No device groups available') + '</span>');
                            deviceGroupDisplayField.show();
                            deviceGroupCombo.hide();
                            Ext.resumeLayouts(true);
                        }
                        completedFunc.call(caller, form);
                    }
                });
            }
        });
        return form;
    },

    saveTaskForm: function (panel, formErrorsPanel, saveOperationComplete, controller) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            editForm = panel.down('registered-devices-kpi-addedit-tgm'),
            record = editForm.getRecord() || Ext.create('Mdc.registereddevices.model.RegisteredDevicesKPI'),
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

        // set selected tasks
        var selectedTask = [];
        Ext.Array.each(editForm.down('#followedBy-combo').getValue(), function (value) {
            selectedTask.push({id: value});
        });

        record.nextRecurrentTasksStore = Ext.create('Ext.data.Store', {
            fields: ['id'],
            data: selectedTask
        });
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
            form = me.getKpiEditForm();

        operationStartFunc.call(controller);
        Ext.Ajax.request({
            url: '/api/ddr/registereddevkpis/recurrenttask/' + taskManagementId,
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Mdc.registereddevices.store.RegisteredDevicesKPIs');
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

    historyTaskManagement: function (taskManagement) {
        return false;
    },

    getTask: function (controller, taskManagementId, operationCompleted) {
        var me = this;

        Ext.Ajax.request({
            url: '/api/ddr/registereddevkpis/recurrenttask/' + taskManagementId,
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Mdc.registereddevices.store.RegisteredDevicesKPIs');
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
    },

    viewTaskManagement: function (taskId, actionMenu, taskManagementRecord) {
        var me = this,
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            widget = Ext.widget('registered-devices-kpi-details', {
                actionMenu: actionMenu,
                canAdministrate: me.canAdministrate()
            });

        pageMainContent.setLoading(true);

        Ext.Ajax.request({
            url: '/api/ddr/registereddevkpis/recurrenttask/' + taskManagementRecord.get('id'),
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Mdc.registereddevices.store.RegisteredDevicesKPIs');
                store.loadRawData([response]);
                store.each(function (record) {
                    me.getApplication().fireEvent('changecontentevent', widget);
                    me.getApplication().fireEvent('loadTask', record.get('deviceGroup').name);
                    widget.down('#registered-devices-kpi-details-side-menu').setHeader(record.get('deviceGroup').name);
                    widget.down('#registered-devices-kpi-device-group').setValue(record.get('deviceGroup').name);
                    widget.down('#registered-devices-kpi-frequency').setValue(Mdc.util.ScheduleToStringConverter.convert(record.get('frequency')));
                    widget.down('#registered-devices-kpi-target').setValue(record.get('target'));
                    widget.setRecurrentTasks('#followedBy-field-container', record.get('nextRecurrentTasks'));
                    widget.setRecurrentTasks('#precededBy-field-container', record.get('previousRecurrentTasks'));

                    widget.down('#' + actionMenu.itemId) && (widget.down('#' + actionMenu.itemId).record = taskManagementRecord);
                });
            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        })
    }
});


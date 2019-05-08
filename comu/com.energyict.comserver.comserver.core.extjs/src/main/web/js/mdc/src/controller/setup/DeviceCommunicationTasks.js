/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.DeviceCommunicationTasks', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.ux.window.Notification',
        'Mdc.store.DeviceSchedules',
        'Mdc.view.setup.devicecommunicationtask.ChangeConnectionItemPopUp'
    ],

    views: [
        'Mdc.view.setup.devicecommunicationtask.DeviceCommunicationTaskSetup',
        'Mdc.view.setup.devicecommunicationtask.DeviceCommunicationTaskGrid'
    ],

    stores: [
        'CommunicationTasksOfDevice',
        'ConnectionMethodsOfDevice',
        'ConnectionFunctions'
    ],

    refs: [
        {ref: 'deviceCommunicationTaskGrid', selector: '#deviceCommunicationTaskGrid'},
        {ref: 'deviceCommunicationTaskPreviewForm', selector: '#deviceCommunicationTaskPreviewForm'},
        {ref: 'deviceCommunicationTaskPreview', selector: '#deviceCommunicationTaskPreview'},
        {ref: 'changeConnectionItemForm', selector: '#changeConnectionItemForm'},
        {ref: 'changeConnectionItemPopUp', selector: '#changeConnectionItemPopUp'},
        {ref: 'deviceCommunicationTaskActionMenu', selector: '#device-communication-task-action-menu'},
        {ref: 'deviceCommunicationTaskOverview', selector: 'deviceCommunicationTaskSetup'}
    ],

    init: function () {
        this.control({
                '#deviceCommunicationTaskGrid': {
                    select: this.showDeviceCommunicationTaskPreview
                },
                'device-communication-task-action-menu': {
                    beforeshow: this.configureMenu
                },
                '#changeFrequencyOfDeviceComTask[action=changeFrequencyOfDeviceComTask]': {
                    click: this.showChangePopUp
                },
                '#changeConnectionMethodOfDeviceComTask[action=changeConnectionMethodOfDeviceComTask]': {
                    click: this.showChangePopUp
                },
                '#changeUrgencyOfDeviceComTask[action=changeUrgencyOfDeviceComTask]': {
                    click: this.showChangePopUp
                },
                '#viewHistoryOfDeviceComTask[action=viewHistoryOfDeviceComTask]': {
                    click: this.showHistory
                },
                '#runDeviceComTask[action=runDeviceComTask]': {
                    click: this.runDeviceComTask
                },
                '#runDeviceComTaskNow[action=runDeviceComTaskNow]': {
                    click: this.runDeviceComTaskNow
                },
                '#changeButton[action=changeUrgencyOfDeviceComTask]': {
                    click: this.changeUrgency
                },
                '#changeButton[action=changeFrequencyOfDeviceComTask]': {
                    click: this.changeFrequency
                },
                '#changeButton[action=changeConnectionMethodOfDeviceComTask]': {
                    click: this.changeConnectionMethod
                },
                '#changeButton[action=viewHistoryOfDeviceComTask]': {
                    click: this.showHistory
                },
                '#activateDeviceComTask[action=activateDeviceComTask]': {
                    click: this.activateDeviceComTask
                },
                '#deactivateDeviceComTask[action=deactivateDeviceComTask]': {
                    click: this.deactivateDeviceComTask
                },
                '#changeButton[action=activateDeviceComTask]': {
                    click: this.activateDeviceComTask
                },
                '#changeButton[action=deactivateDeviceComTask]': {
                    click: this.deactivateDeviceComTask
                }
            }
        );
    },

    showDeviceCommunicationTasksView: function (deviceId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            communicationTasksOfDeviceStore = me.getCommunicationTasksOfDeviceStore(),
            connectionFunctionsStore = me.getConnectionFunctionsStore();

        this.deviceId = deviceId;

        viewport.setLoading();
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                var widget = Ext.widget('deviceCommunicationTaskSetup', {device: device});
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getApplication().fireEvent('loadDevice', device);
                viewport.setLoading(false);
                communicationTasksOfDeviceStore.getProxy().setExtraParam('deviceId', deviceId);
                connectionFunctionsStore.getProxy().extraParams = ({deviceType: device.data.deviceTypeId});
                communicationTasksOfDeviceStore.load({
                    callback: function () {
                        me.getDeviceCommunicationTaskGrid().getSelectionModel().doSelect(0);
                    }
                });

            }
        });
    },

    configureMenu: function (menu) {
        var selection = menu.record || this.getDeviceCommunicationTaskGrid().getSelectionModel().getSelection()[0],
            isNotShared = selection.get('scheduleTypeKey') !== 'SHARED',
            isOnHold = selection.get('isOnHold'),
            isSystemComtask = selection.get('comTask').isSystemComTask,
            connectionDefinedOnDevice = selection.get('connectionDefinedOnDevice'),
            isMinimizeConnections = !connectionDefinedOnDevice ? false : selection.get('connectionStrategyKey') === 'MINIMIZE_CONNECTIONS';

        if (menu.down('#changeConnectionMethodOfDeviceComTask')) {
            menu.down('#changeConnectionMethodOfDeviceComTask').show();
        }
        if (menu.down('#changeUrgencyOfDeviceComTask')) {
            menu.down('#changeUrgencyOfDeviceComTask').show();
        }
        if (menu.down('#runDeviceComTaskNow')) {
            if (connectionDefinedOnDevice && !isOnHold && !isSystemComtask) {
                menu.down('#runDeviceComTaskNow').show();
            } else {
                menu.down('#runDeviceComTaskNow').hide();
            }
        }
        if (menu.down('#runDeviceComTask')) {
            if (connectionDefinedOnDevice && !isOnHold && isMinimizeConnections && !isSystemComtask) {
                menu.down('#runDeviceComTask').show();
            } else {
                menu.down('#runDeviceComTask').hide();
            }
        }
        if (menu.down('#viewHistoryOfDeviceComTask')) {
            menu.down('#viewHistoryOfDeviceComTask').show();
        }
        if (menu.down('#activateDeviceComTask')) {
            if (isOnHold && !isSystemComtask) {
                menu.down('#activateDeviceComTask').show();
            } else {
                menu.down('#activateDeviceComTask').hide();
            }
        }
        if (menu.down('#deactivateDeviceComTask')) {
            if (!isOnHold && !isSystemComtask) {
                menu.down('#deactivateDeviceComTask').show();
            } else {
                menu.down('#deactivateDeviceComTask').hide();
            }
        }
        menu.reorderItems();
    },

    showDeviceCommunicationTaskPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getDeviceCommunicationTaskPreview(),
            menu = preview.down('device-communication-task-action-menu');

        Ext.suspendLayouts();
        me.getDeviceCommunicationTaskPreviewForm().loadRecord(record);
        preview.getLayout().setActiveItem(1);
        preview.setTitle(Ext.String.htmlEncode(record.get('comTask').name));
        Ext.resumeLayouts(true);
        if (menu) {
            menu.record = record;
        }
    },

    runDeviceComTask: function () {
        var request = {};
        this.comTask = this.getDeviceCommunicationTaskGrid().getSelectionModel().getSelection()[0];
        this.sendToServer(request, '/api/ddr/devices/' + encodeURIComponent(this.deviceId) + '/comtasks/' + this.comTask.get('comTask').id + '/run', Uni.I18n.translate('deviceCommunicationTask.run', 'MDC', 'Run succeeded'));
    },

    runDeviceComTaskNow: function () {
        var request = {};
        this.comTask = this.getDeviceCommunicationTaskGrid().getSelectionModel().getSelection()[0];
        this.sendToServer(request, '/api/ddr/devices/' + encodeURIComponent(this.deviceId) + '/comtasks/' + this.comTask.get('comTask').id + '/runnow', Uni.I18n.translate('deviceCommunicationTask.runNow', 'MDC', 'Run now succeeded'));
    },

    showChangePopUp: function (menuItem) {
        var me = this;
        var comTask = this.getDeviceCommunicationTaskGrid().getSelectionModel().getSelection()[0];
        switch (menuItem.action) {
            case 'changeConnectionMethodOfDeviceComTask':
                var connectionMethodsOfDeviceStore = me.getConnectionMethodsOfDeviceStore(),
                    connectionFunctionsStore = me.getConnectionFunctionsStore();
                connectionMethodsOfDeviceStore.getProxy().setExtraParam('deviceId', this.deviceId);
                connectionMethodsOfDeviceStore.getProxy().setExtraParam('fullTopology', true); // Which indicates we want to load the connection methods of the full topology
                                                                                               // (the device and - if present - the devices gateway)
                connectionMethodsOfDeviceStore.load({
                    callback: function () {
                        connectionFunctionsStore.getProxy().setExtraParam('connectionFunctionType', 1); // 1 = the consumable connection functions
                        connectionFunctionsStore.load({
                            callback: function (records) {
                                me.addSpecialConnectionMethodsToConnectionMethodsOfDeviceStore(connectionMethodsOfDeviceStore, records);
                                if (!Ext.isEmpty(comTask.get('connectionFunctionInfo'))) {
                                    initialValue = -comTask.get('connectionFunctionInfo').id;
                                } else {
                                    initialValue = me.findConnectionMethodIdBasedOnName(connectionMethodsOfDeviceStore, comTask.get('connectionMethod'));
                                    // Which should be either 0 fo the 'Default (name)' connection method or should be the real id of the connection method
                                }
                                me.showPopUp(menuItem.action, connectionMethodsOfDeviceStore, initialValue, comTask.get('scheduleName'));
                            }
                        });
                    }
                });
                break;
            case 'changeUrgencyOfDeviceComTask':
                me.showPopUp(menuItem.action, null, comTask.get('urgency'), comTask.get('scheduleName'));
                break;
            case 'runDeviceComTask':
                break;
        }
    },

    addSpecialConnectionMethodsToConnectionMethodsOfDeviceStore: function (connectionMethodsOfDeviceStore, records) {
        var defaultConnectionMethod = this.findDefaultConnectionMethod(connectionMethodsOfDeviceStore);
        connectionMethodsOfDeviceStore.add(defaultConnectionMethod);

        for (var i = 0; i < records.length; i++) {
            connectionMethodsOfDeviceStore.add(this.findConnectionMethodWithConnectionFunction(connectionMethodsOfDeviceStore, records[i].getData()));
        }
    },

    findDefaultConnectionMethod: function (connectionMethodsOfDeviceStore) {
        var defaultConnectionMethodName = Uni.I18n.translate('deviceCommunicationTask.notDefinedYet', 'MDC', 'Not defined yet');
        Ext.each(connectionMethodsOfDeviceStore.data.items, function (value) {
            if (value.data.isDefault) {
                defaultConnectionMethodName = value.data.name
            }
        });
        return Ext.create('Mdc.model.DeviceConnectionMethod', {
            id: 0,
            name: Uni.I18n.translate('deviceCommunicationTask.defaultWithCount', 'MDC', 'Default ({0})', defaultConnectionMethodName),
            isDefault: false
        });
    },

    findConnectionMethodWithConnectionFunction: function (connectionMethodsOfDeviceStore, connectionFunction) {
        var connectionMethodName = Uni.I18n.translate('deviceCommunicationTask.notDefinedYet', 'MDC', 'Not defined yet');
        Ext.each(connectionMethodsOfDeviceStore.data.items, function (value) {
            if (!Ext.isEmpty(value.data.connectionFunctionInfo) && (value.data.connectionFunctionInfo['localizedValue'] === connectionFunction['localizedValue'])) {
                connectionMethodName = value.data.name;
            }
        });
        return Ext.create('Mdc.model.DeviceConnectionMethod', {
            id: -connectionFunction['id'], // Negate the connection function id and use it as special connection method id
            name: Uni.I18n.translate(
                'communicationtasks.form.selectKnownPartialConnectionTaskBasedOnConnectionFunction',
                'MDC',
                'Connection method with \'{0}\' function ({1})',
                [connectionFunction['localizedValue'], connectionMethodName]
            ),
            connectionFunctionInfo: connectionFunction
        });
    },

    findConnectionMethodIdBasedOnName: function (connectionMethodsOfDeviceStore, name) {
        var connectionMethod = null;
        connectionMethodsOfDeviceStore.data.items.some(function (value) {
            if (value.data.name.indexOf(name) !== -1) {
                connectionMethod = value.data;
                return true;
            } else {
                return false;
            }
        });
        return !Ext.isEmpty(connectionMethod) ? connectionMethod.id : 0;
    },

    showPopUp: function (action, store, initialValue, scheduleName) {
        var comTask = this.getDeviceCommunicationTaskGrid().getSelectionModel().getSelection()[0];
        this.comTask = comTask.get('comTask');
        var widget = Ext.widget('changeConnectionItemPopUp', {action: action, store: store, init: initialValue, scheduleName: scheduleName, comTaskName: this.comTask.name});
        widget.show();
    },

    changeConnectionMethod: function () {
        var request = {};
        var values = this.getChangeConnectionItemForm().getForm().getValues();
        request.connectionMethod = values.connectionMethodId;
        this.sendToServer(request,
            '/api/ddr/devices/' + encodeURIComponent(this.deviceId) + '/comtasks/' + this.comTask.id + '/connectionmethod',
            Uni.I18n.translate('deviceCommunicationTask.connectionMethodChanged', 'MDC', 'Connection method changed'));
    },

    changeUrgency: function () {
        var request = {};
        var values = this.getChangeConnectionItemForm().getForm().getValues();
        request.urgency = values.urgency;
        this.sendToServer(request,
            '/api/ddr/devices/' + encodeURIComponent(this.deviceId) + '/comtasks/' + this.comTask.id + '/urgency',
            Uni.I18n.translate('deviceCommunicationTask.urgencyChanged', 'MDC', 'Urgency changed'));
    },

    changeFrequency: function () {
        var request = {};
        var values = this.getChangeConnectionItemForm().getForm().getValues();
        request.temporalExpression = values.schedule;
        this.sendToServer(request,
            '/api/ddr/devices/' + encodeURIComponent(this.deviceId) + '/comtasks/' + this.comTask.id + '/frequency',
            Uni.I18n.translate('deviceCommunicationTask.frequencyChanged', 'MDC', 'Frequency changed'));
    },

    activateDeviceComTask: function () {
        var request = {};
        this.comTask = this.getDeviceCommunicationTaskGrid().getSelectionModel().getSelection()[0];
        this.sendToServer(request, '/api/ddr/devices/' + encodeURIComponent(this.deviceId) + '/comtasks/' + this.comTask.get('comTask').id + '/activate', Uni.I18n.translate('device.communication.toggle.activate', 'MDC', 'Communication task activated'));
    },

    deactivateDeviceComTask: function () {
        var request = {};
        this.comTask = this.getDeviceCommunicationTaskGrid().getSelectionModel().getSelection()[0];
        this.sendToServer(request, '/api/ddr/devices/' + encodeURIComponent(this.deviceId) + '/comtasks/' + this.comTask.get('comTask').id + '/deactivate', Uni.I18n.translate('device.communication.toggle.deactivate', 'MDC', 'Communication task deactivated'));
    },

    sendToServer: function (request, actionUrl, actionMsg) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            device = me.getDeviceCommunicationTaskOverview().device;

        Ext.Ajax.request({
            url: actionUrl,
            method: 'PUT',
            params: '',
            isNotEdit: true,
            jsonData: Ext.merge(request, {device: _.pick(device.getRecordData(), 'name', 'version', 'parent')}),
            timeout: 180000,
            success: function (response) {
                var changeConnectionItemPopUp = me.getChangeConnectionItemPopUp();
                if (changeConnectionItemPopUp) {
                    changeConnectionItemPopUp.close();
                }
                me.getApplication().fireEvent('acknowledge', actionMsg);
                router.getRoute().forward();
            },
            failure: function (response) {
                var json = Ext.decode(response.responseText),
                    changeConnectionItemPopUp = me.getChangeConnectionItemPopUp(),
                    form = changeConnectionItemPopUp && changeConnectionItemPopUp.down('form').getForm();

                if (response.status === 400 && json && json.errors && form) {
                    Ext.each(json.errors, function (error) {
                        switch (error.id) {
                            case 'plannedPriority':
                                error.id = 'urgency';
                                break;
                        }
                    });
                    form.markInvalid(json.errors);
                } else if (changeConnectionItemPopUp) {
                    changeConnectionItemPopUp.close();
                }
            }
        });
    },

    showHistory: function (menuItem) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('devices/device/communicationtasks/history').forward(Ext.merge({comTaskId: menuItem.up().record.get('comTask').id}, router.arguments));
    }
});
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
        'ProtocolDialectsOfDevice'
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
                '#changeProtocolDialectOfDeviceComTask[action=changeProtocolDialectOfDeviceComTask]': {
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
                '#changeButton[action=changeProtocolDialectOfDeviceComTask]': {
                    click: this.changeProtocolDialect
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
            communicationTasksOfDeviceStore = me.getCommunicationTasksOfDeviceStore();

        this.deviceId = deviceId;

        viewport.setLoading();
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                var widget = Ext.widget('deviceCommunicationTaskSetup', {device: device});
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getApplication().fireEvent('loadDevice', device);
                viewport.setLoading(false);
                communicationTasksOfDeviceStore.getProxy().setExtraParam('deviceId', deviceId);
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
            connectionDefinedOnDevice = selection.get('connectionDefinedOnDevice'),
            isMinimizeConnections = !connectionDefinedOnDevice ? false : selection.get('connectionStrategyKey') === 'MINIMIZE_CONNECTIONS';

        if(menu.down('#changeConnectionMethodOfDeviceComTask')) {
            menu.down('#changeConnectionMethodOfDeviceComTask').show();
        }
        if(menu.down('#changeProtocolDialectOfDeviceComTask')) {
            if (isNotShared) {
                menu.down('#changeProtocolDialectOfDeviceComTask').show();
            }
        }
        if(menu.down('#changeUrgencyOfDeviceComTask')) {
            menu.down('#changeUrgencyOfDeviceComTask').show();
        }
        if(menu.down('#runDeviceComTaskNow')) {
            if (connectionDefinedOnDevice && !isOnHold) {
                menu.down('#runDeviceComTaskNow').show();
            } else {
                menu.down('#runDeviceComTaskNow').hide();
            }
        }
        if(menu.down('#runDeviceComTask')) {
            if (connectionDefinedOnDevice && !isOnHold && isMinimizeConnections) {
                menu.down('#runDeviceComTask').show();
            } else {
                menu.down('#runDeviceComTask').hide();
            }
        }
        if(menu.down('#viewHistoryOfDeviceComTask')) {
            menu.down('#viewHistoryOfDeviceComTask').show();
        }
        if(menu.down('#activateDeviceComTask')) {
            if (isOnHold) {
                menu.down('#activateDeviceComTask').show();
            } else {
                menu.down('#activateDeviceComTask').hide();
            }
        }
        if(menu.down('#deactivateDeviceComTask')) {
            if (isOnHold) {
                menu.down('#deactivateDeviceComTask').hide();
            } else {
                menu.down('#deactivateDeviceComTask').show();
            }
        }
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
                var connectionMethodsOfDeviceStore = this.getConnectionMethodsOfDeviceStore();
                connectionMethodsOfDeviceStore.getProxy().setExtraParam('deviceId', this.deviceId);
                connectionMethodsOfDeviceStore.load({
                    callback: function () {
                        var nameOfDefaultConnectionMethod = Uni.I18n.translate('deviceCommunicationTask.notDefinedYey', 'MDC', 'Not defined yet');
                        Ext.each(connectionMethodsOfDeviceStore.data.items,function(value){
                            if(value.data.isDefault){
                                nameOfDefaultConnectionMethod = value.data.name;
                            }
                        });
                        connectionMethodsOfDeviceStore.insert(0,Ext.create('Mdc.model.DeviceConnectionMethod', {
                            id: -1,
                            name: Uni.I18n.translate('deviceCommunicationTask.defaultWithCount', 'MDC', 'Default ({0})',[nameOfDefaultConnectionMethod]),
                            isDefault: false
                        }));
                        if(comTask.get('connectionMethod').toLowerCase().indexOf('default')>-1){
                            var initialValue = Uni.I18n.translate('deviceCommunicationTask.defaultWithCount', 'MDC', 'Default ({0})',[nameOfDefaultConnectionMethod]);

                        } else {
                            initialValue = comTask.get('connectionMethod');
                        }
                        me.showPopUp(menuItem.action, connectionMethodsOfDeviceStore, initialValue,comTask.get('scheduleName'));
                    }
                });
                break;
//            case 'changeFrequencyOfDeviceComTask':
//                me.showPopUp(menuItem.action,null,comTask.get('temporalExpression'));
//                break;
            case 'changeProtocolDialectOfDeviceComTask':
                var protocolDialectsOfDeviceStore = this.getProtocolDialectsOfDeviceStore();
                protocolDialectsOfDeviceStore.getProxy().setExtraParam('deviceId', this.deviceId);
                protocolDialectsOfDeviceStore.load({
                    callback: function () {
                        me.showPopUp(menuItem.action, protocolDialectsOfDeviceStore, comTask.get('protocolDialect'));
                    }
                });
                break;
            case 'changeUrgencyOfDeviceComTask':
                me.showPopUp(menuItem.action, null, comTask.get('urgency'),comTask.get('scheduleName'));
                break;
            case 'runDeviceComTask':
                break;
        }
    },

    showPopUp: function (action, store, initialValue, scheduleName) {
        var comTask = this.getDeviceCommunicationTaskGrid().getSelectionModel().getSelection()[0];
        this.comTask = comTask.get('comTask');
        var widget = Ext.widget('changeConnectionItemPopUp', {action: action, store: store, init: initialValue, scheduleName: scheduleName,comTaskName: this.comTask.name});
        // widget.down('#changeConnectionItemForm').loadRecord(comTask);
        widget.show();
    },

    changeConnectionMethod: function () {
        var request = {};
        var values = this.getChangeConnectionItemForm().getForm().getValues();
        request.connectionMethod = values.name;
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

    changeProtocolDialect: function () {
        var request = {};
        var values = this.getChangeConnectionItemForm().getForm().getValues();
        request.protocolDialect = values.name;
        this.sendToServer(request,
            '/api/ddr/devices/' + encodeURIComponent(this.deviceId) + '/comtasks/' + this.comTask.id + '/protocoldialect',
            Uni.I18n.translate('deviceCommunicationTask.protocolDialectChanged', 'MDC', 'Protocol dialect changed'));
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
                if(changeConnectionItemPopUp){
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
                } else if(changeConnectionItemPopUp){
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
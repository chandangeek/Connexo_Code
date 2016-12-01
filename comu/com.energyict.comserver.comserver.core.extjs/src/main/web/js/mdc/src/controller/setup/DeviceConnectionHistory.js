Ext.define('Mdc.controller.setup.DeviceConnectionHistory', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.store.DeviceConnectionHistory'
    ],

    views: [
        'setup.deviceconnectionmethod.DeviceConnectionMethodSetup',
        'setup.deviceconnectionmethod.DeviceConnectionMethodsGrid',
        'setup.deviceconnectionmethod.DeviceConnectionMethodPreview',
        'setup.deviceconnectionmethod.DeviceConnectionMethodEdit',
        'Mdc.view.setup.deviceconnectionhistory.DeviceConnectionHistoryMain',
        'Mdc.view.setup.deviceconnectionhistory.DeviceConnectionHistoryGrid',
        'Mdc.view.setup.deviceconnectionhistory.DeviceConnectionHistoryPreview',
        'Mdc.view.setup.deviceconnectionhistory.DeviceCommunicationTaskExecutionGrid',
        'Mdc.view.setup.deviceconnectionhistory.DeviceCommunicationTaskExecutionPreview',
        'Mdc.view.setup.deviceconnectionhistory.DeviceConnectionLogMain',
        'Mdc.view.setup.deviceconnectionhistory.DeviceConnectionLogGrid',
        'Mdc.view.setup.deviceconnectionhistory.DeviceConnectionLogPreview',
        'Mdc.model.ConnectionLogFilter'
    ],

    stores: [
        'DeviceConnectionHistory',
        'DeviceCommunicationTaskExecutions',
        'DeviceConnectionLog'
    ],

    refs: [
        {ref: 'deviceConnectionHistoryGrid', selector: '#deviceConnectionHistoryGrid'},
        {ref: 'deviceConnectionHistoryPreview', selector: '#deviceConnectionHistoryPreview'},
        {ref: 'deviceConnectionHistoryPreviewForm', selector: '#deviceConnectionHistoryPreviewForm'},
        {ref: 'deviceCommunicationTaskExecutionPreviewForm', selector: '#deviceCommunicationTaskExecutionPreviewForm'},
        {ref: 'deviceCommunicationTaskExecutionGrid', selector: '#deviceCommunicationTaskExecutionGrid'},
        {ref: 'deviceCommunicationTaskExecutionPreview', selector: '#deviceCommunicationTaskExecutionPreview'},
        {
            ref: 'deviceCommunicationTaskExecutionPreviewMenu',
            selector: '#deviceCommunicationTaskExecutionPreview button menu'
        },
        {ref: 'deviceConnectionHistoryPreviewMenu', selector: '#deviceConnectionHistoryPreview button menu'},
        {ref: 'deviceConnectionHistoryGridActionColumn', selector: '#deviceConnectionHistoryGrid #action'},
        {ref: 'titlePanel', selector: '#titlePanel'},
        {ref: 'comPortField', selector: '#comPort'},
        {ref: 'deviceConnectionLogOverviewForm', selector: '#deviceConnectionLogOverviewForm'},
        {ref: 'deviceconnectionhistorySideFilterForm', selector: '#deviceconnectionhistorySideFilterForm'},
        {ref: 'deviceConnectionLogGrid', selector: '#deviceConnectionLogGrid'},
        {ref: 'deviceConnectionLogPreviewForm', selector: '#deviceConnectionLogPreviewForm'},
        {
            ref: 'deviceConnectionLogFilterPanel',
            selector: '#deviceConnectionLogMain #deviceConnectionLogFilterTopPanel'
        },
        {ref: 'statusLink', selector: '#statusLink'}
    ],

    init: function () {
        this.control({
            '#deviceConnectionHistoryGrid': {
                selectionchange: this.previewDeviceConnectionHistory
            },
            '#deviceCommunicationTaskExecutionGrid': {
                selectionchange: this.previewDeviceCommunicationTaskExecution
            },
            '#deviceConnectionLogGrid': {
                selectionchange: this.previewConnectionLog
            },
            'mdc-device-connection-history-grid-action-menu': {
                click: this.showConnectionLog
            },
            'mdc-device-communication-task-grid-action-menu': {
                click: this.showComTaskLog
            }
        });
    },

    showDeviceConnectionMethodHistory: function (deviceId, connectionMethodId) {
        var me = this,
            deviceModel = Ext.ModelManager.getModel('Mdc.model.Device');

        me.deviceId = deviceId;
        me.connectionMethodId = connectionMethodId;
        me.getDeviceCommunicationTaskExecutionsStore().removeAll(true);

        deviceModel.load(deviceId, {
            success: function (device) {
                me.device = device;
                var connectionMethodModel = Ext.ModelManager.getModel('Mdc.model.DeviceConnectionMethod');
                connectionMethodModel.getProxy().setExtraParam('deviceId', deviceId);
                connectionMethodModel.load(connectionMethodId, {
                    success: function (connectionMethod) {
                        var widget = Ext.widget('deviceConnectionHistoryMain', {
                            device: device,
                            deviceId: deviceId,
                            connectionMethodId: connectionMethodId,
                            connectionMethodName: connectionMethod.get('name')
                        });
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.getApplication().fireEvent('loadDevice', device);
                        me.getApplication().fireEvent('loadConnectionMethod', connectionMethod);
                    }
                });
            }
        });
    },

    previewDeviceConnectionHistory: function () {
        var me = this,
            connectionHistory = me.getDeviceConnectionHistoryGrid().getSelectionModel().getSelection()[0],
            deviceCommunicationTaskExecutionsStore = me.getDeviceCommunicationTaskExecutionsStore();

        me.getDeviceConnectionHistoryPreviewForm().loadRecord(connectionHistory);

        me.getStatusLink().setValue('<a href="#/devices/' + this.deviceId
            + '/connectionmethods/'
            + me.connectionMethodId + '/history/' + me.getDeviceConnectionHistoryGrid().getSelectionModel().getSelection()[0].get('id') + '/viewlog'
            + '?logLevels=Error&logLevels=Warning&logLevels=Information&communications=Connections&communications=Communications">'
            + connectionHistory.get('status') + '</a>');

        me.getComPortField().setValue(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.on', 'MDC', '{0} on {1}'), connectionHistory.get('comPort'), '<a href="#/administration/comservers/' + connectionHistory.get('comServer').id + '/overview">' + connectionHistory.get('comServer').name + '</a>'));
        me.getDeviceConnectionHistoryPreview().setTitle(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.on', 'MDC', '{0} on {1}'), connectionHistory.get('connectionMethod').name, me.device.get('name')));
        me.getTitlePanel().setTitle(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.comtasksTitle', 'MDC', 'Communications of {0} connection on {1}'), connectionHistory.get('connectionMethod').name, this.device.get('name')));

        Ext.suspendLayouts();

        deviceCommunicationTaskExecutionsStore.getProxy().setExtraParam('deviceId', this.deviceId);
        deviceCommunicationTaskExecutionsStore.getProxy().setExtraParam('connectionId', this.connectionMethodId);
        deviceCommunicationTaskExecutionsStore.getProxy().setExtraParam('sessionId', connectionHistory.get('id'));

        me.getDeviceCommunicationTaskExecutionGrid().hide();
        me.getDeviceCommunicationTaskExecutionPreview().hide();

        deviceCommunicationTaskExecutionsStore.load({
            callback: function () {
                me.getDeviceCommunicationTaskExecutionGrid().show();
                me.getDeviceCommunicationTaskExecutionPreview().show();
            }
        });
        Ext.resumeLayouts();
    },

    previewDeviceCommunicationTaskExecution: function () {
        var me = this,
            communication = me.getDeviceCommunicationTaskExecutionGrid().getSelectionModel().getSelection()[0],
            deviceCommunicationTaskExecutionPreviewMenu = me.getDeviceCommunicationTaskExecutionPreviewMenu();
        me.getDeviceCommunicationTaskExecutionPreviewForm().loadRecord(communication);
        deviceCommunicationTaskExecutionPreviewMenu.record = communication;
        me.getDeviceCommunicationTaskExecutionPreview().setTitle(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.on', 'MDC', '{0} on {1}'), communication.get('name'), me.device.get('name')));
    },

    showConnectionLog: function () {
        location.href = '#/devices/' + this.deviceId + '/connectionmethods/' + this.connectionMethodId + '/history/' + this.getDeviceConnectionHistoryGrid().getSelectionModel().getSelection()[0].get('id') + '/viewlog' +
        '?logLevels=Error&logLevels=Warning&logLevels=Information&communications=Connections&communications=Communications'
    },

    showDeviceConnectionMethodHistoryLog: function (deviceId, deviceConnectionMethodId, deviceConnectionHistoryId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            deviceModel = Ext.ModelManager.getModel('Mdc.model.Device'),
            connectionMethodModel = Ext.ModelManager.getModel('Mdc.model.DeviceConnectionMethod'),
            comSessionHistory = Ext.ModelManager.getModel('Mdc.model.DeviceConnectionHistory'),
            store = me.getDeviceConnectionLogStore(),
            widget;

        deviceModel.load(deviceId, {
            success: function (device) {
                connectionMethodModel.getProxy().setExtraParam('deviceId', deviceId);
                connectionMethodModel.load(deviceConnectionMethodId, {
                    success: function (connectionMethod) {
                        comSessionHistory.getProxy().setExtraParam('deviceId', deviceId);
                        comSessionHistory.getProxy().setExtraParam('connectionId', deviceConnectionMethodId);
                        comSessionHistory.load(deviceConnectionHistoryId, {
                            success: function (deviceConnectionHistory) {
                                store.getProxy().setExtraParam('deviceId', deviceId);
                                store.getProxy().setExtraParam('connectionId', deviceConnectionMethodId);
                                store.getProxy().setExtraParam('sessionId', deviceConnectionHistoryId);

                                widget = Ext.widget('deviceConnectionLogMain', {device: device, deviceId: deviceId});
                                me.getApplication().fireEvent('changecontentevent', widget);

                                store.load(function () {
                                    me.getApplication().fireEvent('loadDevice', device);
                                    me.getApplication().fireEvent('loadConnectionMethod', connectionMethod);
                                    me.getDeviceConnectionLogOverviewForm().loadRecord(deviceConnectionHistory);
                                });
                            }
                        });
                    }
                });
            }
        });
    },

    showComTaskLog: function (menu) {
        var record = menu.record;
        location.href = '#/devices/' + record.get('device').name
            + '/communicationtasks/' + record.get('comTasks')[0].id
            + '/history/' + record.get('id')
            + '/viewlog'
            + '?logLevels=Error&logLevels=Warning&logLevels=Information';
    },

    previewConnectionLog: function () {
        var connectionLog = this.getDeviceConnectionLogGrid().getSelectionModel().getSelection()[0],
            preview = this.getDeviceConnectionLogPreviewForm();
        preview.loadRecord(connectionLog);
        preview.up('#deviceConnectionLogPreview').setTitle(Uni.DateTime.formatDateTimeLong(connectionLog.get('timestamp')));
    }
});
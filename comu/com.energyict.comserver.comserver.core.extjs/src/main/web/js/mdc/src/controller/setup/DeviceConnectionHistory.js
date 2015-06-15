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
        {
            ref: 'deviceCommunicationTaskExecutionGridActionColumn',
            selector: '#deviceCommunicationTaskExecutionGrid #action'
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
            '#actionColumn menuitem': {
                click: this.showConnectionLog
            },
            '#deviceConnectionHistoryPreview menuitem': {
                click: this.showConnectionLog
            },
            '#deviceConnectionLogGrid': {
                selectionchange: this.previewConnectionLog
            }
        });
    },

    showDeviceConnectionMethodHistory: function (deviceMrId, connectionMethodId) {
        var me = this,
            deviceModel = Ext.ModelManager.getModel('Mdc.model.Device');

        me.deviceMrId = deviceMrId;
        me.connectionMethodId = connectionMethodId;
        me.getDeviceCommunicationTaskExecutionsStore().removeAll(true);

        deviceModel.load(deviceMrId, {
            success: function (device) {
                me.device = device;
                var connectionMethodModel = Ext.ModelManager.getModel('Mdc.model.DeviceConnectionMethod');
                connectionMethodModel.getProxy().setExtraParam('mrid', encodeURIComponent(deviceMrId));
                connectionMethodModel.load(connectionMethodId, {
                    success: function (connectionMethod) {
                        var widget = Ext.widget('deviceConnectionHistoryMain', {
                            device: device,
                            mrid: deviceMrId,
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
            deviceConnectionHistoryPreviewMenu = me.getDeviceConnectionHistoryPreviewMenu(),
            deviceCommunicationTaskExecutionsStore = me.getDeviceCommunicationTaskExecutionsStore(),
            deviceConnectionHistoryGridActionColumn = me.getDeviceConnectionHistoryGridActionColumn();

        me.getDeviceConnectionHistoryPreviewForm().loadRecord(connectionHistory);
        
        me.getStatusLink().setValue('<a href="#/devices/' + this.deviceMrId + '/connectionmethods/' + this.connectionMethodId + '/history/' + this.getDeviceConnectionHistoryGrid().getSelectionModel().getSelection()[0].get('id') + '/viewlog' + '?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22logTypes%22%3A%5B%22Connections%22%2C%22Communications%22%5D%7D">' +
        connectionHistory.get('status') + '</a>');

        me.getComPortField().setValue(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.on', 'MDC', '{0} on {1}'), connectionHistory.get('comPort'), '<a href="#/administration/comservers/' + connectionHistory.get('comServer').id + '/overview">' + connectionHistory.get('comServer').name + '</a>'));
        me.getDeviceConnectionHistoryPreview().setTitle(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.on', 'MDC', '{0} on {1}'), connectionHistory.get('connectionMethod').name, me.device.get('mRID')));
        me.getTitlePanel().setTitle(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.comtasksTitle', 'MDC', 'Communications of {0} connection on {1}'), connectionHistory.get('connectionMethod').name, this.device.get('mRID')));

        deviceConnectionHistoryPreviewMenu.removeAll();

        deviceConnectionHistoryGridActionColumn.menu.removeAll();

        var menuItem = {
            text: Uni.I18n.translate('deviceconnectionhistory.viewLog', 'MDC', 'View connection log'),
            action: 'viewLog',
            listeners: {
                click: function () {
                    me.showConnectionLog();
                }
            }
        };

        Ext.suspendLayouts();

        deviceConnectionHistoryPreviewMenu.add(menuItem);
        deviceConnectionHistoryGridActionColumn.menu.add(menuItem);
        deviceCommunicationTaskExecutionsStore.getProxy().setExtraParam('mRID', encodeURIComponent(this.deviceMrId));
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
        var me = this;
        var communication = this.getDeviceCommunicationTaskExecutionGrid().getSelectionModel().getSelection()[0];
        var menuItems = [];
        var deviceCommunicationTaskExecutionPreviewMenu = me.getDeviceCommunicationTaskExecutionPreviewMenu();
        deviceCommunicationTaskExecutionPreviewMenu.removeAll();
        var deviceCommunicationTaskExecutionGridActionColumn = me.getDeviceCommunicationTaskExecutionGridActionColumn();
        deviceCommunicationTaskExecutionGridActionColumn.menu.removeAll();

        Ext.each(communication.get('comTasks'), function (item) {
            menuItems.push({
                text: Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.viewComTaskLog', 'MDC', 'View \'{0}\' log'), item.name),
                action: {
                    action: 'viewlog',
                    comTask: {
                        mRID: me.deviceMrId,
                        sessionId: communication.get('id'),
                        comTaskId: item.id
                    }
                },
                listeners: {
                    click: me.showComTaskLog
                }
            });
        });

        deviceCommunicationTaskExecutionGridActionColumn.menu.add(menuItems);
        deviceCommunicationTaskExecutionPreviewMenu.add(menuItems);

        me.getDeviceCommunicationTaskExecutionPreviewForm().loadRecord(communication);
        me.getDeviceCommunicationTaskExecutionPreview().setTitle(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.on', 'MDC', '{0} on {1}'), communication.get('name'), me.device.get('mRID')));
    },

    showConnectionLog: function () {
        location.href = '#/devices/' + this.deviceMrId + '/connectionmethods/' + this.connectionMethodId + '/history/' + this.getDeviceConnectionHistoryGrid().getSelectionModel().getSelection()[0].get('id') + '/viewlog' +
        '?logLevels=Error&logLevels=Warning&logLevels=Information&communications=Connections&communications=Communications'
    },

    showDeviceConnectionMethodHistoryLog: function (deviceMrId, deviceConnectionMethodId, deviceConnectionHistoryId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            deviceModel = Ext.ModelManager.getModel('Mdc.model.Device'),
            connectionMethodModel = Ext.ModelManager.getModel('Mdc.model.DeviceConnectionMethod'),
            comSessionHistory = Ext.ModelManager.getModel('Mdc.model.DeviceConnectionHistory'),
            store = me.getDeviceConnectionLogStore(),
            widget;

        deviceModel.load(deviceMrId, {
            success: function (device) {
                connectionMethodModel.getProxy().setExtraParam('mrid', encodeURIComponent(deviceMrId));
                connectionMethodModel.load(deviceConnectionMethodId, {
                    success: function (connectionMethod) {
                        comSessionHistory.getProxy().setExtraParam('mRID', encodeURIComponent(deviceMrId));
                        comSessionHistory.getProxy().setExtraParam('connectionId', deviceConnectionMethodId);
                        comSessionHistory.load(deviceConnectionHistoryId, {
                            success: function (deviceConnectionHistory) {
                                store.getProxy().setExtraParam('mRID', encodeURIComponent(deviceMrId));
                                store.getProxy().setExtraParam('connectionId', deviceConnectionMethodId);
                                store.getProxy().setExtraParam('sessionId', deviceConnectionHistoryId);

                                widget = Ext.widget('deviceConnectionLogMain', {device: device, mrid: deviceMrId});
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

    showComTaskLog: function (item) {
        location.href = '#/devices/' + item.action.comTask.mRID
        + '/communicationtasks/' + item.action.comTask.comTaskId
        + '/history/' + item.action.comTask.sessionId
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
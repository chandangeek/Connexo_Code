Ext.define('Mdc.controller.setup.DeviceCommunicationTaskHistory', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.store.DeviceCommunicationTaskHistory',
        'Mdc.model.CommunicationTask',
        'Mdc.model.DeviceComTaskLogFilter'
    ],
    views: [
        'Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryMain',
        'Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryGrid',
        'Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryPreview',
        'Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryActionMenu',
        'Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryLogMain',
        'Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryLogGrid',
        'Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryLogPreview'
    ],

    stores: [
        'DeviceCommunicationTaskHistory',
        'DeviceCommunicationTaskLog'
    ],

    refs: [
        {ref: 'deviceCommunicationTaskHistoryGrid', selector: '#deviceCommunicationTaskHistoryGrid'},
        {ref: 'deviceCommunicationTaskHistoryPreviewForm', selector: '#deviceCommunicationTaskHistoryPreviewForm'},
        {ref: 'deviceConnectionHistoryPreviewForm', selector: '#deviceConnectionHistoryPreviewForm'},
        {ref: 'deviceConnectionHistoryPreviewPanel', selector: '#deviceConnectionHistoryPreviewPanel'},
        {
            ref: 'DeviceCommunicationTaskHistoryLogPreviewForm',
            selector: '#DeviceCommunicationTaskHistoryLogPreviewForm'
        },
        {ref: 'deviceCommunicationTaskHistoryLogGrid', selector: '#deviceCommunicationTaskHistoryLogGrid'},
        {ref: 'deviceCommunicationTaskHistoryLogPreview', selector: '#deviceCommunicationTaskHistoryLogPreview'},
        {ref: 'deviceCommunicationTaskLogOverviewForm', selector: '#deviceCommunicationTaskLogOverviewForm'},
        {ref: 'comPortField', selector: '#comPort'}
    ],

    init: function () {
        this.control({
            '#deviceCommunicationTaskHistoryGrid': {
                selectionchange: this.previewDeviceCommunicationTaskHistory
            },
            '#viewCommunicationLog[action=viewCommunicationLog]': {
                click: this.viewCommunicationLog
            },
            '#viewConnectionLog[action=viewConnectionLog]': {
                click: this.viewConnectionLog
            },
            '#deviceCommunicationTaskHistoryLogGrid': {
                selectionchange: this.previewDeviceCommunicationTaskHistoryLog
            }
        });
    },

    showDeviceCommunicationTaskHistory: function (deviceId, comTaskId) {
        var me = this;
        this.deviceId = deviceId;
        this.comTaskId = comTaskId;
        var comTaskModel = Ext.ModelManager.getModel('Mdc.model.CommunicationTask');
        var deviceModel = Ext.ModelManager.getModel('Mdc.model.Device');
        deviceModel.load(deviceId, {
            success: function (device) {
                comTaskModel.load(comTaskId, {
                    success: function (comTask) {
                        var widget = Ext.widget('deviceCommunicationTaskHistoryMain', {
                            device: device,
                            deviceId: deviceId,
                            comTaskId: comTaskId,
                            comTaskName: comTask.get('name')
                        });
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.getApplication().fireEvent('loadDevice', device);
                        me.getApplication().fireEvent('loadCommunicationTask', comTask);
                    }
                });
            }
        });
    },

    previewDeviceCommunicationTaskHistory: function () {
        var me = this,
            communicationTaskHistory = me.getDeviceCommunicationTaskHistoryGrid().getSelectionModel().getSelection()[0];

        Ext.suspendLayouts();
        me.getDeviceCommunicationTaskHistoryPreviewForm().loadRecord(communicationTaskHistory);
        me.getComPortField().setValue(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.on', 'MDC', '{0} on {1}'), communicationTaskHistory.get('comSession').comPort, '<a href="#/administration/comservers/' + communicationTaskHistory.get('comSession').comServer.id + '/overview">' + communicationTaskHistory.get('comSession').comServer.name + '</a>'));
        me.getDeviceConnectionHistoryPreviewForm().loadRecord(communicationTaskHistory.getComSession());
        me.getDeviceConnectionHistoryPreviewPanel().setTitle(Uni.I18n.translate('devicecommunicationtaskhistory.connectionPreviewTitle', 'MDC', '{0} on {1}', [communicationTaskHistory.getComSession().get('connectionMethod').name, communicationTaskHistory.getComSession().get('device').name]));
        Ext.resumeLayouts(true);
    },

    viewCommunicationLog: function () {
        var communicationTaskHistory = this.getDeviceCommunicationTaskHistoryGrid().getSelectionModel().getSelection()[0];
        location.href = '#/devices/' + communicationTaskHistory.get('device').id
        + '/communicationtasks/' + communicationTaskHistory.get('comTasks')[0].id
        + '/history/' + communicationTaskHistory.get('id')
        + '/viewlog' +
        '?logLevels=Error&logLevels=Warning&logLevels=Information';

    },

    viewConnectionLog: function () {
        var communicationTaskHistory = this.getDeviceCommunicationTaskHistoryGrid().getSelectionModel().getSelection()[0];
        location.href = '#/devices/' + communicationTaskHistory.getComSession().get('device').id
        + '/connectionmethods/' + communicationTaskHistory.getComSession().get('connectionMethod').id
        + '/history/' + communicationTaskHistory.getComSession().get('id')
        + '/viewlog'
        + '?logLevels=Error&logLevels=Warning&logLevels=Information&communications=Connections&communications=Communications'
    },

    showDeviceCommunicationTaskHistoryLog: function (deviceId, comTaskId, comTaskHistoryId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            comTaskModel = Ext.ModelManager.getModel('Mdc.model.CommunicationTask'),
            deviceModel = Ext.ModelManager.getModel('Mdc.model.Device'),
            comTaskHistoryModel = Ext.ModelManager.getModel('Mdc.model.DeviceCommunicationTaskHistory');

        viewport.setLoading();

        comTaskHistoryModel.getProxy().setExtraParam('deviceId', deviceId);
        comTaskHistoryModel.getProxy().setExtraParam('comTaskId', comTaskId);

        deviceModel.load(deviceId, {
            success: function (device) {
                var widget = Ext.widget('deviceCommunicationTaskHistoryLogMain', {device: device, deviceId: deviceId});
                me.getApplication().fireEvent('changecontentevent', widget);

                comTaskModel.load(comTaskId, {
                    success: function (comTask) {
                        me.getApplication().fireEvent('loadDevice', device);
                        me.getApplication().fireEvent('loadCommunicationTask', comTask);
                    }
                });

                comTaskHistoryModel.load(comTaskHistoryId, {
                    success: function (comTaskHistory) {
                        var comTaskModel = Ext.ModelManager.getModel('Mdc.model.CommunicationTask');
                        comTaskModel.load(comTaskId, {
                            success: function (comTask) {
                                me.getDeviceCommunicationTaskLogOverviewForm().loadRecord(comTaskHistory);
                                var store = me.getDeviceCommunicationTaskLogStore();
                                store.getProxy().setExtraParam('deviceId', deviceId);
                                store.getProxy().setExtraParam('comTaskId', comTaskId);
                                store.getProxy().setExtraParam('sessionId', comTaskHistoryId);

                                viewport.setLoading(false);
                                store.load();
                            }
                        });
                    }
                });
            }
        });
    },

    previewDeviceCommunicationTaskHistoryLog: function () {
        var comTaskLog = this.getDeviceCommunicationTaskHistoryLogGrid().getSelectionModel().getSelection()[0];
        this.getDeviceCommunicationTaskHistoryLogPreviewForm().loadRecord(comTaskLog);
        var timeStamp = Uni.DateTime.formatDateTimeLong(new Date(comTaskLog.get('timestamp')));
        this.getDeviceCommunicationTaskHistoryLogPreview().setTitle(Ext.String.format(Uni.I18n.translate('devicecommunicationtaskhistory.communicationOn', 'MDC', 'Communication on {0}'), timeStamp));
    }
});

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        {ref: 'deviceCommunicationTaskHistoryPreview', selector: '#deviceCommunicationTaskHistoryPreview'},
        {ref: 'deviceConnectionHistoryPreviewForm', selector: '#deviceConnectionHistoryPreviewForm'},
        {ref: 'deviceConnectionHistoryPreviewPanel', selector: '#deviceConnectionHistoryPreviewPanel'},
        {
            ref: 'DeviceCommunicationTaskHistoryLogPreviewForm',
            selector: '#DeviceCommunicationTaskHistoryLogPreviewForm'
        },
        {ref: 'deviceCommunicationTaskHistoryLogGrid', selector: '#deviceCommunicationTaskHistoryLogGrid'},
        {ref: 'deviceCommunicationTaskHistoryLogPreview', selector: '#deviceCommunicationTaskHistoryLogPreview'},
        {ref: 'deviceCommunicationTaskLogOverviewForm', selector: '#deviceCommunicationTaskLogOverviewForm'},
        {ref: 'comPortField', selector: '#comPort'},
        {ref: 'showComTaskConnectionDetails', selector: '#btn-com-task-show-connection-details'},
        {ref: 'showComTaskCommunicationDetails', selector: '#btn-com-task-show-communication-details'},
        {ref: 'comTaskConnectionDetails', selector: '#deviceConnectionHistoryPreviewForm'},
        {ref: 'comTaskCommunicationDetails', selector: '#deviceCommunicationTaskHistoryPreviewForm'},
        {ref: 'comTaskConnectionSummary', selector: '#com-task-connection-summary'},
        {ref: 'comTaskCommunicationSummary', selector: '#com-task-communication-summary'}
    ],

    init: function () {
        this.control({
            '#deviceCommunicationTaskHistoryGrid': {
                select: this.previewDeviceCommunicationTaskHistory
            },
            '#viewCommunicationLog[action=viewCommunicationLog]': {
                click: this.viewCommunicationLog
            },
            '#viewConnectionLog[action=viewConnectionLog]': {
                click: this.viewConnectionLog
            },
            '#deviceCommunicationTaskHistoryLogGrid': {
                selectionchange: this.previewDeviceCommunicationTaskHistoryLog
            },
            'button[action=showComTaskConnectionDetails]': {
                click: this.showComTaskConnectionDetails
            },
            'button[action=showComTaskCommunicationDetails]': {
                click: this.showComTaskCommunicationDetails
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

    previewDeviceCommunicationTaskHistory: function (selectionModel, record) {
        var me = this,
            communicationPreview = me.getDeviceCommunicationTaskHistoryPreview(),
            communicationMenu = communicationPreview.down('menu'),
            connectionPreview = me.getDeviceConnectionHistoryPreviewPanel(),
            connectionMenu = connectionPreview.down('menu'),
            comTaskConnectionSummary = me.getComTaskConnectionSummary(),
            comTaskCommunicationSummary = me.getComTaskCommunicationSummary(),
            errorListConnection = [],
            errorListCommunication = [],
            communicationTaskHistory = me.getDeviceCommunicationTaskHistoryGrid().getSelectionModel().getSelection()[0];

        Ext.suspendLayouts();
        me.getDeviceCommunicationTaskHistoryPreviewForm().loadRecord(communicationTaskHistory);
        me.getComPortField().setValue(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.on', 'MDC', '{0} on {1}'), communicationTaskHistory.get('comSession').comPort, '<a href="#/administration/comservers/' + communicationTaskHistory.get('comSession').comServer.id + '">' + communicationTaskHistory.get('comSession').comServer.name + '</a>'));
        me.getDeviceConnectionHistoryPreviewForm().loadRecord(communicationTaskHistory.getComSession());
        connectionPreview.setTitle(Uni.I18n.translate('devicecommunicationtaskhistory.connectionPreviewTitle', 'MDC', '{0} on {1}', [communicationTaskHistory.getComSession().get('connectionMethod').name, communicationTaskHistory.getComSession().get('device').name]));

        if (communicationTaskHistory.get('comSession').errors && communicationTaskHistory.get('comSession').errors.length > 0) {
            errorListConnection.push((Uni.I18n.translate('deviceconnectionhistory.errorsTitle', 'MDC', 'Errors:')));
            Ext.Array.forEach(communicationTaskHistory.get('comSession').errors, function (error){
                errorListConnection.push(Uni.DateTime.formatDateTime(error.timestamp, Uni.DateTime.SHORT, Uni.DateTime.LONGWITHMILLIS) + ' - ' + error.details);
            });
            errorListConnection.push(' '); //new empty line
        }

        if (communicationTaskHistory.get('comSession').warnings && communicationTaskHistory.get('comSession').warnings.length > 0) {
            errorListCommunication.push((Uni.I18n.translate('deviceconnectionhistory.warningsTitle', 'MDC', 'Warnings:')));
            Ext.Array.forEach(communicationTaskHistory.get('comSession').warnings, function (warn){
                errorListCommunication.push(Uni.DateTime.formatDateTime(warn.timestamp, Uni.DateTime.SHORT, Uni.DateTime.LONGWITHMILLIS) + ' - ' + warn.details);
            });
            errorListCommunication.push(' '); //new empty line
        }

        if (communicationTaskHistory.get('errors') && communicationTaskHistory.get('errors').length > 0) {
            errorListCommunication.push((Uni.I18n.translate('deviceconnectionhistory.errorsTitle', 'MDC', 'Errors:')));
            Ext.Array.forEach(communicationTaskHistory.get('errors'), function (error){
                errorListCommunication.push(Uni.DateTime.formatDateTime(error.timestamp, Uni.DateTime.SHORT, Uni.DateTime.LONGWITHMILLIS) + ' - ' + error.details);
            });
            errorListCommunication.push(' '); //new empty line
        }

        if (communicationTaskHistory.get('warnings') && communicationTaskHistory.get('warnings').length > 0) {
            errorListCommunication.push((Uni.I18n.translate('deviceconnectionhistory.warningsTitle', 'MDC', 'Warnings:')));
            Ext.Array.forEach(communicationTaskHistory.get('warnings'), function (warn){
                errorListCommunication.push(Uni.DateTime.formatDateTime(warn.timestamp, Uni.DateTime.SHORT, Uni.DateTime.LONGWITHMILLIS) + ' - ' + warn.details);
            });
            errorListCommunication.push(' '); //new empty line
        }

        me.getComTaskConnectionDetails().setVisible(!errorListConnection.length > 0);
        me.getShowComTaskConnectionDetails().setVisible(errorListConnection.length > 0);
        comTaskConnectionSummary.setVisible(errorListConnection.length > 0);
        comTaskConnectionSummary.setValue(errorListConnection.join('<br/>'));

        me.getComTaskCommunicationDetails().setVisible(!errorListCommunication.length > 0);
        me.getShowComTaskCommunicationDetails().setVisible(errorListCommunication.length > 0);
        comTaskCommunicationSummary.setVisible(errorListCommunication.length > 0);
        comTaskCommunicationSummary.setValue(errorListCommunication.join('<br/>'));

        Ext.resumeLayouts(true);
        if (communicationMenu) {
            communicationMenu.record = record;
        }
        if (connectionMenu) {
            connectionMenu.record = record;
        }
    },

    viewCommunicationLog: function (menuItem) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('devices/device/communicationtasks/history/viewlog').forward(
            Ext.merge({historyId: menuItem.up().record.getId()}, router.arguments),
            {logLevels: ['Error', 'Warning', 'Information']}
        );
    },

    viewConnectionLog: function (menuItem) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            comSession = menuItem.up().record.getComSession();

        router.getRoute('devices/device/connectionmethods/history/viewlog').forward(
            Ext.merge({connectionMethodId: comSession.get('connectionMethod').id, historyId: comSession.getId()}, router.arguments),
            {logLevels: ['Error', 'Warning', 'Information'], logTypes: ['Connections', 'Communications']}
        );
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
    },

    showComTaskConnectionDetails: function(){
        var me = this;

        me.getShowComTaskConnectionDetails().setVisible(false);
        me.getComTaskConnectionDetails().setVisible(true);
    },

    showComTaskCommunicationDetails: function(){
        var me = this;

        me.getShowComTaskCommunicationDetails().setVisible(false);
        me.getComTaskCommunicationDetails().setVisible(true);
    }
});

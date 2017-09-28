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
        {ref: 'hideComTaskConnectionDetails', selector: '#btn-com-task-hide-connection-details'},
        {ref: 'showComTaskCommunicationDetails', selector: '#btn-com-task-show-communication-details'},
        {ref: 'hideComTaskCommunicationDetails', selector: '#btn-com-task-hide-communication-details'},
        {ref: 'comTaskConnectionDetails', selector: '#deviceConnectionHistoryPreviewForm'},
        {ref: 'comTaskCommunicationDetails', selector: '#deviceCommunicationTaskHistoryPreviewForm'},
        {ref: 'comTaskConnectionSummary', selector: '#com-task-connection-summary'},
        {ref: 'comTaskCommunicationSummary', selector: '#com-task-communication-summary'},
        {ref: 'deviceCommunicationTaskHistoryPreviewPanel', selector: '#deviceCommunicationTaskHistoryPreviewPanel'}
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
            'button[action=hideComTaskConnectionDetails]': {
                click: this.hideComTaskConnectionDetails
            },
            'button[action=showComTaskCommunicationDetails]': {
                click: this.showComTaskCommunicationDetails
            },
            'button[action=hideComTaskCommunicationDetails]': {
                click: this.hideComTaskCommunicationDetails
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
            communicationTaskHistory = record,
            comTaskPreview = me.getDeviceCommunicationTaskHistoryPreviewPanel();

        Ext.suspendLayouts();
        me.getDeviceCommunicationTaskHistoryPreviewForm().loadRecord(communicationTaskHistory);
        me.getComPortField().setValue(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.on', 'MDC', '{0} on {1}'), communicationTaskHistory.get('comSession').comPort, '<a href="#/administration/comservers/' + communicationTaskHistory.get('comSession').comServer.id + '">' + communicationTaskHistory.get('comSession').comServer.name + '</a>'));
        me.getDeviceConnectionHistoryPreviewForm().loadRecord(communicationTaskHistory.getComSession());
        connectionPreview.setTitle(Uni.I18n.translate('devicecommunicationtaskhistory.connectionPreviewTitle', 'MDC', '{0} on {1}', [communicationTaskHistory.getComSession().get('connectionMethod').name, communicationTaskHistory.getComSession().get('device').name]));
        comTaskPreview.setTitle(Uni.DateTime.formatDateTime(communicationTaskHistory.get('startTime'), Uni.DateTime.SHORT, Uni.DateTime.LONG));

        if (communicationTaskHistory.get('comSession').errors && communicationTaskHistory.get('comSession').errors.length > 0) {
            errorListConnection.push((Uni.I18n.translate('deviceconnectionhistory.errorsTitle', 'MDC', 'Errors:')));
            Ext.Array.forEach(communicationTaskHistory.get('comSession').errors, function (error){
                errorListConnection.push(Uni.DateTime.formatDateTime(error.timestamp, Uni.DateTime.SHORT, Uni.DateTime.LONGWITHMILLIS) + ' - ' + error.details);
            });
            errorListConnection.push(' '); //new empty line
        }

        if (communicationTaskHistory.get('comSession').warnings && communicationTaskHistory.get('comSession').warnings.length > 0) {
            errorListConnection.push((Uni.I18n.translate('deviceconnectionhistory.warningsTitle', 'MDC', 'Warnings:')));
            Ext.Array.forEach(communicationTaskHistory.get('comSession').warnings, function (warn){
                errorListConnection.push(Uni.DateTime.formatDateTime(warn.timestamp, Uni.DateTime.SHORT, Uni.DateTime.LONGWITHMILLIS) + ' - ' + warn.details);
            });
            errorListConnection.push(' '); //new empty line
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

        me.getComTaskConnectionDetails().setVisible(false);
        me.getShowComTaskConnectionDetails().setVisible(true);
        me.getHideComTaskConnectionDetails().setVisible(false);
        if(errorListConnection.length > 0){
            comTaskConnectionSummary.setValue(errorListConnection.join('<br/>'));
        }else{
            comTaskConnectionSummary.setValue(Uni.I18n.translate('deviceconnectionhistory.noErrorsOrWarnings', 'MDC', 'No errors or warnings.'));
        }

        me.getComTaskCommunicationDetails().setVisible(false);
        me.getShowComTaskCommunicationDetails().setVisible(true);
        me.getHideComTaskCommunicationDetails().setVisible(false);
        if(errorListCommunication.length > 0){
            comTaskCommunicationSummary.setValue(errorListCommunication.join('<br/>'));
        }else{
            comTaskCommunicationSummary.setValue(Uni.I18n.translate('devicecommunicationhistory.noErrorsOrWarnings', 'MDC', 'No errors or warnings.'));
        }

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
            {logLevels: ['Debug']}
        );
    },

    viewConnectionLog: function (menuItem) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            comSession = menuItem.up().record.getComSession();
        
        router.getRoute('devices/device/connectionmethods/history/viewlog').forward(
            Ext.merge({connectionMethodId: comSession.get('connectionMethod').id, historyId: comSession.getId(), deviceId: comSession.data.device.name, comTaskId: router.arguments.comTaskId}),
            {logLevels: ['Debug'], logTypes: ['Connections', 'Communications']}
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
        this.getDeviceCommunicationTaskHistoryLogPreview().setTitle(Ext.String.format(Uni.DateTime.formatDateTime(comTaskLog.get('timestamp'), Uni.DateTime.SHORT, Uni.DateTime.LONGWITHMILLIS)));
    },

    showComTaskConnectionDetails: function(){
        var me = this;

        me.getShowComTaskConnectionDetails().setVisible(false);
        me.getHideComTaskConnectionDetails().setVisible(true);
        me.getComTaskConnectionDetails().setVisible(true);
    },

    hideComTaskConnectionDetails: function(){
        var me = this;

        me.getShowComTaskConnectionDetails().setVisible(true);
        me.getHideComTaskConnectionDetails().setVisible(false);
        me.getComTaskConnectionDetails().setVisible(false);
    },

    showComTaskCommunicationDetails: function(){
        var me = this;

        me.getShowComTaskCommunicationDetails().setVisible(false);
        me.getHideComTaskCommunicationDetails().setVisible(true);
        me.getComTaskCommunicationDetails().setVisible(true);
    },

    hideComTaskCommunicationDetails: function(){
        var me = this;

        me.getShowComTaskCommunicationDetails().setVisible(true);
        me.getHideComTaskCommunicationDetails().setVisible(false);
        me.getComTaskCommunicationDetails().setVisible(false);
    }
});

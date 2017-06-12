/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        {ref: 'rightConnectionDetails', selector: '#right-connection-details'},
        {ref: 'leftConnectionDetails', selector: '#left-connection-details'},
        {ref: 'leftCommunicationDetails', selector: '#left-communication-details'},
        {ref: 'rightCommunicationDetails', selector: '#right-communication-details'},
        {ref: 'showConnectionDetailsButton', selector: '#btn-show-connection-details'},
        {ref: 'hideConnectionDetailsButton', selector: '#btn-hide-connection-details'},
        {ref: 'showCommunicationDetailsButton', selector: '#btn-show-communication-details'},
        {ref: 'hideCommunicationDetailsButton', selector: '#btn-hide-communication-details'},
        {ref: 'connectionSummaryField', selector: '#connection-summary'},
        {ref: 'communicationSummaryField', selector: '#communication-summary'},
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
            },
            'button[action=showConnectionDetails]': {
                click: this.showConnectionDetails
            },
            'button[action=hideConnectionDetails]': {
                click: this.hideConnectionDetails
            },
            'button[action=showCommunicationDetails]': {
                click: this.showCommunicationDetails
            },
            'button[action=hideCommunicationDetails]': {
                click: this.hideCommunicationDetails
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
            connectionSummary = me.getConnectionSummaryField(),
            errorList = [],
            deviceCommunicationTaskExecutionsStore = me.getDeviceCommunicationTaskExecutionsStore();

        me.getDeviceConnectionHistoryPreviewForm().loadRecord(connectionHistory);

        if (connectionHistory.get('errors') && connectionHistory.get('errors').length > 0) {
            errorList.push((Uni.I18n.translate('deviceconnectionhistory.errorsTitle', 'MDC', 'Errors:')));
            Ext.Array.forEach(connectionHistory.get('errors'), function (error){
                errorList.push(Uni.DateTime.formatDateTime(error.timestamp, Uni.DateTime.SHORT, Uni.DateTime.LONGWITHMILLIS) + ' - ' + error.details);
            });
            errorList.push(' '); //new empty line
        }

        if (connectionHistory.get('warnings') && connectionHistory.get('warnings').length > 0) {
            errorList.push((Uni.I18n.translate('deviceconnectionhistory.warningsTitle', 'MDC', 'Warnings:')));
            Ext.Array.forEach(connectionHistory.get('warnings'), function (warn){
                errorList.push(Uni.DateTime.formatDateTime(warn.timestamp, Uni.DateTime.SHORT, Uni.DateTime.LONGWITHMILLIS) + ' - ' + warn.details);
            });
            errorList.push(' '); //new empty line
        }

        me.getRightConnectionDetails().setVisible(false);
        me.getLeftConnectionDetails().setVisible(false);
        me.getShowConnectionDetailsButton().setVisible(true);
        me.getHideConnectionDetailsButton().setVisible(false);
        if(errorList.length > 0){
            connectionSummary.setValue(errorList.join('<br/>'));
        }else{
            connectionSummary.setValue(Uni.I18n.translate('deviceconnectionhistory.noErrorsOrWarnings', 'MDC', 'No errors or warnings.'));
        }

        me.getStatusLink().setValue('<a href="#/devices/' + this.deviceId
            + '/connectionmethods/'
            + me.connectionMethodId + '/history/' + me.getDeviceConnectionHistoryGrid().getSelectionModel().getSelection()[0].get('id') + '/viewlog'
            + '?logLevels=Debug&logTypes=Connections&logTypes=Communications">'
            + connectionHistory.get('status') + '</a>');

        me.getComPortField().setValue(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.on', 'MDC', '{0} on {1}'), connectionHistory.get('comPort'), '<a href="#/administration/comservers/' + connectionHistory.get('comServer').id + '">' + connectionHistory.get('comServer').name + '</a>'));
        me.getDeviceConnectionHistoryPreview().setTitle(Uni.DateTime.formatDateTime(connectionHistory.get('startedOn'), Uni.DateTime.SHORT, Uni.DateTime.LONG));
        me.getTitlePanel().setTitle(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.communicationTasksTitle', 'MDC', 'Communication tasks')));

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
            errorList = [],
            communicationSummary = me.getCommunicationSummaryField(),
            deviceCommunicationTaskExecutionPreviewMenu = me.getDeviceCommunicationTaskExecutionPreviewMenu();
        me.getDeviceCommunicationTaskExecutionPreviewForm().loadRecord(communication);

        if (communication.get('errors') && communication.get('errors').length > 0) {
            errorList.push((Uni.I18n.translate('devicecommunicationhistory.errorsTitle', 'MDC', 'Errors:')));
            Ext.Array.forEach(communication.get('errors'), function (error){
                errorList.push(Uni.DateTime.formatDateTime(error.timestamp, Uni.DateTime.SHORT, Uni.DateTime.LONGWITHMILLIS) + ' - ' + error.details);
            });
            errorList.push(' '); //new empty line
        }

        if (communication.get('warnings') && communication.get('warnings').length > 0) {
            errorList.push((Uni.I18n.translate('devicecommunicationhistory.warningsTitle', 'MDC', 'Warnings:')));
            Ext.Array.forEach(communication.get('warnings'), function (warn){
                errorList.push(Uni.DateTime.formatDateTime(warn.timestamp, Uni.DateTime.SHORT, Uni.DateTime.LONGWITHMILLIS) + ' - ' + warn.details);
            });
            errorList.push(' '); //new empty line
        }

        me.getRightCommunicationDetails().setVisible(false);
        me.getLeftCommunicationDetails().setVisible(false);
        me.getShowCommunicationDetailsButton().setVisible(true);
        me.getHideCommunicationDetailsButton().setVisible(false);
        if(errorList.length > 0){
            communicationSummary.setValue(errorList.join('<br/>'));
        }else {
            communicationSummary.setValue(Uni.I18n.translate('devicecommunicationhistory.noErrorsOrWarnings', 'MDC', 'No errors or warnings.'));
        }

        deviceCommunicationTaskExecutionPreviewMenu.record = communication;
        me.getDeviceCommunicationTaskExecutionPreview().setTitle(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.on', 'MDC', '{0} on {1}'), communication.get('name'), me.device.get('name')));
    },

    showConnectionLog: function () {
        location.href = '#/devices/' + this.deviceId + '/connectionmethods/' + this.connectionMethodId + '/history/' + this.getDeviceConnectionHistoryGrid().getSelectionModel().getSelection()[0].get('id') + '/viewlog' +
        '?logLevels=Debug&logTypes=Connections&logTypes=Communications'
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
            + '?logLevels=Debug';
    },

    showConnectionDetails: function (){
        var me = this;

        me.getRightConnectionDetails().setVisible(true);
        me.getLeftConnectionDetails().setVisible(true);
        me.getShowConnectionDetailsButton().setVisible(false);
        me.getHideConnectionDetailsButton().setVisible(true);
    },

    hideConnectionDetails: function (){
        var me = this;

        me.getRightConnectionDetails().setVisible(false);
        me.getLeftConnectionDetails().setVisible(false);
        me.getShowConnectionDetailsButton().setVisible(true);
        me.getHideConnectionDetailsButton().setVisible(false);
    },

    showCommunicationDetails: function (){
        var me = this;

        me.getRightCommunicationDetails().setVisible(true);
        me.getLeftCommunicationDetails().setVisible(true);
        me.getShowCommunicationDetailsButton().setVisible(false);
        me.getHideCommunicationDetailsButton().setVisible(true);
    },

    hideCommunicationDetails: function (){
        var me = this;

        me.getRightCommunicationDetails().setVisible(false);
        me.getLeftCommunicationDetails().setVisible(false);
        me.getShowCommunicationDetailsButton().setVisible(true);
        me.getHideCommunicationDetailsButton().setVisible(false);
    },

    previewConnectionLog: function () {
        var connectionLog = this.getDeviceConnectionLogGrid().getSelectionModel().getSelection()[0],
            preview = this.getDeviceConnectionLogPreviewForm();

        preview.loadRecord(connectionLog);
        preview.up('#deviceConnectionLogPreview').setTitle(Uni.DateTime.formatDateTime(connectionLog.get('timestamp'), Uni.DateTime.SHORT, Uni.DateTime.LONGWITHMILLIS));
    }
});
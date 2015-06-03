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
        'Mdc.view.setup.deviceconnectionhistory.SideFilter',
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
        {ref: 'deviceCommunicationTaskExecutionPreviewMenu', selector: '#deviceCommunicationTaskExecutionPreview button menu'},
        {ref: 'deviceCommunicationTaskExecutionGridActionColumn', selector: '#deviceCommunicationTaskExecutionGrid #action'},
        {ref: 'deviceConnectionHistoryPreviewMenu', selector: '#deviceConnectionHistoryPreview button menu'},
        {ref: 'deviceConnectionHistoryGridActionColumn', selector: '#deviceConnectionHistoryGrid #action'},
        {ref: 'titlePanel', selector: '#titlePanel'},
        {ref: 'comPortField', selector: '#comPort'},
        {ref: 'deviceConnectionLogOverviewForm', selector: '#deviceConnectionLogOverviewForm'},
        {ref: 'deviceconnectionhistorySideFilterForm', selector: '#deviceconnectionhistorySideFilterForm'},
        {ref: 'deviceConnectionLogGrid', selector: '#deviceConnectionLogGrid'},
        {ref: 'deviceConnectionLogPreviewForm', selector: '#deviceConnectionLogPreviewForm'},
        {ref: 'deviceConnectionLogFilterPanel', selector: '#deviceConnectionLogMain #deviceConnectionLogFilterTopPanel'},
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
            '#deviceconnectionhistorySideFilter button[action=applyfilter]': {
                click: this.applyFilter
            },
            '#deviceconnectionhistorySideFilter button[action=resetfilter]': {
                click: this.resetFilter
            },
            '#deviceConnectionLogGrid': {
                selectionchange: this.previewConnectionLog
            },
            '#deviceConnectionLogMain #deviceConnectionLogFilterTopPanel': {
                removeFilter: this.removeFilter,
                clearAllFilters: this.resetFilter
            }



        });
    },

    showDeviceConnectionMethodHistory: function (deviceMrId, connectionMethodId) {
        var me = this;
        this.deviceMrId = deviceMrId;
        this.connectionMethodId = connectionMethodId;
        var deviceModel = Ext.ModelManager.getModel('Mdc.model.Device');
        this.getDeviceCommunicationTaskExecutionsStore().removeAll(true);
        deviceModel.load(deviceMrId, {
            success: function (device) {
                me.device = device;
                var connectionMethodModel = Ext.ModelManager.getModel('Mdc.model.DeviceConnectionMethod');
                connectionMethodModel.getProxy().setExtraParam('mrid', encodeURIComponent(deviceMrId));
                connectionMethodModel.load(connectionMethodId, {
                    success: function (connectionMethod) {
                        var widget = Ext.widget('deviceConnectionHistoryMain', {device: device, mrid: deviceMrId, connectionMethodId: connectionMethodId, connectionMethodName: connectionMethod.get('name')});
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.getApplication().fireEvent('loadDevice', device);
                        me.getApplication().fireEvent('loadConnectionMethod', connectionMethod);
                    }
                });
            }
        });
    },

    previewDeviceConnectionHistory: function () {
        var me = this;
        var connectionHistory = this.getDeviceConnectionHistoryGrid().getSelectionModel().getSelection()[0];
        this.getDeviceConnectionHistoryPreviewForm().loadRecord(connectionHistory);


        this.getStatusLink().setValue('<a href="#/devices/' + encodeURIComponent(this.deviceMrId) + '/connectionmethods/' + this.connectionMethodId + '/history/' + this.getDeviceConnectionHistoryGrid().getSelectionModel().getSelection()[0].get('id') + '/viewlog' +'?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22logTypes%22%3A%5B%22Connections%22%2C%22Communications%22%5D%7D">' +
            connectionHistory.get('status')+'</a>');



        this.getComPortField().setValue(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.on', 'MDC', '{0} on {1}'), connectionHistory.get('comPort'), '<a href="#/administration/comservers/' + connectionHistory.get('comServer').id + '/overview">' + connectionHistory.get('comServer').name + '</a>'));
        this.getDeviceConnectionHistoryPreview().setTitle(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.on', 'MDC', '{0} on {1}'), connectionHistory.get('connectionMethod').name, me.device.get('mRID')));
        this.getTitlePanel().setTitle(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.comtasksTitle', 'MDC', 'Communications of {0} connection on {1}'), connectionHistory.get('connectionMethod').name, this.device.get('mRID')));
        var deviceCommunicationTaskExecutionsStore = this.getDeviceCommunicationTaskExecutionsStore();


        var deviceConnectionHistoryPreviewMenu = this.getDeviceConnectionHistoryPreviewMenu();
        deviceConnectionHistoryPreviewMenu.removeAll();
        var deviceConnectionHistoryGridActionColumn = this.getDeviceConnectionHistoryGridActionColumn();
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
        })
        Ext.resumeLayouts();
    },

    previewDeviceCommunicationTaskExecution: function () {
        var me = this;
        var communication = this.getDeviceCommunicationTaskExecutionGrid().getSelectionModel().getSelection()[0];
        var menuItems = [];
        var deviceCommunicationTaskExecutionPreviewMenu = this.getDeviceCommunicationTaskExecutionPreviewMenu();
        deviceCommunicationTaskExecutionPreviewMenu.removeAll();
        var deviceCommunicationTaskExecutionGridActionColumn = this.getDeviceCommunicationTaskExecutionGridActionColumn();
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
        this.getDeviceCommunicationTaskExecutionPreviewForm().loadRecord(communication);
        this.getDeviceCommunicationTaskExecutionPreview().setTitle(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.on', 'MDC', '{0} on {1}'), communication.get('name'), me.device.get('mRID')));
    },

    showConnectionLog: function () {
        location.href = '#/devices/' + encodeURIComponent(this.deviceMrId) + '/connectionmethods/' + encodeURIComponent(this.connectionMethodId) + '/history/' + encodeURIComponent(this.getDeviceConnectionHistoryGrid().getSelectionModel().getSelection()[0].get('id')) + '/viewlog' +
            '?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22logTypes%22%3A%5B%22Connections%22%2C%22Communications%22%5D%7D'
    },

    showDeviceConnectionMethodHistoryLog: function (deviceMrId, deviceConnectionMethodId, deviceConnectionHistoryId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            deviceModel = Ext.ModelManager.getModel('Mdc.model.Device'),
            connectionMethodModel = Ext.ModelManager.getModel('Mdc.model.DeviceConnectionMethod'),
            comSessionHistory = Ext.ModelManager.getModel('Mdc.model.DeviceConnectionHistory'),
            store = me.getDeviceConnectionLogStore(),
            filter = router.filter,
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
                                store.setFilterModel(filter);
                                widget = Ext.widget('deviceConnectionLogMain', {device: device, mrid: deviceMrId});
                                me.getApplication().fireEvent('changecontentevent', widget);
                                store.load(function () {
                                    me.getApplication().fireEvent('loadDevice', device);
                                    me.getApplication().fireEvent('loadConnectionMethod', connectionMethod);
                                    me.getDeviceconnectionhistorySideFilterForm().loadRecord(filter);
                                    for (prop in filter.data) {
                                        if (filter.data.hasOwnProperty(prop) && prop.toString() !== 'id' && filter.data[prop]) {
                                            if(prop.toString()==='logLevels'){
                                                me.getDeviceConnectionLogFilterPanel().setFilter(prop.toString(), me.getDeviceconnectionhistorySideFilterForm().down('#logLevelField').getFieldLabel(), filter.data[prop]);
                                            } else if (prop.toString()==='logTypes'){
                                                me.getDeviceConnectionLogFilterPanel().setFilter(prop.toString(), me.getDeviceconnectionhistorySideFilterForm().down('#logTypeField').getFieldLabel(), filter.data[prop]);
                                            }
                                        }
                                    }
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
        location.href = '#/devices/' + encodeURIComponent(item.action.comTask.mRID)
            + '/communicationtasks/' + encodeURIComponent(item.action.comTask.comTaskId)
            + '/history/' + encodeURIComponent(item.action.comTask.sessionId)
            + '/viewlog' +
            '?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22id%22%3Anull%7D';
    },

    applyFilter: function () {
        var values = this.getDeviceconnectionhistorySideFilterForm().getValues();
        for (var prop in values) {
            if (values.hasOwnProperty(prop) && typeof values[prop] === 'string') {
                values[prop] = [values[prop]];
            }
        }

        this.getDeviceconnectionhistorySideFilterForm().getRecord().set(values);
        if (!values.hasOwnProperty('logLevels')) {
            delete this.getDeviceconnectionhistorySideFilterForm().getRecord().data.logLevels;
        }
        if (!values.hasOwnProperty('logTypes')) {
            delete this.getDeviceconnectionhistorySideFilterForm().getRecord().data.logTypes;
        }
        this.getDeviceconnectionhistorySideFilterForm().getRecord().save();
    },

    resetFilter: function () {
        this.getDeviceconnectionhistorySideFilterForm().getRecord().getProxy().destroy();
    },

    previewConnectionLog: function () {
        var connectionLog = this.getDeviceConnectionLogGrid().getSelectionModel().getSelection()[0],
            preview = this.getDeviceConnectionLogPreviewForm();
        preview.loadRecord(connectionLog);
        preview.up('#deviceConnectionLogPreview').setTitle(Uni.DateTime.formatDateTimeLong(connectionLog.get('timestamp')));
    },

    removeFilter: function (key) {
        var router = this.getController('Uni.controller.history.Router'),
            record = router.filter;

        record.set(key, null);
        record.save();
    }
});
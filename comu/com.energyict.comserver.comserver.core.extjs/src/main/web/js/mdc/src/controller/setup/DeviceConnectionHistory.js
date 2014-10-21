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
        {ref: 'deviceConnectionLogFilterPanel', selector: '#deviceConnectionLogMain #deviceConnectionLogFilterTopPanel'}
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
        deviceModel.load(deviceMrId, {
            success: function (device) {
                me.device = device;
                var connectionMethodModel = Ext.ModelManager.getModel('Mdc.model.DeviceConnectionMethod');
                connectionMethodModel.getProxy().setExtraParam('mrid', deviceMrId);
                connectionMethodModel.load(connectionMethodId, {
                    success: function (connectionMethod) {
                        var deviceConnectionHistoryStore = me.getDeviceConnectionHistoryStore();
                        deviceConnectionHistoryStore.getProxy().setExtraParam('mRID', deviceMrId);
                        deviceConnectionHistoryStore.getProxy().setExtraParam('connectionId', connectionMethodId);
                        deviceConnectionHistoryStore.load({
                                callback: function () {
                                    var widget = Ext.widget('deviceConnectionHistoryMain', {mrid: deviceMrId, connectionMethodId: connectionMethodId, connectionMethodName: connectionMethod.get('name')});
                                    me.getApplication().fireEvent('changecontentevent', widget);
                                    me.getApplication().fireEvent('loadDevice', device);
                                    me.getApplication().fireEvent('loadConnectionMethod', connectionMethod);
                                }
                            }
                        );
                    }
                });
            }
        });
    },

    previewDeviceConnectionHistory: function () {
        var me = this;
        var connectionHistory = this.getDeviceConnectionHistoryGrid().getSelectionModel().getSelection()[0];
        this.getDeviceConnectionHistoryPreviewForm().loadRecord(connectionHistory);
        this.getComPortField().setValue(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.on', 'MDC', '{0} on {1}'), connectionHistory.get('comPort'), '<a href="#/administration/comservers/' + connectionHistory.get('comServer').id + '/overview">' + connectionHistory.get('comServer').name + '</a>'));
        this.getDeviceConnectionHistoryPreview().setTitle(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.on', 'MDC', '{0} on {1}'), connectionHistory.get('connectionMethod'), me.device.get('mRID')));
        this.getTitlePanel().setTitle(Ext.String.format(Uni.I18n.translate('deviceconnectionhistory.comtasksTitle', 'MDC', 'Communications of {0} connection on {1}'), connectionHistory.get('connectionMethod'), this.device.get('mRID')));
        var deviceCommunicationTaskExecutionsStore = this.getDeviceCommunicationTaskExecutionsStore();


        var deviceConnectionHistoryPreviewMenu = this.getDeviceConnectionHistoryPreviewMenu();
        deviceConnectionHistoryPreviewMenu.removeAll();
        var deviceConnectionHistoryGridActionColumn = this.getDeviceConnectionHistoryGridActionColumn();
        deviceConnectionHistoryGridActionColumn.menu.removeAll();
        var menuItem = {
            text: Uni.I18n.translate('deviceconnectionhistory.viewLog', 'MDC', 'View log'),
            action: 'viewLog',
            listeners: {
                click: function () {
                    me.showConnectionLog();
                }
            }
        };
        deviceConnectionHistoryPreviewMenu.add(menuItem);
        deviceConnectionHistoryGridActionColumn.menu.add(menuItem);
        deviceCommunicationTaskExecutionsStore.getProxy().setExtraParam('mRID', this.deviceMrId);
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
                    comTask: item.name
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
        location.href = '#/devices/' + this.deviceMrId + '/connectionmethods/' + this.connectionMethodId + '/history/' + this.getDeviceConnectionHistoryGrid().getSelectionModel().getSelection()[0].get('id') + '/viewlog' +
            '?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22logTypes%22%3A%5B%22connections%22%2C%22communications%22%5D%7D'
    },

    showDeviceConnectionMethodHistoryLog: function (deviceMrId, deviceConnectionMethodId, deviceConnectionHistoryId) {
        var me = this;
        var router = this.getController('Uni.controller.history.Router');


        var deviceModel = Ext.ModelManager.getModel('Mdc.model.Device');
        deviceModel.load(deviceMrId, {
            success: function (device) {
                var connectionMethodModel = Ext.ModelManager.getModel('Mdc.model.DeviceConnectionMethod');
                connectionMethodModel.getProxy().setExtraParam('mrid', deviceMrId);
                connectionMethodModel.load(deviceConnectionMethodId, {
                    success: function (connectionMethod) {
                        var comSessionHistory = Ext.ModelManager.getModel('Mdc.model.DeviceConnectionHistory');
                        comSessionHistory.getProxy().setExtraParam('mRID', deviceMrId);
                        comSessionHistory.getProxy().setExtraParam('connectionId', deviceConnectionMethodId);
                        comSessionHistory.load(deviceConnectionHistoryId, {
                            success: function (deviceConnectionHistory) {
                                var widget = Ext.widget('deviceConnectionLogMain', {mrid: deviceMrId});
                                me.getApplication().fireEvent('changecontentevent', widget);
                                me.getDeviceConnectionLogOverviewForm().loadRecord(deviceConnectionHistory);
                                me.getApplication().fireEvent('loadDevice', device);
                                me.getApplication().fireEvent('loadConnectionMethod', connectionMethod);

                                //to test

                                var store = me.getDeviceConnectionLogStore();
                                store.getProxy().setExtraParam('mRID', deviceMrId);
                                store.getProxy().setExtraParam('connectionId', deviceConnectionMethodId);
                                store.getProxy().setExtraParam('sessionId', deviceConnectionHistoryId);


                                var filter = router.filter;
                                me.getDeviceconnectionhistorySideFilterForm().loadRecord(filter);
                                store.setFilterModel(filter);
                                for (prop in filter.data) {
                                    if (filter.data.hasOwnProperty(prop) && prop.toString() !== 'id' && filter.data[prop]) {
                                        me.getDeviceConnectionLogFilterPanel().setFilter(prop.toString(), prop.toString(), filter.data[prop]);
                                    }
                                }
                                store.load();
                            }
                        });
                    }
                });
            }
        });
    },

    showComTaskLog: function (item) {
        console.log('dfdfdffdfd');
    },

    applyFilter: function () {
        var values = this.getDeviceconnectionhistorySideFilterForm().getValues();
        for (var prop in values) {
            if (values.hasOwnProperty(prop) && typeof values[prop] === 'string') {
                values[prop] = [values[prop]];
            }
        }
        this.getDeviceconnectionhistorySideFilterForm().getRecord().set(values);
        this.getDeviceconnectionhistorySideFilterForm().getRecord().save();
    },

    resetFilter: function () {
        this.getDeviceconnectionhistorySideFilterForm().getRecord().getProxy().destroy();
    },

    previewConnectionLog: function () {
        var connectionLog = this.getDeviceConnectionLogGrid().getSelectionModel().getSelection()[0];
        this.getDeviceConnectionLogPreviewForm().loadRecord(connectionLog);
    },

    removeFilter: function (key) {
        var router = this.getController('Uni.controller.history.Router'),
            record = router.filter;

        record.set(key, null);
        record.save();
    }

});
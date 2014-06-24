Ext.define('Mdc.controller.setup.DeviceConnectionMethods', {
    extend: 'Ext.app.Controller',
    deviceTypeId: null,
    deviceConfigurationId: null,
    requires: [
        'Mdc.store.ConnectionMethodsOfDevice'
    ],

    views: [
        'setup.deviceconnectionmethod.DeviceConnectionMethodSetup',
        'setup.deviceconnectionmethod.DeviceConnectionMethodsGrid',
        'setup.deviceconnectionmethod.DeviceConnectionMethodPreview',
        'setup.deviceconnectionmethod.DeviceConnectionMethodEdit'
    ],

    stores: [
        'ConnectionMethodsOfDevice',
        'ConnectionStrategies'
    ],

    refs: [
        {ref: 'deviceConnectionMethodsGrid', selector: '#deviceconnectionmethodsgrid'},
//        {ref: 'connectionMethodPreviewForm', selector: '#connectionMethodPreviewForm'},
        {ref: 'deviceConnectionMethodPreview', selector: '#deviceConnectionMethodPreview'},
        {ref: 'deviceConnectionMethodPreviewTitle', selector: '#deviceConnectionMethodPreviewTitle'},
        {ref: 'deviceConnectionMethodPreviewForm', selector: '#deviceConnectionMethodPreviewForm'},
        {ref: 'deviceConnectionMethodEditView', selector: '#deviceConnectionMethodEdit'},
        {ref: 'deviceConnectionMethodEditForm', selector: '#deviceConnectionMethodEditForm'},
        {ref: 'deviceConnectionMethodComboBox', selector: '#deviceConnectionMethodComboBox'},
        {ref: 'connectionStrategyComboBox', selector: '#connectionStrategyComboBox'},
        {ref: 'scheduleField', selector: '#scheduleField'},
//        {ref: 'connectionTypeComboBox', selector: '#connectionTypeComboBox'},
        {ref: 'toggleDefaultMenuItem', selector: '#toggleDefaultMenuItem'},
        {ref: 'toggleActiveMenuItem', selector: '#toggleActiveMenuItem'}
    ],

    init: function () {
        this.control({
            '#deviceconnectionmethodsgrid': {
                selectionchange: this.previewDeviceConnectionMethod
            },
            '#deviceconnectionmethodsgrid menuitem[action = createDeviceOutboundConnectionMethod]': {
                click: this.addOutboundConnectionMethodHistory
            },
            '#deviceconnectionmethodsgrid menuitem[action = createDeviceInboundConnectionMethod]': {
                click: this.addInboundConnectionMethodHistory
            },
            'button[action = createDeviceOutboundConnectionMethod]': {
                click: this.addOutboundConnectionMethodHistory
            },
            'button[action = createDeviceInboundConnectionMethod]': {
                click: this.addInboundConnectionMethodHistory
            },
            '#deviceconnectionmethodsgrid actioncolumn': {
                editDeviceConnectionMethod: this.editDeviceConnectionMethodHistory,
                deleteDeviceConnectionMethod: this.deleteDeviceConnectionMethod,
                toggleDefault: this.toggleDefaultDeviceConnectionMethod,
                toggleActive: this.toggleActiveDeviceconnectionMethod
            },
            '#deviceConnectionMethodPreview menuitem[action=editDeviceConnectionMethod]': {
                click: this.editDeviceConnectionMethodHistoryFromPreview
            },
            '#deviceConnectionMethodPreview menuitem[action=toggleDefault]': {
                click: this.toggleDefaultDeviceConnectionMethod
            },
            '#deviceConnectionMethodPreview menuitem[action=deleteDeviceConnectionMethod]': {
                click: this.deleteDeviceConnectionMethod
            },
            '#deviceConnectionMethodEdit #deviceConnectionMethodComboBox': {
                select: this.selectDeviceConfigConnectionMethod
            },
            '#addEditButton[action=addDeviceOutboundConnectionMethod]': {
                click: this.addDeviceOutboundConnectionMethod
            },
            '#addEditButton[action=editDeviceOutboundConnectionMethod]': {
                click: this.editDeviceOutboundConnectionMethod
            },
            '#addEditButton[action=addDeviceInboundConnectionMethod]': {
                click: this.addDeviceInboundConnectionMethod
            },
            '#addEditButton[action=editDeviceInboundConnectionMethod]': {
                click: this.editDeviceInboundConnectionMethod
            },
            '#connectionStrategyComboBox': {
                select: this.showScheduleField
            }
        });
    },

    showDeviceConnectionMethods: function (mrid) {
        var me = this;
        this.mrid = mrid;
        var widget = Ext.widget('deviceConnectionMethodSetup', {mrid: mrid});
        Ext.ModelManager.getModel('Mdc.model.Device').load(mrid, {
            success: function (device) {
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getApplication().fireEvent('loadDevice', device);
                me.getDeviceConnectionMethodsGrid().getSelectionModel().doSelect(0);
            }
        });
    },

    previewDeviceConnectionMethod: function () {
        var connectionMethod = this.getDeviceConnectionMethodsGrid().getSelectionModel().getSelection();
        if (connectionMethod.length == 1) {

            var toggleDefaultMenuItemText =
                connectionMethod[0].get('isDefault') ?
                    Uni.I18n.translate('deviceconnectionmethod.unsetAsDefault', 'MDC', 'Remove as default') :
                    Uni.I18n.translate('deviceconnectionmethod.setAsDefault', 'MDC', 'Set as default');
            this.getToggleDefaultMenuItem().setText(toggleDefaultMenuItemText);

            var toggleActiveMenuItemText =
                connectionMethod[0].get('paused') ?
                    Uni.I18n.translate('deviceconnectionmethod.activate', 'MDC', 'Activate'):
                    Uni.I18n.translate('deviceconnectionmethod.deActivate', 'MDC', 'Deactivate');
            this.getToggleActiveMenuItem().setText(toggleActiveMenuItemText);

            this.getDeviceConnectionMethodPreviewForm().loadRecord(connectionMethod[0]);
            var connectionMethodName = connectionMethod[0].get('name');
            this.getDeviceConnectionMethodPreview().getLayout().setActiveItem(1);
            this.getDeviceConnectionMethodPreview().setTitle(connectionMethodName);
            this.getDeviceConnectionMethodPreviewForm().loadRecord(connectionMethod[0]);
            this.getDeviceConnectionMethodPreview().down('#toggleDefaultMenuItem').setText(connectionMethod[0].get('isDefault') === true ? Uni.I18n.translate('deviceconnectionmethod.unsetAsDefault', 'MDC', 'Remove as default') : Uni.I18n.translate('deviceconnectionmethod.setAsDefault', 'MDC', 'Set as default'));
            if(connectionMethod[0].propertiesStore.data.items.length > 0) {
                this.getDeviceConnectionMethodPreview().down('#connectionDetailsTitle').setVisible(true);
            } else {
                this.getDeviceConnectionMethodPreview().down('#connectionDetailsTitle').setVisible(false);
            }
            this.getPropertiesViewController().showProperties(connectionMethod[0], this.getDeviceConnectionMethodPreview());
        } else {
            this.getDeviceConnectionMethodPreview().getLayout().setActiveItem(0);
        }
    },

    getPropertiesViewController: function () {
        return this.getController('Mdc.controller.setup.PropertiesView');
    },

    addOutboundConnectionMethodHistory: function () {
        location.href = '#/devices/' + this.mrid + '/connectionmethods/addoutbound';
    },

    addInboundConnectionMethodHistory: function () {
        location.href = '#/devices/' + this.mrid + '/connectionmethods/addinbound';
    },

    editDeviceConnectionMethodHistory: function (record) {
        location.href = '#/devices/' + this.mrid + '/connectionmethods/' + record.get('id') + '/edit';
    },

    editDeviceConnectionMethodHistoryFromPreview: function () {
        this.editDeviceConnectionMethodHistory(this.getDeviceConnectionMethodPreviewForm().getRecord());
    },

    showAddDeviceConnectionMethodView: function (mrid, direction) {
        this.mrid = mrid;
        var me = this;
        var deviceModel = Ext.ModelManager.getModel('Mdc.model.Device');
        var connectionMethodsStore = Ext.StoreManager.get('ConnectionMethodsOfDeviceConfiguration');
        var comPortPoolStore = Ext.StoreManager.get('ComPortPools');
        var connectionStrategiesStore = Ext.StoreManager.get('ConnectionStrategies');
        var widget = Ext.widget('deviceConnectionMethodEdit', {
            edit: false,
            returnLink: '#/devices/' + this.mrid + '/connectionmethods',
            connectionMethods: connectionMethodsStore,
            comPortPools: comPortPoolStore,
            connectionStrategies: connectionStrategiesStore,
            direction: direction
        });
        widget.setLoading(true);
        me.getApplication().fireEvent('changecontentevent', widget);
        deviceModel.load(mrid, {
            success: function (device) {
                connectionMethodsStore.getProxy().extraParams = ({deviceType: device.get('deviceTypeId'), deviceConfig: device.get('deviceConfigurationId')});
                connectionMethodsStore.getProxy().setExtraParam('available',true);
                connectionMethodsStore.getProxy().setExtraParam('mrId',mrid);
                connectionMethodsStore.clearFilter(true);
                connectionMethodsStore.filter('direction', direction);
                connectionMethodsStore.load({
                    callback: function () {
                        comPortPoolStore.clearFilter(true);
                        comPortPoolStore.filter('direction', direction);
                        comPortPoolStore.load({
                            callback: function () {
                                connectionStrategiesStore.load({
                                    callback: function () {
                                        var title = direction === 'Outbound' ? Uni.I18n.translate('deviceconnectionmethod.addOutboundConnectionMethod', 'MDC', 'Add outbound connection method') : Uni.I18n.translate('deviceconnectionmethod.addInboundConnectionMethod', 'MDC', 'Add inbound connection method');
                                        widget.down('#deviceConnectionMethodEditAddTitle').update('<h1>' + title + '</h1>');
                                        widget.setLoading(false);
                                    }
                                });

                            }
                        });

                    }
                });
            }
        });
    },

    selectDeviceConfigConnectionMethod: function (comboBox, connectionMethod) {
        var connectionMethod = comboBox.findRecordByValue(comboBox.getValue())
        this.getDeviceConnectionMethodEditView().down('#communicationPortPoolComboBox').setDisabled(false);
        this.getDeviceConnectionMethodEditView().down('#connectionStrategyComboBox').setDisabled(false);
        this.getDeviceConnectionMethodEditView().down('#scheduleField').setDisabled(false);
       // this.getDeviceConnectionMethodEditView().down('#rescheduleRetryDelay').setDisabled(false);
        this.getDeviceConnectionMethodEditView().down('#allowSimultaneousConnections').setDisabled(false);
        if (connectionMethod.get('connectionStrategy') === 'minimizeConnections') {
            this.getDeviceConnectionMethodEditView().down('form').down('#scheduleField').setVisible(true);
        }
        this.getDeviceConnectionMethodEditView().down('form').loadRecord(connectionMethod);
        this.getDeviceConnectionMethodEditView().down('form').down('#communicationPortPoolComboBox').setValue(connectionMethod.get('comPortPool'));
        this.getDeviceConnectionMethodEditView().down('form').down('#connectionStrategyComboBox').setValue(connectionMethod.get('connectionStrategy'));
        this.getPropertiesController().showProperties(connectionMethod, this.getDeviceConnectionMethodEditView(), false);
    },

    showScheduleField: function (combobox, objList) {
        if (objList[0].get('connectionStrategy') === 'minimizeConnections') {
            this.getScheduleField().setVisible(true);
        } else {
            this.getScheduleField().setVisible(false);
            this.getScheduleField().clear();
        }
    },

    addDeviceOutboundConnectionMethod: function () {
        var values = this.getDeviceConnectionMethodEditForm().getValues();
        values.direction = 'Outbound';
        this.addDeviceConnectionMethod(values);
    },

    addDeviceInboundConnectionMethod: function () {
        var values = this.getDeviceConnectionMethodEditForm().getValues();
        values.direction = 'Inbound';
        this.addDeviceConnectionMethod(values);
    },

    editDeviceOutboundConnectionMethod: function () {
        var values = this.getDeviceConnectionMethodEditForm().getValues();
        values.direction = 'Outbound';
        this.editDeviceConnectionMethod(values);
    },

    editDeviceInboundConnectionMethod: function () {
        var values = this.getDeviceConnectionMethodEditForm().getValues();
        values.direction = 'Inbound';
        this.editDeviceConnectionMethod(values);
    },

    editDeviceConnectionMethod: function (values) {
        var record = this.getDeviceConnectionMethodEditForm().getRecord();
        this.updateRecord(record, values);

    },

    addDeviceConnectionMethod: function (values) {
        var record = Ext.create(Mdc.model.DeviceConnectionMethod);
        this.updateRecord(record, values);
    },

    updateRecord: function (record, values) {
        var me = this;
        if (record) {
            record.set(values);

            if (values.connectionStrategy === 'asSoonAsPossible') {
                record.set('nextExecutionSpecs', null);
            }
            record.propertiesStore = this.getPropertiesController().updateProperties();
            record.getProxy().extraParams = ({mrid: me.mrid});
            record.save({
                success: function (record) {
                    location.href = '#/devices/' + me.mrid + '/connectionmethods/';
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getConnectionMethodEditForm().getForm().markInvalid(json.errors);
                        me.getPropertiesController().showErrors(json.errors);
                    }
                }
            });

        }
    },

    deleteDeviceConnectionMethod: function (connectionMethodToDelete) {
        var me = this;
        if (connectionMethodToDelete.hasOwnProperty('action')) {
            connectionMethodToDelete = this.getDeviceConnectionMethodsGrid().getSelectionModel().getSelection()[0];
        }
        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('deviceconnectionmethod.deleteConnectionMethod', 'MDC', 'Are you sure you want to delete connection method ' + connectionMethodToDelete.get('name') + '?'),
            title: Uni.I18n.translate('deviceconnectionmethod.deleteConnectionMethod.title', 'MDC', 'Delete connection method ') + ' ' + connectionMethodToDelete.get('name') + '?',
            config: {
                connectionMethodToDelete: connectionMethodToDelete,
                me: me
            },
            fn: me.removeDeviceConnectionMethod
        });
    },

    removeDeviceConnectionMethod: function (btn, text, opt) {
        if (btn === 'confirm') {
            var connectionMethodToDelete = opt.config.connectionMethodToDelete;
            var me = opt.config.me;
            connectionMethodToDelete.getProxy().extraParams = ({mrid: me.mrid});
            connectionMethodToDelete.destroy({
                callback: function () {
                    location.href = '#/devices/'+me.mrid+'/connectionmethods';
                }
            });

        }
    },
//
//    showConnectionTypeProperties: function (combobox, objList) {
//        var objectWithProperties = this.getConnectionTypeComboBox().findRecordByValue(this.getConnectionTypeComboBox().getValue());
//        if (objectWithProperties.propertiesStore.data.items.length > 0) {
//            this.getConnectionMethodEditView().down('#connectionDetailsTitle').setVisible(true);
//        } else {
//            this.getConnectionMethodEditView().down('#connectionDetailsTitle').setVisible(false);
//        }
//        this.getPropertiesController().showProperties(objectWithProperties, this.getConnectionMethodEditView(), false);
//    },
//
    getPropertiesController: function () {
        return this.getController('Mdc.controller.setup.Properties');
    },

    showDeviceConnectionMethodEditView: function (mrid, connectionMethodId) {
        this.mrid = mrid;
        var me = this;
        var deviceModel = Ext.ModelManager.getModel('Mdc.model.Device');
        var connectionMethodModel = Ext.ModelManager.getModel('Mdc.model.DeviceConnectionMethod');
        var connectionMethodsStore = Ext.StoreManager.get('ConnectionMethodsOfDeviceConfiguration');
        var comPortPoolStore = Ext.StoreManager.get('ComPortPools');
        var connectionStrategiesStore = Ext.StoreManager.get('ConnectionStrategies');

        deviceModel.load(mrid, {
            success: function (device) {
                connectionMethodModel.getProxy().setExtraParam('mrid',mrid);
                connectionMethodModel.load(connectionMethodId,{
                    success: function(connectionMethod){
                        var widget = Ext.widget('deviceConnectionMethodEdit', {
                            edit: true,
                            returnLink: '#/devices/' + me.mrid + '/connectionmethods',
                            connectionMethods: connectionMethodsStore,
                            comPortPools: comPortPoolStore,
                            connectionStrategies: connectionStrategiesStore,
                            direction: connectionMethod.get('direction')
                        });
                        widget.setLoading(true);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        connectionMethodsStore.getProxy().extraParams = ({deviceType: device.get('deviceTypeId'), deviceConfig: device.get('deviceConfigurationId')});
                        connectionMethodsStore.clearFilter(true);
                        connectionMethodsStore.filter('direction', connectionMethod.get('direction'));
                        connectionMethodsStore.load({
                            callback: function () {
                                comPortPoolStore.clearFilter(true);
                                comPortPoolStore.filter('direction', connectionMethod.get('direction'));
                                comPortPoolStore.load({
                                    callback: function () {
                                        connectionStrategiesStore.load({
                                            callback: function () {
                                                var title = connectionMethod.get('direction') === 'Outbound' ? Uni.I18n.translate('deviceconnectionmethod.editOutboundConnectionMethod', 'MDC', 'Edit outbound connection method') : Uni.I18n.translate('deviceconnectionmethod.editInboundConnectionMethod', 'MDC', 'Edit inbound connection method');
                                                widget.down('#deviceConnectionMethodEditAddTitle').update('<h1>' + title + '</h1>');
                                                me.getDeviceConnectionMethodEditView().down('#communicationPortPoolComboBox').setDisabled(false);
                                                me.getDeviceConnectionMethodEditView().down('#allowSimultaneousConnections').setDisabled(false);
                                                me.getDeviceConnectionMethodEditView().down('#connectionStrategyComboBox').setDisabled(false);
                                                me.getDeviceConnectionMethodEditView().down('#scheduleField').setDisabled(false);
                                                me.getDeviceConnectionMethodComboBox().setDisabled(true);
                                                me.getDeviceConnectionMethodEditView().down('form').loadRecord(connectionMethod);
                                                if (connectionMethod.get('connectionStrategy') === 'minimizeConnections') {
                                                    widget.down('form').down('#scheduleField').setVisible(true);
                                                }
                                                widget.down('form').down('#communicationPortPoolComboBox').setValue(connectionMethod.get('comPortPool'));
                                                widget.down('form').down('#connectionStrategyComboBox').setValue(connectionMethod.get('connectionStrategy'));
                                                debugger;
                                                me.getPropertiesController().showProperties(connectionMethod, me.getDeviceConnectionMethodEditView(), false);
                                                widget.setLoading(false);
                                            }
                                        });

                                    }
                                });

                            }
                        });
                    }
                });
            }
        });
    },

    toggleDefaultDeviceConnectionMethod: function (connectionMethod) {
        var me = this;
        if (connectionMethod.hasOwnProperty('action')) {
            connectionMethod = this.getConnectionmethodsgrid().getSelectionModel().getSelection()[0];
        }
        if (connectionMethod.get('isDefault') === true) {
            connectionMethod.set('isDefault', false);
        } else {
            connectionMethod.set('isDefault', true);
        }
        if (connectionMethod.get('connectionStrategy') === 'asSoonAsPossible' || connectionMethod.get('direction') === 'Inbound') {
            connectionMethod.set('nextExecutionSpecs', null);
        }
        this.getPropertiesController().updatePropertiesWithoutView(connectionMethod);
        connectionMethod.getProxy().extraParams = ({mrid: this.mrid});
        connectionMethod.save({
            callback: function () {
                me.getConnectionMethodsOfDeviceStore().load();
                me.previewDeviceConnectionMethod();
            }
        });
    },

    toggleActiveDeviceconnectionMethod: function(connectionMethod){
        var me = this;
        if (connectionMethod.hasOwnProperty('action')) {
            connectionMethod = this.getConnectionmethodsgrid().getSelectionModel().getSelection()[0];
        }
        if (connectionMethod.get('paused') === true) {
            connectionMethod.set('paused', false);
        } else {
            connectionMethod.set('paused', true);
        }
        if (connectionMethod.get('connectionStrategy') === 'asSoonAsPossible' || connectionMethod.get('direction') === 'Inbound') {
            connectionMethod.set('nextExecutionSpecs', null);
        }
        this.getPropertiesController().updatePropertiesWithoutView(connectionMethod);
        connectionMethod.getProxy().extraParams = ({mrid: this.mrid});
        connectionMethod.save({
            callback: function () {
                me.getConnectionMethodsOfDeviceStore().load();
                me.previewDeviceConnectionMethod();
            }
        });
    }
});
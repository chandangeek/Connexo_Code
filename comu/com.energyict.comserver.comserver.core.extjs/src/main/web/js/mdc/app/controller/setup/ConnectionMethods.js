Ext.define('Mdc.controller.setup.ConnectionMethods', {
    extend: 'Ext.app.Controller',
    deviceTypeId: null,
    deviceConfigurationId: null,
    requires: [
        'Mdc.store.ConnectionMethodsOfDeviceConfiguration'
    ],

    views: [
        'setup.connectionmethod.ConnectionMethodSetup',
        'setup.connectionmethod.ConnectionMethodsGrid',
        'setup.connectionmethod.ConnectionMethodPreview',
        'setup.connectionmethod.ConnectionMethodEdit'
    ],

    stores: [
        'ConnectionMethodsOfDeviceConfiguration',
        'ConnectionTypes',
        'ConnectionStrategies'
    ],

    refs: [
        {ref: 'connectionmethodsgrid', selector: '#connectionmethodsgrid'},
        {ref: 'connectionMethodPreviewForm', selector: '#connectionMethodPreviewForm'},
        {ref: 'connectionMethodPreview', selector: '#connectionMethodPreview'},
        {ref: 'connectionMethodPreviewTitle', selector: '#connectionMethodPreviewTitle'},
        {ref: 'connectionMethodPreviewForm', selector: '#connectionMethodPreviewForm'},
        {ref: 'connectionMethodEditView', selector: '#connectionMethodEdit'},
        {ref: 'connectionMethodEditForm', selector: '#connectionMethodEditForm'},
        {ref: 'connectionStrategyComboBox', selector: '#connectionStrategyComboBox'},
        {ref: 'scheduleField', selector: '#scheduleField'},
        {ref: 'connectionTypeComboBox', selector: '#connectionTypeComboBox'},
        {ref: 'toggleDefaultMenuItem', selector: '#toggleDefaultMenuItem'},
        {ref: 'comWindowStart', selector: '#connectionMethodEdit #comWindowStart'},
        {ref: 'comWindowEnd', selector: '#connectionMethodEdit #comWindowEnd'},
        {ref: 'activateComWindowCheckBox', selector: '#connectionMethodEdit #activateComWindowCheckBox'}
    ],

    init: function () {
        this.control({
            '#connectionmethodsgrid': {
                selectionchange: this.previewConnectionMethod
            },
            '#connectionmethodsgrid menuitem[action = createOutboundConnectionMethod]': {
                click: this.addOutboundConnectionMethodHistory
            },
            '#connectionmethodsgrid menuitem[action = createInboundConnectionMethod]': {
                click: this.addInboundConnectionMethodHistory
            },
            'button[action = createOutboundConnectionMethod]': {
                click: this.addOutboundConnectionMethodHistory
            },
            'button[action = createInboundConnectionMethod]': {
                click: this.addInboundConnectionMethodHistory
            },
            '#connectionmethodsgrid actioncolumn': {
                editConnectionMethod: this.editConnectionMethodHistory,
                deleteConnectionMethod: this.deleteConnectionMethod,
                toggleDefault: this.toggleDefaultConnectionMethod
            },
            '#connectionMethodPreview menuitem[action=editConnectionMethod]': {
                click: this.editConnectionMethodHistoryFromPreview
            },
            '#connectionMethodPreview menuitem[action=toggleDefault]': {
                click: this.toggleDefaultConnectionMethod
            },
            '#connectionMethodPreview menuitem[action=deleteConnectionMethod]': {
                click: this.deleteConnectionMethod
            },
            '#connectionTypeComboBox': {
                select: this.showConnectionTypeProperties
            },
            '#addEditButton[action=addOutboundConnectionMethod]': {
                click: this.addOutboundConnectionMethod
            },
            '#addEditButton[action=editOutboundConnectionMethod]': {
                click: this.editOutboundConnectionMethod
            },
            '#addEditButton[action=addInboundConnectionMethod]': {
                click: this.addInboundConnectionMethod
            },
            '#addEditButton[action=editInboundConnectionMethod]': {
                click: this.editInboundConnectionMethod
            },
            '#connectionStrategyComboBox': {
                select: this.showScheduleField
            },
            '#connectionMethodEdit #activateComWindowCheckBox':{
                change: this.activateComWindow
            }
        });
    },

    showConnectionMethods: function (deviceTypeId, deviceConfigurationId) {
        var me = this;
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigurationId = deviceConfigurationId;
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        var widget = Ext.widget('connectionMethodSetup', {deviceTypeId: deviceTypeId, deviceConfigId: deviceConfigurationId, isDirectlyAddressable: deviceConfig.get('isDirectlyAddressable')});
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        widget.down('#connectionMethodSetupPanel').setTitle(Uni.I18n.translate('connectionmethod.connectionmethods', 'MDC', 'Connection methods'));
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.getConnectionmethodsgrid().getSelectionModel().doSelect(0);
                    }
                });
            }
        });
    },

    previewConnectionMethod: function () {
        var connectionMethod = this.getConnectionmethodsgrid().getSelectionModel().getSelection();
        if (connectionMethod.length == 1) {

            var toggleDefaultMenuItemText =
                connectionMethod[0].get('isDefault') ?
                    Uni.I18n.translate('connectionmethod.unsetAsDefault', 'MDC', 'Remove as default') :
                    Uni.I18n.translate('connectionmethod.setAsDefault', 'MDC', 'Set as default');
            this.getToggleDefaultMenuItem().setText(toggleDefaultMenuItemText);

            this.getConnectionMethodPreviewForm().loadRecord(connectionMethod[0]);
            var connectionMethodName = connectionMethod[0].get('name');
            this.getConnectionMethodPreview().getLayout().setActiveItem(1);
            this.getConnectionMethodPreview().setTitle(connectionMethodName);
            this.getConnectionMethodPreviewForm().loadRecord(connectionMethod[0]);
            this.getConnectionMethodPreview().down('#toggleDefaultMenuItem').setText(connectionMethod[0].get('isDefault') === true ? Uni.I18n.translate('connectionmethod.unsetAsDefault', 'MDC', 'Remove as default') : Uni.I18n.translate('connectionmethod.setAsDefault', 'MDC', 'Set as default'));
            this.getConnectionMethodPreview().down('property-form').loadRecord(connectionMethod[0]);
            if (connectionMethod[0].propertiesStore.data.items.length > 0) {
                this.getConnectionMethodPreview().down('#connectionDetailsTitle').setVisible(true);
            } else {
                this.getConnectionMethodPreview().down('#connectionDetailsTitle').setVisible(false);
            }
        } else {
            this.getConnectionMethodPreview().getLayout().setActiveItem(0);
        }
    },

    addOutboundConnectionMethodHistory: function () {
        location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/connectionmethods/addoutbound';
    },

    addInboundConnectionMethodHistory: function () {
        location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/connectionmethods/addinbound';
    },

    editConnectionMethodHistory: function (record) {
        location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/connectionmethods/' + record.get('id') + '/edit';
    },

    editConnectionMethodHistoryFromPreview: function () {
        this.editConnectionMethodHistory(this.getConnectionMethodPreviewForm().getRecord());
    },

    showAddConnectionMethodView: function (deviceTypeId, deviceConfigId, direction, a, b) {
        var connectionTypesStore = Ext.StoreManager.get('ConnectionTypes');
        this.comPortPoolStore = Ext.StoreManager.get('ComPortPools');
        var connectionStrategiesStore = Ext.StoreManager.get('ConnectionStrategies');
        var me = this;
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigurationId = deviceConfigId;
        var widget = Ext.widget('connectionMethodEdit', {
            edit: false,
            returnLink: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/connectionmethods',
            connectionTypes: connectionTypesStore,
            comPortPools: this.comPortPoolStore,
            connectionStrategies: connectionStrategiesStore,
            direction: direction
        });
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                                connectionStrategiesStore.load({
                                    callback: function () {
                                        connectionTypesStore.getProxy().setExtraParam('protocolId', deviceType.get('deviceProtocolPluggableClassId'));
                                          connectionTypesStore.getProxy().setExtraParam('filter', Ext.encode([
                                            {
                                                property: 'direction',
                                                value: direction
                                            }
                                        ]));

                                        connectionTypesStore.load({
                                            callback: function () {
                                                var deviceTypeName = deviceType.get('name');
                                                var deviceConfigName = deviceConfig.get('name');
                                                var title = direction === 'Outbound' ? Uni.I18n.translate('connectionmethod.addOutboundConnectionMethod', 'MDC', 'Add outbound connection method') : Uni.I18n.translate('connectionmethod.addInboundConnectionMethod', 'MDC', 'Add inbound connection method');
                                                widget.down('#connectionMethodEditAddTitle').update('<h1>' + title + '</h1>');
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

    showScheduleField: function (combobox, objList) {
        if (objList[0].get('connectionStrategy') === 'minimizeConnections') {
            this.getScheduleField().setVisible(true);
            this.getConnectionMethodEditView().down('form').down('#allowSimultaneousConnections').setVisible(false);
        } else {
            this.getScheduleField().setVisible(false);
            this.getConnectionMethodEditView().down('form').down('#allowSimultaneousConnections').setVisible(true);
            this.getScheduleField().clear();
        }
    },

    addOutboundConnectionMethod: function () {
        var values = this.getConnectionMethodEditForm().getValues();
        values.direction = 'Outbound';
        this.addConnectionMethod(values);

    },

    addInboundConnectionMethod: function () {
        var values = this.getConnectionMethodEditForm().getValues();
        values.direction = 'Inbound';
        this.addConnectionMethod(values);
    },

    editOutboundConnectionMethod: function () {
        var values = this.getConnectionMethodEditForm().getValues();
        values.direction = 'Outbound';
        this.editConnectionMethod(values);
    },

    editInboundConnectionMethod: function () {
        var values = this.getConnectionMethodEditForm().getValues();
        values.direction = 'Inbound';
        this.editConnectionMethod(values);
    },

    editConnectionMethod: function (values) {
        var record = this.getConnectionMethodEditForm().getRecord();
        this.updateRecord(record, values);

    },

    addConnectionMethod: function (values) {
        var record = Ext.create(Mdc.model.ConnectionMethod);
        this.updateRecord(record, values);
    },

    updateRecord: function (record, values) {
        var me = this;
        var propertyForm = me.getConnectionMethodEditView().down('property-form');
        if (record) {
            record.set(values);
            if (values.connectionStrategy === 'asSoonAsPossible') {
                record.set('nextExecutionSpecs', null);
            }
            if(!values.hasOwnProperty('comWindowStart')){
                record.set('comWindowStart', 0);
                record.set('comWindowEnd', 0);
            }
            propertyForm.updateRecord(record);
            record.propertiesStore = propertyForm.getRecord().properties();
            record.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigurationId});
            record.save({
                success: function (record) {
                    location.href = '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/connectionmethods';
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getConnectionMethodEditForm().getForm().markInvalid(json.errors);
                        propertyForm.getForm().markInvalid(json.errors);
                    }
                }
            });

        }
    },

    deleteConnectionMethod: function (connectionMethodToDelete) {
        var me = this;
        if (connectionMethodToDelete.hasOwnProperty('action')) {
            connectionMethodToDelete = this.getConnectionmethodsgrid().getSelectionModel().getSelection()[0];
        }
        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('connectionmethod.deleteConnectionMethod', 'MDC', 'This connection method will no longer be available.'),
            title: Uni.I18n.translate('connectionmethod.deleteConnectionMethod.title', 'MDC', 'Remove ') + " '" + connectionMethodToDelete.get('name') + "'?",
            config: {
                connectionMethodToDelete: connectionMethodToDelete,
                me: me
            },
            fn: me.removeConnectionMethod
        });
    },

    removeConnectionMethod: function (btn, text, opt) {
        if (btn === 'confirm') {
            var connectionMethodToDelete = opt.config.connectionMethodToDelete;
            var me = opt.config.me;
            connectionMethodToDelete.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigurationId});
            connectionMethodToDelete.destroy({
                callback: function () {
                    location.href = '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/connectionmethods';
                }
            });

        }
    },

    showConnectionTypeProperties: function (combobox, objList) {
        var objectWithProperties = this.getConnectionTypeComboBox().findRecordByValue(this.getConnectionTypeComboBox().getValue());
        this.comPortPoolStore.clearFilter(true);
        this.comPortPoolStore.getProxy().extraParams = ({compatibleWithConnectionType: objectWithProperties.get('id')});
        this.comPortPoolStore.load();
        var properties = objectWithProperties.properties();
        var form = this.getConnectionMethodEditView().down('property-form');

        if (properties.count()) {
            form.loadRecordAsNotRequired(objectWithProperties);
            form.show();
        } else {
            form.hide();
        }
    },

    showConnectionMethodEditView: function (deviceTypeId, deviceConfigId, connectionMethodId) {
        var connectionTypesStore = Ext.StoreManager.get('ConnectionTypes');
        this.comPortPoolStore = Ext.StoreManager.get('ComPortPools');
        var connectionStrategiesStore = Ext.StoreManager.get('ConnectionStrategies');
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigurationId = deviceConfigId;
        var me = this;

        var model = Ext.ModelManager.getModel('Mdc.model.ConnectionMethod');
        model.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigId});
        model.load(connectionMethodId, {
            success: function (connectionMethod) {
                me.getApplication().fireEvent('loadConnectionMethod', connectionMethod);
                var widget = Ext.widget('connectionMethodEdit', {
                    edit: true,
                    returnLink: me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens(),
                    connectionTypes: connectionTypesStore,
                    comPortPools: me.comPortPoolStore,
                    connectionStrategies: connectionStrategiesStore,
                    direction: connectionMethod.get('direction')
                });

                me.getApplication().fireEvent('changecontentevent', widget);
                widget.setLoading(true);

                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                    success: function (deviceType) {
                        me.getApplication().fireEvent('loadDeviceType', deviceType);
                        var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                        model.getProxy().setExtraParam('deviceType', deviceTypeId);
                        model.load(deviceConfigId, {
                            success: function (deviceConfig) {
                                me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                                connectionTypesStore.getProxy().setExtraParam('protocolId', deviceType.get('deviceProtocolPluggableClassId'));
                                connectionTypesStore.getProxy().setExtraParam('filter', Ext.encode([
                                    {
                                        property: 'direction',
                                        value: connectionMethod.get('direction')
                                    }
                                ]));
                                connectionTypesStore.load({
                                    callback: function () {
                                        connectionStrategiesStore.load({
                                            callback: function () {
                                                me.comPortPoolStore.clearFilter(true);
                                                me.comPortPoolStore.getProxy().extraParams = ({compatibleWithConnectionType: connectionTypesStore.findRecord('name',connectionMethod.get('connectionType')).get('id')});
                                                me.comPortPoolStore.load({

                                                    callback: function () {
                                                        var deviceTypeName = deviceType.get('name');
                                                        var deviceConfigName = deviceConfig.get('name');
                                                        if (connectionMethod.get('connectionStrategy') === 'minimizeConnections') {
                                                            widget.down('form').down('#scheduleField').setVisible(true);
                                                        }
                                                        widget.down('form').loadRecord(connectionMethod);
                                                        var title = Uni.I18n.translate('general.edit', 'MDC', 'Edit') + " '" + connectionMethod.get('name') + "'";
                                                        widget.down('#connectionMethodEditAddTitle').update('<h1>' + title + '</h1>');
                                                        widget.down('form').down('#connectionTypeComboBox').setValue(connectionMethod.get('connectionType'));
                                                        widget.down('form').down('#communicationPortPoolComboBox').setValue(connectionMethod.get('comPortPool'));
                                                        widget.down('form').down('#connectionStrategyComboBox').setValue(connectionMethod.get('connectionStrategy'));
                                                        if(connectionMethod.get('comWindowStart')===0 && connectionMethod.get('comWindowEnd')===0){
                                                            widget.down('form').down('#activateComWindowCheckBox').setValue(false);
                                                        } else {
                                                            widget.down('form').down('#activateComWindowCheckBox').setValue(true);
                                                        }
                                                        var form = widget.down('property-form');
                                                        form.loadRecordAsNotRequired(connectionMethod);
                                                        form.show();
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
            }
        });
    },

    toggleDefaultConnectionMethod: function (connectionMethod) {
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

//        this.getPropertiesController().updatePropertiesWithoutView(connectionMethod);
        connectionMethod.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigurationId});
        connectionMethod.save({
            callback: function () {
                me.getConnectionMethodsOfDeviceConfigurationStore().load();
                me.previewConnectionMethod();
            }
        });
    },

    activateComWindow: function(checkbox,newValue){
        if(newValue){
            this.getComWindowStart().setDisabled(false);
            this.getComWindowEnd().setDisabled(false);
        } else {
            this.getComWindowStart().setDisabled(true);
            this.getComWindowEnd().setDisabled(true);
            this.getComWindowStart().setValue(0);
            this.getComWindowEnd().setValue(0);
        }
    }
});
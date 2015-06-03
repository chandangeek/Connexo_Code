Ext.define('Mdc.controller.setup.ConnectionMethods', {
    extend: 'Ext.app.Controller',
    deviceTypeId: null,
    deviceConfigurationId: null,
    requires: [
        'Mdc.store.ConnectionMethodsOfDeviceConfiguration',
        'Mdc.store.TimeUnits'
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
        'ConnectionStrategies',
        'TimeUnits',
        'ComPortPools',
        'ComPortPoolsWithoutPaging'
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
        {ref: 'scheduleFieldContainer', selector: '#scheduleFieldContainer'},
        {ref: 'connectionTypeComboBox', selector: '#connectionTypeComboBox'},
        {ref: 'toggleDefaultMenuItem', selector: '#toggleDefaultMenuItem'},
        {ref: 'comWindowStart', selector: '#connectionMethodEdit #comWindowStart'},
        {ref: 'comWindowEnd', selector: '#connectionMethodEdit #comWindowEnd'},
        {ref: 'communicationPortPoolComboBox', selector: '#communicationPortPoolComboBox'},
        {ref: 'rescheduleRetryDelayField', selector: '#rescheduleRetryDelay'}

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
            '#connectionMethodEdit #connectionStrategyComboBox': {
                select: this.showScheduleField
            },
            '#connectionMethodEdit #activateConnWindowRadiogroup': {
                change: this.activateConnWindow
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
                        widget.down('#stepsMenu #deviceConfigurationOverviewLink').setText(deviceConfig.get('name'));
                        widget.down('#connectionMethodSetupPanel').setTitle(Uni.I18n.translate('connectionmethod.connectionmethods', 'MDC', 'Connection methods'));
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.getConnectionmethodsgrid().getView().refresh();
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
            if(this.getToggleDefaultMenuItem())
                this.getToggleDefaultMenuItem().setText(toggleDefaultMenuItemText);

            this.getConnectionMethodPreviewForm().loadRecord(connectionMethod[0]);
            var connectionMethodName = connectionMethod[0].get('name');
            this.getConnectionMethodPreview().getLayout().setActiveItem(1);
            this.getConnectionMethodPreview().setTitle(Ext.String.htmlEncode(connectionMethodName));
            var menuItem =  this.getConnectionMethodPreview().down('#toggleDefaultMenuItem');
            menuItem && menuItem.setText(connectionMethod[0].get('isDefault') === true ? Uni.I18n.translate('connectionmethod.unsetAsDefault', 'MDC', 'Remove as default') : Uni.I18n.translate('connectionmethod.setAsDefault', 'MDC', 'Set as default'));
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
        location.href = '#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(this.deviceConfigurationId) + '/connectionmethods/addoutbound';
    },

    addInboundConnectionMethodHistory: function () {
        location.href = '#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(this.deviceConfigurationId) + '/connectionmethods/addinbound';
    },

    editConnectionMethodHistory: function (record) {
        location.href = '#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(this.deviceConfigurationId) + '/connectionmethods/' + encodeURIComponent(record.get('id')) + '/edit';
    },

    editConnectionMethodHistoryFromPreview: function () {
        this.editConnectionMethodHistory(this.getConnectionMethodPreviewForm().getRecord());
    },

    showAddConnectionMethodView: function (deviceTypeId, deviceConfigId, direction, a, b) {
        var connectionTypesStore = Ext.StoreManager.get('ConnectionTypes'),
            timeUnitsStore = Ext.StoreManager.get('TimeUnits');
        this.comPortPoolStore = Ext.StoreManager.get('ComPortPoolsWithoutPaging');
        var connectionStrategiesStore = Ext.StoreManager.get('ConnectionStrategies');
        var me = this;
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigurationId = deviceConfigId;
        var widget = Ext.widget('connectionMethodEdit', {
            edit: false,
            returnLink: '#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(this.deviceConfigurationId) + '/connectionmethods',
            connectionTypes: connectionTypesStore,
            comPortPools: this.comPortPoolStore,
            connectionStrategies: connectionStrategiesStore,
            direction: direction
        });
        me.getCommunicationPortPoolComboBox().disable();
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        this.getComWindowStart().setValue(0);
        this.getComWindowEnd().setValue(0);
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
                                        widget.down('#connectionMethodEditAddTitle').setTitle(title);
                                        widget.setLoading(false);
                                    }
                                });


                            }
                        });
                    }
                });

                me.getRescheduleRetryDelayField().getUnitStore().load({
                    callback: function () {
                        var rescheduleRetryDelayField = me.getRescheduleRetryDelayField();
                        rescheduleRetryDelayField.setValue({
                            count: 5,
                            timeUnit: 'minutes'
                        })
                    }
                });

            }
        });
    },

    showScheduleField: function (combobox, objList) {
        this.getScheduleField().clear();
        if (objList[0].get('connectionStrategy') === 'minimizeConnections') {
            this.getScheduleFieldContainer().setVisible(true);
            this.getScheduleField().setValue({
                every: {
                    count: 5,
                    timeUnit: 'minutes'
                },
                offset: {
                    count: 0,
                    timeUnit: 'seconds'
                }
            });
            this.getConnectionMethodEditView().down('form').down('#allowSimultaneousConnections').setVisible(false);
        } else {
            this.getScheduleFieldContainer().setVisible(false);
            this.getConnectionMethodEditView().down('form').down('#allowSimultaneousConnections').setVisible(true);
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
        this.updateRecord(record, values, false);

    },

    addConnectionMethod: function (values) {
        var record = Ext.create(Mdc.model.ConnectionMethod);
        this.updateRecord(record, values, true);
    },

    updateRecord: function (record, values, isNewRecord) {
        var me = this;
        var propertyForm = me.getConnectionMethodEditView().down('property-form');
        if (record) {
            if (propertyForm.down('#connectionTimeoutnumberfield')) {
                propertyForm.down('#connectionTimeoutnumberfield').clearInvalid();
                propertyForm.down('#connectionTimeoutcombobox').clearInvalid();
            }
            record.beginEdit();
            record.set(values);
            if (values.connectionStrategy === 'asSoonAsPossible') {
                record.set('temporalExpression', null);
            }
            if (!values.hasOwnProperty('comWindowStart')) {
                record.set('comWindowStart', 0);
                record.set('comWindowEnd', 0);
            }
            propertyForm.updateRecord(record);
            if (typeof propertyForm.getRecord() !== 'undefined') {
                record.propertiesStore = propertyForm.getRecord().properties();
            }
            record.endEdit();
            record.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigurationId});
            record.save({
                success: function (record) {
                    location.href = '#/administration/devicetypes/' + encodeURIComponent(me.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(me.deviceConfigurationId) + '/connectionmethods';
                    if (isNewRecord === true) {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('connectionmethod.acknowlegment.add', 'MDC', 'Connection method added'));
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('connectionmethod.acknowlegment.save', 'MDC', 'Connection method saved'));
                    }
              //      me.showConnectionMethods(me.deviceTypeId, me.deviceConfigurationId);
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getConnectionMethodEditForm().getForm().markInvalid(json.errors);
                        propertyForm.getForm().markInvalid(json.errors);
                        Ext.Array.each(json.errors, function (item) {
                            if (item.id.indexOf("timeCount") !== -1) {
                                propertyForm.down('#connectionTimeoutnumberfield').markInvalid(item.msg);
                            }
                            if (item.id.indexOf("timeUnit") !== -1) {
                                propertyForm.down('#connectionTimeoutcombobox').markInvalid(item.msg);
                            }
                        });
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
                success: function () {
                    location.href = '#/administration/devicetypes/' + encodeURIComponent(me.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(me.deviceConfigurationId) + '/connectionmethods';
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('connectionmethod.acknowlegment.remove', 'MDC', 'Connection method removed'));
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

        form.loadRecordAsNotRequired(objectWithProperties);
        if (properties.count()) {
            form.show();
        } else {
            form.hide();
        }
        this.getCommunicationPortPoolComboBox().enable();
    },

    showConnectionMethodEditView: function (deviceTypeId, deviceConfigId, connectionMethodId) {
        var connectionTypesStore = Ext.StoreManager.get('ConnectionTypes');
        this.comPortPoolStore = Ext.StoreManager.get('ComPortPoolsWithoutPaging');
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
                                                me.comPortPoolStore.getProxy().extraParams = ({compatibleWithConnectionType: connectionTypesStore.findRecord('name', connectionMethod.get('connectionTypePluggableClass')).get('id')});
                                                me.comPortPoolStore.load({

                                                    callback: function () {
                                                        var deviceTypeName = deviceType.get('name');
                                                        var deviceConfigName = deviceConfig.get('name');
                                                        if (connectionMethod.get('connectionStrategy') === 'minimizeConnections') {
                                                            widget.down('form').down('#scheduleFieldContainer').setVisible(true);
                                                        }
                                                        widget.down('form').loadRecord(connectionMethod);
                                                        var title = Uni.I18n.translate('general.edit', 'MDC', 'Edit') + " '" + connectionMethod.get('name') + "'";
                                                        widget.down('#connectionMethodEditAddTitle').setTitle(title);
                                                        widget.down('form').down('#connectionTypeComboBox').setValue(connectionMethod.get('connectionTypePluggableClass'));
                                                        me.getConnectionTypeComboBox().disable();
                                                        widget.down('form').down('#communicationPortPoolComboBox').setValue(connectionMethod.get('comPortPool'));
                                                        widget.down('form').down('#connectionStrategyComboBox').setValue(connectionMethod.get('connectionStrategy'));
                                                        if (connectionMethod.get('comWindowStart') || connectionMethod.get('comWindowEnd')) {
                                                            widget.down('form').down('#activateConnWindowRadiogroup').items.items[1].setValue(true);
                                                        } else {
                                                            widget.down('form').down('#activateConnWindowRadiogroup').items.items[0].setValue(true);
                                                        }
                                                        var form = widget.down('property-form');
                                                        form.loadRecordAsNotRequired(connectionMethod);
                                                        me.getRescheduleRetryDelayField().getUnitStore().load({
                                                            callback: function () {
                                                                var rescheduleRetryDelayField = me.getRescheduleRetryDelayField();
                                                                rescheduleRetryDelayField.setValue(connectionMethod.get('rescheduleRetryDelay'));
                                                            }
                                                        });
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
            connectionMethod.set('temporalExpression', null);
        }

//        this.getPropertiesController().updatePropertiesWithoutView(connectionMethod);
        connectionMethod.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigurationId});
        connectionMethod.save({
            success: function () {
                if (connectionMethod.get('isDefault') === true) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('connectionmethod.acknowlegment.setAsDefault', 'MDC', 'Connection method set as default'));
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('connectionmethod.acknowlegment.removeDefault', 'MDC', 'Connection method removed as default'));
                }
            },
            callback: function () {
                me.getConnectionMethodsOfDeviceConfigurationStore().load();
                me.previewConnectionMethod();
            }
        });
    },

    activateConnWindow: function (radiogroup, value) {
        if (value.enableConnWindow) {
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
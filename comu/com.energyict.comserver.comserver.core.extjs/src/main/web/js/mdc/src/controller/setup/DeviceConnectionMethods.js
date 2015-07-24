Ext.define('Mdc.controller.setup.DeviceConnectionMethods', {
    extend: 'Ext.app.Controller',
    deviceTypeId: null,
    deviceConfigurationId: null,
    requires: [
        'Mdc.store.ConnectionMethodsOfDevice',
        'Mdc.store.ConnectionMethodsOfDeviceConfigurationCombo',
        'Mdc.store.ConnectionStrategies',
        'Mdc.store.ComPortPools',
        'Mdc.store.ConnectionTypes'
    ],

    views: [
        'setup.deviceconnectionmethod.DeviceConnectionMethodSetup',
        'setup.deviceconnectionmethod.DeviceConnectionMethodsGrid',
        'setup.deviceconnectionmethod.DeviceConnectionMethodPreview',
        'setup.deviceconnectionmethod.DeviceConnectionMethodEdit'
    ],

    stores: [
        'ConnectionMethodsOfDevice',
        'ConnectionStrategies',
        'ConnectionMethodsOfDeviceConfigurationCombo',
        'ComPortPools',
        'ConnectionTypes'
    ],

    refs: [
        {ref: 'deviceConnectionMethodsGrid', selector: '#deviceconnectionmethodsgrid'},
        {ref: 'deviceConnectionMethodPreview', selector: '#deviceConnectionMethodPreview'},
        {ref: 'deviceConnectionMethodPreviewTitle', selector: '#deviceConnectionMethodPreviewTitle'},
        {ref: 'deviceConnectionMethodPreviewForm', selector: '#deviceConnectionMethodPreviewForm'},
        {ref: 'deviceConnectionMethodEditView', selector: '#deviceConnectionMethodEdit'},
        {ref: 'deviceConnectionMethodEditForm', selector: '#deviceConnectionMethodEditForm'},
        {ref: 'deviceConnectionMethodComboBox', selector: '#deviceConnectionMethodComboBox'},
        {ref: 'connectionStrategyComboBox', selector: '#connectionStrategyComboBox'},
        {ref: 'scheduleField', selector: '#scheduleField'},
        {ref: 'scheduleFieldContainer', selector: '#scheduleFieldContainer'},
        {ref: 'toggleDefaultMenuItem', selector: '#toggleDefaultMenuItem'},
        {ref: 'toggleActiveMenuItem', selector: '#toggleActiveMenuItem'},
        {ref: 'comWindowStart', selector: '#deviceConnectionMethodEdit #comWindowStart'},
        {ref: 'comWindowEnd', selector: '#deviceConnectionMethodEdit #comWindowEnd'},
        {ref: 'activateConnWindowRadiogroup', selector: '#activateConnWindowRadiogroup'},
        {ref: 'propertyForm', selector: '#propertyForm'}

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
                toggleActive: this.toggleActiveDeviceconnectionMethod,
                viewConnectionHistory: this.viewConnectionHistory
            },
            '#deviceConnectionMethodPreview menuitem[action=editDeviceConnectionMethod]': {
                click: this.editDeviceConnectionMethodHistoryFromPreview
            },
            '#deviceConnectionMethodPreview menuitem[action=toggleDefault]': {
                click: this.toggleDefaultDeviceConnectionMethod
            },
            '#deviceConnectionMethodPreview menuitem[action=toggleActive]': {
                click: this.toggleActiveDeviceconnectionMethod
            },
            '#deviceConnectionMethodPreview menuitem[action=deleteDeviceConnectionMethod]': {
                click: this.deleteDeviceConnectionMethod
            },
            '#deviceConnectionMethodEdit #deviceConnectionMethodComboBox': {
                select: this.selectDeviceConfigConnectionMethod
            },
            '#deviceConnectionMethodEdit #addEditButton[action=addDeviceOutboundConnectionMethod]': {
                click: this.addDeviceOutboundConnectionMethod
            },
            '#deviceConnectionMethodEdit #addEditButton[action=editDeviceOutboundConnectionMethod]': {
                click: this.editDeviceOutboundConnectionMethod
            },
            '#deviceConnectionMethodEdit #addEditButton[action=addDeviceInboundConnectionMethod]': {
                click: this.addDeviceInboundConnectionMethod
            },
            '#deviceConnectionMethodEdit #addEditButton[action=editDeviceInboundConnectionMethod]': {
                click: this.editDeviceInboundConnectionMethod
            },
            '#deviceConnectionMethodEdit #connectionStrategyComboBox': {
                select: this.showScheduleField
            },
            '#deviceConnectionMethodEdit #activateConnWindowRadiogroup': {
                change: this.activateConnWindow
            },
            '#deviceConnectionMethodPreview menuitem[action=viewConnectionHistory]': {
                click: this.viewConnectionHistoryFromPreview
            }
        });
    },

    showDeviceConnectionMethods: function (mrid) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            connectionStrategiesStore = Ext.StoreManager.get('ConnectionStrategies');

        this.mrid = mrid;

        viewport.setLoading();
        connectionStrategiesStore.load({
            callback: function () {
                Ext.ModelManager.getModel('Mdc.model.Device').load(mrid, {
                    success: function (device) {
                        var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                        model.getProxy().setExtraParam('deviceType', device.get('deviceTypeId'));
                        model.load(device.get('deviceConfigurationId'), {
                            success: function (deviceConfig) {
                                var widget = Ext.widget('deviceConnectionMethodSetup', {device: device, isDirectlyAddressable: deviceConfig.get('isDirectlyAddressable')});
                                me.getApplication().fireEvent('changecontentevent', widget);
                                me.getApplication().fireEvent('loadDevice', device);
                                viewport.setLoading(false);
                            }
                        });
                    }
                });
            }
        });
    },

    previewDeviceConnectionMethod: function () {
        var connectionMethod = this.getDeviceConnectionMethodsGrid().getSelectionModel().getSelection();
        Ext.suspendLayouts();
        if (connectionMethod.length == 1) {
            if (!!this.getToggleDefaultMenuItem()) {
                this.getToggleDefaultMenuItem().setText(connectionMethod[0].get('isDefault') === true ? Uni.I18n.translate('deviceconnectionmethod.unsetAsDefault', 'MDC', 'Remove as default') : Uni.I18n.translate('deviceconnectionmethod.setAsDefault', 'MDC', 'Set as default'));
                this.getToggleActiveMenuItem().setText(connectionMethod[0].get('status') === 'connectionTaskStatusInActive' ? Uni.I18n.translate('deviceconnectionmethod.activate', 'MDC', 'Activate') : Uni.I18n.translate('deviceconnectionmethod.deActivate', 'MDC', 'Deactivate'));
                if (connectionMethod[0].get('status') === 'connectionTaskStatusIncomplete') {
                    this.getToggleActiveMenuItem().setVisible(false);
                } else {
                    this.getToggleActiveMenuItem().setVisible(true);
                }
            }
            this.getDeviceConnectionMethodPreviewForm().loadRecord(connectionMethod[0]);
            var connectionMethodName = connectionMethod[0].get('name');
            this.getDeviceConnectionMethodPreview().getLayout().setActiveItem(1);
            this.getDeviceConnectionMethodPreview().setTitle(connectionMethodName);
            !!this.getDeviceConnectionMethodPreview().down('#toggleDefaultMenuItem') && this.getDeviceConnectionMethodPreview().down('#toggleDefaultMenuItem').setText(connectionMethod[0].get('isDefault') === true ? Uni.I18n.translate('deviceconnectionmethod.unsetAsDefault', 'MDC', 'Remove as default') : Uni.I18n.translate('deviceconnectionmethod.setAsDefault', 'MDC', 'Set as default'));
            if (!!this.getDeviceConnectionMethodPreview().down('#toggleActiveMenuItem')) {
                this.getDeviceConnectionMethodPreview().down('#toggleActiveMenuItem').setText(connectionMethod[0].get('status') === 'connectionTaskStatusInActive' || connectionMethod[0].get('status') === 'connectionTaskStatusIncomplete' ? Uni.I18n.translate('deviceconnectionmethod.activate', 'MDC', 'Activate') : Uni.I18n.translate('deviceconnectionmethod.deActivate', 'MDC', 'Deactivate'));
                if (connectionMethod[0].get('status') === 'connectionTaskStatusIncomplete') {
                    this.getDeviceConnectionMethodPreview().down('#toggleActiveMenuItem').hidden = true;
                } else {
                    this.getDeviceConnectionMethodPreview().down('#toggleActiveMenuItem').hidden = false;
                }
            }
            if (connectionMethod[0].propertiesStore.data.items.length > 0) {
                this.getDeviceConnectionMethodPreview().down('#connectionDetailsTitle').setVisible(true);
            } else {
                this.getDeviceConnectionMethodPreview().down('#connectionDetailsTitle').setVisible(false);
            }
            this.getPropertyForm().loadRecord(connectionMethod[0]);
        } else {
            this.getDeviceConnectionMethodPreview().getLayout().setActiveItem(0);
        }
        Ext.resumeLayouts(true);
    },

    addOutboundConnectionMethodHistory: function () {
        location.href = '#/devices/' + encodeURIComponent(this.mrid) + '/connectionmethods/addoutbound';
    },

    addInboundConnectionMethodHistory: function () {
        location.href = '#/devices/' + encodeURIComponent(this.mrid) + '/connectionmethods/addinbound';
    },

    editDeviceConnectionMethodHistory: function (record) {
        location.href = '#/devices/' + encodeURIComponent(this.mrid) + '/connectionmethods/' + encodeURIComponent(record.get('id')) + '/edit';
    },

    editDeviceConnectionMethodHistoryFromPreview: function () {
        this.editDeviceConnectionMethodHistory(this.getDeviceConnectionMethodPreviewForm().getRecord());
    },

    viewConnectionHistory: function (record) {
        location.href = '#/devices/' + encodeURIComponent(this.mrid) + '/connectionmethods/' + encodeURIComponent(record.get('id')) + '/history';
    },

    viewConnectionHistoryFromPreview: function () {
        this.viewConnectionHistory(this.getDeviceConnectionMethodPreviewForm().getRecord());
    },

    showAddDeviceConnectionMethodView: function (mrid, direction) {
        var me = this,
            deviceModel = Ext.ModelManager.getModel('Mdc.model.Device'),
            connectionMethodsStore = Ext.StoreManager.get('ConnectionMethodsOfDeviceConfigurationCombo'),
            connectionStrategiesStore = Ext.StoreManager.get('ConnectionStrategies');

        me.mrid = mrid;
        me.comPortPoolStore = Ext.StoreManager.get('ComPortPools');

        deviceModel.load(mrid, {
            success: function (device) {
                var widget = Ext.widget('deviceConnectionMethodEdit', {
                    edit: false,
                    returnLink: '#/devices/' + encodeURIComponent(me.mrid) + '/connectionmethods',
                    connectionMethods: connectionMethodsStore,
                    comPortPools: me.comPortPoolStore,
                    connectionStrategies: connectionStrategiesStore,
                    direction: direction,
                    device: device
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.setLoading(true);
                me.getApplication().fireEvent('loadDevice', device);
                connectionMethodsStore.getProxy().extraParams = ({deviceType: device.get('deviceTypeId'), deviceConfig: device.get('deviceConfigurationId')});
                connectionMethodsStore.getProxy().setExtraParam('available', true);
                connectionMethodsStore.getProxy().setExtraParam('mrId', encodeURIComponent(mrid));
                connectionMethodsStore.clearFilter(true);
                connectionMethodsStore.filter('direction', direction);
                connectionMethodsStore.load({
                    callback: function () {
                        connectionStrategiesStore.load({
                            callback: function () {
                                var title = direction === 'Outbound' ? Uni.I18n.translate('deviceconnectionmethod.addOutboundConnectionMethod', 'MDC', 'Add outbound connection method') : Uni.I18n.translate('deviceconnectionmethod.addInboundConnectionMethod', 'MDC', 'Add inbound connection method');
                                widget.down('#deviceConnectionMethodEditAddTitle').setTitle(title);
                                widget.setLoading(false);
                            }
                        });

                    }
                });
            }
        });
    },

    showProperties: function (connectionMethod) {
        if (connectionMethod.propertiesStore.data.items.length > 0) {
            this.getDeviceConnectionMethodEditView().down('#connectionDetailsTitle').setVisible(true);
        } else {
            this.getDeviceConnectionMethodEditView().down('#connectionDetailsTitle').setVisible(false);
        }
        this.getDeviceConnectionMethodEditView().down('property-form').loadRecord(connectionMethod);
    },

    showPropertiesAsInherited: function (connectionMethod) {
        if (connectionMethod.propertiesStore.data.items.length > 0) {
            this.getDeviceConnectionMethodEditView().down('#connectionDetailsTitle').setVisible(true);
        } else {
            this.getDeviceConnectionMethodEditView().down('#connectionDetailsTitle').setVisible(false);
        }
        this.getDeviceConnectionMethodEditView().down('property-form').loadRecord(connectionMethod);
        this.getDeviceConnectionMethodEditView().down('property-form').useInheritedValues();

        //this.getPropertiesController().showPropertiesAsInherited(connectionMethod, this.getDeviceConnectionMethodEditView(), false);
    },

    selectDeviceConfigConnectionMethod: function (comboBox) {
        this.getDeviceConnectionMethodEditForm().down('#deviceConnectionMethodComboBox').clearInvalid();
        var connectionMethod = comboBox.findRecordByValue(comboBox.getValue());
        this.comPortPoolStore.clearFilter(true);
        var connectionTypesStore = Ext.StoreManager.get('ConnectionTypes');
        this.comPortPoolStore.getProxy().extraParams = ({compatibleWithConnectionTask: connectionMethod.get('id')});
        this.comPortPoolStore.load();
        this.getDeviceConnectionMethodEditView().down('#communicationPortPoolComboBox').setDisabled(false);
        this.getDeviceConnectionMethodEditView().down('#connectionStrategyComboBox').setDisabled(false);
        this.getDeviceConnectionMethodEditView().down('#scheduleFieldContainer').setDisabled(false);
        this.getDeviceConnectionMethodEditView().down('#activeRadioGroup').setDisabled(false);
        this.getDeviceConnectionMethodEditView().down('#comWindowField').setDisabled(false);
        // this.getDeviceConnectionMethodEditView().down('#rescheduleRetryDelay').setDisabled(false);
        this.getDeviceConnectionMethodEditView().down('#allowSimultaneousConnections').setDisabled(false);
        if (connectionMethod.get('connectionStrategy') === 'minimizeConnections') {
            this.getDeviceConnectionMethodEditView().down('form').down('#scheduleFieldContainer').setVisible(true);
            if (connectionMethod.get('temporalExpression')) {
                this.getDeviceConnectionMethodEditView().down('#scheduleField').setValue(connectionMethod.get('temporalExpression'));
            }
            this.getDeviceConnectionMethodEditView().down('form').down('#allowSimultaneousConnections').setVisible(false);
        }
        if (connectionMethod.get('comWindowStart') || connectionMethod.get('comWindowEnd')) {
            this.getActivateConnWindowRadiogroup().items.items[1].setValue(true);
        } else {
            this.getActivateConnWindowRadiogroup().items.items[0].setValue(true);
        }
        this.getDeviceConnectionMethodEditView().down('form').loadRecord(connectionMethod);
        this.getDeviceConnectionMethodEditView().down('form').down('#communicationPortPoolComboBox').setValue(connectionMethod.get('comPortPool'));
        this.getDeviceConnectionMethodEditView().down('form').down('#connectionStrategyComboBox').setValue(connectionMethod.get('connectionStrategy'));
        this.showPropertiesAsInherited(connectionMethod);
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
            this.getDeviceConnectionMethodEditView().down('form').down('#allowSimultaneousConnections').setVisible(false);
        } else {
            this.getScheduleFieldContainer().setVisible(false);
            this.getDeviceConnectionMethodEditView().down('form').down('#allowSimultaneousConnections').setVisible(true);
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
        this.updateRecord(record, values, false);

    },

    addDeviceConnectionMethod: function (values) {
        if (values.name === "") {
            this.getDeviceConnectionMethodEditForm().down('#deviceConnectionMethodComboBox').markInvalid(
                Uni.I18n.translate('deviceconnectionmethod.configurationConnectionMethodRequired', 'UNI', 'Configuration connection method is required')
            );
        } else {
            var record = Ext.create(Mdc.model.DeviceConnectionMethod);
            this.updateRecord(record, values, true);
        }
    },

    updateRecord: function (record, values, isNewRecord) {
        var me = this;
        if (record) {
            record.beginEdit();
            record.set(values);
            if (values.connectionStrategy === 'asSoonAsPossible') {
                record.set('nextExecutionSpecs', null);
            }
            if (!values.hasOwnProperty('comWindowStart')) {
                record.set('comWindowStart', 0);
                record.set('comWindowEnd', 0);
            }
            record.endEdit();
            var propertyForm = me.getDeviceConnectionMethodEditView().down('property-form');
            if (propertyForm) {
                propertyForm.updateRecord(record);
                record.propertiesStore = propertyForm.getRecord().properties();
            }

            this.saveRecord(record, isNewRecord);

        }
    },

    saveRecord: function (record, isNewRecord) {
        var me = this;
        if (me.getDeviceConnectionMethodEditView().down('property-form').down('#connectionTimeoutnumberfield')) {
            me.getDeviceConnectionMethodEditView().down('property-form').down('#connectionTimeoutnumberfield').clearInvalid();
            me.getDeviceConnectionMethodEditView().down('property-form').down('#connectionTimeoutcombobox').clearInvalid();
        }
        record.getProxy().extraParams = ({mrid: me.mrid});
        record.save({
            success: function (record) {
                location.href = '#/devices/' + encodeURIComponent(me.mrid) + '/connectionmethods/';
                if (isNewRecord === true) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceconnectionmethod.saveSuccess.msg.add', 'MDC', 'Connection method added'));
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceconnectionmethod.saveSuccess.msg.edit', 'MDC', 'Connection method saved'));
                }
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    if (json.errors.every(function (error) {
                        return error.id === 'status'
                    })) {
                        Ext.create('Uni.view.window.Confirmation', {
                            confirmText: Uni.I18n.translate('general.yes', 'UNI', 'Yes'),
                            cancelText: Uni.I18n.translate('general.no', 'UNI', 'No')
                        }).show({
                                msg: Uni.I18n.translate('deviceconnectionmethod.createIncomplete.msg', 'MDC', 'Are you sure you want to add this incomplete connection method?'),
                                title: Uni.I18n.translate('deviceconnectionmethod.createIncomplete.title', 'MDC', 'One or more required attributes are missing'),
                                config: {
                                    me: me,
                                    record: record,
                                    isNewRecord: isNewRecord
                                },

                                fn: me.saveAsIncomplete
                            });
                    } else {
                        me.getDeviceConnectionMethodEditForm().getForm().markInvalid(json.errors);
                        me.getDeviceConnectionMethodEditView().down('property-form').getForm().markInvalid(json.errors);
                        Ext.Array.each(json.errors, function (item) {
                            if (item.id.indexOf("timeCount") !== -1) {
                                me.getDeviceConnectionMethodEditView().down('property-form').down('#connectionTimeoutnumberfield').markInvalid(item.msg);
                            }
                            if (item.id.indexOf("timeUnit") !== -1) {
                                me.getDeviceConnectionMethodEditView().down('property-form').down('#connectionTimeoutcombobox').markInvalid(item.msg);
                            }
                        });
                    }
                }
            }
        });
    },

    saveAsIncomplete: function (btn, text, opt) {
        if (btn === 'confirm') {
            var record = opt.config.record;
            record.set('status', 'connectionTaskStatusIncomplete');
            var isNewRecord = opt.config.isNewRecord;
            opt.config.me.saveRecord(record, isNewRecord);
        }
    },

    deleteDeviceConnectionMethod: function (connectionMethodToDelete) {
        var me = this;
        if (connectionMethodToDelete.hasOwnProperty('action')) {
            connectionMethodToDelete = this.getDeviceConnectionMethodsGrid().getSelectionModel().getSelection()[0];
        }
        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('deviceconnectionmethod.deleteConnectionMethod', 'MDC', 'This connection method will no longer be available.'),
            title: Uni.I18n.translate('deviceconnectionmethod.deleteConnectionMethod.title', 'MDC', 'Remove ') + ' ' + connectionMethodToDelete.get('name') + '?',
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
                success: function () {
                    location.href = '#/devices/' + encodeURIComponent(me.mrid) + '/connectionmethods';
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceconnectionmethod.saveSuccess.msg.remove', 'MDC', 'Connection method removed'));
                }
            });

        }
    },

    showDeviceConnectionMethodEditView: function (mrid, connectionMethodId) {
        this.mrid = mrid;
        var me = this;
        var deviceModel = Ext.ModelManager.getModel('Mdc.model.Device');
        var connectionMethodModel = Ext.ModelManager.getModel('Mdc.model.DeviceConnectionMethod');
        var connectionMethodsStore = Ext.getStore('ConnectionMethodsOfDeviceConfigurationCombo');
        this.comPortPoolStore = Ext.getStore('ComPortPools');
        var connectionStrategiesStore = Ext.StoreManager.get('ConnectionStrategies');

        deviceModel.load(mrid, {
            success: function (device) {
                connectionMethodModel.getProxy().setExtraParam('mrid', encodeURIComponent(mrid));
                connectionMethodModel.load(connectionMethodId, {
                    success: function (connectionMethod) {
                        me.getApplication().fireEvent('loadDevice', device);
                        me.getApplication().fireEvent('loadConnectionMethod', connectionMethod);
                        var widget = Ext.widget('deviceConnectionMethodEdit', {
                            edit: true,
                            returnLink: '#/devices/' + encodeURIComponent(me.mrid) + '/connectionmethods',
                            connectionMethods: connectionMethodsStore,
                            comPortPools: me.comPortPoolStore,
                            connectionStrategies: connectionStrategiesStore,
                            direction: connectionMethod.get('direction'),
                            device: device
                        });
                        me.getApplication().fireEvent('changecontentevent', widget);
                        widget.setLoading(true);
                        connectionMethodsStore.getProxy().extraParams = ({deviceType: device.get('deviceTypeId'), deviceConfig: device.get('deviceConfigurationId')});
                        connectionMethodsStore.clearFilter(true);
                        connectionMethodsStore.filter('direction', connectionMethod.get('direction'));
                        connectionMethodsStore.load({
                            callback: function () {
                                me.comPortPoolStore.clearFilter(true);
                                var connectionTypesStore = Ext.StoreManager.get('ConnectionTypes');
                                me.comPortPoolStore.getProxy().extraParams = ({compatibleWithConnectionTask: connectionMethodsStore.findRecord('name', connectionMethod.get('name')).get('id')});
                                me.comPortPoolStore.load({
                                    callback: function () {
                                        connectionStrategiesStore.load({
                                            callback: function () {
                                                var title = Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' \'' + connectionMethod.get('name') + '\'';
                                                widget.down('#deviceConnectionMethodEditAddTitle').setTitle(title);
                                                me.getDeviceConnectionMethodEditView().down('#communicationPortPoolComboBox').setDisabled(false);
                                                me.getDeviceConnectionMethodEditView().down('#allowSimultaneousConnections').setDisabled(false);
                                                me.getDeviceConnectionMethodEditView().down('#connectionStrategyComboBox').setDisabled(false);
                                                me.getDeviceConnectionMethodEditView().down('#scheduleFieldContainer').setDisabled(false);
                                                me.getDeviceConnectionMethodEditView().down('#activeRadioGroup').setDisabled(false);
                                                me.getDeviceConnectionMethodEditView().down('#comWindowField').setDisabled(false);
                                                me.getDeviceConnectionMethodComboBox().setDisabled(true);
                                                me.getDeviceConnectionMethodEditView().down('form').loadRecord(connectionMethod);
                                                if (connectionMethod.get('connectionStrategy') === 'minimizeConnections') {
                                                    widget.down('form').down('#scheduleFieldContainer').setVisible(true);
                                                    me.getDeviceConnectionMethodEditView().down('#allowSimultaneousConnections').setVisible(false);
                                                }
                                                if (connectionMethod.get('comWindowStart') || connectionMethod.get('comWindowEnd')) {
                                                    me.getActivateConnWindowRadiogroup().items.items[1].setValue(true);
                                                } else {
                                                    me.getActivateConnWindowRadiogroup().items.items[0].setValue(true);
                                                }
                                                widget.down('form').down('#communicationPortPoolComboBox').setValue(connectionMethod.get('comPortPool'));
                                                widget.down('form').down('#connectionStrategyComboBox').setValue(connectionMethod.get('connectionStrategy'));
                                                me.showProperties(connectionMethod);
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
            connectionMethod = this.getDeviceConnectionMethodsGrid().getSelectionModel().getSelection()[0];
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
        connectionMethod.getProxy().extraParams = ({mrid: this.mrid});
        connectionMethod.save({
            success: function () {
                if (connectionMethod.get('isDefault') === true) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceconnectionmethod.acknowlegment.setAsDefault', 'MDC', 'Connection method set as default'));
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceconnectionmethod.acknowlegment.removeDefault', 'MDC', 'Connection method removed as default'));
                }
            },
            callback: function () {
                me.getConnectionMethodsOfDeviceStore().load();
                me.previewDeviceConnectionMethod();
            }
        });
    },

    toggleActiveDeviceconnectionMethod: function (connectionMethod) {
        var me = this;
        if (connectionMethod.hasOwnProperty('action')) {
            connectionMethod = this.getDeviceConnectionMethodsGrid().getSelectionModel().getSelection()[0];
        }
        if (connectionMethod.get('status') === 'connectionTaskStatusIncomplete' || connectionMethod.get('status') === 'connectionTaskStatusInActive') {
            connectionMethod.set('status', 'connectionTaskStatusActive');
        } else {
            connectionMethod.set('status', 'connectionTaskStatusInActive');
        }
        if (connectionMethod.get('connectionStrategy') === 'asSoonAsPossible' || connectionMethod.get('direction') === 'Inbound') {
            connectionMethod.set('nextExecutionSpecs', null);
        }
//        this.getPropertiesController().updatePropertiesWithoutView(connectionMethod);
        //connectionMethod.propertiesStore = this.getPropertiesController().updateProperties();
        connectionMethod.getProxy().extraParams = ({mrid: this.mrid});
        connectionMethod.save({
            success: function () {
                if (connectionMethod.get('status') === 'connectionTaskStatusActive') {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceconnectionmethod.acknowlegment.activated', 'MDC', 'Connection method activated'));
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceconnectionmethod.acknowlegment.deactivated', 'MDC', 'Connection method deactivated'));
                }
            },
            callback: function () {
                me.getConnectionMethodsOfDeviceStore().load();
                me.previewDeviceConnectionMethod();
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
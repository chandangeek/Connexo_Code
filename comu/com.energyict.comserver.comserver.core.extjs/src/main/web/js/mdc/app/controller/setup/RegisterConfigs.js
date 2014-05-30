Ext.define('Mdc.controller.setup.RegisterConfigs', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.registerconfig.RegisterConfigSetup',
        'setup.registerconfig.RegisterConfigGrid',
        'setup.registerconfig.RegisterConfigPreview',
        'setup.registerconfig.RegisterConfigEdit'
    ],

    requires: [
        'Mdc.store.RegisterConfigsOfDeviceConfig',
        'Mdc.store.AvailableRegisterTypesForDeviceConfiguration',
        'Mdc.store.RegisterTypesOfDevicetype'
    ],

    stores: [
        'RegisterConfigsOfDeviceConfig',
        'RegisterTypesOfDevicetype',
        'AvailableRegisterTypesForDeviceConfiguration'
    ],

    refs: [
        {ref: 'registerConfigGrid', selector: '#registerconfiggrid'},
        {ref: 'registerConfigPreviewForm', selector: '#registerConfigPreviewForm'},
        {ref: 'registerConfigPreview', selector: '#registerConfigPreview'},
        {ref: 'registerConfigPreviewTitle', selector: '#registerConfigPreviewTitle'},
        {ref: 'readingTypeDetailsForm', selector: '#readingTypeDetailsForm'},
        {ref: 'registerConfigEditForm', selector: '#registerConfigEditForm'},
        {ref: 'createRegisterConfigBtn', selector: '#createRegisterConfigBtn'},
        {ref: 'previewMrId', selector: '#preview_mrid'},
        {ref: 'readingTypeContainer', selector: '#readingTypeContainer'},
        {ref: 'overflowValueInfo', selector: '#overflowValueInfo'},
        {ref: 'numberOfDigits', selector: '#numberOfDigits'}

    ],

    deviceTypeId: null,
    deviceConfigId: null,

    init: function () {

        this.control({
            '#registerconfiggrid': {
                selectionchange: this.previewRegisterConfig
            },
            '#registerConfigSetup button[action = createRegisterConfig]': {
                click: this.createRegisterConfigurationHistory
            },
            '#registerconfiggrid actioncolumn': {
                showReadingTypeInfo: this.showReadingType,
                editItem: this.editRegisterConfigurationHistory,
                deleteItem: this.deleteRegisterConfiguration
            },
            '#registerConfigPreviewForm button[action = showReadingTypeInfo]': {
                showReadingTypeInfo: this.showReadingType
            },
            '#registerConfigEditForm button[action = showReadingTypeInfo]': {
                showReadingTypeInfo: this.showReadingType
            },
            '#registerConfigEditForm combobox': {
                change: this.changeRegisterType
            },
            '#createEditButton[action=createRegisterConfiguration]': {
                click: this.createRegisterConfiguration
            },
            '#registerConfigPreview menuitem[action=deleteRegisterConfig]': {
                click: this.deleteRegisterConfigurationFromPreview
            },
            '#registerConfigEditForm numberfield[name=numberOfDigits]': {
                change: this.changeNumberOfDigits
            },
            '#registerConfigPreview menuitem[action=editRegisterConfig]': {
                click: this.editRegisterConfigurationHistoryFromPreview
            },
            '#createEditButton[action=editRegisterConfiguration]': {
                click: this.editRegisterConfiguration
            }
        });
    },

    editRegisterConfigurationHistory: function (record) {
        location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigId + '/registerconfigurations/' + record.get('id') + '/edit';
    },

    editRegisterConfigurationHistoryFromPreview: function () {
            location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigId + '/registerconfigurations/' + this.getRegisterConfigGrid().getSelectionModel().getSelection()[0].get("id") + '/edit';
        },

    createRegisterConfigurationHistory: function () {
        location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigId + '/registerconfigurations/create';
    },

    previewRegisterConfig: function (grid, record) {
        var registerConfigs = this.getRegisterConfigGrid().getSelectionModel().getSelection();
        if (registerConfigs.length == 1) {
            this.getRegisterConfigPreviewForm().loadRecord(registerConfigs[0]);
            var registerConfigsName = this.getRegisterConfigPreviewForm().form.findField('name').getSubmitValue();
            this.getRegisterConfigPreview().getLayout().setActiveItem(1);
            this.getRegisterConfigPreview().setTitle(registerConfigsName);
            this.getPreviewMrId().setValue(registerConfigs[0].getReadingType().get('mrid'));
            this.getRegisterConfigPreviewForm().loadRecord(registerConfigs[0]);
        } else {
            this.getRegisterConfigPreview().getLayout().setActiveItem(0);
        }
    },

    showRegisterConfigs: function (deviceTypeId, deviceConfigId) {
        var me = this;
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigId = deviceConfigId;
        var widget = Ext.widget('registerConfigSetup', {deviceTypeId: deviceTypeId, deviceConfigId: deviceConfigId});

        me.getCreateRegisterConfigBtn().href = '#/administration/devicetypes/' + deviceTypeId + '/deviceconfigurations/' + deviceConfigId + '/registerconfigurations/create';
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigId, {
                    success: function (deviceConfig) {
                        var deviceTypeName = deviceType.get('name');
                        var deviceConfigName = deviceConfig.get('name');
                        me.getApplication().fireEvent('changecontentevent', widget);
                    }
                });
            }
        });
    },

    showRegisterConfigurationCreateView: function (deviceTypeId, deviceConfigId) {
        var me = this;
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigId = deviceConfigId;
        var registerTypesOfDevicetypeStore = Ext.data.StoreManager.lookup('AvailableRegisterTypesForDeviceConfiguration');

        registerTypesOfDevicetypeStore.getProxy().setExtraParam('deviceType', deviceTypeId);
        registerTypesOfDevicetypeStore.getProxy().setExtraParam('filter',Ext.encode([{
            property:'available',
            value:true
        },{
            property:'deviceconfigurationid',
            value: this.deviceConfigId
        }]));

        registerTypesOfDevicetypeStore.load({
            callback: function (store) {
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                    success: function (deviceType) {
                        var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                        model.getProxy().setExtraParam('deviceType', deviceTypeId);
                        model.load(deviceConfigId, {
                            success: function (deviceConfig) {
                                var deviceTypeName = deviceType.get('name');
                                var deviceConfigName = deviceConfig.get('name');
                                var widget = Ext.widget('registerConfigEdit', {
                                    edit: false,
                                    registerTypesOfDeviceType: registerTypesOfDevicetypeStore,
                                    returnLink: '#/administration/devicetypes/' + deviceTypeId + '/deviceconfigurations/' + deviceConfigId + '/registerconfigurations'
                                });
                                me.getApplication().fireEvent('changecontentevent', widget);
                                me.getRegisterConfigEditForm().setTitle(Uni.I18n.translate('registerConfigs.createRegisterConfig', 'MDC', 'Create register configuration'));
                                widget.down('#editNumberOfDigitsField').setValue(8);
                                widget.down('#editNumberOfFractionDigitsField').setValue(0);
                                widget.down('#editMultiplierField').setValue(1);
                                widget.down('#editOverflowValueField').setValue(100000000);
                            }
                        });
                    }
                });
            }
        });
    },

    showReadingType: function (record) {
        var widget = Ext.widget('readingTypeDetails');
        this.getReadingTypeDetailsForm().loadRecord(record.getReadingType());
        widget.show();
    },

    changeRegisterType: function (field, value, options) {
        var me = this;
        var view = this.getRegisterConfigEditForm();
        if (field.name === 'registerMapping') {
            var registerType = me.getAvailableRegisterTypesForDeviceConfigurationStore().findRecord('id', value);
            if (registerType != null) {
                view.down('#create_mrid').setValue(registerType.getReadingType().get('mrid'));
                view.down('#editObisCodeField').setValue(registerType.get('obisCode'));
                view.down('#editOverruledObisCodeField').setValue(registerType.get('obisCode'));
                view.down('#readingTypeContainer').enable();
            }
        }
    },

    createRegisterConfiguration: function () {
        var me = this;
        var record = Ext.create(Mdc.model.RegisterConfiguration),
            values = this.getRegisterConfigEditForm().getValues();

        var view = this.getRegisterConfigEditForm();
        var newObisCode = view.down('#editObisCodeField').getValue();
        var originalObisCode = view.down('#editOverruledObisCodeField').getValue();

        if (record) {
            record.set(values);
            if (newObisCode === originalObisCode) {
                record.overruledObisCode = null;
            }
            record.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigId});
            record.save({
                success: function (record) {
                    location.href = '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/registerconfigurations';
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getRegisterConfigEditForm().getForm().markInvalid(json.errors);
                    }
                }
            });

        }
    },

    deleteRegisterConfiguration: function (registerConfigurationToDelete) {
        var me = this;
        Ext.MessageBox.show({
            msg: Uni.I18n.translate('registerConfig.removeUsedRegisterConfig', 'MDC', 'The register configuration will no longer be available.'),
            title: Uni.I18n.translate('general.remove', 'MDC', 'Remove') + ' ' + registerConfigurationToDelete.get('name') + '?',
            config: {
                registerConfigurationToDelete: registerConfigurationToDelete,
                me: me
            },
            buttons: Ext.MessageBox.YESNO,
            fn: me.removeRegisterConfigFromDeviceType,
            icon: Ext.MessageBox.WARNING
        });
    },

    deleteRegisterConfigurationFromPreview: function () {
        this.deleteRegisterConfiguration(this.getRegisterConfigGrid().getSelectionModel().getSelection()[0]);
    },


    removeRegisterConfigFromDeviceType: function (btn, text, opt) {
        if (btn === 'yes') {
            var registerConfigurationToDelete = opt.config.registerConfigurationToDelete;
            var me = opt.config.me;
            registerConfigurationToDelete.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigId});
            registerConfigurationToDelete.destroy({
                callback: function () {
                    location.href = '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/registerconfigurations';
                }
            });

        }
    },

    changeNumberOfDigits: function () {
        var view = this.getRegisterConfigEditForm();
        view.down('#editOverflowValueField').setValue(null);
        var numberOfDigits = view.down('#editNumberOfDigitsField').getValue();
        var maxOverFlowValue = Math.pow(10, numberOfDigits);
        this.getOverflowValueInfo().update('<span style="color: grey"><i>' + Uni.I18n.translate('registerConfig.overflowValueInfo', 'MDC', 'The maximum overflow value is {0}.', [maxOverFlowValue]) + '</i></span>');
        view.down('#editOverflowValueField').setMaxValue(maxOverFlowValue);
    },

    showRegisterConfigurationEditView: function (deviceTypeId, deviceConfigurationId, registerConfigurationId) {
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigId = deviceConfigurationId;
        var me = this;
        var registerTypesOfDevicetypeStore = Ext.data.StoreManager.lookup('RegisterTypesOfDevicetype')
        registerTypesOfDevicetypeStore.getProxy().setExtraParam('deviceType', deviceTypeId);

        registerTypesOfDevicetypeStore.load({
            callback: function (store) {
                var widget = Ext.widget('registerConfigEdit', {
                    edit: true,
                    registerTypesOfDeviceType: registerTypesOfDevicetypeStore,
                    returnLink: me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens()
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.setLoading(true);
                var model = Ext.ModelManager.getModel('Mdc.model.RegisterConfiguration');
                model.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
                model.load(registerConfigurationId, {
                    success: function (registerConfiguration) {
                        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                            success: function (deviceType) {
                                var deviceConfigModel = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                                deviceConfigModel.getProxy().setExtraParam('deviceType', deviceTypeId);
                                deviceConfigModel.load(deviceConfigurationId, {
                                    success: function (deviceConfiguration) {
                                        widget.down('form').loadRecord(registerConfiguration);
                                        me.getRegisterConfigEditForm().setTitle(Uni.I18n.translate('registerConfigs.editRegisterConfig', 'MDC', 'Edit register configuration'));
                                        widget.down('#registerTypeComboBox').setValue(registerConfiguration.get('registerMapping'));
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

    editRegisterConfiguration: function () {
        var record = this.getRegisterConfigEditForm().getRecord(),
            values = this.getRegisterConfigEditForm().getValues();
        var me = this;

        var view = this.getRegisterConfigEditForm();
        var newObisCode = view.down('#editObisCodeField').getValue();
        var originalObisCode = view.down('#editOverruledObisCodeField').getValue();

        if (record) {
            record.set(values);
            if (newObisCode === originalObisCode) {
                record.overruledObisCode = null;
            }
            record.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigId});
            record.save({
                callback: function (record) {
                    location.href = me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens()
                }
            });
        }
    }

});
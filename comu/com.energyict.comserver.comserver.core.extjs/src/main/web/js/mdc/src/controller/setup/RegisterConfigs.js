Ext.define('Mdc.controller.setup.RegisterConfigs', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.registerconfig.RegisterConfigSetup',
        'setup.registerconfig.RegisterConfigGrid',
        'setup.registerconfig.RegisterConfigPreview',
        'setup.registerconfig.RegisterConfigEdit',
        'Cfg.view.validation.RulePreview'
    ],

    requires: [
        'Mdc.store.RegisterConfigsOfDeviceConfig',
        'Mdc.store.AvailableRegisterTypesForDeviceConfiguration',
        'Mdc.store.RegisterTypesOfDevicetype',
        'Mdc.store.RegisterConfigValidationRules'
    ],

    stores: [
        'RegisterConfigsOfDeviceConfig',
        'RegisterTypesOfDevicetype',
        'AvailableRegisterTypesForDeviceConfiguration',
        'RegisterConfigValidationRules'
    ],

    refs: [
        {ref: 'registerConfigGrid', selector: '#registerconfiggrid'},
        {ref: 'rulesForRegisterConfigGrid', selector: '#rulesForRegisterConfigGrid'},
        {ref: 'registerConfigPreviewForm', selector: '#registerConfigPreviewForm'},
        {ref: 'ruleForRegisterConfigPreview', selector: '#ruleForRegisterConfigPreview'},
        {ref: 'registerConfigPreview', selector: '#registerConfigPreview'},
        {ref: 'registerConfigEditForm', selector: '#registerConfigEditForm'},
        {ref: 'createRegisterConfigBtn', selector: '#createRegisterConfigBtn'},
        {ref: 'overflowValueInfo', selector: '#overflowValueInfo'},
        {ref: 'numberOfDigits', selector: '#numberOfDigits'},
        {ref: 'rulesForRegisterConfigGrid', selector: 'validation-rules-for-registerconfig-grid'},
        {ref: 'rulesForRegisterConfigPreview', selector: 'register-config-and-rules-preview-container > #rulesForRegisterConfigPreview'},
        {ref: 'registerTypeCombo', selector: '#registerConfigEditForm #registerTypeComboBox'},
        {ref: 'validationRulesForRegisterConfigPreview', selector: 'register-config-and-rules-preview-container validation-rule-preview'},
        {ref: 'registerConfigNumberPanel', selector: '#registerConfigNumberPanel'}
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
                editRegisterConfig: this.editRegisterConfigurationHistory,
                deleteRegisterConfig: this.deleteRegisterConfiguration
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
            },
            '#rulesForRegisterConfigGrid': {
                selectionchange: this.previewValidationRule
            },
            '#rulesForRegisterConfigGrid actioncolumn': {
                viewRuleForRegisterConfig: this.viewRule
            },
            'register-config-and-rules-preview-container rules-for-registerconfig-actionmenu': {
                click: this.viewRule
            }, '#registerConfigEditForm #valueTypeRadioGroup': {
                change: this.hideShowNumberFields
            }
        });
    },

    viewRule: function (menu, item) {
        var me = this,
            record = menu.record || me.getValidationRulesForRegisterConfigPreview().getRecord();

        location.href = '#/administration/validation/rulesets/' + record.data.ruleSet.id + '/rules/' + record.data.id;
    },

    previewValidationRule: function (grid, record) {
        var selectedRules = this.getRulesForRegisterConfigGrid().getSelectionModel().getSelection();
        this.getValidationRulesForRegisterConfigPreview().updateValidationRule(selectedRules[0]);
    },


    editRegisterConfigurationHistory: function (record) {
        location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigId + '/registerconfigurations/' + record.get('id') + '/edit';
    },

    editRegisterConfigurationHistoryFromPreview: function () {
        location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigId + '/registerconfigurations/' + this.getRegisterConfigGrid().getSelectionModel().getSelection()[0].get("id") + '/edit';
    },

    createRegisterConfigurationHistory: function () {
        location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigId + '/registerconfigurations/add';
    },

    previewRegisterConfig: function (grid, record) {
        var me = this,
            registerConfigs = this.getRegisterConfigGrid().getSelectionModel().getSelection();

        if (registerConfigs.length === 1) {
            var registerConfig = registerConfigs[0];

            me.getRegisterConfigPreview().updateRegisterConfig(registerConfig);

            me.getRegisterConfigValidationRulesStore().getProxy().extraParams =
                ({deviceType: this.deviceTypeId, deviceConfig: this.deviceConfigId, registerConfig: registerConfigs[0].getId()});

            me.getRulesForRegisterConfigGrid().down('pagingtoolbartop').totalCount = -1;
            if (registerConfig.get('asText')) {
                me.getRegisterConfigNumberPanel().hide();
            } else {
                me.getRegisterConfigNumberPanel().show();
            }
            if (Cfg.privileges.Validation.canUpdateDeviceValidation()) {
                me.getRulesForRegisterConfigPreview().setTitle(registerConfig.get('name') + ' validation rules');
                me.getRegisterConfigValidationRulesStore().load();
            } else {
                me.getRulesForRegisterConfigPreview().setTitle('');
                me.getValidationRulesForRegisterConfigPreview().setVisible(false);
            }
        }
    },

    showRegisterConfigs: function (deviceTypeId, deviceConfigId) {
        var me = this;
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigId = deviceConfigId;
        var widget = Ext.widget('registerConfigSetup', {deviceTypeId: deviceTypeId, deviceConfigId: deviceConfigId});

        if (me.getCreateRegisterConfigBtn())
            me.getCreateRegisterConfigBtn().href = '#/administration/devicetypes/' + deviceTypeId + '/deviceconfigurations/' + deviceConfigId + '/registerconfigurations/add';
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        widget.down('#stepsMenu #deviceConfigurationOverviewLink').setText(deviceConfig.get('name'));
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.getRegisterConfigGrid().getSelectionModel().doSelect(0);
                    }
                });
            }
        });
    },

    showRegisterConfigurationCreateView: function (deviceTypeId, deviceConfigId) {
        var me = this;
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigId = deviceConfigId;
        var registerTypesOfDevicetypeStore = Ext.data.StoreManager.lookup('AvailableRegisterTypesForDeviceConfiguration'),
            router = this.getController('Uni.controller.history.Router');

        registerTypesOfDevicetypeStore.getProxy().setExtraParam('deviceType', deviceTypeId);
        registerTypesOfDevicetypeStore.getProxy().setExtraParam('filter', Ext.encode([
            {
                property: 'available',
                value: true
            },
            {
                property: 'deviceconfigurationid',
                value: parseInt(this.deviceConfigId)
            }
        ]));
        registerTypesOfDevicetypeStore.getProxy().startParam = null;
        registerTypesOfDevicetypeStore.getProxy().limitParam = null;
        registerTypesOfDevicetypeStore.load({
            callback: function () {
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                    success: function (deviceType) {
                        me.getApplication().fireEvent('loadDeviceType', deviceType);
                        var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                        model.getProxy().setExtraParam('deviceType', deviceTypeId);
                        model.load(deviceConfigId, {
                            success: function (deviceConfig) {
                                me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                                var widget = Ext.widget('registerConfigEdit', {
                                    edit: false,
                                    registerTypesOfDeviceType: registerTypesOfDevicetypeStore,
                                    returnLink: router.getRoute('administration/devicetypes/view/deviceconfigurations/view/registerconfigurations').buildUrl()
                                });
                                me.getApplication().fireEvent('changecontentevent', widget);
                                me.getRegisterConfigEditForm().setTitle(Uni.I18n.translate('registerConfigs.createRegisterConfig', 'MDC', 'Add register configuration'));
                                widget.down('#editNumberOfDigitsField').setValue(8);
                                widget.down('#editNumberOfFractionDigitsField').setValue(0);
                                widget.down('#editOverflowValueField').setValue(100000000);
                                me.getRegisterTypeCombo().on('change', me.changeRegisterType, me);
                            }
                        });
                    }
                });
            }
        });
    },

    changeRegisterType: function (field, value, options) {
        var me = this;
        var view = this.getRegisterConfigEditForm();
        if (field.name === 'registerType') {
            var registerType = me.getAvailableRegisterTypesForDeviceConfigurationStore().findRecord('id', value);
            if (registerType != null) {
                view.down('[name="readingType"]').setValue(registerType.get('readingType')).enable();
                view.down('#editObisCodeField').setValue(registerType.get('obisCode'));
                view.down('#editOverruledObisCodeField').setValue(registerType.get('obisCode'));
            }
        }
    },

    createRegisterConfiguration: function () {
        var me = this,
            record = Ext.create(Mdc.model.RegisterConfiguration),
            form = me.getRegisterConfigEditForm(),
            baseForm = form.getForm(),
            warningMsg = form.down('uni-form-error-message'),
            values = form.getValues(),
            newObisCode = form.down('#editObisCodeField').getValue(),
            originalObisCode = form.down('#editOverruledObisCodeField').getValue(),
            router = this.getController('Uni.controller.history.Router');

        Ext.suspendLayouts();
        baseForm.clearInvalid();
        warningMsg.hide();
        Ext.resumeLayouts(true);
        if (record) {
            record.set(values);
            if (newObisCode === originalObisCode) {
                record.overruledObisCode = null;
            }
            record.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigId});
            form.setLoading();
            record.save({
                success: function () {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('registerConfig.acknowlegment.added', 'MDC', 'Register configuration added'));
                    router.getRoute('administration/devicetypes/view/deviceconfigurations/view/registerconfigurations').forward();
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText, true);

                    Ext.suspendLayouts();
                    warningMsg.show();
                    if (json && json.errors) {
                        baseForm.markInvalid(json.errors);
                    }
                    Ext.resumeLayouts(true);
                },
                callback: function () {
                    form.setLoading(false);
                }
            });

        }
    },

    deleteRegisterConfiguration: function (registerConfigurationToDelete) {
        var me = this;

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('registerConfig.removeUsedRegisterConfig', 'MDC', 'The register configuration will no longer be available.'),
            title: Uni.I18n.translate('general.removeConfirmation', 'MDC', 'Remove \'{0}\'?', [registerConfigurationToDelete.get('name')]),
            config: {
                registerConfigurationToDelete: registerConfigurationToDelete,
                me: me
            },
            fn: me.removeRegisterConfigFromDeviceType
        });
    },

    deleteRegisterConfigurationFromPreview: function () {
        this.deleteRegisterConfiguration(this.getRegisterConfigGrid().getSelectionModel().getSelection()[0]);
    },


    removeRegisterConfigFromDeviceType: function (btn, text, opt) {
        if (btn === 'confirm') {
            var registerConfigurationToDelete = opt.config.registerConfigurationToDelete;
            var me = opt.config.me;
            registerConfigurationToDelete.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigId});
            registerConfigurationToDelete.destroy({
                success: function () {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('registerConfig.acknowlegment.removed', 'MDC', 'Register configuration removed'));
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
        var registerTypesOfDevicetypeStore = Ext.data.StoreManager.lookup('RegisterTypesOfDevicetype'),
            router = this.getController('Uni.controller.history.Router');

        registerTypesOfDevicetypeStore.getProxy().setExtraParam('deviceType', deviceTypeId);

        registerTypesOfDevicetypeStore.load({
            callback: function (store) {
                var widget = Ext.widget('registerConfigEdit', {
                    edit: true,
                    registerTypesOfDeviceType: registerTypesOfDevicetypeStore,
                    returnLink: router.getRoute('administration/devicetypes/view/deviceconfigurations/view/registerconfigurations').buildUrl()
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.setLoading(true);
                var model = Ext.ModelManager.getModel('Mdc.model.RegisterConfiguration');
                model.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
                model.load(registerConfigurationId, {
                    success: function (registerConfiguration) {
                        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                            success: function (deviceType) {
                                me.getApplication().fireEvent('loadDeviceType', deviceType);
                                var deviceConfigModel = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                                deviceConfigModel.getProxy().setExtraParam('deviceType', deviceTypeId);
                                deviceConfigModel.load(deviceConfigurationId, {
                                    success: function (deviceConfiguration) {
                                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfiguration);
                                        widget.down('form').loadRecord(registerConfiguration);
                                        me.getApplication().fireEvent('loadRegisterConfiguration', registerConfiguration);
                                        me.getRegisterConfigEditForm().setTitle(Uni.I18n.translate('general.edit', 'MDC', 'Edit') + " '" + registerConfiguration.get('readingType').fullAliasName + "'");
                                        widget.down('#registerTypeComboBox').setValue(registerConfiguration.get('registerType'));
                                        if (registerConfiguration.get('asText') === true) {
                                            widget.down('#textRadio').setValue(true);
                                        } else {
                                            widget.down('#numberRadio').setValue(true);
                                        }
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
            values = this.getRegisterConfigEditForm().getValues(),
            router = this.getController('Uni.controller.history.Router');

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
                success: function (record) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('registerConfig.acknowlegment.saved', 'MDC', 'Register configuration saved'));
                    router.getRoute('administration/devicetypes/view/deviceconfigurations/view/registerconfigurations').forward();
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

    hideShowNumberFields: function (radioGroup) {
        var visible = !(radioGroup.getValue().asText);
        this.getRegisterConfigEditForm().down('#editNumberOfDigitsField').setVisible(visible);
        this.getRegisterConfigEditForm().down('#editNumberOfFractionDigitsField').setVisible(visible);
        this.getRegisterConfigEditForm().down('#editOverflowValueField').setVisible(visible);
        this.getRegisterConfigEditForm().down('#overflowMsg').setVisible(visible);
        if (visible) {
            var values = this.getRegisterConfigEditForm().getValues();
            if (values.numberOfDigits === '') {
                this.getRegisterConfigEditForm().down('#editNumberOfDigitsField').setValue(8)
            }
            ;
            if (values.numberOfFractionDigits === '') {
                this.getRegisterConfigEditForm().down('#editNumberOfFractionDigitsField').setValue(0)
            }
            ;
            if (values.overflow === '') {
                this.getRegisterConfigEditForm().down('#editOverflowValueField').setValue(100000000)
            }
            ;
        }
    }

});
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
        {ref: 'rulesForRegisterConfigGrid', selector: 'validation-rules-for-registerconfig-grid'},
        {ref: 'rulesForRegisterConfigPreview', selector: 'register-config-and-rules-preview-container > #rulesForRegisterConfigPreview'},
        {ref: 'registerTypeCombo', selector: '#registerConfigEditForm #registerTypeComboBox'},
        {ref: 'validationRulesForRegisterConfigPreview', selector: 'register-config-and-rules-preview-container validation-rule-preview'},
        {
            ref: 'registerConfigPreviewOverflowField',
            selector: 'registerConfigPreview #mdc-register-config-preview-overflow'
        },
        {
            ref: 'registerConfigPreviewFractionDigitsField',
            selector: 'registerConfigPreview #mdc-register-config-preview-fractionDigits'
        },
        {
            ref: 'registerConfigPreviewCalculatedReadingTypeField',
            selector: 'registerConfigPreview #mdc-register-config-preview-calculated'
        },
        {
            ref: 'overruledObisCodeField',
            selector: '#registerConfigEditForm #editOverruledObisCodeField'
        },
        {
            ref: 'restoreObisCodeBtn',
            selector: '#registerConfigEditForm #mdc-restore-obiscode-btn'
        }
    ],

    deviceTypeId: null,
    deviceConfigId: null,
    registerTypesObisCode: null, // The OBIS code of the selected register type

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
            },
            '#registerConfigEditForm #valueTypeRadioGroup': {
                change: this.hideShowNumberFields
            },
            '#registerConfigEditForm #multiplierRadioGroup': {
                change: this.onMultiplierChange
            },
            '#registerConfigEditForm #editOverruledObisCodeField': {
                change: this.onOverruledObisCodeChange
            },
            '#registerConfigEditForm #mdc-restore-obiscode-btn': {
                click: this.onRestoreObisCodeBtnClicked
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
                me.getRegisterConfigPreviewOverflowField().hide();
                me.getRegisterConfigPreviewFractionDigitsField().hide();
                me.getRegisterConfigPreviewCalculatedReadingTypeField().hide();
            } else {
                me.getRegisterConfigPreviewOverflowField().show();
                me.getRegisterConfigPreviewFractionDigitsField().show();
                // TODO: hide this field when there is not calculated reading type to show:
                me.getRegisterConfigPreviewCalculatedReadingTypeField().show();
            }
            if (Cfg.privileges.Validation.canUpdateDeviceValidation()) {
                me.getRulesForRegisterConfigPreview().setTitle(
                    Uni.I18n.translate('registerConfigs.validationRulesOfRegisterConfigX', 'MDC', '{0} validation rules', registerConfig.get('name'))
                );
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
                                widget.down('#editNumberOfFractionDigitsField').setValue(0);
                                me.getRegisterTypeCombo().on('change', me.onRegisterTypeChange, me);
                            }
                        });
                    }
                });
            }
        });
    },

    onRegisterTypeChange: function (field, value, options) {
        var me = this,
            view = me.getRegisterConfigEditForm();
        if (field.name === 'registerType') {
            var registerType = me.getAvailableRegisterTypesForDeviceConfigurationStore().findRecord('id', value);
            if (registerType != null) {
                me.updateReadingTypeFields(registerType);
                me.registerTypesObisCode = registerType.get('obisCode');
                view.down('#editObisCodeField').setValue(me.registerTypesObisCode);
                me.getOverruledObisCodeField().setValue(me.registerTypesObisCode);
                me.onOverruledObisCodeChange(me.getOverruledObisCodeField(), me.registerTypesObisCode);
            }
        }
    },

    createRegisterConfiguration: function () {
        var me = this,
            record = Ext.create(Mdc.model.RegisterConfiguration),
            form = me.getRegisterConfigEditForm(),
            baseForm = form.getForm(),
            errorMsgPnl = form.down('uni-form-error-message'),
            values = form.getValues(),
            newObisCode = form.down('#editObisCodeField').getValue(),
            originalObisCode = form.down('#editOverruledObisCodeField').getValue(),
            router = this.getController('Uni.controller.history.Router');

        Ext.suspendLayouts();
        baseForm.clearInvalid();
        errorMsgPnl.hide();
        Ext.resumeLayouts(true);
        if (form.isValid()) {
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
                        errorMsgPnl.show();
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
        } else {
            errorMsgPnl.show();
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
                    me.getController('Uni.controller.history.Router').getRoute().forward();
                }
            });

        }
    },

    showRegisterConfigurationEditView: function (deviceTypeId, deviceConfigurationId, registerConfigurationId) {
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigId = deviceConfigurationId;
        var me = this,
            registerTypesOfDevicetypeStore = Ext.data.StoreManager.lookup('RegisterTypesOfDevicetype'),
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
                                        me.getRegisterConfigEditForm().setTitle(
                                            Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", registerConfiguration.get('readingType').fullAliasName)
                                        );
                                        me.getRegisterTypeCombo().setValue(registerConfiguration.get('registerType'));
                                        if (registerConfiguration.get('asText') === true) {
                                            widget.down('#textRadio').setValue(true);
                                        } else {
                                            widget.down('#numberRadio').setValue(true);
                                        }
                                        widget.down('#valueTypeRadioGroup').setDisabled(deviceConfiguration.get('active'));
                                        widget.down('#multiplierRadioGroup').setDisabled(deviceConfiguration.get('active'));
                                        me.registerTypesObisCode = registerConfiguration.get('obisCode');
                                        me.onOverruledObisCodeChange(me.getOverruledObisCodeField(), me.registerTypesObisCode);
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
        var me = this,
            form = me.getRegisterConfigEditForm(),
            baseForm = form.getForm(),
            errorMsgPnl = form.down('uni-form-error-message'),
            record = form.getRecord(),
            values = form.getValues(),
            router = me.getController('Uni.controller.history.Router'),
            newObisCode = form.down('#editObisCodeField').getValue(),
            originalObisCode = form.down('#editOverruledObisCodeField').getValue();

        Ext.suspendLayouts();
        baseForm.clearInvalid();
        errorMsgPnl.hide();
        Ext.resumeLayouts(true);
        if (form.isValid()) {
            if (record) {
                record.set(values);
                if (newObisCode === originalObisCode) {
                    record.overruledObisCode = null;
                }
                record.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigId});
                form.setLoading();
                record.save({
                    backUrl: router.getRoute('administration/devicetypes/view/deviceconfigurations/view/registerconfigurations').buildUrl(),
                    success: function (record) {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('registerConfig.acknowlegment.saved', 'MDC', 'Register configuration saved'));
                        router.getRoute('administration/devicetypes/view/deviceconfigurations/view/registerconfigurations').forward();
                    },
                    failure: function (record, operation) {
                        if (operation.response.status === 400) {
                            return
                        }
                        Ext.suspendLayouts();
                        errorMsgPnl.show();
                        var json = Ext.decode(operation.response.responseText);
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
        } else {
            errorMsgPnl.show();
        }
    },

    hideShowNumberFields: function (radioGroup) {
        var visible = !(radioGroup.getValue().asText);
        this.getRegisterConfigEditForm().down('#editNumberOfFractionDigitsField').setVisible(visible);
        this.getRegisterConfigEditForm().down('#editOverflowValueField').setVisible(visible);
        this.getRegisterConfigEditForm().down('#multiplierRadioGroup').setVisible(visible);
        if (visible) {
            var values = this.getRegisterConfigEditForm().getValues();
            if (values.numberOfFractionDigits === '') {
                this.getRegisterConfigEditForm().down('#editNumberOfFractionDigitsField').setValue(0);
            }
        }
        this.getRegisterConfigEditForm().down('#editOverflowValueField').allowBlank = radioGroup.getValue().asText;
    },

    onMultiplierChange: function(radioGroup) {
        this.updateReadingTypeFields(
            this.getAvailableRegisterTypesForDeviceConfigurationStore().findRecord('id', this.getRegisterTypeCombo().getValue())
        );
    },

    updateReadingTypeFields: function(selectedRegisterType) {
        var me = this,
            form = this.getRegisterConfigEditForm(),
            multiplierRadioGroup = form.down('#multiplierRadioGroup'),
            collectedReadingTypeField = form.down('#mdc-collected-readingType-field'),
            calculatedReadingTypeField = form.down('#mdc-calculated-readingType-field'),
            calculatedReadingTypeCombo = form.down('#mdc-calculated-readingType-combo'),
            useMultiplier = multiplierRadioGroup.getValue().useMultiplier,
            multipliedReadingTypes = selectedRegisterType.get('multipliedCalculatedReadingType');

        collectedReadingTypeField.setValue(selectedRegisterType.get('readingType'));
        collectedReadingTypeField.setVisible(selectedRegisterType);
        if (!multipliedReadingTypes || multipliedReadingTypes.length === 0) {
            multiplierRadioGroup.setValue({ useMultiplier : false });
            useMultiplier = multiplierRadioGroup.getValue().useMultiplier
            multiplierRadioGroup.setDisabled(true);
        } else {
            multiplierRadioGroup.setDisabled(false);
        }

        if (useMultiplier) {
            if (multipliedReadingTypes.length === 1) {
                calculatedReadingTypeField.setValue(multipliedReadingTypes[0]);
                calculatedReadingTypeField.setVisible(true);
                calculatedReadingTypeCombo.setVisible(false);
            } else {
                var readingTypesStore = Ext.create('Ext.data.Store', {model: 'Mdc.model.ReadingType'});
                Ext.Array.forEach(multipliedReadingTypes, function(item) {
                    readingTypesStore.add(item);
                });
                calculatedReadingTypeCombo.bindStore(readingTypesStore, true);
                calculatedReadingTypeCombo.setValue(readingTypesStore.getAt(0));
                calculatedReadingTypeCombo.setVisible(true);
                calculatedReadingTypeField.setVisible(false);
            }
        } else {
            calculatedReadingTypeField.setVisible(false);
            calculatedReadingTypeCombo.setVisible(false);
        }
    },

    onOverruledObisCodeChange: function(overruledObisCodeField, newValue) {
        var me = this;
        me.getRestoreObisCodeBtn().setDisabled(newValue === me.registerTypesObisCode);
        me.getRestoreObisCodeBtn().setTooltip(
            newValue === me.registerTypesObisCode
                ? null
                : Uni.I18n.translate('general.obisCode.reset.tooltip', 'MDC', 'Reset to {0}, the OBIS code of the register type', me.registerTypesObisCode)
        );
    },

    onRestoreObisCodeBtnClicked: function() {
        var me = this;
        me.getRegisterConfigEditForm().down('#editObisCodeField').setValue(me.registerTypesObisCode);
        me.getOverruledObisCodeField().setValue(me.registerTypesObisCode);
        me.onOverruledObisCodeChange(me.getOverruledObisCodeField(), me.registerTypesObisCode);
    }

});
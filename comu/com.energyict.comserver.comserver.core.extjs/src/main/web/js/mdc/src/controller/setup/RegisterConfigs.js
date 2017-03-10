/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
    registerConfigurationBeingEdited: null,
    registerTypesObisCode: null, // The OBIS code of the selected register type

    init: function () {
        this.control({
            '#registerConfigSetupPanel #registerconfiggrid': {
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

    previewRegisterConfig: function (selectionModel, selectedRegisterConfigs) {
        var me = this;

        if (selectedRegisterConfigs.length === 1) {
            var registerConfig = selectedRegisterConfigs[0];

            if (me.getRegisterConfigPreview().rendered) {
                me.getRegisterConfigPreview().updateRegisterConfig(registerConfig);
            } else {
                me.getRegisterConfigPreview().on('afterrender', function() {
                    me.getRegisterConfigPreview().updateRegisterConfig(registerConfig);
                }, me, {single:true});
            }

            me.getRegisterConfigValidationRulesStore().getProxy().extraParams =
                ({deviceType: this.deviceTypeId, deviceConfig: this.deviceConfigId, registerConfig: selectedRegisterConfigs[0].getId()});

            me.getRulesForRegisterConfigGrid().down('pagingtoolbartop').totalCount = -1;
            if (registerConfig.get('asText')) {
                me.getRegisterConfigPreviewOverflowField().hide();
                me.getRegisterConfigPreviewFractionDigitsField().hide();
                me.getRegisterConfigPreviewCalculatedReadingTypeField().hide();
            } else {
                me.getRegisterConfigPreviewOverflowField().show();
                me.getRegisterConfigPreviewFractionDigitsField().show();
                if (registerConfig.get('calculatedReadingType') === undefined || registerConfig.get('calculatedReadingType') === '') {
                    me.getRegisterConfigPreviewCalculatedReadingTypeField().hide();
                } else {
                    me.getRegisterConfigPreviewCalculatedReadingTypeField().show();
                }
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
        var me = this,
            widget = Ext.widget('registerConfigSetup', {deviceTypeId: deviceTypeId, deviceConfigId: deviceConfigId}),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        if (mainView) mainView.setLoading(Uni.I18n.translate('general.loading', 'MDC', 'Loading...'));
        me.deviceTypeId = deviceTypeId;
        me.deviceConfigId = deviceConfigId;

        if (me.getCreateRegisterConfigBtn()) {
            me.getCreateRegisterConfigBtn().href =
                '#/administration/devicetypes/' + deviceTypeId + '/deviceconfigurations/' + deviceConfigId + '/registerconfigurations/add';
        }
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigId, {
                    success: function (deviceConfig) {
                        if (mainView) mainView.setLoading(false);
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        widget.down('#stepsMenu').setHeader(deviceConfig.get('name'));
                        me.getApplication().fireEvent('changecontentevent', widget);
                    }
                });
            }
        });
    },

    showRegisterConfigurationCreateView: function (deviceTypeId, deviceConfigId) {
        var me = this;
        this.registerConfigurationBeingEdited = null;
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

    onRegisterTypeChange: function (field, value) {
        var me = this,
            view = me.getRegisterConfigEditForm(),
            registerType = undefined,
            useMultiplier = undefined;

        if (field.name === 'registerType') {
            view.down('#multiplierRadioGroup').setDisabled(false);
            registerType = me.getAvailableRegisterTypesForDeviceConfigurationStore().findRecord('id', value);
            useMultiplier = view.down('#multiplierRadioGroup').getValue().useMultiplier;
            me.updateReadingTypeFields(registerType, useMultiplier);
            me.registerTypesObisCode = registerType.get('obisCode');
            view.down('#editObisCodeField').setValue(me.registerTypesObisCode);
            me.getOverruledObisCodeField().setValue(me.registerTypesObisCode);
            me.onOverruledObisCodeChange(me.getOverruledObisCodeField(), me.registerTypesObisCode);
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
            asText = form.down('#valueTypeRadioGroup').getValue().asText,
            useMultiplier = asText ? false : form.down('#multiplierRadioGroup').getValue().useMultiplier,
            calculatedReadingTypeField = form.down('#mdc-calculated-readingType-field'),
            calculatedReadingTypeCombo = form.down('#mdc-calculated-readingType-combo'),
            router = this.getController('Uni.controller.history.Router');

        Ext.suspendLayouts();
        baseForm.clearInvalid();
        errorMsgPnl.hide();
        Ext.resumeLayouts(true);
        if (form.isValid()) {
            if (record) {
                record.set(values);
                if (newObisCode === originalObisCode) {
                    record.set('overruledObisCode', null);
                }
                if (useMultiplier) {
                    if (calculatedReadingTypeField.isVisible()) {
                        record.setCalculatedReadingType(calculatedReadingTypeField.getValue());
                    } else if (calculatedReadingTypeCombo.isVisible()) {
                        record.setCalculatedReadingType(
                            calculatedReadingTypeCombo.getStore().findRecord(calculatedReadingTypeCombo.valueField, calculatedReadingTypeCombo.getValue())
                        );
                    }
                } else {
                    record.setCalculatedReadingType(null);
                    if (asText) {
                        record.set('multiplier', false);
                    }
                }

                record.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigId});
                form.setLoading();
                record.save({
                    success: function () {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('registerConfig.acknowledgment.added', 'MDC', 'Register configuration added'));
                        router.getRoute('administration/devicetypes/view/deviceconfigurations/view/registerconfigurations').forward();
                    },
                    failure: function (record, operation) {
                        var json = Ext.decode(operation.response.responseText, true);

                    if (json && !Ext.isEmpty(json.errors)) {
                        Ext.suspendLayouts();
                        errorMsgPnl.show();
                        baseForm.markInvalid(json.errors);
                        var calculatedReadingTypeError = Ext.Array.findBy(json.errors, function (item) { return item.id == 'calculatedReadingType';});
                        if (calculatedReadingTypeError && form.down('[name=calculatedReadingType]').isHidden()) {
                            form.down('#mdc-calculated-readingType-combo').markInvalid(calculatedReadingTypeError.msg);
                        }
                        Ext.resumeLayouts(true);
                    }
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
            title: Uni.I18n.translate('general.removeConfirmation', 'MDC', 'Remove \'{0}\'?', registerConfigurationToDelete.get('registerTypeName')),
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
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('registerConfig.acknowledgment.removed', 'MDC', 'Register configuration removed'));
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
                        me.registerConfigurationBeingEdited = registerConfiguration;
                        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                            success: function (deviceType) {
                                me.getApplication().fireEvent('loadDeviceType', deviceType);
                                var deviceConfigModel = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                                deviceConfigModel.getProxy().setExtraParam('deviceType', deviceTypeId);
                                deviceConfigModel.load(deviceConfigurationId, {
                                    success: function (deviceConfiguration) {
                                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfiguration);
                                        widget.down('form').loadRecord(registerConfiguration);
                                        // Don't know why these aren't set when loading the record:
                                        widget.down('#valueTypeRadioGroup').setValue({ asText : registerConfiguration.get('asText') });
                                        widget.down('#multiplierRadioGroup').setValue({ useMultiplier : registerConfiguration.get('useMultiplier') });

                                        me.getApplication().fireEvent('loadRegisterConfiguration', registerConfiguration);
                                        me.getRegisterConfigEditForm().setTitle(
                                            Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", registerConfiguration.get('collectedReadingType').fullAliasName)
                                        );
                                        me.registerTypesObisCode = registerConfiguration.get('obisCode');
                                        widget.down('#editObisCodeField').setValue(me.registerTypesObisCode);
                                        widget.down('#mdc-calculated-readingType-combo').setDisabled(deviceConfiguration.get('active'));
                                        widget.down('#valueTypeRadioGroup').setDisabled(deviceConfiguration.get('active'));
                                        widget.down('#multiplierRadioGroup').setDisabled(
                                            !me.registerConfigurationBeingEdited.get('possibleCalculatedReadingTypes') ||
                                            me.registerConfigurationBeingEdited.get('possibleCalculatedReadingTypes').length === 0 ||
                                            deviceConfiguration.get('active')
                                        );
                                        me.getOverruledObisCodeField().setValue(registerConfiguration.get('overruledObisCode'));
                                        me.onOverruledObisCodeChange(me.getOverruledObisCodeField(), registerConfiguration.get('overruledObisCode'));
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
            originalObisCode = form.down('#editOverruledObisCodeField').getValue(),
            useMultiplier = form.down('#multiplierRadioGroup').getValue().useMultiplier,
            calculatedReadingTypeField = form.down('#mdc-calculated-readingType-field'),
            calculatedReadingTypeCombo = form.down('#mdc-calculated-readingType-combo');

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
                if (useMultiplier) {
                    if (calculatedReadingTypeField.isVisible()) {
                        record.setCalculatedReadingType(calculatedReadingTypeField.getValue());
                    } else if (calculatedReadingTypeCombo.isVisible()) {
                        record.setCalculatedReadingType(
                            calculatedReadingTypeCombo.getStore().findRecord(calculatedReadingTypeCombo.valueField, calculatedReadingTypeCombo.getValue())
                        );
                    }
                } else {
                    record.setCalculatedReadingType(null);
                }

                record.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigId});
                form.setLoading();
                record.save({
                    backUrl: router.getRoute('administration/devicetypes/view/deviceconfigurations/view/registerconfigurations').buildUrl(),
                    success: function (record) {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('registerConfig.acknowledgment.saved', 'MDC', 'Register configuration saved'));
                        router.getRoute('administration/devicetypes/view/deviceconfigurations/view/registerconfigurations').forward();
                    },
                    failure: function (record, operation) {
                        Ext.suspendLayouts();
                        errorMsgPnl.show();
                        var json = Ext.decode(operation.response.responseText);
                        if (json && json.errors) {
                            baseForm.markInvalid(json.errors);
                            var calculatedReadingTypeError = Ext.Array.findBy(json.errors, function (item) { return item.id == 'calculatedReadingType';});
                            if (calculatedReadingTypeError && form.down('[name=calculatedReadingType]').isHidden()) {
                                form.down('#mdc-calculated-readingType-combo').markInvalid(calculatedReadingTypeError.msg);
                            }
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
        if (radioGroup.getValue().asText === undefined) {
            return;
        }
        var me = this,
            asNumber = !(radioGroup.getValue().asText),
            overflowValueField = me.getRegisterConfigEditForm().down('#editOverflowValueField'),
            numberOfFractionDigitsField = me.getRegisterConfigEditForm().down('#editNumberOfFractionDigitsField'),
            multiplierRadioGroup = me.getRegisterConfigEditForm().down('#multiplierRadioGroup'),
            dataContainer,
            useMultiplier;

        numberOfFractionDigitsField.setVisible(asNumber);
        overflowValueField.setVisible(asNumber);
        multiplierRadioGroup.setVisible(asNumber);
        if (asNumber) {
            var values = this.getRegisterConfigEditForm().getValues();
            if (values.numberOfFractionDigits === '') {
                numberOfFractionDigitsField.setValue(0);
            }
        }
        overflowValueField.allowBlank = radioGroup.getValue().asText;

        if (me.getRegisterConfigEditForm().up('#registerConfigEdit').isEdit()) { // Busy editing a register config
            me.registerConfigurationBeingEdited.set('isCumulative', me.registerConfigurationBeingEdited.get('readingType').isCumulative);
            dataContainer = me.registerConfigurationBeingEdited;
        } else { // Busy adding a register config
            dataContainer = me.getAvailableRegisterTypesForDeviceConfigurationStore().findRecord('id', me.getRegisterTypeCombo().getValue());
        }
        useMultiplier = !asNumber
            ? false
            : me.getRegisterConfigEditForm().down('#multiplierRadioGroup').getValue().useMultiplier;
        me.updateReadingTypeFields(dataContainer, useMultiplier);
    },

    onMultiplierChange: function(radioGroup) {
        var me = this,
            contentContainer = this.getRegisterConfigEditForm().up('#registerConfigEdit'),
            useMultiplier = radioGroup.getValue().useMultiplier,
            dataContainer;

        if (contentContainer.isEdit()) { // Busy editing a register config
            me.registerConfigurationBeingEdited.set('isCumulative', me.registerConfigurationBeingEdited.get('readingType').isCumulative);
            dataContainer = me.registerConfigurationBeingEdited;
        } else { // Busy adding a register config
            dataContainer = me.getAvailableRegisterTypesForDeviceConfigurationStore().findRecord('id', me.getRegisterTypeCombo().getValue());
        }
        me.updateReadingTypeFields(dataContainer, useMultiplier);
    },

    updateReadingTypeFields: function(dataContainer, useMultiplier) {
        var me = this,
            form = me.getRegisterConfigEditForm(),
            multiplierRadioGroup = form.down('#multiplierRadioGroup'),
            collectedReadingTypeField = form.down('#mdc-collected-readingType-field'),
            calculatedReadingTypeField = form.down('#mdc-calculated-readingType-field'),
            calculatedReadingTypeCombo = form.down('#mdc-calculated-readingType-combo'),
            overflowField = form.down('#editOverflowValueField'),
            possibleCalculatedReadingTypes = dataContainer.get('possibleCalculatedReadingTypes'),
            isCumulative = dataContainer.get('isCumulative');

        if (dataContainer.get('collectedReadingType') !== undefined) {
            collectedReadingTypeField.setValue(dataContainer.get('collectedReadingType'));
        } else {
            collectedReadingTypeField.setValue(dataContainer.get('readingType'));
        }
        collectedReadingTypeField.setVisible(dataContainer);
        if (!possibleCalculatedReadingTypes || possibleCalculatedReadingTypes.length === 0) {
            multiplierRadioGroup.setValue({ useMultiplier : false });
            useMultiplier = multiplierRadioGroup.getValue().useMultiplier;
            multiplierRadioGroup.setDisabled(true);
        } else {
            multiplierRadioGroup.setDisabled(false);
        }

        if (useMultiplier) {
            if (possibleCalculatedReadingTypes.length === 1) {
                calculatedReadingTypeField.setValue(possibleCalculatedReadingTypes[0]);
                calculatedReadingTypeField.setVisible(true);
                calculatedReadingTypeCombo.setVisible(false);
            } else {
                var readingTypesStore = Ext.create('Ext.data.Store', {model: 'Mdc.model.ReadingType'});
                Ext.Array.forEach(possibleCalculatedReadingTypes, function(item) {
                    readingTypesStore.add(item);
                });
                calculatedReadingTypeCombo.bindStore(readingTypesStore, true);
                if ( !Ext.isEmpty(dataContainer.get('calculatedReadingType')) ) {
                    calculatedReadingTypeCombo.setValue(dataContainer.get('calculatedReadingType').mRID);
                } else {
                    calculatedReadingTypeCombo.setValue(readingTypesStore.getAt(0));
                }
                calculatedReadingTypeCombo.setVisible(true);
                calculatedReadingTypeField.setVisible(false);
            }
        } else {
            calculatedReadingTypeField.setVisible(false);
            calculatedReadingTypeCombo.setVisible(false);
        }

        if (me.registerConfigurationBeingEdited === null) {
            overflowField.setValue(isCumulative ? 99999999 : null);
        }
        overflowField.required = isCumulative;
        overflowField.allowBlank = !isCumulative;
        // Geert: I find the following lines of code not so neat. If anyone finds another way to make (dis)appear
        //        the label's little red star indicating the field is (not) required, please tell me.
        if (isCumulative && !overflowField.labelEl.dom.classList.contains('uni-form-item-label-required')) {
            overflowField.labelEl.dom.classList.add('uni-form-item-label-required');
        } else if (!isCumulative && overflowField.labelEl.dom.classList.contains('uni-form-item-label-required')) {
            overflowField.labelEl.dom.classList.remove('uni-form-item-label-required');
        }
        overflowField.labelEl.repaint();
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
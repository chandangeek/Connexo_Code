/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.DeviceRegisterValidation', {
    extend: 'Ext.app.Controller',

    views: [
        'Cfg.configuration.view.RuleWithAttributesForm',
        'Cfg.configuration.view.RuleWithAttributesEdit',
        'Mdc.view.setup.deviceregisterconfiguration.TabbedDeviceRegisterView'
    ],

    models: [
        'Mdc.model.RegisterValidationConfigurationForReadingType',
        'Mdc.model.Register'
    ],

    stores: [
        'Mdc.store.RegisterValidationConfiguration'
    ],

    showDeviceRegisterValidationView: function (deviceId, registerId, tabController) {
        var me = this,
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            registerModel = me.getModel('Mdc.model.Register'),
            router = me.getController('Uni.controller.history.Router'),
            registersStore = me.getStore('Mdc.store.RegisterConfigsOfDevice'),
            validationConfigurationStore = me.getStore('Mdc.store.RegisterValidationConfiguration'),
            dependenciesCount = 4,
            device,
            register,
            onDependenciesLoad = function () {
                dependenciesCount--;
                if (!dependenciesCount) {
                    var hasValidationRules = validationConfigurationStore.first().rulesForCollectedReadingType().getCount() || validationConfigurationStore.first().rulesForCalculatedReadingType().getCount();

                    if (!hasValidationRules) {
                        window.location.replace(router.getRoute('devices/device/registers/registerdata').buildUrl());
                        return;
                    }
                    var widget = Ext.widget('tabbedDeviceRegisterView', {
                        device: device,
                        router: router,
                        validationConfigurationStore: validationConfigurationStore
                    });

                    me.getApplication().fireEvent('changecontentevent', widget);

                    Ext.suspendLayouts();
                    widget.down('#registerTabPanel').setTitle(register.get('readingType').fullAliasName);
                    widget.down('#register-validation-configuration').add(me.addValidationConfiguration(validationConfigurationStore));
                    tabController.showTab(2);
                    Ext.resumeLayouts(true);
                    contentPanel.setLoading(false);
                }
            };

        contentPanel.setLoading();
        registersStore.getProxy().extraParams = {deviceId: deviceId};
        registersStore.load(onDependenciesLoad);
        me.getModel('Mdc.model.Device').load(deviceId, {
            success: function (record) {
                device = record;
                me.getApplication().fireEvent('loadDevice', device);
                onDependenciesLoad();
            }
        });
        registerModel.getProxy().setExtraParam('deviceId', deviceId);
        registerModel.load(registerId, {
            success: function (record) {
                register = record;
                me.getApplication().fireEvent('loadRegisterConfiguration', register);
                onDependenciesLoad();
            }
        });

        validationConfigurationStore.getProxy().extraParams = {deviceId: deviceId, registerId: registerId};
        validationConfigurationStore.load(function () {
            onDependenciesLoad();
        });
    },

    addValidationConfiguration: function (store) {
        var me = this,
            validationConfigurationRecord = store.first(),
            valRulesForCollectedReadingTypeStore = validationConfigurationRecord.rulesForCollectedReadingType(),
            valRulesForCalculatedReadingTypeStore = validationConfigurationRecord.rulesForCalculatedReadingType(),
            validationRuleWithAttributesForms = [];

        if (valRulesForCollectedReadingTypeStore.getCount()) {
            validationRuleWithAttributesForms.push(me.prepareForm(valRulesForCollectedReadingTypeStore, 'collected'));
        }

        if (valRulesForCalculatedReadingTypeStore.getCount()) {
            validationRuleWithAttributesForms.push(me.prepareForm(valRulesForCalculatedReadingTypeStore, 'calculated'));
        }

        return validationRuleWithAttributesForms;
    },

    prepareForm: function (store, kindOfReadingType) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        return {
            xtype: 'rule-with-attributes-form',
            itemId: 'rule-with-attributes-register-validation-form-' + kindOfReadingType,
            isRegister: true,
            kindOfReadingType: kindOfReadingType,
            router: router,
            records: store.getRange(),
            type: 'validation',
            ui: 'medium',
            title: Ext.String.format("{0} {1}", Uni.I18n.translate('general.validationConfigurationFor', 'MDC', 'Validation configuration for'), store.first().get('readingType').fullAliasName),
            application: me.getApplication(),
            hasAdministerPrivileges: Mdc.privileges.Device.canAdministerValidationConfiguration()
        };
    },

    showEditRuleWithAttributes: function () {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            deviceId = router.arguments.deviceId,
            registerId = router.arguments.registerId,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            registerModel = me.getModel('Mdc.model.Register'),
            dependenciesCounter = 3,
            deviceModel = me.getModel('Mdc.model.Device'),
            ruleWithAttributesModel = me.getModel('Mdc.model.RegisterValidationConfigurationForReadingType'),
            route = router.getRoute('devices/device/registers/validation'),
            form,
            rule,
            widget;

        mainView.setLoading();
        ruleWithAttributesModel.getProxy().extraParams = {
            deviceId: Uni.util.Common.encodeURIComponent(router.arguments.deviceId),
            registerId: registerId,
            readingType: router.queryParams.readingType
        };
        registerModel.getProxy().setExtraParam('deviceId', deviceId);
        deviceModel.load(deviceId, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                displayPage();
            }
        });
        registerModel.load(registerId, {
            success: function (record) {
                me.getApplication().fireEvent('loadRegisterConfiguration', record);
                displayPage();
            }
        });
        ruleWithAttributesModel.load(router.arguments.ruleId, {
            success: function (record) {
                rule = record;
                displayPage();
            }
        });

        function displayPage() {
            dependenciesCounter--;
            if (!dependenciesCounter) {
                mainView.setLoading(false);
                widget = Ext.widget('rule-with-attributes-edit', {
                    itemId: 'rule-with-attributes-edit-validation',
                    type: 'validation',
                    route: route,
                    application: me.getApplication()
                });
                form = widget.down('#rule-with-attributes-edit-form');
                form.loadRecord(rule);
                form.setTitle(Ext.String.format("{0} '{1}'", Uni.I18n.translate('general.editAttributesFor', 'MDC', 'Edit attributes for'), rule.get('name')));
                form.down('property-form').loadRecord(rule);
                app.fireEvent('rule-with-attributes-loaded', rule);
                app.fireEvent('changecontentevent', widget);
            }
        }
    }
});
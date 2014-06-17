Ext.define('Mdc.controller.setup.ValidationRules', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.store.DeviceConfigValidationRuleSets',
        'Mdc.store.ValidationRuleSets'
    ],

    views: [
        'setup.validation.RulesOverview',
        'setup.validation.AddRuleSets'
    ],

    stores: [
        'DeviceConfigValidationRuleSets',
        'ValidationRuleSets'
    ],

    refs: [
        {ref: 'validationRuleSetsGrid', selector: 'validation-rules-overview validation-rulesets-grid'},
        {ref: 'validationRulesGrid', selector: 'validation-rules-overview validation-rules-grid'},
        {ref: 'addValidationRuleSetsGrid', selector: 'validation-add-rulesets validation-add-rulesets-grid'},
        {ref: 'addValidationRulesGrid', selector: 'validation-add-rulesets validation-add-rules-grid'}
    ],

    deviceTypeId: null,
    deviceConfigId: null,

    init: function () {
        this.callParent(arguments);

        this.control({
            'validation-add-rulesets validation-add-rulesets-grid': {
                selectionchange: this.onValidationRuleSetsSelectionChange
            },
            'validation-add-rulesets button[action=addValidationRuleSets]': {
                click: this.onAddValidationRuleSets
            }
        });
    },

    showValidationRulesOverview: function (deviceTypeId, deviceConfigId) {
        var me = this;
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigId = deviceConfigId;

        me.getDeviceConfigValidationRuleSetsStore().getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigId});
        var widget = Ext.widget('validation-rules-overview', {deviceTypeId: deviceTypeId, deviceConfigId: deviceConfigId});

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);

                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);

                model.load(deviceConfigId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.getValidationRuleSetsGrid().getSelectionModel().doSelect(0);
                    }
                });
            }
        });
    },

    showAddValidationRuleSets: function (deviceTypeId, deviceConfigId) {
        var me = this;
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigId = deviceConfigId;

        me.getDeviceConfigValidationRuleSetsStore().getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigId});
        var widget = Ext.widget('validation-add-rulesets', {deviceTypeId: deviceTypeId, deviceConfigId: deviceConfigId});

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);

                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);

                // This can be loaded asynchronously with the device configuration model.
                me.getValidationRuleSetsStore().load();

                model.load(deviceConfigId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.getAddValidationRuleSetsGrid().getSelectionModel().doSelect(0);
                    }
                });
            }
        });
    },

    onValidationRuleSetsSelectionChange: function (grid) {
        var count = grid.view.getSelectionModel().getSelection().length,
            counter = Ext.ComponentQuery.query('validation-add-rulesets #selection-counter')[0],
            selectionText = Uni.I18n.translatePlural(
                'validation.validationRuleSetSelection',
                count,
                'MDC',
                '{0} validation rule sets selected'
            );

        counter.setText(selectionText);
    },

    onAddValidationRuleSets: function () {
        // TODO
    }
});

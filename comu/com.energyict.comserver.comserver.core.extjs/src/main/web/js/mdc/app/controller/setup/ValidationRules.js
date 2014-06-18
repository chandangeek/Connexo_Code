Ext.define('Mdc.controller.setup.ValidationRules', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.store.DeviceConfigValidationRuleSets',
        'Cfg.store.ValidationRuleSets'
    ],

    views: [
        'setup.validation.RulesOverview',
        'setup.validation.AddRuleSets'
    ],

    stores: [
        'DeviceConfigValidationRuleSets'
    ],

    refs: [
        {ref: 'validationRuleSetsGrid', selector: 'validation-rules-overview validation-rulesets-grid'},
        {ref: 'validationRulesGrid', selector: 'validation-rules-overview validation-rules-grid'},
        {ref: 'addValidationRuleSets', selector: 'validation-add-rulesets'},
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
            },
            'validation-add-rulesets button[action=uncheckAll]': {
                click: this.onUncheckAll
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
                var store = Ext.getStore('ValidationRuleSets') || Ext.create('Cfg.store.ValidationRuleSets');
                store.load();

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
        var view = this.getAddValidationRuleSets(),
            selection = grid.view.getSelectionModel().getSelection(),
            counter = Ext.ComponentQuery.query('validation-add-rulesets #selection-counter')[0],
            selectionText = Uni.I18n.translatePlural(
                'validation.validationRuleSetSelection',
                selection.length,
                'MDC',
                '{0} validation rule sets selected'
            );

        counter.setText(selectionText);

        if (selection.length > 0) {
            view.updateValidationRuleSetPreview(selection[0]);
        }
    },

    onAddValidationRuleSets: function () {
        // TODO
    },

    onUncheckAll: function () {
        var grid = this.getAddValidationRuleSetsGrid();
        grid.getView().getSelectionModel().deselectAll();
    }
});

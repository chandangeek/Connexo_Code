Ext.define('Mdc.controller.setup.ValidationRuleSets', {
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
        {ref: 'validationRuleSetsOverview', selector: 'validation-rules-overview'},
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
                selectionchange: this.onAddValidationRuleSetsSelectionChange
            },
            'validation-add-rulesets button[action=addValidationRuleSets]': {
                click: this.onAddValidationRuleSets
            },
            'validation-add-rulesets button[action=uncheckAll]': {
                click: this.onUncheckAll
            },
            'validation-rules-overview validation-rulesets-grid': {
                selectionchange: this.onValidationRuleSetsSelectionChange
            }
        });
    },

    showValidationRuleSetsOverview: function (deviceTypeId, deviceConfigId) {
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

                // Load the store in asynchronously.
                me.getDeviceConfigValidationRuleSetsStore().load();

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
        var view = this.getValidationRuleSetsOverview(),
            selection = grid.view.getSelectionModel().getSelection();

        if (selection.length > 0) {
            view.updateValidationRuleSet(selection[0]);
        }
    },

    onAddValidationRuleSetsSelectionChange: function (grid) {
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
            view.updateValidationRuleSet(selection[0]);
        }
    },

    onAddValidationRuleSets: function () {
        var me = this,
            view = me.getAddValidationRuleSets(),
            grid = me.getAddValidationRuleSetsGrid(),
            selection = grid.view.getSelectionModel().getSelection(),
            url = '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/validationrulesets',
            loadMask = Ext.create('Ext.LoadMask', {
                target: view
            }),
            ids = [];

        Ext.Array.each(selection, function (item) {
            ids.push(item.internalId);
        });

        loadMask.show();
        Ext.Ajax.request({
            url: url,
            method: 'POST',
            jsonData: Ext.encode(ids),
            success: function () {
                location.href = '#/administration/devicetypes/'
                    + me.deviceTypeId + '/deviceconfigurations/'
                    + me.deviceConfigId + '/validationrulesets';

                var message = Uni.I18n.translatePlural(
                    'validation.ruleSetAdded',
                    selection.length,
                    'MDC',
                    'Succesfully added validation rule sets.'
                );

                me.getApplication().fireEvent('acknowledge', message);
            },
            failure: function (response) {
                if (response.status === 400) {
                    var result = Ext.decode(response.responseText, true),
                        title = Uni.I18n.translate('general.failedToAdd', 'MDC', 'Failed to add'),
                        message = Uni.I18n.translatePlural(
                            'validation.failedToAddMessage',
                            selection.length,
                            'MDC',
                            'Validation rule sets could not be added. There was a problem accessing the database'
                        );

                    if (result !== null) {
                        title = result.error;
                        message = result.message;
                    }

                    me.getApplication().getController('Uni.controller.Error').showError(title, message);
                }
            },
            callback: function () {
                loadMask.destroy();
            }
        });
    },

    onUncheckAll: function () {
        var grid = this.getAddValidationRuleSetsGrid();
        grid.getView().getSelectionModel().deselectAll();
    }
});

Ext.define('Mdc.controller.setup.RuleDeviceConfigurations', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.store.RuleDeviceConfigurations',
        'Cfg.store.ValidationRuleSets',
        'Mdc.store.RuleDeviceConfigurationsNotLinked'
    ],

    models: [
        'Mdc.model.RuleDeviceConfiguration'
    ],

    views: [
        'setup.ruledeviceconfiguration.RuleDeviceConfigurationBrowse',
        'setup.ruledeviceconfiguration.RuleDeviceConfigurationGrid',
        'setup.ruledeviceconfiguration.RuleDeviceConfigurationAdd',
        'setup.ruledeviceconfiguration.RuleDeviceConfigurationPreview',
        'setup.ruledeviceconfiguration.RuleDeviceConfigurationAddGrid',
        'setup.ruledeviceconfiguration.RuleAddDeviceConfigurationActionMenu'
    ],

    refs: [
        {ref: 'ruleDeviceConfigurationBrowsePanel', selector: 'ruleDeviceConfigurationBrowse'},
        {ref: 'ruleDeviceConfigurationAddPanel', selector: 'rule-device-configuration-add'}
    ],

    ruleSetId: null,

    init: function () {
        this.control({
            'rule-device-configuration-grid': {
                select: this.loadDetails
            },
            'rule-device-configuration-add rule-device-configuration-add-grid': {
                allitemsadd: this.onAllDeviceConfigurationsAdd,
                selecteditemsadd: this.onSelectedDeviceConfigurationsAdd
            },
            'rule-device-configuration-action-menu': {
                click: this.chooseAction
            }
        });
    },

    showDeviceConfigView: function (ruleSetId) {
        var me = this,
            ruleDeviceConfigStore = me.getStore('Mdc.store.RuleDeviceConfigurations'),
            ruleSetsStore = me.getStore('Cfg.store.ValidationRuleSets');
        ruleDeviceConfigStore.getProxy().setExtraParam('ruleSetId', ruleSetId);

        me.ruleSetId = ruleSetId;

        ruleSetsStore.load({
            params: {
                id: ruleSetId
            },
            callback: function () {
                var ruleSet = ruleSetsStore.getById(parseInt(ruleSetId)),
                    widget = Ext.widget('ruleDeviceConfigurationBrowse', {ruleSetId: ruleSetId});
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('#stepsMenu #ruleSetOverviewLink').setText(ruleSet.get('name'));
                widget.down('#deviceConfigLink').show();
                me.getApplication().fireEvent('loadRuleSet', ruleSet);
            }
        });
    },

    showAddDeviceConfigView: function (ruleSetId) {
        var me = this,
            ruleDeviceConfigNotLinkedStore = me.getStore('Mdc.store.RuleDeviceConfigurationsNotLinked'),
            ruleSetsStore = me.getStore('Cfg.store.ValidationRuleSets'),
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('rule-device-configuration-add', {ruleSetId: router.arguments.ruleSetId});

        me.ruleSetId = ruleSetId;

        if (widget.down('#addDeviceConfigGrid')) {
            widget.down('#addDeviceConfigGrid').getStore().removeAll();
        }

        me.getApplication().fireEvent('changecontentevent', widget);
        ruleDeviceConfigNotLinkedStore.getProxy().setExtraParam('ruleSetId', router.arguments.ruleSetId);
        ruleDeviceConfigNotLinkedStore.load(function () {
            ruleSetsStore.load({
                params: {
                    id: router.arguments.ruleSetId
                },
                callback: function () {
                    var ruleSet = ruleSetsStore.getById(parseInt(router.arguments.ruleSetId));
                    me.getApplication().fireEvent('loadRuleSet', ruleSet);
                }
            });
        });
    },

    loadDetails: function (rowModel, record) {
        var me = this,
            itemForm = me.getRuleDeviceConfigurationBrowsePanel().down('rule-device-configuration-preview');
        itemForm.loadRecord(record);
        itemForm.setTitle(record.get('config_name'));
    },

    onAllDeviceConfigurationsAdd: function () {
        this.addDeviceConfigurations(true, []);
    },

    onSelectedDeviceConfigurationsAdd: function (selection) {
        this.addDeviceConfigurations(false, selection);
    },

    addDeviceConfigurations: function (allPressed, selection) {
        var me = this,
            url = '/api/dtc/validationruleset/' + me.ruleSetId + '/deviceconfigurations',
            ids = [];

        if (!allPressed) {
            Ext.Array.each(selection, function (item) {
                ids.push(item.data.config.id);
            });
        }

        me.getRuleDeviceConfigurationAddPanel().setLoading();
        Ext.Ajax.request({
            url: url,
            method: 'POST',
            timeout: 120000,
            params: {
                all: allPressed
            },
            jsonData: Ext.encode(ids),
            success: function () {
                location.href = '#/administration/validation/rulesets/' + me.ruleSetId + '/deviceconfigurations';
                var message = Uni.I18n.translatePlural(
                    'validation.deviceconfiguration.addSuccess',
                    selection.length,
                    'CFG',
                    'Succesfully added device configurations'
                );
                me.getApplication().fireEvent('acknowledge', message);
            },
            callback: function () {
                me.getRuleDeviceConfigurationAddPanel().setLoading(false);
            }
        });
    },

    chooseAction: function (menu, item) {
        var record;
        record = menu.record || this.getRuleDeviceConfigurationBrowsePanel().down('rule-device-configuration-grid').getSelectionModel().getLastSelected();
        location.href = '#/administration/devicetypes/' + record.data.deviceType.id + '/deviceconfigurations/' + record.data.config.id;
    }
});


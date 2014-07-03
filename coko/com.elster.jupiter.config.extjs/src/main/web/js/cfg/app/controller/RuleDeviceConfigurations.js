Ext.define('Cfg.controller.RuleDeviceConfigurations', {
    extend: 'Ext.app.Controller',

    stores: [
        'Cfg.store.RuleDeviceConfigurations',
        'Cfg.store.ValidationRuleSets',
        'Cfg.store.RuleDeviceConfigurationsNotLinked'
    ],

    requires: [
    ],

    models: [
        'Cfg.model.RuleDeviceConfiguration'
    ],

    views: [
        'deviceconfiguration.RuleDeviceConfigurationBrowse',
        'deviceconfiguration.RuleDeviceConfigurationGrid',
        'deviceconfiguration.RuleDeviceConfigurationAdd'
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
            'rule-device-configuration-add grid': {
                selectionchange: this.countSelectedDeviceConfigurations
            },
            'rule-device-configuration-add button[action=add]': {
                click: this.addDeviceConfigurations
            }
        });
    },

    showDeviceConfigView: function (ruleSetId) {
        var me = this,
            ruleDeviceConfigStore = me.getStore('Cfg.store.RuleDeviceConfigurations'),
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
                widget.down('#stepsMenu').setTitle(ruleSet.get('name'));
                me.getApplication().fireEvent('loadRuleSet', ruleSet);
            }
        });
    },

    showAddDeviceConfigView: function () {
        var me = this,
            ruleDeviceConfigNotLinkedStore = me.getStore('Cfg.store.RuleDeviceConfigurationsNotLinked'),
            ruleSetsStore = me.getStore('Cfg.store.ValidationRuleSets');
        ruleDeviceConfigNotLinkedStore.getProxy().setExtraParam('ruleSetId', me.ruleSetId);
        ruleDeviceConfigNotLinkedStore.load({
            callback: function () {
                var widget = Ext.widget('rule-device-configuration-add', {ruleSetId: me.ruleSetId});
                me.getApplication().fireEvent('changecontentevent', widget);
                var textLabel = me.getRuleDeviceConfigurationAddPanel().down('#countLabel'),
                    grid = me.getRuleDeviceConfigurationAddPanel().down('#addDeviceConfigGrid');
                textLabel.setText('No configurations selected');
                if (this.getCount() < 1) {
                    grid.hide();
                    me.getRuleDeviceConfigurationAddPanel().down('#addDeviceConfigToolbar').hide();
                    grid.next().show();
                }
                ruleSetsStore.load({
                    params: {
                        id: me.ruleSetId
                    },
                    callback: function () {
                        var ruleSet = ruleSetsStore.getById(parseInt(me.ruleSetId));
                        widget.down('#stepsMenu').setTitle(ruleSet.get('name'));
                        me.getApplication().fireEvent('loadRuleSet', ruleSet);
                    }
                });
            }
        });
    },

    loadDetails: function (rowModel, record) {
        var me = this,
            itemForm = me.getRuleDeviceConfigurationBrowsePanel().down('rule-device-configuration-preview');
        itemForm.loadRecord(record);
        itemForm.setTitle(record.get('config_name'));
    },

    countSelectedDeviceConfigurations: function (grid) {
        var me = this,
            textLabel = me.getRuleDeviceConfigurationAddPanel().down('#countLabel'),
            chosenLogBookCount = grid.view.getSelectionModel().getSelection().length;
        textLabel.setText(chosenLogBookCount >= 1 ? (chosenLogBookCount + (chosenLogBookCount > 1 ? ' configurations' : ' configuration') + ' selected') : 'No configurations selected');
    },

    addDeviceConfigurations: function () {

    }
});

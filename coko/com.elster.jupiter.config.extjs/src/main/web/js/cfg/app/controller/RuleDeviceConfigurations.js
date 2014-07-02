Ext.define('Cfg.controller.RuleDeviceConfigurations', {
    extend: 'Ext.app.Controller',

    stores: [
        'Cfg.store.RuleDeviceConfigurations',
        'Cfg.store.ValidationRuleSets'
    ],

    requires: [
    ],

    models: [
        'Cfg.model.RuleDeviceConfiguration'
    ],

    views: [
        'deviceconfiguration.RuleDeviceConfigurationBrowse'
    ],

    refs: [
    ],

    ruleSetId: null,

    init: function () {
        this.control({
        });
    },

    showDeviceConfigView: function (ruleSetId) {
        var me = this,
            ruleDeviceConfigStore = me.getStore('Cfg.store.RuleDeviceConfigurations'),
            ruleSetsStore = Ext.create('Cfg.store.ValidationRuleSets');
        me.ruleSetId = ruleSetId;
        console.log(ruleSetId);
        ruleSetsStore.load({
            params: {
                id: ruleSetId
            },
            callback: function () {
                var ruleSet = ruleSetsStore.getById(ruleSetId),
                    widget = Ext.widget('ruleDeviceConfigurationBrowse', {ruleSetId: ruleSetId});
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('#stepsMenu').setTitle(ruleSet.get('name'));
                me.getApplication().fireEvent('loadRuleSet', ruleSet);
            }
        });
    }
});

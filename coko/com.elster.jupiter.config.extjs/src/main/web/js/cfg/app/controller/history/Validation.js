Ext.define('Cfg.controller.history.Validation', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'administration',

    init: function () {
        var me = this;

        crossroads.addRoute('administration/rules', function (id) {
            me.getApplication().getController('Cfg.controller.Validation').showRuleSets();
        });
        crossroads.addRoute('administration/rules/{id}', function (id) {
            me.getApplication().getController('Cfg.controller.Validation').showRules(id);
        });
        crossroads.addRoute('administration/overview/{id}', function (id) {
            me.getApplication().getController('Cfg.controller.Validation').showRuleSetOverview(id);
        });
        crossroads.addRoute('administration/createset', function () {
            me.getApplication().getController('Cfg.controller.Validation').newRuleSet();
        });
        crossroads.addRoute('administration/addRule/{id}', function (id) {
            me.getApplication().getController('Cfg.controller.Validation').addRule(id);
        });

        this.callParent(arguments);
    }
});
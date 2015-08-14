Ext.define('Cfg.view.validation.RuleSetPreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.validation-ruleset-preview',
    frame: true,
    requires: [
        'Cfg.model.ValidationRuleSet'
    ],

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    title: '',

    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },

    items: [
        {
            name: 'name',
            fieldLabel: Uni.I18n.translate('general.name', 'CFG', 'Name')
        },
        {
            name: 'description',
            fieldLabel: Uni.I18n.translate('general.description', 'CFG', 'Description')
        },
        {
            name: 'numberOfRules',
            fieldLabel: Uni.I18n.translate('validation.numberOfRules', 'CFG', 'Number of rules')
        },
        {
            name: 'numberOfInactiveRules',
            fieldLabel: Uni.I18n.translate('validation.numberOfInActiveRules', 'CFG', 'Number of inactive rules')
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    },

    updateValidationRuleSet: function (ruleSet) {
        this.setTitle(ruleSet.get('name'));
        this.loadRecord(ruleSet);
        this.destroy();
    }
});

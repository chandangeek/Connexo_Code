Ext.define('Imt.metrologyconfiguration.view.validation.RuleSetActionMenu', {
    extend: 'Ext.menu.Menu',
    xtype: 'validation-ruleset-actionmenu',

    plain: true,
    border: false,
    shadow: false,

    items: [
        {
            text: Uni.I18n.translate('general.view', 'IMT', 'View'),
            itemId: 'viewRuleSet',
            action: 'viewRuleSet'
        },
        {
            text: Uni.I18n.translate('general.remove', 'IMT', 'Remove'),
            privileges: Cfg.privileges.Validation.deviceConfiguration,
            itemId: 'removeRuleSet',
            action: 'removeRuleSet'
        }
    ]
});

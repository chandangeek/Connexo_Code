Ext.define('Mdc.view.setup.validation.RuleSetActionMenu', {
    extend: 'Ext.menu.Menu',
    xtype: 'validation-ruleset-actionmenu',

    plain: true,
    border: false,
    shadow: false,

    items: [
        {
            text: Uni.I18n.translate('general.view', 'MDC', 'View'),
            itemId: 'viewRuleSet'
        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            itemId: 'removeRuleSet'
        }
    ]
});

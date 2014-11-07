Ext.define('Mdc.view.setup.validation.RuleSetActionMenu', {
    extend: 'Ext.menu.Menu',
    xtype: 'validation-ruleset-actionmenu',

    plain: true,
    border: false,
    shadow: false,

    items: [
        {
            text: Uni.I18n.translate('general.view', 'MDC', 'View'),
            itemId: 'viewRuleSet',
            action: 'viewRuleSet'
        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
            itemId: 'removeRuleSet',
            action: 'removeRuleSet'
        }
    ]
});

Ext.define('Cfg.view.validation.RuleSetActionMenu', {
    extend: 'Ext.menu.Menu',
    xtype: 'ruleset-action-menu',
    itemId: 'ruleset-action-menu',

    plain: true,
    border: false,
    shadow: false,

    items: [
        {
            itemId: 'editRuleSet',
            text: Uni.I18n.translate('general.edit', 'CFG', 'Edit'),
            action: 'editRuleSet'
        },
        {
            itemId: 'deleteRuleSet',
            text: Uni.I18n.translate('general.remove', 'CFG', 'Remove'),
            action: 'deleteRuleSet'
        }
    ]
});

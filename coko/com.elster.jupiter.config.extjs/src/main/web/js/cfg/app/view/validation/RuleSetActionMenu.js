Ext.define('Cfg.view.validation.RuleSetActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.ruleset-action-menu',
    plain: true,
    border: false,
    itemId: 'ruleset-action-menu',
    shadow: false,
    items: [
        {
            itemId: 'editRuleSet',
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            action: 'editRuleSet'
        },
        {
            itemId: 'deleteRuleSet',
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            action: 'deleteRuleSet'
        }
    ]
});

Ext.define('Cfg.view.validation.RuleActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.rule-action-menu',
    plain: true,
    border: false,
    itemId: 'rule-action-menu',
    shadow: false,
    items: [
        {
            itemId: 'editRule',
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            action: 'editRule'
        },
        {
            itemId: 'deleteRule',
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            action: 'deleteRule'
        }
    ]
});


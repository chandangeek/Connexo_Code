Ext.define('Est.estimationrulesets.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.estimation-rule-sets-action-menu',
    plain: true,
    border: false,
    shadow: false,
    itemId: 'rule-set-action-menu',
    defaultAlign: 'tr-br?',
    items: [
        {
            itemId: 'edit',
            text: Uni.I18n.translate('general.edit', 'EST', 'Edit'),
            action: 'edit'
        },
        {
            itemId: 'remove',
            text: Uni.I18n.translate('general.remove', 'EST', 'Remove'),
            action: 'remove'
        }
    ]
});

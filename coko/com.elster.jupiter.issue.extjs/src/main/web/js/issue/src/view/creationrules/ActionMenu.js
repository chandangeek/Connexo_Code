Ext.define('Isu.view.creationrules.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.creation-rule-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'edit',
            text: Uni.I18n.translate('general.edit', 'ISU', 'Edit'),
            action: 'edit'
        },
        {
            itemId: 'remove',
            text:  Uni.I18n.translate('general.remove', 'ISU', 'Remove'),
            action: 'remove'
        }
    ]
});

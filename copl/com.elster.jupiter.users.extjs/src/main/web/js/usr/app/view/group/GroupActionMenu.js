Ext.define('Usr.view.group.GroupActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.group-action-menu',
    plain: true,
    border: false,
    itemId: 'group-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'USM', 'Edit'),
            itemId: 'editGroup',
            action: 'edit'
        }
    ]
});

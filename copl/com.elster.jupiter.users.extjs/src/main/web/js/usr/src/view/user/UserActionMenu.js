Ext.define('Usr.view.user.UserActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.user-action-menu',
    plain: true,
    border: false,
    itemId: 'user-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'USR', 'Edit'),
            itemId: 'editUser',
            action: 'edit'
        },
        {
            itemId: 'activate-user',
            text: Uni.I18n.translate('general.activate', 'USR', 'Activate'),
            action: 'activate'
        },
        {
            itemId: 'deactivate-user',
            text: Uni.I18n.translate('general.deactivate', 'USR', 'Deactivate'),
            action: 'activate'
        }
    ]
});

Ext.define('Usr.view.user.UserActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.user-action-menu',
    plain: true,
    border: false,
    itemId: 'user-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'USM', 'Edit'),
            itemId: 'editUser',
            action: 'edit',
            hidden: Uni.Auth.hasNoPrivilege('privilege.update.user')
        }
    ]
});

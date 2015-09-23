Ext.define('Usr.view.userDirectory.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.usr-user-directory-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'edit-user-directory',
            text: Uni.I18n.translate('general.edit', 'USR', 'Edit'),
            privileges: Usr.privileges.Users.admin,
            action: 'editUserDirectory'
        },
        {
            itemId: 'synchronize-user-directory',
            text: Uni.I18n.translate('userDirectories.synchronize', 'USR', 'Synchronize'),
            privileges: Usr.privileges.Users.admin,
            action: 'synchronizeUserDirectory'
        },
        {
            itemId: 'remove-user-directory',
            text: Uni.I18n.translate('general.remove', 'USR', 'Remove'),
            privileges: Usr.privileges.Users.admin,
            action: 'removeUserDirectory'
        },
        {
            itemId: 'set-as-default-user-directory',
            text: Uni.I18n.translate('userDirectories.setAsDefault', 'USR', 'Set as default'),
            privileges: Usr.privileges.Users.admin,
            action: 'setAsDefault'
        }
    ]
});
Ext.define('Usr.view.workgroup.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.usr-workgroup-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'edit-workgroup',
            text: Uni.I18n.translate('general.edit', 'USR', 'Edit'),
            privileges: Usr.privileges.Users.admin,
            action: 'editWorkgroup'
        },
        {
            itemId: 'remove-workgroup',
            text: Uni.I18n.translate('general.remove', 'USR', 'Remove'),
            privileges: Usr.privileges.Users.admin,
            action: 'removeWorkgroup'
        }
    ]
});
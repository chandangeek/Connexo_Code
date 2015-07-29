Ext.define('Apr.view.appservers.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.appservers-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'edit-appserver',
            text: Uni.I18n.translate('general.edit', 'APR', 'Edit'),
            privileges: Apr.privileges.AppServer.admin,
            action: 'editAppServer'
        },
        {
            itemId: 'remove-appserver',
            text: Uni.I18n.translate('general.remove', 'UNI', 'Remove'),
            privileges: Apr.privileges.AppServer.admin,
            action: 'removeAppServer'
        },
        {
            itemId: 'activate-appserver',
            text: Uni.I18n.translate('general.activate', 'APR', 'Activate'),
            privileges: Apr.privileges.AppServer.admin,
            action: 'activateAppServer'
        }
    ]
});
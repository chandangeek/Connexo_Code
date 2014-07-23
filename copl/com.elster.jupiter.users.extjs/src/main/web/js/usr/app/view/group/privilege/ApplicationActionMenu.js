Ext.define('Usr.view.group.privilege.ApplicationActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.application-action-menu',
    plain: true,
    border: false,
    itemId: 'application-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('privilege.no.access', 'USM', 'No access'),
            itemId: 'privilegeNoAccess',
            action: 'privilegeNoAccess'
        },
        {
            text: Uni.I18n.translate('privilege.full.control', 'USM', 'Full control'),
            itemId: 'privilegeFullControl',
            action: 'privilegeFullControl'
        }
    ]
});

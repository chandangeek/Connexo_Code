Ext.define('Usr.view.group.privilege.ApplicationActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.application-action-menu',
    plain: true,
    border: false,
    itemId: 'application-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('privilege.noAccess', 'USR', 'No access'),
            icon: '../sky/build/resources/images/grid/drop-no.png',
            itemId: 'privilegeNoAccess',
            action: 'privilegeNoAccess'
        },
        {
            text: Uni.I18n.translate('privilege.fullControl', 'USR', 'Full control'),
            icon: '../sky/build/resources/images/grid/drop-yes.png',
            itemId: 'privilegeFullControl',
            action: 'privilegeFullControl'
        }
    ]
});

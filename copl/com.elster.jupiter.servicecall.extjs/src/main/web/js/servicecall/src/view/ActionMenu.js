Ext.define('Scs.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.scs-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'change-log-level-scs',
            text: Uni.I18n.translate('general.changeLogLevel', 'SCS', 'Change log level'),
            //privileges: Apr.privileges.AppServer.admin,
            action: 'test'
        }
    ]
});
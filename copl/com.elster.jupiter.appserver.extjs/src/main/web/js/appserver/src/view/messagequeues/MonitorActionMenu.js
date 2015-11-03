Ext.define('Apr.view.messagequeues.MonitorActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.monitor-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'clear-error-queue',
            text: Uni.I18n.translate('general.clearErrorQueue', 'APR', 'Clear error queue'),
            privileges: Apr.privileges.AppServer.admin,
            action: 'clearErrorQueue'
        }
    ]
});

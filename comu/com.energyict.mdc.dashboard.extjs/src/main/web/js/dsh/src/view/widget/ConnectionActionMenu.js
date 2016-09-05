Ext.define('Dsh.view.widget.ConnectionActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.connection-action-menu',
    items: [
        {
            text: Uni.I18n.translate('general.runNow', 'DSH', 'Run now'),
            privileges : Mdc.privileges.Device.operateDeviceCommunication,
            action: 'run'
        },
        {
            text: Uni.I18n.translate('general.viewHistory', 'DSH', 'View history'),
            action: 'viewHistory'
        },
        {
            text: Uni.I18n.translate('general.viewLog', 'DSH', 'View log'),
            action: 'viewLog'
        }
    ]
});


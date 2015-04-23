Ext.define('Dsh.view.widget.ConnectionActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.connection-action-menu',
    items: [
        {
            text: 'Run now',
            privileges : Mdc.privileges.Device.operateDeviceCommunication,
            action: 'run'
        },
        {
            text: 'View history',
            action: 'viewHistory'
        },
        {
            text: 'View log',
            action: 'viewLog'
        }
    ]
});


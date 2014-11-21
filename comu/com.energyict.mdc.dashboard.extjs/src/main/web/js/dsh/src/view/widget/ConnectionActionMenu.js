Ext.define('Dsh.view.widget.ConnectionActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.connection-action-menu',
    items: [
        {
            text: 'View connection history',
            action: 'viewHistory'
        },
        {
            text: 'View connection log',
            action: 'viewLog'
        }
    ]
});

